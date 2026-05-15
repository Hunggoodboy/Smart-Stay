document.addEventListener('DOMContentLoaded', function() {
    const reportType = document.getElementById('reportType');
    const reportYear = document.getElementById('reportYear');
    const reportMonth = document.getElementById('reportMonth');
    const monthControl = document.getElementById('monthControl');
    const btnFilter = document.getElementById('btnFilter');
    const loadingOverlay = document.getElementById('loadingOverlay');
    
    // Stats elements
    const totalRevenue = document.getElementById('totalRevenue');
    const totalBills = document.getElementById('totalBills');
    const avgRevenue = document.getElementById('avgRevenue');
    const revenueSub = document.getElementById('revenueSub');
    const tableBody = document.getElementById('revenueTableBody');

    let revenueChart = null;

    // Initialize years
    const currentYear = new Date().getFullYear();
    for (let i = currentYear; i >= 2020; i--) {
        const option = document.createElement('option');
        option.value = i;
        option.textContent = i;
        reportYear.appendChild(option);
    }

    reportType.addEventListener('change', function() {
        monthControl.style.display = this.value === 'monthly' ? 'block' : 'none';
    });

    btnFilter.addEventListener('click', loadReport);

    async function loadReport() {
        const type = reportType.value;
        const year = reportYear.value;
        const month = reportMonth.value;
        const token = localStorage.getItem('smartstay_token');

        loadingOverlay.classList.add('active');

        try {
            let data;
            if (type === 'year') {
                data = await fetchYearlyData(year, token);
            } else {
                const res = await fetch(`/api/revenue-report/monthly?year=${year}&month=${month}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                });
                data = await res.json();
            }

            updateDashboard(data, type);
        } catch (error) {
            console.error('Error fetching revenue data:', error);
            alert('Không thể tải dữ liệu báo cáo. Vui lòng thử lại sau.');
        } finally {
            loadingOverlay.classList.remove('active');
        }
    }

    async function fetchYearlyData(year, token) {
        // Since the API only returns a total for the year, 
        // if we want a month-by-month breakdown for the chart, 
        // we'd need to fetch all months. 
        // For now, let's just fetch the year summary and 
        // maybe try to fetch months in parallel for the chart.
        
        const yearRes = await fetch(`/api/revenue-report/year?year=${year}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const yearSummary = await yearRes.json();
        
        // Fetch months for the chart
        const monthsData = await Promise.all(
            Array.from({length: 12}, (_, i) => 
                fetch(`/api/revenue-report/monthly?year=${year}&month=${i+1}`, {
                    headers: { 'Authorization': `Bearer ${token}` }
                }).then(r => r.json())
            )
        );

        return {
            summary: yearSummary,
            months: monthsData
        };
    }

    function updateDashboard(data, type) {
        if (type === 'monthly') {
            totalRevenue.textContent = fmtMoney(data.totalRevenue);
            totalBills.textContent = data.paidCount;
            avgRevenue.textContent = fmtMoney(data.totalRevenue); // For single month, avg is the same
            revenueSub.textContent = `Trong tháng ${data.month}/${data.year}`;
            
            renderTable([data]);
            renderChart([data], 'monthly');
        } else {
            totalRevenue.textContent = fmtMoney(data.summary.totalRevenue);
            totalBills.textContent = data.summary.paidCount;
            avgRevenue.textContent = fmtMoney(data.summary.totalRevenue / 12);
            revenueSub.textContent = `Trong năm ${data.summary.year}`;
            
            renderTable(data.months);
            renderChart(data.months, 'year');
        }
    }

    function renderTable(list) {
        tableBody.innerHTML = '';
        list.forEach((item, index) => {
            if (item.totalRevenue === 0 && list.length > 1) return; // Skip empty months in yearly view unless all empty
            
            const tr = document.createElement('tr');
            const timeLabel = item.month ? `Tháng ${item.month}/${item.year}` : `Năm ${item.year}`;
            
            // Calculate growth if possible
            let growth = '—';
            if (index > 0 && list[index-1].totalRevenue > 0) {
                const g = ((item.totalRevenue - list[index-1].totalRevenue) / list[index-1].totalRevenue) * 100;
                growth = `<span style="color: ${g >= 0 ? '#16a34a' : '#dc2626'}">${g >= 0 ? '+' : ''}${g.toFixed(1)}%</span>`;
            }

            tr.innerHTML = `
                <td style="font-weight: 700;">${timeLabel}</td>
                <td style="font-weight: 800; color: var(--blue-700);">${fmtMoney(item.totalRevenue)}</td>
                <td>${item.paidCount} hóa đơn</td>
                <td style="font-weight: 600;">${growth}</td>
            `;
            tableBody.appendChild(tr);
        });

        if (tableBody.innerHTML === '') {
            tableBody.innerHTML = '<tr><td colspan="4" style="text-align: center; padding: 2rem; color: var(--gray-400);">Không có dữ liệu trong khoảng thời gian này</td></tr>';
        }
    }

    function renderChart(list, type) {
        const ctx = document.getElementById('revenueChart').getContext('2d');
        
        if (revenueChart) {
            revenueChart.destroy();
        }

        const labels = list.map(item => item.month ? `T${item.month}` : item.year);
        const data = list.map(item => item.totalRevenue);
        const rentData = list.map(item => item.rentAmount);
        const utilityData = list.map(item => item.utilityAmount);

        revenueChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Tiền phòng',
                        data: rentData,
                        backgroundColor: 'rgba(37, 99, 235, 0.7)',
                        borderRadius: 6,
                        stack: 'combined'
                    },
                    {
                        label: 'Tiền dịch vụ',
                        data: utilityData,
                        backgroundColor: 'rgba(14, 165, 233, 0.7)',
                        borderRadius: 6,
                        stack: 'combined'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            usePointStyle: true,
                            font: { family: 'Nunito', weight: '700' }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        padding: 12,
                        titleFont: { family: 'Nunito', size: 14, weight: '800' },
                        bodyFont: { family: 'Nunito', size: 13 },
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': ' + fmtMoney(context.raw);
                            }
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: '#f1f5f9' },
                        ticks: {
                            font: { family: 'Nunito', weight: '600' },
                            callback: value => value.toLocaleString('vi-VN')
                        }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { font: { family: 'Nunito', weight: '700' } }
                    }
                }
            }
        });
    }

    function fmtMoney(v) {
        if (v == null) return '0 VNĐ';
        return Number(v).toLocaleString('vi-VN') + ' VNĐ';
    }

    // Initial load
    loadReport();
});

document.addEventListener('DOMContentLoaded', function () {
    const token = localStorage.getItem('smartstay_token');
    const currentYear = new Date().getFullYear();

    const statTotalUsers = document.getElementById('statTotalUsers');
    const statTotalRooms = document.getElementById('statTotalRooms');
    const statPending = document.getElementById('statPending');
    const statPosts = document.getElementById('statPosts');
    const userListEl = document.getElementById('userList');
    const activityListEl = document.getElementById('activityList');
    const timestampEl = document.getElementById('timestamp');

    function handleAdminLogout() {
        localStorage.removeItem('smartstay_token');
        localStorage.removeItem('smartstay_user');
        document.cookie = 'smartstay_token=; Max-Age=0; path=/';
        window.location.href = '/login';
    }

    window.handleAdminLogout = handleAdminLogout;

    if (!token) {
        window.location.href = '/login';
        return;
    }

    const now = new Date();
    timestampEl.textContent = 'Cập nhật: ' + now.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
        + ', ' + now.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });

    let activityChart = null;
    let userDistChart = null;

    loadStats();

    async function loadStats() {
        try {
            const res = await fetch(`/api/admin/system-stats?year=${currentYear}`, {
                headers: { Authorization: `Bearer ${token}` }
            });

            if (res.status === 401 || res.status === 403) {
                handleAdminLogout();
                return;
            }

            if (!res.ok) throw new Error('API error: ' + res.status);

            const data = await res.json();
            renderOverview(data);
            renderActivityChart(data.monthlyStats || []);
            renderUserDistribution(data);
            renderRecentUsers(data.recentUsers || []);
            renderRecentActivity(data.recentUsers || []);
        } catch (err) {
            console.error('Lỗi tải thống kê:', err);
            statTotalUsers.textContent = '!';
            statTotalRooms.textContent = '!';
            statPending.textContent = '!';
            statPosts.textContent = '!';
            userListEl.innerHTML = '<div class="admin-state">Không tải được dữ liệu người dùng.</div>';
            activityListEl.innerHTML = '<div class="admin-state">Không tải được hoạt động hệ thống.</div>';
        }
    }

    function renderOverview(data) {
        statTotalUsers.textContent = Number(data.totalUsers || 0).toLocaleString('vi-VN');
        statTotalRooms.textContent = Number(data.totalRooms || 0).toLocaleString('vi-VN');
        statPending.textContent = Number(data.pendingApprovals || 0).toLocaleString('vi-VN');
        statPosts.textContent = Number(data.postsNeedReview || 0).toLocaleString('vi-VN');
    }

    function renderActivityChart(monthlyStats) {
        const ctx = document.getElementById('activityChart').getContext('2d');
        if (activityChart) activityChart.destroy();

        const labels = monthlyStats.map(m => 'T' + m.month);
        const regData = monthlyStats.map(m => m.newRegistrations);
        const rentData = monthlyStats.map(m => m.roomsRented);
        const revData = monthlyStats.map(m => m.revenue / 1000000);

        activityChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels,
                datasets: [
                    {
                        label: 'Đăng ký mới',
                        data: regData,
                        borderColor: '#2563eb',
                        backgroundColor: 'rgba(37,99,235,0.10)',
                        borderWidth: 2.5,
                        pointRadius: 4,
                        pointBackgroundColor: '#2563eb',
                        tension: 0.4
                    },
                    {
                        label: 'Phòng được thuê',
                        data: rentData,
                        borderColor: '#0ea5e9',
                        backgroundColor: 'rgba(14,165,233,0.10)',
                        borderWidth: 2.5,
                        pointRadius: 4,
                        pointBackgroundColor: '#0ea5e9',
                        tension: 0.4
                    },
                    {
                        label: 'Doanh thu (triệu đ)',
                        data: revData,
                        borderColor: '#22c55e',
                        backgroundColor: 'rgba(34,197,94,0.10)',
                        borderWidth: 2.5,
                        pointRadius: 4,
                        pointBackgroundColor: '#22c55e',
                        borderDash: [6, 3],
                        tension: 0.4
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
                            color: '#334155',
                            usePointStyle: true,
                            font: { family: 'Nunito', weight: '700', size: 12 }
                        }
                    },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#e0f2fe',
                        bodyColor: '#f8fafc',
                        padding: 12,
                        titleFont: { family: 'Nunito', size: 13, weight: '800' },
                        bodyFont: { family: 'Nunito', size: 12 }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        grid: { color: 'rgba(148,163,184,0.18)' },
                        ticks: { color: '#64748b', font: { family: 'Nunito', weight: '700' } }
                    },
                    x: {
                        grid: { display: false },
                        ticks: { color: '#64748b', font: { family: 'Nunito', weight: '700' } }
                    }
                }
            }
        });
    }

    function renderUserDistribution(data) {
        const ctx = document.getElementById('userDistChart').getContext('2d');
        if (userDistChart) userDistChart.destroy();

        document.getElementById('countCustomer').textContent = Number(data.customerCount || 0).toLocaleString('vi-VN');
        document.getElementById('countLandlord').textContent = Number(data.landlordCount || 0).toLocaleString('vi-VN');
        document.getElementById('countAdmin').textContent = Number(data.adminCount || 0).toLocaleString('vi-VN');
        document.getElementById('verifiedInfo').textContent = `${data.verifiedLandlords || 0} / ${data.totalLandlords || 0}`;

        const pct = data.totalLandlords > 0
            ? Math.round((data.verifiedLandlords / data.totalLandlords) * 100)
            : 0;
        document.getElementById('verifiedPct').textContent = pct + '% đã xác thực';

        userDistChart = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['Khách thuê', 'Chủ nhà', 'Admin'],
                datasets: [{
                    data: [data.customerCount || 0, data.landlordCount || 0, data.adminCount || 0],
                    backgroundColor: ['#2563eb', '#0ea5e9', '#22c55e'],
                    borderColor: '#ffffff',
                    borderWidth: 4,
                    hoverOffset: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                cutout: '65%',
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        backgroundColor: '#0f172a',
                        titleColor: '#e0f2fe',
                        bodyColor: '#f8fafc',
                        padding: 10,
                        bodyFont: { family: 'Nunito', size: 13, weight: '700' }
                    }
                }
            }
        });
    }

    function renderRecentUsers(users) {
        userListEl.innerHTML = '';
        if (!users.length) {
            userListEl.innerHTML = '<div class="admin-state">Chưa có người dùng gần đây.</div>';
            return;
        }

        const roleLabel = { CUSTOMER: 'Khách thuê', LANDLORD: 'Chủ nhà', ADMIN: 'Admin' };
        const roleColors = { CUSTOMER: '#2563eb', LANDLORD: '#0ea5e9', ADMIN: '#22c55e' };

        users.forEach(u => {
            const initials = getInitials(u.fullName);
            const roleTxt = roleLabel[u.role] || u.role;
            const color = roleColors[u.role] || '#64748b';

            let statusBadge;
            if (!u.active) {
                statusBadge = '<span class="badge badge-inactive">Bị khóa</span>';
            } else if (u.role === 'LANDLORD' && u.verified === false) {
                statusBadge = '<span class="badge badge-pending">Chờ duyệt</span>';
            } else {
                statusBadge = '<span class="badge badge-active">Hoạt động</span>';
            }

            const subText = u.role === 'LANDLORD'
                ? roleTxt + ' · ' + (u.verified ? 'Đã xác thực' : 'Chưa xác thực')
                : roleTxt;

            const item = document.createElement('div');
            item.className = 'user-item';
            item.innerHTML = `
                <div class="user-avatar" style="background:${color}">${initials}</div>
                <div class="user-info">
                    <div class="user-name">${u.fullName || 'Không tên'}</div>
                    <div class="user-role">${subText}</div>
                </div>
                ${statusBadge}
            `;
            userListEl.appendChild(item);
        });
    }

    function renderRecentActivity(users) {
        activityListEl.innerHTML = '';

        const activities = [];

        users.forEach(u => {
            if (u.role === 'CUSTOMER') {
                activities.push({
                    icon: '👤',
                    color: '#2563eb',
                    html: `<strong>${u.fullName || 'User'}</strong> đăng ký tài khoản khách thuê mới`,
                    time: u.createdAt
                });
            } else if (u.role === 'LANDLORD') {
                if (u.verified) {
                    activities.push({
                        icon: '✓',
                        color: '#22c55e',
                        html: `<strong>Admin</strong> xác thực chủ nhà <strong>${u.fullName || 'User'}</strong> thành công`,
                        time: u.createdAt
                    });
                } else {
                    activities.push({
                        icon: '⏳',
                        color: '#f59e0b',
                        html: `<strong>${u.fullName || 'User'}</strong> đăng ký chủ nhà, chờ xác thực`,
                        time: u.createdAt
                    });
                }
            }
        });

        activities.slice(0, 8).forEach(act => {
            const item = document.createElement('div');
            item.className = 'activity-item';
            item.innerHTML = `
                <div class="activity-icon" style="background:${act.color}1f; color:${act.color}">${act.icon}</div>
                <div class="activity-content">${act.html}</div>
                <div class="activity-time">${act.time || ''}</div>
            `;
            activityListEl.appendChild(item);
        });

        if (!activities.length) {
            activityListEl.innerHTML = '<div class="admin-state">Chưa có hoạt động.</div>';
        }
    }

    function getInitials(name) {
        if (!name) return '?';
        const parts = name.trim().split(/\s+/);
        if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
        return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
});

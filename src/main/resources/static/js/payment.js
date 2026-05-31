let authToken = null;

    function openQuickLinkModal() {
        const modal = document.getElementById('quicklink-modal');
        if (modal) modal.classList.remove('hidden');
    }

    function closeQuickLinkModal() {
        const modal = document.getElementById('quicklink-modal');
        if (modal) modal.classList.add('hidden');
    }

    async function loadQuickLink() {
        const res = await fetch('/api/payments/vietqr/quicklink', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': authToken ? `Bearer ${authToken}` : ''
            }
        });

        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.message || 'Không tạo được link thanh toán PayOS');
        }

        if (data.status === 'PAID') {
            if (data.orderCode) {
                localStorage.setItem('smartstay_paid_order_code', String(data.orderCode));
            }
            renderPaidReturnState();
            return;
        }

        if (data.checkoutUrl) {
            window.location.href = data.checkoutUrl;
            return;
        }

        const qrImage = document.getElementById('quicklink-image');
        const qrAmount = document.getElementById('quicklink-amount');
        const qrInfo = document.getElementById('quicklink-info');
        const qrAccount = document.getElementById('quicklink-account');
        const qrOpen = document.getElementById('quicklink-open');

        if (qrImage) qrImage.src = data.imageUrl || data.qrCode || '';
        if (qrAmount) {
            const amount = Number(data.amount || 0).toLocaleString('vi-VN');
            qrAmount.textContent = `So tien: ${amount} VND`;
        }
        if (qrInfo) qrInfo.textContent = data.addInfo ? `Noi dung: ${data.addInfo}` : '';
        if (qrAccount) {
            const bank = data.bankId ? data.bankId.toUpperCase() : '';
            const acct = data.accountNoMasked || '';
            const name = data.accountName || '';
            qrAccount.textContent = `Tai khoan: ${bank} ${acct} ${name}`.trim();
        }
        if (qrOpen) {
            qrOpen.href = data.imageUrl || data.checkoutUrl || '#';
        }

        openQuickLinkModal();
    }

    document.addEventListener("DOMContentLoaded", function () {
        authToken = localStorage.getItem('smartstay_token');
        const paymentReturn = getPayOsReturnState();
        if (paymentReturn.paid && paymentReturn.orderCode) {
            localStorage.setItem('smartstay_paid_order_code', String(paymentReturn.orderCode));
        }
        fetch('/api/utility-bills/newest', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': authToken ? `Bearer ${authToken}` : ''
            }
        })
            .then(response => {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(resData => {
                const data = resData.data ? resData.data : (resData.body ? resData.body : resData);

                if (data.billingMonth) {
                    document.getElementById('billingMonth').textContent = 'Chi tiết hóa đơn Tháng ' + data.billingMonth;
                }

                if (data.status) {
                    const statusChip = document.getElementById('payment-status-chip');
                    const statusDot = document.getElementById('payment-status-dot');
                    const statusPreview = document.getElementById('statusPreview');
                    const payNowButton = document.getElementById('pay-now-button');
                    const statusDetail = document.getElementById('status');

                    const savedPaidOrderCode = Number(localStorage.getItem('smartstay_paid_order_code') || 0);
                    const isPaid = data.status === 'PAID'
                        || data.status === 'DA_THANH_TOAN'
                        || (savedPaidOrderCode && Number(data.rentPaymentId) === savedPaidOrderCode);

                    if (isPaid) {
                        if (statusChip) statusChip.className = 'bg-green-50 text-green-600 px-3 py-1.5 rounded-full text-xs font-semibold border border-green-100 flex items-center gap-1.5';
                        if (statusDot) statusDot.className = 'w-1.5 h-1.5 rounded-full bg-green-500';
                        if (statusPreview) statusPreview.textContent = 'Đã thanh toán';
                        if (statusDetail) statusDetail.textContent = 'Đã thanh toán';
                        if (statusDetail) statusDetail.className = 'm-0 mt-1 text-xl font-bold text-green-600';
                        if (payNowButton) payNowButton.style.display = 'none';
                    } else {
                        if (statusChip) statusChip.className = 'bg-red-50 text-red-600 px-3 py-1.5 rounded-full text-xs font-semibold border border-red-100 flex items-center gap-1.5';
                        if (statusDot) statusDot.className = 'w-1.5 h-1.5 rounded-full bg-red-500 animate-pulse';
                        if (statusPreview) statusPreview.textContent = 'Chưa thanh toán';
                        if (statusDetail) statusDetail.textContent = 'Chưa thanh toán';
                        if (statusDetail) statusDetail.className = 'm-0 mt-1 text-xl font-bold text-red-600';
                        if (payNowButton) payNowButton.style.display = 'flex';
                    }
                }

                if (data.dueDate) {
                    const parts = data.dueDate.split('-');
                    let formattedDate = data.dueDate;
                    if (parts.length === 3) formattedDate = `${parts[2]}/${parts[1]}/${parts[0]}`;
                    document.getElementById('dueDatePreview').textContent = `Cần thanh toán trước ${formattedDate}`;
                    document.getElementById('dueDate').textContent = formattedDate;
                }

                if (data.createdAt) {
                    const date = new Date(data.createdAt);
                    document.getElementById('createdAt').textContent = date.toLocaleDateString('vi-VN');
                }

                if (data.totalAmount !== undefined && data.totalAmount !== null) {
                    const totalFormatted = data.totalAmount.toLocaleString('vi-VN');
                    document.getElementById('totalAmountPreview').textContent = totalFormatted;
                    document.getElementById('totalAmount').textContent = totalFormatted;
                }

                if (data.electricityAmount !== undefined && data.electricityAmount !== null) {
                    document.getElementById('electricityAmount').textContent = data.electricityAmount.toLocaleString('vi-VN');
                }

                if (data.waterAmount !== undefined && data.waterAmount !== null) {
                    document.getElementById('waterAmount').textContent = data.waterAmount.toLocaleString('vi-VN');
                }

                const rawServiceAmount = Number(data.serviceAmount);
                const fallbackServiceAmount =
                    Number(data.internetFee || 0) +
                    Number(data.parkingFee || 0) +
                    Number(data.cleaningFee || 0) +
                    Number(data.otherFee || 0);
                const serviceAmount = Number.isFinite(rawServiceAmount) ? rawServiceAmount : fallbackServiceAmount;
                if (Number.isFinite(serviceAmount)) {
                    document.getElementById('serviceAmount').textContent = serviceAmount.toLocaleString('vi-VN');
                }

                if (data.roomName) {
                    const roomLabel = document.getElementById('user-room-label');
                    if (roomLabel) roomLabel.textContent = `Phòng ${data.roomName}`;
                }
                if (paymentReturn.paid && paymentReturn.matches(data.rentPaymentId)) {
                    renderPaidReturnState();
                }
            })
            .catch(error => {
                console.error('Lỗi khi tải dữ liệu hóa đơn:', error);
            });

        const payNowButton = document.getElementById('pay-now-button');
        if (payNowButton) {
            payNowButton.addEventListener('click', function () {
                loadQuickLink().catch((error) => {
                    alert(error.message || 'Không tạo được link thanh toán PayOS');
                });
            });
        }

        const closeButton = document.getElementById('quicklink-close');
        if (closeButton) {
            closeButton.addEventListener('click', closeQuickLinkModal);
        }

        const modal = document.getElementById('quicklink-modal');
        if (modal) {
            modal.addEventListener('click', function (event) {
                if (event.target && event.target.getAttribute('data-overlay') === 'true') {
                    closeQuickLinkModal();
                }
            });
        }

    });

    function getPayOsReturnState() {
        const params = new URLSearchParams(window.location.search);
        const orderCode = params.get('orderCode');

        return {
            paid: params.get('code') === '00'
                && params.get('status') === 'PAID'
                && params.get('cancel') === 'false',
            orderCode: orderCode ? Number(orderCode) : null,
            matches(rentPaymentId) {
                return !this.orderCode || Number(rentPaymentId) === this.orderCode;
            }
        };
    }

    function renderPaidReturnState() {
        const alert = document.getElementById('payment-return-alert');
        const statusChip = document.getElementById('payment-status-chip');
        const statusDot = document.getElementById('payment-status-dot');
        const statusPreview = document.getElementById('statusPreview');
        const statusDetail = document.getElementById('status');
        const payNowButton = document.getElementById('pay-now-button');

        if (alert) alert.classList.remove('hidden');
        if (statusChip) statusChip.className = 'bg-green-50 text-green-600 px-3 py-1.5 rounded-full text-xs font-semibold border border-green-100 flex items-center gap-1.5';
        if (statusDot) statusDot.className = 'w-1.5 h-1.5 rounded-full bg-green-500';
        if (statusPreview) statusPreview.textContent = 'Đã thanh toán';
        if (statusDetail) statusDetail.textContent = 'Đã thanh toán';
        if (statusDetail) statusDetail.className = 'm-0 mt-1 text-xl font-bold text-green-600';
        if (payNowButton) payNowButton.style.display = 'none';
    }

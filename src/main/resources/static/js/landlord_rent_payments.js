document.addEventListener('DOMContentLoaded', function () {
    const state = {
        payments: [],
        selectedPayment: null
    };

    const els = {
        statusFilter: document.getElementById('statusFilter'),
        searchInput: document.getElementById('searchInput'),
        btnReload: document.getElementById('btnReload'),
        btnClearFilter: document.getElementById('btnClearFilter'),
        tbody: document.getElementById('paymentsTableBody'),
        statTotal: document.getElementById('statTotal'),
        statPaid: document.getElementById('statPaid'),
        statUnpaid: document.getElementById('statUnpaid'),
        statPending: document.getElementById('statPending'),
        modal: document.getElementById('confirmModal'),
        modalPaymentTitle: document.getElementById('modalPaymentTitle'),
        paidDateInput: document.getElementById('paidDateInput'),
        paymentMethodInput: document.getElementById('paymentMethodInput'),
        transactionIdInput: document.getElementById('transactionIdInput'),
        notesInput: document.getElementById('notesInput'),
        btnCloseModal: document.getElementById('btnCloseModal'),
        btnCancelConfirm: document.getElementById('btnCancelConfirm'),
        btnSubmitConfirm: document.getElementById('btnSubmitConfirm'),
        toast: document.getElementById('toast')
    };

    els.statusFilter.addEventListener('change', loadPayments);
    els.searchInput.addEventListener('input', renderTable);
    els.btnReload.addEventListener('click', loadPayments);
    els.btnClearFilter.addEventListener('click', function () {
        els.statusFilter.value = 'ALL';
        els.searchInput.value = '';
        loadPayments();
    });
    els.btnCloseModal.addEventListener('click', closeModal);
    els.btnCancelConfirm.addEventListener('click', closeModal);
    els.btnSubmitConfirm.addEventListener('click', submitConfirm);
    els.modal.addEventListener('click', function (event) {
        if (event.target === els.modal) closeModal();
    });

    loadPayments();

    async function loadPayments() {
        setLoading(true);
        try {
            const status = els.statusFilter.value;
            const endpoint = getEndpoint(status);
            const res = await fetch(endpoint, {
                headers: authHeaders(),
                credentials: 'include'
            });

            if (!res.ok) {
                throw new Error('HTTP ' + res.status);
            }

            const data = await res.json();
            state.payments = Array.isArray(data) ? data : [];
            updateStats();
            renderTable();
        } catch (error) {
            console.error('loadPayments error:', error);
            state.payments = [];
            updateStats();
            els.tbody.innerHTML = '<tr><td colspan="8" class="empty-row">Khong the tai danh sach thanh toan.</td></tr>';
            showToast('Khong the tai danh sach thanh toan.');
        } finally {
            setLoading(false);
        }
    }

    function getEndpoint(status) {
        if (status === 'PAID') return '/api/landlord/rent-payments/paid';
        if (status === 'UNPAID') return '/api/landlord/rent-payments/unpaid';
        return '/api/landlord/rent-payments';
    }

    function renderTable() {
        const rows = getFilteredPayments();
        if (!rows.length) {
            els.tbody.innerHTML = '<tr><td colspan="8" class="empty-row">Khong co hoa don phu hop.</td></tr>';
            return;
        }

        els.tbody.innerHTML = rows.map(function (payment) {
            const status = normalizeStatus(payment.status);
            const isPaid = status === 'PAID';
            const statusBadge = getStatusBadge(payment);
            const title = escapeHtml(payment.roomNumber || 'Phong chua ro');
            const customer = escapeHtml(payment.customerName || payment.customerEmail || 'Khach thue');
            const address = payment.roomAddress ? '<div class="sub-text">' + escapeHtml(payment.roomAddress) + '</div>' : '';
            const paidInfo = payment.paidDate ? '<div class="sub-text">Da thu: ' + formatDate(payment.paidDate) + '</div>' : '';

            return '<tr>' +
                '<td><div class="main-text">' + title + '</div><div class="sub-text">' + customer + '</div>' + address + '</td>' +
                '<td><div class="main-text">' + escapeHtml(payment.billingMonth || '-') + '</div><div class="sub-text">' + escapeHtml(payment.contractCode || '') + '</div></td>' +
                '<td class="money">' + formatMoney(payment.rentAmount) + '</td>' +
                '<td class="money">' + formatMoney(payment.utilityAmount) + '</td>' +
                '<td><div class="money">' + formatMoney(payment.totalAmount) + '</div>' + paidInfo + '</td>' +
                '<td>' + formatDate(payment.dueDate) + '</td>' +
                '<td>' + statusBadge + '</td>' +
                '<td>' +
                    '<button class="btn btn-success" type="button" data-payment-id="' + payment.id + '"' + (isPaid ? ' disabled' : '') + '>Xac nhan</button>' +
                '</td>' +
                '</tr>';
        }).join('');

        els.tbody.querySelectorAll('[data-payment-id]').forEach(function (button) {
            button.addEventListener('click', function () {
                const paymentId = Number(button.getAttribute('data-payment-id'));
                const payment = state.payments.find(function (item) { return item.id === paymentId; });
                if (payment) openModal(payment);
            });
        });
    }

    function getFilteredPayments() {
        const keyword = normalizeText(els.searchInput.value);
        if (!keyword) return state.payments.slice();

        return state.payments.filter(function (payment) {
            return [
                payment.billingMonth,
                payment.roomNumber,
                payment.roomAddress,
                payment.customerName,
                payment.customerEmail,
                payment.customerPhoneNumber,
                payment.contractCode,
                payment.status
            ].some(function (value) {
                return normalizeText(value).includes(keyword);
            });
        });
    }

    function updateStats() {
        const total = state.payments.length;
        const paid = state.payments.filter(function (payment) {
            return normalizeStatus(payment.status) === 'PAID';
        }).length;
        const unpaidList = state.payments.filter(function (payment) {
            return normalizeStatus(payment.status) !== 'PAID';
        });
        const pending = unpaidList.reduce(function (sum, payment) {
            return sum + numberValue(payment.totalAmount);
        }, 0);

        els.statTotal.textContent = total;
        els.statPaid.textContent = paid;
        els.statUnpaid.textContent = unpaidList.length;
        els.statPending.textContent = formatMoney(pending);
    }

    function openModal(payment) {
        state.selectedPayment = payment;
        els.modalPaymentTitle.textContent = (payment.roomNumber || 'Phong') + ' - ' + (payment.billingMonth || '');
        els.paidDateInput.value = new Date().toISOString().slice(0, 10);
        els.paymentMethodInput.value = payment.paymentMethod || '';
        els.transactionIdInput.value = payment.transactionId || '';
        els.notesInput.value = payment.notes || '';
        els.modal.classList.add('open');
    }

    function closeModal() {
        els.modal.classList.remove('open');
        state.selectedPayment = null;
    }

    async function submitConfirm() {
        const payment = state.selectedPayment;
        if (!payment) return;

        els.btnSubmitConfirm.disabled = true;
        try {
            const body = {
                paidDate: els.paidDateInput.value || null,
                paymentMethod: trimOrNull(els.paymentMethodInput.value),
                transactionId: trimOrNull(els.transactionIdInput.value),
                notes: trimOrNull(els.notesInput.value)
            };

            const res = await fetch('/api/landlord/rent-payments/' + payment.id + '/paid', {
                method: 'PATCH',
                headers: Object.assign({ 'Content-Type': 'application/json' }, authHeaders()),
                credentials: 'include',
                body: JSON.stringify(body)
            });

            const data = await res.json().catch(function () { return {}; });
            if (!res.ok || data.success === false) {
                throw new Error(data.message || 'Khong the cap nhat trang thai thanh toan.');
            }

            showToast(data.message || 'Da cap nhat thanh toan.');
            closeModal();
            await loadPayments();
        } catch (error) {
            console.error('submitConfirm error:', error);
            showToast(error.message || 'Khong the xac nhan thanh toan.');
        } finally {
            els.btnSubmitConfirm.disabled = false;
        }
    }

    function getStatusBadge(payment) {
        const status = normalizeStatus(payment.status);
        if (status === 'PAID') return '<span class="badge badge-paid">Da thanh toan</span>';
        if (isOverdue(payment.dueDate)) return '<span class="badge badge-overdue">Qua han</span>';
        return '<span class="badge badge-unpaid">Chua thanh toan</span>';
    }

    function isOverdue(dateValue) {
        if (!dateValue) return false;
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dueDate = new Date(dateValue);
        dueDate.setHours(0, 0, 0, 0);
        return dueDate < today;
    }

    function authHeaders() {
        const token = localStorage.getItem('smartstay_token');
        return token ? { 'Authorization': 'Bearer ' + token } : {};
    }

    function setLoading(isLoading) {
        els.btnReload.disabled = isLoading;
        if (isLoading) {
            els.tbody.innerHTML = '<tr><td colspan="8" class="empty-row">Dang tai du lieu...</td></tr>';
        }
    }

    function showToast(message) {
        els.toast.textContent = message;
        els.toast.classList.add('show');
        window.clearTimeout(showToast.timer);
        showToast.timer = window.setTimeout(function () {
            els.toast.classList.remove('show');
        }, 2600);
    }

    function formatMoney(value) {
        return numberValue(value).toLocaleString('vi-VN') + ' VND';
    }

    function formatDate(value) {
        if (!value) return '-';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return String(value);
        return date.toLocaleDateString('vi-VN');
    }

    function numberValue(value) {
        const number = Number(value);
        return Number.isFinite(number) ? number : 0;
    }

    function normalizeStatus(value) {
        return String(value || '').toUpperCase();
    }

    function normalizeText(value) {
        return String(value || '').toLowerCase().trim();
    }

    function trimOrNull(value) {
        const text = String(value || '').trim();
        return text ? text : null;
    }

    function escapeHtml(value) {
        return String(value || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }
});

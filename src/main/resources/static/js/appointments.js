const API_URL = '/api/appointments';
const grid = document.getElementById('appointment-grid');
const overlay = document.getElementById('detail-overlay');
const content = document.getElementById('detail-content');
const actions = document.getElementById('action-buttons');

const getToken = () => localStorage.getItem('smartstay_token');

function parseJwt(token) {
    try {
        const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
        return JSON.parse(atob(base64));
    } catch { return null; }
}

function getUserRole() {
    const token = getToken();
    if (!token) return null;
    const payload = parseJwt(token);
    if (!payload) return null;
    const rawRoles = [].concat(payload.roles || payload.authorities || payload.role || []);
    const roles = rawRoles.map(r => (typeof r === 'string' ? r : (r.authority || '')).toUpperCase());
    if (roles.some(r => r.includes('LANDLORD'))) return 'LANDLORD';
    if (roles.some(r => r.includes('CUSTOMER'))) return 'CUSTOMER';
    return 'CUSTOMER';
}

function getStatusLabel(status) {
    const map = {
        'PENDING': 'Đang chờ duyệt',
        'CONFIRMED_BY_TENANT': 'Khách đã xác nhận',
        'CONFIRMED_BY_LANDLORD': 'Chủ nhà đã xác nhận',
        'CONFIRMED_BY_BOTH': 'Đã chốt lịch',
        'COMPLETED': 'Đã hoàn tất',
        'CANCELLED': 'Đã hủy'
    };
    return map[status] || status;
}

function getStatusBadge(status) {
    return `<span class="status-badge status-${status}">${getStatusLabel(status)}</span>`;
}

function getAvatarUrl(avatarUrl, fallbackName) {
    if (avatarUrl && avatarUrl !== 'null' && avatarUrl.trim() !== '') return avatarUrl;
    return 'https://ui-avatars.com/api/?name=' + encodeURIComponent(fallbackName || 'User') + '&background=e0f2fe&color=0284c7';
}

// ── LOAD & RENDER GRID ────────────────────────────────────────────
async function loadAppointments() {
    const token = getToken();
    if (!token) { window.location.href = '/login'; return; }

    try {
        const res = await fetch(API_URL, { headers: { 'Authorization': `Bearer ${token}` } });
        if (!res.ok) throw new Error('Không thể tải danh sách lịch hẹn.');
        const data = await res.json();
        renderGrid(data);
    } catch (err) {
        grid.innerHTML = `<div class="col-span-full text-red-500 text-center font-semibold bg-red-50 p-6 rounded-2xl border border-red-100">${err.message}</div>`;
    }
}

function renderGrid(appointments) {
    const role = getUserRole();
    if (appointments.length === 0) {
        grid.innerHTML = `<div class="col-span-full py-20 text-center text-slate-500 bg-white/50 rounded-3xl border-2 border-dashed border-slate-200">
            <div class="text-4xl mb-4 opacity-50">📅</div>
            <div class="font-medium">Chưa có lịch hẹn nào.</div>
        </div>`;
        return;
    }

    grid.innerHTML = appointments.map(appt => {
        const partnerName = role === 'LANDLORD' ? appt.customerName : appt.landlordName;
        const partnerAvatar = role === 'LANDLORD' ? appt.customerAvatarUrl : appt.landlordAvatarUrl;
        const apptDate = new Date(appt.appointmentTime).toLocaleString('vi-VN');
        const isCancelledOrDone = appt.status === 'CANCELLED' || appt.status === 'COMPLETED';

        return `
        <div class="appointment-card" onclick="openDetail(${appt.id})">
            <div class="flex items-center gap-3">
                <img src="${getAvatarUrl(partnerAvatar, partnerName)}" class="avatar-img" onerror="this.src='https://ui-avatars.com/api/?name=User&background=eee&color=999'">
                <div class="flex-1 min-w-0">
                    <h3 class="font-bold text-slate-800 truncate">${partnerName}</h3>
                    <p class="text-xs text-slate-500">${role === 'LANDLORD' ? 'Khách thuê' : 'Chủ nhà'}</p>
                </div>
                ${isCancelledOrDone ? `<button onclick="event.stopPropagation(); confirmDelete(${appt.id})" class="delete-quick-btn" title="Xoá lịch hẹn">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6l-1 14H6L5 6"></path><path d="M10 11v6"></path><path d="M14 11v6"></path>
                        <path d="M9 6V4h6v2"></path>
                    </svg>
                </button>` : ''}
            </div>
            <div class="bg-slate-50 p-3 rounded-xl border border-slate-100 text-sm">
                <div class="font-semibold text-indigo-700 truncate mb-1">🏠 ${appt.roomPostTitle || 'Phòng thuê'}</div>
                <div class="text-slate-600 flex items-center gap-1"><span class="opacity-70">⏰</span> ${apptDate}</div>
            </div>
            <div class="mt-auto pt-2 flex justify-between items-center border-t border-slate-50">
                ${getStatusBadge(appt.status)}
                <span class="text-xs font-bold text-indigo-500">Chi tiết →</span>
            </div>
        </div>`;
    }).join('');
}

// ── DETAIL MODAL ──────────────────────────────────────────────────
let currentApptId = null;

async function openDetail(id) {
    currentApptId = id;
    overlay.style.display = 'flex';
    setTimeout(() => overlay.classList.add('show'), 10);
    content.innerHTML = '<div class="text-center py-10"><div class="w-8 h-8 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mx-auto"></div></div>';
    actions.innerHTML = '';

    const token = getToken();
    try {
        const res = await fetch(`${API_URL}/${id}`, { headers: { 'Authorization': `Bearer ${token}` } });
        if (!res.ok) throw new Error('Lỗi khi tải chi tiết lịch hẹn.');
        const appt = await res.json();
        renderDetail(appt);
    } catch (err) {
        content.innerHTML = `<div class="text-red-500 text-center font-medium">${err.message}</div>`;
    }
}

function renderDetail(appt) {
    const role = getUserRole();
    const isLandlord = role === 'LANDLORD';
    const partnerName = isLandlord ? appt.customerName : appt.landlordName;
    const partnerPhone = isLandlord ? appt.customerPhone : appt.landlordPhone;

    content.innerHTML = `
        <div class="bg-slate-50 rounded-2xl p-4 border border-slate-100 space-y-3">
            <div>
                <p class="text-[11px] font-bold text-slate-400 uppercase">Đối tác</p>
                <p class="font-semibold text-slate-800">${partnerName}${partnerPhone ? ' - ' + partnerPhone : ''}</p>
            </div>
            <div>
                <p class="text-[11px] font-bold text-slate-400 uppercase">Phòng thuê</p>
                <p class="font-semibold text-slate-800">${appt.roomPostTitle || 'N/A'}</p>
                <p class="text-sm text-slate-500">${appt.roomPostAddress || ''}</p>
            </div>
        </div>

        <div class="grid grid-cols-2 gap-3">
            <div class="bg-indigo-50/50 rounded-xl p-3 border border-indigo-100">
                <p class="text-[11px] font-bold text-indigo-400 uppercase">Thời gian hẹn</p>
                <p class="font-semibold text-indigo-900">${new Date(appt.appointmentTime).toLocaleString('vi-VN')}</p>
            </div>
            <div class="bg-emerald-50/50 rounded-xl p-3 border border-emerald-100">
                <p class="text-[11px] font-bold text-emerald-500 uppercase">Trạng thái</p>
                <div class="mt-1">${getStatusBadge(appt.status)}</div>
            </div>
        </div>

        <div class="bg-slate-50 rounded-xl p-3 border border-slate-100">
            <p class="text-[11px] font-bold text-slate-400 uppercase">Địa điểm gặp</p>
            <p class="text-sm text-slate-700 font-medium">${appt.location || 'Chưa cập nhật'}</p>
        </div>

        ${appt.note ? `
        <div class="bg-yellow-50/50 rounded-xl p-3 border border-yellow-100">
            <p class="text-[11px] font-bold text-yellow-600 uppercase">Ghi chú</p>
            <p class="text-sm text-yellow-800">${appt.note}</p>
        </div>` : ''}
    `;

    renderActions(appt, isLandlord);
}

function renderActions(appt, isLandlord) {
    const status = appt.status;
    const id = appt.id;
    const isFinalStatus = status === 'CANCELLED' || status === 'COMPLETED';
    let btns = '';

    if (!isFinalStatus) {
        // ── Nút HỦY ─────────────────────────────────────────────
        btns += `
        <button onclick="doAction(${id}, 'cancel')" class="btn-modern btn-reject flex items-center gap-2">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                <circle cx="12" cy="12" r="10"></circle>
                <line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line>
            </svg>
            Hủy lịch hẹn
        </button>`;

        // ── Nút XÁC NHẬN ────────────────────────────────────────
        if (status === 'PENDING' && !isLandlord) {
            btns += `
            <button onclick="doAction(${id}, 'confirm')" class="btn-modern btn-accept flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                Xác nhận lịch hẹn
            </button>`;
        } else if (status === 'CONFIRMED_BY_TENANT' && isLandlord) {
            btns += `
            <button onclick="doAction(${id}, 'confirm')" class="btn-modern btn-accept flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                Xác nhận lịch hẹn
            </button>`;
        } else if (status === 'PENDING' && isLandlord) {
            btns += `
            <button onclick="doAction(${id}, 'confirm')" class="btn-modern btn-accept flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                    <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>
                </svg>
                Tôi đã tạo, xác nhận lại
            </button>`;
        }

        // ── Nút HOÀN TẤT ────────────────────────────────────────
        if (status === 'CONFIRMED_BY_BOTH' && isLandlord) {
            btns += `
            <button onclick="doAction(${id}, 'complete')" class="btn-modern btn-complete flex items-center gap-2">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                    <polyline points="20 6 9 17 4 12"></polyline>
                </svg>
                Đã gặp mặt thành công
            </button>`;
        }
    } else {
        // ── Trạng thái cuối: hiển thị badge + nút XOÁ ───────────
        const isCancel = status === 'CANCELLED';
        btns += `
        <div class="w-full flex items-center gap-3 ${isCancel ? 'bg-red-50 border-red-100' : 'bg-slate-50 border-slate-100'} border rounded-2xl px-4 py-3">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="${isCancel ? '#dc2626' : '#475569'}" stroke-width="2" stroke-linecap="round">
                ${isCancel
                    ? '<circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line>'
                    : '<path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>'
                }
            </svg>
            <span class="text-sm font-semibold ${isCancel ? 'text-red-600' : 'text-slate-600'}">
                ${isCancel ? 'Lịch hẹn này đã bị hủy' : 'Lịch hẹn đã hoàn tất thành công'}
            </span>
        </div>
        <button onclick="confirmDelete(${id})" class="btn-modern flex items-center gap-2" style="background:#fee2e2;color:#dc2626;border:1.5px solid #fecaca;">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
                <polyline points="3 6 5 6 21 6"></polyline>
                <path d="M19 6l-1 14H6L5 6"></path><path d="M10 11v6"></path><path d="M14 11v6"></path>
                <path d="M9 6V4h6v2"></path>
            </svg>
            Xoá lịch hẹn này
        </button>`;
    }

    actions.innerHTML = btns;
}

// ── ACTIONS ───────────────────────────────────────────────────────
async function doAction(id, actionPath) {
    const labelMap = { cancel: 'HỦY', confirm: 'XÁC NHẬN', complete: 'HOÀN TẤT' };
    if (!confirm(`Bạn có chắc chắn muốn ${labelMap[actionPath] || 'thực hiện'} lịch hẹn này?`)) return;

    const originalActions = actions.innerHTML;
    actions.innerHTML = '<div class="text-center text-slate-500 text-sm py-3"><div class="w-5 h-5 border-2 border-slate-300 border-t-slate-600 rounded-full animate-spin mx-auto mb-2"></div>Đang xử lý...</div>';
    const token = getToken();

    try {
        const res = await fetch(`${API_URL}/${id}/${actionPath}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();

        if (res.ok) {
            closeDetail();
            loadAppointments();
        } else {
            alert(data.message || 'Có lỗi xảy ra');
            actions.innerHTML = originalActions;
        }
    } catch (err) {
        alert('Lỗi kết nối: ' + err.message);
        actions.innerHTML = originalActions;
    }
}

async function confirmDelete(id) {
    if (!confirm('Bạn có chắc chắn muốn XOÁ lịch hẹn này?\nHành động này không thể hoàn tác.')) return;

    const originalActions = actions.innerHTML;
    actions.innerHTML = '<div class="text-center text-slate-500 text-sm py-3"><div class="w-5 h-5 border-2 border-slate-300 border-t-slate-600 rounded-full animate-spin mx-auto mb-2"></div>Đang xoá...</div>';
    const token = getToken();

    try {
        const res = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();

        if (res.ok) {
            closeDetail();
            loadAppointments();
        } else {
            alert(data.message || 'Có lỗi xảy ra khi xoá lịch hẹn');
            actions.innerHTML = originalActions;
        }
    } catch (err) {
        alert('Lỗi kết nối: ' + err.message);
        actions.innerHTML = originalActions;
    }
}

function closeDetail(e) {
    if (e && e.target !== overlay) return;
    overlay.classList.remove('show');
    setTimeout(() => overlay.style.display = 'none', 300);
    currentApptId = null;
}

document.addEventListener('DOMContentLoaded', loadAppointments);

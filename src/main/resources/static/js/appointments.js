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

function getStatusBadge(status) {
    const statusMap = {
        'PENDING': 'Đang chờ duyệt',
        'CONFIRMED_BY_TENANT': 'Khách đã duyệt',
        'CONFIRMED_BY_LANDLORD': 'Chủ nhà đã duyệt',
        'CONFIRMED_BY_BOTH': 'Đã chốt lịch',
        'COMPLETED': 'Đã hoàn tất',
        'CANCELLED': 'Đã hủy'
    };
    return `<span class="status-badge status-${status}">${statusMap[status] || status}</span>`;
}

function getAvatarUrl(avatarUrl, fallbackName) {
    if (avatarUrl && avatarUrl !== 'null' && avatarUrl.trim() !== '') return avatarUrl;
    return 'https://ui-avatars.com/api/?name=' + encodeURIComponent(fallbackName || 'User') + '&background=e0f2fe&color=0284c7';
}

async function loadAppointments() {
    const token = getToken();
    if (!token) {
        window.location.href = '/login';
        return;
    }

    try {
        const res = await fetch(API_URL, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
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
        
        return `
        <div class="appointment-card" onclick="openDetail(${appt.id})">
            <div class="flex items-center gap-3">
                <img src="${getAvatarUrl(partnerAvatar, partnerName)}" class="avatar-img">
                <div class="flex-1 min-w-0">
                    <h3 class="font-bold text-slate-800 truncate">${partnerName}</h3>
                    <p class="text-xs text-slate-500">${role === 'LANDLORD' ? 'Khách thuê' : 'Chủ nhà'}</p>
                </div>
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

async function openDetail(id) {
    overlay.style.display = 'flex';
    setTimeout(() => overlay.classList.add('show'), 10);
    content.innerHTML = '<div class="text-center py-10"><div class="w-8 h-8 border-4 border-indigo-200 border-t-indigo-600 rounded-full animate-spin mx-auto"></div></div>';
    actions.innerHTML = '';

    const token = getToken();
    try {
        const res = await fetch(`${API_URL}/${id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
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
                <p class="font-semibold text-slate-800">${partnerName} ${partnerPhone ? ' - ' + partnerPhone : ''}</p>
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

    // Render Buttons
    let btns = '';
    
    // Nút Hủy (Cho phép hủy nếu trước 2 giờ và chưa bị hủy/hoàn tất)
    if (appt.status !== 'CANCELLED' && appt.status !== 'COMPLETED') {
        const timeDiff = new Date(appt.appointmentTime) - new Date();
        if (timeDiff > 2 * 60 * 60 * 1000) {
            btns += `<button onclick="doAction(${appt.id}, 'cancel')" class="btn-modern btn-reject">Hủy lịch hẹn</button>`;
        }
    }

    // Nút Xác nhận
    if (appt.status === 'PENDING' && !isLandlord) {
        btns += `<button onclick="doAction(${appt.id}, 'confirm')" class="btn-modern btn-accept">Xác nhận lịch hẹn</button>`;
    } else if (appt.status === 'CONFIRMED_BY_TENANT' && isLandlord) {
        btns += `<button onclick="doAction(${appt.id}, 'confirm')" class="btn-modern btn-accept">Xác nhận lịch hẹn</button>`;
    } else if (appt.status === 'PENDING' && isLandlord) {
        btns += `<button onclick="doAction(${appt.id}, 'confirm')" class="btn-modern btn-accept">Tôi đã tạo, xác nhận lại</button>`;
    }

    // Nút Hoàn tất (Chỉ Landlord khi đã CONFIRMED_BY_BOTH)
    if (appt.status === 'CONFIRMED_BY_BOTH' && isLandlord) {
        btns += `<button onclick="doAction(${appt.id}, 'complete')" class="btn-modern btn-complete">Đã gặp mặt thành công</button>`;
    }

    actions.innerHTML = btns;
}

async function doAction(id, actionPath) {
    if (!confirm('Bạn có chắc chắn thực hiện hành động này?')) return;
    
    actions.innerHTML = '<div class="text-center text-slate-500 text-sm">Đang xử lý...</div>';
    const token = getToken();
    
    try {
        const res = await fetch(`${API_URL}/${id}/${actionPath}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await res.json();
        
        if (res.ok) {
            alert('Thành công!');
            closeDetail();
            loadAppointments();
        } else {
            alert(data.message || 'Có lỗi xảy ra');
            openDetail(id);
        }
    } catch (err) {
        alert('Lỗi kết nối: ' + err.message);
        openDetail(id);
    }
}

function closeDetail(e) {
    if (e && e.target !== overlay) return;
    overlay.classList.remove('show');
    setTimeout(() => overlay.style.display = 'none', 300);
}

document.addEventListener('DOMContentLoaded', loadAppointments);

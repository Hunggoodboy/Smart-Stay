// ==========================================
// CẤU HÌNH & BIẾN TOÀN CỤC
// ==========================================
const API_URL = '/api/my/requests';
const grid    = document.getElementById('request-grid');
const overlay = document.getElementById('detail-overlay');
const content = document.getElementById('detail-content');
const actions = document.getElementById('action-buttons');

// Lấy token từ localStorage
const getToken = () => localStorage.getItem('smartstay_token');

// Giải mã JWT để lấy role
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
    return 'CUSTOMER'; // fallback
}

// ==========================================
// LOAD DANH SÁCH YÊU CẦU
// ==========================================
async function loadRequests() {
    const token = getToken();
    if (!token) {
        window.location.href = '/login';
        return;
    }

    try {
        const res = await fetch(API_URL, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!res.ok) throw new Error('Không thể tải dữ liệu. Vui lòng thử lại.');
        const data = await res.json();
        renderGrid(data);
    } catch (err) {
        grid.innerHTML = `<div class="col-span-full text-red-500 text-center font-semibold bg-red-50 p-6 rounded-2xl border border-red-100">${err.message}</div>`;
    }
}

// ==========================================
// HELPER: ẢNH
// ==========================================
function getAvatarUrl(user) {
    if (user && user.avatarUrl && user.avatarUrl !== 'null' && user.avatarUrl.trim() !== '') {
        return user.avatarUrl;
    }
    return 'https://ui-avatars.com/api/?name=' + encodeURIComponent(user?.fullName || 'User') + '&background=e0f2fe&color=0284c7&size=100';
}

function getRoomThumbnail(roomPost) {
    if (roomPost && roomPost.thumbnailUrl && roomPost.thumbnailUrl !== 'null' && roomPost.thumbnailUrl.trim() !== '') {
        return roomPost.thumbnailUrl;
    }
    return 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?q=80&w=200&auto=format&fit=crop';
}

// ==========================================
// RENDER LƯỚI CARD
// ==========================================
function renderGrid(requests) {
    const role = getUserRole();

    if (requests.length === 0) {
        const emptyMsg = role === 'LANDLORD'
            ? 'Chưa có yêu cầu nào gửi đến bạn.'
            : 'Bạn chưa gửi yêu cầu thuê phòng nào.';
        grid.innerHTML = `<div class="col-span-full py-20 text-center text-slate-500 bg-white/50 rounded-3xl border-2 border-dashed border-slate-200">
            <div class="text-4xl mb-4 opacity-50">📫</div>
            <div class="font-medium">${emptyMsg}</div>
        </div>`;
        return;
    }

    grid.innerHTML = requests.map(req => {
        // Label hiển thị trên card tùy role
        const personLabel = role === 'LANDLORD'
            ? `<h3 class="font-bold text-lg text-slate-800 leading-tight">${req.customer?.fullName || 'Khách hàng'}</h3>
               <p class="text-xs text-slate-500 mt-1 font-medium">Gửi: ${new Date(req.createdAt).toLocaleDateString('vi-VN')}</p>`
            : `<h3 class="font-bold text-lg text-slate-800 leading-tight">${req.roomPost?.title || 'Phòng'}</h3>
               <p class="text-xs text-slate-500 mt-1 font-medium">Gửi: ${new Date(req.createdAt).toLocaleDateString('vi-VN')}</p>`;

        const avatarSrc = role === 'LANDLORD'
            ? getAvatarUrl(req.customer)
            : getAvatarUrl(req.landlord);

        // Badge hợp đồng trên card (chỉ hiện với LANDLORD khi đã có contractId)
        const contractBadge = (role === 'LANDLORD' && req.contractId)
            ? `<span class="contract-badge">
                   <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5">
                       <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                       <polyline points="14 2 14 8 20 8"></polyline>
                   </svg>
                   Có hợp đồng
               </span>`
            : '';

        return `
        <div class="request-card" onclick="showDetail(${JSON.stringify(req).replace(/"/g, '&quot;')})">
            <div class="flex items-center gap-4">
                <img src="${avatarSrc}" class="avatar-img" onerror="this.src='https://ui-avatars.com/api/?name=User&background=eee&color=999'">
                <div class="flex-1 min-w-0">${personLabel}</div>
                ${contractBadge}
            </div>
            <div class="bg-slate-50/80 rounded-2xl p-3.5 flex items-center gap-3 border border-slate-100 mt-2">
                <img src="${getRoomThumbnail(req.roomPost)}" class="room-img" onerror="this.src='https://placehold.co/100x100?text=Room'">
                <div class="flex-1 overflow-hidden">
                    <p class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-0.5">Phòng yêu cầu</p>
                    <p class="text-sm font-semibold text-slate-700 whitespace-nowrap overflow-hidden text-ellipsis">${req.roomPost?.title || '--'}</p>
                </div>
            </div>
            <div class="mt-2 flex justify-between items-center pt-2 border-t border-slate-100">
                <span class="status-badge status-${req.status}">${req.status === 'PENDING' ? 'CHỜ DUYỆT' : req.status === 'APPROVED' ? 'ĐÃ DUYỆT' : 'TỪ CHỐI'}</span>
                <span class="text-primary text-sm font-bold bg-blue-50 px-3 py-1.5 rounded-full transition">Chi tiết →</span>
            </div>
        </div>`;
    }).join('');
}

// ==========================================
// MODAL CHI TIẾT
// ==========================================
function showDetail(req) {
    overlay.style.display = 'flex';
    setTimeout(() => overlay.classList.add('show'), 10);

    const role = getUserRole();

    content.innerHTML = `
        <div class="space-y-5">
            <div class="space-y-3">
                <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider pl-1">
                    ${role === 'LANDLORD' ? 'Khách hàng gửi yêu cầu' : 'Thông tin chủ nhà'}
                </p>
                <div class="flex items-center gap-4 p-4 border border-slate-100 rounded-2xl bg-white shadow-sm">
                    <img src="${role === 'LANDLORD' ? getAvatarUrl(req.customer) : getAvatarUrl(req.landlord)}"
                         class="avatar-img"
                         onerror="this.src='https://ui-avatars.com/api/?name=User&background=eee&color=999'">
                    <div>
                        <p class="font-bold text-slate-800 text-base">
                            ${role === 'LANDLORD' ? (req.customer?.fullName || 'Khách hàng') : (req.landlord?.fullName || 'Chủ nhà')}
                        </p>
                        <button onclick="openChatWith(${role === 'LANDLORD' ? req.customer?.id : req.landlord?.id})"
                                class="mt-2 text-sm btn-modern btn-chat px-4 py-2">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
                            </svg>
                            Nhắn tin
                        </button>
                    </div>
                </div>
            </div>

            <div class="space-y-3">
                <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider pl-1">Thông tin phòng</p>
                <div class="flex items-center gap-4 p-4 border border-slate-100 rounded-2xl bg-slate-50 shadow-sm">
                    <img src="${getRoomThumbnail(req.roomPost)}" class="w-20 h-20 rounded-xl object-cover border border-slate-200"
                         onerror="this.src='https://placehold.co/100x100?text=Room'">
                    <div>
                        <p class="font-bold text-slate-800 text-base leading-snug line-clamp-2">${req.roomPost?.title || '--'}</p>
                        <p class="text-sm text-slate-500 mt-1">ID Phòng: #${req.roomPost?.roomId || req.roomPost?.id || '--'}</p>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div class="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm">
                    <div class="flex items-center gap-2 mb-2">
                        <svg class="text-slate-400" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line>
                            <line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line>
                        </svg>
                        <p class="text-[11px] font-bold text-slate-500 uppercase">Ngày gửi</p>
                    </div>
                    <p class="text-sm font-semibold text-slate-800">${new Date(req.createdAt).toLocaleString('vi-VN')}</p>
                </div>
                <div class="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm">
                    <div class="flex items-center gap-2 mb-2">
                        <svg class="text-slate-400" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>
                        </svg>
                        <p class="text-[11px] font-bold text-slate-500 uppercase">Trạng thái</p>
                    </div>
                    <span class="status-badge status-${req.status} !px-0 !bg-transparent !border-none !shadow-none !pb-0 text-sm">
                        ${req.status === 'PENDING' ? 'Đang chờ xử lý' : req.status === 'APPROVED' ? 'Đã duyệt' : 'Đã từ chối'}
                    </span>
                </div>
            </div>
        </div>
    `;

    // ── RENDER BUTTONS THEO ROLE ──────────────────────────────
    if (role === 'LANDLORD') {
        renderLandlordActions(req);
    } else {
        renderCustomerActions(req);
    }
}

// ==========================================
// NÚT HÀNH ĐỘNG: LANDLORD
// ==========================================
function renderLandlordActions(req) {
    if (req.status === 'PENDING') {
        // Chưa duyệt → cho phép chấp nhận / từ chối
        actions.innerHTML = `
            <button onclick="handleAction(${req.id}, 'REJECTED')" class="flex-1 btn-modern btn-reject">Từ chối</button>
            <button onclick="handleAction(${req.id}, 'APPROVED')" class="flex-1 btn-modern btn-accept">Chấp nhận yêu cầu</button>
        `;

    } else if (req.status === 'APPROVED') {

        if (req.contractId) {
            // ✅ ĐÃ CÓ HỢP ĐỒNG → hiển thị badge + nút tạo phòng quản lý
            actions.innerHTML = `
                <div class="w-full flex flex-col gap-4">

                    <!-- Badge: đã có hợp đồng -->
                    <div class="w-full flex items-center gap-3 bg-indigo-50 border border-indigo-200 rounded-2xl px-5 py-4">
                        <span class="flex-shrink-0 w-9 h-9 rounded-xl bg-indigo-100 flex items-center justify-center">
                            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#6366f1" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                                <polyline points="14 2 14 8 20 8"></polyline>
                                <polyline points="9 15 11 17 15 13"></polyline>
                            </svg>
                        </span>
                        <div class="flex-1">
                            <p class="text-sm font-bold text-indigo-700">Đã có hợp đồng</p>
                            <p class="text-xs text-indigo-500 mt-0.5">Hợp đồng #${req.contractId} đã được tạo cho yêu cầu này</p>
                        </div>
                        <a href="/contractDetail/${req.contractId}"
                           class="flex-shrink-0 text-xs font-bold text-indigo-600 bg-indigo-100 hover:bg-indigo-200 transition px-3 py-1.5 rounded-xl no-underline">
                            Xem →
                        </a>
                    </div>

                    <!-- Nút tạo phòng quản lý -->
                    <button onclick="openRoomManageForm(${req.id}, ${req.customer?.id}, ${req.roomPost?.roomId || req.roomPost?.id})"
                            class="w-full btn-modern btn-room-manage text-base">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
                            <polyline points="9 22 9 12 15 12 15 22"></polyline>
                        </svg>
                        Tạo phòng quản lý cho khách này
                    </button>

                </div>
            `;

        } else {
            // ⏳ ĐƯỢC DUYỆT nhưng chưa có hợp đồng → tiến hành làm hợp đồng
            actions.innerHTML = `
                <div class="w-full flex flex-col gap-4">
                    <div class="w-full text-center bg-emerald-50/50 py-3 rounded-2xl border border-emerald-100 text-emerald-600 font-bold text-sm shadow-sm flex items-center justify-center gap-2">
                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>
                        </svg>
                        Yêu cầu đã được CHẤP NHẬN
                    </div>
                    <button onclick="openContractForm(${req.roomPost?.roomId || req.roomPost?.id}, ${req.customer?.id}, ${req.id})"
                            class="w-full btn-modern btn-contract text-base">
                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                            <polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line>
                            <line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline>
                        </svg>
                        Tiến hành làm hợp đồng
                    </button>
                </div>
            `;
        }

    } else {
        // REJECTED / HỦY
        actions.innerHTML = `<div class="w-full text-center bg-slate-50 py-4 rounded-2xl border border-slate-100 text-slate-500 font-medium text-sm flex items-center justify-center gap-2">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line>
            </svg>
            Yêu cầu này đã bị TỪ CHỐI hoặc ĐÃ HỦY
        </div>`;
    }
}

// ==========================================
// NÚT HÀNH ĐỘNG: CUSTOMER
// ==========================================
function renderCustomerActions(req) {
    if (req.status === 'PENDING') {
        actions.innerHTML = `
            <div class="w-full flex flex-col gap-3">
                <div class="w-full text-center bg-amber-50 py-3 rounded-2xl border border-amber-100 text-amber-700 font-semibold text-sm flex items-center justify-center gap-2">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    Đang chờ chủ nhà xác nhận
                </div>
                <button onclick="deleteRequest(${req.id})"
                        class="w-full btn-modern" style="background:#fee2e2;color:#dc2626;border:1.5px solid #fecaca;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6l-1 14H6L5 6"></path><path d="M10 11v6"></path><path d="M14 11v6"></path>
                        <path d="M9 6V4h6v2"></path>
                    </svg>
                    Huỷ yêu cầu thuê
                </button>
            </div>
        `;
    } else if (req.status === 'APPROVED') {
        actions.innerHTML = `<div class="w-full text-center bg-emerald-50 py-4 rounded-2xl border border-emerald-100 text-emerald-600 font-bold text-sm flex items-center justify-center gap-2">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline>
            </svg>
            Yêu cầu đã được CHẤP NHẬN – Chủ nhà sẽ liên hệ với bạn
        </div>`;
    } else {
        actions.innerHTML = `
            <div class="w-full flex flex-col gap-3">
                <div class="w-full text-center bg-slate-50 py-3 rounded-2xl border border-slate-100 text-slate-500 font-medium text-sm flex items-center justify-center gap-2">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line>
                    </svg>
                    Yêu cầu đã bị TỪ CHỐI
                </div>
                <button onclick="deleteRequest(${req.id})"
                        class="w-full btn-modern" style="background:#fee2e2;color:#dc2626;border:1.5px solid #fecaca;">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <polyline points="3 6 5 6 21 6"></polyline>
                        <path d="M19 6l-1 14H6L5 6"></path><path d="M10 11v6"></path><path d="M14 11v6"></path>
                        <path d="M9 6V4h6v2"></path>
                    </svg>
                    Xoá yêu cầu này
                </button>
            </div>
        `;
    }
}

function closeDetail(event) {
    if (event && event.target !== overlay) return;
    overlay.classList.remove('show');
    setTimeout(() => { overlay.style.display = 'none'; }, 300);
}

// ==========================================
// LANDLORD: DUYỆT / TỪ CHỐI
// ==========================================
async function handleAction(requestId, newStatus) {
    const actionName = newStatus === 'APPROVED' ? 'CHẤP NHẬN' : 'TỪ CHỐI';
    if (!confirm(`Bạn có chắc chắn muốn ${actionName} yêu cầu này không?`)) return;

    const token = getToken();
    const originalActions = actions.innerHTML;
    actions.innerHTML = `<div class="w-full text-center py-4 bg-slate-50 rounded-2xl border border-slate-100 text-slate-500 font-semibold flex items-center justify-center gap-3">
        <div class="w-5 h-5 border-2 border-slate-300 border-t-slate-600 rounded-full animate-spin"></div>
        Đang xử lý...
    </div>`;

    try {
        const response = await fetch(`/api/landlord/changeRequestStatus/${requestId}?status=${newStatus}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        if (response.ok && data.success) {
            overlay.classList.remove('show');
            setTimeout(() => { overlay.style.display = 'none'; loadRequests(); }, 300);
        } else {
            alert(data.message || 'Có lỗi xảy ra, vui lòng thử lại.');
            actions.innerHTML = originalActions;
        }
    } catch (err) {
        alert('Lỗi kết nối đến máy chủ: ' + err.message);
        actions.innerHTML = originalActions;
    }
}

// ==========================================
// CUSTOMER: XOÁ / HUỶ YÊU CẦU
// ==========================================
async function deleteRequest(requestId) {
    if (!confirm('Bạn có chắc chắn muốn huỷ / xoá yêu cầu thuê phòng này không?')) return;

    const token = getToken();
    const originalActions = actions.innerHTML;
    actions.innerHTML = `<div class="w-full text-center py-4 bg-slate-50 rounded-2xl border border-slate-100 text-slate-500 font-semibold flex items-center justify-center gap-3">
        <div class="w-5 h-5 border-2 border-slate-300 border-t-slate-600 rounded-full animate-spin"></div>
        Đang xoá...
    </div>`;

    try {
        const response = await fetch(`/api/delete/request/${requestId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        const data = await response.json();
        if (response.ok && data.success) {
            overlay.classList.remove('show');
            setTimeout(() => {
                overlay.style.display = 'none';
                loadRequests();
            }, 300);
        } else {
            alert(data.message || 'Có lỗi xảy ra, vui lòng thử lại.');
            actions.innerHTML = originalActions;
        }
    } catch (err) {
        alert('Lỗi kết nối đến máy chủ: ' + err.message);
        actions.innerHTML = originalActions;
    }
}

// ==========================================
// CHAT & HỢP ĐỒNG
// ==========================================
function openChatWith(userId) {
    localStorage.setItem('chat_receiver_id', userId);
    window.location.href = '/chatMessage';
}

// Giữ nguyên để không break code cũ gọi openChatWithCustomer
function openChatWithCustomer(customerId) { openChatWith(customerId); }

function openContractForm(roomPostId, tenantId, requestId) {
    if (!roomPostId || !tenantId) {
        alert('Thiếu thông tin Phòng hoặc Khách thuê, không thể tạo hợp đồng!');
        return;
    }
    window.location.href = `/contract/create?roomPostId=${roomPostId}&tenantId=${tenantId}&requestId=${requestId}`;
}

// ==========================================
// TẠO PHÒNG QUẢN LÝ (khi đã có hợp đồng)
// ==========================================
function openRoomManageForm(requestId, customerId, roomPostId) {
    if (!requestId) {
        alert('Thiếu thông tin yêu cầu, không thể tạo phòng quản lý!');
        return;
    }
    // Chỉ cần chuyển hướng kèm requestId trên URL
    window.location.href = `/createRoomManage?requestId=${requestId}`;
}

// Khởi chạy khi load trang
document.addEventListener('DOMContentLoaded', loadRequests);
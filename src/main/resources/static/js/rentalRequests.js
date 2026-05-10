const API_URL = '/api/landlord/requests';
const grid = document.getElementById('request-grid');
const overlay = document.getElementById('detail-overlay');
const content = document.getElementById('detail-content');
const actions = document.getElementById('action-buttons');

// Lấy token từ localStorage
const getToken = () => localStorage.getItem('smartstay_token');

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

function getAvatarUrl(customer) {
    if (customer && customer.avatarUrl && customer.avatarUrl !== 'null' && customer.avatarUrl.trim() !== '') {
        return customer.avatarUrl;
    }
    return 'https://ui-avatars.com/api/?name=' + encodeURIComponent(customer.fullName || 'User') + '&background=e0f2fe&color=0284c7&size=100';
}

function getRoomThumbnail(roomPost) {
    if (roomPost && roomPost.thumbnailUrl && roomPost.thumbnailUrl !== 'null' && roomPost.thumbnailUrl.trim() !== '') {
        return roomPost.thumbnailUrl;
    }
    return 'https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?q=80&w=200&auto=format&fit=crop';
}

function renderGrid(requests) {
    if (requests.length === 0) {
        grid.innerHTML = `<div class="col-span-full py-20 text-center text-slate-500 bg-white/50 rounded-3xl border-2 border-dashed border-slate-200">
            <div class="text-4xl mb-4 opacity-50">📫</div>
            <div class="font-medium">Chưa có yêu cầu nào gửi đến bạn.</div>
        </div>`;
        return;
    }

    grid.innerHTML = requests.map(req => `
        <div class="request-card" onclick="showDetail(${JSON.stringify(req).replace(/"/g, '&quot;')})">
            <div class="flex items-center gap-4">
                <img src="${getAvatarUrl(req.customer)}" class="avatar-img" onerror="this.src='https://ui-avatars.com/api/?name=User&background=eee&color=999'">
                <div>
                    <h3 class="font-bold text-lg text-slate-800 leading-tight">${req.customer.fullName}</h3>
                    <p class="text-xs text-slate-500 mt-1 font-medium">Gửi: ${new Date(req.createdAt).toLocaleDateString('vi-VN')}</p>
                </div>
            </div>
            <div class="bg-slate-50/80 rounded-2xl p-3.5 flex items-center gap-3 border border-slate-100 mt-2">
                <img src="${getRoomThumbnail(req.roomPost)}" class="room-img" onerror="this.src='https://placehold.co/100x100?text=Room'">
                <div class="flex-1 overflow-hidden">
                    <p class="text-xs text-slate-400 font-bold uppercase tracking-wider mb-0.5">Phòng yêu cầu</p>
                    <p class="text-sm font-semibold text-slate-700 whitespace-nowrap overflow-hidden text-ellipsis">${req.roomPost.title}</p>
                </div>
            </div>
            <div class="mt-2 flex justify-between items-center pt-2 border-t border-slate-100">
                <span class="status-badge status-${req.status}">${req.status === 'PENDING' ? 'CHỜ DUYỆT' : req.status === 'APPROVED' ? 'ĐÃ DUYỆT' : 'TỪ CHỐI'}</span>
                <span class="text-primary text-sm font-bold bg-blue-50 px-3 py-1.5 rounded-full group-hover:bg-blue-100 transition">Chi tiết →</span>
            </div>
        </div>
    `).join('');
}

function showDetail(req) {
    overlay.style.display = 'flex';
    // Add small delay to trigger CSS transition
    setTimeout(() => overlay.classList.add('show'), 10);
    
    content.innerHTML = `
        <div class="space-y-6">
            <div class="p-6 bg-gradient-to-br from-blue-50 to-indigo-50 rounded-3xl border border-blue-100/50 shadow-inner">
                <div class="flex items-center gap-5">
                    <img src="${getAvatarUrl(req.customer)}" class="w-20 h-20 rounded-full border-4 border-white shadow-md object-cover" onerror="this.src='https://ui-avatars.com/api/?name=User&background=eee&color=999'">
                    <div>
                        <p class="text-[11px] text-blue-600 font-bold uppercase tracking-wider mb-1">Khách thuê tiềm năng</p>
                        <h4 class="text-2xl font-bold text-slate-900 leading-none">${req.customer.fullName}</h4>
                    </div>
                </div>
                
                <button onclick="openChatWithCustomer(${req.customer.id})" class="mt-6 w-full btn-modern btn-chat">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                    Nhắn tin với khách hàng
                </button>
            </div>
            
            <div class="space-y-3">
                <p class="text-[11px] font-bold text-slate-400 uppercase tracking-wider pl-1">Thông tin phòng</p>
                <div class="flex items-center gap-4 p-4 border border-slate-100 rounded-2xl bg-slate-50 shadow-sm transition hover:shadow-md">
                    <img src="${getRoomThumbnail(req.roomPost)}" class="w-20 h-20 rounded-xl object-cover border border-slate-200" onerror="this.src='https://placehold.co/100x100?text=Room'">
                    <div>
                        <p class="font-bold text-slate-800 text-base leading-snug line-clamp-2">${req.roomPost.title}</p>
                        <p class="text-sm text-slate-500 mt-1">ID Phòng: #${req.roomPost.roomId || req.roomPost.id}</p>
                    </div>
                </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div class="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm">
                    <div class="flex items-center gap-2 mb-2">
                        <svg class="text-slate-400" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
                        <p class="text-[11px] font-bold text-slate-500 uppercase">Ngày gửi</p>
                    </div>
                    <p class="text-sm font-semibold text-slate-800">${new Date(req.createdAt).toLocaleString('vi-VN')}</p>
                </div>
                <div class="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm">
                    <div class="flex items-center gap-2 mb-2">
                        <svg class="text-slate-400" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                        <p class="text-[11px] font-bold text-slate-500 uppercase">Trạng thái</p>
                    </div>
                    <span class="status-badge status-${req.status} !px-0 !bg-transparent !border-none !shadow-none !pb-0 text-sm">
                        ${req.status === 'PENDING' ? 'Đang chờ xử lý' : req.status === 'APPROVED' ? 'Đã duyệt' : 'Đã từ chối'}
                    </span>
                </div>
            </div>
        </div>
    `;

    // PHÂN LOẠI HIỂN THỊ NÚT THEO TRẠNG THÁI
    if (req.status === 'PENDING') {
        actions.innerHTML = `
            <button onclick="handleAction(${req.id}, 'REJECTED')" class="flex-1 btn-modern btn-reject">Từ chối</button>
            <button onclick="handleAction(${req.id}, 'APPROVED')" class="flex-1 btn-modern btn-accept">Chấp nhận yêu cầu</button>
        `;
    } else if (req.status === 'APPROVED') {
        // NẾU ĐÃ APPROVED -> HIỆN NÚT LÀM HỢP ĐỒNG
        actions.innerHTML = `
            <div class="w-full flex flex-col gap-4">
                <div class="w-full text-center bg-emerald-50/50 py-3 rounded-2xl border border-emerald-100 text-emerald-600 font-bold text-sm shadow-sm flex items-center justify-center gap-2">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
                    Yêu cầu đã được CHẤP NHẬN
                </div>
                <button onclick="openContractForm(${req.roomPost.roomId || req.roomPost.id}, ${req.customer.id}, ${req.id})" class="w-full btn-modern btn-contract text-base">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                    Tiến hành làm hợp đồng
                </button>
            </div>
        `;
    } else {
        // TRƯỜNG HỢP REJECTED HOẶC KHÁC
        actions.innerHTML = `<div class="w-full text-center bg-slate-50 py-4 rounded-2xl border border-slate-100 text-slate-500 font-medium text-sm flex items-center justify-center gap-2">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="15" y1="9" x2="9" y2="15"></line><line x1="9" y1="9" x2="15" y2="15"></line></svg>
            Yêu cầu này đã bị TỪ CHỐI hoặc ĐÃ HỦY
        </div>`;
    }
}

function closeDetail() {
    overlay.classList.remove('show');
    setTimeout(() => {
        overlay.style.display = 'none';
    }, 300); // match transition time
}

// ==========================================
// HÀM XỬ LÝ GỌI API CHẤP NHẬN / TỪ CHỐI
// ==========================================
async function handleAction(requestId, newStatus) {
    const actionName = newStatus === 'APPROVED' ? 'CHẤP NHẬN' : 'TỪ CHỐI';
    if(!confirm(`Bạn có chắc chắn muốn ${actionName} yêu cầu này không?`)) return;

    const token = getToken();
    const originalActions = actions.innerHTML;

    // Hiển thị trạng thái đang xử lý
    actions.innerHTML = `<div class="w-full text-center py-4 bg-slate-50 rounded-2xl border border-slate-100 text-slate-500 font-semibold flex items-center justify-center gap-3">
        <div class="w-5 h-5 border-2 border-slate-300 border-t-slate-600 rounded-full animate-spin"></div>
        Đang xử lý...
    </div>`;

    try {
        const response = await fetch(`/api/landlord/changeRequestStatus/${requestId}?status=${newStatus}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const data = await response.json();

        if (response.ok && data.success) {
            closeDetail();
            loadRequests(); // Tự động load lại danh sách sau khi duyệt
        } else {
            alert(data.message || "Có lỗi xảy ra, vui lòng thử lại.");
            actions.innerHTML = originalActions; // Khôi phục nút nếu lỗi
        }
    } catch (err) {
        alert("Lỗi kết nối đến máy chủ: " + err.message);
        actions.innerHTML = originalActions;
    }
}

// ==========================================
// HÀM XỬ LÝ ĐIỀU HƯỚNG ĐẾN TRANG CHAT
// ==========================================
function openChatWithCustomer(customerId) {
    localStorage.setItem('chat_receiver_id', customerId);
    window.location.href = '/chatMessage';
}

// ==========================================
// HÀM CHUYỂN HƯỚNG SANG TRANG LÀM HỢP ĐỒNG
// ==========================================
function openContractForm(roomId, tenantId, requestId) {
    if(!roomId || !tenantId) {
        alert("Thiếu thông tin Phòng hoặc Khách thuê, không thể tạo hợp đồng!");
        return;
    }
    // Đẩy dữ liệu lên URL để trang contractCreation có thể lấy được
    window.location.href = `/contract/create?roomId=${roomId}&tenantId=${tenantId}&requestId=${requestId}`;
}

// Khởi chạy khi load trang
document.addEventListener('DOMContentLoaded', loadRequests);
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
        grid.innerHTML = `<div class="col-span-full text-red-500 text-center font-semibold">${err.message}</div>`;
    }
}

function renderGrid(requests) {
    if (requests.length === 0) {
        grid.innerHTML = `<div class="col-span-full py-20 text-center text-slate-500 bg-white/50 rounded-3xl border-2 border-dashed border-slate-200">Chưa có yêu cầu nào gửi đến bạn.</div>`;
        return;
    }

    grid.innerHTML = requests.map(req => `
        <div class="request-card bg-white p-5 rounded-3xl shadow-sm border border-slate-100" onclick="showDetail(${JSON.stringify(req).replace(/"/g, '&quot;')})">
            <div class="flex items-center gap-4 mb-4">
                <img src="${req.customer.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(req.customer.fullName)}" class="w-12 h-12 rounded-full border object-cover">
                <div>
                    <h3 class="font-bold text-slate-900">${req.customer.fullName}</h3>
                    <p class="text-xs text-slate-400">Gửi lúc: ${new Date(req.createdAt).toLocaleDateString('vi-VN')}</p>
                </div>
            </div>
            <div class="bg-slate-50 rounded-2xl p-4 flex items-center gap-3">
                <img src="${req.roomPost.thumbnailUrl || 'https://placehold.co/100x100?text=Room'}" class="w-14 h-14 rounded-xl object-cover border border-slate-200">
                <p class="text-sm font-semibold text-slate-700 line-clamp-1">${req.roomPost.title}</p>
            </div>
            <div class="mt-4 flex justify-between items-center">
                <span class="status-badge status-${req.status}">${req.status === 'PENDING' ? 'CHỜ DUYỆT' : req.status === 'APPROVED' ? 'ĐÃ DUYỆT' : req.status}</span>
                <span class="text-primary text-sm font-bold">Xem chi tiết →</span>
            </div>
        </div>
    `).join('');
}

function showDetail(req) {
    overlay.style.display = 'flex';
    content.innerHTML = `
        <div class="space-y-6">
            <div class="p-5 bg-blue-50/50 rounded-3xl border border-blue-100">
                <div class="flex items-center gap-5">
                    <img src="${req.customer.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(req.customer.fullName)}" class="w-16 h-16 rounded-2xl border-2 border-white shadow-sm object-cover">
                    <div>
                        <p class="text-[11px] text-blue-600 font-bold uppercase tracking-wider mb-1">Khách thuê tiềm năng</p>
                        <h4 class="text-xl font-bold text-slate-900 leading-none">${req.customer.fullName}</h4>
                    </div>
                </div>
                
                <button onclick="openChatWithCustomer(${req.customer.id})" class="mt-5 w-full bg-white text-blue-600 border border-blue-200 py-2.5 rounded-xl font-bold hover:bg-blue-50 transition flex justify-center items-center gap-2 shadow-sm">
                    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
                    Nhắn tin với khách hàng
                </button>
            </div>
            
            <div class="space-y-2">
                <p class="text-xs font-bold text-slate-400 uppercase tracking-wide">Phòng đang yêu cầu</p>
                <div class="flex items-center gap-4 p-3 border border-slate-100 rounded-2xl bg-white shadow-sm">
                    <img src="${req.roomPost.thumbnailUrl || 'https://placehold.co/100x100?text=Room'}" class="w-16 h-16 rounded-xl object-cover border border-slate-100">
                    <p class="font-semibold text-slate-800 text-sm leading-snug">${req.roomPost.title}</p>
                </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                    <p class="text-[10px] font-bold text-slate-400 uppercase mb-1">Ngày gửi</p>
                    <p class="text-sm font-semibold text-slate-700">${new Date(req.createdAt).toLocaleString('vi-VN')}</p>
                </div>
                <div class="p-4 bg-slate-50 rounded-2xl border border-slate-100">
                    <p class="text-[10px] font-bold text-slate-400 uppercase mb-1">Trạng thái</p>
                    <p class="text-sm font-semibold text-slate-700">${req.status === 'PENDING' ? 'Đang chờ xử lý' : req.status === 'APPROVED' ? 'Đã duyệt' : req.status}</p>
                </div>
            </div>
        </div>
    `;

    // PHÂN LOẠI HIỂN THỊ NÚT THEO TRẠNG THÁI
    if (req.status === 'PENDING') {
        actions.innerHTML = `
            <button onclick="handleAction(${req.id}, 'APPROVED')" class="flex-1 bg-[#10b981] text-white py-3.5 rounded-2xl font-bold hover:bg-[#059669] transition shadow-lg shadow-emerald-200">Chấp nhận yêu cầu</button>
            <button onclick="handleAction(${req.id}, 'REJECTED')" class="flex-1 bg-rose-50 text-rose-600 border border-rose-100 py-3.5 rounded-2xl font-bold hover:bg-rose-100 transition">Từ chối</button>
        `;
    } else if (req.status === 'APPROVED') {
        // NẾU ĐÃ APPROVED -> HIỆN NÚT LÀM HỢP ĐỒNG
        // Lưu ý: Lấy ID phòng từ req.roomPost.roomId hoặc req.roomPost.id tùy vào DTO của bạn
        actions.innerHTML = `
            <div class="w-full flex flex-col gap-3">
                <div class="w-full text-center bg-emerald-50 py-2.5 rounded-2xl border border-emerald-100 text-emerald-600 font-medium text-sm">
                    Yêu cầu đã được CHẤP NHẬN
                </div>
                <button onclick="openContractForm(${req.roomPost.roomId || req.roomPost.id}, ${req.customer.id}, ${req.id})" class="w-full bg-blue-600 text-white py-3.5 rounded-2xl font-bold hover:bg-blue-700 transition shadow-lg shadow-blue-200 flex justify-center items-center gap-2">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
                    Tiến hành làm hợp đồng
                </button>
            </div>
        `;
    } else {
        // TRƯỜNG HỢP REJECTED HOẶC KHÁC
        actions.innerHTML = `<div class="w-full text-center bg-slate-50 py-3 rounded-2xl border border-slate-100 text-slate-500 font-medium text-sm">Yêu cầu này đã bị TỪ CHỐI hoặc ĐÃ HỦY</div>`;
    }
}

function closeDetail() {
    overlay.style.display = 'none';
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
    actions.innerHTML = `<div class="w-full text-center py-3 text-slate-500 font-semibold animate-pulse">Đang xử lý...</div>`;

    try {
        const response = await fetch(`/api/landlord/changeRequestStatus/${requestId}?status=${newStatus}`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        const data = await response.json();

        if (response.ok && data.success) {
            alert(data.message || `Đã ${actionName.toLowerCase()} thành công!`);
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
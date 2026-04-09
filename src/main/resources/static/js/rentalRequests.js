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

        if (!res.ok) throw new Error('Không thể tải dữ liệu');
        const data = await res.json();
        renderGrid(data);
    } catch (err) {
        grid.innerHTML = `<div class="col-span-full text-red-500 text-center">${err.message}</div>`;
    }
}

function renderGrid(requests) {
    if (requests.length === 0) {
        grid.innerHTML = `<div class="col-span-full py-20 text-center text-slate-500 bg-white/50 rounded-3xl border-2 border-dashed border-slate-200">Chưa có yêu cầu nào gửi đến bạn.</div>`;
        return;
    }

    grid.innerHTML = requests.map(req => `
        <div class="request-card bg-white p-5 rounded-3xl shadow-sm border border-white" onclick="showDetail(${JSON.stringify(req).replace(/"/g, '&quot;')})">
            <div class="flex items-center gap-4 mb-4">
                <img src="${req.customer.avatarUrl || 'https://ui-avatars.com/api/?name=' + req.customer.fullName}" class="w-12 h-12 rounded-full border">
                <div>
                    <h3 class="font-bold text-slate-900">${req.customer.fullName}</h3>
                    <p class="text-xs text-slate-400">Gửi lúc: ${new Date(req.createdAt).toLocaleDateString('vi-VN')}</p>
                </div>
            </div>
            <div class="bg-slate-50 rounded-2xl p-4 flex items-center gap-3">
                <img src="${req.roomPost.thumbnailUrl}" class="w-14 h-14 rounded-xl object-cover">
                <p class="text-sm font-semibold text-slate-700 line-clamp-1">${req.roomPost.title}</p>
            </div>
            <div class="mt-4 flex justify-between items-center">
                <span class="status-badge status-${req.status}">${req.status}</span>
                <span class="text-primary text-sm font-medium">Xem chi tiết →</span>
            </div>
        </div>
    `).join('');
}

function showDetail(req) {
    overlay.style.display = 'flex';
    content.innerHTML = `
        <div class="space-y-6">
            <div class="flex items-center gap-6 p-4 bg-amber-50 rounded-3xl border border-amber-100">
                <img src="${req.customer.avatarUrl || 'https://ui-avatars.com/api/?name=' + req.customer.fullName}" class="w-20 h-20 rounded-2xl border-2 border-white shadow-sm">
                <div>
                    <p class="text-xs text-amber-600 font-bold uppercase tracking-wider">Thông tin khách thuê</p>
                    <h4 class="text-xl font-bold text-slate-900">${req.customer.fullName}</h4>
                    <p class="text-sm text-slate-500">ID Khách hàng: #${req.customer.id}</p>
                </div>
            </div>
            
            <div class="space-y-2">
                <p class="text-sm font-bold text-slate-400 uppercase">Phòng quan tâm</p>
                <div class="flex items-center gap-4 p-3 border border-slate-100 rounded-2xl">
                    <img src="${req.roomPost.thumbnailUrl}" class="w-16 h-16 rounded-xl object-cover">
                    <p class="font-semibold text-slate-800">${req.roomPost.title}</p>
                </div>
            </div>

            <div class="grid grid-cols-2 gap-4">
                <div class="p-4 bg-slate-50 rounded-2xl">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">Ngày gửi</p>
                    <p class="font-semibold text-slate-700">${new Date(req.createdAt).toLocaleString('vi-VN')}</p>
                </div>
                <div class="p-4 bg-slate-50 rounded-2xl">
                    <p class="text-[10px] font-bold text-slate-400 uppercase">Trạng thái hiện tại</p>
                    <p class="font-semibold text-slate-700">${req.status}</p>
                </div>
            </div>
        </div>
    `;

    // Chỉ hiện nút hành động nếu trạng thái là PENDING
    if (req.status === 'PENDING') {
        actions.innerHTML = `
            <button onclick="handleAction(${req.id}, 'APPROVED')" class="flex-1 bg-primary text-white py-3 rounded-2xl font-bold hover:opacity-90 transition">Chấp nhận yêu cầu</button>
            <button onclick="handleAction(${req.id}, 'REJECTED')" class="flex-1 bg-slate-100 text-slate-600 py-3 rounded-2xl font-bold hover:bg-slate-200 transition">Từ chối</button>
        `;
    } else {
        actions.innerHTML = `<p class="text-center w-full text-slate-400 text-sm">Yêu cầu này đã được xử lý lúc ${new Date(req.reviewedAt).toLocaleDateString('vi-VN')}</p>`;
    }
}

function closeDetail() {
    overlay.style.display = 'none';
}

async function handleAction(id, newStatus) {
    // Gọi API cập nhật trạng thái (Hùng cần viết thêm API này ở Backend nếu chưa có)
    alert(`Bạn đã chọn: ${newStatus} cho yêu cầu #${id}. Chức năng cập nhật đang được kết nối...`);
    closeDetail();
}

// Khởi chạy khi load trang
document.addEventListener('DOMContentLoaded', loadRequests);
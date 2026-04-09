const token = localStorage.getItem('smartstay_token');
const tableBody = document.getElementById('requestTableBody');

if (!token) window.location.href = '/login';

async function loadRequests(type = 'verifyFalse') {
    tableBody.innerHTML = '<tr><td colspan="4" class="p-20 text-center text-slate-400">Đang tải hồ sơ...</td></tr>';

    try {
        // Đã sửa đường dẫn khớp với Controller
        const res = await fetch(`/api/admin/landlord/${type}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!res.ok) throw new Error('Phiên đăng nhập hết hạn hoặc không có quyền ADMIN');

        const data = await res.json();
        renderTable(data, type === 'verifyFalse');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="4" class="p-20 text-center text-red-500 font-bold">${err.message}</td></tr>`;
    }
}

function renderTable(items, isWaiting) {
    if (!items || items.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="4" class="p-20 text-center text-slate-400">Không có yêu cầu nào trong mục này.</td></tr>';
        return;
    }

    tableBody.innerHTML = items.map(item => {
        // TRỌNG TÂM: Lấy dữ liệu từ landLord vì đối tượng user đang bị null
        const d = item.landLord;

        return `
        <tr class="border-t border-slate-50 hover:bg-slate-50/50 transition">
            <td class="p-6">
                <div class="flex items-center gap-4">
                    <img src="${d.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(d.fullName)}" class="avatar-circle">
                    <div>
                        <div class="font-bold text-slate-900">${d.fullName}</div>
                        <div class="text-xs text-slate-400 font-medium">${d.email}</div>
                    </div>
                </div>
            </td>
            <td class="p-6">
                <div class="text-sm font-mono text-slate-700 font-bold">${d.idCardNumber || 'N/A'}</div>
                <div class="text-xs text-slate-500 mt-1">${d.address || 'N/A'}</div>
            </td>
            <td class="p-6">
                <span class="badge ${isWaiting ? 'badge-pending' : 'badge-success'}">
                    ${isWaiting ? 'CHỜ DUYỆT' : 'ĐÃ XÁC THỰC'}
                </span>
            </td>
            <td class="p-6 text-right">
                ${isWaiting ? `
                    <button onclick="approve(${d.id})" class="btn-approve">Phê duyệt</button>
                ` : '<span class="text-emerald-500 text-xl">✔</span>'}
            </td>
        </tr>`;
    }).join('');
}

async function approve(id) {
    if (!confirm("Xác nhận cấp quyền Chủ nhà cho người dùng này?")) return;
    try {
        const res = await fetch(`/api/admin/landlord/verify/${id}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (res.ok) {
            alert("Phê duyệt thành công!");
            loadRequests('verifyFalse');
        }
    } catch (err) { alert("Lỗi kết nối hệ thống!"); }
}

function switchTab(type) {
    document.querySelectorAll('.tab-btn').forEach(t => t.classList.remove('tab-active'));
    document.getElementById(`btn-${type}`).classList.add('tab-active');
    loadRequests(type);
}

document.addEventListener('DOMContentLoaded', () => loadRequests('verifyFalse'));
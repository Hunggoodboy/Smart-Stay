const token = localStorage.getItem('smartstay_token');
const tableBody = document.getElementById('requestTableBody');

if (!token) window.location.href = '/login';

async function loadRequests(type = 'verifyFalse') {
    tableBody.innerHTML = '<tr><td colspan="4" style="padding:20px;text-align:center;color:#94a3b8;font-weight:600;">Đang tải hồ sơ...</td></tr>';

    try {
        // Đã sửa đường dẫn khớp với Controller
        const res = await fetch(`/api/admin/landlord/${type}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });

        if (!res.ok) throw new Error('Phiên đăng nhập hết hạn hoặc không có quyền ADMIN');

        const data = await res.json();
        renderTable(data, type === 'verifyFalse');
    } catch (err) {
        tableBody.innerHTML = `<tr><td colspan="4" style="padding:20px;text-align:center;color:#dc2626;font-weight:700;">${err.message}</td></tr>`;
    }
}

function renderTable(items, isWaiting) {
    if (!items || items.length === 0) {
        tableBody.innerHTML = '<tr><td colspan="4" style="padding:20px;text-align:center;color:#94a3b8;font-weight:600;">Không có yêu cầu nào trong mục này.</td></tr>';
        return;
    }

    tableBody.innerHTML = items.map(item => {
        // TRỌNG TÂM: Lấy dữ liệu từ landLord vì đối tượng user đang bị null
        const d = item.landLord;
        const statusLabel = isWaiting ? 'CHỜ DUYỆT' : 'ĐÃ XÁC THỰC';

        return `
        <tr>
            <td>
                <div style="display:flex;align-items:center;gap:0.6rem;">
                    <img src="${d.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(d.fullName)}" class="avatar-circle">
                    <div>
                        <div class="admin-table-title">${d.fullName}</div>
                        <div class="admin-table-sub">${d.email}</div>
                    </div>
                </div>
            </td>
            <td>
                <div class="admin-table-title">${d.idCardNumber || 'N/A'}</div>
                <div class="admin-table-sub">${d.address || 'N/A'}</div>
            </td>
            <td>
                <span class="badge ${isWaiting ? 'badge-pending' : 'badge-success'}">${statusLabel}</span>
            </td>
            <td style="text-align:right;">
                ${isWaiting ? `
                    <button onclick="approve(${d.id})" class="btn-approve">Phê duyệt</button>
                ` : '<span style="color:#16a34a;font-weight:800;">✔</span>'}
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
    document.querySelectorAll('.ss-tab').forEach(t => t.classList.remove('active'));
    document.getElementById(`btn-${type}`).classList.add('active');
    loadRequests(type);
}

document.addEventListener('DOMContentLoaded', () => loadRequests('verifyFalse'));
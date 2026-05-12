document.addEventListener('DOMContentLoaded', () => {
    // Tìm các vùng chứa menu trong HTML
    const navLinks = document.getElementById('dynamicNavLinks');
    const navActions = document.getElementById('navActions');

    // Nếu trang nào không có Navbar (ví dụ trang Login/Register) thì JS tự động bỏ qua
    if (!navLinks || !navActions) return;

    // Lấy thông tin user đã đăng nhập
    const token = localStorage.getItem('smartstay_token');
    const userStr = localStorage.getItem('smartstay_user');
    const user = userStr ? JSON.parse(userStr) : null;

    if (token && user) {
        let extraLinks = '';

        // 1. Phân quyền hiển thị Menu dựa trên Role
        if (user.role === 'CUSTOMER') {
            extraLinks += `<a href="/payment" class="nav-link">Phòng đang thuê</a>`;
            extraLinks += `<a href="/myContracts" class="nav-link">Hợp đồng của tôi</a>`;
            extraLinks += `<a href="/registerLandLord" class="nav-link">Đăng ký chủ nhà</a>`;
        }
        else if (user.role === 'LANDLORD') {
            extraLinks += `<a href="/payment" class="nav-link">Phòng đang thuê</a>`;
            extraLinks += `<a href="/MyRentalRequest" class="nav-link">Yêu cầu thuê</a>`;
            extraLinks += `<a href="/postRooms" class="nav-link">Đăng phòng</a>`;
            extraLinks += `<a href="/myContracts" class="nav-link">Hợp đồng của tôi</a>`;
        }
        else if (user.role === 'ADMIN') {
            // Highlight màu xanh cho Admin nổi bật
            extraLinks += `<a href="/adminVerify" class="nav-link" style="color: #1d4ed8; font-weight: bold;">Quản lý Admin</a>`;
        }

        // Chèn thêm các link vừa tạo vào HTML
        navLinks.innerHTML += extraLinks;

        // 2. Đổi nút Login/Register thành Tên + Nút Đăng xuất
        navActions.innerHTML = `
            <div style="display: flex; align-items: center; gap: 16px;">
                <span style="font-weight: 600; color: #1e293b; font-size: 15px;">👋 Chào, ${user.fullName}</span>
                <button onclick="handleLogout()" style="background: #fee2e2; color: #ef4444; border: none; padding: 8px 16px; border-radius: 10px; cursor: pointer; font-weight: 600; transition: 0.2s;" onmouseover="this.style.background='#fecaca'" onmouseout="this.style.background='#fee2e2'">Đăng xuất</button>
            </div>
        `;
    } else {
        // Trạng thái chưa đăng nhập (Khách vãng lai)
        navActions.innerHTML = `
            <a href="/login" style="color: #64748b; font-weight: 600; text-decoration: none;">Đăng nhập</a>
            <a href="/register" style="background: #1d4ed8; color: white; padding: 8px 20px; border-radius: 10px; font-weight: 600; text-decoration: none;">Đăng ký</a>
        `;
    }
});

function handleLogout() {
    localStorage.removeItem('smartstay_token');
    localStorage.removeItem('smartstay_user');
    window.location.href = '/login';
}
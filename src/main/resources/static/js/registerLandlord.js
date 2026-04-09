document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('formRegister');
    const token = localStorage.getItem('smartstay_token'); // Lấy token để xác thực

    // Kiểm tra đăng nhập cơ bản
    if (!token) {
        alert("Bạn cần đăng nhập để thực hiện chức năng này!");
        window.location.href = '/login';
        return;
    }

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Thu thập dữ liệu dựa trên LandLordRegisterRequest.java
        const payload = {
            idCardNumber: document.getElementById('idCardNumber').value.trim(),
            address: document.getElementById('address').value.trim(),
            description: document.getElementById('description').value.trim()
        };

        try {
            const response = await fetch('/api/landlord/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // Gửi token
                },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                alert("Gửi yêu cầu thành công! Vui lòng chờ Admin phê duyệt.");
                window.location.href = '/myHome'; // Chuyển về trang cá nhân
            } else {
                const errorData = await response.json();
                alert(`Lỗi: ${errorData.message || 'Không thể gửi yêu cầu'}`);
            }
        } catch (error) {
            console.error('Lỗi kết nối:', error);
            alert("Có lỗi xảy ra, vui lòng thử lại sau.");
        }
    });
});
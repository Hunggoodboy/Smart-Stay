(() => {
    const DASHBOARD_ENDPOINTS = {
        totalUsers: '/api/admin/dashboard/users/total',
        totalCustomers: '/api/admin/dashboard/users/customers/total',
        totalLandlords: '/api/admin/dashboard/users/landlords/total',
        pendingLandlordVerifications: '/api/admin/dashboard/landlords/pending-verifications/total',
        verifiedLandlords: '/api/admin/dashboard/landlords/verified/total',
        postsNeedReview: '/api/admin/dashboard/room-posts/need-review/total',
        totalRoomPosts: '/api/admin/dashboard/room-posts/total',
        totalRooms: '/api/admin/dashboard/rooms/total'
    };

    function handleAdminLogout() {
        localStorage.removeItem('smartstay_token');
        localStorage.removeItem('smartstay_user');
        document.cookie = 'smartstay_token=; Max-Age=0; path=/';
        window.location.href = '/login';
    }

    function getToken() {
        return localStorage.getItem('smartstay_token');
    }

    function hasDashboardStats() {
        return Boolean(document.getElementById('admin-stat-users'));
    }

    function setText(id, value) {
        const element = document.getElementById(id);
        if (element) element.textContent = value;
    }

    function setDashboardLoading() {
        setText('admin-stat-users', '...');
        setText('admin-stat-pending', '...');
        setText('admin-stat-rooms', '...');
        setText('admin-stat-flagged', '...');
    }

    function setDashboardError() {
        setText('admin-stat-users', '!');
        setText('admin-stat-pending', '!');
        setText('admin-stat-rooms', '!');
        setText('admin-stat-flagged', '!');
    }

    function renderSystemStatsLoading() {
        const list = document.getElementById('admin-system-stats-list');
        if (!list) return;

        list.innerHTML = `
            <div class="msg-item">
                <div class="msg-avatar">...</div>
                <div>
                    <div class="msg-name">Đang tải thống kê...</div>
                    <div class="msg-preview">Vui lòng chờ trong giây lát.</div>
                </div>
            </div>
        `;
    }

    function renderSystemStats(stats) {
        const list = document.getElementById('admin-system-stats-list');
        if (!list) return;

        const items = [
            ['Khách thuê', stats.totalCustomers],
            ['Chủ nhà', stats.totalLandlords],
            ['Chủ nhà đã xác thực', stats.verifiedLandlords],
            ['Tổng bài đăng', stats.totalRoomPosts]
        ];

        list.innerHTML = items.map(([label, value]) => `
            <div class="msg-item">
                <div class="msg-avatar">${Number(value || 0).toLocaleString('vi-VN')}</div>
                <div>
                    <div class="msg-name">${label}</div>
                    <div class="msg-preview">Số liệu hiện tại trong hệ thống</div>
                </div>
            </div>
        `).join('');
    }

    async function fetchCount(endpoint) {
        const token = getToken();
        if (!token) {
            window.location.href = '/login';
            return null;
        }

        const response = await fetch(endpoint, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });

        if (response.status === 401 || response.status === 403) {
            handleAdminLogout();
            return null;
        }

        if (!response.ok) {
            throw new Error(`Cannot load dashboard count: ${response.status}`);
        }

        const data = await response.json();
        return Number(data.value || 0);
    }

    async function loadAdminDashboardStats() {
        if (!hasDashboardStats()) return;

        setDashboardLoading();
        renderSystemStatsLoading();

        try {
            const results = await Promise.all(
                Object.entries(DASHBOARD_ENDPOINTS).map(async ([key, endpoint]) => [key, await fetchCount(endpoint)])
            );
            const stats = Object.fromEntries(results);

            setText('admin-stat-users', stats.totalUsers ?? 0);
            setText('admin-stat-pending', stats.pendingLandlordVerifications ?? 0);
            setText('admin-stat-rooms', stats.totalRooms ?? 0);
            setText('admin-stat-flagged', stats.postsNeedReview ?? 0);
            renderSystemStats(stats);
        } catch (error) {
            console.error(error);
            setDashboardError();
        }
    }

    document.addEventListener('DOMContentLoaded', loadAdminDashboardStats);

    window.handleAdminLogout = handleAdminLogout;
})();

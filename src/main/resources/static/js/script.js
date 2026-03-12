const wrapper = document.getElementById('user-menu');
const dropdown = document.getElementById('user-dropdown');
const menuButton = document.getElementById('user-menu-button');
const dashboardRoot = document.getElementById('tenant-dashboard');

const el = {
    welcomeMessage: document.getElementById('welcome-message'),
    roomLocation: document.getElementById('room-location'),
    userAvatar: document.getElementById('user-avatar'),
    userRoomLabel: document.getElementById('user-room-label'),
    userRoleLabel: document.getElementById('user-role-label'),
    dropdownUserName: document.getElementById('dropdown-user-name'),
    dropdownUserRole: document.getElementById('dropdown-user-role'),
    notificationDot: document.getElementById('notification-dot'),
    unreadCount: document.getElementById('unread-count'),
    billingMonth: document.getElementById('billing-month'),
    paymentStatusChip: document.getElementById('payment-status-chip'),
    paymentStatusDot: document.getElementById('payment-status-dot'),
    paymentStatusText: document.getElementById('payment-status-text'),
    paymentDueDate: document.getElementById('payment-due-date'),
    paymentTotal: document.getElementById('payment-total'),
    electricUsage: document.getElementById('electric-usage'),
    waterUsage: document.getElementById('water-usage'),
    notificationList: document.getElementById('notification-list')
};

function setUserMenuState(isOpen) {
    if (!dropdown) {
        return;
    }

    dropdown.classList.toggle('hidden', !isOpen);

    if (menuButton) {
        menuButton.setAttribute('aria-expanded', String(isOpen));
    }
}

function toggleUserMenu() {
    setUserMenuState(dropdown ? dropdown.classList.contains('hidden') : false);
}

window.addEventListener('click', function (e) {
    if (!wrapper || !dropdown) {
        return;
    }
    if (!wrapper.contains(e.target) && !dropdown.classList.contains('hidden')) {
        setUserMenuState(false);
    }
});

window.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
        setUserMenuState(false);
    }
});

if (dropdown) {
    dropdown.querySelectorAll('a').forEach(function (item) {
        item.addEventListener('click', function () {
            setUserMenuState(false);
        });
    });
}

function setText(node, value) {
    if (node && value !== undefined && value !== null) {
        node.textContent = String(value);
    }
}

function formatCurrency(value) {
    const amount = Number(value || 0);
    return new Intl.NumberFormat('vi-VN').format(amount);
}

function formatDate(value) {
    if (!value) {
        return 'Chua xac dinh';
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return 'Chua xac dinh';
    }

    return date.toLocaleDateString('vi-VN');
}

function getSavedUser() {
    const raw = localStorage.getItem('smartstay_user');
    if (!raw) {
        return null;
    }

    try {
        return JSON.parse(raw);
    } catch (error) {
        return null;
    }
}

function applyStatusStyle(status) {
    if (!el.paymentStatusChip || !el.paymentStatusDot) {
        return;
    }

    el.paymentStatusChip.className =
        'px-3 py-1.5 rounded-full text-xs font-semibold border flex items-center gap-1.5';
    el.paymentStatusDot.className = 'w-1.5 h-1.5 rounded-full';

    const normalized = String(status || 'UNKNOWN').toUpperCase();
    if (normalized === 'PAID') {
        el.paymentStatusChip.classList.add('bg-emerald-50', 'text-emerald-600', 'border-emerald-100');
        el.paymentStatusDot.classList.add('bg-emerald-500');
        return;
    }

    if (normalized === 'OVERDUE') {
        el.paymentStatusChip.classList.add('bg-amber-50', 'text-amber-700', 'border-amber-100');
        el.paymentStatusDot.classList.add('bg-amber-500', 'animate-pulse');
        return;
    }

    el.paymentStatusChip.classList.add('bg-red-50', 'text-red-600', 'border-red-100');
    el.paymentStatusDot.classList.add('bg-red-500', 'animate-pulse');
}

function applyDashboardData(data) {
    if (!data) {
        return;
    }

    const displayName = data.displayName || data.fullName || 'Khach thue';
    const roomLabel = data.roomLabel || 'Phong 204';
    const roleLabel = data.roleLabel || 'Khach thue';

    setText(el.welcomeMessage, `Xin chao, ${displayName}!`);
    setText(el.roomLocation, roomLabel);
    setText(el.userRoomLabel, roomLabel);
    setText(el.userRoleLabel, roleLabel);
    setText(el.dropdownUserName, displayName);
    setText(el.dropdownUserRole, roleLabel);

    if (el.userAvatar && data.avatarUrl) {
        el.userAvatar.src = data.avatarUrl;
    }

    const unread = Number(data.unreadNotifications || 0);
    if (el.unreadCount) {
        el.unreadCount.textContent = `(${unread})`;
    }
    if (el.notificationDot) {
        el.notificationDot.classList.toggle('hidden', unread <= 0);
    }

    setText(el.billingMonth, `Hoa don ${data.billingMonth || ''}`.trim());
    setText(el.paymentStatusText, data.paymentStatusLabel || 'Chua co hoa don');
    setText(el.paymentDueDate, `Can thanh toan truoc ${formatDate(data.dueDate)}`);
    setText(el.paymentTotal, formatCurrency(data.totalAmount));
    setText(el.electricUsage, data.electricUsage ?? 100);
    setText(el.waterUsage, data.waterUsage ?? 4);
    applyStatusStyle(data.paymentStatus);

    if (el.notificationList && Array.isArray(data.notifications) && data.notifications.length > 0) {
        el.notificationList.innerHTML = data.notifications
            .slice(0, 5)
            .map(function (item) {
                const title = item.title || 'Thong bao';
                const content = item.content || '';
                const time = item.relativeTime || '';
                return `
                    <div class="flex gap-3 items-start group cursor-pointer">
                        <div class="mt-0.5 grid place-items-center w-8 h-8 rounded-full bg-blue-50 text-blue-500 shrink-0 group-hover:bg-blue-100 transition-colors">
                            <div class="i-mdi-information-outline text-lg"></div>
                        </div>
                        <div>
                            <p class="font-semibold text-gray-800 text-sm m-0 group-hover:text-blue-600 transition-colors">${title}</p>
                            <p class="text-xs text-gray-500 mt-1 m-0 line-clamp-2 leading-relaxed">${content}</p>
                            <p class="text-[10px] font-medium text-gray-400 mt-1.5 m-0 uppercase tracking-wide">${time}</p>
                        </div>
                    </div>
                `;
            })
            .join('<div class="h-px w-full bg-gray-50"></div>');
    }
}

async function loadDashboard() {
    if (!dashboardRoot) {
        return;
    }

    const endpoint = dashboardRoot.dataset.endpoint || '/api/dashboard/tenant';
    const user = getSavedUser();
    const username = user && user.username ? user.username : null;
    const url = username ? `${endpoint}?username=${encodeURIComponent(username)}` : endpoint;

    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            return;
        }

        const data = await response.json();
        applyDashboardData(data);
    } catch (error) {
        // Keep current static UI when API is unavailable.
    }
}

loadDashboard();
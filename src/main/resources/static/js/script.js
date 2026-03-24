const wrapper = document.getElementById('user-menu');
const dropdown = document.getElementById('user-dropdown');
const menuButton = document.getElementById('user-menu-button');
const dashboardRoot = document.getElementById('tenant-dashboard');

function byId() {
    for (let i = 0; i < arguments.length; i += 1) {
        const node = document.getElementById(arguments[i]);
        if (node) {
            return node;
        }
    }
    return null;
}

const el = {
    welcomeMessage: byId('welcome-message'),
    roomLocation: byId('room-location'),
    userAvatar: byId('user-avatar'),
    userRoomLabel: byId('user-room-label'),
    userRoleLabel: byId('user-role-label'),
    dropdownUserName: byId('dropdown-user-name'),
    dropdownUserRole: byId('dropdown-user-role'),
    notificationDot: byId('notification-dot'),
    unreadCount: byId('unread-count'),
    billingMonth: byId('billingMonth', 'billing-month'),
    paymentStatusChip: byId('payment-status-chip'),
    paymentStatusDot: byId('payment-status-dot'),
    statusPreview: byId('statusPreview', 'payment-status-text'),
    dueDatePreview: byId('dueDatePreview', 'payment-due-date'),
    totalAmountPreview: byId('totalAmountPreview', 'payment-total'),
    electricityAmount: byId('electricityAmount', 'electricity-amount'),
    waterAmount: byId('waterAmount', 'water-amount'),
    serviceAmount: byId('serviceAmount', 'service-amount'),
    status: byId('status', 'bill-status'),
    createdAt: byId('createdAt', 'bill-created-at'),
    dueDate: byId('dueDate', 'bill-due-date'),
    totalAmount: byId('totalAmount', 'bill-total-cost'),
    electricityConsumed: byId('electricityConsumed', 'electric-usage'),
    waterConsumed: byId('waterConsumed', 'water-usage'),
    notificationList: byId('notification-list')
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

function getStatusLabel(status) {
    const normalized = String(status || '').toUpperCase();
    if (normalized === 'PAID') {
        return 'Da thanh toan';
    }
    if (normalized === 'OVERDUE') {
        return 'Qua han';
    }
    if (normalized === 'UNPAID') {
        return 'Chua thanh toan';
    }
    return 'Chua xac dinh';
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

function pickLatestBill(bills) {
    if (!Array.isArray(bills) || bills.length === 0) {
        return null;
    }

    return bills
        .slice()
        .sort(function (a, b) {
            const monthA = Number((a && a.billingMonth) || 0);
            const monthB = Number((b && b.billingMonth) || 0);
            if (monthA !== monthB) {
                return monthB - monthA;
            }

            const createdA = new Date((a && a.createdAt) || 0).getTime();
            const createdB = new Date((b && b.createdAt) || 0).getTime();
            return createdB - createdA;
        })[0];
}

function mergeDashboardData(userData, latestBill) {
    const data = Object.assign({}, userData || {});

    if (!latestBill) {
        return data;
    }

    data.paymentStatus = latestBill.status;
    data.totalCost = latestBill.totalAmount;
    data.dueDate = latestBill.dueDate;
    data.createdAt = latestBill.createdAt;
    data.electricityConsumed = latestBill.electricityConsumed;
    data.waterConsumed = latestBill.waterConsumed ?? latestBill.water_consumed;
    data.electricityAmount = latestBill.electricityAmount;
    data.waterAmount = latestBill.waterAmount;
    if (latestBill.billingMonth !== undefined && latestBill.billingMonth !== null) {
        data.billingMonth = `thang ${latestBill.billingMonth}`;
    }

    return data;
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

    setText(el.userRoomLabel, displayName);
    setText(el.userRoleLabel, roleLabel);
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

    const paymentStatus = data.paymentStatus || data.status;
    const paymentStatusLabel = data.paymentStatusLabel || getStatusLabel(paymentStatus);
    const dueDate = data.dueDate;
    const totalCost = data.totalCost ?? data.totalAmount;

    setText(el.billingMonth, `Hoa don ${data.billingMonth || ''}`.trim());
    setText(el.statusPreview, paymentStatusLabel || 'Chua co hoa don');
    setText(el.dueDatePreview, `Can thanh toan truoc ${formatDate(dueDate)}`);
    setText(el.totalAmountPreview, formatCurrency(totalCost));
    setText(el.electricityAmount, formatCurrency(data.electricityAmount));
    setText(el.waterAmount, formatCurrency(data.waterAmount));
    setText(el.serviceAmount, formatCurrency(data.serviceAmount));
    setText(el.status, paymentStatusLabel);
    setText(el.createdAt, formatDate(data.createdAt));
    setText(el.dueDate, formatDate(dueDate));
    setText(el.totalAmount, formatCurrency(totalCost));
    setText(el.electricityConsumed, data.electricityConsumed ?? data.electricUsage ?? 100);
    setText(el.waterConsumed, data.waterConsumed ?? data.water_consumed ?? data.waterUsage ?? data.water_usage ?? 4);
    applyStatusStyle(paymentStatus);

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

function renderUserMenu(isLoggedIn, data) {
    if (!wrapper) return;

    if (!isLoggedIn) {
        // Ẩn nút notification khi chưa đăng nhập
        const notifBtn = document.querySelector('button:has(#notification-dot)');
        const divider = notifBtn && notifBtn.nextElementSibling;
        if (notifBtn) notifBtn.style.display = 'none';
        if (divider && divider.tagName !== 'DIV') { /* skip */ } else if (divider) divider.style.display = 'none';

        // Thay toàn bộ user-menu thành 2 nút đăng nhập / đăng ký
        wrapper.innerHTML = `
            <div class="flex items-center gap-2">
                <a href="/login"
                    class="flex items-center gap-1.5 rounded-xl border border-gray-200 bg-white px-4 py-2 text-sm font-semibold text-gray-700 transition hover:bg-gray-50 no-underline">
                    <div class="i-material-symbols-login text-base text-gray-500"></div>
                    Đăng nhập
                </a>
                <a href="/register"
                    class="flex items-center gap-1.5 rounded-xl bg-blue-600 px-4 py-2 text-sm font-semibold text-white shadow-md shadow-blue-200/50 transition hover:bg-blue-700 active:scale-95 no-underline">
                    <div class="i-material-symbols-person-add text-base"></div>
                    Đăng ký
                </a>
            </div>
        `;
        return;
        if (data) {
            const displayName = data.displayName || data.fullName || 'Khách thuê';
            const roleLabel = data.roleLabel || 'Khách thuê';

            if (el.userRoomLabel) el.userRoomLabel.textContent = displayName; // ✅
            if (el.userRoleLabel) el.userRoleLabel.textContent = roleLabel;
        }
    }

    // Đã đăng nhập — cập nhật thông tin trong button
    if (data) {
        const displayName = data.displayName || data.fullName || 'Khách thuê';
        const roomLabel = data.roomLabel || 'Phòng 204';
        const roleLabel = data.roleLabel || 'Khách thuê';
        const avatarUrl = data.avatarUrl || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=eff6ff&color=1d4ed8`;

        if (el.userAvatar) el.userAvatar.src = avatarUrl;
        if (el.userRoomLabel) el.userRoomLabel.textContent = roomLabel;
        if (el.userRoleLabel) el.userRoleLabel.textContent = roleLabel;
    }
}

async function loadDashboard() {
    try {
        const [userResponse, billsResponse] = await Promise.all([
            fetch('/api/user/tenant', {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            }),
            fetch('/api/utility-bills', {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include'
            }).catch(function () {
                return null;
            })
        ]);

        if (userResponse.status === 401 || userResponse.status === 403) {
            renderUserMenu(false, null);
            return;
        }

        if (!userResponse.ok) return;

        const rawData = await userResponse.json();

        const userData = rawData.body ? rawData.body : rawData;
        renderUserMenu(true, userData);

        let latestBill = null;
        if (billsResponse && billsResponse.ok) {
            const billsData = await billsResponse.json();
            latestBill = pickLatestBill(billsData);
        }

        const dashboardData = mergeDashboardData(userData, latestBill);
        if (dashboardRoot) {
            applyDashboardData(dashboardData);
        }

    } catch (error) {
        console.error("Lỗi khi tải thông tin người dùng:", error);
    }
}

// Khởi chạy — xem cuối file
// ==================== NOTIFICATIONS ====================

// Dữ liệu cache để không phải gọi API lại khi mở dropdown
let _cachedNotifications = [];
let _notifDropdownOpen = false;

function getNotifIconStyle(type) {
    const t = String(type || '').toUpperCase();
    if (t.includes('WARN') || t.includes('ALERT'))
        return { bg: '#fff7ed', color: '#f97316', svg: '<path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>' };
    if (t.includes('BILL') || t.includes('PAYMENT'))
        return { bg: '#f0fdf4', color: '#22c55e', svg: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>' };
    if (t.includes('CHAT') || t.includes('MESSAGE'))
        return { bg: '#eff6ff', color: '#3b82f6', svg: '<path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>' };
    return { bg: '#eff6ff', color: '#3b82f6', svg: '<circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/>' };
}

function relativeTime(dateStr) {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1) return 'Vừa xong';
    if (m < 60) return `${m} phút trước`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h} giờ trước`;
    const d = Math.floor(h / 24);
    if (d === 1) return 'Hôm qua';
    return `${d} ngày trước`;
}

function renderNotifDropdown(notifications) {
    const list = document.getElementById('notif-dropdown-list');
    if (!list) return;

    if (!notifications.length) {
        list.innerHTML = `<div style="padding:40px 20px;text-align:center;color:#94a3b8;">
            <svg style="margin:0 auto 10px;display:block;" width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#cbd5e1" stroke-width="1.5">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <p style="margin:0;font-size:13px;">Không có thông báo mới</p>
        </div>`;
        return;
    }

    const isChatType = (type) => String(type || '').toUpperCase().includes('CHAT') || String(type || '').toUpperCase().includes('MESSAGE');

    list.innerHTML = notifications.map(item => {
        const s = getNotifIconStyle(item.notificationType);
        const time = relativeTime(item.createdAt);
        const isUnread = !item.isRead;
        const isChat = isChatType(item.notificationType);
        const clickAction = isChat
            ? `onclick="openChat();closeNotifDropdown();"`
            : `onclick="markItemRead(${item.id})"`;

        return `
        <div class="notif-item ${isUnread ? 'unread' : ''}" ${clickAction} data-id="${item.id}">
            <div class="notif-icon" style="background:${s.bg};">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="${s.color}" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">${s.svg}</svg>
            </div>
            <div style="flex:1;min-width:0;">
                <p class="notif-title">${item.title || 'Thông báo'}</p>
                <p class="notif-content">${item.content || ''}</p>
                ${time ? `<p class="notif-time">${time}</p>` : ''}
            </div>
            ${isUnread ? '<div class="notif-unread-dot"></div>' : ''}
        </div>`;
    }).join('');
}

function renderNotifSidebar(notifications) {
    // Render vào card bên phải của dashboard (giữ nguyên như cũ)
    const list = el.notificationList;
    if (!list || !notifications.length) return;
    list.innerHTML = notifications.slice(0, 5).map((item, i) => {
        const s = getNotifIconStyle(item.notificationType);
        const time = relativeTime(item.createdAt);
        return `
            ${i > 0 ? '<div class="h-px w-full bg-gray-50"></div>' : ''}
            <div class="flex gap-3 items-start group cursor-pointer" onclick="toggleNotifDropdown()">
                <div class="mt-0.5 grid place-items-center w-8 h-8 rounded-full shrink-0 group-hover:opacity-80 transition-opacity" style="background:${s.bg};">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="${s.color}" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">${s.svg}</svg>
                </div>
                <div>
                    <p class="font-semibold text-gray-800 text-sm m-0 group-hover:text-blue-600 transition-colors">${item.title || 'Thông báo'}</p>
                    <p class="text-xs text-gray-500 mt-1 m-0 line-clamp-2 leading-relaxed">${item.content || ''}</p>
                    ${time ? `<p class="text-[10px] font-medium text-gray-400 mt-1.5 m-0 uppercase tracking-wide">${time}</p>` : ''}
                </div>
            </div>`;
    }).join('');
}

async function loadNotifications() {
    try {
        const res = await fetch('/api/notifications', { credentials: 'include' });
        if (!res.ok) return;
        const notifications = await res.json();
        if (!Array.isArray(notifications)) return;

        _cachedNotifications = notifications;
        const unread = notifications.filter(n => !n.isRead).length;

        // Cập nhật badge chuông
        const dot = document.getElementById('notification-dot');
        const badge = document.getElementById('notif-badge');
        if (dot) dot.style.display = unread > 0 ? 'flex' : 'none';
        if (badge) {
            badge.textContent = unread;
            badge.style.display = unread > 0 ? 'inline' : 'none';
        }
        if (el.unreadCount) el.unreadCount.textContent = `(${unread})`;

        // Render dropdown list sẵn
        renderNotifDropdown(notifications);
        // Render sidebar card
        renderNotifSidebar(notifications);

    } catch (e) {
        console.error('Lỗi load notifications:', e);
    }
}

function toggleNotifDropdown() {
    _notifDropdownOpen ? closeNotifDropdown() : openNotifDropdown();
}

function openNotifDropdown() {
    const d = document.getElementById('notif-dropdown');
    if (!d) return;
    _notifDropdownOpen = true;
    d.style.display = 'block';
    // Nếu chưa có data thì loading state vẫn hiển thị cho đến khi loadNotifications xong
    if (_cachedNotifications.length > 0) {
        renderNotifDropdown(_cachedNotifications);
    }
    // Đóng user dropdown nếu đang mở
    setUserMenuState(false);
}

function closeNotifDropdown() {
    const d = document.getElementById('notif-dropdown');
    if (d) d.style.display = 'none';
    _notifDropdownOpen = false;
}

function markItemRead(id) {
    // Gọi API backend đánh dấu đã đọc
    fetch(`/api/notifications/${id}/read`, { method: 'PATCH', credentials: 'include' })
        .then(() => {
            _cachedNotifications = _cachedNotifications.map(n => n.id === id ? { ...n, isRead: true } : n);
            renderNotifDropdown(_cachedNotifications);
            // Cập nhật lại badge
            const unread = _cachedNotifications.filter(n => !n.isRead).length;
            const badge = document.getElementById('notif-badge');
            const dot = document.getElementById('notification-dot');
            if (badge) { badge.textContent = unread; badge.style.display = unread > 0 ? 'inline' : 'none'; }
            if (dot) dot.style.display = unread > 0 ? 'flex' : 'none';
        }).catch(() => { });
}

function markAllRead() {
    fetch('/api/notifications/read-all', { method: 'PATCH', credentials: 'include' })
        .then(() => {
            _cachedNotifications = _cachedNotifications.map(n => ({ ...n, isRead: true }));
            renderNotifDropdown(_cachedNotifications);
            const badge = document.getElementById('notif-badge');
            const dot = document.getElementById('notification-dot');
            if (badge) { badge.style.display = 'none'; }
            if (dot) dot.style.display = 'none';
            if (el.unreadCount) el.unreadCount.textContent = '(0)';
        }).catch(() => { });
}

// Đóng dropdown khi click ra ngoài
window.addEventListener('click', function (e) {
    const menu = document.getElementById('notif-menu');
    if (menu && !menu.contains(e.target) && _notifDropdownOpen) {
        closeNotifDropdown();
    }
});

// ==================== INBOX (danh sách hội thoại) ====================

let _inboxOpen = false;

function toggleInboxDropdown() {
    _inboxOpen ? closeInboxDropdown() : openInboxDropdown();
}

function openInboxDropdown() {
    _inboxOpen = true;
    document.getElementById("inbox-dropdown").style.display = "block";
    closeNotifDropdown();
    setUserMenuState(false);
    loadInboxConversations();
}

function closeInboxDropdown() {
    _inboxOpen = false;
    const d = document.getElementById("inbox-dropdown");
    if (d) d.style.display = "none";
}

window.addEventListener("click", function (e) {
    const menu = document.getElementById("inbox-menu");
    if (menu && !menu.contains(e.target) && _inboxOpen) closeInboxDropdown();
}, true);

function relativeTimeInbox(dateStr) { return relativeTime(dateStr); }

async function loadInboxConversations() {
    const listEl = document.getElementById('inbox-list');
    if (!listEl) return;
    listEl.innerHTML = '<div style="padding:32px;text-align:center;color:#94a3b8;font-size:13px;">Đang tải...</div>';
    try {
        const res = await fetch('/api/chat/conversations', { credentials: 'include' });
        if (!res.ok) { listEl.innerHTML = renderInboxEmpty('Không thể tải tin nhắn'); return; }
        const conversations = await res.json();

        const totalUnread = conversations.reduce((s, c) => s + (c.unreadCount || 0), 0);
        const dot = document.getElementById('inbox-dot');
        const badge = document.getElementById('inbox-unread-badge');
        if (dot) dot.style.display = totalUnread > 0 ? 'block' : 'none';
        if (badge) { badge.style.display = totalUnread > 0 ? 'inline' : 'none'; badge.textContent = totalUnread; }

        if (!conversations.length) { listEl.innerHTML = renderInboxEmpty('Chưa có cuộc trò chuyện nào'); return; }
        listEl.innerHTML = conversations.map(renderConversationItem).join('');
    } catch (e) {
        listEl.innerHTML = renderInboxEmpty('Lỗi kết nối');
        console.error('Lỗi load inbox:', e);
    }
}

function renderConversationItem(conv) {
    const name = escapeHtml(conv.partnerName || 'Người dùng');
    const lastMsg = escapeHtml(conv.lastMessage || 'Chưa có tin nhắn');
    const time = relativeTime(conv.lastMessageAt);
    const unread = conv.unreadCount || 0;
    const avatar = (conv.partnerName || 'U').charAt(0).toUpperCase();
    const isLandlord = String(conv.partnerRole || '').toUpperCase().includes('LANDLORD');
    const avatarBg = isLandlord ? '#1e3a8a' : '#0891b2';
    const roleBadge = isLandlord
        ? '<span style="font-size:10px;background:#dbeafe;color:#1e40af;padding:1px 6px;border-radius:4px;font-weight:600;">Chủ nhà</span>'
        : '';
    const unreadBg = unread > 0 ? '#f8fbff' : '#fff';

    return `
        <div onclick="openChatWithPartner(${conv.partnerId},'${name}');closeInboxDropdown();"
            style="display:flex;gap:12px;padding:12px 16px;cursor:pointer;border-bottom:1px solid #f8fafc;background:${unreadBg};transition:background .15s;"
            onmouseover="this.style.background='#f0f4ff'" onmouseout="this.style.background='${unreadBg}'">
            <div style="position:relative;flex-shrink:0;">
                <div style="width:42px;height:42px;border-radius:50%;background:${avatarBg};color:#fff;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:16px;">${avatar}</div>
                <span style="position:absolute;bottom:1px;right:1px;width:10px;height:10px;background:#22c55e;border-radius:50%;border:2px solid #fff;"></span>
            </div>
            <div style="flex:1;min-width:0;">
                <div style="display:flex;align-items:center;justify-content:space-between;gap:4px;">
                    <div style="display:flex;align-items:center;gap:6px;min-width:0;">
                        <span style="font-size:13px;font-weight:${unread > 0 ? 700 : 600};color:#1e293b;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${name}</span>
                        ${roleBadge}
                    </div>
                    <span style="font-size:10px;color:#94a3b8;white-space:nowrap;flex-shrink:0;">${time}</span>
                </div>
                <div style="display:flex;align-items:center;justify-content:space-between;margin-top:2px;">
                    <p style="margin:0;font-size:12px;color:${unread > 0 ? '#374151' : '#94a3b8'};font-weight:${unread > 0 ? 500 : 400};overflow:hidden;text-overflow:ellipsis;white-space:nowrap;max-width:200px;">${lastMsg}</p>
                    ${unread > 0 ? `<span style="min-width:18px;height:18px;background:#3b82f6;color:#fff;border-radius:999px;font-size:10px;font-weight:700;display:flex;align-items:center;justify-content:center;padding:0 5px;flex-shrink:0;">${unread}</span>` : ''}
                </div>
            </div>
        </div>`;
}

function renderInboxEmpty(msg) {
    return '<div style="display:flex;flex-direction:column;align-items:center;padding:36px 16px;gap:10px;color:#94a3b8;"><svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg><p style="margin:0;font-size:13px;">' + msg + '</p></div>';
}

function openChatWithPartner(partnerId, partnerName) {
    const nameEl = document.getElementById('chat-landlord-name');
    const avatarEl = document.getElementById('chat-landlord-avatar');
    if (nameEl) nameEl.textContent = partnerName;
    if (avatarEl) avatarEl.textContent = (partnerName || 'U').charAt(0).toUpperCase();
    window._chatPartnerId = partnerId;
    openChat();
    const tryLoad = () => {
        if (chatStomp && chatStomp.connected && chatCurrentUserId) {
            clearChatMessages();
            chatStomp.publish({
                destination: '/app/chat.history',
                body: JSON.stringify({ senderId: chatCurrentUserId, receiverId: partnerId })
            });
        } else {
            setTimeout(tryLoad, 300);
        }
    };
    tryLoad();
}



function renderInboxEmpty(msg) {
    return '<div style="display:flex;flex-direction:column;align-items:center;padding:36px 16px;gap:10px;color:#94a3b8;"><svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="#d1d5db" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg><p style="margin:0;font-size:13px;">' + msg + '</p></div>';
}


// ==================== CHAT WIDGET ====================

let chatStomp = null;
let chatCurrentUserId = null;
let chatOpen = false;

function toggleChat() {
    chatOpen ? closeChat() : openChat();
}

function openChat() {
    chatOpen = true;
    document.getElementById('chat-toggle-btn').style.display = 'none';
    const panel = document.getElementById('chat-panel');
    panel.style.display = 'flex';
    if (!chatStomp || !chatStomp.connected) {
        initChatWidget();
    }
    document.getElementById('chat-input').focus();
}

function closeChat() {
    chatOpen = false;
    document.getElementById('chat-panel').style.display = 'none';
    document.getElementById('chat-toggle-btn').style.display = 'flex';
    // Reset về mặc định để lần sau mở lại sẽ chat với chủ nhà
    window._chatPartnerId = null;
    // Reset tên header
    const nameEl = document.getElementById('chat-landlord-name');
    if (nameEl) nameEl.textContent = 'Chủ nhà';
}

async function initChatWidget() {
    setChatStatus('Đang kết nối...');
    try {
        const res = await fetch('/api/user/myid', { credentials: 'include' });
        if (!res.ok) { setChatStatus('Lỗi xác thực'); return; }
        chatCurrentUserId = await res.json();
    } catch (e) {
        setChatStatus('Không lấy được thông tin');
        return;
    }

    chatStomp = new StompJs.Client({
        webSocketFactory: () => new SockJS('/gs-guide-websocket'),
        onConnect: async () => {
            setChatStatus('Trực tuyến');
            document.getElementById('chat-status-text').style.color = 'rgba(134,239,172,0.9)';

            chatStomp.subscribe('/user/queue/private', (msg) => {
                renderChatMessage(JSON.parse(msg.body));
            });
            chatStomp.subscribe('/user/queue/history', (msg) => {
                clearChatMessages();
                JSON.parse(msg.body).forEach(renderChatMessage);
            });

            // Nếu chưa có partner cụ thể (mở từ nút "Nhắn tin chủ nhà")
            // → tự fetch landlordId từ backend
            if (!window._chatPartnerId) {
                try {
                    const res = await fetch('/api/user/landlord-id', { credentials: 'include' });
                    if (res.ok) {
                        window._chatPartnerId = await res.json();
                        // Cập nhật tên chủ nhà lên header chat
                        const nameEl = document.getElementById('chat-landlord-name');
                        if (nameEl && nameEl.textContent === 'Chủ nhà') {
                            nameEl.textContent = 'Chủ nhà';  // giữ nguyên, hoặc fetch tên nếu cần
                        }
                    }
                } catch (e) {
                    console.warn('Không lấy được landlord ID', e);
                }
            }

            // Load lịch sử chat với partner
            if (window._chatPartnerId) {
                chatStomp.publish({
                    destination: '/app/chat.history',
                    body: JSON.stringify({
                        senderId: chatCurrentUserId,
                        receiverId: window._chatPartnerId
                    })
                });
            }
        },
        onDisconnect: () => setChatStatus('Mất kết nối'),
        onStompError: () => setChatStatus('Lỗi kết nối'),
    });

    chatStomp.activate();
}

function sendChatMessage() {
    const input = document.getElementById('chat-input');
    const content = input.value.trim();
    if (!content || !chatStomp || !chatStomp.connected) return;

    chatStomp.publish({
        destination: '/app/chat.private',
        body: JSON.stringify({
            senderId: chatCurrentUserId,
            receiverId: window._chatPartnerId || 0,  // 0 → backend tự resolve chủ nhà
            content: content,
            messageType: 'TEXT',
            chatType: 'PRIVATE'
        })
    });

    input.value = '';
    input.style.height = 'auto';
}

function renderChatMessage(msg) {
    const messagesEl = document.getElementById('chat-messages');
    const emptyState = document.getElementById('chat-empty-state');
    if (emptyState) emptyState.style.display = 'none';

    const isMe = msg.senderId === chatCurrentUserId;
    const time = msg.sentAt
        ? new Date(msg.sentAt).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
        : new Date().toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });

    const wrapper = document.createElement('div');
    wrapper.className = isMe ? 'chat-msg-me' : 'chat-msg-them';
    wrapper.innerHTML = `
        <div style="display:flex;flex-direction:column;align-items:${isMe ? 'flex-end' : 'flex-start'}">
            <div class="${isMe ? 'chat-bubble-me' : 'chat-bubble-them'}">${escapeHtml(msg.content)}</div>
            <span class="chat-meta">${time}</span>
        </div>`;

    // Chèn trước empty state nếu còn đó, hoặc append
    messagesEl.appendChild(wrapper);
    messagesEl.scrollTop = messagesEl.scrollHeight;
}

function clearChatMessages() {
    const el = document.getElementById('chat-messages');
    // Giữ lại empty state, xóa message cũ
    el.querySelectorAll('.chat-msg-me, .chat-msg-them').forEach(n => n.remove());
}

function setChatStatus(text) {
    const s = document.getElementById('chat-status-text');
    if (s) { s.textContent = text; s.style.color = 'rgba(255,255,255,0.75)'; }
}

function escapeHtml(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
}

// ==================== KHỞI CHẠY ====================

loadDashboard();
loadNotifications();
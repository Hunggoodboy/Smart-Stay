const API_URL = '/api/landlord/posts';
const PUBLIC_API_URL = '/room-posted';

const getToken = () => localStorage.getItem('smartstay_token');

let cachedPosts = [];

const filterEls = {
    search: document.getElementById('houseSearchInput'),
    status: document.getElementById('houseStatusFilter'),
    sort: document.getElementById('houseSortSelect'),
    count: document.getElementById('houseCountLabel'),
    tableBody: document.getElementById('house-management-body')
};

function setText(node, value) {
    if (node) {
        node.textContent = value;
    }
}

function normalizeText(value) {
    return String(value || '').toLowerCase().trim();
}

function toNumber(value) {
    const num = Number(value);
    return Number.isFinite(num) ? num : 0;
}

function getTimestamp(value) {
    const date = new Date(value || 0);
    return Number.isNaN(date.getTime()) ? 0 : date.getTime();
}

function getAddress(post) {
    return post.shortAddress || post.address || 'Chưa có địa chỉ';
}

function getStatusMeta(status) {
    const normalized = String(status || 'UNKNOWN').toUpperCase();
    if (normalized === 'ACTIVE') {
        return {
            label: 'Đang hoạt động',
            badgeClass: 'bg-emerald-500',
            tableClass: 'bg-emerald-50 text-emerald-600 border border-emerald-100'
        };
    }
    if (normalized === 'RENTED') {
        return {
            label: 'Đã cho thuê',
            badgeClass: 'bg-amber-500',
            tableClass: 'bg-amber-50 text-amber-700 border border-amber-100'
        };
    }
    if (normalized === 'DRAFT') {
        return {
            label: 'Nháp',
            badgeClass: 'bg-slate-600',
            tableClass: 'bg-slate-100 text-slate-600 border border-slate-200'
        };
    }
    if (normalized === 'INACTIVE') {
        return {
            label: 'Tạm ẩn',
            badgeClass: 'bg-gray-500',
            tableClass: 'bg-gray-100 text-gray-600 border border-gray-200'
        };
    }
    return {
        label: normalized,
        badgeClass: 'bg-gray-500',
        tableClass: 'bg-gray-100 text-gray-600 border border-gray-200'
    };
}

function formatRelativeTime(value) {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    const diffMs = Date.now() - date.getTime();
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1) return 'Vừa xong';
    if (minutes < 60) return `${minutes} phút trước`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `${hours} giờ trước`;
    const days = Math.floor(hours / 24);
    if (days === 1) return 'Hôm qua';
    return `${days} ngày trước`;
}

async function loadLandlordPosts() {
    const listEl = document.getElementById('landlord-posts-list');
    if (!listEl) return;
    listEl.innerHTML = '<div class="text-sm text-gray-500">Đang tải...</div>';
    try {
        const token = getToken();
        const url = token ? API_URL : PUBLIC_API_URL;
        const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
        const res = await fetch(url, { headers, credentials: 'include' });
        if (!res.ok) throw new Error('Fetch error');
        const posts = await res.json();
        cachedPosts = Array.isArray(posts) ? posts : [];

        renderPostCards(cachedPosts);
        applyManagementFilters();
    } catch (err) {
        listEl.innerHTML = '<div class="rounded-2xl border border-red-100 bg-red-50 px-5 py-4 text-sm text-red-600">Lỗi khi tải bài đăng</div>';
        console.error(err);
    }
}

function renderPostCards(posts) {
    const listEl = document.getElementById('landlord-posts-list');
    if (!listEl) return;

    if (!Array.isArray(posts) || posts.length === 0) {
        listEl.innerHTML = `
            <div class="rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-5 py-8 text-center">
                <p class="text-base font-semibold text-gray-800 m-0">Chưa có bài đăng nào</p>
                <p class="mt-2 text-sm text-gray-500">Tạo bài đăng đầu tiên để bắt đầu quản lý nhà của bạn.</p>
                <div class="mt-4">
                    <a href="/postRooms" class="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white no-underline hover:bg-slate-800">Đăng nhà mới</a>
                </div>
            </div>`;
        return;
    }

    listEl.innerHTML = '';
    posts.forEach(p => {
        const statusMeta = getStatusMeta(p.status);
        const card = document.createElement('article');
        card.className = 'group overflow-hidden rounded-[1.5rem] border border-gray-100 bg-white shadow-[0_10px_30px_rgba(15,23,42,0.05)] transition hover:-translate-y-0.5 hover:shadow-[0_18px_40px_rgba(15,23,42,0.08)]';

        const shell = document.createElement('div');
        shell.className = 'grid gap-0 md:grid-cols-[180px_minmax(0,1fr)]';

        const thumbWrap = document.createElement('div');
        thumbWrap.className = 'relative h-48 md:h-full bg-slate-100';
        const thumb = document.createElement('img');
        thumb.src = p.thumbnailUrl || '../static/images/default-room.png';
        thumb.alt = p.title || '';
        thumb.className = 'h-full w-full object-cover';
        const overlay = document.createElement('div');
        overlay.className = 'absolute inset-0 bg-gradient-to-t from-slate-950/30 via-transparent to-transparent';
        const statusTag = document.createElement('span');
        statusTag.className = `absolute left-4 top-4 rounded-full px-3 py-1 text-xs font-bold text-white shadow-sm ${statusMeta.badgeClass}`;
        statusTag.textContent = statusMeta.label;

        thumbWrap.appendChild(thumb);
        thumbWrap.appendChild(overlay);
        thumbWrap.appendChild(statusTag);

        const body = document.createElement('div');
        body.className = 'flex flex-col justify-between gap-4 p-5 md:p-6';

        const top = document.createElement('div');
        top.className = 'space-y-3';

        const info = document.createElement('div');
        info.innerHTML = `
            <div class="flex flex-wrap items-center gap-2 text-xs font-semibold uppercase tracking-[0.18em] text-gray-400">
                <span>${escapeHtml(p.roomType || 'Loại phòng')}</span>
                <span class="h-1 w-1 rounded-full bg-gray-300"></span>
                <span>${p.areaM2 ? `${p.areaM2} m²` : 'Chưa rõ diện tích'}</span>
            </div>
            <h3 class="mt-2 text-xl font-bold leading-tight text-gray-900">${escapeHtml(p.title || '')}</h3>
            <p class="mt-2 text-sm leading-6 text-gray-500">${escapeHtml(getAddress(p))}</p>
            <div class="mt-4 flex flex-wrap items-center gap-3 text-sm font-semibold text-gray-700">
                <span class="inline-flex items-center gap-1 rounded-full bg-slate-50 px-3 py-1.5">
                    <div class="i-mdi-currency-vnd text-slate-500"></div>
                    ${formatMoney(p.monthlyRent)} / tháng
                </span>
                <span class="inline-flex items-center gap-1 rounded-full bg-slate-50 px-3 py-1.5">
                    <div class="i-mdi-image-multiple-outline text-slate-500"></div>
                    Ảnh đại diện
                </span>
            </div>`;

        top.appendChild(info);

        const actions = document.createElement('div');
        actions.className = 'flex flex-wrap gap-2';

        const viewBtn = document.createElement('a');
        viewBtn.href = `/rooms/${p.id}`;
        viewBtn.className = 'inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-slate-800 no-underline';
        viewBtn.textContent = 'Xem chi tiết';

        const editBtn = document.createElement('a');
        editBtn.href = '/postRooms';
        editBtn.className = 'inline-flex items-center justify-center rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 transition hover:bg-gray-50 no-underline';
        editBtn.textContent = 'Mở form đăng';

        const copyBtn = document.createElement('button');
        copyBtn.type = 'button';
        copyBtn.className = 'inline-flex items-center justify-center rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 transition hover:bg-gray-50';
        copyBtn.textContent = 'Sao chép link';
        copyBtn.addEventListener('click', function () {
            navigator.clipboard.writeText(`${window.location.origin}/rooms/${p.id}`);
            copyBtn.textContent = 'Đã sao chép';
            setTimeout(function () {
                copyBtn.textContent = 'Sao chép link';
            }, 1200);
        });

        actions.appendChild(viewBtn);
        actions.appendChild(editBtn);
        actions.appendChild(copyBtn);

        body.appendChild(top);
        body.appendChild(actions);

        shell.appendChild(thumbWrap);
        shell.appendChild(body);
        card.appendChild(shell);
        listEl.appendChild(card);
    });
}

function getFilteredPosts(posts) {
    let result = Array.isArray(posts) ? posts.slice() : [];

    const query = normalizeText(filterEls.search && filterEls.search.value);
    if (query) {
        result = result.filter(post => {
            const title = normalizeText(post.title);
            const address = normalizeText(getAddress(post));
            return title.includes(query) || address.includes(query);
        });
    }

    const statusFilter = filterEls.status ? filterEls.status.value : 'all';
    if (statusFilter && statusFilter !== 'all') {
        result = result.filter(post => String(post.status || '').toUpperCase() === statusFilter);
    }

    const sortValue = filterEls.sort ? filterEls.sort.value : 'newest';
    if (sortValue === 'priceAsc') {
        result.sort((a, b) => toNumber(a.monthlyRent) - toNumber(b.monthlyRent));
    } else if (sortValue === 'priceDesc') {
        result.sort((a, b) => toNumber(b.monthlyRent) - toNumber(a.monthlyRent));
    } else {
        result.sort((a, b) => getTimestamp(b.publishedAt || b.createdAt) - getTimestamp(a.publishedAt || a.createdAt));
    }

    return result;
}

function renderManagementTable(posts) {
    if (!filterEls.tableBody) return;

    if (!Array.isArray(posts) || posts.length === 0) {
        filterEls.tableBody.innerHTML = `
            <tr>
                <td class="px-5 py-6 text-sm text-gray-500" colspan="4">Không có nhà nào khớp bộ lọc.</td>
            </tr>`;
        return;
    }

    const rows = posts.map(post => {
        const statusMeta = getStatusMeta(post.status);
        const title = escapeHtml(post.title || 'Chưa có tiêu đề');
        const address = escapeHtml(getAddress(post));
        const price = formatMoney(post.monthlyRent);
        const detailUrl = `/rooms/${post.id}`;
        const copyUrl = `${window.location.origin}${detailUrl}`;
        return `
            <tr>
                <td class="px-5 py-4">
                    <div class="font-semibold text-gray-900">${title}</div>
                    <div class="mt-1 text-xs text-gray-500">${address}</div>
                </td>
                <td class="px-5 py-4 font-semibold text-gray-700">${price}</td>
                <td class="px-5 py-4">
                    <span class="inline-flex items-center rounded-full px-3 py-1 text-xs font-semibold border ${statusMeta.tableClass}">${statusMeta.label}</span>
                </td>
                <td class="px-5 py-4">
                    <div class="flex flex-wrap gap-2">
                        <a href="${detailUrl}" class="rounded-lg bg-slate-900 px-3 py-2 text-xs font-semibold text-white no-underline hover:bg-slate-800">Xem</a>
                        <button type="button" data-copy-url="${copyUrl}" class="rounded-lg border border-gray-200 bg-white px-3 py-2 text-xs font-semibold text-gray-700 hover:bg-gray-50">Sao chép link</button>
                    </div>
                </td>
            </tr>`;
    }).join('');

    filterEls.tableBody.innerHTML = rows;
    filterEls.tableBody.querySelectorAll('[data-copy-url]').forEach(button => {
        button.addEventListener('click', function () {
            const url = button.getAttribute('data-copy-url');
            if (!url) return;
            navigator.clipboard.writeText(url);
            button.textContent = 'Đã sao chép';
            setTimeout(function () {
                button.textContent = 'Sao chép link';
            }, 1200);
        });
    });
}

function applyManagementFilters() {
    const filtered = getFilteredPosts(cachedPosts);
    renderManagementTable(filtered);
    setText(filterEls.count, `${filtered.length} nhà`);
}

function bindFilters() {
    if (bindFilters.bound) return;

    if (filterEls.search) {
        filterEls.search.addEventListener('input', applyManagementFilters);
    }
    if (filterEls.status) {
        filterEls.status.addEventListener('change', applyManagementFilters);
    }
    if (filterEls.sort) {
        filterEls.sort.addEventListener('change', applyManagementFilters);
    }

    bindFilters.bound = true;
}

async function loadUnreadMessages() {
    const listEl = document.getElementById('unreadMessageList');
    const countEl = document.getElementById('unreadMessageCount');
    if (!listEl || !countEl) return;

    const token = getToken();
    if (!token) {
        countEl.textContent = '0';
        listEl.innerHTML = '<div class="rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-4 py-5 text-sm text-gray-500">Đăng nhập để xem tin nhắn.</div>';
        return;
    }

    listEl.innerHTML = '<div class="rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-4 py-5 text-sm text-gray-500">Đang tải tin nhắn...</div>';

    try {
        const res = await fetch('/api/chat/conversations', {
            headers: { 'Authorization': `Bearer ${token}` },
            credentials: 'include'
        });
        if (!res.ok) throw new Error('Fetch error');
        const conversations = await res.json();

        const unreadConversations = Array.isArray(conversations)
            ? conversations.filter(conv => (conv.unreadCount || 0) > 0)
            : [];
        const totalUnread = unreadConversations.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0);
        countEl.textContent = String(totalUnread);

        if (!unreadConversations.length) {
            listEl.innerHTML = '<div class="rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-4 py-5 text-sm text-gray-500">Không có tin nhắn chưa đọc.</div>';
            return;
        }

        listEl.innerHTML = unreadConversations.slice(0, 3).map(conv => {
            const name = escapeHtml(conv.partnerName || 'Khách thuê');
            const avatar = (conv.partnerName || 'K').charAt(0).toUpperCase();
            const lastMessage = escapeHtml(conv.lastMessage || 'Có tin nhắn mới');
            const time = formatRelativeTime(conv.lastMessageAt);
            const unreadCount = conv.unreadCount || 0;
            return `
                <a href="/chatMessage" class="flex items-start gap-3 rounded-2xl border border-gray-100 bg-gray-50 px-4 py-3 no-underline transition hover:border-blue-200 hover:bg-blue-50">
                    <div class="h-10 w-10 flex-shrink-0 overflow-hidden rounded-full bg-white grid place-items-center text-sm font-bold text-blue-600">
                        ${avatar}
                    </div>
                    <div class="flex-1">
                        <div class="flex items-center justify-between gap-2">
                            <p class="text-sm font-semibold text-gray-900 m-0">${name}</p>
                            <span class="rounded-full bg-red-50 px-2 py-0.5 text-[10px] font-semibold text-red-600">${unreadCount}</span>
                        </div>
                        <p class="mt-1 text-xs text-gray-500">"${lastMessage}"</p>
                        <p class="mt-2 text-[11px] font-semibold text-gray-400">${time}</p>
                    </div>
                </a>`;
        }).join('');
    } catch (err) {
        countEl.textContent = '0';
        listEl.innerHTML = '<div class="rounded-2xl border border-red-100 bg-red-50 px-4 py-5 text-sm text-red-600">Lỗi khi tải tin nhắn.</div>';
        console.error(err);
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
}

function formatMoney(val) {
    if (val == null) return '';
    try {
        // assume it's a number or string
        const n = Number(val);
        return n.toLocaleString('vi-VN') + ' VNĐ';
    } catch (e) { return val; }
}

// Khởi chạy khi DOM sẵn sàng (theo chuẩn các file JS khác)
document.addEventListener('DOMContentLoaded', function () {
    bindFilters();
    loadLandlordPosts();
    loadUnreadMessages();
});

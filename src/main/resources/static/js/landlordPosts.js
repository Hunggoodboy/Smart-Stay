const API_URL = '/api/landlord/posts';
const PUBLIC_API_URL = '/room-posted';

const getToken = () => localStorage.getItem('smartstay_token');

let cachedPosts = [];

// --- BIẾN QUẢN LÝ TRẠNG THÁI HIỂN THỊ ---
let currentPostPage = 1;
let visiblePostsCount = 6;
const POSTS_PER_PAGE = 12;

const filterEls = {
    search: document.getElementById('houseSearchInput'),
    status: document.getElementById('houseStatusFilter'),
    sort: document.getElementById('houseSortSelect'),
    count: document.getElementById('houseCountLabel'),
    tableBody: document.getElementById('house-management-body')
};

function setText(node, value) {
    if (node) node.textContent = value;
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
        return { label: 'Đang hoạt động', badgeClass: 'bg-emerald-500', tableClass: 'bg-emerald-50 text-emerald-600 border border-emerald-100' };
    }
    if (normalized === 'RENTED') {
        return { label: 'Đã cho thuê', badgeClass: 'bg-amber-500', tableClass: 'bg-amber-50 text-amber-700 border border-amber-100' };
    }
    if (normalized === 'DRAFT') {
        return { label: 'Nháp', badgeClass: 'bg-slate-600', tableClass: 'bg-slate-100 text-slate-600 border border-slate-200' };
    }
    if (normalized === 'INACTIVE') {
        return { label: 'Tạm ẩn', badgeClass: 'bg-gray-500', tableClass: 'bg-gray-100 text-gray-600 border border-gray-200' };
    }
    return { label: normalized, badgeClass: 'bg-gray-500', tableClass: 'bg-gray-100 text-gray-600 border border-gray-200' };
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

// --- HÀM VẼ CARD ĐÃ TÍCH HỢP PHÂN TRANG ---
function renderPostCards(posts) {
    const listEl = document.getElementById('landlord-posts-list');
    if (!listEl) return;

    listEl.className = '';

    if (!Array.isArray(posts) || posts.length === 0) {
        listEl.innerHTML = `
            <div class="rounded-xl border border-dashed border-gray-200 bg-gray-50 px-5 py-8 text-center col-span-full">
                <p class="text-base font-semibold text-gray-800 m-0">Chưa có bài đăng nào</p>
                <p class="mt-2 text-sm text-gray-500">Tạo bài đăng đầu tiên để bắt đầu quản lý nhà của bạn.</p>
                <div class="mt-4">
                    <a href="/postRooms" class="inline-flex items-center justify-center rounded-lg bg-blue-600 px-4 py-2.5 text-sm font-semibold text-white no-underline hover:bg-blue-700">Đăng nhà mới</a>
                </div>
            </div>`;
        return;
    }

    const totalPages = Math.ceil(posts.length / POSTS_PER_PAGE);
    if (currentPostPage > totalPages) currentPostPage = totalPages || 1;

    const startIndex = (currentPostPage - 1) * POSTS_PER_PAGE;
    const pagePosts = posts.slice(startIndex, startIndex + POSTS_PER_PAGE);
    const postsToShow = pagePosts.slice(0, visiblePostsCount);

    let html = '<div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 pt-4">';

    postsToShow.forEach(p => {
        const statusMeta = getStatusMeta(p.status);
        const typeLabel = escapeHtml(p.roomType || 'Loại phòng').replace(/_/g, ' ');
        const area = p.areaM2 ? `${p.areaM2} m²` : 'Chưa cập nhật';
        const title = escapeHtml(p.title || 'Chưa có tiêu đề');
        const addr = escapeHtml(getAddress(p));
        const price = formatMoney(p.monthlyRent);
        const thumbUrl = p.thumbnailUrl || '../static/images/default-room.png';

        html += `
        <article class="flex flex-col overflow-hidden rounded-2xl border border-gray-200 bg-white shadow-sm transition hover:shadow-md hover:-translate-y-1">
            <div class="relative h-48 w-full bg-slate-100 border-b border-gray-100">
                <img src="${thumbUrl}" alt="Ảnh phòng" class="h-full w-full object-cover">
                <span class="absolute left-3 top-3 rounded-md px-2.5 py-1 text-[11px] font-bold text-white shadow-sm ${statusMeta.badgeClass}">${statusMeta.label}</span>
            </div>
            <div class="flex flex-1 flex-col p-5">
                <div class="flex items-center gap-2 text-[10px] font-bold uppercase tracking-wider text-gray-500 mb-2">
                    <span class="bg-blue-50 px-2 py-0.5 rounded text-blue-700 border border-blue-100">${typeLabel}</span>
                    <span>•</span>
                    <span>${area}</span>
                </div>
                <h3 class="text-base font-bold text-gray-900 leading-snug line-clamp-2 m-0">${title}</h3>
                <p class="mt-2 text-xs text-gray-500 line-clamp-1">${addr}</p>
                <div class="mt-4 text-[15px] font-black text-blue-700">
                    ${price} <span class="text-xs text-gray-400 font-medium">/ tháng</span>
                </div>
                <div class="mt-5 flex gap-3 border-t border-gray-100 pt-4">
                    <a href="/rooms/${p.id}" class="flex-1 text-center rounded-xl bg-slate-900 px-3 py-2.5 text-[13px] font-bold text-white transition hover:bg-slate-800 no-underline">Xem chi tiết</a>
                    <button type="button" onclick="copyPostLink('${p.id}', this)" class="flex-1 text-center rounded-xl border border-gray-200 bg-white px-3 py-2.5 text-[13px] font-bold text-gray-700 transition hover:bg-gray-50 cursor-pointer">Sao chép link</button>
                </div>
            </div>
        </article>`;
    });

    html += '</div>';

    if (visiblePostsCount < pagePosts.length) {
        html += `
        <div class="mt-6 text-center">
            <button onclick="showMorePostsOnPage()" class="inline-flex items-center justify-center rounded-full border border-blue-200 bg-blue-50 px-6 py-2.5 text-sm font-bold text-blue-700 transition hover:bg-blue-100 cursor-pointer">
                Xem thêm (${pagePosts.length - visiblePostsCount} nhà)
                <svg class="ml-2 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M19 9l-7 7-7-7"></path></svg>
            </button>
        </div>`;
    }

    if (totalPages > 1) {
        html += `<div class="mt-8 flex items-center justify-center gap-2">`;
        for (let i = 1; i <= totalPages; i++) {
            if (i === currentPostPage) {
                html += `<button class="flex h-9 w-9 items-center justify-center rounded-lg bg-blue-600 text-sm font-bold text-white shadow-sm">${i}</button>`;
            } else {
                html += `<button onclick="goToPostPage(${i})" class="flex h-9 w-9 items-center justify-center rounded-lg border border-gray-200 bg-white text-sm font-bold text-gray-600 transition hover:bg-gray-50 cursor-pointer">${i}</button>`;
            }
        }
        html += `</div>`;
    }

    listEl.innerHTML = html;
}

// Các hàm cho sự kiện ở nút bấm
window.showMorePostsOnPage = function() {
    visiblePostsCount = POSTS_PER_PAGE;
    renderPostCards(cachedPosts);
};

window.goToPostPage = function(page) {
    currentPostPage = page;
    visiblePostsCount = 6;
    renderPostCards(cachedPosts);
    const listEl = document.getElementById('landlord-posts-list');
    if (listEl) {
        const offset = listEl.getBoundingClientRect().top + window.scrollY - 100;
        window.scrollTo({ top: offset, behavior: 'smooth' });
    }
};

window.copyPostLink = function(id, btn) {
    navigator.clipboard.writeText(`${window.location.origin}/rooms/${id}`);
    const originalText = btn.textContent;
    const originalClass = btn.className;
    btn.textContent = 'Đã chép!';
    btn.className = 'flex-1 text-center rounded-xl bg-emerald-50 px-3 py-2.5 text-[13px] font-bold text-emerald-700 border border-emerald-200 transition cursor-pointer';
    setTimeout(function () {
        btn.textContent = originalText;
        btn.className = originalClass;
    }, 1500);
};

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
        filterEls.tableBody.innerHTML = `<tr><td class="px-5 py-6 text-sm text-gray-500" colspan="4">Không có nhà nào khớp bộ lọc.</td></tr>`;
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
            setTimeout(() => button.textContent = 'Sao chép link', 1200);
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
    if (filterEls.search) filterEls.search.addEventListener('input', applyManagementFilters);
    if (filterEls.status) filterEls.status.addEventListener('change', applyManagementFilters);
    if (filterEls.sort) filterEls.sort.addEventListener('change', applyManagementFilters);
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
        const res = await fetch('/api/chat/conversations', { headers: { 'Authorization': `Bearer ${token}` }, credentials: 'include' });
        if (!res.ok) throw new Error('Fetch error');
        const conversations = await res.json();
        const unreadConversations = Array.isArray(conversations) ? conversations.filter(conv => (conv.unreadCount || 0) > 0) : [];
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
                    <div class="h-10 w-10 flex-shrink-0 overflow-hidden rounded-full bg-white grid place-items-center text-sm font-bold text-blue-600">${avatar}</div>
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
    return String(str).replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');
}

function formatMoney(val) {
    if (val == null) return '';
    try {
        const n = Number(val);
        return n.toLocaleString('vi-VN') + ' VNĐ';
    } catch (e) { return val; }
}

document.addEventListener('DOMContentLoaded', function () {
    bindFilters();
    loadLandlordPosts();
    loadUnreadMessages();
});
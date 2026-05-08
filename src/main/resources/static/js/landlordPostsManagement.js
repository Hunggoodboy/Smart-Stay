document.addEventListener('DOMContentLoaded', () => {
    const API_POSTS = '/api/landlord/posts';
    const API_REQUESTS = '/api/landlord/requests';

    const postGrid = document.getElementById('landlord-posts-grid');
    const postCountLabel = document.getElementById('houseCountLabel');
    const searchInput = document.getElementById('houseSearchInput');
    const statusSelect = document.getElementById('houseStatusFilter');
    const sortSelect = document.getElementById('houseSortSelect');

    const requestTotalLabel = document.getElementById('requestTotalLabel');
    const requestPendingLabel = document.getElementById('requestPendingLabel');
    const requestApprovedLabel = document.getElementById('requestApprovedLabel');
    const requestRejectedLabel = document.getElementById('requestRejectedLabel');
    const requestContractedLabel = document.getElementById('requestContractedLabel');
    const requestList = document.getElementById('requestSummaryList');

    if (!postGrid) {
        return;
    }

    let cachedPosts = [];
    let cachedRequests = [];

    const getToken = () => {
        try {
            return localStorage.getItem('smartstay_token');
        } catch (err) {
            return null;
        }
    };

    const buildAuthHeaders = () => {
        const token = getToken();
        return token ? { Authorization: `Bearer ${token}` } : {};
    };

    const escapeHtml = (value) => {
        if (!value) return '';
        return String(value)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;');
    };

    const formatMoney = (value) => {
        if (value == null) return 'Chưa cập nhật';
        const num = Number(value);
        if (!Number.isFinite(num)) return String(value);
        return `${num.toLocaleString('vi-VN')} VNĐ`;
    };

    const formatMonthlyRent = (value) => {
        const base = formatMoney(value);
        return base === 'Chưa cập nhật' ? base : `${base} / tháng`;
    };

    const formatArea = (value) => (value ? `${value} m²` : 'Chưa cập nhật');

    const normalizeText = (value) => String(value || '').toLowerCase().trim();

    const toNumber = (value) => {
        const num = Number(value);
        return Number.isFinite(num) ? num : 0;
    };

    const getTimestamp = (value) => {
        const date = new Date(value || 0);
        return Number.isNaN(date.getTime()) ? 0 : date.getTime();
    };

    const getAddress = (post) => post.shortAddress || post.fullAddress || post.address || 'Chưa có địa chỉ';

    const POST_STATUS_META = {
        ACTIVE: {
            label: 'Đang hoạt động',
            badge: 'text-red-100/80',
            gradient: 'linear-gradient(135deg, #0f172a 0%, #1d4ed8 55%, #38bdf8 100%)',
            priceClass: 'bg-emerald-50 text-emerald-700'
        },
        RENTED: {
            label: 'Đã cho thuê',
            badge: 'text-violet-100/80',
            gradient: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 55%, #c084fc 100%)',
            priceClass: 'bg-amber-50 text-amber-700'
        },
        DRAFT: {
            label: 'Nháp',
            badge: 'text-teal-100/80',
            gradient: 'linear-gradient(135deg, #0f766e 0%, #14b8a6 55%, #99f6e4 100%)',
            priceClass: 'bg-slate-100 text-slate-700'
        },
        INACTIVE: {
            label: 'Tạm ẩn',
            badge: 'text-gray-100/80',
            gradient: 'linear-gradient(135deg, #475569 0%, #64748b 55%, #cbd5f5 100%)',
            priceClass: 'bg-gray-100 text-gray-600'
        }
    };

    const REQUEST_STATUS_META = {
        PENDING: { label: 'Chờ xử lý', badge: 'bg-amber-50 text-amber-700 border-amber-100' },
        APPROVED: { label: 'Đã chấp nhận', badge: 'bg-emerald-50 text-emerald-700 border-emerald-100' },
        REJECTED: { label: 'Đã từ chối', badge: 'bg-rose-50 text-rose-700 border-rose-100' },
        CONTRACTED: { label: 'Đã hoàn tất', badge: 'bg-slate-100 text-slate-700 border-slate-200' },
        CANCELLED: { label: 'Đã hủy', badge: 'bg-slate-100 text-slate-700 border-slate-200' }
    };

    const getPostStatusMeta = (status) => {
        const key = String(status || 'INACTIVE').toUpperCase();
        return POST_STATUS_META[key] || POST_STATUS_META.INACTIVE;
    };

    const getRequestStatusMeta = (status) => {
        const key = String(status || 'PENDING').toUpperCase();
        return REQUEST_STATUS_META[key] || REQUEST_STATUS_META.PENDING;
    };

    const setPostCount = (value) => {
        if (postCountLabel) {
            postCountLabel.textContent = `${value} bài đăng`;
        }
    };

    const renderPostCards = (posts) => {
        if (!Array.isArray(posts) || posts.length === 0) {
            postGrid.innerHTML = `
                <div class="col-span-full rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-5 py-8 text-center">
                    <p class="text-base font-semibold text-gray-800 m-0">Chưa có bài đăng nào</p>
                    <p class="mt-2 text-sm text-gray-500">Tạo bài đăng đầu tiên để quản lý nhà.</p>
                    <div class="mt-4">
                        <a href="/postRooms" class="inline-flex items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white no-underline hover:bg-slate-800">Đăng bài mới</a>
                    </div>
                </div>`;
            return;
        }

        postGrid.innerHTML = '';
        posts.forEach((post) => {
            const meta = getPostStatusMeta(post.status);
            const title = escapeHtml(post.title || 'Chưa có tiêu đề');
            const address = escapeHtml(getAddress(post));
            const price = formatMonthlyRent(post.monthlyRent);
            const area = formatArea(post.areaM2);

            const card = document.createElement('article');
            card.className = 'overflow-hidden rounded-2xl border border-gray-100 bg-white shadow-sm transition hover:-translate-y-0.5 hover:shadow-md';
            card.innerHTML = `
                <div class="flex h-40 items-end p-4 text-white" style="background: ${meta.gradient};">
                    <div>
                        <p class="m-0 text-xs font-semibold uppercase tracking-[0.2em] ${meta.badge}">${meta.label}</p>
                        <h4 class="m-0 mt-2 text-lg font-bold">${title}</h4>
                        <p class="m-0 mt-1 text-sm text-white/90">${address}</p>
                    </div>
                </div>
                <div class="space-y-4 p-4">
                    <div class="flex items-center justify-between gap-3 text-sm">
                        <span class="rounded-full px-3 py-1 font-semibold ${meta.priceClass}">${price}</span>
                        <span class="text-gray-500">${area}</span>
                    </div>
                    <div class="flex flex-wrap gap-2">
                        <a href="/rooms/${post.id}" class="inline-flex flex-1 items-center justify-center rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white no-underline">Xem chi tiết</a>
                        <a href="/postRooms" class="inline-flex items-center justify-center rounded-xl border border-gray-200 bg-white px-4 py-2.5 text-sm font-semibold text-gray-700 no-underline">Chỉnh sửa</a>
                    </div>
                </div>`;
            postGrid.appendChild(card);
        });
    };

    const filterPosts = (posts) => {
        let result = Array.isArray(posts) ? posts.slice() : [];

        const query = normalizeText(searchInput && searchInput.value);
        if (query) {
            result = result.filter((post) => {
                const title = normalizeText(post.title);
                const address = normalizeText(getAddress(post));
                return title.includes(query) || address.includes(query);
            });
        }

        const statusValue = statusSelect ? statusSelect.value : 'all';
        if (statusValue && statusValue !== 'all') {
            result = result.filter((post) => String(post.status || '').toUpperCase() === statusValue);
        }

        const sortValue = sortSelect ? sortSelect.value : 'newest';
        if (sortValue === 'priceAsc') {
            result.sort((a, b) => toNumber(a.monthlyRent) - toNumber(b.monthlyRent));
        } else if (sortValue === 'priceDesc') {
            result.sort((a, b) => toNumber(b.monthlyRent) - toNumber(a.monthlyRent));
        } else {
            result.sort((a, b) => getTimestamp(b.publishedAt || b.createdAt) - getTimestamp(a.publishedAt || a.createdAt));
        }

        return result;
    };

    const applyPostFilters = () => {
        const filtered = filterPosts(cachedPosts);
        renderPostCards(filtered);
        setPostCount(filtered.length);
    };

    const loadPosts = async () => {
        postGrid.innerHTML = `
            <div class="col-span-full rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-5 py-8 text-center text-sm text-gray-500">
                Đang tải bài đăng...
            </div>`;
        try {
            const res = await fetch(API_POSTS, {
                headers: buildAuthHeaders(),
                credentials: 'include'
            });
            if (!res.ok) throw new Error('Không thể tải bài đăng');
            const data = await res.json();
            cachedPosts = Array.isArray(data) ? data : [];
            applyPostFilters();
        } catch (err) {
            postGrid.innerHTML = `
                <div class="col-span-full rounded-2xl border border-red-100 bg-red-50 px-5 py-8 text-center text-sm text-red-600">
                    Lỗi khi tải bài đăng.
                </div>`;
            console.error(err);
        }
    };

    const formatDate = (value) => {
        if (!value) return 'Chưa rõ';
        const date = new Date(value);
        if (Number.isNaN(date.getTime())) return 'Chưa rõ';
        return date.toLocaleDateString('vi-VN');
    };

    const renderRequestSummary = (requests) => {
        const list = Array.isArray(requests) ? requests : [];
        const counts = {
            PENDING: 0,
            APPROVED: 0,
            REJECTED: 0,
            CONTRACTED: 0
        };

        list.forEach((req) => {
            const key = String(req.status || 'PENDING').toUpperCase();
            if (counts[key] != null) {
                counts[key] += 1;
            }
        });

        if (requestTotalLabel) requestTotalLabel.textContent = String(list.length);
        if (requestPendingLabel) requestPendingLabel.textContent = String(counts.PENDING);
        if (requestApprovedLabel) requestApprovedLabel.textContent = String(counts.APPROVED);
        if (requestRejectedLabel) requestRejectedLabel.textContent = String(counts.REJECTED);
        if (requestContractedLabel) requestContractedLabel.textContent = String(counts.CONTRACTED);

        if (!requestList) {
            return;
        }

        if (list.length === 0) {
            requestList.innerHTML = `
                <div class="rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-4 py-5 text-sm text-gray-500">
                    Chưa có yêu cầu nào.
                </div>`;
            return;
        }

        requestList.innerHTML = list.slice(0, 6).map((req) => {
            const statusMeta = getRequestStatusMeta(req.status);
            const customerName = escapeHtml(req.customer && req.customer.fullName ? req.customer.fullName : 'Khách thuê');
            const postTitle = escapeHtml(req.roomPost && req.roomPost.title ? req.roomPost.title : 'Bài đăng');
            const createdAt = formatDate(req.createdAt);
            const isPending = String(req.status || '').toUpperCase() === 'PENDING';

            const actionButtons = isPending
                ? `
                    <div class="mt-3 flex gap-2">
                        <button type="button" data-request-id="${req.id}" data-request-action="APPROVED" class="flex-1 rounded-xl bg-emerald-500 px-3 py-2 text-xs font-semibold text-white">Chấp nhận</button>
                        <button type="button" data-request-id="${req.id}" data-request-action="REJECTED" class="flex-1 rounded-xl bg-rose-100 px-3 py-2 text-xs font-semibold text-rose-700">Từ chối</button>
                    </div>
                `
                : `
                    <div class="mt-3 text-xs text-slate-400">Đã xử lý</div>
                `;

            return `
                <div class="rounded-2xl border border-slate-100 bg-white px-4 py-4 shadow-sm">
                    <div class="flex items-start justify-between gap-3">
                        <div>
                            <p class="m-0 text-xs font-semibold uppercase text-slate-400">${postTitle}</p>
                            <p class="m-0 mt-2 text-sm font-semibold text-slate-900">${customerName}</p>
                            <p class="m-0 mt-1 text-xs text-slate-500">Gửi lúc ${createdAt}</p>
                        </div>
                        <span class="rounded-full border px-3 py-1 text-[10px] font-semibold ${statusMeta.badge}">${statusMeta.label}</span>
                    </div>
                    ${actionButtons}
                </div>`;
        }).join('');
    };

    const loadRequests = async () => {
        if (!requestList) return;
        requestList.innerHTML = `
            <div class="rounded-2xl border border-dashed border-gray-200 bg-gray-50 px-4 py-5 text-sm text-gray-500">
                Đang tải yêu cầu...
            </div>`;
        try {
            const res = await fetch(API_REQUESTS, {
                headers: buildAuthHeaders(),
                credentials: 'include'
            });
            if (!res.ok) throw new Error('Không thể tải yêu cầu');
            const data = await res.json();
            cachedRequests = Array.isArray(data) ? data : [];
            renderRequestSummary(cachedRequests);
        } catch (err) {
            requestList.innerHTML = `
                <div class="rounded-2xl border border-red-100 bg-red-50 px-4 py-5 text-sm text-red-600">
                    Lỗi khi tải yêu cầu.
                </div>`;
            console.error(err);
        }
    };

    const updateRequestStatus = async (requestId, status) => {
        if (!requestId || !status) return;
        try {
            const res = await fetch(`${API_REQUESTS}/${requestId}`, {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json',
                    ...buildAuthHeaders()
                },
                credentials: 'include',
                body: JSON.stringify({ status })
            });
            if (!res.ok) throw new Error('Cập nhật thất bại');
            await loadRequests();
        } catch (err) {
            console.error(err);
            alert('Không thể cập nhật yêu cầu. Vui lòng thử lại.');
        }
    };

    if (requestList) {
        requestList.addEventListener('click', (event) => {
            const button = event.target.closest('[data-request-action]');
            if (!button) return;
            const requestId = button.getAttribute('data-request-id');
            const action = button.getAttribute('data-request-action');
            updateRequestStatus(requestId, action);
        });
    }

    if (searchInput) searchInput.addEventListener('input', applyPostFilters);
    if (statusSelect) statusSelect.addEventListener('change', applyPostFilters);
    if (sortSelect) sortSelect.addEventListener('change', applyPostFilters);

    loadPosts();
    loadRequests();
});

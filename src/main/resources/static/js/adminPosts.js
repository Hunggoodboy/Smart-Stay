(() => {
    'use strict';

    const POST_STATUSES = ['DRAFT', 'ACTIVE', 'INACTIVE', 'RENTED', 'DELETED'];
    const ROOM_STATUSES = ['AVAILABLE', 'RENTED', 'MAINTENANCE'];

    const state = {
        activeView: 'review',
        postMode: 'review',
        posts: [],
        rooms: []
    };

    const $ = id => document.getElementById(id);

    const els = {
        postKeyword: $('post-keyword'),
        postStatus: $('post-status'),
        postSearchBtn: $('post-search-btn'),
        postTableBody: $('post-table-body'),
        postCountBadge: $('post-count-badge'),
        postSection: $('admin-post-list'),
        postListTitle: $('post-list-title'),
        postListSubtitle: $('post-list-subtitle'),
        postTableHeadRow: $('post-table-head-row'),

        roomKeyword: $('room-keyword'),
        roomStatus: $('room-status'),
        roomSearchBtn: $('room-search-btn'),
        roomTableBody: $('room-table-body'),
        roomCountBadge: $('room-count-badge'),
        roomSection: $('admin-room-list'),

        editModal: $('edit-post-modal'),
        editForm: $('edit-post-form'),
        editPostId: $('edit-post-id'),

        featuredModal: $('featured-post-modal'),
        featuredForm: $('featured-post-form'),
        featuredPostId: $('featured-post-id')
    };

    function authHeaders() {
        const token = localStorage.getItem('smartstay_token');
        const headers = { 'Content-Type': 'application/json' };
        if (token) headers.Authorization = `Bearer ${token}`;
        return headers;
    }

    async function api(path, options = {}) {
        const res = await fetch(path, {
            ...options,
            headers: {
                ...authHeaders(),
                ...(options.headers || {})
            }
        });

        const contentType = res.headers.get('content-type') || '';
        const text = await res.text();
        const isJson = contentType.includes('application/json');
        const data = isJson && text ? JSON.parse(text) : null;

        if (!res.ok) {
            const message = data?.message || data?.error || getHttpErrorMessage(res, text);
            throw new Error(message);
        }

        if (!isJson && text.trim().startsWith('<')) {
            throw new Error('Server tra ve HTML thay vi JSON. Vui long kiem tra dang nhap admin hoac quyen truy cap API.');
        }

        return data;
    }

    function getHttpErrorMessage(res, text) {
        if (res.status === 401 || res.status === 403) {
            return 'Ban chua dang nhap admin hoac token khong co quyen ADMIN.';
        }
        if (res.status === 404) {
            return 'Khong tim thay API. Kiem tra lai duong dan endpoint.';
        }
        if (text && text.trim().startsWith('<')) {
            return `Server tra ve trang HTML loi thay vi JSON (${res.status}).`;
        }
        return text || `Request failed: ${res.status}`;
    }

    function esc(value) {
        return value == null ? '' : String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function money(value) {
        if (value == null || value === '') return '-';
        return Number(value).toLocaleString('vi-VN') + ' VND';
    }

    function dateTime(value) {
        if (!value) return '-';
        return new Date(value).toLocaleString('vi-VN');
    }

    function postStatusLabel(status) {
        const labels = {
            DRAFT: 'CHỜ DUYỆT',
            ACTIVE: 'ĐÃ DUYỆT',
            INACTIVE: 'TỪ CHỐI',
            RENTED: 'ĐÃ THUÊ',
            DELETED: 'ĐÃ XÓA'
        };
        return labels[status] || status || '-';
    }

    function roomStatusLabel(status) {
        const labels = {
            AVAILABLE: 'CÒN TRỐNG',
            RENTED: 'ĐÃ THUÊ',
            MAINTENANCE: 'BẢO TRÌ'
        };
        return labels[status] || status || '-';
    }

    function postMedia(post) {
        const title = esc(post.title || 'Bai dang');
        const imageUrl = post.mainImageUrl;
        const image = imageUrl
            ? `<img src="${esc(imageUrl)}" alt="${title}" loading="lazy" onerror="this.parentElement.innerHTML='<div class=&quot;post-thumb-placeholder&quot;>NO IMG</div>'">`
            : '<div class="post-thumb-placeholder">NO IMG</div>';

        return `
            <div class="post-cell">
                <div class="post-thumb">${image}</div>
                <div class="post-info">
                    <div class="admin-row-title">${title}</div>
                    <div class="admin-row-sub">${esc([post.address, post.district, post.city].filter(Boolean).join(', '))}</div>
                </div>
            </div>
        `;
    }

    function toDateTimeLocal(value) {
        if (!value) return '';
        const d = new Date(value);
        if (Number.isNaN(d.getTime())) return '';
        const pad = n => String(n).padStart(2, '0');
        return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
    }

    function emptyToNull(value) {
        const trimmed = value == null ? '' : String(value).trim();
        return trimmed === '' ? null : trimmed;
    }

    function numberOrNull(value) {
        const normalized = emptyToNull(value);
        return normalized == null ? null : Number(normalized);
    }

    function buildQuery(params) {
        const query = new URLSearchParams();
        Object.entries(params).forEach(([key, value]) => {
            if (value != null && String(value).trim() !== '') query.set(key, value);
        });
        const str = query.toString();
        return str ? `?${str}` : '';
    }

    async function loadPosts() {
        setPostState('Dang tai bai dang...');
        try {
            const query = buildQuery({
                status: els.postStatus.value,
                keyword: els.postKeyword.value
            });
            state.posts = await api(`/api/admin/room-posts${query}`);
            renderPosts();
        } catch (err) {
            setPostState(err.message);
        }
    }

    async function loadRooms() {
        setRoomState('Đang tải phòng...');
        try {
            const query = buildQuery({
                status: els.roomStatus.value,
                keyword: els.roomKeyword.value
            });
            state.rooms = await api(`/api/admin/rooms${query}`);
            renderRooms();
        } catch (err) {
            setRoomState(err.message);
        }
    }

    function setPostState(message) {
        renderPostHeader();
        const colspan = state.postMode === 'featured' ? 5 : 5;
        els.postTableBody.innerHTML = `<tr><td colspan="${colspan}" class="admin-state">${esc(message)}</td></tr>`;
        els.postCountBadge.textContent = `${state.posts.length || 0} bai`;
    }

    function setRoomState(message) {
        els.roomTableBody.innerHTML = `<tr><td colspan="6" class="admin-state">${esc(message)}</td></tr>`;
        els.roomCountBadge.textContent = `${state.rooms.length || 0} phòng`;
    }

    function renderPosts() {
        renderPostHeader();
        els.postCountBadge.textContent = `${state.posts.length} bai`;
        if (!state.posts.length) {
            setPostState('Khong co bai dang phu hop.');
            return;
        }

        els.postTableBody.innerHTML = state.posts.map(post => {
            const featured = Boolean(post.featured);
            if (state.postMode === 'featured') {
                return `
                <tr data-post-id="${post.id}">
                    <td>${postMedia(post)}</td>
                    <td>
                        <div class="price-value">${money(post.monthlyRent)}</div>
                        <div class="type-pill">${esc(post.roomType || '-')}</div>
                    </td>
                    <td>
                        <div>${esc(post.landlordName || '-')}</div>
                        <div class="admin-row-sub">${esc(post.landlordEmail || '')}</div>
                    </td>
                    <td>
                        <span class="status-pill ${featured ? 'featured-pill' : ''}">${featured ? 'NOI BAT' : 'THUONG'}</span>
                        <div class="admin-row-sub">Priority: ${post.featuredPriority ?? 0}</div>
                        <div class="admin-row-sub">Het han: ${dateTime(post.featuredUntil)}</div>
                    </td>
                    <td>
                        <div class="admin-actions">
                            <button class="admin-btn secondary" type="button" data-action="feature-post">Noi bat</button>
                        </div>
                    </td>
                </tr>
            `;
            }

            return `
                <tr data-post-id="${post.id}">
                    <td>${postMedia(post)}</td>
                    <td>
                        <div class="price-value">${money(post.monthlyRent)}</div>
                        <div class="type-pill">${esc(post.roomType || '-')}</div>
                    </td>
                    <td>
                        <div>${esc(post.landlordName || '-')}</div>
                        <div class="admin-row-sub">${esc(post.landlordEmail || '')}</div>
                    </td>
                    <td>
                        <span class="status-pill">${esc(postStatusLabel(post.status))}</span>
                        <div class="admin-row-sub">${dateTime(post.createdAt)}</div>
                    </td>
                    <td>
                        <div class="admin-actions">
                            <select class="admin-select js-post-status" style="max-width:150px">
                                ${POST_STATUSES.map(s => `<option value="${s}" ${s === post.status ? 'selected' : ''}>${postStatusLabel(s)}</option>`).join('')}
                            </select>
                            <button class="admin-btn" type="button" data-action="save-post-status">Lưu</button>
                            <button class="admin-btn secondary" type="button" data-action="edit-post">Sửa</button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    function renderPostHeader() {
        if (state.postMode === 'featured') {
            els.postListTitle.textContent = 'Danh sach gan nhan noi bat';
            els.postListSubtitle.textContent = 'Chi hien thao tac gan hoac bo nhan noi bat cho bai dang.';
            els.postTableHeadRow.innerHTML = `
                <th>Bai dang</th>
                <th>Gia / Loai</th>
                <th>Chu nha</th>
                <th>Noi bat</th>
                <th>Thao tac</th>
            `;
            return;
        }

        els.postListTitle.textContent = 'Danh sach kiem duyet bai dang';
        els.postListSubtitle.textContent = 'Chi hien thao tac sua noi dung bai dang can kiem duyet.';
        els.postTableHeadRow.innerHTML = `
            <th>Bai dang</th>
            <th>Gia / Loai</th>
            <th>Chu nha</th>
            <th>Trang thai</th>
            <th>Thao tac</th>
        `;
    }

    function renderRooms() {
        els.roomCountBadge.textContent = `${state.rooms.length} phòng`;
        if (!state.rooms.length) {
            setRoomState('Không có phòng phù hợp.');
            return;
        }

        els.roomTableBody.innerHTML = state.rooms.map(room => `
            <tr data-room-id="${room.id}">
                <td>
                    <div class="admin-row-title">${esc(room.roomNumber || `Phòng #${room.id}`)}</div>
                    <div class="admin-row-sub">${esc([room.address, room.district, room.city].filter(Boolean).join(', '))}</div>
                </td>
                <td>
                    <div class="price-value">${money(room.rentPrice)}</div>
                    <div class="type-pill">${esc(room.roomType || '-')}</div>
                </td>
                <td>${esc(room.landlordName || '-')}</td>
                <td>
                    <div>${esc(room.tenantName || 'Chưa có')}</div>
                    <div class="admin-row-sub">${esc(room.tenantEmail || '')}</div>
                </td>
                <td>
                    <span class="status-pill">${esc(roomStatusLabel(room.status))}</span>
                    <div class="admin-row-sub">${dateTime(room.updatedAt || room.createdAt)}</div>
                </td>
                <td>
                    <div class="admin-actions">
                        <select class="admin-select js-room-status" style="max-width:165px">
                            ${ROOM_STATUSES.map(s => `<option value="${s}" ${s === room.status ? 'selected' : ''}>${roomStatusLabel(s)}</option>`).join('')}
                        </select>
                        <button class="admin-btn" type="button" data-action="save-room-status">Lưu</button>
                    </div>
                </td>
            </tr>
        `).join('');
    }

    function getPost(id) {
        return state.posts.find(post => String(post.id) === String(id));
    }

    function openModal(id) {
        $(id).classList.add('open');
    }

    function closeModal(id) {
        $(id).classList.remove('open');
    }

    function openEditPost(id) {
        const post = getPost(id);
        if (!post) return;

        setValue('edit-post-id', post.id);
        setValue('edit-title', post.title);
        setValue('edit-description', post.description);
        setValue('edit-monthly-rent', post.monthlyRent);
        setValue('edit-deposit-amount', post.depositAmount);
        setValue('edit-area-m2', post.areaM2);
        setValue('edit-max-occupants', post.maxOccupants);
        setValue('edit-room-type', post.roomType);
        setValue('edit-address', post.address);
        setValue('edit-ward', post.ward);
        setValue('edit-district', post.district);
        setValue('edit-city', post.city);
        openModal('edit-post-modal');
    }

    function openFeaturedPost(id) {
        const post = getPost(id);
        if (!post) return;

        setValue('featured-post-id', post.id);
        setValue('featured-enabled', 'true');
        setValue('featured-priority', post.featuredPriority ?? 0);
        setValue('featured-until', toDateTimeLocal(post.featuredUntil));
        openModal('featured-post-modal');
    }

    function setValue(id, value) {
        const el = $(id);
        if (el) el.value = value ?? '';
    }

    async function submitEditPost(event) {
        event.preventDefault();
        const id = els.editPostId.value;

        const payload = {
            title: emptyToNull($('edit-title').value),
            description: emptyToNull($('edit-description').value),
            monthlyRent: numberOrNull($('edit-monthly-rent').value),
            depositAmount: numberOrNull($('edit-deposit-amount').value),
            areaM2: numberOrNull($('edit-area-m2').value),
            maxOccupants: numberOrNull($('edit-max-occupants').value),
            roomType: emptyToNull($('edit-room-type').value),
            address: emptyToNull($('edit-address').value),
            ward: emptyToNull($('edit-ward').value),
            district: emptyToNull($('edit-district').value),
            city: emptyToNull($('edit-city').value)
        };

        try {
            await api(`/api/admin/room-posts/${id}`, {
                method: 'PATCH',
                body: JSON.stringify(payload)
            });
            closeModal('edit-post-modal');
            await loadPosts();
        } catch (err) {
            alert(err.message);
        }
    }

    async function submitFeaturedPost(event) {
        event.preventDefault();
        const id = els.featuredPostId.value;
        const enabled = $('featured-enabled').value === 'true';

        const payload = {
            featured: enabled,
            featuredPriority: enabled ? Number($('featured-priority').value || 0) : 0,
            featuredUntil: enabled ? emptyToNull($('featured-until').value) : null
        };

        try {
            await api(`/api/admin/room-posts/${id}/featured`, {
                method: 'PATCH',
                body: JSON.stringify(payload)
            });
            closeModal('featured-post-modal');
            await loadPosts();
        } catch (err) {
            alert(err.message);
        }
    }

    async function updatePostStatus(row) {
        const id = row.dataset.postId;
        const status = row.querySelector('.js-post-status').value;

        try {
            await api(`/api/admin/room-posts/${id}/status`, {
                method: 'PATCH',
                body: JSON.stringify({ status })
            });
            await loadPosts();
        } catch (err) {
            alert(err.message);
        }
    }

    async function updateRoomStatus(row) {
        const id = row.dataset.roomId;
        const status = row.querySelector('.js-room-status').value;

        try {
            await api(`/api/admin/rooms/${id}/status`, {
                method: 'PATCH',
                body: JSON.stringify({ status })
            });
            await loadRooms();
        } catch (err) {
            alert(err.message);
        }
    }

    function switchView(view) {
        state.activeView = view;

        if (view === 'rooms') {
            els.postSection.classList.add('admin-section-hidden');
            els.roomSection.classList.remove('admin-section-hidden');
            if (!state.rooms.length) loadRooms();
            return;
        }

        state.postMode = view === 'featured' ? 'featured' : 'review';
        els.roomSection.classList.add('admin-section-hidden');
        els.postSection.classList.remove('admin-section-hidden');
        renderPosts();
        if (!state.posts.length) loadPosts();
    }

    function scrollToViewTarget(link) {
        const href = link.getAttribute('href');
        if (!href || !href.startsWith('#')) return;

        const target = document.querySelector(href);
        if (!target) return;

        window.history.replaceState(null, '', href);
        requestAnimationFrame(() => {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        });
    }

    function handlePostTableClick(event) {
        const button = event.target.closest('button[data-action]');
        if (!button) return;

        const row = button.closest('tr[data-post-id]');
        if (!row) return;

        if (button.dataset.action === 'edit-post') openEditPost(row.dataset.postId);
        if (button.dataset.action === 'feature-post') openFeaturedPost(row.dataset.postId);
        if (button.dataset.action === 'save-post-status') updatePostStatus(row);
    }

    function handleRoomTableClick(event) {
        const button = event.target.closest('button[data-action]');
        if (!button) return;

        const row = button.closest('tr[data-room-id]');
        if (!row) return;

        if (button.dataset.action === 'save-room-status') updateRoomStatus(row);
    }

    function bindEvents() {
        document.querySelectorAll('[data-admin-view]').forEach(link => {
            link.addEventListener('click', event => {
                event.preventDefault();
                switchView(link.dataset.adminView);
                scrollToViewTarget(link);
            });
        });
        els.postSearchBtn.addEventListener('click', loadPosts);
        els.roomSearchBtn.addEventListener('click', loadRooms);
        els.postKeyword.addEventListener('keydown', event => {
            if (event.key === 'Enter') loadPosts();
        });
        els.roomKeyword.addEventListener('keydown', event => {
            if (event.key === 'Enter') loadRooms();
        });
        els.postStatus.addEventListener('change', loadPosts);
        els.roomStatus.addEventListener('change', loadRooms);
        els.postTableBody.addEventListener('click', handlePostTableClick);
        els.roomTableBody.addEventListener('click', handleRoomTableClick);
        els.editForm.addEventListener('submit', submitEditPost);
        els.featuredForm.addEventListener('submit', submitFeaturedPost);

        document.querySelectorAll('[data-close-modal]').forEach(button => {
            button.addEventListener('click', () => closeModal(button.dataset.closeModal));
        });

        document.querySelectorAll('.admin-modal').forEach(modal => {
            modal.addEventListener('click', event => {
                if (event.target === modal) closeModal(modal.id);
            });
        });
    }

    bindEvents();
    switchView('review');
    loadPosts();
})();

(() => {
    'use strict';

    const $ = id => document.getElementById(id);

    const els = {
        keyword: $('admin-user-keyword'),
        role: $('admin-user-role'),
        active: $('admin-user-active'),
        includeDeleted: $('admin-user-include-deleted'),
        searchBtn: $('admin-user-search-btn'),
        tableBody: $('admin-users-table-body'),
        countBadge: $('admin-users-count-badge'),
        totalUsers: $('admin-users-total'),
        pendingLandlords: $('admin-users-pending'),
        lockedUsers: $('admin-users-locked'),
        listTitle: $('admin-users-list-title'),
        listSubtitle: $('admin-users-list-subtitle')
    };

    const state = {
        mode: 'manage',
        users: []
    };

    function hasUserManagement() {
        return Boolean(els.tableBody);
    }

    function authHeaders(json = false) {
        const token = localStorage.getItem('smartstay_token');
        const headers = {};
        if (json) headers['Content-Type'] = 'application/json';
        if (token) headers.Authorization = `Bearer ${token}`;
        return headers;
    }

    async function api(path, options = {}) {
        const res = await fetch(path, {
            ...options,
            headers: {
                ...authHeaders(Boolean(options.body)),
                ...(options.headers || {})
            }
        });

        if (res.status === 401 || res.status === 403) {
            window.location.href = '/login';
            return null;
        }

        const contentType = res.headers.get('content-type') || '';
        const text = await res.text();
        const data = contentType.includes('application/json') && text ? JSON.parse(text) : null;

        if (!res.ok) {
            throw new Error(data?.message || text || `Request failed: ${res.status}`);
        }

        return data;
    }

    function esc(value) {
        return value == null ? '' : String(value)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
    }

    function dateTime(value) {
        if (!value) return '-';
        return new Date(value).toLocaleString('vi-VN');
    }

    function includeDeletedQuery() {
        const params = new URLSearchParams();
        if (state.mode === 'restore' || els.includeDeleted?.checked) params.set('includeDeleted', 'true');
        const query = params.toString();
        return query ? `?${query}` : '';
    }

    function buildUserApiPath() {
        const keyword = els.keyword?.value.trim();
        const role = els.role?.value;
        const active = els.active?.value;
        const deletedQuery = includeDeletedQuery();

        if (keyword) {
            const separator = deletedQuery ? '&' : '?';
            return `/api/admin/users/keyword${deletedQuery}${separator}keyword=${encodeURIComponent(keyword)}`;
        }

        if (role) {
            return `/api/admin/users/role/${encodeURIComponent(role)}${deletedQuery}`;
        }

        if (active) {
            const separator = deletedQuery ? '&' : '?';
            return `/api/admin/users/status${deletedQuery}${separator}active=${encodeURIComponent(active)}`;
        }

        return `/api/admin/users/all${deletedQuery}`;
    }

    function applyLocalFilters(users) {
        const keyword = els.keyword?.value.trim().toLowerCase();
        const role = els.role?.value;
        const active = els.active?.value;

        return users.filter(user => {
            const deleted = Boolean(user.deletedAt);
            if (state.mode === 'restore' && !deleted) return false;
            if (state.mode === 'role' && deleted) return false;

            const matchesKeyword = !keyword
                || (user.fullName || '').toLowerCase().includes(keyword)
                || (user.email || '').toLowerCase().includes(keyword)
                || (user.username || '').toLowerCase().includes(keyword)
                || (user.phoneNumber || '').toLowerCase().includes(keyword);
            const matchesRole = !role || user.role === role;
            const matchesActive = !active || String(Boolean(user.active)) === active;

            return matchesKeyword && matchesRole && matchesActive;
        });
    }

    function renderModeHeader() {
        if (!els.listTitle || !els.listSubtitle) return;

        if (state.mode === 'restore') {
            els.listTitle.textContent = 'Khôi phục tài khoản bị xóa';
            els.listSubtitle.textContent = 'Chỉ hiển thị tài khoản đã xóa mềm để admin khôi phục.';
            return;
        }

        if (state.mode === 'role') {
            els.listTitle.textContent = 'Cập nhật vai trò tài khoản';
            els.listSubtitle.textContent = 'Chọn vai trò mới trong danh sách rồi bấm Lưu vai trò.';
            return;
        }

        els.listTitle.textContent = 'Quản lý tài khoản';
        els.listSubtitle.textContent = 'Quản lý tài khoản và xử lý yêu cầu xác thực chủ nhà.';
    }

    function setTableState(message) {
        if (!els.tableBody) return;
        els.tableBody.innerHTML = `<tr><td colspan="6" class="admin-state">${esc(message)}</td></tr>`;
    }

    async function loadStats() {
        try {
            const [total, pending, locked] = await Promise.all([
                api('/api/admin/dashboard/users/total'),
                api('/api/admin/dashboard/landlords/pending-verifications/total'),
                api('/api/admin/users/locked/total')
            ]);

            if (els.totalUsers) els.totalUsers.textContent = total?.value ?? 0;
            if (els.pendingLandlords) els.pendingLandlords.textContent = pending?.value ?? 0;
            if (els.lockedUsers) els.lockedUsers.textContent = locked?.value ?? 0;
        } catch (error) {
            if (els.totalUsers) els.totalUsers.textContent = '!';
            if (els.pendingLandlords) els.pendingLandlords.textContent = '!';
            if (els.lockedUsers) els.lockedUsers.textContent = '!';
            console.error(error);
        }
    }

    async function loadUsers() {
        renderModeHeader();
        setTableState('Dang tai danh sach tai khoan...');

        try {
            const users = await api(buildUserApiPath()) || [];
            state.users = applyLocalFilters(users);
            renderUsers();
        } catch (error) {
            setTableState(error.message);
        }
    }

    function renderUsers() {
        renderModeHeader();
        if (els.countBadge) {
            els.countBadge.textContent = `${state.users.length} tai khoan`;
        }

        if (!state.users.length) {
            setTableState('Khong co tai khoan phu hop.');
            return;
        }

        els.tableBody.innerHTML = state.users.map(user => {
            const deleted = Boolean(user.deletedAt);
            const active = Boolean(user.active);
            const statusClass = deleted ? 'status-deleted' : (active ? 'status-active' : 'status-inactive');
            const statusLabel = deleted ? 'DA XOA' : (active ? 'HOAT DONG' : 'DA KHOA');
            const avatar = user.avatarUrl
                ? `<img src="${esc(user.avatarUrl)}" class="avatar-circle" alt="${esc(user.fullName || user.username)}">`
                : `<img src="https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName || user.username || 'User')}&background=e0f2fe&color=0284c7" class="avatar-circle" alt="">`;

            return `
                <tr data-user-id="${user.id}">
                    <td>
                        <div style="display:flex;align-items:center;gap:0.6rem;">
                            ${avatar}
                            <div>
                                <div class="admin-table-title">${esc(user.fullName || '-')}</div>
                                <div class="admin-table-sub">@${esc(user.username || '-')}</div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <div class="admin-table-title">${esc(user.email || '-')}</div>
                        <div class="admin-table-sub">${esc(user.phoneNumber || '-')}</div>
                    </td>
                    <td>
                        <span class="admin-mini-tag">${esc(user.role || '-')}</span>
                    </td>
                    <td>
                        <span class="status-pill ${statusClass}">${statusLabel}</span>
                        ${deleted ? `<div class="admin-table-sub">${dateTime(user.deletedAt)}</div>` : ''}
                    </td>
                    <td>${dateTime(user.createdAt)}</td>
                    <td>
                        <div class="admin-actions">
                            ${renderUserActions(user, deleted, active)}
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    function renderUserActions(user, deleted, active) {
        if (state.mode === 'restore') {
            return '<button type="button" class="btn-outline" data-action="restore">Khoi phuc</button>';
        }

        if (state.mode === 'role') {
            return `
                <select class="admin-select js-user-role" style="max-width:145px">
                    ${['ADMIN', 'LANDLORD', 'CUSTOMER'].map(role => `<option value="${role}" ${role === user.role ? 'selected' : ''}>${role}</option>`).join('')}
                </select>
                <button type="button" class="btn-outline" data-action="save-role">Luu vai tro</button>
            `;
        }

        if (deleted) {
            return '<button type="button" class="btn-outline" data-action="restore">Khoi phuc</button>';
        }

        return `
            <button type="button" class="btn-outline" data-action="${active ? 'lock' : 'unlock'}">${active ? 'Khoa' : 'Mo khoa'}</button>
            <button type="button" class="btn-danger" data-action="delete">Xoa</button>
        `;
    }

    async function updateStatus(row, active) {
        const id = row.dataset.userId;
        await api(`/api/admin/users/${id}/status`, {
            method: 'PATCH',
            body: JSON.stringify({ active })
        });
    }

    async function deleteUser(row) {
        const id = row.dataset.userId;
        await api(`/api/admin/users/${id}`, { method: 'DELETE' });
    }

    async function restoreUser(row) {
        const id = row.dataset.userId;
        await api(`/api/admin/users/${id}/restore`, { method: 'PATCH' });
    }

    async function updateRole(row) {
        const id = row.dataset.userId;
        const role = row.querySelector('.js-user-role')?.value;
        if (!role) throw new Error('Vai tro tai khoan khong hop le');

        await api(`/api/admin/users/${id}/role`, {
            method: 'PATCH',
            body: JSON.stringify({ role })
        });
    }

    async function handleTableClick(event) {
        const button = event.target.closest('button[data-action]');
        if (!button) return;

        const row = button.closest('tr[data-user-id]');
        if (!row) return;

        const action = button.dataset.action;

        try {
            if (action === 'lock') {
                if (!confirm('Xac nhan khoa tai khoan nay?')) return;
                await updateStatus(row, false);
            }
            if (action === 'unlock') {
                await updateStatus(row, true);
            }
            if (action === 'delete') {
                if (!confirm('Xac nhan xoa tai khoan nay?')) return;
                await deleteUser(row);
            }
            if (action === 'restore') {
                await restoreUser(row);
            }
            if (action === 'save-role') {
                await updateRole(row);
            }

            await Promise.all([loadUsers(), loadStats()]);
        } catch (error) {
            alert(error.message);
        }
    }

    function switchUserMode(mode, targetSelector) {
        state.mode = mode || 'manage';

        if (state.mode === 'restore' && els.includeDeleted) {
            els.includeDeleted.checked = true;
        }

        loadUsers();

        const target = targetSelector ? document.querySelector(targetSelector) : null;
        if (target) {
            requestAnimationFrame(() => target.scrollIntoView({ behavior: 'smooth', block: 'start' }));
        }
    }

    function bindEvents() {
        els.searchBtn?.addEventListener('click', loadUsers);
        els.keyword?.addEventListener('keydown', event => {
            if (event.key === 'Enter') loadUsers();
        });
        els.role?.addEventListener('change', loadUsers);
        els.active?.addEventListener('change', loadUsers);
        els.includeDeleted?.addEventListener('change', loadUsers);
        els.tableBody?.addEventListener('click', handleTableClick);
        document.querySelectorAll('[data-user-mode]').forEach(link => {
            link.addEventListener('click', event => {
                event.preventDefault();
                switchUserMode(link.dataset.userMode, link.getAttribute('href'));
            });
        });
    }

    function scrollToCurrentHash() {
        if (!window.location.hash) return;

        const target = document.querySelector(window.location.hash);
        if (!target) return;

        setTimeout(() => {
            target.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 150);
    }

    document.addEventListener('DOMContentLoaded', () => {
        if (!hasUserManagement()) return;
        bindEvents();
        loadStats();
        loadUsers();
        scrollToCurrentHash();
    });
})();

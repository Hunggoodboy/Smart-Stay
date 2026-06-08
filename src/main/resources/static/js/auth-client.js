/**
 * SmartStay Auth Client
 * - Wrapper fetch() tự động refresh accessToken khi 401
 * - Logout: gọi POST /api/user/logout → clear localStorage → redirect /login
 * - Refresh: gọi POST /api/user/refresh-token (browser tự gửi refresh_token cookie)
 *
 * Dùng thay cho fetch() ở toàn bộ frontend:
 *   const res = await authFetch('/api/...', { method: 'GET' });
 */

(function (global) {
    'use strict';

    // ────────────────────────────────────────────────────────────
    // Internal helpers
    // ────────────────────────────────────────────────────────────
    function getAccessToken() {
        return localStorage.getItem('smartstay_token');
    }

    function saveAccessToken(token) {
        localStorage.setItem('smartstay_token', token);
    }

    function clearAuth() {
        localStorage.removeItem('smartstay_token');
        localStorage.removeItem('smartstay_user');
    }

    function redirectToLogin() {
        window.location.href = '/login';
    }

    // Biến cờ để tránh nhiều yêu cầu refresh cùng lúc
    let _isRefreshing = false;
    let _refreshQueue = []; // [{resolve, reject}]

    function processQueue(error, newToken) {
        _refreshQueue.forEach(({ resolve, reject }) => {
            if (error) reject(error);
            else resolve(newToken);
        });
        _refreshQueue = [];
    }

    // ────────────────────────────────────────────────────────────
    // Refresh access token bằng refresh_token cookie
    // ────────────────────────────────────────────────────────────
    async function tryRefreshToken() {
        const res = await fetch('/api/user/refresh-token', {
            method: 'POST',
            credentials: 'include', // gửi cookie refresh_token
        });

        if (!res.ok) {
            throw new Error('REFRESH_FAILED');
        }

        const data = await res.json();
        if (!data.success || !data.accessToken) {
            throw new Error('REFRESH_FAILED');
        }

        saveAccessToken(data.accessToken);
        return data.accessToken;
    }

    // ────────────────────────────────────────────────────────────
    // authFetch – wrapper chính, tự retry sau khi refresh
    // ────────────────────────────────────────────────────────────
    async function authFetch(url, options = {}) {
        const token = getAccessToken();

        // Gắn Authorization header
        const headers = {
            'Content-Type': 'application/json',
            ...(options.headers || {}),
        };
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        let res = await fetch(url, {
            ...options,
            headers,
            credentials: 'include',
        });

        // Nếu không phải 401 → trả về bình thường
        if (res.status !== 401) {
            return res;
        }

        // ── 401: thử refresh ───────────────────────────────────
        if (_isRefreshing) {
            // Đang refresh rồi: xếp hàng chờ
            return new Promise((resolve, reject) => {
                _refreshQueue.push({
                    resolve: async (newToken) => {
                        const retryHeaders = { ...headers, Authorization: `Bearer ${newToken}` };
                        resolve(await fetch(url, { ...options, headers: retryHeaders, credentials: 'include' }));
                    },
                    reject,
                });
            });
        }

        _isRefreshing = true;

        try {
            const newToken = await tryRefreshToken();
            processQueue(null, newToken);

            // Retry yêu cầu gốc với token mới
            const retryHeaders = { ...headers, Authorization: `Bearer ${newToken}` };
            return await fetch(url, { ...options, headers: retryHeaders, credentials: 'include' });

        } catch (err) {
            processQueue(err, null);
            // Refresh thất bại → logout hẳn
            clearAuth();
            redirectToLogin();
            // Trả về response 401 gốc để caller handle nếu cần
            return res;
        } finally {
            _isRefreshing = false;
        }
    }

    // ────────────────────────────────────────────────────────────
    // logout – gọi API backend, clear state, redirect
    // ────────────────────────────────────────────────────────────
    async function logout() {
        try {
            const token = getAccessToken();
            await fetch('/api/user/logout', {
                method: 'POST',
                credentials: 'include', // gửi cookie để backend xóa refresh token
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
        } catch (_) {
            // Ignore network error — vẫn clear local state
        } finally {
            clearAuth();
            redirectToLogin();
        }
    }

    // ────────────────────────────────────────────────────────────
    // Expose ra global
    // ────────────────────────────────────────────────────────────
    global.authFetch = authFetch;
    global.authLogout = logout;

})(window);

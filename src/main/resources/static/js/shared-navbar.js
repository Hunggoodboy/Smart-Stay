(function () {
    // ── 1. GHI ĐÈ FETCH ĐỂ XỬ LÝ AUTH & REFRESH TOKEN TỰ ĐỘNG ──
    const originalFetch = window.fetch;
    let isRefreshing = false;
    let refreshQueue = [];

    function processQueue(error, token) {
        refreshQueue.forEach(prom => {
            if (error) prom.reject(error);
            else prom.resolve(token);
        });
        refreshQueue = [];
    }

    window.fetch = async function () {
        let args = arguments;
        let url = args[0];
        let options = args[1] || {};

        // Chỉ gắn token và credentials cho request API nội bộ
        if (typeof url === 'string' && url.startsWith('/api/')) {
            const token = localStorage.getItem('smartstay_token');
            if (token) {
                options.headers = {
                    ...options.headers,
                    'Authorization': `Bearer ${token}`
                };
            }
            options.credentials = 'include';
            args[1] = options;
        }

        let response = await originalFetch.apply(this, args);

        // Xử lý khi token hết hạn (401)
        if (response.status === 401 && typeof url === 'string' && url.startsWith('/api/') 
            && !url.includes('/refresh-token') && !url.includes('/login') && !url.includes('/logout')) {
            
            if (isRefreshing) {
                return new Promise(function (resolve, reject) {
                    refreshQueue.push({ resolve, reject });
                }).then(newToken => {
                    options.headers['Authorization'] = `Bearer ${newToken}`;
                    return originalFetch(url, options);
                }).catch(err => {
                    return response;
                });
            }

            isRefreshing = true;

            try {
                const refreshRes = await originalFetch('/api/user/refresh-token', {
                    method: 'POST',
                    credentials: 'include'
                });

                if (!refreshRes.ok) throw new Error('Refresh failed');
                const data = await refreshRes.json();
                if (!data.success || !data.accessToken) throw new Error('Refresh token invalid');

                localStorage.setItem('smartstay_token', data.accessToken);
                options.headers['Authorization'] = `Bearer ${data.accessToken}`;
                
                processQueue(null, data.accessToken);
                
                // Gọi lại API ban đầu với token mới
                response = await originalFetch(url, options);
            } catch (err) {
                processQueue(err, null);
                // Hết hạn cả refresh token -> Logout
                window._doNavLogout(true);
            } finally {
                isRefreshing = false;
            }
        }

        return response;
    };

    // Toggle dropdown khi click trigger
    document.querySelectorAll('.nav-dropdown-trigger').forEach(function (trigger) {
        trigger.addEventListener('click', function (e) {
            e.stopPropagation();
            var dropdown = trigger.closest('.nav-dropdown');
            var isOpen = dropdown.classList.contains('open');
            document.querySelectorAll('.nav-dropdown.open').forEach(function (d) {
                d.classList.remove('open');
                d.querySelector('.nav-dropdown-trigger').setAttribute('aria-expanded', 'false');
            });
            if (!isOpen) {
                dropdown.classList.add('open');
                trigger.setAttribute('aria-expanded', 'true');
            }
        });
    });

    document.addEventListener('click', function () {
        document.querySelectorAll('.nav-dropdown.open').forEach(function (d) {
            d.classList.remove('open');
            d.querySelector('.nav-dropdown-trigger').setAttribute('aria-expanded', 'false');
        });
    });

    const hamburger = document.getElementById('hamburger');
    const navInner = document.getElementById('navInner');
    if (hamburger && navInner) {
        hamburger.addEventListener('click', function () {
            navInner.classList.toggle('open');
        });
    }

    function parseJwt(token) {
        try {
            const base64 = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
            return JSON.parse(atob(base64));
        } catch { return null; }
    }

    const token = localStorage.getItem('smartstay_token');
    const payload = token ? parseJwt(token) : null;
    
    // Lưu ý: không dùng exp trong payload để logout cứng nữa.
    // JWT có thể hết hạn, nhưng ta dựa vào refresh_token gọi API 401. 
    // Nên nếu có token, ta vẫn coi là "có vẻ" đang login, kệ 401 sau.
    if (!token) return;

    // Ẩn Login / Register
    const btnLogin = document.getElementById('btnLogin');
    const btnRegister = document.getElementById('btnRegister');
    if (btnLogin) btnLogin.style.display = 'none';
    if (btnRegister) btnRegister.style.display = 'none';

    // Roles
    const rawRoles = payload ? [].concat(payload.roles || payload.authorities || payload.role || []) : [];
    const roles = rawRoles.map(r => (typeof r === 'string' ? r : (r.authority || '')).toUpperCase());

    const isAdmin = roles.some(r => r.includes('ADMIN'));
    const isLandlord = roles.some(r => r.includes('LANDLORD'));
    const isTenant = !isAdmin && !isLandlord;

    if (isAdmin) {
        const btn = document.getElementById('btnAdminVerify');
        if (btn) btn.classList.add('visible');
    }
    if (isLandlord) {
        document.querySelectorAll('.nav-link-landlord').forEach(el => { el.style.display = 'inline-flex'; });
        const reg = document.getElementById('linkRegisterLandlord');
        if (reg) reg.style.display = 'none';
    }
    if (isTenant) {
        document.querySelectorAll('.nav-link-tenant').forEach(el => { el.style.display = 'inline-flex'; });
    }

    const roleLabel = isAdmin ? 'Quản trị viên' : (isLandlord ? 'Chủ nhà' : 'Khách thuê');

    // Render user chip mới (avatar + tên + dropdown logout)
    function renderUserChip(displayName, avatarUrl) {
        const navActions = document.querySelector('.nav-actions');
        if (!navActions) return;

        const avatarSrc = avatarUrl
            || `https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=eff6ff&color=1d4ed8`;

        navActions.innerHTML = `
            <div id="navUserChipWrap" style="position:relative;">
                <button id="navUserChip" type="button"
                    style="display:flex;align-items:center;gap:10px;cursor:pointer;padding:5px 12px 5px 5px;border-radius:999px;background:#f8fafc;border:1px solid #e5e7eb;font-family:inherit;transition:background .15s;"
                    onmouseover="this.style.background='#f1f5f9'" onmouseout="this.style.background='#f8fafc'"
                    onclick="window._toggleNavUserDrop()">
                    <img src="${avatarSrc}" alt="Avatar"
                        style="width:32px;height:32px;border-radius:50%;object-fit:cover;border:2px solid #e5e7eb;flex-shrink:0;"
                        onerror="this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(displayName)}&background=eff6ff&color=1d4ed8'">
                    <span style="font-size:14px;font-weight:600;color:#374151;max-width:130px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${displayName}</span>
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </button>
                <div id="navUserDrop"
                    style="display:none;position:absolute;top:calc(100% + 8px);right:0;min-width:200px;background:#fff;border-radius:14px;border:1px solid #e5e7eb;box-shadow:0 16px 40px rgba(0,0,0,0.12);z-index:999;overflow:hidden;padding:6px 0;">
                    <div style="padding:10px 16px 10px;border-bottom:1px solid #f1f5f9;">
                        <p style="margin:0;font-size:14px;font-weight:700;color:#1e293b;">${displayName}</p>
                        <p style="margin:3px 0 0;font-size:12px;color:#64748b;">${roleLabel}</p>
                    </div>
                    <button onclick="window._doNavLogout()"
                        style="width:100%;padding:10px 16px;background:none;border:none;cursor:pointer;display:flex;align-items:center;gap:10px;font-size:13px;font-weight:600;color:#dc2626;text-align:left;font-family:inherit;"
                        onmouseover="this.style.background='#fef2f2'" onmouseout="this.style.background='none'">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                        Đăng xuất
                    </button>
                </div>
            </div>`;
    }

    window._toggleNavUserDrop = function () {
        const d = document.getElementById('navUserDrop');
        if (d) d.style.display = d.style.display === 'none' ? 'block' : 'none';
    };

    document.addEventListener('click', function (e) {
        const wrap = document.getElementById('navUserChipWrap');
        const drop = document.getElementById('navUserDrop');
        if (drop && wrap && !wrap.contains(e.target)) {
            drop.style.display = 'none';
        }
    }, true);

    // Cập nhật hàm Logout: GỌI BACKEND API
    window._doNavLogout = async function (skipApiCall = false) {
        if (!skipApiCall) {
            try {
                // originalFetch để tránh loop nếu có lỗi
                await originalFetch('/api/user/logout', {
                    method: 'POST',
                    credentials: 'include'
                });
            } catch (e) {
                console.error("Logout API failed", e);
            }
        }
        
        localStorage.removeItem('smartstay_token');
        localStorage.removeItem('smartstay_user');
        document.cookie = 'smartstay_token=; Max-Age=0; path=/';
        window.location.href = '/login';
    };

    // Fetch user info từ API (sẽ tự động refresh token nếu hết hạn do interceptor)
    const apiEndpoint = (isLandlord || isAdmin) ? '/api/user/landlord' : '/api/user/tenant';
    fetch(apiEndpoint) 
    .then(r => r.ok ? r.json() : null)
    .then(raw => {
        const fallback = payload ? (payload.sub || payload.username || payload.name || 'User') : 'User';
        if (!raw) { renderUserChip(fallback, null); return; }
        const data = raw.body ? raw.body : raw;
        const displayName = data.fullName || data.displayName || data.username || fallback;
        renderUserChip(displayName, data.avatarUrl || null);
    })
    .catch(() => {
        const fallback = payload ? (payload.sub || payload.username || payload.name || 'User') : 'User';
        renderUserChip(fallback, null);
    });
})();

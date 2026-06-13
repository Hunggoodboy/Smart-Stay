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
                window._doNavLogout(false);
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
    let navAvatarObjectUrl = null;

    function getNavDefaultAvatar(name) {
        return `https://ui-avatars.com/api/?name=${encodeURIComponent(name || 'User')}&background=eff6ff&color=1d4ed8`;
    }

    function setNavAvatarPreview(src, label) {
        const preview = document.getElementById('_nav-avatar-preview');
        const nameBox = document.getElementById('_nav-avatar-name');
        if (preview) {
            preview.src = src || getNavDefaultAvatar(document.getElementById('_nav-fullName')?.value || 'User');
        }
        if (nameBox) {
            nameBox.textContent = label || 'JPG, PNG, WEBP hoặc GIF, tối đa 5MB';
        }
    }

    // Render user chip mới (avatar + tên + dropdown logout)
    function renderUserChip(displayName, avatarUrl) {
        const navActions = document.querySelector('.nav-actions');
        if (!navActions) return;

        const avatarSrc = avatarUrl || getNavDefaultAvatar(displayName);

        navActions.innerHTML = `
            <div id="navUserChipWrap" style="position:relative;">
                <button id="navUserChip" type="button"
                    style="display:flex;align-items:center;gap:10px;cursor:pointer;padding:5px 12px 5px 5px;border-radius:999px;background:#f8fafc;border:1px solid #e5e7eb;font-family:inherit;transition:background .15s;"
                    onmouseover="this.style.background='#f1f5f9'" onmouseout="this.style.background='#f8fafc'"
                    onclick="window._toggleNavUserDrop()">
                    <img src="${avatarSrc}" alt="Avatar"
                        style="width:32px;height:32px;border-radius:50%;object-fit:cover;border:2px solid #e5e7eb;flex-shrink:0;"
                        onerror="this.src='${getNavDefaultAvatar(displayName)}'">
                    <span style="font-size:14px;font-weight:600;color:#374151;max-width:130px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;">${displayName}</span>
                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="#9ca3af" stroke-width="2.5"><polyline points="6 9 12 15 18 9"/></svg>
                </button>
                <div id="navUserDrop"
                    style="display:none;position:absolute;top:calc(100% + 8px);right:0;min-width:220px;background:#fff;border-radius:14px;border:1px solid #e5e7eb;box-shadow:0 16px 40px rgba(0,0,0,0.12);z-index:9999;overflow:hidden;padding:6px 0;">
                    <div style="padding:10px 16px 10px;border-bottom:1px solid #f1f5f9;">
                        <p style="margin:0;font-size:14px;font-weight:700;color:#1e293b;">${displayName}</p>
                        <p style="margin:3px 0 0;font-size:12px;color:#64748b;">${roleLabel}</p>
                    </div>
                    <button onclick="window._openNavProfile();window._toggleNavUserDrop();"
                        style="width:100%;padding:10px 16px;background:none;border:none;cursor:pointer;display:flex;align-items:center;gap:10px;font-size:13px;font-weight:600;color:#374151;text-align:left;font-family:inherit;"
                        onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background='none'">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
                        Thông tin cá nhân
                    </button>
                    <button onclick="window._openNavPassword();window._toggleNavUserDrop();"
                        style="width:100%;padding:10px 16px;background:none;border:none;cursor:pointer;display:flex;align-items:center;gap:10px;font-size:13px;font-weight:600;color:#374151;text-align:left;font-family:inherit;"
                        onmouseover="this.style.background='#f8fafc'" onmouseout="this.style.background='none'">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                        Đổi mật khẩu
                    </button>
                    <div style="height:1px;background:#f1f5f9;margin:4px 0;"></div>
                    <button onclick="window._doNavLogout()"
                        style="width:100%;padding:10px 16px;background:none;border:none;cursor:pointer;display:flex;align-items:center;gap:10px;font-size:13px;font-weight:600;color:#dc2626;text-align:left;font-family:inherit;"
                        onmouseover="this.style.background='#fef2f2'" onmouseout="this.style.background='none'">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                        Đăng xuất
                    </button>
                </div>
            </div>`;

        // Inject modals nếu chưa có
        _injectNavModals(displayName);
    }

    window._toggleNavUserDrop = function () {
        const d = document.getElementById('navUserDrop');
        if (d) d.style.display = d.style.display === 'none' ? 'block' : 'none';
    };

    // ─── Inject modals Profile + Password vào trang nếu chưa có ───
    function _injectNavModals(displayName) {
        if (document.getElementById('_nav-profile-modal')) return; // đã inject rồi

        const modalStyles = `
        <style id="_nav-modal-style">
        ._nav-modal-overlay {
            display:none;position:fixed;inset:0;z-index:10000;
            background:rgba(15,23,42,0.5);backdrop-filter:blur(4px);
            align-items:center;justify-content:center;padding:24px;
        }
        ._nav-modal-overlay.open { display:flex; }
        ._nav-modal-card {
            width:min(720px,100%);max-height:calc(100vh - 48px);
            overflow:auto;border-radius:16px;background:#fff;
            box-shadow:0 24px 80px rgba(15,23,42,0.24);
            animation:_navSlideUp 0.25s ease;
        }
        ._nav-modal-card-sm { width:min(520px,100%); }
        @keyframes _navSlideUp { from{opacity:0;transform:translateY(20px)} to{opacity:1;transform:translateY(0)} }
        ._nav-modal-header {
            display:flex;align-items:center;justify-content:space-between;gap:16px;
            padding:20px 24px;border-bottom:1px solid #e5e7eb;
        }
        ._nav-modal-header h2 { margin:0;color:#1e293b;font-size:20px;font-weight:900; }
        ._nav-modal-header p { margin:4px 0 0;color:#64748b;font-size:14px; }
        ._nav-modal-close {
            width:36px;height:36px;border-radius:50%;border:none;background:#f1f5f9;
            color:#64748b;cursor:pointer;font-size:18px;display:grid;place-items:center;
        }
        ._nav-modal-close:hover { background:#e2e8f0; }
        ._nav-profile-form { display:grid;gap:16px;padding:20px 24px 24px; }
        ._nav-form-grid { display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px; }
        @media(max-width:560px){ ._nav-form-grid{grid-template-columns:1fr;} }
        ._nav-form-field { display:block;min-width:0; }
        ._nav-form-field span { display:block;margin-bottom:6px;color:#374151;font-size:14px;font-weight:700; }
        ._nav-form-input {
            width:100%;border:1.5px solid #dbe3ef;border-radius:12px;
            padding:11px 14px;color:#1e293b;font:inherit;outline:none;box-sizing:border-box;
        }
        ._nav-form-input:focus { border-color:#2563eb;box-shadow:0 0 0 3px rgba(37,99,235,0.14); }
        ._nav-form-msg { border-radius:10px;padding:11px 14px;font-size:14px;font-weight:700;display:none; }
        ._nav-form-msg.error { display:block;background:#fef2f2;color:#b91c1c; }
        ._nav-form-msg.success { display:block;background:#ecfdf5;color:#047857; }
        ._nav-avatar-upload {
            display:flex;align-items:center;gap:16px;min-height:88px;
            padding:14px;border:1px solid #e2e8f0;border-radius:14px;background:#f8fafc;
        }
        ._nav-avatar-preview {
            width:72px;height:72px;flex:0 0 72px;object-fit:cover;
            border-radius:14px;background:#eff6ff;border:1px solid #dbeafe;
        }
        ._nav-avatar-controls {
            display:flex;flex-direction:column;align-items:flex-start;gap:8px;min-width:0;
        }
        ._nav-upload-btn {
            display:inline-flex;align-items:center;justify-content:center;
            min-height:40px;padding:9px 14px;line-height:1.2;
        }
        ._nav-avatar-name {
            max-width:100%;color:#64748b;font-size:13px;font-weight:600;
            line-height:1.45;overflow-wrap:anywhere;
        }
        ._nav-form-actions { display:flex;justify-content:flex-end;gap:12px;padding-top:16px;border-top:1px solid #e5e7eb; }
        ._nav-btn-secondary {
            border-radius:12px;padding:10px 18px;font:inherit;font-size:14px;font-weight:700;
            border:1.5px solid #dbe3ef;background:#fff;color:#374151;cursor:pointer;
        }
        ._nav-btn-secondary:hover { background:#f8fafc; }
        ._nav-btn-primary {
            border-radius:12px;padding:10px 18px;font:inherit;font-size:14px;font-weight:700;
            border:none;background:#2563eb;color:#fff;cursor:pointer;
        }
        ._nav-btn-primary:hover { background:#1d4ed8; }
        ._nav-btn-primary:disabled { opacity:0.6;cursor:wait; }
        @media(max-width:560px){ ._nav-avatar-upload{align-items:flex-start;} }
        </style>`;

        const profileModal = `
        <div id="_nav-profile-modal" class="_nav-modal-overlay">
            <div class="_nav-modal-card">
                <div class="_nav-modal-header">
                    <div>
                        <h2>Thông tin cá nhân</h2>
                        <p>Cập nhật thông tin hồ sơ của bạn</p>
                    </div>
                    <button class="_nav-modal-close" onclick="window._closeNavProfile()">&#x2715;</button>
                </div>
                <form id="_nav-profile-form" class="_nav-profile-form">
                    <div id="_nav-profile-msg" class="_nav-form-msg"></div>
                    <div class="_nav-form-grid">
                        <label class="_nav-form-field"><span>Họ tên</span><input id="_nav-fullName" type="text" class="_nav-form-input" required></label>
                        <label class="_nav-form-field"><span>Email</span><input id="_nav-email" type="email" class="_nav-form-input" required></label>
                        <label class="_nav-form-field"><span>Số điện thoại</span><input id="_nav-phone" type="tel" class="_nav-form-input"></label>
                        <label class="_nav-form-field"><span>Giới tính</span>
                            <select id="_nav-gender" class="_nav-form-input">
                                <option value="">Chưa cập nhật</option>
                                <option value="MALE">Nam</option>
                                <option value="FEMALE">Nữ</option>
                                <option value="OTHER">Khác</option>
                            </select>
                        </label>
                        <label class="_nav-form-field"><span>Ngày sinh</span><input id="_nav-dob" type="date" class="_nav-form-input"></label>
                        <label class="_nav-form-field"><span>CCCD/CMND</span><input id="_nav-idcard" type="text" class="_nav-form-input"></label>
                    </div>
                    <label class="_nav-form-field"><span>Địa chỉ</span><input id="_nav-address" type="text" class="_nav-form-input"></label>
                    <div class="_nav-form-field">
                        <span>Ảnh đại diện</span>
                        <div class="_nav-avatar-upload">
                            <img id="_nav-avatar-preview" class="_nav-avatar-preview"
                                 src="${getNavDefaultAvatar(displayName)}" alt="Ảnh đại diện">
                            <div class="_nav-avatar-controls">
                                <label for="_nav-avatar-file" class="_nav-btn-secondary _nav-upload-btn">Tải ảnh lên</label>
                                <input id="_nav-avatar-file" name="avatarFile" type="file" accept="image/*" hidden>
                                <div id="_nav-avatar-name" class="_nav-avatar-name">JPG, PNG, WEBP hoặc GIF, tối đa 5MB</div>
                            </div>
                        </div>
                    </div>
                    <div class="_nav-form-actions">
                        <button type="button" class="_nav-btn-secondary" onclick="window._closeNavProfile()">Hủy</button>
                        <button type="submit" id="_nav-profile-submit" class="_nav-btn-primary">Lưu thay đổi</button>
                    </div>
                </form>
            </div>
        </div>`;

        const passwordModal = `
        <div id="_nav-password-modal" class="_nav-modal-overlay">
            <div class="_nav-modal-card _nav-modal-card-sm">
                <div class="_nav-modal-header">
                    <div>
                        <h2>Đổi mật khẩu</h2>
                        <p>Nhập mật khẩu hiện tại trước khi đặt mật khẩu mới</p>
                    </div>
                    <button class="_nav-modal-close" onclick="window._closeNavPassword()">&#x2715;</button>
                </div>
                <form id="_nav-password-form" class="_nav-profile-form">
                    <div id="_nav-password-msg" class="_nav-form-msg"></div>
                    <label class="_nav-form-field"><span>Mật khẩu hiện tại</span><input id="_nav-cur-pw" type="password" class="_nav-form-input" autocomplete="current-password" required></label>
                    <label class="_nav-form-field"><span>Mật khẩu mới</span><input id="_nav-new-pw" type="password" class="_nav-form-input" autocomplete="new-password" minlength="6" required></label>
                    <label class="_nav-form-field"><span>Xác nhận mật khẩu mới</span><input id="_nav-confirm-pw" type="password" class="_nav-form-input" autocomplete="new-password" minlength="6" required></label>
                    <div class="_nav-form-actions">
                        <button type="button" class="_nav-btn-secondary" onclick="window._closeNavPassword()">Hủy</button>
                        <button type="submit" id="_nav-pw-submit" class="_nav-btn-primary">Đổi mật khẩu</button>
                    </div>
                </form>
            </div>
        </div>`;

        document.head.insertAdjacentHTML('beforeend', modalStyles);
        document.body.insertAdjacentHTML('beforeend', profileModal + passwordModal);

        // Close on backdrop click
        document.getElementById('_nav-profile-modal').addEventListener('click', function(e) {
            if (e.target === this) window._closeNavProfile();
        });
        document.getElementById('_nav-password-modal').addEventListener('click', function(e) {
            if (e.target === this) window._closeNavPassword();
        });

        const avatarInput = document.getElementById('_nav-avatar-file');
        if (avatarInput) {
            avatarInput.addEventListener('change', function() {
                const file = avatarInput.files && avatarInput.files[0];
                const msg = document.getElementById('_nav-profile-msg');
                if (msg) { msg.className = '_nav-form-msg'; msg.textContent = ''; }
                if (!file) {
                    setNavAvatarPreview(null);
                    return;
                }
                if (!file.type || !file.type.startsWith('image/')) {
                    avatarInput.value = '';
                    if (msg) { msg.className = '_nav-form-msg error'; msg.textContent = 'File ảnh đại diện phải là ảnh hợp lệ'; }
                    setNavAvatarPreview(null);
                    return;
                }
                if (file.size > 5 * 1024 * 1024) {
                    avatarInput.value = '';
                    if (msg) { msg.className = '_nav-form-msg error'; msg.textContent = 'Ảnh đại diện không được vượt quá 5MB'; }
                    setNavAvatarPreview(null);
                    return;
                }
                if (navAvatarObjectUrl) {
                    URL.revokeObjectURL(navAvatarObjectUrl);
                }
                navAvatarObjectUrl = URL.createObjectURL(file);
                setNavAvatarPreview(navAvatarObjectUrl, file.name);
            });
        }

        // Profile form submit
        document.getElementById('_nav-profile-form').addEventListener('submit', async function(e) {
            e.preventDefault();
            const btn = document.getElementById('_nav-profile-submit');
            const msg = document.getElementById('_nav-profile-msg');
            btn.disabled = true; btn.textContent = 'Đang lưu...';
            msg.className = '_nav-form-msg'; msg.textContent = '';
            try {
                const t = localStorage.getItem('smartstay_token');
                const payload = new FormData();
                payload.append('fullName', document.getElementById('_nav-fullName').value.trim());
                payload.append('email', document.getElementById('_nav-email').value.trim());
                payload.append('phoneNumber', document.getElementById('_nav-phone').value.trim());
                payload.append('gender', document.getElementById('_nav-gender').value);
                const dob = document.getElementById('_nav-dob').value;
                if (dob) payload.append('dateOfBirth', dob);
                payload.append('idCardNumber', document.getElementById('_nav-idcard').value.trim());
                payload.append('address', document.getElementById('_nav-address').value.trim());
                const avatarFile = document.getElementById('_nav-avatar-file')?.files?.[0];
                if (avatarFile) payload.append('avatarFile', avatarFile);

                const res = await fetch('/api/user/profile', {
                    method: 'PUT',
                    headers: t ? {'Authorization': `Bearer ${t}`} : {},
                    credentials: 'include',
                    body: payload
                });
                const data = await res.json().catch(() => null);
                if (!res.ok) throw new Error((data && data.message) || 'Đã xảy ra lỗi khi cập nhật');
                if (data) {
                    localStorage.setItem('smartstay_user', JSON.stringify(data));
                    renderUserChip(data.fullName || data.displayName || data.username || 'Người dùng', data.avatarUrl || null);
                    setNavAvatarPreview(data.avatarUrl || getNavDefaultAvatar(data.fullName || data.username), 'Đang dùng ảnh đại diện hiện tại');
                }
                msg.className = '_nav-form-msg success'; msg.textContent = '✅ Đã lưu thông tin thành công!';
                setTimeout(() => window._closeNavProfile(), 1200);
            } catch(err) {
                msg.className = '_nav-form-msg error'; msg.textContent = err.message || 'Lỗi không xác định';
            } finally {
                btn.disabled = false; btn.textContent = 'Lưu thay đổi';
            }
        });

        // Password form submit
        document.getElementById('_nav-password-form').addEventListener('submit', async function(e) {
            e.preventDefault();
            const btn = document.getElementById('_nav-pw-submit');
            const msg = document.getElementById('_nav-password-msg');
            const np = document.getElementById('_nav-new-pw').value;
            const cp = document.getElementById('_nav-confirm-pw').value;
            msg.className = '_nav-form-msg'; msg.textContent = '';
            if (np !== cp) { msg.className = '_nav-form-msg error'; msg.textContent = 'Mật khẩu xác nhận không khớp'; return; }
            if (np.length < 6) { msg.className = '_nav-form-msg error'; msg.textContent = 'Mật khẩu phải ít nhất 6 ký tự'; return; }
            btn.disabled = true; btn.textContent = 'Đang lưu...';
            try {
                const t = localStorage.getItem('smartstay_token');
                const res = await fetch('/api/user/password', {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json', ...(t ? {'Authorization': `Bearer ${t}`} : {}) },
                    credentials: 'include',
                    body: JSON.stringify({
                        currentPassword: document.getElementById('_nav-cur-pw').value,
                        newPassword: np,
                        confirmPassword: cp
                    })
                });
                const data = await res.json().catch(() => null);
                if (!res.ok) throw new Error((data && data.message) || 'Đã xảy ra lỗi');
                msg.className = '_nav-form-msg success'; msg.textContent = '✅ Đổi mật khẩu thành công!';
                document.getElementById('_nav-password-form').reset();
                setTimeout(() => window._closeNavPassword(), 1200);
            } catch(err) {
                msg.className = '_nav-form-msg error'; msg.textContent = err.message || 'Lỗi không xác định';
            } finally {
                btn.disabled = false; btn.textContent = 'Đổi mật khẩu';
            }
        });
    }

    window._openNavProfile = async function() {
        const modal = document.getElementById('_nav-profile-modal');
        if (!modal) return;
        modal.classList.add('open');
        // Load current profile data
        try {
            const t = localStorage.getItem('smartstay_token');
            const res = await fetch('/api/user/tenant', {
                headers: t ? {'Authorization': `Bearer ${t}`} : {},
                credentials: 'include'
            });
            if (res.ok) {
                const raw = await res.json();
                const d = raw.body ? raw.body : raw;
                document.getElementById('_nav-fullName').value = d.fullName || '';
                document.getElementById('_nav-email').value = d.email || '';
                document.getElementById('_nav-phone').value = d.phoneNumber || '';
                document.getElementById('_nav-gender').value = d.gender || '';
                document.getElementById('_nav-dob').value = d.dateOfBirth || '';
                document.getElementById('_nav-idcard').value = d.idCardNumber || '';
                document.getElementById('_nav-address').value = d.address || '';
                const avatarFile = document.getElementById('_nav-avatar-file');
                if (avatarFile) avatarFile.value = '';
                setNavAvatarPreview(d.avatarUrl || getNavDefaultAvatar(d.fullName || d.username), d.avatarUrl ? 'Đang dùng ảnh đại diện hiện tại' : undefined);
            }
        } catch(e) { console.warn('Could not load profile', e); }
    };

    window._closeNavProfile = function() {
        const modal = document.getElementById('_nav-profile-modal');
        if (modal) modal.classList.remove('open');
        const msg = document.getElementById('_nav-profile-msg');
        if (msg) { msg.className = '_nav-form-msg'; msg.textContent = ''; }
        const avatarFile = document.getElementById('_nav-avatar-file');
        if (avatarFile) avatarFile.value = '';
    };

    window._openNavPassword = function() {
        const modal = document.getElementById('_nav-password-modal');
        if (modal) { modal.classList.add('open'); }
        const form = document.getElementById('_nav-password-form');
        if (form) form.reset();
        const msg = document.getElementById('_nav-password-msg');
        if (msg) { msg.className = '_nav-form-msg'; msg.textContent = ''; }
    };

    window._closeNavPassword = function() {
        const modal = document.getElementById('_nav-password-modal');
        if (modal) modal.classList.remove('open');
    };

    // Close modals on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            window._closeNavProfile && window._closeNavProfile();
            window._closeNavPassword && window._closeNavPassword();
        }
    });

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

    // Fetch user info từ API - dùng /tenant cho mọi role (hoạt động cho cả landlord, admin, customer)
    fetch('/api/user/tenant') 
    .then(r => r.ok ? r.json() : null)
    .then(raw => {
        // Fallback: dùng name/username từ JWT, KHÔNG dùng sub (có thể là số ID)
        const jwtName = payload ? (payload.name || payload.username || payload.fullName || null) : null;
        const fallback = jwtName || 'Người dùng';
        if (!raw) { renderUserChip(fallback, null); return; }
        const data = raw.body ? raw.body : raw;
        const displayName = data.fullName || data.displayName || data.username || fallback;
        renderUserChip(displayName, data.avatarUrl || null);
    })
    .catch(() => {
        const jwtName = payload ? (payload.name || payload.username || payload.fullName || null) : null;
        renderUserChip(jwtName || 'Người dùng', null);
    });
})();

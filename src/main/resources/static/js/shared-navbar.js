(function () {
    // Toggle dropdown khi click trigger
    document.querySelectorAll('.nav-dropdown-trigger').forEach(function (trigger) {
        trigger.addEventListener('click', function (e) {
            e.stopPropagation();
            var dropdown = trigger.closest('.nav-dropdown');
            var isOpen = dropdown.classList.contains('open');
            // Đóng tất cả dropdown khác
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

    // Đóng dropdown khi click ra ngoài
    document.addEventListener('click', function () {
        document.querySelectorAll('.nav-dropdown.open').forEach(function (d) {
            d.classList.remove('open');
            d.querySelector('.nav-dropdown-trigger').setAttribute('aria-expanded', 'false');
        });
    });

    // Hamburger menu toggle
    const hamburger = document.getElementById('hamburger');
    const navInner = document.getElementById('navInner');
    if (hamburger && navInner) {
        hamburger.addEventListener('click', function() {
            navInner.classList.toggle('open');
        });
    }

    // Login Auth Logic
    function parseJwt(token) {
        try {
            const base64 = token.split('.')[1]
                .replace(/-/g, '+').replace(/_/g, '/');
            return JSON.parse(atob(base64));
        } catch { return null; }
    }

    const token = localStorage.getItem('smartstay_token');
    const payload = token ? parseJwt(token) : null;
    const isLoggedIn = payload && (payload.exp * 1000 > Date.now());

    if (!isLoggedIn) {
        return;
    }

    // ── Ẩn Login / Register ──────────────────────────────
    const btnLogin = document.getElementById('btnLogin');
    const btnRegister = document.getElementById('btnRegister');
    if(btnLogin) btnLogin.style.display = 'none';
    if(btnRegister) btnRegister.style.display = 'none';

    // ── Hiện tên user ────────────────────────────────────
    const username = payload.sub || payload.username || payload.name || 'User';
    const userAvatar = document.getElementById('userAvatar');
    const userNameEl = document.getElementById('userName');
    const navUser = document.getElementById('navUser');
    const btnLogout = document.getElementById('btnLogout');
    
    if(userAvatar) userAvatar.textContent = username.charAt(0).toUpperCase();
    if(userNameEl) userNameEl.textContent = username;
    if(navUser) navUser.classList.add('visible');
    if(btnLogout) btnLogout.classList.add('visible');

    // ── Lấy roles từ Token ────────────────────────────────
    const rawRoles = [].concat(
        payload.roles || payload.authorities || payload.role || []
    );
    const roles = rawRoles.map(r =>
        (typeof r === 'string' ? r : (r.authority || '')).toUpperCase()
    );

    // ── XỬ LÝ PHÂN QUYỀN (ROLE-BASED UI) ───────────────────
    const isAdmin = roles.some(r => r.includes('ADMIN'));
    const isLandlord = roles.some(r => r.includes('LANDLORD'));
    const isTenant = !isAdmin && !isLandlord;

    if (isAdmin) {
        const btnAdminVerify = document.getElementById('btnAdminVerify');
        if(btnAdminVerify) btnAdminVerify.classList.add('visible');
    }

    if (isLandlord) {
        document.querySelectorAll('.nav-link-landlord').forEach(el => {
            el.style.display = 'inline-flex';
        });
        const regLandlordLink = document.getElementById('linkRegisterLandlord');
        if (regLandlordLink) regLandlordLink.style.display = 'none';
    }

    if (isTenant) {
        document.querySelectorAll('.nav-link-tenant').forEach(el => {
            el.style.display = 'inline-flex';
        });
    }

    // ── Logout ────────────────────────────────────────────
    if(btnLogout) {
        btnLogout.addEventListener('click', function () {
            localStorage.removeItem('smartstay_token');
            localStorage.removeItem('smartstay_user');
            document.cookie = 'smartstay_token=; Max-Age=0; path=/';
            window.location.href = '/login';
        });
    }
})();

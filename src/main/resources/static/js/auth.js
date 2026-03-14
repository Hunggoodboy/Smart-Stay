const btnLogin = document.getElementById('btn-login');
const btnRegister = document.getElementById('btn-register');
const formLogin = document.getElementById('form-login');
const formRegister = document.getElementById('form-register');
const loginUsername = document.getElementById('login-username');
const loginPassword = document.getElementById('login-password');
const loginSubmit = document.getElementById('login-submit');
const loginMessage = document.getElementById('login-message');

const registerFullName = document.getElementById('register-full-name');
const registerPhone = document.getElementById('register-phone');
const registerEmail = document.getElementById('register-email');
const registerUsername = document.getElementById('register-username');
const registerPassword = document.getElementById('register-password');
const registerConfirmPassword = document.getElementById('register-confirm-password');
const registerSubmit = document.getElementById('register-submit');
const registerMessage = document.getElementById('register-message');
const registerPasswordError = document.getElementById('register-password-error');

const API_ROUTES = {
    users: '/api/user/register',
    sessions: '/api/user/login'
};

function setMode(mode) {
    const loginMode = mode === 'login';

    if (formLogin) formLogin.classList.toggle('hidden', !loginMode);
    if (formRegister) formRegister.classList.toggle('hidden', loginMode);

    if (btnLogin) {
        btnLogin.className = loginMode
            ? 'rounded-lg bg-white px-4 py-2 text-slate-900 shadow-sm transition'
            : 'rounded-lg px-4 py-2 text-slate-500 transition hover:text-slate-700';
    }

    if (btnRegister) {
        btnRegister.className = loginMode
            ? 'rounded-lg px-4 py-2 text-slate-500 transition hover:text-slate-700'
            : 'rounded-lg bg-white px-4 py-2 text-slate-900 shadow-sm transition';
    }
}

// Thêm optional chaining (?.) để tránh lỗi nếu phần tử không tồn tại trên DOM
btnLogin?.addEventListener('click', () => {
    setMode('login');
    hideMessage(loginMessage); // Ẩn thông báo cũ khi chuyển tab
});
btnRegister?.addEventListener('click', () => {
    setMode('register');
    hideMessage(registerMessage);
});

function showMessage(target, message, success) {
    if (!target) return;

    target.textContent = message;
    target.classList.remove('hidden');
    target.className = success
        ? 'rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700'
        : 'rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700';
}

function hideMessage(target) {
    if (!target) return;

    target.classList.add('hidden');
    target.textContent = '';
}

async function postJson(url, payload, defaultMessage = 'Đã có lỗi xảy ra, vui lòng thử lại.') {
    const response = await fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
    });

    let data;
    try {
        data = await response.json();
    } catch (error) {
        data = null;
    }

    if (!response.ok) {
        throw new Error(data && data.message ? data.message : defaultMessage);
    }

    return data;
}

function validateRegisterPasswordMatch() {
    if (!registerPassword || !registerConfirmPassword || !registerPasswordError) {
        return true;
    }

    const isMatch = registerPassword.value === registerConfirmPassword.value;

    if (!isMatch) {
        registerConfirmPassword.setCustomValidity('Mật khẩu nhập lại chưa khớp.');
        registerPasswordError.classList.remove('hidden');
    } else {
        registerConfirmPassword.setCustomValidity('');
        registerPasswordError.classList.add('hidden');
    }

    return isMatch;
}

if (registerPassword && registerConfirmPassword) {
    registerPassword.addEventListener('input', validateRegisterPasswordMatch);
    registerConfirmPassword.addEventListener('input', validateRegisterPasswordMatch);
}

if (formRegister) {
    formRegister.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideMessage(registerMessage);

        if (!validateRegisterPasswordMatch()) {
            registerConfirmPassword.reportValidity();
            return;
        }

        if (!registerPhone.checkValidity()) {
            registerPhone.reportValidity();
            return;
        }

        const payload = {
            username: registerUsername.value.trim(),
            password: registerPassword.value,
            confirmPassword: registerConfirmPassword.value,
            fullName: registerFullName.value.trim(),
            email: registerEmail.value.trim() || null,
            phoneNumber: registerPhone.value.trim()
        };

        registerSubmit.disabled = true;
        registerSubmit.textContent = 'Đang xử lý...';

        try {
            const registerResponse = await postJson(API_ROUTES.users, payload, 'Đăng ký thất bại, vui lòng thử lại.');

            formRegister.reset();
            validateRegisterPasswordMatch();
            setMode('login');

            if (loginUsername) {
                loginUsername.value = payload.username;
            }

            // Hiển thị thông báo thành công ở bên form đăng nhập (vì form đăng ký đã bị ẩn)
            const successMsg = registerResponse && registerResponse.message ? registerResponse.message : 'Đăng ký thành công. Vui lòng đăng nhập.';
            showMessage(loginMessage, successMsg, true);

        } catch (error) {
            showMessage(registerMessage, error.message, false);
        } finally {
            registerSubmit.disabled = false;
            registerSubmit.textContent = 'Đăng ký';
        }
    });
}

if (formLogin) {
    formLogin.addEventListener('submit', async (event) => {
        event.preventDefault();
        hideMessage(loginMessage);

        const payload = {
            username: loginUsername.value.trim(),
            password: loginPassword.value
        };

        loginSubmit.disabled = true;
        loginSubmit.textContent = 'Đang đăng nhập...';

        try {
            const data = await postJson(API_ROUTES.sessions, payload);
            if (data && data.user) {
                localStorage.setItem('smartstay_user', JSON.stringify(data.user));
            }
            showMessage(loginMessage, data && data.message ? data.message : 'Đăng nhập thành công.', true);
            window.location.href = '/';
        } catch (error) {
            showMessage(loginMessage, error.message, false);
        } finally {
            loginSubmit.disabled = false;
            loginSubmit.textContent = 'Đăng nhập';
        }
    });
}

// Khởi tạo trạng thái ban đầu
if (window.location.pathname === '/register') {
    setMode('register');
} else {
    setMode('login');
}
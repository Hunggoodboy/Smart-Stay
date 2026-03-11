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

    formLogin.classList.toggle('hidden', !loginMode);
    formRegister.classList.toggle('hidden', loginMode);

    btnLogin.className = loginMode
        ? 'rounded-lg bg-white px-4 py-2 text-slate-900 shadow-sm transition'
        : 'rounded-lg px-4 py-2 text-slate-500 transition hover:text-slate-700';

    btnRegister.className = loginMode
        ? 'rounded-lg px-4 py-2 text-slate-500 transition hover:text-slate-700'
        : 'rounded-lg bg-white px-4 py-2 text-slate-900 shadow-sm transition';
}

btnLogin.addEventListener('click', () => setMode('login'));
btnRegister.addEventListener('click', () => setMode('register'));

function showMessage(target, message, success) {
    if (!target) {
        return;
    }

    target.textContent = message;
    target.classList.remove('hidden');
    target.className = success
        ? 'rounded-lg border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-700'
        : 'rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700';
}

function hideMessage(target) {
    if (!target) {
        return;
    }

    target.classList.add('hidden');
    target.textContent = '';
}

async function postJson(url, payload) {
    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(payload)
    });

    let data;
    try {
        data = await response.json();
    } catch (error) {
        data = null;
    }

    if (!response.ok) {
        throw new Error(data && data.message ? data.message : 'Tên đăng nhập hoặc mật khẩu không đúng.');
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
        registerSubmit.textContent = 'Dang xu ly...';

        try {
            const data = await postJson(API_ROUTES.users, payload);
            showMessage(registerMessage, data && data.message ? data.message : 'Dang ky thanh cong.', true);
            formRegister.reset();
            validateRegisterPasswordMatch();
            if (loginUsername) {
                loginUsername.value = payload.username;
            }
            setMode('login');
        } catch (error) {
            showMessage(registerMessage, error.message, false);
        } finally {
            registerSubmit.disabled = false;
            registerSubmit.textContent = 'Dang ky';
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
        loginSubmit.textContent = 'Dang nhap...';

        try {
            const data = await postJson(API_ROUTES.sessions, payload);
            if (data && data.user) {
                localStorage.setItem('smartstay_user', JSON.stringify(data.user));
            }
            showMessage(loginMessage, data && data.message ? data.message : 'Dang nhap thanh cong.', true);
            window.location.href = '/';
        } catch (error) {
            showMessage(loginMessage, error.message, false);
        } finally {
            loginSubmit.disabled = false;
            loginSubmit.textContent = 'Dang nhap';
        }
    });
}

if (window.location.pathname === '/register') {
    setMode('register');
} else {
    setMode('login');
}

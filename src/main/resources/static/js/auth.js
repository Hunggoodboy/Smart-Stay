const btnLogin = document.getElementById('btn-login');
const btnRegister = document.getElementById('btn-register');
const formLogin = document.getElementById('form-login');
const formRegister = document.getElementById('form-register');
const registerPassword = document.getElementById('register-password');
const registerConfirmPassword = document.getElementById('register-confirm-password');
const registerPasswordError = document.getElementById('register-password-error');

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
    formRegister.addEventListener('submit', (event) => {
        if (!validateRegisterPasswordMatch()) {
            event.preventDefault();
            registerConfirmPassword.reportValidity();
        }
    });
}

(() => {
    function handleAdminLogout() {
        localStorage.removeItem('smartstay_token');
        localStorage.removeItem('smartstay_user');
        document.cookie = 'smartstay_token=; Max-Age=0; path=/';
        window.location.href = '/login';
    }

    window.handleAdminLogout = handleAdminLogout;
})();

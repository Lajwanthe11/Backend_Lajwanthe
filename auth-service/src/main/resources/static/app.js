(function () {
    const token = localStorage.getItem('accessToken');

    const loggedOut = document.getElementById('loggedOut');
    const loggedIn  = document.getElementById('loggedIn');

    function showLoggedOut() {
        loggedOut.style.display = 'block';
        loggedIn.style.display  = 'none';
    }

    function showLoggedIn() {
        loggedOut.style.display = 'none';
        loggedIn.style.display  = 'block';
    }

    async function loadProfile() {
        const res = await fetch('/api/oauth2/whoami', {
            headers: { 'Authorization': 'Bearer ' + token }
        });
        if (!res.ok) {
            localStorage.removeItem('accessToken');
            showLoggedOut();
            return;
        }
        const data = await res.json();
        document.getElementById('u-username').textContent  = data.username  ?? '-';
        document.getElementById('u-email').textContent     = data.email     ?? '-';
        document.getElementById('u-tenant').textContent    = data.tenantId  ?? '-';
        document.getElementById('u-roles').textContent     = data.authorities ?? '-';
        document.getElementById('u-principal').textContent = data.principalType ?? '-';
        showLoggedIn();
    }

    document.getElementById('logout').addEventListener('click', () => {
        localStorage.clear();
        showLoggedOut();
    });

    if (token) {
        loadProfile();
    } else {
        showLoggedOut();
    }
})();
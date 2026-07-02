function renderHeader() {
    const headerDiv = document.getElementById('header');
    if (!headerDiv) return;

    const role = localStorage.getItem('userRole');

    if (window.location.pathname.endsWith('/')) {
        headerDiv.innerHTML = `
            <header class="main-header">
                <div class="logo-section"><h1>Smart Clinic</h1></div>
            </header>`;
        return;
    }

    if (!role) {
        localStorage.clear();
        window.location.href = '/';
        return;
    }

    let navLinks = '';
    if (role === 'admin') {
        navLinks = `<button id="logoutBtn" class="adminBtn">Logout</button>`;
    } else if (role === 'doctor') {
        navLinks = `<a href="/doctor/appointments">Appointments</a>
                    <button id="logoutBtn" class="adminBtn">Logout</button>`;
    }

    headerDiv.innerHTML = `
        <header class="main-header">
            <div class="logo-section"><h1>Smart Clinic</h1></div>
            <nav class="header-nav">${navLinks}</nav>
        </header>
    `;

    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', () => {
            localStorage.removeItem('token');
            localStorage.removeItem('userRole');
            window.location.href = '/';
        });
    }
}

document.addEventListener('DOMContentLoaded', renderHeader);
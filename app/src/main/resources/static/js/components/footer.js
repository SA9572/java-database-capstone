function renderFooter() {
    const footerDiv = document.getElementById('footer');
    if (!footerDiv) return;

    footerDiv.innerHTML = `
        <footer class="main-footer">
            <p>&copy; ${new Date().getFullYear()} Smart Clinic Management System. All rights reserved.</p>
        </footer>
    `;
}

document.addEventListener('DOMContentLoaded', renderFooter);
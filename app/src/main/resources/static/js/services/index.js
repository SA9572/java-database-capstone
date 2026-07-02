import { openModal, closeModal } from './components/modals.js';

document.addEventListener('DOMContentLoaded', () => {
    // Clear any stale session on landing page
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');

    const adminBtn = document.getElementById('adminLoginBtn');
    const doctorBtn = document.getElementById('doctorLoginBtn');
    const patientBtn = document.getElementById('patientLoginBtn');

    if (adminBtn) adminBtn.addEventListener('click', () => openModal('adminLogin'));
    if (doctorBtn) doctorBtn.addEventListener('click', () => openModal('doctorLogin'));
    if (patientBtn) patientBtn.addEventListener('click', () => openModal('patientLogin'));

    const closeBtn = document.getElementById('closeModal');
    if (closeBtn) closeBtn.addEventListener('click', closeModal);
});

// Called by modal.js form submissions
window.adminLoginHandler = async function (event) {
    event.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/api/admin/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier: username, password })
        });
        const data = await response.json();

        if (response.ok && data.token) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('userRole', 'admin');
            window.location.href = `/adminDashboard/${data.token}`;
        } else {
            alert(data.error || 'Invalid username or password.');
        }
    } catch (error) {
        console.error('Admin login error:', error);
        alert('Something went wrong. Please try again.');
    }
};

window.doctorLoginHandler = async function (event) {
    event.preventDefault();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/api/doctor/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier: email, password })
        });
        const data = await response.json();

        if (response.ok && data.token) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('userRole', 'doctor');
            window.location.href = `/doctorDashboard/${data.token}`;
        } else {
            alert(data.error || 'Invalid email or password.');
        }
    } catch (error) {
        console.error('Doctor login error:', error);
        alert('Something went wrong. Please try again.');
    }
};

window.patientLoginHandler = async function (event) {
    event.preventDefault();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;

    try {
        const response = await fetch('/api/patient/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ identifier: email, password })
        });
        const data = await response.json();

        if (response.ok && data.token) {
            localStorage.setItem('token', data.token);
            localStorage.setItem('userRole', 'patient');
            window.location.href = '/patient/dashboard';
        } else {
            alert(data.error || 'Invalid email or password.');
        }
    } catch (error) {
        console.error('Patient login error:', error);
        alert('Something went wrong. Please try again.');
    }
};
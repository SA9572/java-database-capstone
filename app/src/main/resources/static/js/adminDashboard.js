import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';
import { openModal, closeModal } from './components/modals.js';

document.addEventListener('DOMContentLoaded', () => {
    loadDoctorCards();

    document.getElementById('addDocBtn').addEventListener('click', () => {
        openModal('addDoctor');
    });

    document.getElementById('searchBar').addEventListener('input', filterDoctorsOnChange);
    document.getElementById('filterTime').addEventListener('change', filterDoctorsOnChange);
    document.getElementById('filterSpecialty').addEventListener('change', filterDoctorsOnChange);

    document.getElementById('closeModal').addEventListener('click', closeModal);
});

// Load all doctors on initial page load
async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        renderDoctorList(doctors);
    } catch (error) {
        console.error('Failed to load doctors:', error);
        showError('Unable to load doctors. Please try again later.');
    }
}

// Render doctor cards to the content area
function renderDoctorList(doctors) {
    const contentDiv = document.getElementById('content');
    contentDiv.innerHTML = '';

    if (!doctors || doctors.length === 0) {
        contentDiv.innerHTML = '<p class="no-results">No doctors found.</p>';
        return;
    }

    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor, 'admin');
        contentDiv.appendChild(card);
    });
}

// Combine search + filters into one call
async function filterDoctorsOnChange() {
    const name = document.getElementById('searchBar').value.trim() || null;
    const time = document.getElementById('filterTime').value || null;
    const specialty = document.getElementById('filterSpecialty').value || null;

    try {
        const doctors = await filterDoctors(name, time, specialty);
        renderDoctorList(doctors);
    } catch (error) {
        console.error('Filter failed:', error);
        showError('Something went wrong while filtering doctors.');
    }
}

// Called by modal.js when the "Add Doctor" form is submitted
window.adminAddDoctor = async function (event) {
    event.preventDefault();

    const token = localStorage.getItem('token');
    if (!token) {
        alert('Session expired. Please log in again.');
        window.location.href = '/';
        return;
    }

    const name = document.getElementById('docName').value.trim();
    const specialty = document.getElementById('docSpecialty').value.trim();
    const email = document.getElementById('docEmail').value.trim();
    const password = document.getElementById('docPassword').value;
    const phone = document.getElementById('docPhone').value.trim();
    const availableTimes = document.getElementById('docAvailableTimes').value
        .split(',')
        .map(t => t.trim())
        .filter(Boolean);

    const doctor = { name, specialty, email, password, phone, availableTimes };

    try {
        const result = await saveDoctor(doctor, token);
        if (result.success) {
            alert('Doctor added successfully.');
            closeModal();
            loadDoctorCards();
        } else {
            alert(result.message || 'Failed to add doctor.');
        }
    } catch (error) {
        console.error('Error adding doctor:', error);
        alert('An error occurred while adding the doctor.');
    }
};

function showError(message) {
    const contentDiv = document.getElementById('content');
    contentDiv.innerHTML = `<p class="error-message">${message}</p>`;
}
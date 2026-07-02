import { getConfirmedPatientAppointments, filterPatientAppointments } from './services/patientServices.js';
import { getPrescriptionByAppointment } from './services/prescriptionServices.js';
import { openModal, closeModal } from './components/modals.js';

let selectedDate = new Date().toISOString().split('T')[0]; // today, yyyy-mm-dd

document.addEventListener('DOMContentLoaded', () => {
    const filterDateInput = document.getElementById('filterDate');
    filterDateInput.value = selectedDate;

    loadAppointments();

    document.getElementById('searchBar').addEventListener('input', handleFilterChange);

    filterDateInput.addEventListener('change', (e) => {
        selectedDate = e.target.value;
        handleFilterChange();
    });

    document.getElementById('todayBtn').addEventListener('click', () => {
        selectedDate = new Date().toISOString().split('T')[0];
        filterDateInput.value = selectedDate;
        handleFilterChange();
    });

    document.getElementById('closeModal').addEventListener('click', closeModal);
});

async function loadAppointments() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/';
        return;
    }

    try {
        const appointments = await getConfirmedPatientAppointments(selectedDate, token);
        renderAppointments(appointments);
    } catch (error) {
        console.error('Failed to load appointments:', error);
        showError('Unable to load appointments right now.');
    }
}

async function handleFilterChange() {
    const name = document.getElementById('searchBar').value.trim() || null;
    const token = localStorage.getItem('token');

    try {
        const appointments = await filterPatientAppointments(name, selectedDate, token);
        renderAppointments(appointments);
    } catch (error) {
        console.error('Filter failed:', error);
        showError('Something went wrong while filtering appointments.');
    }
}

function renderAppointments(appointments) {
    const tableBody = document.getElementById('patientTableBody');
    const noResultsMsg = document.getElementById('noAppointmentsMsg');
    tableBody.innerHTML = '';

    if (!appointments || appointments.length === 0) {
        noResultsMsg.classList.remove('hidden');
        return;
    }
    noResultsMsg.classList.add('hidden');

    appointments.forEach(appt => {
        const row = document.createElement('tr');

        const dateStr = appt.appointmentDate || appt.appointmentTime?.split('T')[0];
        const timeStr = appt.appointmentTimeOnly || appt.appointmentTime?.split('T')[1];

        row.innerHTML = `
            <td>${appt.patientName}</td>
            <td>${dateStr}</td>
            <td>${timeStr}</td>
            <td>${appt.status === 0 ? 'Scheduled' : 'Completed'}</td>
            <td><button class="viewPrescriptionsBtn adminBtn">View</button></td>
        `;

        row.querySelector('.viewPrescriptionsBtn').addEventListener('click', () => {
            viewPrescriptions(appt.id);
        });

        tableBody.appendChild(row);
    });
}

async function viewPrescriptions(appointmentId) {
    const token = localStorage.getItem('token');
    try {
        const result = await getPrescriptionByAppointment(appointmentId, token);
        const modalBody = document.getElementById('modal-body');

        if (!result.prescriptions || result.prescriptions.length === 0) {
            modalBody.innerHTML = `<h2>Prescriptions</h2><p>No prescriptions found for this appointment.</p>`;
        } else {
            const items = result.prescriptions.map(p => `
                <div class="prescription-item">
                    <p><strong>Medication:</strong> ${p.medication}</p>
                    <p><strong>Dosage:</strong> ${p.dosage}</p>
                    <p><strong>Notes:</strong> ${p.doctorNotes || 'None'}</p>
                </div>
            `).join('');
            modalBody.innerHTML = `<h2>Prescriptions</h2>${items}`;
        }

        document.getElementById('modal').classList.remove('hidden');
    } catch (error) {
        console.error('Failed to load prescriptions:', error);
        alert('Unable to load prescriptions right now.');
    }
}

function showError(message) {
    const tableBody = document.getElementById('patientTableBody');
    tableBody.innerHTML = `<tr><td colspan="5" class="error-message">${message}</td></tr>`;
}
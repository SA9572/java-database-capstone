export function openModal(type) {
    const modal = document.getElementById('modal');
    const modalBody = document.getElementById('modal-body');

    if (type === 'addDoctor') {
        modalBody.innerHTML = `
            <h2>Add Doctor</h2>
            <form onsubmit="window.adminAddDoctor(event)">
                <input type="text" id="docName" placeholder="Doctor Name" required />
                <input type="text" id="docSpecialty" placeholder="Specialty" required />
                <input type="email" id="docEmail" placeholder="Email" required />
                <input type="password" id="docPassword" placeholder="Password" required />
                <input type="tel" id="docPhone" placeholder="Phone (10 digits)" required />
                <input type="text" id="docAvailableTimes" placeholder="Available times, e.g. 09:00-10:00, 10:00-11:00" />
                <button type="submit" class="adminBtn">Save Doctor</button>
            </form>
        `;
    } else if (type === 'adminLogin') {
        modalBody.innerHTML = `
            <h2>Admin Login</h2>
            <form onsubmit="window.adminLoginHandler(event)">
                <input type="text" id="username" placeholder="Username" required />
                <input type="password" id="password" placeholder="Password" required />
                <button type="submit" class="adminBtn">Login</button>
            </form>
        `;
    } else if (type === 'doctorLogin') {
        modalBody.innerHTML = `
            <h2>Doctor Login</h2>
            <form onsubmit="window.doctorLoginHandler(event)">
                <input type="email" id="email" placeholder="Email" required />
                <input type="password" id="password" placeholder="Password" required />
                <button type="submit" class="adminBtn">Login</button>
            </form>
        `;
    } else if (type === 'patientLogin') {
        modalBody.innerHTML = `
            <h2>Patient Login</h2>
            <form onsubmit="window.patientLoginHandler(event)">
                <input type="email" id="email" placeholder="Email" required />
                <input type="password" id="password" placeholder="Password" required />
                <button type="submit" class="adminBtn">Login</button>
            </form>
        `;
    }

    modal.classList.remove('hidden');
}

export function closeModal() {
    const modal = document.getElementById('modal');
    modal.classList.add('hidden');
    document.getElementById('modal-body').innerHTML = '';
}
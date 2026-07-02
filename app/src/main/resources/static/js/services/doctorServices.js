const API_BASE_URL = '/api/doctors';

// Fetch all doctors
export async function getDoctors() {
    try {
        const response = await fetch(`${API_BASE_URL}`);
        if (!response.ok) throw new Error('Failed to fetch doctors');
        const data = await response.json();
        return data.doctors || [];
    } catch (error) {
        console.error('getDoctors error:', error);
        return [];
    }
}

// Delete a doctor by id (admin only, requires token)
export async function deleteDoctor(id, token) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}/${token}`, {
            method: 'DELETE'
        });
        const data = await response.json();
        return { success: response.ok, message: data.message };
    } catch (error) {
        console.error('deleteDoctor error:', error);
        return { success: false, message: 'Error deleting doctor.' };
    }
}

// Save (add) a new doctor
export async function saveDoctor(doctor, token) {
    try {
        const response = await fetch(`${API_BASE_URL}/${token}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(doctor)
        });
        const data = await response.json();
        return { success: response.ok, message: data.message };
    } catch (error) {
        console.error('saveDoctor error:', error);
        return { success: false, message: 'Error saving doctor.' };
    }
}

// Filter doctors by name / time / specialty
export async function filterDoctors(name, time, specialty) {
    try {
        const params = new URLSearchParams();
        if (name) params.append('name', name);
        if (time) params.append('time', time);
        if (specialty) params.append('specialty', specialty);

        const response = await fetch(`${API_BASE_URL}/filter?${params.toString()}`);
        if (!response.ok) throw new Error('Failed to filter doctors');
        const data = await response.json();
        return data.doctors || [];
    } catch (error) {
        console.error('filterDoctors error:', error);
        alert('Unable to filter doctors right now.');
        return [];
    }
}
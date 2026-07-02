const API_BASE_URL = '/api/prescriptions';

// Fetch prescriptions tied to a specific appointment
export async function getPrescriptionByAppointment(appointmentId, token) {
    try {
        const response = await fetch(`${API_BASE_URL}/${appointmentId}/${token}`);
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('getPrescriptionByAppointment error:', error);
        return { prescriptions: [] };
    }
}

// Save a new prescription (doctor only)
export async function savePrescription(prescription, token) {
    try {
        const response = await fetch(`${API_BASE_URL}/${token}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(prescription)
        });
        const data = await response.json();
        return { success: response.ok, message: data.message };
    } catch (error) {
        console.error('savePrescription error:', error);
        return { success: false, message: 'Error saving prescription.' };
    }
}
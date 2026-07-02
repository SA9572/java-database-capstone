const API_BASE_URL = '/api/patients';

// Fetch a doctor's appointments for a given date, optionally filtered by patient name
export async function getConfirmedPatientAppointments(date, token) {
    try {
        const response = await fetch(`/api/appointments/doctor/${date}/${token}`);
        if (!response.ok) throw new Error('Failed to fetch appointments');
        const data = await response.json();
        return data.appointments || [];
    } catch (error) {
        console.error('getConfirmedPatientAppointments error:', error);
        return [];
    }
}

// Filter a doctor's appointments by patient name and/or date
export async function filterPatientAppointments(name, date, token) {
    try {
        const params = new URLSearchParams();
        if (name) params.append('name', name);
        if (date) params.append('date', date);

        const response = await fetch(`/api/appointments/doctor/filter/${token}?${params.toString()}`);
        if (!response.ok) throw new Error('Failed to filter appointments');
        const data = await response.json();
        return data.appointments || [];
    } catch (error) {
        console.error('filterPatientAppointments error:', error);
        alert('Unable to filter appointments right now.');
        return [];
    }
}

// Fetch a single patient's own profile (used on patient-facing pages)
export async function getPatientData(token) {
    try {
        const response = await fetch(`${API_BASE_URL}/${token}`);
        if (!response.ok) throw new Error('Failed to fetch patient data');
        const data = await response.json();
        return data.patient || null;
    } catch (error) {
        console.error('getPatientData error:', error);
        return null;
    }
}
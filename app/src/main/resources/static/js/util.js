// Redirects to login if no token is present; returns the token if valid
export function requireAuth() {
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = '/';
        return null;
    }
    return token;
}

// Formats an ISO datetime string into separate date/time strings
export function splitDateTime(isoString) {
    const [date, time] = isoString.split('T');
    return { date, time: time ? time.substring(0, 5) : '' };
}

// Debounce helper for search bar inputs
export function debounce(fn, delay = 300) {
    let timer;
    return (...args) => {
        clearTimeout(timer);
        timer = setTimeout(() => fn(...args), delay);
    };
}
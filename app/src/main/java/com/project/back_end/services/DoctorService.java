package com.project.back_end.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.back_end.models.Doctor;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.DoctorRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TokenService tokenService;

    // Return all doctors
    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    // Add a new doctor. Returns 1 = success, -1 = already exists, 0 = internal error
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Update an existing doctor. Returns 1 = success, -1 = not found, 0 = internal error
    public int updateDoctor(Doctor doctor) {
        try {
            if (!doctorRepository.existsById(doctor.getId())) {
                return -1;
            }
            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Delete a doctor by id. Returns 1 = success, -1 = not found, 0 = internal error
    public int deleteDoctor(Long id) {
        try {
            if (!doctorRepository.existsById(id)) {
                return -1;
            }
            appointmentRepository.deleteAllByDoctorId(id);
            doctorRepository.deleteById(id);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Filter by name only
    public List<Doctor> filterByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name);
    }

    // Filter by specialty only
    public List<Doctor> filterBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }

    // Filter by name + specialty
    public List<Doctor> filterByNameAndSpecialty(String name, String specialty) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
    }

    // Combines name / time (AM-PM) / specialty filters — called by the admin dashboard filter bar
    public List<Doctor> filterDoctors(String name, String time, String specialty) {
        List<Doctor> doctors;

        if (name != null && specialty != null) {
            doctors = filterByNameAndSpecialty(name, specialty);
        } else if (name != null) {
            doctors = filterByName(name);
        } else if (specialty != null) {
            doctors = filterBySpecialty(specialty);
        } else {
            doctors = getDoctors();
        }

        if (time != null && !time.isEmpty()) {
            doctors = filterDoctorsByTime(doctors, time);
        }

        return doctors;
    }

    // Filters an in-memory list of doctors by AM/PM availability
    private List<Doctor> filterDoctorsByTime(List<Doctor> doctors, String amOrPm) {
        List<Doctor> result = new ArrayList<>();
        for (Doctor doctor : doctors) {
            boolean matches = doctor.getAvailableTimes().stream().anyMatch(slot -> {
                String startTimeStr = slot.split("-")[0].trim();
                LocalTime startTime = LocalTime.parse(startTimeStr);
                if ("AM".equalsIgnoreCase(amOrPm)) {
                    return startTime.isBefore(LocalTime.NOON);
                } else {
                    return !startTime.isBefore(LocalTime.NOON);
                }
            });
            if (matches) {
                result.add(doctor);
            }
        }
        return result;
    }

    // Checks if the doctor is available at the given time on the given date (used before booking)
    public boolean isDoctorAvailable(Long doctorId, LocalDateTime requestedTime) {
        LocalDateTime dayStart = requestedTime.toLocalDate().atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        List<com.project.back_end.models.Appointment> existing =
                appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, dayStart, dayEnd);

        return existing.stream().noneMatch(a -> a.getAppointmentTime().equals(requestedTime));
    }

    // Doctor login: validates credentials and returns a JWT, or null if invalid
    public String validateLogin(String email, String rawPassword) {
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null || !doctor.getPassword().equals(rawPassword)) {
            return null;
        }
        return tokenService.generateToken(doctor.getEmail());
    }
}
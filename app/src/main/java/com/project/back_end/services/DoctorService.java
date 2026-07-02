package com.project.back_end.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Doctor> getDoctors() {
        return doctorRepository.findAll();
    }

    // Add a new doctor. Returns 1 = success, -1 = already exists, 0 = internal error
    public int saveDoctor(Doctor doctor) {
        try {
            if (doctorRepository.findByEmail(doctor.getEmail()) != null) {
                return -1;
            }
            doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
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

            // Re-hash only if a new plain-text password was actually sent;
            // otherwise keep the existing hash untouched.
            Doctor existing = doctorRepository.findById(doctor.getId()).orElse(null);
            if (doctor.getPassword() != null && !doctor.getPassword().isBlank()
                    && (existing == null || !passwordEncoder.matches(doctor.getPassword(), existing.getPassword()))) {
                doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
            } else if (existing != null) {
                doctor.setPassword(existing.getPassword());
            }

            doctorRepository.save(doctor);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

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

    public List<Doctor> filterByName(String name) {
        return doctorRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Doctor> filterBySpecialty(String specialty) {
        return doctorRepository.findBySpecialtyIgnoreCase(specialty);
    }

    public List<Doctor> filterByNameAndSpecialty(String name, String specialty) {
        return doctorRepository.findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(name, specialty);
    }

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
        if (doctor == null || !passwordEncoder.matches(rawPassword, doctor.getPassword())) {
            return null;
        }
        return tokenService.generateToken(doctor.getEmail());
    }
}
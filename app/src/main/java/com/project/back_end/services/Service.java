package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Admin;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AdminRepository;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.repository.PatientRepository;

@Component
public class Service {

    private static final Logger logger = LoggerFactory.getLogger(Service.class);

    private final TokenService tokenService;
    private final AdminRepository adminRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;

    public Service(TokenService tokenService,
                    AdminRepository adminRepository,
                    DoctorRepository doctorRepository,
                    PatientRepository patientRepository,
                    DoctorService doctorService,
                    PatientService patientService) {
        this.tokenService = tokenService;
        this.adminRepository = adminRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
    }

    // Validates a token for a given role. Used by DashboardController and API controllers.
    public ResponseEntity<Map<String, Object>> validateToken(String token, String role) {
        Map<String, Object> response = new HashMap<>();
        boolean isValid = tokenService.validateToken(token, role);

        if (!isValid) {
            response.put("error", "Invalid or expired token");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Validates admin login credentials
    public ResponseEntity<Map<String, Object>> validateAdmin(String username, String password) {
        Map<String, Object> response = new HashMap<>();
        try {
            Admin admin = adminRepository.findByUsername(username);

            if (admin == null) {
                response.put("error", "Invalid username or password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            if (!admin.getPassword().equals(password)) {
                response.put("error", "Invalid username or password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String token = tokenService.generateToken(admin.getUsername());
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error validating admin login: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Flexible doctor filtering by any combination of name / specialty / time
    public ResponseEntity<Map<String, Object>> filterDoctor(String name, String specialty, String time) {
        Map<String, Object> response = new HashMap<>();
        List<Doctor> doctors = doctorService.filterDoctors(name, time, specialty);
        response.put("doctors", doctors);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Validates a requested appointment time against the doctor's configured available slots
    public int validateAppointment(Appointment appointment) {
        Doctor doctor = doctorRepository.findById(appointment.getDoctor().getId()).orElse(null);

        if (doctor == null) {
            return -1;
        }

        LocalDateTime requestedTime = appointment.getAppointmentTime();
        LocalTime requestedStart = requestedTime.toLocalTime();

        List<String> availableTimes = doctor.getAvailableTimes();
        for (String slot : availableTimes) {
            String startStr = slot.split("-")[0].trim();
            LocalTime slotStart = LocalTime.parse(startStr);
            if (slotStart.equals(requestedStart)) {
                return 1;
            }
        }

        return 0;
    }

    // Checks that a new patient's email/phone isn't already registered
    public boolean validatePatient(Patient patient) {
        Patient existing = patientRepository.findByEmailOrPhone(patient.getEmail(), patient.getPhone());
        return existing == null;
    }

    // Validates patient login credentials
    public ResponseEntity<Map<String, Object>> validatePatientLogin(Login login) {
        Map<String, Object> response = new HashMap<>();
        try {
            Patient patient = patientRepository.findByEmail(login.getIdentifier());

            if (patient == null || !patient.getPassword().equals(login.getPassword())) {
                response.put("error", "Invalid email or password");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            String token = tokenService.generateToken(patient.getEmail());
            response.put("token", token);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error validating patient login: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Filters the logged-in patient's appointments by condition and/or doctor name
    public ResponseEntity<Map<String, Object>> filterPatient(String condition, String doctorName, String token) {
        String email = tokenService.extractIdentifier(token);
        Patient patient = patientRepository.findByEmail(email);

        if (patient == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Patient not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Long patientId = patient.getId();

        if (condition != null && doctorName != null) {
            return patientService.filterByDoctorAndCondition(doctorName, condition, patientId);
        } else if (condition != null) {
            return patientService.filterByCondition(condition, patientId);
        } else if (doctorName != null) {
            return patientService.filterByDoctor(doctorName, patientId);
        } else {
            return patientService.getPatientAppointment(patientId, token);
        }
    }
}
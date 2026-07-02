package com.project.back_end.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repository.AppointmentRepository;
import com.project.back_end.repository.PatientRepository;

@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PatientService(PatientRepository patientRepository,
                           AppointmentRepository appointmentRepository,
                           TokenService tokenService,
                           PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    // Create a new patient. Returns 1 = success, 0 = failure
    public int createPatient(Patient patient) {
        try {
            patient.setPassword(passwordEncoder.encode(patient.getPassword()));
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            logger.error("Error creating patient: {}", e.getMessage());
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(email);

            if (patient == null || !patient.getId().equals(id)) {
                response.put("error", "Unauthorized access to patient records");
                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }

            List<Appointment> appointments = appointmentRepository.findByPatientId(id);
            List<AppointmentDTO> dtos = appointments.stream().map(this::toDTO).collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching patient appointments: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByCondition(String condition, Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("error", "Invalid condition. Use 'past' or 'future'.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<Appointment> appointments =
                    appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(id, status);
            List<AppointmentDTO> dtos = appointments.stream().map(this::toDTO).collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering appointments by condition: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByDoctor(String doctorName, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Appointment> appointments =
                    appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);
            List<AppointmentDTO> dtos = appointments.stream().map(this::toDTO).collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering appointments by doctor: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(String doctorName, String condition, Long patientId) {
        Map<String, Object> response = new HashMap<>();
        try {
            int status;
            if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else {
                response.put("error", "Invalid condition. Use 'past' or 'future'.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            List<Appointment> appointments =
                    appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);
            List<AppointmentDTO> dtos = appointments.stream().map(this::toDTO).collect(Collectors.toList());

            response.put("appointments", dtos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error filtering appointments by doctor and condition: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Map<String, Object>> getPatientDetails(String token) {
        Map<String, Object> response = new HashMap<>();
        try {
            String email = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(email);

            if (patient == null) {
                response.put("error", "Patient not found");
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }

            response.put("patient", patient);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching patient details: {}", e.getMessage());
            response.put("error", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private AppointmentDTO toDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
}
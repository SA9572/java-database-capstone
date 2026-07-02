package com.project.back_end.controllers;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repository.DoctorRepository;
import com.project.back_end.services.AppointmentService;
import com.project.back_end.services.Service;
import com.project.back_end.services.TokenService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final Service service;
    private final TokenService tokenService;
    private final DoctorRepository doctorRepository;

    public AppointmentController(AppointmentService appointmentService, Service service,
                                  TokenService tokenService, DoctorRepository doctorRepository) {
        this.appointmentService = appointmentService;
        this.service = service;
        this.tokenService = tokenService;
        this.doctorRepository = doctorRepository;
    }

    // Get a doctor's appointments for a given date, optionally filtered by patient name
    @GetMapping("/doctor/{date}/{token}")
    public ResponseEntity<Map<String, Object>> getDoctorAppointments(
            @PathVariable String date, @PathVariable String token,
            @RequestParam(required = false) String name) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "doctor");
        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String email = tokenService.extractIdentifier(token);
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        LocalDate localDate = LocalDate.parse(date);
        List<AppointmentDTO> appointments =
                appointmentService.getAppointmentsForDoctor(doctor.getId(), localDate, name);

        response.put("appointments", appointments);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Filter a doctor's appointments by patient name and/or date
    @GetMapping("/doctor/filter/{token}")
    public ResponseEntity<Map<String, Object>> filterDoctorAppointments(
            @PathVariable String token,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String date) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "doctor");
        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String email = tokenService.extractIdentifier(token);
        Doctor doctor = doctorRepository.findByEmail(email);
        if (doctor == null) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        LocalDate localDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        List<AppointmentDTO> appointments =
                appointmentService.getAppointmentsForDoctor(doctor.getId(), localDate, name);

        response.put("appointments", appointments);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Book a new appointment (patient only)
    @PostMapping("/patient/{token}")
    public ResponseEntity<Map<String, Object>> bookAppointment(
            @RequestBody Appointment appointment, @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "patient");
        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int validation = service.validateAppointment(appointment);
        if (validation == -1) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else if (validation == 0) {
            response.put("message", "Requested time is not in doctor's available slots");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        int result = appointmentService.bookAppointment(appointment);
        if (result == 1) {
            response.put("message", "Appointment booked successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else if (result == -1) {
            response.put("message", "Doctor is not available at this time");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update/reschedule an appointment
    @PutMapping("/{token}")
    public ResponseEntity<Map<String, Object>> updateAppointment(
            @RequestBody Appointment appointment, @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "patient");
        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = appointmentService.updateAppointment(appointment);
        if (result == 1) {
            response.put("message", "Appointment updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (result == -1) {
            response.put("message", "Appointment not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Cancel an appointment
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id, @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "patient");
        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = appointmentService.cancelAppointment(id);
        if (result == 1) {
            response.put("message", "Appointment cancelled successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (result == -1) {
            response.put("message", "Appointment not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
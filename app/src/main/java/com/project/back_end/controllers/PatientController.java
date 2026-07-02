package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final Service service;

    public PatientController(PatientService patientService, Service service) {
        this.patientService = patientService;
        this.service = service;
    }

    // Register a new patient
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Patient patient) {
        Map<String, Object> response = new HashMap<>();

        if (!service.validatePatient(patient)) {
            response.put("message", "Patient with this email or phone already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        }

        int result = patientService.createPatient(patient);
        if (result == 1) {
            response.put("message", "Patient registered successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Patient login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    // Get the logged-in patient's own details
    @GetMapping("/{token}")
    public ResponseEntity<Map<String, Object>> getPatientDetails(@PathVariable String token) {
        return patientService.getPatientDetails(token);
    }

    // Get all appointments for a patient
    @GetMapping("/appointments/{id}/{token}")
    public ResponseEntity<Map<String, Object>> getPatientAppointments(
            @PathVariable Long id, @PathVariable String token) {
        return patientService.getPatientAppointment(id, token);
    }

    // Filter the logged-in patient's appointments by condition and/or doctor name
    @GetMapping("/appointments/filter/{token}")
    public ResponseEntity<Map<String, Object>> filterAppointments(
            @PathVariable String token,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) String doctorName) {
        return service.filterPatient(condition, doctorName, token);
    }
}
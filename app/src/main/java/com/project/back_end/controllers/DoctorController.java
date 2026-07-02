package com.project.back_end.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;
    private final Service service;

    public DoctorController(DoctorService doctorService, Service service) {
        this.doctorService = doctorService;
        this.service = service;
    }

    // Get all doctors
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDoctors() {
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctorService.getDoctors());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // Filter doctors by name / time / specialty
    @GetMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String time,
            @RequestParam(required = false) String specialty) {
        return service.filterDoctor(name, specialty, time);
    }

    // Add a new doctor (admin only, requires token)
    @PostMapping("/{token}")
    public ResponseEntity<Map<String, Object>> saveDoctor(
            @RequestBody Doctor doctor, @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "admin");

        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.saveDoctor(doctor);
        if (result == 1) {
            response.put("message", "Doctor added successfully");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else if (result == -1) {
            response.put("message", "Doctor with this email already exists");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update an existing doctor (admin only)
    @PostMapping("/update/{token}")
    public ResponseEntity<Map<String, Object>> updateDoctor(
            @RequestBody Doctor doctor, @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "admin");

        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.updateDoctor(doctor);
        if (result == 1) {
            response.put("message", "Doctor updated successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (result == -1) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Delete a doctor (admin only)
    @DeleteMapping("/{id}/{token}")
    public ResponseEntity<Map<String, Object>> deleteDoctor(
            @PathVariable Long id, @PathVariable String token) {

        Map<String, Object> response = new HashMap<>();
        ResponseEntity<Map<String, Object>> tokenCheck = service.validateToken(token, "admin");

        if (tokenCheck.getStatusCode() != HttpStatus.OK) {
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        int result = doctorService.deleteDoctor(id);
        if (result == 1) {
            response.put("message", "Doctor deleted successfully");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else if (result == -1) {
            response.put("message", "Doctor not found");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } else {
            response.put("message", "Internal server error");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Doctor login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
        Map<String, Object> response = new HashMap<>();
        String token = doctorService.validateLogin(login.getIdentifier(), login.getPassword());

        if (token == null) {
            response.put("error", "Invalid email or password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
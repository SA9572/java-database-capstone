package com.project.back_end.mvc;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.Service;

@Controller
public class DashboardController {

    @Autowired
    private Service service;

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, Object>> validationResult = service.validateToken(token, "admin");

        if (validationResult.getStatusCode() == HttpStatus.OK) {
            return "admin/adminDashboard";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        ResponseEntity<Map<String, Object>> validationResult = service.validateToken(token, "doctor");

        if (validationResult.getStatusCode() == HttpStatus.OK) {
            return "doctor/doctorDashboard";
        } else {
            return "redirect:/";
        }
    }
}
package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.DTO.Login;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final Service service;

    public AdminController(Service service) {
        this.service = service;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Login login) {
        return service.validateAdmin(login.getIdentifier(), login.getPassword());
    }
}
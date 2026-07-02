package com.project.back_end.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.back_end.models.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    Patient findByEmail(String email);

    Patient findByEmailOrPhone(String email, String phone);
}
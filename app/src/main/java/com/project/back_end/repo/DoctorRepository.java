package com.project.back_end.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.back_end.models.Doctor;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Doctor findByEmail(String email);

    List<Doctor> findByNameContainingIgnoreCase(String name);

    List<Doctor> findBySpecialtyIgnoreCase(String specialty);

    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);
}
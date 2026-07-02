package com.project.back_end.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.project.back_end.models.Prescription;

public interface PrescriptionRepository extends MongoRepository<Prescription, String> {

    List<Prescription> findByAppointmentId(Long appointmentId);
}
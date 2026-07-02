package com.project.back_end.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.project.back_end.models.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            Long doctorId, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByDoctorIdAndPatientNameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId, String patientName, LocalDateTime start, LocalDateTime end);

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.name LIKE %:doctorName% AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(@Param("doctorName") String doctorName,
                                                       @Param("patientId") Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.name LIKE %:doctorName% AND a.patient.id = :patientId AND a.status = :status")
    List<Appointment> filterByDoctorNameAndPatientIdAndStatus(@Param("doctorName") String doctorName,
                                                                @Param("patientId") Long patientId,
                                                                @Param("status") int status);

    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);
}
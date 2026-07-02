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

    // All appointments for a doctor within a time range (used for "today's appointments")
    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(
            Long doctorId, LocalDateTime start, LocalDateTime end);

    // Doctor's appointments filtered by patient name + time range (for search bar)
    List<Appointment> findByDoctorIdAndPatientNameContainingIgnoreCaseAndAppointmentTimeBetween(
            Long doctorId, String patientName, LocalDateTime start, LocalDateTime end);

    // All appointments for a given patient
    List<Appointment> findByPatientId(Long patientId);

    // Patient appointments filtered by status, ordered by time
    List<Appointment> findByPatient_IdAndStatusOrderByAppointmentTimeAsc(Long patientId, int status);

    // Patient appointments filtered by doctor name (search)
    @Query("SELECT a FROM Appointment a WHERE a.doctor.name LIKE %:doctorName% AND a.patient.id = :patientId")
    List<Appointment> filterByDoctorNameAndPatientId(@Param("doctorName") String doctorName,
                                                       @Param("patientId") Long patientId);

    // Used when deleting a doctor — cascade-clean their appointments
    @Modifying
    @Transactional
    void deleteAllByDoctorId(Long doctorId);
}
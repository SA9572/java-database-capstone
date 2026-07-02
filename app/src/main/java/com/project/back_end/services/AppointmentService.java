package com.project.back_end.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.repository.AppointmentRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorService doctorService;

    // Book a new appointment. Returns 1 = success, -1 = doctor unavailable, 0 = internal error
    public int bookAppointment(Appointment appointment) {
        try {
            boolean available = doctorService.isDoctorAvailable(
                    appointment.getDoctor().getId(), appointment.getAppointmentTime());

            if (!available) {
                return -1;
            }

            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Update an existing appointment (e.g. reschedule). Returns 1 = success, -1 = not found, 0 = error
    public int updateAppointment(Appointment appointment) {
        try {
            if (!appointmentRepository.existsById(appointment.getId())) {
                return -1;
            }
            appointmentRepository.save(appointment);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Cancel/delete an appointment. Returns 1 = success, -1 = not found, 0 = error
    public int cancelAppointment(Long appointmentId) {
        try {
            if (!appointmentRepository.existsById(appointmentId)) {
                return -1;
            }
            appointmentRepository.deleteById(appointmentId);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // Get all appointments for a doctor on a given date, optionally filtered by patient name
    public List<AppointmentDTO> getAppointmentsForDoctor(Long doctorId, LocalDate date, String patientName) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        List<Appointment> appointments;
        if (patientName != null && !patientName.isEmpty()) {
            appointments = appointmentRepository
                    .findByDoctorIdAndPatientNameContainingIgnoreCaseAndAppointmentTimeBetween(
                            doctorId, patientName, start, end);
        } else {
            appointments = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, start, end);
        }

        return appointments.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Get all appointments for a patient
    public List<AppointmentDTO> getAppointmentsForPatient(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatientId(patientId);
        return appointments.stream().map(this::toDTO).collect(Collectors.toList());
    }

    // Converts an Appointment entity into a flat AppointmentDTO
    private AppointmentDTO toDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getDoctor().getName(),
                appointment.getPatient().getId(),
                appointment.getPatient().getName(),
                appointment.getPatient().getEmail(),
                appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(),
                appointment.getAppointmentTime(),
                appointment.getStatus()
        );
    }
}
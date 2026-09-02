package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;
import java.util.List;

public interface AppointmentDAO {
    boolean create(Appointment appointment);
    boolean existsActiveAppointment(int dentistId, java.time.LocalDate date, java.time.LocalTime time, int excludedId);
        boolean existsActiveAppointment(int dentistId, java.time.LocalDate date, java.time.LocalTime time,
            int durationMinutes, int excludedId);
    Appointment findByAppointmentNumber(String number);
    boolean update(Appointment appointment);
    List<Appointment> findByDate(java.time.LocalDate date);
    List<Appointment> findAll();
    List<Appointment> findByDentistUserId(int userId);
    boolean updateStatus(int id, String status);
    boolean updateStatusForDentist(int id, String status, int dentistUserId);
    void markCompletedAppointments();
    List<Appointment> findByDentistUserIdAndDate(int userId, java.time.LocalDate date);
}
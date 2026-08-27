package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;
import java.util.List;

public interface AppointmentDAO {
    boolean create(Appointment appointment);
    Appointment findByAppointmentNumber(String number);
    List<Appointment> findByDate(java.time.LocalDate date);
    List<Appointment> findAll();
    boolean updateStatus(int id, String status);
    List<Appointment> findByDentistUserIdAndDate(int userId, java.time.LocalDate date);
}
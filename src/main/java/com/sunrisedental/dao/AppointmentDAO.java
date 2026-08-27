package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;
import java.util.List;

public interface AppointmentDAO {
    boolean create(Appointment appointment);
    Appointment findByAppointmentNumber(String number);
}
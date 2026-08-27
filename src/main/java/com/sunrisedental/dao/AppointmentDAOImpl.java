package com.sunrisedental.dao;

import com.sunrisedental.model.Appointment;

public class AppointmentDAOImpl implements AppointmentDAO {
    @Override
    public boolean create(Appointment appointment) { return false; }
    
    @Override
    public Appointment findByAppointmentNumber(String number) { return null; }
}
package com.sunrisedental.controller;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.AppointmentDAOImpl;
import com.sunrisedental.model.Appointment;

public class AppointmentController {
    private AppointmentDAO appointmentDAO;

    public AppointmentController() {
        this.appointmentDAO = new AppointmentDAOImpl();
    }

    public boolean bookAppointment(Appointment appointment) {
        // Business Logic: You could add double-booking checks here later
        return appointmentDAO.create(appointment);
    }
}
package com.sunrisedental.controller;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.AppointmentDAOImpl;
import com.sunrisedental.model.Appointment;
import java.time.LocalDate;

public class AppointmentController {
    private AppointmentDAO appointmentDAO;

    public AppointmentController() {
        this.appointmentDAO = new AppointmentDAOImpl();
    }

    public AppointmentController(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
    }

    public boolean bookAppointment(Appointment appointment) {
        if (appointment == null || appointment.getPatient() == null || appointment.getDentist() == null
                || appointment.getTreatment() == null || appointment.getAppointmentDate() == null
                || appointment.getAppointmentTime() == null || appointment.getAppointmentNumber() == null
                || appointment.getAppointmentNumber().isBlank()) {
            return false;
        }
        if (appointment.getAppointmentDate().isBefore(LocalDate.now())) {
            return false;
        }
        return appointmentDAO.create(appointment);
    }

    public boolean hasAppointmentConflict(Appointment appointment) {
        if (appointment == null || appointment.getDentist() == null || appointment.getTreatment() == null
                || appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null) {
            return false;
        }
        return appointmentDAO.existsActiveAppointment(appointment.getDentist().getId(), appointment.getAppointmentDate(),
                appointment.getAppointmentTime(), appointment.getTreatment().getDurationMinutes(), appointment.getId());
    }

    public java.util.List<Appointment> getAllAppointments() {
        return appointmentDAO.findAll();
    }

    public java.util.List<Appointment> getDentistAppointments(int userId) {
        return appointmentDAO.findByDentistUserId(userId);
    }

    public Appointment searchByNumber(String number) {
        return number == null || number.isBlank() ? null : appointmentDAO.findByAppointmentNumber(number.trim());
    }

    public boolean updateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentDate() == null
                || appointment.getAppointmentDate().isBefore(LocalDate.now())) return false;
        return appointmentDAO.update(appointment);
    }

    public boolean cancelAppointment(int id) {
        return appointmentDAO.updateStatus(id, "cancelled");
    }

    public boolean confirmAppointment(int id, int dentistUserId) {
        return appointmentDAO.updateStatusForDentist(id, "confirmed", dentistUserId);
    }

    public boolean cancelAppointmentByDentist(int id, int dentistUserId) {
        return appointmentDAO.updateStatusForDentist(id, "cancelled", dentistUserId);
    }

    public java.util.List<Appointment> getDentistSchedule(int userId, java.time.LocalDate date) {
        return appointmentDAO.findByDentistUserIdAndDate(userId, date);
    }
}
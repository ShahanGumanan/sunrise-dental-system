package com.sunrisedental.controller;

import com.sunrisedental.dao.AppointmentDAO;
import com.sunrisedental.dao.AppointmentDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.util.ApiClient;
import com.sunrisedental.util.JsonUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

public class AppointmentController {
    private AppointmentDAO appointmentDAO;
    private final boolean remote;

    public AppointmentController() {
        this(new AppointmentDAOImpl());
    }

    public AppointmentController(AppointmentDAO appointmentDAO) {
        this.appointmentDAO = appointmentDAO;
        this.remote = false;
    }

    public boolean bookAppointment(Appointment appointment) {
        if (appointment == null || appointment.getPatient() == null || appointment.getDentist() == null
                || appointment.getTreatment() == null || appointment.getAppointmentDate() == null
                || appointment.getAppointmentTime() == null || appointment.getAppointmentNumber() == null
                || appointment.getAppointmentNumber().isBlank()) {
            return false;
        }
        if (LocalDateTime.of(appointment.getAppointmentDate(), appointment.getAppointmentTime())
            .isBefore(LocalDateTime.now())) {
            return false;
        }
        if (remote) {
            try {
                return JsonUtil.fromJson(ApiClient.post("/appointments", Map.of("appointment", appointment)), com.google.gson.JsonObject.class)
                        .get("success").getAsBoolean();
            } catch (Exception ignored) { return false; }
        }
        return appointmentDAO.create(appointment);
    }

    public boolean hasAppointmentConflict(Appointment appointment) {
        if (appointment == null || appointment.getDentist() == null || appointment.getTreatment() == null
                || appointment.getAppointmentDate() == null || appointment.getAppointmentTime() == null) {
            return false;
        }
        if (remote) {
            try {
                String endpoint = "/appointments?conflict=true&dentistId=" + appointment.getDentist().getId()
                        + "&date=" + appointment.getAppointmentDate() + "&time=" + appointment.getAppointmentTime()
                        + "&duration=" + appointment.getTreatment().getDurationMinutes() + "&excludedId=" + appointment.getId();
                return JsonUtil.fromJson(ApiClient.get(endpoint), com.google.gson.JsonObject.class).get("conflict").getAsBoolean();
            } catch (Exception ignored) { return false; }
        }
        return appointmentDAO.existsActiveAppointment(appointment.getDentist().getId(), appointment.getAppointmentDate(),
                appointment.getAppointmentTime(), appointment.getTreatment().getDurationMinutes(), appointment.getId());
    }

    public java.util.List<Appointment> getAllAppointments() {
        if (remote) {
            try { return JsonUtil.fromJsonList(ApiClient.get("/appointments"), Appointment.class); }
            catch (Exception ignored) { return java.util.Collections.emptyList(); }
        }
        return appointmentDAO.findAll();
    }

    public java.util.List<Appointment> getDentistAppointments(int userId) {
        if (remote) {
            try { return JsonUtil.fromJsonList(ApiClient.get("/appointments?dentistUserId=" + userId), Appointment.class); }
            catch (Exception ignored) { return java.util.Collections.emptyList(); }
        }
        return appointmentDAO.findByDentistUserId(userId);
    }

    public Appointment searchByNumber(String number) {
        if (number == null || number.isBlank()) return null;
        if (remote) {
            try { return JsonUtil.fromJson(ApiClient.get("/appointments?number=" + ApiClient.encode(number.trim())), Appointment.class); }
            catch (Exception ignored) { return null; }
        }
        return appointmentDAO.findByAppointmentNumber(number.trim());
    }

    public boolean updateAppointment(Appointment appointment) {
        if (appointment == null || appointment.getAppointmentDate() == null
                || appointment.getAppointmentDate().isBefore(LocalDate.now())) return false;
        if (remote) {
            try {
                return JsonUtil.fromJson(ApiClient.post("/appointments/update", Map.of("appointment", appointment)), com.google.gson.JsonObject.class)
                        .get("success").getAsBoolean();
            } catch (Exception ignored) { return false; }
        }
        return appointmentDAO.update(appointment);
    }

    public boolean cancelAppointment(int id) {
        if (remote) return postStatus("/appointments/cancel", Map.of("id", id));
        return appointmentDAO.updateStatus(id, "cancelled");
    }

    public boolean confirmAppointment(int id, int dentistUserId) {
        if (remote) return postStatus("/appointments/confirm", Map.of("id", id, "dentistUserId", dentistUserId));
        return appointmentDAO.updateStatusForDentist(id, "confirmed", dentistUserId);
    }

    public boolean cancelAppointmentByDentist(int id, int dentistUserId) {
        if (remote) return postStatus("/appointments/cancel-dentist", Map.of("id", id, "dentistUserId", dentistUserId));
        return appointmentDAO.updateStatusForDentist(id, "cancelled", dentistUserId);
    }

    public java.util.List<Appointment> getDentistSchedule(int userId, java.time.LocalDate date) {
        if (remote) {
            try { return JsonUtil.fromJsonList(ApiClient.get("/appointments?dentistUserId=" + userId + "&date=" + date), Appointment.class); }
            catch (Exception ignored) { return java.util.Collections.emptyList(); }
        }
        return appointmentDAO.findByDentistUserIdAndDate(userId, date);
    }

    private boolean postStatus(String endpoint, Object request) {
        try { return JsonUtil.fromJson(ApiClient.post(endpoint, request), com.google.gson.JsonObject.class).get("success").getAsBoolean(); }
        catch (Exception ignored) { return false; }
    }
}
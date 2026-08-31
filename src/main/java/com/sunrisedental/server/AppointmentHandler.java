package com.sunrisedental.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.dao.AppointmentDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.util.JsonUtil;
import java.time.LocalDate;
import java.util.Map;

public final class AppointmentHandler implements HttpHandler {
    private final AppointmentController controller = new AppointmentController(new AppointmentDAOImpl());

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> query = RequestSupport.query(exchange);
                if (query.containsKey("conflict")) {
                    boolean conflict = new AppointmentDAOImpl().existsActiveAppointment(
                            RequestSupport.id(query, "dentistId"), LocalDate.parse(query.get("date")),
                            java.time.LocalTime.parse(query.get("time")), Integer.parseInt(query.get("duration")),
                            Integer.parseInt(query.get("excludedId")));
                    ApiResponses.json(exchange, 200, java.util.Map.of("conflict", conflict));
                } else if (query.containsKey("number")) ApiResponses.json(exchange, 200, controller.searchByNumber(query.get("number")));
                else if (query.containsKey("dentistUserId") && query.containsKey("date"))
                    ApiResponses.json(exchange, 200, controller.getDentistSchedule(RequestSupport.id(query, "dentistUserId"), LocalDate.parse(query.get("date"))));
                else if (query.containsKey("dentistUserId"))
                    ApiResponses.json(exchange, 200, controller.getDentistAppointments(RequestSupport.id(query, "dentistUserId")));
                else ApiResponses.json(exchange, 200, controller.getAllAppointments());
            } else if ("POST".equals(exchange.getRequestMethod())) {
                JsonObject request = JsonUtil.fromJson(RequestSupport.body(exchange), JsonObject.class);
                if (path.endsWith("/confirm") || path.endsWith("/cancel-dentist")) {
                    int id = request.get("id").getAsInt();
                    int userId = request.get("dentistUserId").getAsInt();
                    boolean success = path.endsWith("/confirm")
                            ? controller.confirmAppointment(id, userId)
                            : controller.cancelAppointmentByDentist(id, userId);
                    ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
                } else if (path.endsWith("/cancel")) {
                    boolean success = controller.cancelAppointment(request.get("id").getAsInt());
                    ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
                } else if (path.endsWith("/update")) {
                    Appointment appointment = JsonUtil.fromJson(request.get("appointment"), Appointment.class);
                    boolean success = controller.updateAppointment(appointment);
                    ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
                } else {
                    Appointment appointment = JsonUtil.fromJson(request.get("appointment"), Appointment.class);
                    boolean success = controller.bookAppointment(appointment);
                    ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
                }
            } else {
                ApiResponses.methodNotAllowed(exchange);
            }
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
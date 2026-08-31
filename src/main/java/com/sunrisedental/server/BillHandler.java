package com.sunrisedental.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.controller.BillController;
import com.sunrisedental.dao.BillDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import com.sunrisedental.util.JsonUtil;
import java.util.Map;

public final class BillHandler implements HttpHandler {
    private final BillController controller = new BillController(new BillDAOImpl());

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                Map<String, String> query = RequestSupport.query(exchange);
                Bill bill = controller.findByAppointmentId(RequestSupport.id(query, "appointmentId"));
                ApiResponses.json(exchange, 200, bill);
            } else if ("POST".equals(exchange.getRequestMethod())) {
                JsonObject request = JsonUtil.fromJson(RequestSupport.body(exchange), JsonObject.class);
                Appointment appointment = JsonUtil.fromJson(request.get("appointment"), Appointment.class);
                String type = request.get("billType").getAsString();
                Bill bill = controller.generateBill(appointment, type);
                if (bill == null) ApiResponses.json(exchange, 400, java.util.Map.of("error", "Bill could not be generated"));
                else ApiResponses.json(exchange, 200, bill);
            } else {
                ApiResponses.methodNotAllowed(exchange);
            }
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
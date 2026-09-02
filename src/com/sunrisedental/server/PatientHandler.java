package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.dao.PatientDAO;
import com.sunrisedental.dao.PatientDAOImpl;
import com.sunrisedental.model.Patient;
import com.sunrisedental.util.JsonUtil;

public final class PatientHandler implements HttpHandler {
    private final PatientDAO patientDAO = new PatientDAOImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                ApiResponses.json(exchange, 200, patientDAO.findAll());
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Patient patient = JsonUtil.fromJson(RequestSupport.body(exchange), Patient.class);
                boolean success = patient != null && patientDAO.create(patient);
                ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
            } else {
                ApiResponses.methodNotAllowed(exchange);
            }
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
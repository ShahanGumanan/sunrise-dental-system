package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.dao.TreatmentDAO;
import com.sunrisedental.dao.TreatmentDAOImpl;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.util.JsonUtil;

public final class TreatmentHandler implements HttpHandler {
    private final TreatmentDAO treatmentDAO = new TreatmentDAOImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                ApiResponses.json(exchange, 200, treatmentDAO.findAll());
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Treatment treatment = JsonUtil.fromJson(RequestSupport.body(exchange), Treatment.class);
                boolean success = treatment != null && treatmentDAO.create(treatment);
                ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
            } else {
                ApiResponses.methodNotAllowed(exchange);
            }
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
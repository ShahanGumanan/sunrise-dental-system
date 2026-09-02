package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.dao.DentistDAOImpl;
import com.sunrisedental.dao.PatientDAOImpl;
import com.sunrisedental.dao.TreatmentDAOImpl;

public final class ReferenceHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                ApiResponses.methodNotAllowed(exchange);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if (path.endsWith("/patients")) ApiResponses.json(exchange, 200, new PatientDAOImpl().findAll());
            else if (path.endsWith("/dentists")) ApiResponses.json(exchange, 200, new DentistDAOImpl().findAll());
            else if (path.endsWith("/treatments")) ApiResponses.json(exchange, 200, new TreatmentDAOImpl().findAll());
            else ApiResponses.json(exchange, 404, java.util.Map.of("error", "Reference resource not found"));
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
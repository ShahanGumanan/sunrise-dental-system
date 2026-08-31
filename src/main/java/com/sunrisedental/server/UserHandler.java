package com.sunrisedental.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.DentistDAOImpl;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.UserDAOImpl;
import com.sunrisedental.model.User;
import com.sunrisedental.util.JsonUtil;

public final class UserHandler implements HttpHandler {
    private final UserDAO userDAO = new UserDAOImpl();
    private final DentistDAO dentistDAO = new DentistDAOImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if ("GET".equals(exchange.getRequestMethod())) {
                ApiResponses.json(exchange, 200, userDAO.findAll());
            } else if ("POST".equals(exchange.getRequestMethod())) {
                JsonObject request = JsonUtil.fromJson(RequestSupport.body(exchange), JsonObject.class);
                if (request.has("id") && request.has("active")) {
                    int id = request.get("id").getAsInt();
                    boolean active = request.get("active").getAsBoolean();
                    int actingId = request.has("actingUserId") ? request.get("actingUserId").getAsInt() : -1;
                    boolean success = userDAO.updateStatus(id, active, actingId);
                    ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
                    return;
                }
                User user = JsonUtil.fromJson(request, User.class);
                boolean success = user != null && userDAO.create(user);
                if (success && "dentist".equalsIgnoreCase(user.getRole())) success = dentistDAO.ensureProfileForUser(user.getId());
                ApiResponses.json(exchange, success ? 200 : 400, java.util.Map.of("success", success));
            } else {
                ApiResponses.methodNotAllowed(exchange);
            }
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
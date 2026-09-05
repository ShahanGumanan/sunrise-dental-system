package com.sunrisedental.server;

import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.UserDAOImpl;
import com.sunrisedental.model.User;
import com.sunrisedental.util.JsonUtil;
import com.sunrisedental.util.PasswordUtil;

public final class LoginHandler implements HttpHandler {
    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"POST".equals(exchange.getRequestMethod())) {
                ApiResponses.methodNotAllowed(exchange);
                return;
            }
            JsonObject request = JsonUtil.fromJson(RequestSupport.body(exchange), JsonObject.class);
            String username = request.has("username") ? request.get("username").getAsString().trim() : "";
            String password = request.has("password") ? request.get("password").getAsString() : "";
            User user = userDAO.findByUsername(username);
            if (user != null && !user.isActive()) {
                ApiResponses.json(exchange, 403, java.util.Map.of("error", "Account deactivated"));
            } else if (user != null && !password.isBlank()
                    && PasswordUtil.verify(password, user.getPasswordHash())) {
                ApiResponses.json(exchange, 200, user);
            } else {
                ApiResponses.json(exchange, 401, java.util.Map.of("error", "Invalid credentials"));
            }
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
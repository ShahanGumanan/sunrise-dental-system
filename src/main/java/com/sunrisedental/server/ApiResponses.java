package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sunrisedental.util.JsonUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

final class ApiResponses {
    private ApiResponses() { }

    static void json(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    static void methodNotAllowed(HttpExchange exchange) throws IOException {
        json(exchange, 405, java.util.Map.of("error", "Method not allowed"));
    }

    static void serverError(HttpExchange exchange, Exception error) throws IOException {
        error.printStackTrace();
        json(exchange, 500, java.util.Map.of("error", "Internal server error"));
    }
}
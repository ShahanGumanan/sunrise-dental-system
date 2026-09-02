package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

final class RequestSupport {
    private RequestSupport() { }

    static String body(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    static Map<String, String> query(HttpExchange exchange) {
        Map<String, String> values = new HashMap<>();
        String raw = exchange.getRequestURI().getRawQuery();
        if (raw == null || raw.isBlank()) return values;
        for (String item : raw.split("&")) {
            String[] pair = item.split("=", 2);
            if (pair.length == 2) values.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
        }
        return values;
    }

    static int id(Map<String, String> query, String name) {
        return Integer.parseInt(query.get(name));
    }
}
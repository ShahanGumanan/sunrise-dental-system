package com.sunrisedental.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sunrisedental.controller.ReportController;
import java.time.LocalDate;
import java.util.Map;

public final class RevenueReportHandler implements HttpHandler {
    private final ReportController controller = new ReportController();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                ApiResponses.methodNotAllowed(exchange);
                return;
            }
            Map<String, String> query = RequestSupport.query(exchange);
            ApiResponses.json(exchange, 200, controller.getRevenueReport(
                    LocalDate.parse(query.get("start")), LocalDate.parse(query.get("end"))));
        } catch (Exception error) {
            try { ApiResponses.serverError(exchange, error); } catch (Exception ignored) { }
        }
    }
}
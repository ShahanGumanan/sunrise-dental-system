package com.sunrisedental.server;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public final class DentalServer {
    private DentalServer() { }

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/patients", new PatientHandler());
        server.createContext("/api/appointments", new AppointmentHandler());
        server.createContext("/api/bills", new BillHandler());
        server.createContext("/api/reports/revenue", new RevenueReportHandler());
        server.createContext("/api/reference", new ReferenceHandler());
        server.createContext("/api/users", new UserHandler());
        server.createContext("/api/treatments", new TreatmentHandler());
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();
        System.out.println("Sunrise Dental web service is running at http://localhost:8080");
    }
}
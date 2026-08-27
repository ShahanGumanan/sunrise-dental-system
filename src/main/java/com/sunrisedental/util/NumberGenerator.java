package com.sunrisedental.util;

import com.sunrisedental.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NumberGenerator {
    
    public static String generateAppointmentNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSeq = 1;

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            Statement stmt = conn.createStatement();
            // Get the total number of appointments to generate the next sequence number
            ResultSet rs = stmt.executeQuery("SELECT COUNT(id) FROM appointments");
            if (rs.next()) {
                nextSeq = rs.getInt(1) + 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Format: APT-20261023-0001
        return String.format("APT-%s-%04d", datePart, nextSeq);
    }
}
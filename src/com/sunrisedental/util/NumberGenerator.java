package com.sunrisedental.util;

import com.sunrisedental.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class NumberGenerator {
    
    public static String generateAppointmentNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int nextSeq = 1;

        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(appointment_number, '-', -1) AS UNSIGNED)), 0) "
                + "FROM appointments WHERE appointment_number LIKE ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "APT-" + datePart + "-%");
            try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                    nextSeq = rs.getInt(1) + 1;
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate appointment number", e);
        }

        // Format: APT-20261023-0001
        return String.format("APT-%s-%04d", datePart, nextSeq);
    }

    public static String generateReceiptNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(receipt_number, '-', -1) AS UNSIGNED)), 0) "
                + "FROM bills WHERE receipt_number LIKE ?";
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "REC-" + datePart + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                int nextSeq = rs.next() ? rs.getInt(1) + 1 : 1;
                return String.format("REC-%s-%04d", datePart, nextSeq);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to generate receipt number", e);
        }
    }
}
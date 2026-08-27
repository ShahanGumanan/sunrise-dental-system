package com.sunrisedental.dao;

import com.sunrisedental.db.DatabaseConnection;
import com.sunrisedental.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BillDAOImpl implements BillDAO {
    private Connection getConnection() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public boolean create(Bill bill) {
        String sql = "INSERT INTO bills (appointment_id, receipt_number, consultation_fee, treatment_fee, discount, total, bill_type) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, bill.getAppointment().getId());
            ps.setString(2, bill.getReceiptNumber());
            ps.setDouble(3, bill.getConsultationFee());
            ps.setDouble(4, bill.getTreatmentFee());
            ps.setDouble(5, bill.getDiscount());
            ps.setDouble(6, bill.getTotal());
            ps.setString(7, bill.getBillType());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}
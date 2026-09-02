package com.sunrisedental.dao;

import com.sunrisedental.db.DatabaseConnection;
import com.sunrisedental.model.Dentist;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DentistDAOImpl implements DentistDAO {
    private Connection getConnection() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public List<Dentist> findAll() {
        List<Dentist> list = new ArrayList<>();
        repairMissingProfiles();
        // Join with users table to get the full name!
        String sql = "SELECT d.*, u.full_name FROM dentists d JOIN users u ON d.user_id = u.id WHERE u.is_active = TRUE";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapDentist(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    private void repairMissingProfiles() {
        String sql = "INSERT INTO dentists (user_id, specialization, available_days) "
                + "SELECT u.id, 'General Dentistry', 'Mon-Fri' FROM users u "
                + "WHERE u.role = 'dentist' AND u.is_active = TRUE "
                + "AND NOT EXISTS (SELECT 1 FROM dentists d WHERE d.user_id = u.id)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Dentist findById(int id) {
        String sql = "SELECT d.*, u.full_name FROM dentists d JOIN users u ON d.user_id = u.id WHERE d.id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapDentist(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean ensureProfileForUser(int userId) {
        String sql = "INSERT INTO dentists (user_id, specialization, available_days) "
                + "SELECT ?, 'General Dentistry', 'Mon-Fri' FROM DUAL "
                + "WHERE EXISTS (SELECT 1 FROM users WHERE id = ? AND role = 'dentist') "
                + "AND NOT EXISTS (SELECT 1 FROM dentists WHERE user_id = ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Dentist mapDentist(ResultSet rs) throws SQLException {
        Dentist d = new Dentist();
        d.setId(rs.getInt("id"));
        d.setUserId(rs.getInt("user_id"));
        d.setSpecialization(rs.getString("specialization"));
        d.setAvailableDays(rs.getString("available_days"));
        d.setFullName(rs.getString("full_name")); // From JOIN
        return d;
    }
}
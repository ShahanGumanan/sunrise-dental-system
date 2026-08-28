package com.sunrisedental.dao;

import com.sunrisedental.db.DatabaseConnection;
import com.sunrisedental.model.Treatment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TreatmentDAOImpl implements TreatmentDAO {
    private Connection getConnection() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public List<Treatment> findAll() {
        List<Treatment> list = new ArrayList<>();
        String sql = "SELECT * FROM treatments";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { list.add(mapTreatment(rs)); }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    @Override
    public Treatment findById(int id) {
        String sql = "SELECT * FROM treatments WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapTreatment(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public boolean create(Treatment treatment) {
        String sql = "INSERT INTO treatments (name, base_fee, consultation_fee, description) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, treatment.getName());
            ps.setDouble(2, treatment.getBaseFee());
            ps.setDouble(3, treatment.getConsultationFee());
            ps.setString(4, treatment.getDescription());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean update(Treatment treatment) {
        String sql = "UPDATE treatments SET name = ?, base_fee = ?, consultation_fee = ?, description = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, treatment.getName());
            ps.setDouble(2, treatment.getBaseFee());
            ps.setDouble(3, treatment.getConsultationFee());
            ps.setString(4, treatment.getDescription());
            ps.setInt(5, treatment.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM treatments WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Treatment mapTreatment(ResultSet rs) throws SQLException {
        Treatment t = new Treatment();
        t.setId(rs.getInt("id"));
        t.setName(rs.getString("name"));
        t.setBaseFee(rs.getDouble("base_fee"));
        t.setConsultationFee(rs.getDouble("consultation_fee"));
        t.setDescription(rs.getString("description"));
        return t;
    }
}
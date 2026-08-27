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
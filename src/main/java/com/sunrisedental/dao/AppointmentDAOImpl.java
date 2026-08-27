package com.sunrisedental.dao;

import com.sunrisedental.db.DatabaseConnection;
import com.sunrisedental.model.Appointment;
import java.sql.*;

public class AppointmentDAOImpl implements AppointmentDAO {
    
    private Connection getConnection() { 
        return DatabaseConnection.getInstance().getConnection(); 
    }

    @Override
    public boolean create(Appointment appt) {
        String sql = "INSERT INTO appointments (appointment_number, patient_id, dentist_id, treatment_id, appointment_date, appointment_time, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, appt.getAppointmentNumber());
            ps.setInt(2, appt.getPatient().getId());
            ps.setInt(3, appt.getDentist().getId());
            ps.setInt(4, appt.getTreatment().getId());
            ps.setDate(5, Date.valueOf(appt.getAppointmentDate()));
            ps.setTime(6, Time.valueOf(appt.getAppointmentTime()));
            ps.setString(7, appt.getStatus());
            ps.setString(8, appt.getNotes());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Appointment findByAppointmentNumber(String number) {
        // We will implement this fully in Part 2 when we build the Search screen
        return null; 
    }
}
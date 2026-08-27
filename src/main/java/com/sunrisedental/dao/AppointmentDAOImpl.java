package com.sunrisedental.dao;

import com.sunrisedental.db.DatabaseConnection;
import com.sunrisedental.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "SELECT a.*, p.name as p_name, t.name as t_name, t.base_fee, t.consultation_fee " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id " +
                     "JOIN treatments t ON a.treatment_id = t.id " +
                     "WHERE a.appointment_number = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setId(rs.getInt("id"));
                    appt.setAppointmentNumber(rs.getString("appointment_number"));

                    com.sunrisedental.model.Patient patient = new com.sunrisedental.model.Patient();
                    patient.setName(rs.getString("p_name"));
                    appt.setPatient(patient);

                    com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
                    treatment.setName(rs.getString("t_name"));
                    treatment.setBaseFee(rs.getDouble("base_fee"));
                    treatment.setConsultationFee(rs.getDouble("consultation_fee"));
                    appt.setTreatment(treatment);

                    return appt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Appointment> findByDate(java.time.LocalDate date) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id " +
                     "JOIN dentists d ON a.dentist_id = d.id " +
                     "JOIN users d_u ON d.user_id = d_u.id " +
                     "JOIN treatments t ON a.treatment_id = t.id " +
                     "WHERE a.appointment_date = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setAppointmentNumber(rs.getString("appointment_number"));
                    appt.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
                    appt.setStatus(rs.getString("status"));

                    com.sunrisedental.model.Patient patient = new com.sunrisedental.model.Patient();
                    patient.setName(rs.getString("p_name"));
                    appt.setPatient(patient);

                    com.sunrisedental.model.Dentist dentist = new com.sunrisedental.model.Dentist();
                    dentist.setFullName(rs.getString("d_name"));
                    appt.setDentist(dentist);

                    com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
                    treatment.setName(rs.getString("t_name"));
                    appt.setTreatment(treatment);

                    list.add(appt);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<Appointment> findAll() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id " +
                     "JOIN dentists d ON a.dentist_id = d.id " +
                     "JOIN users d_u ON d.user_id = d_u.id " +
                     "JOIN treatments t ON a.treatment_id = t.id ORDER BY a.appointment_date DESC";
        try (PreparedStatement ps = getConnection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapAppointmentFull(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean updateStatus(int id, String status) {
        String sql = "UPDATE appointments SET status = ? WHERE id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<Appointment> findByDentistUserIdAndDate(int userId, java.time.LocalDate date) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id " +
                     "JOIN dentists d ON a.dentist_id = d.id " +
                     "JOIN users d_u ON d.user_id = d_u.id " +
                     "JOIN treatments t ON a.treatment_id = t.id " +
                     "WHERE d.user_id = ? AND a.appointment_date = ? ORDER BY a.appointment_time";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAppointmentFull(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Appointment mapAppointmentFull(ResultSet rs) throws SQLException {
        Appointment appt = new Appointment();
        appt.setId(rs.getInt("id"));
        appt.setAppointmentNumber(rs.getString("appointment_number"));
        appt.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        appt.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
        appt.setStatus(rs.getString("status"));

        com.sunrisedental.model.Patient patient = new com.sunrisedental.model.Patient();
        patient.setName(rs.getString("p_name"));
        appt.setPatient(patient);

        com.sunrisedental.model.Dentist dentist = new com.sunrisedental.model.Dentist();
        dentist.setFullName(rs.getString("d_name"));
        appt.setDentist(dentist);

        com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
        treatment.setName(rs.getString("t_name"));
        appt.setTreatment(treatment);
        return appt;
    }
}
package com.sunrisedental.dao;

import com.sunrisedental.db.DatabaseConnection;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class BillDAOImpl implements BillDAO {
    private Connection getConnection() { return DatabaseConnection.getInstance().getConnection(); }

    @Override
    public List<Bill> findBillsByDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        List<Bill> list = new ArrayList<>();
        String sql = "SELECT b.*, p.name as p_name, t.name as t_name FROM bills b " +
                     "JOIN appointments a ON b.appointment_id = a.id " +
                     "JOIN patients p ON a.patient_id = p.id " +
                     "JOIN treatments t ON a.treatment_id = t.id " +
                     "WHERE DATE(b.paid_at) BETWEEN ? AND ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bill bill = new Bill();
                    bill.setReceiptNumber(rs.getString("receipt_number"));
                    bill.setTotal(rs.getDouble("total"));
                    bill.setBillType(rs.getString("bill_type"));

                    Appointment appt = new Appointment();
                    com.sunrisedental.model.Patient patient = new com.sunrisedental.model.Patient();
                    patient.setName(rs.getString("p_name"));
                    appt.setPatient(patient);

                    com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
                    treatment.setName(rs.getString("t_name"));
                    appt.setTreatment(treatment);

                    bill.setAppointment(appt);
                    list.add(bill);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

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

    @Override
    public boolean existsForAppointment(int appointmentId) {
        String sql = "SELECT 1 FROM bills WHERE appointment_id = ? LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Bill findByAppointmentId(int appointmentId) {
        String sql = "SELECT b.*, a.appointment_number, p.name as p_name, d_u.full_name as d_name, "
                + "t.name as t_name, t.description, t.duration_minutes FROM bills b "
                + "JOIN appointments a ON b.appointment_id = a.id JOIN patients p ON a.patient_id = p.id "
                + "JOIN dentists d ON a.dentist_id = d.id JOIN users d_u ON d.user_id = d_u.id "
                + "JOIN treatments t ON a.treatment_id = t.id WHERE b.appointment_id = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, appointmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Bill bill = new Bill();
                bill.setId(rs.getInt("id"));
                bill.setReceiptNumber(rs.getString("receipt_number"));
                bill.setConsultationFee(rs.getDouble("consultation_fee"));
                bill.setTreatmentFee(rs.getDouble("treatment_fee"));
                bill.setDiscount(rs.getDouble("discount"));
                bill.setTotal(rs.getDouble("total"));
                bill.setBillType(rs.getString("bill_type"));
                Appointment appointment = new Appointment();
                appointment.setId(appointmentId);
                appointment.setAppointmentNumber(rs.getString("appointment_number"));
                com.sunrisedental.model.Patient patient = new com.sunrisedental.model.Patient();
                patient.setName(rs.getString("p_name"));
                appointment.setPatient(patient);
                com.sunrisedental.model.Dentist dentist = new com.sunrisedental.model.Dentist();
                dentist.setFullName(rs.getString("d_name"));
                appointment.setDentist(dentist);
                com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
                treatment.setName(rs.getString("t_name"));
                treatment.setDescription(rs.getString("description"));
                treatment.setDurationMinutes(rs.getInt("duration_minutes"));
                appointment.setTreatment(treatment);
                bill.setAppointment(appointment);
                return bill;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
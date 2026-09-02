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

    public AppointmentDAOImpl() {
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate("ALTER TABLE appointments MODIFY COLUMN status ENUM('scheduled', 'pending', 'confirmed', 'cancelled', 'completed') NOT NULL DEFAULT 'pending'");
            statement.executeUpdate("UPDATE appointments SET status = 'pending' WHERE status = 'scheduled'");
            statement.executeUpdate("ALTER TABLE appointments MODIFY COLUMN status ENUM('pending', 'confirmed', 'cancelled', 'completed') NOT NULL DEFAULT 'pending'");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean create(Appointment appt) {
        if (appt == null || appt.getPatient() == null || appt.getDentist() == null || appt.getTreatment() == null
                || appt.getAppointmentDate() == null || appt.getAppointmentTime() == null) {
            return false;
        }
        if (existsActiveAppointment(appt.getDentist().getId(), appt.getAppointmentDate(), appt.getAppointmentTime(),
            appt.getTreatment().getDurationMinutes(), appt.getId())) {
            return false;
        }
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
    public boolean existsActiveAppointment(int dentistId, java.time.LocalDate date, java.time.LocalTime time, int excludedId) {
        return existsActiveAppointment(dentistId, date, time, 30, excludedId);
        }

        @Override
        public boolean existsActiveAppointment(int dentistId, java.time.LocalDate date, java.time.LocalTime time,
            int durationMinutes, int excludedId) {
        String sql = "SELECT 1 FROM appointments a JOIN treatments t ON a.treatment_id = t.id "
            + "WHERE a.dentist_id = ? AND a.appointment_date = ? AND a.status <> 'cancelled' AND a.id <> ? "
            + "AND TIMESTAMP(a.appointment_date, a.appointment_time) < DATE_ADD(TIMESTAMP(?, ?), INTERVAL ? MINUTE) "
            + "AND DATE_ADD(TIMESTAMP(a.appointment_date, a.appointment_time), "
            + "INTERVAL COALESCE(t.duration_minutes, 30) MINUTE) > TIMESTAMP(?, ?) LIMIT 1";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, dentistId);
            ps.setDate(2, Date.valueOf(date));
            ps.setInt(3, excludedId);
            ps.setDate(4, Date.valueOf(date));
            ps.setTime(5, Time.valueOf(time));
            ps.setInt(6, Math.max(durationMinutes, 1));
            ps.setDate(7, Date.valueOf(date));
            ps.setTime(8, Time.valueOf(time));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Appointment findByAppointmentNumber(String number) {
        markCompletedAppointments();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name, " +
                 "t.base_fee, t.consultation_fee, t.duration_minutes, t.description as treatment_description " +
                     "FROM appointments a " +
                     "JOIN patients p ON a.patient_id = p.id " +
                 "JOIN dentists d ON a.dentist_id = d.id " +
                 "JOIN users d_u ON d.user_id = d_u.id " +
                     "JOIN treatments t ON a.treatment_id = t.id " +
                     "WHERE a.appointment_number = ?";

        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, number);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Appointment appt = new Appointment();
                    appt.setId(rs.getInt("id"));
                    appt.setAppointmentNumber(rs.getString("appointment_number"));
                    appt.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
                    appt.setAppointmentTime(rs.getTime("appointment_time").toLocalTime());
                    appt.setStatus(rs.getString("status"));
                    appt.setNotes(rs.getString("notes"));

                    com.sunrisedental.model.Patient patient = new com.sunrisedental.model.Patient();
                    patient.setId(rs.getInt("patient_id"));
                    patient.setName(rs.getString("p_name"));
                    appt.setPatient(patient);

                    com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
                    treatment.setId(rs.getInt("treatment_id"));
                    treatment.setName(rs.getString("t_name"));
                    treatment.setBaseFee(rs.getDouble("base_fee"));
                    treatment.setConsultationFee(rs.getDouble("consultation_fee"));
                    treatment.setDurationMinutes(rs.getInt("duration_minutes"));
                    treatment.setDescription(rs.getString("treatment_description"));
                    appt.setTreatment(treatment);

                    com.sunrisedental.model.Dentist dentist = new com.sunrisedental.model.Dentist();
                    dentist.setId(rs.getInt("dentist_id"));
                    dentist.setFullName(rs.getString("d_name"));
                    appt.setDentist(dentist);

                    return appt;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean update(Appointment appt) {
        if (appt == null || appt.getAppointmentDate() == null || appt.getAppointmentTime() == null) return false;
        if (appt.getDentist() == null || appt.getTreatment() == null) return false;
        if (existsActiveAppointment(appt.getDentist().getId(), appt.getAppointmentDate(), appt.getAppointmentTime(),
            appt.getTreatment().getDurationMinutes(), appt.getId())) return false;
        String sql = "UPDATE appointments SET patient_id = ?, dentist_id = ?, treatment_id = ?, appointment_date = ?, appointment_time = ?, notes = ? WHERE id = ? AND status = 'pending'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, appt.getPatient().getId());
            ps.setInt(2, appt.getDentist().getId());
            ps.setInt(3, appt.getTreatment().getId());
            ps.setDate(4, Date.valueOf(appt.getAppointmentDate()));
            ps.setTime(5, Time.valueOf(appt.getAppointmentTime()));
            ps.setString(6, appt.getNotes());
            ps.setInt(7, appt.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Appointment> findByDate(java.time.LocalDate date) {
        markCompletedAppointments();
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name, "
            + "t.base_fee, t.consultation_fee, t.duration_minutes, t.description as treatment_description " +
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
                    treatment.setDurationMinutes(rs.getInt("duration_minutes"));
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
        markCompletedAppointments();
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name, "
            + "t.base_fee, t.consultation_fee, t.duration_minutes, t.description as treatment_description " +
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
    public List<Appointment> findByDentistUserId(int userId) {
        markCompletedAppointments();
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name, "
            + "t.base_fee, t.consultation_fee, t.duration_minutes, t.description as treatment_description "
                + "FROM appointments a JOIN patients p ON a.patient_id = p.id "
                + "JOIN dentists d ON a.dentist_id = d.id JOIN users d_u ON d.user_id = d_u.id "
                + "JOIN treatments t ON a.treatment_id = t.id WHERE d.user_id = ? "
                + "ORDER BY a.appointment_date DESC, a.appointment_time";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapAppointmentFull(rs));
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
    public boolean updateStatusForDentist(int id, String status, int dentistUserId) {
        String sql = "UPDATE appointments a JOIN dentists d ON a.dentist_id = d.id "
                + "SET a.status = ? WHERE a.id = ? AND d.user_id = ? AND a.status = 'pending'";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.setInt(3, dentistUserId);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void markCompletedAppointments() {
        String sql = "UPDATE appointments a JOIN treatments t ON a.treatment_id = t.id "
                + "SET a.status = 'completed' WHERE a.status = 'confirmed' "
                + "AND DATE_ADD(TIMESTAMP(a.appointment_date, a.appointment_time), "
                + "INTERVAL COALESCE(t.duration_minutes, 30) MINUTE) <= NOW()";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Appointment> findByDentistUserIdAndDate(int userId, java.time.LocalDate date) {
        markCompletedAppointments();
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.*, p.name as p_name, d_u.full_name as d_name, t.name as t_name, "
            + "t.base_fee, t.consultation_fee, t.duration_minutes, t.description as treatment_description " +
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
        appt.setNotes(rs.getString("notes"));

        com.sunrisedental.model.Patient patient = new com.sunrisedental.model.Patient();
        patient.setId(rs.getInt("patient_id"));
        patient.setName(rs.getString("p_name"));
        appt.setPatient(patient);

        com.sunrisedental.model.Dentist dentist = new com.sunrisedental.model.Dentist();
        dentist.setId(rs.getInt("dentist_id"));
        dentist.setFullName(rs.getString("d_name"));
        appt.setDentist(dentist);

        com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
        treatment.setId(rs.getInt("treatment_id"));
        treatment.setName(rs.getString("t_name"));
        treatment.setBaseFee(rs.getDouble("base_fee"));
        treatment.setConsultationFee(rs.getDouble("consultation_fee"));
        treatment.setDurationMinutes(rs.getInt("duration_minutes"));
        treatment.setDescription(rs.getString("treatment_description"));
        appt.setTreatment(treatment);
        return appt;
    }
}
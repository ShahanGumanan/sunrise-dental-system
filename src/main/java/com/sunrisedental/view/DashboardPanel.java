package com.sunrisedental.view;

import com.sunrisedental.db.DatabaseConnection;
import com.sunrisedental.util.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        refresh();
    }

    public void refresh() {
        removeAll();
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(244, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildDashboardContent(), BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("Clinic Dashboard");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(22, 38, 62));

        JLabel subtitle = new JLabel("Role: " + formatRole(SessionManager.getRole()) + "  |  User: " + SessionManager.getCurrentUser().getFullName());
        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(new Color(85, 96, 112));

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildDashboardContent() {
        String role = SessionManager.getRole();
        if ("admin".equalsIgnoreCase(role) || "receptionist".equalsIgnoreCase(role)) {
            return buildAdminDashboard();
        }
        if ("dentist".equalsIgnoreCase(role)) {
            return buildDentistDashboard();
        }
        return buildPatientDashboard();
    }

    private JPanel buildAdminDashboard() {
        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);

        JPanel summaryCards = new JPanel(new GridLayout(0, 4, 14, 14));
        summaryCards.setOpaque(false);

        summaryCards.add(new StatCard("Total Appointments", String.valueOf(count("SELECT COUNT(*) FROM appointments")), "All visits"));
        summaryCards.add(new StatCard("Pending", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE status = 'pending'")), "Awaiting action"));
        summaryCards.add(new StatCard("Confirmed", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE status = 'confirmed'")), "Ready for billing"));
        summaryCards.add(new StatCard("Completed", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE status = 'completed'")), "Closed visits"));

        summaryCards.add(new StatCard("Patients", String.valueOf(count("SELECT COUNT(*) FROM patients")), "Registered"));
        summaryCards.add(new StatCard("Active Dentists", String.valueOf(count("SELECT COUNT(*) FROM dentists d JOIN users u ON u.id = d.user_id WHERE u.is_active = TRUE")), "Clinic staff"));
        summaryCards.add(new StatCard("Cancelled", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE status = 'cancelled'")), "Cancelled bookings"));
        summaryCards.add(new StatCard("Monthly Revenue", formatMoney(sumDouble("SELECT COALESCE(SUM(total), 0) FROM bills WHERE paid_at >= DATE_FORMAT(NOW(), '%Y-%m-01')")), "This month"));

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        lower.add(buildTrendPanel("Appointment Status", statusMap(
                count("SELECT COUNT(*) FROM appointments WHERE status = 'pending'"),
                count("SELECT COUNT(*) FROM appointments WHERE status = 'confirmed'"),
                count("SELECT COUNT(*) FROM appointments WHERE status = 'completed'"),
                count("SELECT COUNT(*) FROM appointments WHERE status = 'cancelled'"))));
        lower.add(buildQuickListPanel("Quick Overview", quickAdminItems()));

        content.add(summaryCards, BorderLayout.NORTH);
        content.add(lower, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildDentistDashboard() {
        int dentistId = getCurrentDentistId();

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);

        JPanel summaryCards = new JPanel(new GridLayout(0, 4, 14, 14));
        summaryCards.setOpaque(false);

        summaryCards.add(new StatCard("Today", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date = CURDATE()", dentistId)), "Scheduled today"));
        summaryCards.add(new StatCard("Pending", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'pending'", dentistId)), "Awaiting review"));
        summaryCards.add(new StatCard("Confirmed", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'confirmed'", dentistId)), "Booked patients"));
        summaryCards.add(new StatCard("Completed", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'completed'", dentistId)), "Closed cases"));

        summaryCards.add(new StatCard("Patients", String.valueOf(count("SELECT COUNT(DISTINCT patient_id) FROM appointments WHERE dentist_id = ?", dentistId)), "Unique visits"));
        summaryCards.add(new StatCard("Upcoming 7 Days", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 6 DAY)", dentistId)), "Next week"));
        summaryCards.add(new StatCard("This Month", formatMoney(sumDouble("SELECT COALESCE(SUM(b.total), 0) FROM bills b JOIN appointments a ON a.id = b.appointment_id WHERE a.dentist_id = ? AND b.paid_at >= DATE_FORMAT(NOW(), '%Y-%m-01')", dentistId)), "Revenue"));
        summaryCards.add(new StatCard("Load", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status IN ('pending', 'confirmed')", dentistId)), "Active queue"));

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        lower.add(buildTrendPanel("Dentist Activity", statusMap(
                count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'pending'", dentistId),
                count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'confirmed'", dentistId),
                count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'completed'", dentistId),
                count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'cancelled'", dentistId))));
        lower.add(buildQuickListPanel("Schedule Snapshot", quickDentistItems(dentistId)));

        content.add(summaryCards, BorderLayout.NORTH);
        content.add(lower, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildPatientDashboard() {
        int patientId = getCurrentPatientId();

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setOpaque(false);

        JPanel summaryCards = new JPanel(new GridLayout(0, 4, 14, 14));
        summaryCards.setOpaque(false);

        summaryCards.add(new StatCard("Total Visits", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE patient_id = ?", patientId)), "Booked visits"));
        summaryCards.add(new StatCard("Upcoming", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status IN ('pending', 'confirmed') AND appointment_date >= CURDATE()", patientId)), "Next visits"));
        summaryCards.add(new StatCard("Completed", String.valueOf(count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'completed'", patientId)), "Finished visits"));
        summaryCards.add(new StatCard("Bills", String.valueOf(count("SELECT COUNT(*) FROM bills b JOIN appointments a ON a.id = b.appointment_id WHERE a.patient_id = ?", patientId)), "Payment records"));

        summaryCards.add(new StatCard("Total Spent", formatMoney(sumDouble("SELECT COALESCE(SUM(b.total), 0) FROM bills b JOIN appointments a ON a.id = b.appointment_id WHERE a.patient_id = ?", patientId)), "Lifetime"));
        summaryCards.add(new StatCard("Next Review", getNextAppointmentLabel(patientId), "Date"));
        summaryCards.add(new StatCard("Current Status", getCommonPatientStatus(patientId), "Health record"));
        summaryCards.add(new StatCard("Last Visit", getLastVisitLabel(patientId), "Latest record"));

        JPanel lower = new JPanel(new GridLayout(1, 2, 16, 0));
        lower.setOpaque(false);
        lower.add(buildTrendPanel("Visit Activity", statusMap(
                count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'pending'", patientId),
                count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'confirmed'", patientId),
                count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'completed'", patientId),
                count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'cancelled'", patientId))));
        lower.add(buildQuickListPanel("Recent Summary", quickPatientItems(patientId)));

        content.add(summaryCards, BorderLayout.NORTH);
        content.add(lower, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildTrendPanel(String title, Map<String, Integer> values) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        panel.add(label, BorderLayout.NORTH);

        JPanel bars = new JPanel();
        bars.setLayout(new BoxLayout(bars, BoxLayout.Y_AXIS));
        bars.setOpaque(false);

        int total = values.values().stream().mapToInt(Integer::intValue).sum();
        for (Map.Entry<String, Integer> entry : values.entrySet()) {
            JPanel row = new JPanel(new BorderLayout(6, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel name = new JLabel(entry.getKey());
            name.setFont(new Font("Arial", Font.PLAIN, 12));
            JProgressBar bar = new JProgressBar();
            bar.setStringPainted(true);
            bar.setString(entry.getValue() + "");
            bar.setValue(total == 0 ? 0 : (int) Math.round((entry.getValue() * 100.0) / total));
            bar.setForeground(colorFor(entry.getKey()));
            bar.setBackground(new Color(237, 241, 245));

            row.add(name, BorderLayout.NORTH);
            row.add(bar, BorderLayout.CENTER);
            bars.add(row);
            bars.add(Box.createVerticalStrut(8));
        }

        panel.add(bars, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildQuickListPanel(String title, List<String> items) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));

        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        panel.add(label, BorderLayout.NORTH);

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setOpaque(false);

        if (items.isEmpty()) {
            JLabel empty = new JLabel("No data available");
            empty.setForeground(new Color(110, 118, 126));
            listPanel.add(empty);
        } else {
            for (String item : items) {
                JLabel row = new JLabel("• " + item);
                row.setFont(new Font("Arial", Font.PLAIN, 12));
                row.setForeground(new Color(52, 64, 75));
                row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
                listPanel.add(row);
            }
        }

        JScrollPane scroll = new JScrollPane(listPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private static class StatCard extends JPanel {
        StatCard(String title, String value, String subtitle) {
            setLayout(new BorderLayout(6, 6));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 224, 230)),
                    BorderFactory.createEmptyBorder(12, 12, 12, 12)));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Arial", Font.PLAIN, 13));
            titleLabel.setForeground(new Color(85, 96, 112));

            JLabel valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Arial", Font.BOLD, 24));
            valueLabel.setForeground(new Color(20, 51, 86));

            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            subtitleLabel.setForeground(new Color(97, 120, 136));

            add(titleLabel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
            add(subtitleLabel, BorderLayout.SOUTH);
        }
    }

    private Map<String, Integer> statusMap(int pending, int confirmed, int completed, int cancelled) {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("Pending", pending);
        map.put("Confirmed", confirmed);
        map.put("Completed", completed);
        map.put("Cancelled", cancelled);
        return map;
    }

    private List<String> quickAdminItems() {
        List<String> items = new ArrayList<>();
        items.add("Pending cases: " + count("SELECT COUNT(*) FROM appointments WHERE status = 'pending'"));
        items.add("Monthly revenue: " + formatMoney(sumDouble("SELECT COALESCE(SUM(total), 0) FROM bills WHERE paid_at >= DATE_FORMAT(NOW(), '%Y-%m-01')")));
        items.add("Patients registered: " + count("SELECT COUNT(*) FROM patients"));
        items.add("Active staff: " + count("SELECT COUNT(*) FROM dentists d JOIN users u ON u.id = d.user_id WHERE u.is_active = TRUE"));
        return items;
    }

    private List<String> quickDentistItems(int dentistId) {
        List<String> items = new ArrayList<>();
        items.add("Today: " + count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND appointment_date = CURDATE()", dentistId));
        items.add("Pending review: " + count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'pending'", dentistId));
        items.add("Confirmed: " + count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'confirmed'", dentistId));
        items.add("Completed: " + count("SELECT COUNT(*) FROM appointments WHERE dentist_id = ? AND status = 'completed'", dentistId));
        return items;
    }

    private List<String> quickPatientItems(int patientId) {
        List<String> items = new ArrayList<>();
        items.add("Upcoming appointments: " + count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status IN ('pending', 'confirmed') AND appointment_date >= CURDATE()", patientId));
        items.add("Completed visits: " + count("SELECT COUNT(*) FROM appointments WHERE patient_id = ? AND status = 'completed'", patientId));
        items.add("Total bills: " + count("SELECT COUNT(*) FROM bills b JOIN appointments a ON a.id = b.appointment_id WHERE a.patient_id = ?", patientId));
        items.add("Last visit: " + getLastVisitLabel(patientId));
        return items;
    }

    private int getCurrentDentistId() {
        Integer id = queryInt("SELECT d.id FROM dentists d JOIN users u ON u.id = d.user_id WHERE u.id = ? LIMIT 1", SessionManager.getCurrentUser().getId());
        return id != null ? id : 0;
    }

    private int getCurrentPatientId() {
        Integer id = queryInt("SELECT id FROM patients WHERE name = ? LIMIT 1", SessionManager.getCurrentUser().getFullName());
        return id != null ? id : 0;
    }

    private String getNextAppointmentLabel(int patientId) {
        if (patientId == 0) return "N/A";
        String value = queryString("SELECT DATE_FORMAT(appointment_date, '%Y-%m-%d') FROM appointments WHERE patient_id = ? AND status IN ('pending', 'confirmed') AND appointment_date >= CURDATE() ORDER BY appointment_date ASC, appointment_time ASC LIMIT 1", patientId);
        return value == null ? "No upcoming" : value;
    }

    private String getLastVisitLabel(int patientId) {
        if (patientId == 0) return "N/A";
        String value = queryString("SELECT DATE_FORMAT(appointment_date, '%Y-%m-%d') FROM appointments WHERE patient_id = ? AND status = 'completed' ORDER BY appointment_date DESC LIMIT 1", patientId);
        return value == null ? "No completed visits" : value;
    }

    private String getCommonPatientStatus(int patientId) {
        if (patientId == 0) return "No profile";
        String value = queryString("SELECT CASE WHEN EXISTS (SELECT 1 FROM appointments WHERE patient_id = ? AND status IN ('pending','confirmed') AND appointment_date >= CURDATE()) THEN 'Active' ELSE 'Stable' END", patientId);
        return value == null ? "Stable" : value;
    }

    private String formatRole(String role) {
        if (role == null) return "Guest";
        return role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
    }

    private Color colorFor(String key) {
        switch (key.toLowerCase()) {
            case "pending": return new Color(244, 180, 26);
            case "confirmed": return new Color(33, 136, 229);
            case "completed": return new Color(34, 197, 94);
            case "cancelled": return new Color(239, 68, 68);
            default: return new Color(115, 123, 255);
        }
    }

    private int count(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private double sumDouble(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Integer queryInt(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String queryString(String sql, Object... params) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String formatMoney(double value) {
        return "Rs " + String.format("%,.0f", value);
    }
}
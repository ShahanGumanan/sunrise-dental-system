package com.sunrisedental.view.dentist;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalTime;
import java.util.List;
import com.sunrisedental.model.Appointment;

public class DentistSchedulePanel extends JPanel {
    private AppointmentController controller = new AppointmentController();
    private DefaultTableModel tableModel;
    private List<Appointment> currentAppointments;

    public DentistSchedulePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JLabel title = new JLabel("My Daily Schedule - Dr. " + SessionManager.getCurrentUser().getFullName());
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        topPanel.add(title, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Date", "Time", "Patient", "Treatment", "Notes", "Action"};
        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setCellRenderer((tableComponent, value, isSelected, hasFocus, row, column) -> {
            JButton button = new JButton(String.valueOf(value));
            button.setFocusPainted(false);
            button.setEnabled(value != null && !String.valueOf(value).isBlank());
            return button;
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                int column = table.columnAtPoint(event.getPoint());
                if (row < 0 || column != 5 || !"Confirm / Cancel".equals(tableModel.getValueAt(row, column))) return;
                String[] options = {"Confirm", "Cancel", "Close"};
                int choice = JOptionPane.showOptionDialog(table, "Choose an action for this pending appointment.",
                        "Appointment Action", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);
                int id = currentAppointments.get(row).getId();
                if (choice == 0 && controller.confirmAppointment(id, SessionManager.getCurrentUser().getId())) {
                    loadSchedule();
                } else if (choice == 1 && controller.cancelAppointmentByDentist(id, SessionManager.getCurrentUser().getId())) {
                    loadSchedule();
                }
            }

        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadSchedule(); // Load on startup
    }

    private void loadSchedule() {
        tableModel.setRowCount(0);
        int userId = SessionManager.getCurrentUser().getId();
        currentAppointments = controller.getDentistAppointments(userId);
        List<Appointment> list = currentAppointments;
        
        for (Appointment a : list) {
            LocalTime endTime = a.getAppointmentTime().plusMinutes(a.getTreatment().getDurationMinutes());
            tableModel.addRow(new Object[]{
                a.getAppointmentDate(), a.getAppointmentTime() + " - " + endTime, a.getPatient().getName(),
                a.getTreatment().getName(), a.getNotes() == null ? "" : a.getNotes(),
                "pending".equals(a.getStatus()) ? "Confirm / Cancel" : statusLabel(a.getStatus())
            });
        }
    }

    private String statusLabel(String status) {
        if ("confirmed".equals(status)) return "Confirmed";
        if ("cancelled".equals(status)) return "Cancelled";
        if ("completed".equals(status)) return "Completed";
        return status;
    }
}
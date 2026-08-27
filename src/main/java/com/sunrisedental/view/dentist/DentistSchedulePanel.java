package com.sunrisedental.view.dentist;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DentistSchedulePanel extends JPanel {
    private AppointmentController controller = new AppointmentController();
    private DefaultTableModel tableModel;

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

        JButton refreshBtn = new JButton("Refresh Today's Schedule");
        refreshBtn.addActionListener(e -> loadSchedule());
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Appt No", "Time", "Patient", "Treatment", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        loadSchedule(); // Load on startup
    }

    private void loadSchedule() {
        tableModel.setRowCount(0);
        int userId = SessionManager.getCurrentUser().getId();
        List<Appointment> list = controller.getDentistSchedule(userId, LocalDate.now());
        
        for (Appointment a : list) {
            tableModel.addRow(new Object[]{
                a.getAppointmentNumber(), a.getAppointmentTime(), a.getPatient().getName(),
                a.getTreatment().getName(), a.getStatus()
            });
        }
    }
}
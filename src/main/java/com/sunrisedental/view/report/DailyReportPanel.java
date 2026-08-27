package com.sunrisedental.view.report;

import com.sunrisedental.controller.ReportController;
import com.sunrisedental.model.Appointment;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class DailyReportPanel extends JPanel {
    private ReportController controller = new ReportController();
    private JTextField dateField;
    private DefaultTableModel tableModel;

    public DailyReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Control Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Report Date (YYYY-MM-DD): "));
        
        dateField = new JTextField(LocalDate.now().toString(), 10);
        JButton generateBtn = new JButton("Generate Report");
        topPanel.add(dateField);
        topPanel.add(generateBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Appt No", "Time", "Patient", "Dentist", "Treatment", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        generateBtn.addActionListener(e -> loadReport());
    }

    private void loadReport() {
        tableModel.setRowCount(0);
        try {
            LocalDate date = LocalDate.parse(dateField.getText());
            List<Appointment> list = controller.getDailyAppointments(date);
            for (Appointment a : list) {
                tableModel.addRow(new Object[]{
                    a.getAppointmentNumber(), a.getAppointmentTime(), a.getPatient().getName(),
                    a.getDentist().getFullName(), a.getTreatment().getName(), a.getStatus()
                });
            }
            if (list.isEmpty()) JOptionPane.showMessageDialog(this, "No appointments for this date.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
package com.sunrisedental.view.appointment;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.model.Appointment;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AppointmentDirectoryPanel extends JPanel {
    private AppointmentController controller = new AppointmentController();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Appointment> currentList;

    public AppointmentDirectoryPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Search Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("All Appointments Directory"));
        
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Selected Appointment");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        
        topPanel.add(refreshBtn);
        topPanel.add(cancelBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Appt No", "Date", "Time", "Patient", "Dentist", "Treatment", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        refreshBtn.addActionListener(e -> loadData());

        cancelBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select an appointment first.");
                return;
            }
            int id = (int) tableModel.getValueAt(row, 0);
            String status = (String) tableModel.getValueAt(row, 7);
            
            if (status.equals("cancelled") || status.equals("completed")) {
                JOptionPane.showMessageDialog(this, "Only scheduled appointments can be cancelled.");
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel this appointment?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.cancelAppointment(id);
                loadData();
                JOptionPane.showMessageDialog(this, "Appointment Cancelled.");
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        currentList = controller.getAllAppointments();
        for (Appointment a : currentList) {
            tableModel.addRow(new Object[]{
                a.getId(), a.getAppointmentNumber(), a.getAppointmentDate(), a.getAppointmentTime(),
                a.getPatient().getName(), a.getDentist().getFullName(), a.getTreatment().getName(), a.getStatus()
            });
        }
    }
}
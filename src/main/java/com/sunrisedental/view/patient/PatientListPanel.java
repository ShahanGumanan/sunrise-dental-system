package com.sunrisedental.view.patient;

import com.sunrisedental.controller.PatientController;
import com.sunrisedental.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PatientListPanel extends JPanel {
    private PatientController controller;
    private JTable table;
    private DefaultTableModel tableModel;

    public PatientListPanel() {
        controller = new PatientController();
        setLayout(new BorderLayout(0, 20));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel with Title and Refresh Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        
        JLabel title = new JLabel("Patient Directory", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        topPanel.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh List");
        refreshBtn.addActionListener(e -> loadPatientsData());
        topPanel.add(refreshBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Setup the Table
        String[] columns = {"ID", "Name", "Contact Number", "Date of Birth"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load data on startup
        loadPatientsData();
    }

    private void loadPatientsData() {
        tableModel.setRowCount(0); // Clear existing data
        List<Patient> patients = controller.getAllPatients();
        for (Patient p : patients) {
            Object[] row = { p.getId(), p.getName(), p.getContactNumber(), p.getDateOfBirth() };
            tableModel.addRow(row);
        }
    }
}
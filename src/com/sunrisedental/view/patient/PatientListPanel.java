package com.sunrisedental.view.patient;

import com.sunrisedental.controller.PatientController;
import com.sunrisedental.model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import com.sunrisedental.view.UiTheme;
import java.util.List;

public class PatientListPanel extends JPanel {
    private PatientController controller;
    private JTable table;
    private DefaultTableModel tableModel;

    public PatientListPanel() {
        controller = new PatientController();
        setLayout(new BorderLayout(0, 20));
        setBackground(UiTheme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel with Title and Refresh Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(UiTheme.CANVAS);
        
        JLabel title = new JLabel("Patient Directory", SwingConstants.LEFT);
        UiTheme.styleTitle(title);
        topPanel.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh List");
        UiTheme.styleButton(refreshBtn);
        refreshBtn.addActionListener(e -> loadPatientsData());
        topPanel.add(refreshBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Setup the Table
        String[] columns = {"ID", "Patient Name", "Action"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UiTheme.styleTable(table);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setCellRenderer((tableComponent, value, isSelected, hasFocus, row, column) -> {
            JButton button = new JButton(String.valueOf(value));
            button.setFocusPainted(false);
            UiTheme.styleButton(button);
            return button;
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                int column = table.columnAtPoint(event.getPoint());
                if (row >= 0 && column == 2) {
                    int patientId = ((Number) tableModel.getValueAt(row, 0)).intValue();
                    controller.getAllPatients().stream()
                            .filter(patient -> patient.getId() == patientId)
                            .findFirst()
                            .ifPresent(PatientListPanel.this::showPatientDetails);
                }
            }
        });
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Load data on startup
        loadPatientsData();
    }

    private void loadPatientsData() {
        tableModel.setRowCount(0); // Clear existing data
        List<Patient> patients = controller.getAllPatients();
        for (Patient p : patients) {
            Object[] row = { p.getId(), p.getName(), "View Full Details" };
            tableModel.addRow(row);
        }
    }

    private void showPatientDetails(Patient patient) {
        String details = "Patient ID: " + patient.getId()
                + "\nFull Name: " + patient.getName()
                + "\nContact Number: " + patient.getContactNumber()
                + "\nDate of Birth: " + patient.getDateOfBirth()
                + "\nHome Address: " + patient.getAddress();
        JOptionPane.showMessageDialog(this, details, "Patient Details", JOptionPane.INFORMATION_MESSAGE);
    }
}
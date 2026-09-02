package com.sunrisedental.view.patient;

import com.sunrisedental.controller.PatientController;
import com.sunrisedental.model.Patient;
import com.sunrisedental.util.ValidationUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.sql.Date;

public class PatientFormPanel extends JPanel {
    private PatientController controller;
    private JTextField nameField, contactField;
    private JDateChooser dobField;
    private JTextArea addressArea;

    public PatientFormPanel() {
        controller = new PatientController();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Register New Patient", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 20));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        nameField = new JTextField();
        contactField = new JTextField();
        dobField = new JDateChooser();
        dobField.setDate(Date.valueOf("2000-01-01"));
        dobField.setDateFormatString("yyyy-MM-dd");
        addressArea = new JTextArea(3, 20);
        addressArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        formPanel.add(new JLabel("Full Name:")); formPanel.add(nameField);
        formPanel.add(new JLabel("Contact Number (10 digits):")); formPanel.add(contactField);
        formPanel.add(new JLabel("Date of Birth (YYYY-MM-DD):")); formPanel.add(dobField);
        formPanel.add(new JLabel("Home Address:")); formPanel.add(new JScrollPane(addressArea));

        add(formPanel, BorderLayout.CENTER);

        JButton saveBtn = new JButton("Register Patient");
        saveBtn.setBackground(new Color(0, 153, 51));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        
        saveBtn.addActionListener(e -> savePatient());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(saveBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void savePatient() {
        String name = nameField.getText();
        String contact = contactField.getText();
        String address = addressArea.getText();

        if (!ValidationUtil.isNotEmpty(name) || !ValidationUtil.isNotEmpty(address)) {
            JOptionPane.showMessageDialog(this, "Name and Address cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ValidationUtil.isValidContact(contact)) {
            JOptionPane.showMessageDialog(this, "Contact must be exactly 10 digits and start with 0.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dobField.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Please select a date of birth.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Patient p = new Patient();
            p.setName(name);
            p.setContactNumber(contact);
            p.setAddress(address);
            p.setDateOfBirth(dobField.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());

            if (controller.registerPatient(p)) {
                JOptionPane.showMessageDialog(this, "Patient Registered Successfully!");
                nameField.setText("");
                contactField.setText("");
                addressArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Database error occurred.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date format. Use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
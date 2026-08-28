package com.sunrisedental.view.appointment;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import com.sunrisedental.util.NumberGenerator;
import com.sunrisedental.util.ValidationUtil;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.sql.Date;
import java.util.List;

public class AppointmentFormPanel extends JPanel {
    private AppointmentController controller;
    private JComboBox<Patient> patientCombo;
    private JComboBox<Dentist> dentistCombo;
    private JComboBox<Treatment> treatmentCombo;
    private JDateChooser dateField;
    private JComboBox<String> timeCombo;
    private JTextArea notesArea;
    private JLabel durationLabel;

    public AppointmentFormPanel() {
        controller = new AppointmentController();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel with Title and Refresh Button
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JLabel title = new JLabel("Register New Appointment", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        topPanel.add(title, BorderLayout.WEST);

        JButton refreshBtn = new JButton("Refresh Dropdowns");
        refreshBtn.addActionListener(e -> refreshDropdowns());
        topPanel.add(refreshBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 20));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        patientCombo = new JComboBox<>();
        patientCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setText(value instanceof Patient ? ((Patient) value).getName() : "");
                return this;
            }
        });
        dentistCombo = new JComboBox<>();
        treatmentCombo = new JComboBox<>();
        durationLabel = new JLabel("Select a treatment");
        refreshDropdowns(); // Load data
        
        dateField = new JDateChooser();
        dateField.setDate(Date.valueOf(LocalDate.now().plusDays(1)));
        dateField.setDateFormatString("yyyy-MM-dd");

        String[] times = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"};
        timeCombo = new JComboBox<>();
        timeCombo.addItem("Select a time");
        for (String time : times) timeCombo.addItem(time);
        timeCombo.addActionListener(e -> updateDurationLabel());
        treatmentCombo.addActionListener(e -> updateDurationLabel());
        updateDurationLabel();

        notesArea = new JTextArea(3, 20);
        notesArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel patientSelectionPanel = new JPanel(new BorderLayout(8, 0));
        patientSelectionPanel.setBackground(Color.WHITE);
        patientSelectionPanel.add(patientCombo, BorderLayout.CENTER);
        JButton viewPatientBtn = new JButton("View Details");
        viewPatientBtn.addActionListener(e -> showSelectedPatientDetails());
        patientSelectionPanel.add(viewPatientBtn, BorderLayout.EAST);

        formPanel.add(new JLabel("Select Patient:")); formPanel.add(patientSelectionPanel);
        formPanel.add(new JLabel("Select Dentist:")); formPanel.add(dentistCombo);
        formPanel.add(new JLabel("Select Treatment:")); formPanel.add(treatmentCombo);
        formPanel.add(new JLabel("Treatment Duration:")); formPanel.add(durationLabel);
        formPanel.add(new JLabel("Appointment Date:")); formPanel.add(dateField);
        formPanel.add(new JLabel("Time Slot:")); formPanel.add(timeCombo);
        formPanel.add(new JLabel("Treatment Notes:")); formPanel.add(new JScrollPane(notesArea));

        add(formPanel, BorderLayout.CENTER);

        JButton saveBtn = new JButton("Book Appointment");
        saveBtn.setBackground(new Color(0, 153, 51));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Arial", Font.BOLD, 14));
        saveBtn.addActionListener(e -> saveAppointment());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(saveBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void refreshDropdowns() {
        patientCombo.removeAllItems();
        dentistCombo.removeAllItems();
        treatmentCombo.removeAllItems();

        for (Patient p : new PatientDAOImpl().findAll()) { patientCombo.addItem(p); }
        for (Dentist d : new DentistDAOImpl().findAll()) { dentistCombo.addItem(d); }
        for (Treatment t : new TreatmentDAOImpl().findAll()) { treatmentCombo.addItem(t); }
        patientCombo.setSelectedItem(null);
        dentistCombo.setSelectedItem(null);
        treatmentCombo.setSelectedItem(null);
        if (timeCombo != null) timeCombo.setSelectedIndex(0);
        updateDurationLabel();
    }

    private void updateDurationLabel() {
        Treatment treatment = (Treatment) treatmentCombo.getSelectedItem();
        if (treatment == null) {
            durationLabel.setText("Select a treatment");
            return;
        }
        LocalTime start = timeCombo == null || timeCombo.getSelectedItem() == null
            || "Select a time".equals(timeCombo.getSelectedItem())
                ? null : LocalTime.parse((String) timeCombo.getSelectedItem());
        String end = start == null ? "" : " | Ends at " + start.plusMinutes(treatment.getDurationMinutes());
        durationLabel.setText(treatment.getDurationMinutes() + " minutes" + end);
    }

    private void saveAppointment() {
        // VALIDATION 1: Check if dropdowns are empty
        if (patientCombo.getSelectedItem() == null || dentistCombo.getSelectedItem() == null || treatmentCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Error: You must have at least 1 Patient, 1 Dentist, and 1 Treatment in the system to book!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dateField.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Please select a future appointment date.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String dateStr = dateField.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString();
        
        // VALIDATION 2: Check Date Format
        if (!ValidationUtil.isValidFutureDate(dateStr)) {
            JOptionPane.showMessageDialog(this, "Please enter a valid future date (YYYY-MM-DD).", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment appt = new Appointment();
        appt.setAppointmentNumber(NumberGenerator.generateAppointmentNumber());
        appt.setPatient((Patient) patientCombo.getSelectedItem());
        appt.setDentist((Dentist) dentistCombo.getSelectedItem());
        appt.setTreatment((Treatment) treatmentCombo.getSelectedItem());
        appt.setAppointmentDate(LocalDate.parse(dateStr));
        
        String timeStr = (String) timeCombo.getSelectedItem();
        if ("Select a time".equals(timeStr)) {
            JOptionPane.showMessageDialog(this, "Please select an appointment time.", "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        LocalDate appointmentDate = LocalDate.parse(dateStr);
        LocalTime appointmentTime = LocalTime.parse(timeStr);
        if (LocalDateTime.of(appointmentDate, appointmentTime).isBefore(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this, "Please select a future appointment time.", "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        appt.setAppointmentTime(LocalTime.parse(timeStr));
        
        appt.setStatus("pending");
        appt.setNotes(notesArea.getText());

        if (controller.hasAppointmentConflict(appt)) {
            JOptionPane.showMessageDialog(this, "This dentist already has an appointment during that time slot.",
                "Time slot already booked", JOptionPane.WARNING_MESSAGE);
        } else if (controller.bookAppointment(appt)) {
            JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!\nNumber: " + appt.getAppointmentNumber());
            notesArea.setText("");
            dateField.setDate(Date.valueOf(LocalDate.now().plusDays(1)));
            if (patientCombo.getItemCount() > 0) patientCombo.setSelectedIndex(0);
            if (dentistCombo.getItemCount() > 0) dentistCombo.setSelectedIndex(0);
            if (treatmentCombo.getItemCount() > 0) treatmentCombo.setSelectedIndex(0);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to book appointment. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showSelectedPatientDetails() {
        Patient patient = (Patient) patientCombo.getSelectedItem();
        if (patient == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient first.", "Patient Details",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 4);
        constraints.gridy = 0;

        addPatientDetail(detailsPanel, constraints, "Patient ID:", String.valueOf(patient.getId()));
        addPatientDetail(detailsPanel, constraints, "Full Name:", patient.getName());
        addPatientDetail(detailsPanel, constraints, "Contact Number:", patient.getContactNumber());
        addPatientDetail(detailsPanel, constraints, "Date of Birth:", String.valueOf(patient.getDateOfBirth()));
        addPatientDetail(detailsPanel, constraints, "Home Address:", patient.getAddress());

        JOptionPane.showMessageDialog(this, detailsPanel, "Patient Details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void addPatientDetail(JPanel panel, GridBagConstraints constraints, String label, String value) {
        constraints.gridx = 0;
        constraints.weightx = 0;
        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(labelComponent.getFont().deriveFont(Font.BOLD));
        panel.add(labelComponent, constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        panel.add(new JLabel(value == null ? "" : value), constraints);
        constraints.gridy++;
    }
}
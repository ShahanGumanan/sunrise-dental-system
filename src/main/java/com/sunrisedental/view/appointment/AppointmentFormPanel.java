package com.sunrisedental.view.appointment;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.dao.*;
import com.sunrisedental.model.*;
import com.sunrisedental.util.NumberGenerator;
import com.sunrisedental.util.ValidationUtil;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AppointmentFormPanel extends JPanel {
    private AppointmentController controller;
    private JComboBox<Patient> patientCombo;
    private JComboBox<Dentist> dentistCombo;
    private JComboBox<Treatment> treatmentCombo;
    private JTextField dateField;
    private JComboBox<String> timeCombo;
    private JTextArea notesArea;

    public AppointmentFormPanel() {
        controller = new AppointmentController();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Register New Appointment", SwingConstants.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        add(title, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 20));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Initialize Dropdowns by fetching data from DB
        patientCombo = new JComboBox<>(fetchPatients());
        dentistCombo = new JComboBox<>(fetchDentists());
        treatmentCombo = new JComboBox<>(fetchTreatments());
        
        dateField = new JTextField(LocalDate.now().plusDays(1).toString()); // Default to tomorrow
        dateField.setBorder(BorderFactory.createTitledBorder("Date (YYYY-MM-DD)"));

        String[] times = {"09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "13:00", "13:30", "14:00"};
        timeCombo = new JComboBox<>(times);

        notesArea = new JTextArea(3, 20);
        notesArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        formPanel.add(new JLabel("Select Patient:")); formPanel.add(patientCombo);
        formPanel.add(new JLabel("Select Dentist:")); formPanel.add(dentistCombo);
        formPanel.add(new JLabel("Select Treatment:")); formPanel.add(treatmentCombo);
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

    private Patient[] fetchPatients() {
        List<Patient> list = new PatientDAOImpl().findAll();
        return list.toArray(new Patient[0]);
    }

    private Dentist[] fetchDentists() {
        List<Dentist> list = new DentistDAOImpl().findAll();
        return list.toArray(new Dentist[0]);
    }

    private Treatment[] fetchTreatments() {
        List<Treatment> list = new TreatmentDAOImpl().findAll();
        return list.toArray(new Treatment[0]);
    }

    private void saveAppointment() {
        String dateStr = dateField.getText();
        
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
        appt.setAppointmentTime(LocalTime.parse(timeStr + ":00"));
        
        appt.setStatus("scheduled");
        appt.setNotes(notesArea.getText());

        if (controller.bookAppointment(appt)) {
            JOptionPane.showMessageDialog(this, "Appointment Booked Successfully!\nNumber: " + appt.getAppointmentNumber());
        } else {
            JOptionPane.showMessageDialog(this, "Failed to book appointment. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
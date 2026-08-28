package com.sunrisedental.view.appointment;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.util.SessionManager;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class AppointmentDirectoryPanel extends JPanel {
    private AppointmentController controller = new AppointmentController();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Appointment> currentList;
    private final boolean dentistOnly;
    private JTextField searchField;
    private JComboBox<String> statusFilter;

    public AppointmentDirectoryPanel() {
        this(false);
    }

    public AppointmentDirectoryPanel(boolean dentistOnly) {
        this.dentistOnly = dentistOnly;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Search Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Appointments"));
        searchField = new JTextField(14);
        searchField.setToolTipText("Search by appointment number");
        statusFilter = new JComboBox<>(new String[]{"All statuses", "scheduled", "completed", "cancelled"});
        
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Selected Appointment");
        JButton editBtn = new JButton("Edit Selected");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        
        topPanel.add(refreshBtn);
        topPanel.add(new JLabel("Search Appointment Number:"));
        topPanel.add(searchField);
        topPanel.add(statusFilter);
        topPanel.add(editBtn);
        topPanel.add(cancelBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Appt No", "Date", "Time", "Patient", "Dentist", "Treatment", "Status", "Notes"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        refreshBtn.addActionListener(e -> {
            statusFilter.setSelectedItem("All statuses");
            loadData();
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilter(); }
        });
        statusFilter.addActionListener(e -> applyFilter());
        editBtn.addActionListener(e -> editSelected());
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int id = ((Number) tableModel.getValueAt(table.getSelectedRow(), 0)).intValue();
                    currentList.stream().filter(a -> a.getId() == id).findFirst().ifPresent(AppointmentDirectoryPanel.this::showDetails);
                }
            }
        });

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
                if (controller.cancelAppointment(id)) {
                    loadData();
                    JOptionPane.showMessageDialog(this, "Appointment Cancelled.");
                } else {
                    JOptionPane.showMessageDialog(this, "The appointment could not be cancelled.", "Cancellation failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        currentList = dentistOnly
            ? controller.getDentistAppointments(SessionManager.getCurrentUser().getId())
            : controller.getAllAppointments();
        LocalDateTime now = LocalDateTime.now();
        currentList.sort((first, second) -> Long.compare(
                distanceFromNow(first, now), distanceFromNow(second, now)));
        applyFilter();
    }

    private long distanceFromNow(Appointment appointment, LocalDateTime now) {
        LocalDateTime appointmentDateTime = LocalDateTime.of(
                appointment.getAppointmentDate(), appointment.getAppointmentTime());
        return Math.abs(Duration.between(now, appointmentDateTime).toSeconds());
    }

    private void applyFilter() {
        if (currentList == null) return;
        tableModel.setRowCount(0);
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilter == null ? "All statuses" : (String) statusFilter.getSelectedItem();
        for (Appointment a : currentList) {
            boolean matchesText = query.isEmpty() || a.getAppointmentNumber().toLowerCase().contains(query);
            boolean matchesStatus = "All statuses".equals(selectedStatus) || selectedStatus.equals(a.getStatus());
            if (!matchesText || !matchesStatus) continue;
            tableModel.addRow(new Object[]{
                a.getId(), a.getAppointmentNumber(), a.getAppointmentDate(), a.getAppointmentTime(),
                a.getPatient().getName(), a.getDentist().getFullName(), a.getTreatment().getName(),
                a.getStatus(), a.getNotes() == null ? "" : a.getNotes()
            });
        }
    }

    private void showDetails(Appointment appointment) {
        String details = "Appointment ID: " + appointment.getId()
            + "\nAppointment Number: " + appointment.getAppointmentNumber()
            + "\nDate: " + appointment.getAppointmentDate()
            + "\nTime: " + appointment.getAppointmentTime()
            + "\nStatus: " + appointment.getStatus()
            + "\nDentist: " + appointment.getDentist().getFullName()
            + "\nTreatment: " + appointment.getTreatment().getName()
            + "\nNotes: " + (appointment.getNotes() == null ? "" : appointment.getNotes())
            + "\n\nPatient ID: " + appointment.getPatient().getId()
            + "\nPatient Name: " + appointment.getPatient().getName()
            + "\nContact Number: " + appointment.getPatient().getContactNumber()
            + "\nDate of Birth: " + appointment.getPatient().getDateOfBirth()
            + "\nAddress: " + appointment.getPatient().getAddress();
        JOptionPane.showMessageDialog(this, details, "Appointment details", JOptionPane.INFORMATION_MESSAGE);
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a scheduled appointment first.");
            return;
        }
        Appointment appointment = currentList.stream()
                .filter(a -> a.getId() == ((Number) tableModel.getValueAt(row, 0)).intValue())
                .findFirst().orElse(null);
        if (appointment == null || !"scheduled".equals(appointment.getStatus())) {
            JOptionPane.showMessageDialog(this, "Only scheduled appointments can be edited.");
            return;
        }
        JDateChooser date = new JDateChooser();
        date.setDate(java.sql.Date.valueOf(appointment.getAppointmentDate()));
        date.setDateFormatString("yyyy-MM-dd");
        JTextField time = new JTextField(appointment.getAppointmentTime().toString());
        JTextArea notes = new JTextArea(appointment.getNotes() == null ? "" : appointment.getNotes(), 3, 20);
        Object[] dialogFields = {"Date (YYYY-MM-DD):", date, "Time (HH:MM):", time, "Notes:", new JScrollPane(notes)};
        if (JOptionPane.showConfirmDialog(this, dialogFields, "Edit appointment", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            if (date.getDate() == null) throw new IllegalArgumentException();
            appointment.setAppointmentDate(date.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            appointment.setAppointmentTime(java.time.LocalTime.parse(time.getText().trim()));
            appointment.setNotes(notes.getText().trim());
            if (controller.hasAppointmentConflict(appointment)) {
                throw new IllegalStateException("This time slot is already booked.");
            }
            if (!controller.updateAppointment(appointment)) throw new IllegalStateException();
            loadData();
        } catch (Exception ex) {
            String errorMessage = "This time slot is already booked. Choose another date or time.";
            if (!(ex instanceof IllegalStateException) || !"This time slot is already booked.".equals(ex.getMessage())) {
                errorMessage = "Enter a valid future date and time.";
            }
            JOptionPane.showMessageDialog(this, errorMessage,
                    "Update failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
package com.sunrisedental.view.appointment;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
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
        searchField.setToolTipText("Search appointment number or patient");
        statusFilter = new JComboBox<>(new String[]{"All statuses", "scheduled", "completed", "cancelled"});
        
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Selected Appointment");
        JButton editBtn = new JButton("Edit Selected");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        
        topPanel.add(refreshBtn);
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);
        topPanel.add(statusFilter);
        topPanel.add(editBtn);
        topPanel.add(cancelBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Appt No", "Date", "Time", "Patient", "Dentist", "Treatment", "Status"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadData();

        refreshBtn.addActionListener(e -> loadData());
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
        applyFilter();
    }

    private void applyFilter() {
        if (currentList == null) return;
        tableModel.setRowCount(0);
        String query = searchField == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedStatus = statusFilter == null ? "All statuses" : (String) statusFilter.getSelectedItem();
        for (Appointment a : currentList) {
            boolean matchesText = query.isEmpty()
                    || a.getAppointmentNumber().toLowerCase().contains(query)
                    || (a.getPatient() != null && a.getPatient().getName().toLowerCase().contains(query));
            boolean matchesStatus = "All statuses".equals(selectedStatus) || selectedStatus.equals(a.getStatus());
            if (!matchesText || !matchesStatus) continue;
            tableModel.addRow(new Object[]{
                a.getId(), a.getAppointmentNumber(), a.getAppointmentDate(), a.getAppointmentTime(),
                a.getPatient().getName(), a.getDentist().getFullName(), a.getTreatment().getName(), a.getStatus()
            });
        }
    }

    private void showDetails(Appointment appointment) {
        String details = "Number: " + appointment.getAppointmentNumber()
                + "\nPatient: " + appointment.getPatient().getName()
                + "\nDentist: " + appointment.getDentist().getFullName()
                + "\nTreatment: " + appointment.getTreatment().getName()
                + "\nDate: " + appointment.getAppointmentDate()
                + "\nTime: " + appointment.getAppointmentTime()
                + "\nStatus: " + appointment.getStatus()
                + "\nNotes: " + (appointment.getNotes() == null ? "" : appointment.getNotes());
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
        JTextField date = new JTextField(appointment.getAppointmentDate().toString());
        JTextField time = new JTextField(appointment.getAppointmentTime().toString());
        JTextArea notes = new JTextArea(appointment.getNotes() == null ? "" : appointment.getNotes(), 3, 20);
        Object[] message = {"Date (YYYY-MM-DD):", date, "Time (HH:MM):", time, "Notes:", new JScrollPane(notes)};
        if (JOptionPane.showConfirmDialog(this, message, "Edit appointment", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            appointment.setAppointmentDate(java.time.LocalDate.parse(date.getText().trim()));
            appointment.setAppointmentTime(java.time.LocalTime.parse(time.getText().trim()));
            appointment.setNotes(notes.getText().trim());
            if (!controller.updateAppointment(appointment)) throw new IllegalStateException();
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid future date and time. The slot may already be booked.",
                    "Update failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
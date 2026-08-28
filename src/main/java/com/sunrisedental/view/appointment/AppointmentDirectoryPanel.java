package com.sunrisedental.view.appointment;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.dao.DentistDAOImpl;
import com.sunrisedental.dao.PatientDAOImpl;
import com.sunrisedental.dao.TreatmentDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Dentist;
import com.sunrisedental.model.Patient;
import com.sunrisedental.model.Treatment;
import com.sunrisedental.util.SessionManager;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class AppointmentDirectoryPanel extends JPanel {
    private AppointmentController controller = new AppointmentController();
    private DefaultTableModel tableModel;
    private JTable table;
    private List<Appointment> currentList;
    private final boolean dentistOnly;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private final Consumer<Appointment> billAction;

    public AppointmentDirectoryPanel() {
        this(false, null);
    }

    public AppointmentDirectoryPanel(boolean dentistOnly) {
        this(dentistOnly, null);
    }

    public AppointmentDirectoryPanel(boolean dentistOnly, Consumer<Appointment> billAction) {
        this.dentistOnly = dentistOnly;
        this.billAction = billAction;
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Search Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Appointments"));
        searchField = new JTextField(14);
        searchField.setToolTipText("Search by appointment number");
        statusFilter = new JComboBox<>(new String[]{"All statuses", "pending", "confirmed", "cancelled", "completed"});
        
        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Selected Appointment");
        cancelBtn.setBackground(Color.RED);
        cancelBtn.setForeground(Color.WHITE);
        
        topPanel.add(refreshBtn);
        topPanel.add(new JLabel("Search Appointment Number:"));
        topPanel.add(searchField);
        topPanel.add(statusFilter);
        topPanel.add(cancelBtn);
        cancelBtn.setVisible(!dentistOnly);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Appt No", "Date", "Time", "Patient", "Dentist", "Treatment", "Status", "Notes", "Action"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                    boolean focused, int row, int column) {
                Component component = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
                if (!selected && column == 7) {
                    component.setForeground("cancelled".equals(value) ? Color.RED
                            : "confirmed".equals(value) ? new Color(0, 128, 0) : Color.BLACK);
                }
                return component;
            }
        });
        table.getColumnModel().getColumn(9).setPreferredWidth(130);
        table.getColumnModel().getColumn(9).setCellRenderer((tableComponent, value, isSelected, hasFocus, row, column) -> {
            JButton button = new JButton(String.valueOf(value));
            button.setFocusPainted(false);
            button.setEnabled(value != null && !String.valueOf(value).isBlank());
            return button;
        });
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
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    int id = ((Number) tableModel.getValueAt(table.getSelectedRow(), 0)).intValue();
                    currentList.stream().filter(a -> a.getId() == id).findFirst().ifPresent(AppointmentDirectoryPanel.this::showDetails);
                }
                if (e.getClickCount() == 1 && table.columnAtPoint(e.getPoint()) == 9) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row < 0) return;
                    String action = String.valueOf(tableModel.getValueAt(row, 9));
                    int id = ((Number) tableModel.getValueAt(row, 0)).intValue();
                    Appointment appointment = currentList.stream().filter(a -> a.getId() == id).findFirst().orElse(null);
                    if (appointment == null) return;
                    if ("Edit".equals(action)) {
                        editAppointment(appointment);
                    } else if (billAction != null && "Create Bill".equals(action)) {
                        billAction.accept(appointment);
                    }
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
            
            if (!status.equals("pending")) {
                JOptionPane.showMessageDialog(this, "Only pending appointments can be cancelled.");
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
                a.getStatus(), a.getNotes() == null ? "" : a.getNotes(),
                "pending".equals(a.getStatus()) ? "Edit" :
                    ("confirmed".equals(a.getStatus()) && billAction != null ? "Create Bill" :
                    ("completed".equals(a.getStatus()) ? "Completed" :
                    ("cancelled".equals(a.getStatus()) ? "Cancelled" : "")))
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

    private void editAppointment(Appointment appointment) {
        JComboBox<Patient> patient = new JComboBox<>(new PatientDAOImpl().findAll().toArray(new Patient[0]));
        JComboBox<Dentist> dentist = new JComboBox<>(new DentistDAOImpl().findAll().toArray(new Dentist[0]));
        JComboBox<Treatment> treatment = new JComboBox<>(new TreatmentDAOImpl().findAll().toArray(new Treatment[0]));
        patient.setSelectedItem(findById(patient, appointment.getPatient().getId()));
        dentist.setSelectedItem(findById(dentist, appointment.getDentist().getId()));
        treatment.setSelectedItem(findById(treatment, appointment.getTreatment().getId()));

        JDateChooser date = new JDateChooser(java.sql.Date.valueOf(appointment.getAppointmentDate()));
        date.setDateFormatString("yyyy-MM-dd");
        String[] times = {"08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
                "12:00", "12:30", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"};
        JComboBox<String> time = new JComboBox<>(times);
        time.setSelectedItem(appointment.getAppointmentTime().toString());
        JTextArea notes = new JTextArea(appointment.getNotes() == null ? "" : appointment.getNotes(), 3, 20);
        Object[] fields = {"Select Patient:", patient, "Select Dentist:", dentist, "Select Treatment:", treatment,
                "Appointment Date:", date, "Time Slot:", time, "Treatment Notes:", new JScrollPane(notes)};
        if (JOptionPane.showConfirmDialog(this, fields, "Edit Pending Appointment", JOptionPane.OK_CANCEL_OPTION)
                != JOptionPane.OK_OPTION) return;

        try {
            appointment.setPatient((Patient) patient.getSelectedItem());
            appointment.setDentist((Dentist) dentist.getSelectedItem());
            appointment.setTreatment((Treatment) treatment.getSelectedItem());
            appointment.setAppointmentDate(date.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate());
            appointment.setAppointmentTime(java.time.LocalTime.parse((String) time.getSelectedItem()));
            appointment.setNotes(notes.getText().trim());
            if (controller.hasAppointmentConflict(appointment) || !controller.updateAppointment(appointment)) {
                throw new IllegalStateException();
            }
            loadData();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter valid appointment details. The time slot may already be booked.",
                    "Update failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private <T> T findById(JComboBox<T> combo, int id) {
        for (int index = 0; index < combo.getItemCount(); index++) {
            T item = combo.getItemAt(index);
            if (item instanceof Patient && ((Patient) item).getId() == id
                    || item instanceof Dentist && ((Dentist) item).getId() == id
                    || item instanceof Treatment && ((Treatment) item).getId() == id) return item;
        }
        return null;
    }
}
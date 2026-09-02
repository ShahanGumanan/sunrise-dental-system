package com.sunrisedental.view.help;

import com.sunrisedental.util.SessionManager;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {
    public HelpPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String role = SessionManager.getRole();
        String roleName = role == null ? "User" : role.substring(0, 1).toUpperCase() + role.substring(1);
        JLabel title = new JLabel(roleName + " User Manual");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        add(title, BorderLayout.NORTH);

        JTextArea manual = new JTextArea(getManual(role));
        manual.setEditable(false);
        manual.setLineWrap(true);
        manual.setWrapStyleWord(true);
        manual.setFont(new Font("SansSerif", Font.PLAIN, 14));
        manual.setMargin(new Insets(16, 16, 16, 16));
        manual.setBackground(new Color(248, 250, 252));
        add(new JScrollPane(manual), BorderLayout.CENTER);
    }

    private String getManual(String role) {
        if ("admin".equalsIgnoreCase(role)) {
            return "ADMIN USER MANUAL\n\n"
                    + "Dashboard\n"
                    + "Use Dashboard for a quick overview of clinic activity.\n\n"
                    + "Staff Management\n"
                    + "Add receptionist, dentist, or admin accounts. Use Activate or Deactivate in the Action column to control access. Your own account cannot be deactivated. Inactive users cannot log in.\n\n"
                    + "Manage Treatments\n"
                    + "Add a treatment with its name, fees, description, and duration. Duration is selected in 15-minute increments and is used to calculate appointment end times and prevent overlapping dentist bookings.\n\n"
                    + "Patients\n"
                    + "Register patients with their full name, contact number, date of birth, and address. The patient directory provides a View Full Details action.\n\n"
                    + "Appointments\n"
                    + "Select a patient, dentist, treatment, future date, and time slot. New appointments are Pending. Pending appointments can be edited or cancelled. Refresh reloads the latest data. Use the status filter to review Pending, Confirmed, Cancelled, or Completed appointments.\n\n"
                    + "Approval and Billing\n"
                    + "A dentist confirms or cancels pending appointments. Only Confirmed appointments show Create Bill. Select the bill type, review the calculated fees, and generate the bill. Existing bills can be reopened as saved e-bills.\n\n"
                    + "Reports\n"
                    + "Use Revenue Report to review recorded bills by date range.\n\n"
                    + "Access and Safety\n"
                    + "Log out when finished. Appointment conflicts are checked using the full treatment duration, not only the start time.";
        }
        if ("receptionist".equalsIgnoreCase(role)) {
            return "RECEPTIONIST USER MANUAL\n\n"
                    + "Register Patients\n"
                    + "Open Patients, choose Register Patient, and enter the patient name, valid 10-digit contact number, date of birth, and address. Use View Full Details in the directory to review the complete record.\n\n"
                    + "Book an Appointment\n"
                    + "Open Book Appointment. Select a patient, dentist, treatment, future date, and time. The treatment duration and calculated end time are shown automatically. A past time on today’s date is not allowed.\n\n"
                    + "Appointment Status\n"
                    + "New appointments are Pending. The dentist decides whether to Confirm or Cancel them. Refresh the Appointment Directory to see the latest status. Use the status filter to review Pending, Confirmed, Cancelled, or Completed appointments.\n\n"
                    + "Edit or Cancel\n"
                    + "Pending appointments have an Edit action in the table. Editing uses the same patient, dentist, treatment, date, time, and notes fields as booking. Pending appointments may also be cancelled. Confirmed appointments cannot be edited.\n\n"
                    + "Create a Bill\n"
                    + "Only Confirmed appointments show Create Bill. Click it to open Billing with the appointment number, patient, dentist, treatment, description, duration, and fees already loaded. Choose Standard, Emergency, or Child, review the total, and generate the bill.\n\n"
                    + "Saved E-bills\n"
                    + "After a bill is generated, it is saved. Search the appointment number again to reopen the saved bill. Use View Saved Bill to display it and Print Receipt to print only the receipt content.";
        }
        return "DENTIST USER MANUAL\n\n"
                + "My Schedule\n"
                + "Use My Schedule to view your appointments. The table shows the appointment date, start and calculated end time, patient, treatment, notes, and current action.\n\n"
                + "Pending Appointments\n"
                + "For each Pending appointment, select Confirm / Cancel in the Action column. Choose Confirm to approve the appointment or Cancel to reject it. The updated result is saved immediately.\n\n"
                + "After a Decision\n"
                + "The Action column changes to Confirmed or Cancelled. The admin and receptionist see the same status in their Appointment Directory.\n\n"
                + "Completion\n"
                + "Confirmed appointments automatically become Completed after the scheduled treatment end time.\n\n"
                + "Restrictions\n"
                + "Dentists cannot book, edit, or manually cancel appointment details. They only decide the outcome of Pending appointments in My Schedule. Treatment duration is used to show the complete time range.";
    }
}
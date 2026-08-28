package com.sunrisedental.view;

import com.sunrisedental.util.SessionManager;
import com.sunrisedental.view.appointment.AppointmentFormPanel;
import com.sunrisedental.view.billing.BillFormPanel;
import com.sunrisedental.view.patient.PatientFormPanel;
import com.sunrisedental.view.patient.PatientListPanel;
import com.sunrisedental.view.admin.UserManagementPanel;
import com.sunrisedental.view.admin.TreatmentManagementPanel;
import com.sunrisedental.view.dentist.DentistSchedulePanel;
import com.sunrisedental.view.report.DailyReportPanel;
import com.sunrisedental.view.report.RevenueReportPanel;
import com.sunrisedental.view.appointment.AppointmentDirectoryPanel;
import com.sunrisedental.view.help.HelpPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel rootPanel;
    private CardLayout cardLayout;

    // App Layout Panels
    private JPanel appPanel;
    private JPanel contentPanel;
    private CardLayout contentCardLayout;
    private JPanel sidebarPanel;

    public MainFrame() {
        setTitle("Sunrise Dental Clinic System");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Root layout handles switching between "Login" and "App"
        cardLayout = new CardLayout();
        rootPanel = new JPanel(cardLayout);

        // 1. Add Login Panel
        rootPanel.add(new LoginPanel(this), "LOGIN");

        add(rootPanel);
    }

    // Called by LoginPanel upon success
    public void loadApplication() {
        buildAppLayout();
        rootPanel.add(appPanel, "APP");
        cardLayout.show(rootPanel, "APP");
    }

    private void buildAppLayout() {
        appPanel = new JPanel(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        JLabel titleLabel = new JLabel("Sunrise Dental System - Logged in as: " + SessionManager.getCurrentUser().getFullName());
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            SessionManager.logout();
            cardLayout.show(rootPanel, "LOGIN"); // Go back to login
            appPanel = null; // Clear memory
        });
        headerPanel.add(logoutBtn, BorderLayout.EAST);

        // Sidebar Navigation
        sidebarPanel = new JPanel(new GridLayout(10, 1, 5, 5));
        sidebarPanel.setPreferredSize(new Dimension(200, 0));
        sidebarPanel.setBackground(new Color(230, 240, 250));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Content Area (For switching between Dashboard, Appointments, etc.)
        contentCardLayout = new CardLayout();
        contentPanel = new JPanel(contentCardLayout);
        contentPanel.add(new DashboardPanel(), "DASHBOARD");
        contentPanel.add(new AppointmentFormPanel(), "NEW_APPOINTMENT");

        JTabbedPane patientTabs = new JTabbedPane();
        patientTabs.addTab("Register Patient", new PatientFormPanel());
        patientTabs.addTab("View All Patients", new PatientListPanel());
        contentPanel.add(patientTabs, "PATIENTS");
        contentPanel.add(new BillFormPanel(), "BILLING");
        contentPanel.add(new UserManagementPanel(), "USERS");
        contentPanel.add(new DentistSchedulePanel(), "SCHEDULE");
        contentPanel.add(new DailyReportPanel(), "DAILY_REPORT");
        contentPanel.add(new RevenueReportPanel(), "REVENUE_REPORT");
        contentPanel.add(new AppointmentDirectoryPanel(), "APPOINTMENT_LIST");
        contentPanel.add(new HelpPanel(), "HELP");
        contentPanel.add(new TreatmentManagementPanel(), "TREATMENTS");

        // Add Sidebar Buttons based on Role
        addNavButton("Dashboard", "DASHBOARD");
        
        if (SessionManager.hasRole("admin") || SessionManager.hasRole("receptionist")) {
            addNavButton("Book Appointment", "NEW_APPOINTMENT");
            addNavButton("Appt Directory", "APPOINTMENT_LIST");
            addNavButton("Patients", "PATIENTS");
            addNavButton("Billing", "BILLING");
            addNavButton("Daily Report", "DAILY_REPORT");
        }

        if (SessionManager.hasRole("admin")) {
            addNavButton("Manage Staff", "USERS");
            addNavButton("Manage Treatments", "TREATMENTS");
            addNavButton("Revenue Report", "REVENUE_REPORT");
        }
        
        if (SessionManager.hasRole("dentist")) {
            addNavButton("My Schedule", "SCHEDULE");
            contentPanel.add(new AppointmentDirectoryPanel(true), "DENTIST_APPOINTMENTS");
            addNavButton("Appointments", "DENTIST_APPOINTMENTS");
        }

        addNavButton("System Help", "HELP");

        appPanel.add(headerPanel, BorderLayout.NORTH);
        appPanel.add(sidebarPanel, BorderLayout.WEST);
        appPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private void addNavButton(String title, String cardName) {
        JButton btn = new JButton(title);
        btn.setFocusPainted(false);
        btn.addActionListener(e -> contentCardLayout.show(contentPanel, cardName));
        sidebarPanel.add(btn);
    }
}
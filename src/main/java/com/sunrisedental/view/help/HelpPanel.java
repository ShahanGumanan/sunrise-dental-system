package com.sunrisedental.view.help;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {
    public HelpPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("System Help & User Guide");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        add(title, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();

        JTextArea adminHelp = new JTextArea("Admin Guide:\n1. Use 'Manage Staff' to add new Dentists/Receptionists.\n2. Revenue report shows total income.\n3. You have full access to book and cancel appointments.");
        adminHelp.setEditable(false);
        tabs.addTab("Admin Help", new JScrollPane(adminHelp));

        JTextArea recHelp = new JTextArea("Receptionist Guide:\n1. Register patients first before booking.\n2. Use the Billing tab to generate invoices.\n3. If a patient calls to cancel, use the Appointment Directory.");
        recHelp.setEditable(false);
        tabs.addTab("Receptionist Help", new JScrollPane(recHelp));

        JTextArea dentHelp = new JTextArea("Dentist Guide:\n1. Check 'My Schedule' daily.\n2. You cannot book or cancel appointments. Ask the receptionist.\n3. The schedule auto-updates.");
        dentHelp.setEditable(false);
        tabs.addTab("Dentist Help", new JScrollPane(dentHelp));

        add(tabs, BorderLayout.CENTER);
    }
}
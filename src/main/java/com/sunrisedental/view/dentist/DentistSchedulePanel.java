package com.sunrisedental.view.dentist;

import com.sunrisedental.util.SessionManager;
import javax.swing.*;
import java.awt.*;

public class DentistSchedulePanel extends JPanel {
    public DentistSchedulePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("My Daily Schedule - Dr. " + SessionManager.getCurrentUser().getFullName());
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 102, 204));
        add(title, BorderLayout.NORTH);

        // Dummy table for now until we link the Appointment List query
        String[] cols = {"Time", "Patient", "Treatment", "Status"};
        Object[][] data = {
            {"09:00", "John Doe", "Checkup", "Scheduled"},
            {"10:00", "Jane Smith", "Root Canal", "Completed"}
        };
        JTable table = new JTable(data, cols);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}
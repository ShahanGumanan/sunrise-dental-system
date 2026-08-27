package com.sunrisedental.view;

import com.sunrisedental.util.SessionManager;
import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Welcome to Sunrise Dental Clinic", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        
        JLabel userLabel = new JLabel("Logged in as: " + SessionManager.getCurrentUser().getFullName() 
                                      + " (" + SessionManager.getRole() + ")", SwingConstants.CENTER);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 16));

        add(welcomeLabel, BorderLayout.CENTER);
        add(userLabel, BorderLayout.SOUTH);
    }
}
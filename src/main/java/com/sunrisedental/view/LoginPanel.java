package com.sunrisedental.view;

import com.sunrisedental.controller.AuthController;
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private AuthController authController;
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.authController = new AuthController();

        setLayout(new GridBagLayout());
        setBackground(new Color(240, 248, 255)); // Light blue clinic theme

        JPanel formPanel = new JPanel(new GridLayout(4, 1, 10, 10));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel titleLabel = new JLabel("Sunrise Dental Login", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 102, 204));

        JTextField usernameField = new JTextField(15);
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(new Color(0, 102, 204));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 14));

        loginBtn.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());

            if (authController.login(username, password)) {
                JOptionPane.showMessageDialog(this, "Login Successful!");
                mainFrame.loadApplication(); // Switch to main app
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Username or Password", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        formPanel.add(titleLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordField);
        formPanel.add(loginBtn);

        add(formPanel);
    }
}
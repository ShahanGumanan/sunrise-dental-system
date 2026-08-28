package com.sunrisedental.view.admin;

import com.sunrisedental.dao.DentistDAO;
import com.sunrisedental.dao.DentistDAOImpl;
import com.sunrisedental.dao.UserDAOImpl;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private UserDAOImpl userDAO;
    private DentistDAO dentistDAO;
    private JTable table;
    private DefaultTableModel tableModel;

    public UserManagementPanel() {
        userDAO = new UserDAOImpl();
        dentistDAO = new DentistDAOImpl();
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        JLabel title = new JLabel("Staff Management (Admin Only)");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        topPanel.add(title, BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add New Staff");
        addBtn.setBackground(new Color(0, 102, 204));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> showAddUserDialog());
        topPanel.add(addBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Username", "Full Name", "Role", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.findAll();
        for (User u : users) {
            if ("dentist".equalsIgnoreCase(u.getRole()) && u.isActive()) {
                dentistDAO.ensureProfileForUser(u.getId());
            }
            tableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole(), u.isActive() ? "Active" : "Inactive"});
        }
    }

    private void showAddUserDialog() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField fullNameField = new JTextField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"receptionist", "dentist", "admin"});

        Object[] message = {
            "Full Name:", fullNameField,
            "Username:", usernameField,
            "Password:", passwordField,
            "Role:", roleCombo
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Staff", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            if (usernameField.getText().trim().isEmpty() || fullNameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty!");
                return;
            }

            User u = new User();
            u.setFullName(fullNameField.getText());
            u.setUsername(usernameField.getText());
            u.setPasswordHash(PasswordUtil.hash(new String(passwordField.getPassword())));
            String role = roleCombo.getSelectedItem().toString();
            u.setRole(role);
            
            if (userDAO.create(u)) {
                if (role.equals("dentist") && !dentistDAO.ensureProfileForUser(u.getId())) {
                    JOptionPane.showMessageDialog(this, "User was created, but the dentist profile could not be linked.",
                            "Dentist profile warning", JOptionPane.WARNING_MESSAGE);
                }
                JOptionPane.showMessageDialog(this, "Staff added successfully!");
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add staff. Username might exist.");
            }
        }
    }
}
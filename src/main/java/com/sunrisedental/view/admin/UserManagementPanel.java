package com.sunrisedental.view.admin;

import com.sunrisedental.model.User;
import com.sunrisedental.util.AdminApiClient;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementPanel extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;

    public UserManagementPanel() {
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
        String[] columns = {"ID", "Username", "Full Name", "Role", "Status", "Action"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);
        table.getColumnModel().getColumn(5).setCellRenderer((tableComponent, value, selected, focused, row, column) -> {
            JButton button = new JButton(String.valueOf(value));
            button.setFocusPainted(false);
            button.setEnabled(!"--".equals(value));
            return button;
        });
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                int row = table.rowAtPoint(event.getPoint());
                if (row < 0 || table.columnAtPoint(event.getPoint()) != 5) return;
                int userId = ((Number) tableModel.getValueAt(row, 0)).intValue();
                if (userId == SessionManager.getCurrentUser().getId()) {
                    JOptionPane.showMessageDialog(UserManagementPanel.this, "You cannot deactivate your own account.",
                            "Status change not allowed", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                boolean activate = "Activate".equals(tableModel.getValueAt(row, 5));
                if (AdminApiClient.updateUserStatus(userId, activate, SessionManager.getCurrentUser().getId())) loadUsers();
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadUsers();
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        List<User> users = AdminApiClient.users();
        for (User u : users) {
                tableModel.addRow(new Object[]{u.getId(), u.getUsername(), u.getFullName(), u.getRole(),
                    u.isActive() ? "Active" : "Inactive",
                    u.getId() == SessionManager.getCurrentUser().getId() ? "--" : (u.isActive() ? "Deactivate" : "Activate")});
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
            
            if (AdminApiClient.createUser(u)) {
                JOptionPane.showMessageDialog(this, "Staff added successfully!");
                loadUsers();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add staff. Username might exist.");
            }
        }
    }
}
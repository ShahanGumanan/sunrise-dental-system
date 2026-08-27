package com.sunrisedental;

import com.sunrisedental.view.MainFrame;
import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.UserDAOImpl;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAOImpl();
        User admin = userDAO.findByUsername("admin");
        if (admin != null) {
            String realHash = PasswordUtil.hash("admin123");
            String sql = "UPDATE users SET password_hash = '" + realHash + "' WHERE username = 'admin'";
            try {
                com.sunrisedental.db.DatabaseConnection.getInstance().getConnection().createStatement().executeUpdate(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Launch the App
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
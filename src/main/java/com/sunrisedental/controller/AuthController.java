package com.sunrisedental.controller;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.UserDAOImpl;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.SessionManager;

public class AuthController {
    private UserDAO userDAO;

    public AuthController() {
        this.userDAO = new UserDAOImpl();
    }

    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean login(String username, String password) {
        User user = userDAO.findByUsername(username);
        
        // Check if user exists and is active
        if (user == null || !user.isActive()) {
            return false;
        }

        // Verify password
        if (PasswordUtil.verify(password, user.getPasswordHash())) {
            SessionManager.setCurrentUser(user); // Set global session
            return true;
        }
        
        return false;
    }

    public void logout() {
        SessionManager.logout();
    }
}
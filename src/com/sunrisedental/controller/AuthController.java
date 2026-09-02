package com.sunrisedental.controller;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.ApiClient;
import com.sunrisedental.util.JsonUtil;
import com.sunrisedental.util.SessionManager;
import java.util.Map;

public class AuthController {
    private UserDAO userDAO;
    private final boolean remote;

    public AuthController() {
        this.userDAO = null;
        this.remote = true;
    }

    public AuthController(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.remote = false;
    }

    public boolean login(String username, String password) {
        if (remote) {
            if (username == null || password == null || username.isBlank() || password.isBlank()) return false;
            try {
                User user = JsonUtil.fromJson(ApiClient.post("/auth/login", Map.of(
                        "username", username, "password", password)), User.class);
                if (user != null && user.isActive()) {
                    SessionManager.setCurrentUser(user);
                    return true;
                }
            } catch (Exception ignored) { }
            return false;
        }
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
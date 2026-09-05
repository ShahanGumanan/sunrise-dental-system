package com.sunrisedental.controller;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.model.User;
import com.sunrisedental.util.PasswordUtil;
import com.sunrisedental.util.ApiClient;
import com.sunrisedental.util.JsonUtil;
import com.sunrisedental.util.SessionManager;
import java.util.Map;

public class AuthController {
    public enum LoginResult {
        SUCCESS,
        INVALID_CREDENTIALS,
        ACCOUNT_DEACTIVATED
    }

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
        return authenticate(username, password) == LoginResult.SUCCESS;
    }

    public LoginResult authenticate(String username, String password) {
        if (remote) {
            if (username == null || password == null || username.isBlank() || password.isBlank()) {
                return LoginResult.INVALID_CREDENTIALS;
            }
            try {
                User user = JsonUtil.fromJson(ApiClient.post("/auth/login", Map.of(
                        "username", username, "password", password)), User.class);
                if (user != null && user.isActive()) {
                    SessionManager.setCurrentUser(user);
                    return LoginResult.SUCCESS;
                }
            } catch (Exception error) {
                if (error.getMessage() != null && error.getMessage().contains("Account deactivated")) {
                    return LoginResult.ACCOUNT_DEACTIVATED;
                }
            }
            return LoginResult.INVALID_CREDENTIALS;
        }
        User user = userDAO.findByUsername(username);
        
        // Check if user exists and is active
        if (user == null) {
            return LoginResult.INVALID_CREDENTIALS;
        }
        if (!user.isActive()) {
            return LoginResult.ACCOUNT_DEACTIVATED;
        }

        // Verify password
        if (PasswordUtil.verify(password, user.getPasswordHash())) {
            SessionManager.setCurrentUser(user); // Set global session
            return LoginResult.SUCCESS;
        }
        
        return LoginResult.INVALID_CREDENTIALS;
    }

    public void logout() {
        SessionManager.logout();
    }
}
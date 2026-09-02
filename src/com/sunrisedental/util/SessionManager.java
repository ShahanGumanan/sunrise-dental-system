package com.sunrisedental.util;

import com.sunrisedental.model.User;

public class SessionManager {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equalsIgnoreCase(role);
    }

    public static void logout() {
        currentUser = null;
    }
}
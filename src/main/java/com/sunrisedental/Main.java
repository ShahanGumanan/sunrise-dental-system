package com.sunrisedental;

import com.sunrisedental.dao.UserDAO;
import com.sunrisedental.dao.UserDAOImpl;
import com.sunrisedental.model.User;

public class Main {
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...");
        
        UserDAO userDAO = new UserDAOImpl();
        User admin = userDAO.findByUsername("admin");
        
        if (admin != null) {
            System.out.println("SUCCESS! Found user: " + admin.getFullName() + " (Role: " + admin.getRole() + ")");
        } else {
            System.out.println("FAILED! Could not find user. Check database connection.");
        }
    }
}
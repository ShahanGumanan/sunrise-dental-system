package com.sunrisedental.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private Properties properties;

    private DatabaseConnection() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                throw new RuntimeException("Sorry, unable to find db.properties");
            }
            prop.load(input);
            this.properties = prop;

            Class.forName(prop.getProperty("db.driver"));
            openConnection();
            System.out.println("Database connected successfully!");
        } catch (Exception e) {
            throw new IllegalStateException("Database connection failed. Check db.properties and ensure MySQL is running.", e);
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed() || !connection.isValid(2)) {
                openConnection();
            }
            return connection;
        } catch (Exception e) {
            throw new IllegalStateException("Database connection is unavailable. Check that MySQL is running.", e);
        }
    }

    private void openConnection() throws java.sql.SQLException {
        connection = DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }
}
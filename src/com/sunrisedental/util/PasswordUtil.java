package com.sunrisedental.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    // Hash a plain text password
    public static String hash(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Verify a plain text password against a hash
    public static boolean verify(String password, String hashed) {
        try {
            return BCrypt.checkpw(password, hashed);
        } catch (Exception e) {
            return false; // Returns false if the hash format in DB is invalid
        }
    }
}
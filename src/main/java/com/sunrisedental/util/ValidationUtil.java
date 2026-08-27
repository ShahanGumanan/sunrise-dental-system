package com.sunrisedental.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ValidationUtil {
    
    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidContact(String contact) {
        // Must be exactly 10 digits and start with 0 (e.g., 0771234567)
        return contact != null && contact.matches("^0\\d{9}$");
    }

    public static boolean isValidFutureDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr); // Expects YYYY-MM-DD
            return !date.isBefore(LocalDate.now()); // Cannot be in the past
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
package com.sunrisedental;

import com.sunrisedental.pattern.factory.BillFactory;
import com.sunrisedental.pattern.strategy.ChildFeeCalculator;
import com.sunrisedental.pattern.strategy.EmergencyFeeCalculator;
import com.sunrisedental.pattern.strategy.FeeCalculator;
import com.sunrisedental.pattern.strategy.StandardFeeCalculator;
import com.sunrisedental.util.ValidationUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    @Test
    void validatesContactsAndDates() {
        assertTrue(ValidationUtil.isValidContact("0771234567"));
        assertFalse(ValidationUtil.isValidContact("1771234567"));
        assertFalse(ValidationUtil.isValidContact("077123"));
        assertFalse(ValidationUtil.isValidFutureDate(LocalDate.now().minusDays(1).toString()));
        assertTrue(ValidationUtil.isValidFutureDate(LocalDate.now().toString()));
    }

    @Test
    void storesTreatmentDescriptionAndDuration() {
        com.sunrisedental.model.Treatment treatment = new com.sunrisedental.model.Treatment();
        treatment.setDescription("Routine cleaning");
        treatment.setDurationMinutes(45);

        assertEquals("Routine cleaning", treatment.getDescription());
        assertEquals(45, treatment.getDurationMinutes());
    }

    @Test
    void calculatesFeesUsingEachStrategy() {
        FeeCalculator standard = new StandardFeeCalculator();
        FeeCalculator emergency = new EmergencyFeeCalculator();
        FeeCalculator child = new ChildFeeCalculator();

        assertEquals(1000.0, standard.calculateTreatmentFee(1000.0), 0.01);
        assertEquals(1500.0, emergency.calculateTreatmentFee(1000.0), 0.01);
        assertEquals(800.0, child.calculateTreatmentFee(1000.0), 0.01);
    }

    @Test
    void factorySelectsTheRequestedStrategy() {
        assertInstanceOf(EmergencyFeeCalculator.class, BillFactory.getCalculator("emergency"));
        assertInstanceOf(ChildFeeCalculator.class, BillFactory.getCalculator("child"));
        assertInstanceOf(StandardFeeCalculator.class, BillFactory.getCalculator("unknown"));
    }

    @Test
    void rejectsEmptyAndMalformedContacts() {
        assertFalse(ValidationUtil.isValidContact(null));
        assertFalse(ValidationUtil.isValidContact(""));
        assertFalse(ValidationUtil.isValidContact("077123456a"));
        assertFalse(ValidationUtil.isValidContact("07712345678"));
        assertFalse(ValidationUtil.isValidContact(" 0771234567"));
    }

    @Test
    void handlesEmptyAndMalformedDates() {
        assertFalse(ValidationUtil.isValidFutureDate(null));
        assertFalse(ValidationUtil.isValidFutureDate(""));
        assertFalse(ValidationUtil.isValidFutureDate("2026/08/28"));
        assertFalse(ValidationUtil.isValidFutureDate("not-a-date"));
        assertTrue(ValidationUtil.isValidFutureDate(LocalDate.now().plusDays(1).toString()));
    }
}

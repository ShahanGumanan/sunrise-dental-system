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
}

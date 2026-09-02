package com.sunrisedental.pattern.strategy;

public interface FeeCalculator {
    double calculateTreatmentFee(double baseFee);
    double getDiscountPercent();
    String getBillType();
}
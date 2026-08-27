package com.sunrisedental.pattern.strategy;

public class StandardFeeCalculator implements FeeCalculator {
    @Override
    public double calculateTreatmentFee(double baseFee) { return baseFee; } // Normal price
    @Override
    public double getDiscountPercent() { return 0.0; }
    @Override
    public String getBillType() { return "standard"; }
}
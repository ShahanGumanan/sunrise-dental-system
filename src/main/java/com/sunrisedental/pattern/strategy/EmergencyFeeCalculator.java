package com.sunrisedental.pattern.strategy;

public class EmergencyFeeCalculator implements FeeCalculator {
    @Override
    public double calculateTreatmentFee(double baseFee) { return baseFee * 1.5; } // 50% extra
    @Override
    public double getDiscountPercent() { return 0.0; }
    @Override
    public String getBillType() { return "emergency"; }
}
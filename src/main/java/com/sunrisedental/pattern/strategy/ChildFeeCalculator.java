package com.sunrisedental.pattern.strategy;

public class ChildFeeCalculator implements FeeCalculator {
    @Override
    public double calculateTreatmentFee(double baseFee) { return baseFee * 0.8; } // 20% discount
    @Override
    public double getDiscountPercent() { return 20.0; }
    @Override
    public String getBillType() { return "child"; }
}
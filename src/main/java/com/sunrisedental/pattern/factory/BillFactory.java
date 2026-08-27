package com.sunrisedental.pattern.factory;

import com.sunrisedental.pattern.strategy.*;

public class BillFactory {
    public static FeeCalculator getCalculator(String type) {
        if (type == null) return new StandardFeeCalculator();
        
        switch (type.toLowerCase()) {
            case "emergency":
                return new EmergencyFeeCalculator();
            case "child":
                return new ChildFeeCalculator();
            default:
                return new StandardFeeCalculator();
        }
    }
}
package com.sunrisedental.controller;

import com.sunrisedental.dao.BillDAO;
import com.sunrisedental.dao.BillDAOImpl;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;
import com.sunrisedental.pattern.factory.BillFactory;
import com.sunrisedental.pattern.strategy.FeeCalculator;
import com.sunrisedental.util.NumberGenerator; 

public class BillController {
    private BillDAO billDAO;

    public BillController() {
        this.billDAO = new BillDAOImpl();
    }

    public BillController(BillDAO billDAO) {
        this.billDAO = billDAO;
    }

    public Bill generateBill(Appointment appt, String billType) {
        if (appt == null || appt.getTreatment() == null || appt.getId() <= 0 || billType == null || billType.isBlank()
                || billDAO.existsForAppointment(appt.getId())) {
            return null;
        }
        // Use Factory to get the correct Strategy
        FeeCalculator calculator = BillFactory.getCalculator(billType);
        
        double consultationFee = appt.getTreatment().getConsultationFee();
        double originalTreatmentFee = appt.getTreatment().getBaseFee();
        
        // Execute Strategy
        double finalTreatmentFee = calculator.calculateTreatmentFee(originalTreatmentFee);
        double discountAmount = originalTreatmentFee - finalTreatmentFee; 
        if(discountAmount < 0) discountAmount = 0; // Don't show negative discount for emergency
        
        double total = consultationFee + finalTreatmentFee;

        Bill bill = new Bill();
        bill.setAppointment(appt);
        // Quick receipt number generator
        bill.setReceiptNumber(NumberGenerator.generateReceiptNumber());
        bill.setConsultationFee(consultationFee);
        bill.setTreatmentFee(finalTreatmentFee);
        bill.setDiscount(discountAmount);
        bill.setTotal(total);
        bill.setBillType(calculator.getBillType());

        if (billDAO.create(bill)) {
            return bill;
        }
        return null; // Failed to save
    }
}
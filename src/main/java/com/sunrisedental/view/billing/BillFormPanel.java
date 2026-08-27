package com.sunrisedental.view.billing;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.controller.BillController;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;

import javax.swing.*;
import java.awt.*;

public class BillFormPanel extends JPanel {
    private AppointmentController apptController;
    private BillController billController;
    private Appointment currentAppt;

    private JTextField searchField;
    private JLabel pNameLbl, tNameLbl, baseFeeLbl, totalLbl;
    private JRadioButton stdRadio, emgRadio, childRadio;

    public BillFormPanel() {
        apptController = new AppointmentController();
        billController = new BillController();
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Top Panel (Search)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Search Appointment No (e.g., APT-2026...): "));
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search");
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        // 2. Middle Panel (Details & Calculation)
        JPanel middlePanel = new JPanel(new GridLayout(6, 2, 10, 10));
        middlePanel.setBackground(Color.WHITE);
        middlePanel.setBorder(BorderFactory.createTitledBorder("Billing Details"));

        pNameLbl = new JLabel("-");
        tNameLbl = new JLabel("-");
        baseFeeLbl = new JLabel("-");
        totalLbl = new JLabel("-");
        totalLbl.setFont(new Font("Arial", Font.BOLD, 16));
        totalLbl.setForeground(Color.RED);

        middlePanel.add(new JLabel("Patient Name:")); middlePanel.add(pNameLbl);
        middlePanel.add(new JLabel("Treatment:")); middlePanel.add(tNameLbl);
        middlePanel.add(new JLabel("Base Treatment Fee:")); middlePanel.add(baseFeeLbl);

        // Radio Buttons for Bill Type
        stdRadio = new JRadioButton("Standard", true);
        emgRadio = new JRadioButton("Emergency (+50%)");
        childRadio = new JRadioButton("Child (-20%)");
        ButtonGroup group = new ButtonGroup();
        group.add(stdRadio); group.add(emgRadio); group.add(childRadio);
        
        JPanel radioPanel = new JPanel();
        radioPanel.setBackground(Color.WHITE);
        radioPanel.add(stdRadio); radioPanel.add(emgRadio); radioPanel.add(childRadio);

        middlePanel.add(new JLabel("Select Bill Type:")); middlePanel.add(radioPanel);
        middlePanel.add(new JLabel("Final Total:")); middlePanel.add(totalLbl);

        add(middlePanel, BorderLayout.CENTER);

        // 3. Bottom Panel (Actions)
        JButton calcBtn = new JButton("Calculate Total");
        JButton generateBtn = new JButton("Generate Bill");
        generateBtn.setBackground(new Color(0, 153, 51));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setEnabled(false); // Disabled until calculated

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(calcBtn);
        bottomPanel.add(generateBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        searchBtn.addActionListener(e -> {
            String apptNo = searchField.getText().trim();
            // Since we didn't add findByApptNo to ApptController yet, we call DAO directly for brevity
            currentAppt = new com.sunrisedental.dao.AppointmentDAOImpl().findByAppointmentNumber(apptNo);
            if (currentAppt != null) {
                pNameLbl.setText(currentAppt.getPatient().getName());
                tNameLbl.setText(currentAppt.getTreatment().getName());
                baseFeeLbl.setText("Rs. " + currentAppt.getTreatment().getBaseFee());
                totalLbl.setText("-");
                generateBtn.setEnabled(false);
            } else {
                JOptionPane.showMessageDialog(this, "Appointment not found!");
            }
        });

        calcBtn.addActionListener(e -> {
            if (currentAppt == null) return;
            String type = stdRadio.isSelected() ? "standard" : (emgRadio.isSelected() ? "emergency" : "child");
            // Dummy calculate just for UI display
            com.sunrisedental.pattern.strategy.FeeCalculator calc = com.sunrisedental.pattern.factory.BillFactory.getCalculator(type);
            double total = calc.calculateTreatmentFee(currentAppt.getTreatment().getBaseFee()) + currentAppt.getTreatment().getConsultationFee();
            totalLbl.setText("Rs. " + total);
            generateBtn.setEnabled(true);
        });

        generateBtn.addActionListener(e -> {
            String type = stdRadio.isSelected() ? "standard" : (emgRadio.isSelected() ? "emergency" : "child");
            Bill bill = billController.generateBill(currentAppt, type);
            if (bill != null) {
                JFrame receiptFrame = new JFrame("Receipt: " + bill.getReceiptNumber());
                receiptFrame.setSize(400, 500);
                receiptFrame.setLocationRelativeTo(this);
                receiptFrame.add(new BillReceiptPanel(bill));
                receiptFrame.setVisible(true);

                generateBtn.setEnabled(false);
            }
        });
    }
}
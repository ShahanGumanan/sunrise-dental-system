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
    private JLabel pNameLbl, dentistLbl, tNameLbl, descriptionLbl, durationLbl, baseFeeLbl, totalLbl;
    private JRadioButton stdRadio, emgRadio, childRadio;
    private JButton generateBtn, viewBillBtn;
    private Bill savedBill;

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
        JPanel middlePanel = new JPanel(new GridLayout(9, 2, 10, 10));
        middlePanel.setBackground(Color.WHITE);
        middlePanel.setBorder(BorderFactory.createTitledBorder("Billing Details"));

        pNameLbl = new JLabel("-");
        dentistLbl = new JLabel("-");
        tNameLbl = new JLabel("-");
        descriptionLbl = new JLabel("-");
        durationLbl = new JLabel("-");
        baseFeeLbl = new JLabel("-");
        totalLbl = new JLabel("-");
        totalLbl.setFont(new Font("Arial", Font.BOLD, 16));
        totalLbl.setForeground(Color.RED);

        middlePanel.add(new JLabel("Patient Name:")); middlePanel.add(pNameLbl);
        middlePanel.add(new JLabel("Dentist:")); middlePanel.add(dentistLbl);
        middlePanel.add(new JLabel("Treatment:")); middlePanel.add(tNameLbl);
        middlePanel.add(new JLabel("Description:")); middlePanel.add(descriptionLbl);
        middlePanel.add(new JLabel("Duration:")); middlePanel.add(durationLbl);
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
        generateBtn = new JButton("Generate Bill");
        viewBillBtn = new JButton("View Saved Bill");
        viewBillBtn.setEnabled(false);
        generateBtn.setBackground(new Color(0, 153, 51));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setEnabled(false); // Disabled until calculated

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(calcBtn);
        bottomPanel.add(generateBtn);
        bottomPanel.add(viewBillBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        searchBtn.addActionListener(e -> {
            String apptNo = searchField.getText().trim();
            // Since we didn't add findByApptNo to ApptController yet, we call DAO directly for brevity
            currentAppt = new com.sunrisedental.dao.AppointmentDAOImpl().findByAppointmentNumber(apptNo);
            if (currentAppt != null) {
                loadAppointment(currentAppt);
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
                showReceipt(bill);
                generateBtn.setEnabled(false);
                savedBill = bill;
                viewBillBtn.setEnabled(true);
            } else {
                Bill existingBill = billController.findByAppointmentId(currentAppt.getId());
                if (existingBill != null) {
                    savedBill = existingBill;
                    viewBillBtn.setEnabled(true);
                    showReceipt(existingBill);
                }
            }
        });

        stdRadio.addActionListener(e -> refreshTotal());
        emgRadio.addActionListener(e -> refreshTotal());
        childRadio.addActionListener(e -> refreshTotal());
        viewBillBtn.addActionListener(e -> {
            if (savedBill != null) showReceipt(savedBill);
        });
    }

    public void loadAppointment(Appointment appointment) {
        currentAppt = appointment;
        savedBill = billController.findByAppointmentId(appointment.getId());
        searchField.setText(appointment.getAppointmentNumber());
        pNameLbl.setText(appointment.getPatient().getName());
        dentistLbl.setText(appointment.getDentist().getFullName());
        tNameLbl.setText(appointment.getTreatment().getName());
        descriptionLbl.setText(appointment.getTreatment().getDescription() == null ? "" : appointment.getTreatment().getDescription());
        durationLbl.setText(appointment.getTreatment().getDurationMinutes() + " minutes");
        baseFeeLbl.setText("Rs. " + appointment.getTreatment().getBaseFee());
        calculateTotal();
        generateBtn.setEnabled("confirmed".equalsIgnoreCase(appointment.getStatus()) && savedBill == null);
        viewBillBtn.setEnabled(savedBill != null);
        }

    private void refreshTotal() {
        calculateTotal();
        generateBtn.setEnabled(currentAppt != null && "confirmed".equalsIgnoreCase(currentAppt.getStatus()));
    }

        private void calculateTotal() {
        if (currentAppt == null) return;
        String type = stdRadio.isSelected() ? "standard" : (emgRadio.isSelected() ? "emergency" : "child");
        com.sunrisedental.pattern.strategy.FeeCalculator calculator =
            com.sunrisedental.pattern.factory.BillFactory.getCalculator(type);
        double total = calculator.calculateTreatmentFee(currentAppt.getTreatment().getBaseFee())
            + currentAppt.getTreatment().getConsultationFee();
        totalLbl.setText("Rs. " + total);
    }

    private void showReceipt(Bill bill) {
        JFrame receiptFrame = new JFrame("Receipt: " + bill.getReceiptNumber());
        receiptFrame.setSize(400, 550);
        receiptFrame.setLocationRelativeTo(this);
        receiptFrame.add(new BillReceiptPanel(bill));
        receiptFrame.setVisible(true);
    }
}
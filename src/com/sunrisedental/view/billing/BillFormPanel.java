package com.sunrisedental.view.billing;

import com.sunrisedental.controller.AppointmentController;
import com.sunrisedental.controller.BillController;
import com.sunrisedental.model.Appointment;
import com.sunrisedental.model.Bill;

import javax.swing.*;
import java.awt.*;
import com.sunrisedental.view.UiTheme;

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
        setBackground(UiTheme.CANVAS);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Top Panel (Search)
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(UiTheme.CANVAS);
        topPanel.add(new JLabel("Search Appointment No (e.g., APT-2026...): "));
        searchField = new JTextField(15);
        UiTheme.styleField(searchField);
        JButton searchBtn = new JButton("Search");
        UiTheme.styleButton(searchBtn);
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        add(topPanel, BorderLayout.NORTH);

        // 2. Middle Panel (Details & Calculation)
        JPanel middlePanel = new JPanel(new GridLayout(9, 2, 10, 10));
        middlePanel.setBackground(UiTheme.SURFACE);
        middlePanel.setBorder(UiTheme.surfaceBorder());

        pNameLbl = new JLabel("-");
        dentistLbl = new JLabel("-");
        tNameLbl = new JLabel("-");
        descriptionLbl = new JLabel("-");
        durationLbl = new JLabel("-");
        baseFeeLbl = new JLabel("-");
        totalLbl = new JLabel("-");
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        totalLbl.setForeground(UiTheme.DANGER);

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
        radioPanel.setBackground(UiTheme.SURFACE);
        radioPanel.add(stdRadio); radioPanel.add(emgRadio); radioPanel.add(childRadio);

        middlePanel.add(new JLabel("Select Bill Type:")); middlePanel.add(radioPanel);
        middlePanel.add(new JLabel("Final Total:")); middlePanel.add(totalLbl);

        add(middlePanel, BorderLayout.CENTER);

        // 3. Bottom Panel (Actions)
        JButton calcBtn = new JButton("Calculate Total");
        UiTheme.styleButton(calcBtn);
        generateBtn = new JButton("Generate Bill");
        UiTheme.stylePrimaryButton(generateBtn);
        viewBillBtn = new JButton("View Saved Bill");
        UiTheme.styleButton(viewBillBtn);
        viewBillBtn.setEnabled(false);
        generateBtn.setBackground(new Color(0, 153, 51));
        generateBtn.setForeground(Color.WHITE);
        generateBtn.setEnabled(false); // Disabled until calculated

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(UiTheme.CANVAS);
        bottomPanel.add(calcBtn);
        bottomPanel.add(generateBtn);
        bottomPanel.add(viewBillBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        searchBtn.addActionListener(e -> {
            String apptNo = searchField.getText().trim();
            currentAppt = apptController.searchByNumber(apptNo);
            if (currentAppt != null) {
                loadAppointment(currentAppt);
            } else {
                JOptionPane.showMessageDialog(this, "Appointment not found!");
            }
        });

        calcBtn.addActionListener(e -> {
            if (currentAppt == null) {
                JOptionPane.showMessageDialog(this, "Appointment not found.");
                return;
            }
            String status = currentAppt.getStatus() == null ? "" : currentAppt.getStatus();
            if ("cancelled".equalsIgnoreCase(status)) {
                totalLbl.setText("Cancelled");
                generateBtn.setEnabled(false);
                JOptionPane.showMessageDialog(this, "This appointment was cancelled.");
                return;
            }
            if ("pending".equalsIgnoreCase(status)) {
                totalLbl.setText("Pending");
                generateBtn.setEnabled(false);
                JOptionPane.showMessageDialog(this, "The dentist has not confirmed or cancelled this appointment yet.");
                return;
            }
            String type = stdRadio.isSelected() ? "standard" : (emgRadio.isSelected() ? "emergency" : "child");
            com.sunrisedental.pattern.strategy.FeeCalculator calc = com.sunrisedental.pattern.factory.BillFactory.getCalculator(type);
            double total = calc.calculateTreatmentFee(currentAppt.getTreatment().getBaseFee()) + currentAppt.getTreatment().getConsultationFee();
            totalLbl.setText("Rs. " + total);
            generateBtn.setEnabled(("confirmed".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) && savedBill == null);
        });

        generateBtn.addActionListener(e -> {
            if (currentAppt == null) {
                JOptionPane.showMessageDialog(this, "Appointment not found.");
                return;
            }
            String status = currentAppt.getStatus() == null ? "" : currentAppt.getStatus();
            if ("cancelled".equalsIgnoreCase(status)) {
                totalLbl.setText("Cancelled");
                generateBtn.setEnabled(false);
                JOptionPane.showMessageDialog(this, "This appointment was cancelled.");
                return;
            }
            if ("pending".equalsIgnoreCase(status)) {
                totalLbl.setText("Pending");
                generateBtn.setEnabled(false);
                JOptionPane.showMessageDialog(this, "The dentist has not confirmed or cancelled this appointment yet.");
                return;
            }
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
                } else {
                    JOptionPane.showMessageDialog(this, "The dentist has not confirmed or cancelled this appointment yet.");
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

        String status = appointment.getStatus() == null ? "" : appointment.getStatus();
        if ("cancelled".equalsIgnoreCase(status)) {
            totalLbl.setText("Cancelled");
            generateBtn.setEnabled(false);
            viewBillBtn.setEnabled(false);
            JOptionPane.showMessageDialog(this, "This appointment was cancelled.");
            return;
        }
        if ("pending".equalsIgnoreCase(status)) {
            totalLbl.setText("Pending");
            generateBtn.setEnabled(false);
            viewBillBtn.setEnabled(savedBill != null);
            JOptionPane.showMessageDialog(this, "The dentist has not confirmed or cancelled this appointment yet.");
            return;
        }

        calculateTotal();
        generateBtn.setEnabled(("confirmed".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) && savedBill == null);
        viewBillBtn.setEnabled(savedBill != null);
    }

    private void refreshTotal() {
        if (currentAppt == null) return;
        String status = currentAppt.getStatus() == null ? "" : currentAppt.getStatus();
        if ("cancelled".equalsIgnoreCase(status)) {
            totalLbl.setText("Cancelled");
            generateBtn.setEnabled(false);
            return;
        }
        if ("pending".equalsIgnoreCase(status)) {
            totalLbl.setText("Pending");
            generateBtn.setEnabled(false);
            return;
        }
        calculateTotal();
        generateBtn.setEnabled(("confirmed".equalsIgnoreCase(status) || "completed".equalsIgnoreCase(status)) && savedBill == null);
    }

    private void calculateTotal() {
        if (currentAppt == null) {
            totalLbl.setText("-");
            return;
        }
        String status = currentAppt.getStatus() == null ? "" : currentAppt.getStatus();
        if ("cancelled".equalsIgnoreCase(status)) {
            totalLbl.setText("Cancelled");
            return;
        }
        if ("pending".equalsIgnoreCase(status)) {
            totalLbl.setText("Pending");
            return;
        }
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
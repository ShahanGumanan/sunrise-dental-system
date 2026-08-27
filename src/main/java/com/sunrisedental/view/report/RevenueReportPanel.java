package com.sunrisedental.view.report;

import com.sunrisedental.controller.ReportController;
import com.sunrisedental.model.Bill;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class RevenueReportPanel extends JPanel {
    private ReportController controller = new ReportController();
    private JTextField startField, endField;
    private DefaultTableModel tableModel;
    private JLabel totalRevenueLbl;

    public RevenueReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        startField = new JTextField(LocalDate.now().withDayOfMonth(1).toString(), 10);
        endField = new JTextField(LocalDate.now().toString(), 10);
        JButton generateBtn = new JButton("Generate Revenue");

        topPanel.add(new JLabel("Start Date:")); topPanel.add(startField);
        topPanel.add(new JLabel(" End Date:")); topPanel.add(endField);
        topPanel.add(generateBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] cols = {"Receipt No", "Patient", "Treatment", "Type", "Total (Rs)"};
        tableModel = new DefaultTableModel(cols, 0);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        // Bottom Summary
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Color.WHITE);
        totalRevenueLbl = new JLabel("Total Revenue: Rs. 0.00");
        totalRevenueLbl.setFont(new Font("Arial", Font.BOLD, 18));
        totalRevenueLbl.setForeground(new Color(0, 153, 51));
        bottomPanel.add(totalRevenueLbl);
        add(bottomPanel, BorderLayout.SOUTH);

        generateBtn.addActionListener(e -> loadReport());
    }

    private void loadReport() {
        tableModel.setRowCount(0);
        double totalSum = 0;
        try {
            LocalDate start = LocalDate.parse(startField.getText());
            LocalDate end = LocalDate.parse(endField.getText());
            List<Bill> list = controller.getRevenueReport(start, end);
            
            for (Bill b : list) {
                tableModel.addRow(new Object[]{
                    b.getReceiptNumber(), b.getAppointment().getPatient().getName(),
                    b.getAppointment().getTreatment().getName(), b.getBillType(), b.getTotal()
                });
                totalSum += b.getTotal();
            }
            totalRevenueLbl.setText(String.format("Total Revenue: Rs. %.2f", totalSum));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
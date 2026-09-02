package com.sunrisedental.view.report;

import com.sunrisedental.controller.ReportController;
import com.sunrisedental.model.Bill;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.sql.Date;

public class RevenueReportPanel extends JPanel {
    private ReportController controller = new ReportController();
    private JDateChooser startField, endField;
    private DefaultTableModel tableModel;
    private JLabel totalRevenueLbl;

    public RevenueReportPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Top Panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        startField = new JDateChooser();
        startField.setDate(Date.valueOf(LocalDate.now().withDayOfMonth(1)));
        startField.setDateFormatString("yyyy-MM-dd");
        endField = new JDateChooser();
        endField.setDate(Date.valueOf(LocalDate.now()));
        endField.setDateFormatString("yyyy-MM-dd");
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
        if (startField.getDate() == null || endField.getDate() == null) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final LocalDate start = startField.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        final LocalDate end = endField.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new SwingWorker<List<Bill>, Void>() {
            @Override
            protected List<Bill> doInBackground() {
                return controller.getRevenueReport(start, end);
            }

            @Override
            protected void done() {
                tableModel.setRowCount(0);
                double totalSum = 0;
                try {
                    List<Bill> list = get();
                    for (Bill b : list) {
                        tableModel.addRow(new Object[]{
                            b.getReceiptNumber(), b.getAppointment().getPatient().getName(),
                            b.getAppointment().getTreatment().getName(), b.getBillType(), b.getTotal()
                        });
                        totalSum += b.getTotal();
                    }
                    totalRevenueLbl.setText(String.format("Total Revenue: Rs. %.2f", totalSum));
                } catch (Exception ex) {
                    totalRevenueLbl.setText("Total Revenue: Rs. 0.00");
                    JOptionPane.showMessageDialog(RevenueReportPanel.this,
                            "Unable to load revenue report. The server may be down or unreachable.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }.execute();
    }
}
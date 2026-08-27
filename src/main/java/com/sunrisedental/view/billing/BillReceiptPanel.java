package com.sunrisedental.view.billing;

import com.sunrisedental.model.Bill;
import javax.swing.*;
import java.awt.*;
import java.awt.print.*;

public class BillReceiptPanel extends JPanel implements Printable {
    private Bill bill;

    public BillReceiptPanel(Bill bill) {
        this.bill = bill;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel clinicName = new JLabel("SUNRISE DENTAL CLINIC");
        clinicName.setFont(new Font("Monospaced", Font.BOLD, 24));
        clinicName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea receiptText = new JTextArea();
        receiptText.setFont(new Font("Monospaced", Font.PLAIN, 14));
        receiptText.setEditable(false);
        receiptText.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        String text = String.format(
            "========================================\n" +
            " RECEIPT NO   : %s\n" +
            " DATE         : %s\n" +
            " BILL TYPE    : %s\n" +
            "========================================\n" +
            " PATIENT      : %s\n" +
            " TREATMENT    : %s\n" +
            "----------------------------------------\n" +
            " CONSULTATION : Rs. %.2f\n" +
            " TREATMENT FEE: Rs. %.2f\n" +
            " DISCOUNT/ADJ : Rs. %.2f\n" +
            "----------------------------------------\n" +
            " TOTAL DUE    : Rs. %.2f\n" +
            "========================================\n" +
            "       Thank you for your visit!        \n",
            bill.getReceiptNumber(), java.time.LocalDate.now().toString(), bill.getBillType().toUpperCase(),
            bill.getAppointment().getPatient().getName(), bill.getAppointment().getTreatment().getName(),
            bill.getConsultationFee(), bill.getTreatmentFee(), bill.getDiscount(), bill.getTotal()
        );
        receiptText.setText(text);

        add(clinicName);
        add(receiptText);

        JButton printBtn = new JButton("Print Receipt");
        printBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        printBtn.addActionListener(e -> printReceipt());
        add(Box.createVerticalStrut(20));
        add(printBtn);
    }

    private void printReceipt() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Printing failed: " + ex.getMessage());
            }
        }
    }

    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
        if (pageIndex > 0) return NO_SUCH_PAGE;
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        this.paintAll(g); // Renders the panel to the printer
        return PAGE_EXISTS;
    }
}
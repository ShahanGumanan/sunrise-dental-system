package com.sunrisedental.view.admin;

import com.sunrisedental.dao.TreatmentDAO;
import com.sunrisedental.dao.TreatmentDAOImpl;
import com.sunrisedental.model.Treatment;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TreatmentManagementPanel extends JPanel {
    private final TreatmentDAO treatmentDAO = new TreatmentDAOImpl();
    private DefaultTableModel tableModel;

    public TreatmentManagementPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.add(new JLabel("Manage Treatments"), BorderLayout.WEST);

        JButton addBtn = new JButton("+ Add Treatment");
        addBtn.addActionListener(e -> addTreatment());
        topPanel.add(addBtn, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        String[] cols = {"ID", "Treatment Name", "Base Fee (Rs)", "Consultation Fee (Rs)", "Duration (minutes)", "Description"};
        tableModel = new DefaultTableModel(cols, 0);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        loadTreatments();
    }

    private void loadTreatments() {
        tableModel.setRowCount(0);
        for (Treatment treatment : treatmentDAO.findAll()) {
            tableModel.addRow(new Object[]{treatment.getId(), treatment.getName(), treatment.getBaseFee(),
                    treatment.getConsultationFee(), treatment.getDurationMinutes(), treatment.getDescription()});
        }
    }

    private void addTreatment() {
        JTextField nameF = new JTextField();
        JTextField baseF = new JTextField();
        JTextField conF = new JTextField("500");
        String[] durationOptions = new String[16];
        for (int index = 0; index < durationOptions.length; index++) {
            durationOptions[index] = (index + 1) * 15 + " minutes";
        }
        JComboBox<String> durationF = new JComboBox<>(durationOptions);
        durationF.setSelectedItem("30 minutes");
        JTextArea descriptionF = new JTextArea(3, 20);

        Object[] msg = {"Treatment Name:", nameF, "Base Fee (Rs):", baseF, "Consultation Fee (Rs):", conF,
            "Duration (minutes):", durationF, "Description:", new JScrollPane(descriptionF)};
        if (JOptionPane.showConfirmDialog(this, msg, "Add Treatment", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                String name = nameF.getText().trim();
                double baseFee = Double.parseDouble(baseF.getText().trim());
                double consultationFee = Double.parseDouble(conF.getText().trim());
                int durationMinutes = Integer.parseInt(((String) durationF.getSelectedItem()).split(" ")[0]);
                if (name.isEmpty() || baseFee < 0 || consultationFee < 0 || durationMinutes <= 0) throw new IllegalArgumentException();
                Treatment treatment = new Treatment();
                treatment.setName(name);
                treatment.setBaseFee(baseFee);
                treatment.setConsultationFee(consultationFee);
                treatment.setDurationMinutes(durationMinutes);
                treatment.setDescription(descriptionF.getText().trim());
                if (!treatmentDAO.create(treatment)) throw new IllegalStateException();
                loadTreatments();
                JOptionPane.showMessageDialog(this, "Treatment Added!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Enter a name, valid non-negative fees, and a positive duration.",
                        "Treatment management", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
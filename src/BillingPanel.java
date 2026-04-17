import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

/**
 * BillingPanel.java
 * Calls the MySQL stored function get_total_treatment_cost(patient_id)
 * to calculate the total bill, then optionally saves it to the Billing table.
 */
public class BillingPanel extends JPanel {

    // ── Widgets ───────────────────────────────────────────────────────────────
    private final JTextField patientIdField = UIHelper.createNumericTextField(10, false);
    private final JLabel     amountLabel    = new JLabel("₹ 0.00");
    private final JLabel     patientName    = new JLabel(" ");
    private final JLabel     statusLabel    = new JLabel(" ");

    // Bill history table
    private final String[] COLS = {"Bill ID", "Patient ID", "Patient Name", "Total Amount", "Bill Date"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable billTable = UIHelper.createTable(tableModel);

    private double lastCalculatedAmount = 0.0;
    private int    lastPatientId        = -1;

    // ────────────────────────────────────────────────────────────────────────
    public BillingPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIHelper.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadBillHistory();
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = UIHelper.createSectionTitle("💰  Billing Calculator");
        JLabel sub   = UIHelper.createLabel("Uses stored function get_total_treatment_cost(patient_id) to compute the bill.");
        sub.setFont(UIHelper.FONT_SMALL);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setBackground(UIHelper.BG_DARK);
        info.add(title); info.add(sub);

        JButton refresh = UIHelper.createButton("↻  History", UIHelper.ACCENT, UIHelper.ACCENT_HOVER);
        refresh.addActionListener(e -> loadBillHistory());

        p.add(info,    BorderLayout.WEST);
        p.add(refresh, BorderLayout.EAST);
        p.add(UIHelper.createSeparator(), BorderLayout.SOUTH);
        return p;
    }

    // ── Main content ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        p.add(buildCalculatorCard(), BorderLayout.WEST);
        p.add(buildHistoryPanel(),  BorderLayout.CENTER);
        return p;
    }

    // ── Calculator card (left) ────────────────────────────────────────────────
    private JPanel buildCalculatorCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 0));
        card.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        card.setPreferredSize(new Dimension(320, 0));

        // Card title
        JLabel cardTitle = new JLabel("Generate Bill");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        cardTitle.setForeground(UIHelper.ACCENT);
        cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Input row
        JPanel inputRow = new JPanel(new GridBagLayout());
        inputRow.setBackground(UIHelper.BG_CARD);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1.0;

        g.gridx = 0; g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);
        inputRow.add(UIHelper.createLabel("Patient ID"), g);
        g.gridy = 1; g.insets = new Insets(0, 0, 0, 0);
        inputRow.add(patientIdField, g);

        // Patient name display
        patientName.setFont(UIHelper.FONT_SMALL);
        patientName.setForeground(UIHelper.TEXT_SECONDARY);
        g.gridy = 2; g.insets = new Insets(6, 2, 0, 0);
        inputRow.add(patientName, g);

        // Amount display — big prominent number
        amountLabel.setFont(UIHelper.FONT_NUMBER);
        amountLabel.setForeground(new Color(16, 220, 140));
        amountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        amountLabel.setBorder(BorderFactory.createEmptyBorder(28, 0, 8, 0));

        JLabel amtCaption = new JLabel("Total Treatment Cost");
        amtCaption.setFont(UIHelper.FONT_SMALL);
        amtCaption.setForeground(UIHelper.TEXT_SECONDARY);
        amtCaption.setHorizontalAlignment(SwingConstants.CENTER);

        // Status label
        statusLabel.setFont(UIHelper.FONT_SMALL);
        statusLabel.setForeground(UIHelper.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        // Buttons
        JButton calcBtn = UIHelper.createButton("🔎  Calculate Bill", UIHelper.ACCENT, UIHelper.ACCENT_HOVER);
        JButton saveBtn = UIHelper.createButton("💾  Save to Billing", UIHelper.SUCCESS, UIHelper.SUCCESS_HOVER);
        calcBtn.addActionListener(e -> calculateBill());
        saveBtn.addActionListener(e -> saveBill());

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 8));
        btnPanel.setBackground(UIHelper.BG_CARD);
        btnPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        btnPanel.add(calcBtn); btnPanel.add(saveBtn);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setBackground(UIHelper.BG_CARD);
        center.add(inputRow,   BorderLayout.NORTH);
        center.add(amountLabel, BorderLayout.CENTER);

        JPanel amtBottom = new JPanel(new GridLayout(3, 1, 0, 0));
        amtBottom.setBackground(UIHelper.BG_CARD);
        amtBottom.add(amtCaption);
        amtBottom.add(statusLabel);
        amtBottom.add(btnPanel);
        center.add(amtBottom, BorderLayout.SOUTH);

        card.add(cardTitle, BorderLayout.NORTH);
        card.add(center,    BorderLayout.CENTER);
        return card;
    }

    // ── Bill history panel (right) ────────────────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UIHelper.BG_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIHelper.BG_DARK);

        JLabel lbl = new JLabel("Saved Bills History");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UIHelper.ACCENT);

        top.add(lbl, BorderLayout.WEST);
        top.add(UIHelper.createSearchPanel(billTable, tableModel), BorderLayout.EAST);

        int[] widths = {60, 80, 140, 120, 120};
        for (int i = 0; i < widths.length; i++)
            billTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        p.add(top, BorderLayout.NORTH);
        p.add(UIHelper.createScrollPane(billTable), BorderLayout.CENTER);
        return p;
    }

    // ── DB Operations ─────────────────────────────────────────────────────────
    private void calculateBill() {
        String pidStr = patientIdField.getText().trim();
        if (pidStr.isEmpty()) { showError("Enter a Patient ID."); return; }

        try {
            int pid = Integer.parseInt(pidStr);
            // Verify patient exists and get name
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT name FROM Patient WHERE patient_id=?")) {
                ps.setInt(1, pid);
                ResultSet r = ps.executeQuery();
                if (!r.next()) {
                    patientName.setText("⚠ Patient not found");
                    patientName.setForeground(UIHelper.DANGER);
                    amountLabel.setText("₹ 0.00"); return;
                }
                patientName.setText("Patient: " + r.getString("name"));
                patientName.setForeground(UIHelper.TEXT_SECONDARY);
            }

            // Call the stored function
            String funcSQL = "SELECT get_total_treatment_cost(?) AS total";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(funcSQL)) {
                ps.setInt(1, pid);
                ResultSet r = ps.executeQuery();
                if (r.next()) {
                    double total = r.getDouble("total");
                    lastCalculatedAmount = total;
                    lastPatientId        = pid;
                    amountLabel .setText(String.format("₹ %.2f", total));
                    statusLabel .setText(total == 0
                        ? "ℹ No treatments found for this patient."
                        : "✔ Bill calculated using stored function.");
                    statusLabel.setForeground(total == 0 ? UIHelper.WARNING : UIHelper.SUCCESS);
                }
            }
        } catch (NumberFormatException e) { showError("Patient ID must be an integer."); }
          catch (SQLException e)         { showError("Calculation failed: " + e.getMessage()); }
    }

    private void saveBill() {
        if (lastPatientId == -1) { showError("Calculate a bill first."); return; }
        if (lastCalculatedAmount == 0.0) {
            showError("Total is ₹0.00 — no treatments to bill."); return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
            String.format("Save bill of ₹%.2f for Patient #%d?", lastCalculatedAmount, lastPatientId),
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        String sql = "INSERT INTO Billing (patient_id, total_amount, bill_date) VALUES (?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, lastPatientId);
            ps.setDouble(2, lastCalculatedAmount);
            ps.setString(3, LocalDate.now().toString());
            ps.executeUpdate();
            showSuccess(String.format("Bill of ₹%.2f saved!", lastCalculatedAmount));
            statusLabel.setText("💾 Bill saved to Billing table.");
            lastPatientId = -1; lastCalculatedAmount = 0.0;
            loadBillHistory();
        } catch (SQLException e) { showError("Save failed: " + e.getMessage()); }
    }

    private void loadBillHistory() {
        tableModel.setRowCount(0);
        String sql = "SELECT b.bill_id, b.patient_id, p.name, b.total_amount, b.bill_date "
                   + "FROM Billing b JOIN Patient p ON b.patient_id=p.patient_id "
                   + "ORDER BY b.bill_id DESC";
        try (Connection c = DBConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            while (r.next()) {
                tableModel.addRow(new Object[]{
                    r.getInt("bill_id"),
                    r.getInt("patient_id"),
                    r.getString("name"),
                    String.format("₹ %.2f", r.getDouble("total_amount")),
                    r.getDate("bill_date")
                });
            }
        } catch (SQLException ex) {
            // Silently ignore — may fail if no bills yet
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void showError  (String m) { JOptionPane.showMessageDialog(this, m, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String m) { JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE); }
}

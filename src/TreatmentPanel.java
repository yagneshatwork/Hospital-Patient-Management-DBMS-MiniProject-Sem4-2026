import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

/**
 * TreatmentPanel.java
 * Manages Treatment records for patients.
 * Allows adding treatments linked to a Patient ID.
 */
public class TreatmentPanel extends JPanel {

    // ── Form fields ──────────────────────────────────────────────────────────
    private final JTextField patientIdField     = UIHelper.createNumericTextField(10, false);
    private final JTextField treatmentNameField = UIHelper.createTextField();
    private final JTextField costField          = UIHelper.createNumericTextField(10, true);
    private final JTextField dateField          = UIHelper.createTextField();

    // ── Table ────────────────────────────────────────────────────────────────
    private final String[] COLS = {"Tx ID", "Patient ID", "Treatment Name", "Cost (₹)", "Date"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable treatmentTable = UIHelper.createTable(tableModel);

    // ────────────────────────────────────────────────────────────────────────
    public TreatmentPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIHelper.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        // Pre-fill date field with today
        dateField.setText(LocalDate.now().toString());

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadTreatments();
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = UIHelper.createSectionTitle("💊  Treatment Records");
        JLabel sub   = UIHelper.createLabel("Add treatments for admitted patients. Costs are used in billing calculation.");
        sub.setFont(UIHelper.FONT_SMALL);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setBackground(UIHelper.BG_DARK);
        info.add(title); info.add(sub);

        JButton refresh = UIHelper.createButton("↻  Refresh", UIHelper.ACCENT, UIHelper.ACCENT_HOVER);
        refresh.addActionListener(e -> loadTreatments());

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
        p.add(buildFormCard(),  BorderLayout.WEST);
        p.add(buildTablePanel(), BorderLayout.CENTER);
        return p;
    }

    // ── Form card ─────────────────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(310, 0));

        JLabel cardTitle = new JLabel("Treatment Details");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardTitle.setForeground(UIHelper.ACCENT);

        card.add(cardTitle,      BorderLayout.NORTH);
        card.add(buildFields(),  BorderLayout.CENTER);
        card.add(buildButtons(), BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFields() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIHelper.BG_CARD);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        addRow(p, g, 0, "Patient ID *",    patientIdField,
               "Use the ID shown in the Patients tab.");
        addRow(p, g, 1, "Treatment Name *", treatmentNameField,
               "e.g. Blood Test, X-Ray, MRI Scan");
        addRow(p, g, 2, "Cost (₹) *",      costField,
               "Enter decimal amount, e.g. 1500.00");
        addRow(p, g, 3, "Date (YYYY-MM-DD) *", dateField,
               "Defaults to today's date.");

        // Info box
        g.gridx = 0; g.gridy = 8; g.gridwidth = 2;
        g.insets = new Insets(16, 0, 0, 0);
        JLabel info = new JLabel("<html><body style='color:#8A90B0;font-size:11px;'>"
            + "ℹ  Costs are summed by the stored function<br>"
            + "<b>get_total_treatment_cost()</b> in Billing.</body></html>");
        p.add(info, g);

        return p;
    }

    private void addRow(JPanel p, GridBagConstraints g, int pos,
                        String label, JComponent field, String tip) {
        g.gridx = 0; g.gridy = pos * 2; g.gridwidth = 2; g.insets = new Insets(6, 0, 2, 0);
        p.add(UIHelper.createLabel(label), g);
        g.gridy = pos * 2 + 1; g.insets = new Insets(0, 0, 0, 0);
        field.setToolTipText(tip);
        p.add(field, g);
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new GridLayout(1, 2, 8, 0));
        p.setBackground(UIHelper.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JButton addBtn   = UIHelper.createButton("➕  Add Treatment", UIHelper.SUCCESS, UIHelper.SUCCESS_HOVER);
        JButton clearBtn = UIHelper.createButton("✖  Clear",          UIHelper.BG_INPUT, UIHelper.BORDER_COLOR);

        addBtn  .addActionListener(e -> addTreatment());
        clearBtn.addActionListener(e -> clearForm());

        p.add(addBtn); p.add(clearBtn);
        return p;
    }

    // ── Table panel ───────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UIHelper.BG_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIHelper.BG_DARK);

        JLabel lbl = new JLabel("All Treatments");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UIHelper.ACCENT);

        top.add(lbl, BorderLayout.WEST);
        top.add(UIHelper.createSearchPanel(treatmentTable, tableModel), BorderLayout.EAST);

        int[] widths = {60, 80, 200, 100, 120};
        for (int i = 0; i < widths.length; i++)
            treatmentTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        p.add(top, BorderLayout.NORTH);
        p.add(UIHelper.createScrollPane(treatmentTable), BorderLayout.CENTER);
        return p;
    }

    // ── DB Operations ─────────────────────────────────────────────────────────
    private void loadTreatments() {
        tableModel.setRowCount(0);
        String sql = "SELECT t.treatment_id, t.patient_id, t.treatment_name, t.cost, t.treatment_date "
                   + "FROM Treatment t ORDER BY t.treatment_id DESC";
        try (Connection c = DBConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            while (r.next()) {
                tableModel.addRow(new Object[]{
                    r.getInt("treatment_id"),
                    r.getInt("patient_id"),
                    r.getString("treatment_name"),
                    String.format("₹ %.2f", r.getDouble("cost")),
                    r.getDate("treatment_date")
                });
            }
        } catch (SQLException ex) { showError("Load failed: " + ex.getMessage()); }
    }

    private void addTreatment() {
        String pidStr  = patientIdField.getText().trim();
        String name    = treatmentNameField.getText().trim();
        String costStr = costField.getText().trim();
        String date    = dateField.getText().trim();

        if (pidStr.isEmpty() || name.isEmpty() || costStr.isEmpty() || date.isEmpty()) {
            showError("All fields marked with * are required."); return;
        }

        if (!date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            showError("Date must be in YYYY-MM-DD format."); return;
        }

        try {
            int    pid  = Integer.parseInt(pidStr);
            double cost = Double.parseDouble(costStr);
            if (cost <= 0) { showError("Cost must be a positive value."); return; }

            // Verify patient exists
            try (Connection c  = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                     "SELECT patient_id FROM Patient WHERE patient_id=?")) {
                ps.setInt(1, pid);
                ResultSet r = ps.executeQuery();
                if (!r.next()) { showError("No patient found with ID: " + pid); return; }
            }

            String sql = "INSERT INTO Treatment (patient_id, treatment_name, cost, treatment_date) "
                       + "VALUES (?, ?, ?, ?)";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt   (1, pid);
                ps.setString(2, name);
                ps.setDouble(3, cost);
                ps.setString(4, date);   // MySQL parses YYYY-MM-DD directly
                ps.executeUpdate();
                showSuccess("Treatment record added successfully!");
                clearForm(); loadTreatments();
            }
        } catch (NumberFormatException e) { showError("Patient ID must be integer; Cost must be numeric."); }
          catch (SQLException e)         { showError("Add failed: " + e.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void clearForm() {
        patientIdField.setText(""); treatmentNameField.setText("");
        costField.setText("");      dateField.setText(LocalDate.now().toString());
    }

    private void showError  (String m) { JOptionPane.showMessageDialog(this, m, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String m) { JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE); }
}

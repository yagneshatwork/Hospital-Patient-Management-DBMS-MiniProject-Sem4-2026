import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/**
 * PatientPanel.java
 * Manages Patient records: Add / Update / Delete / View All
 * UPDATE and DELETE fire database triggers that log to the Audit table.
 */
public class PatientPanel extends JPanel {

    // ── Form fields ──────────────────────────────────────────────────────────
    private final JTextField      nameField  = UIHelper.createTextField();
    private final JTextField      ageField   = UIHelper.createTextField();
    private final JTextField      phoneField = UIHelper.createTextField();
    private final JComboBox<String> genderBox = UIHelper.createComboBox("Male","Female","Other");
    private final JTextArea       addressArea;

    // ── Table ────────────────────────────────────────────────────────────────
    private final String[] COLS = {"ID", "Name", "Age", "Gender", "Phone", "Address", "Registered At"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable patientTable = UIHelper.createTable(tableModel);

    private int selectedId = -1;   // -1 means "no selection"

    // ────────────────────────────────────────────────────────────────────────
    public PatientPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIHelper.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        addressArea = buildAddressArea();

        add(buildHeader(),  BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);

        loadPatients();
    }

    // ── Header bar ───────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = UIHelper.createSectionTitle("👤  Patient Management");
        JLabel sub   = UIHelper.createLabel("Add, edit and remove patient records. Update/Delete events are auto-logged.");
        sub.setFont(UIHelper.FONT_SMALL);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setBackground(UIHelper.BG_DARK);
        info.add(title);
        info.add(sub);

        JButton refresh = UIHelper.createButton("↻  Refresh", UIHelper.ACCENT, UIHelper.ACCENT_HOVER);
        refresh.addActionListener(e -> loadPatients());

        p.add(info,    BorderLayout.WEST);
        p.add(refresh, BorderLayout.EAST);
        p.add(UIHelper.createSeparator(), BorderLayout.SOUTH);
        return p;
    }

    // ── Main content (form left + table right) ────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(14, 0));
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        p.add(buildFormCard(), BorderLayout.WEST);
        p.add(buildTablePanel(), BorderLayout.CENTER);
        return p;
    }

    // ── Form card ─────────────────────────────────────────────────────────────
    private JPanel buildFormCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new BorderLayout(0, 14));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        card.setPreferredSize(new Dimension(310, 0));

        JLabel cardTitle = new JLabel("Patient Details");
        cardTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cardTitle.setForeground(UIHelper.ACCENT);

        card.add(cardTitle,        BorderLayout.NORTH);
        card.add(buildFields(),    BorderLayout.CENTER);
        card.add(buildButtons(),   BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildFields() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(UIHelper.BG_CARD);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1.0;

        addRow(p, g, 0, "Full Name *",   nameField);
        addRow(p, g, 1, "Age *",         ageField);
        addRow(p, g, 2, "Gender",        genderBox);
        addRow(p, g, 3, "Phone",         phoneField);

        // Address row
        g.gridx = 0; g.gridy = 8; g.gridwidth = 2; g.insets = new Insets(6, 0, 2, 0);
        p.add(UIHelper.createLabel("Address"), g);

        JScrollPane addrScroll = UIHelper.createScrollPane(addressArea);
        addrScroll.setPreferredSize(new Dimension(0, 78));
        addrScroll.getViewport().setBackground(UIHelper.BG_INPUT);
        g.gridy = 9; g.insets = new Insets(0, 0, 0, 0);
        p.add(addrScroll, g);

        return p;
    }

    private void addRow(JPanel p, GridBagConstraints g, int pos, String label, JComponent field) {
        g.gridx = 0; g.gridy = pos * 2; g.gridwidth = 2; g.insets = new Insets(6, 0, 2, 0);
        p.add(UIHelper.createLabel(label), g);
        g.gridy = pos * 2 + 1; g.insets = new Insets(0, 0, 0, 0);
        p.add(field, g);
    }

    private JPanel buildButtons() {
        JPanel p = new JPanel(new GridLayout(2, 2, 8, 8));
        p.setBackground(UIHelper.BG_CARD);
        p.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        JButton addBtn    = UIHelper.createButton("➕  Add",    UIHelper.SUCCESS, UIHelper.SUCCESS_HOVER);
        JButton updateBtn = UIHelper.createButton("✏  Update",  UIHelper.WARNING, UIHelper.WARNING_HOVER);
        JButton deleteBtn = UIHelper.createButton("🗑  Delete",  UIHelper.DANGER,  UIHelper.DANGER_HOVER);
        JButton clearBtn  = UIHelper.createButton("✖  Clear",   UIHelper.BG_INPUT, UIHelper.BORDER_COLOR);

        addBtn   .addActionListener(e -> addPatient());
        updateBtn.addActionListener(e -> updatePatient());
        deleteBtn.addActionListener(e -> deletePatient());
        clearBtn .addActionListener(e -> clearForm());

        p.add(addBtn); p.add(updateBtn);
        p.add(deleteBtn); p.add(clearBtn);
        return p;
    }

    // ── Table panel ───────────────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UIHelper.BG_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIHelper.BG_DARK);

        JLabel lbl = new JLabel("All Patients");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UIHelper.ACCENT);

        top.add(lbl, BorderLayout.WEST);
        top.add(UIHelper.createSearchPanel(patientTable, tableModel), BorderLayout.EAST);

        // Column widths
        int[] widths = {40, 130, 40, 70, 100, 140, 145};
        for (int i = 0; i < widths.length; i++)
            patientTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) populateForm();
        });

        p.add(top, BorderLayout.NORTH);
        p.add(UIHelper.createScrollPane(patientTable), BorderLayout.CENTER);
        return p;
    }

    // ── Address area builder ─────────────────────────────────────────────────
    private JTextArea buildAddressArea() {
        JTextArea a = new JTextArea(3, 20);
        a.setFont(UIHelper.FONT_BODY);
        a.setBackground(UIHelper.BG_INPUT);
        a.setForeground(UIHelper.TEXT_PRIMARY);
        a.setCaretColor(UIHelper.TEXT_PRIMARY);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        return a;
    }

    // ── DB Operations ─────────────────────────────────────────────────────────
    private void loadPatients() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM Patient ORDER BY patient_id DESC";
        try (Connection c = DBConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            while (r.next()) {
                tableModel.addRow(new Object[]{
                    r.getInt("patient_id"),
                    r.getString("name"),
                    r.getInt("age"),
                    r.getString("gender"),
                    r.getString("phone"),
                    r.getString("address"),
                    r.getTimestamp("created_at")
                });
            }
        } catch (SQLException ex) { showError("Load failed: " + ex.getMessage()); }
    }

    private void addPatient() {
        String name   = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        if (name.isEmpty() || ageStr.isEmpty()) { showError("Name and Age are required."); return; }

        try {
            int age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 150) { showError("Enter a valid age (1 – 150)."); return; }

            String sql = "INSERT INTO Patient (name,age,gender,phone,address) VALUES (?,?,?,?,?)";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt   (2, age);
                ps.setString(3, (String) genderBox.getSelectedItem());
                ps.setString(4, phoneField.getText().trim());
                ps.setString(5, addressArea.getText().trim());
                ps.executeUpdate();
                showSuccess("Patient added successfully!");
                clearForm(); loadPatients();
            }
        } catch (NumberFormatException e) { showError("Age must be a number."); }
          catch (SQLException e)         { showError("Add failed: " + e.getMessage()); }
    }

    private void updatePatient() {
        if (selectedId == -1) { showError("Select a patient from the table first."); return; }
        String name = nameField.getText().trim();
        String ageStr = ageField.getText().trim();
        if (name.isEmpty() || ageStr.isEmpty()) { showError("Name and Age are required."); return; }

        try {
            int age = Integer.parseInt(ageStr);
            int ok = JOptionPane.showConfirmDialog(this,
                "Update patient #" + selectedId + "?\n(Trigger will log the old values to Audit.)",
                "Confirm Update", JOptionPane.YES_NO_OPTION);
            if (ok != JOptionPane.YES_OPTION) return;

            String sql = "UPDATE Patient SET name=?,age=?,gender=?,phone=?,address=? WHERE patient_id=?";
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setInt   (2, age);
                ps.setString(3, (String) genderBox.getSelectedItem());
                ps.setString(4, phoneField.getText().trim());
                ps.setString(5, addressArea.getText().trim());
                ps.setInt   (6, selectedId);
                ps.executeUpdate();
                showSuccess("Patient updated! Audit log entry created.");
                clearForm(); loadPatients();
            }
        } catch (NumberFormatException e) { showError("Age must be a number."); }
          catch (SQLException e)         { showError("Update failed: " + e.getMessage()); }
    }

    private void deletePatient() {
        if (selectedId == -1) { showError("Select a patient from the table first."); return; }

        int ok = JOptionPane.showConfirmDialog(this,
            "Delete patient #" + selectedId + "?\n" +
            "• Trigger will log the deletion to Audit.\n" +
            "• All treatment records will also be removed.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM Patient WHERE patient_id=?")) {
            ps.setInt(1, selectedId);
            ps.executeUpdate();
            showSuccess("Patient deleted! Audit log entry created.");
            clearForm(); loadPatients();
        } catch (SQLException e) { showError("Delete failed: " + e.getMessage()); }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void populateForm() {
        int row = patientTable.getSelectedRow();
        if (row < 0) return;
        selectedId = (int) tableModel.getValueAt(row, 0);
        nameField  .setText((String) tableModel.getValueAt(row, 1));
        ageField   .setText(String.valueOf(tableModel.getValueAt(row, 2)));
        genderBox  .setSelectedItem(tableModel.getValueAt(row, 3));
        phoneField .setText(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");
        addressArea.setText(tableModel.getValueAt(row, 5) != null ? tableModel.getValueAt(row, 5).toString() : "");
    }

    private void clearForm() {
        selectedId = -1;
        nameField.setText(""); ageField.setText(""); phoneField.setText(""); addressArea.setText("");
        genderBox.setSelectedIndex(0);
        patientTable.clearSelection();
    }

    private void showError  (String m) { JOptionPane.showMessageDialog(this, m, "Error",   JOptionPane.ERROR_MESSAGE); }
    private void showSuccess(String m) { JOptionPane.showMessageDialog(this, m, "Success", JOptionPane.INFORMATION_MESSAGE); }
}

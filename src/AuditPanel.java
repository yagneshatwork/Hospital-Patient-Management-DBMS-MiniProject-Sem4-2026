import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

/**
 * AuditPanel.java
 * Displays all entries written to the Audit table by the MySQL BEFORE UPDATE
 * and BEFORE DELETE triggers defined on the Patient table.
 */
public class AuditPanel extends JPanel {

    private final String[] COLS = {"Audit ID", "Patient ID", "Patient Name", "Action", "Timestamp"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable auditTable = UIHelper.createTable(tableModel);

    private final JLabel countLabel  = new JLabel("0 log entries");
    private final JLabel statusLabel = new JLabel(" ");

    // ────────────────────────────────────────────────────────────────────────
    public AuditPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIHelper.BG_DARK);
        setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        // Colour UPDATE rows amber, DELETE rows red via custom renderer
        applyAuditRenderer();

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        loadAudit();
    }

    // ── Header ───────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = UIHelper.createSectionTitle("📜  Audit Log");
        JLabel sub   = UIHelper.createLabel(
            "Automatic log entries created by BEFORE UPDATE / BEFORE DELETE triggers on the Patient table.");
        sub.setFont(UIHelper.FONT_SMALL);

        JPanel info = new JPanel(new GridLayout(2, 1, 0, 2));
        info.setBackground(UIHelper.BG_DARK);
        info.add(title); info.add(sub);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setBackground(UIHelper.BG_DARK);

        JButton refresh    = UIHelper.createButton("↻  Refresh",    UIHelper.ACCENT,  UIHelper.ACCENT_HOVER);
        JButton clearAllBtn= UIHelper.createButton("🗑  Clear Logs", UIHelper.DANGER,  UIHelper.DANGER_HOVER);
        refresh    .addActionListener(e -> loadAudit());
        clearAllBtn.addActionListener(e -> clearAllLogs());
        buttons.add(refresh);
        buttons.add(clearAllBtn);

        p.add(info,    BorderLayout.WEST);
        p.add(buttons, BorderLayout.EAST);
        p.add(UIHelper.createSeparator(), BorderLayout.SOUTH);
        return p;
    }

    // ── Main content (legend card + table) ────────────────────────────────────
    private JPanel buildContent() {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        p.add(buildLegendCard(), BorderLayout.NORTH);
        p.add(buildTable(),      BorderLayout.CENTER);
        return p;
    }

    private JPanel buildLegendCard() {
        JPanel card = UIHelper.createCard();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 10));
        card.setPreferredSize(new Dimension(0, 50));

        card.add(legendDot(UIHelper.WARNING, " UPDATE  – old values before patient was modified"));
        card.add(legendDot(UIHelper.DANGER,  " DELETE  – old values before patient was removed"));
        card.add(legendDot(UIHelper.SUCCESS, " System  – all events are automatic (no manual insert)"));

        return card;
    }

    private JPanel legendDot(Color c, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setBackground(UIHelper.BG_CARD);

        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(c);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        dot.setPreferredSize(new Dimension(12, 12));
        dot.setBackground(UIHelper.BG_CARD);

        JLabel lbl = new JLabel(text);
        lbl.setFont(UIHelper.FONT_SMALL);
        lbl.setForeground(UIHelper.TEXT_SECONDARY);

        p.add(dot); p.add(lbl);
        return p;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(UIHelper.BG_DARK);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UIHelper.BG_DARK);

        JLabel lbl = new JLabel("Audit Records");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UIHelper.ACCENT);

        top.add(lbl, BorderLayout.WEST);
        top.add(UIHelper.createSearchPanel(auditTable, tableModel), BorderLayout.EAST);

        int[] widths = {70, 80, 160, 90, 180};
        for (int i = 0; i < widths.length; i++)
            auditTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        p.add(top, BorderLayout.NORTH);
        p.add(UIHelper.createScrollPane(auditTable), BorderLayout.CENTER);
        return p;
    }

    // ── Footer ────────────────────────────────────────────────────────────────
    private JPanel buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.BG_DARK);
        p.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        countLabel.setFont(UIHelper.FONT_SMALL);
        countLabel.setForeground(UIHelper.TEXT_SECONDARY);

        statusLabel.setFont(UIHelper.FONT_SMALL);
        statusLabel.setForeground(UIHelper.SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        p.add(countLabel,  BorderLayout.WEST);
        p.add(statusLabel, BorderLayout.EAST);
        return p;
    }

    // ── Custom renderer: colour action column ─────────────────────────────────
    private void applyAuditRenderer() {
        auditTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, selected, focus, row, col);

                if (!selected) {
                    c.setBackground(row % 2 == 0 ? UIHelper.TABLE_EVEN : UIHelper.TABLE_ODD);
                    // Colour-code by action type
                    Object action = t.getModel().getValueAt(row, 3); // column 3 = Action
                    if ("DELETE".equals(action)) {
                        c.setForeground(new Color(255, 120, 120));  // soft red
                    } else if ("UPDATE".equals(action)) {
                        c.setForeground(new Color(255, 200, 80));   // amber
                    } else {
                        c.setForeground(UIHelper.TEXT_PRIMARY);
                    }
                } else {
                    c.setBackground(new Color(99, 102, 241, 170));
                    c.setForeground(Color.WHITE);
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // ── DB Operations ─────────────────────────────────────────────────────────
    private void loadAudit() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM Audit ORDER BY audit_id DESC";
        try (Connection c = DBConnection.getConnection();
             Statement  s = c.createStatement();
             ResultSet  r = s.executeQuery(sql)) {
            while (r.next()) {
                tableModel.addRow(new Object[]{
                    r.getInt("audit_id"),
                    r.getInt("patient_id"),
                    r.getString("name"),
                    r.getString("action_type"),
                    r.getTimestamp("action_time")
                });
            }
            int rows = tableModel.getRowCount();
            countLabel.setText(rows + " log " + (rows == 1 ? "entry" : "entries"));
            statusLabel.setText("Last refreshed: " + new java.util.Date());
        } catch (SQLException ex) {
            statusLabel.setForeground(UIHelper.DANGER);
            statusLabel.setText("Load failed: " + ex.getMessage());
        }
    }

    private void clearAllLogs() {
        int ok = JOptionPane.showConfirmDialog(this,
            "Clear ALL audit log entries? This cannot be undone.",
            "Confirm Clear", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;

        try (Connection c  = DBConnection.getConnection();
             Statement  st = c.createStatement()) {
            st.executeUpdate("DELETE FROM Audit");
            JOptionPane.showMessageDialog(this, "Audit log cleared.", "Done", JOptionPane.INFORMATION_MESSAGE);
            loadAudit();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Clear failed: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

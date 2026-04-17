import javax.swing.*;
import java.awt.*;
/**
 * MainApp.java
 * Entry point for the Hospital Patient Management System.
 * Builds the main JFrame with a dark header and a JTabbedPane containing
 * the four functional panels.
 */
public class MainApp extends JFrame {

    public MainApp() {
        setTitle("Hospital Patient Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1250, 780);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setBackground(UIHelper.BG_DARK);

        // Set custom icon (optional — silently ignored if icon.png not found)
        try {
            setIconImage(Toolkit.getDefaultToolkit()
                .getImage(getClass().getResource("/icon.png")));
        } catch (Exception ignored) {}

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UIHelper.BG_DARK);
        root.add(buildHeader(),      BorderLayout.NORTH);
        root.add(buildTabPane(),     BorderLayout.CENTER);
        root.add(buildStatusBar(),   BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);
    }

    // ── Top header bar ────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient from deep navy to card colour
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(14, 14, 38),
                    getWidth(), 0, new Color(24, 26, 65)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);  // transparent
            }
        };
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(0, 72));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIHelper.ACCENT));

        // Logo / title
        JLabel logo  = new JLabel("🏥");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        logo.setBorder(BorderFactory.createEmptyBorder(0, 22, 0, 10));

        JLabel title = new JLabel("Hospital Patient Management System");
        title.setFont(UIHelper.FONT_TITLE);
        title.setForeground(UIHelper.TEXT_PRIMARY);

        JLabel sub   = new JLabel("DBMS Mini Project  ·  Java Swing  +  JDBC  +  MySQL");
        sub.setFont(UIHelper.FONT_SMALL);
        sub.setForeground(UIHelper.TEXT_SECONDARY);

        JPanel titleBlock = new JPanel(new GridLayout(2, 1, 0, 2));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(sub);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(logo);
        left.add(titleBlock);

        // Right: DB status badge
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 22, 0));
        right.setOpaque(false);

        JLabel dbBadge = new JLabel("● Connected to MySQL");
        dbBadge.setFont(UIHelper.FONT_SMALL);
        dbBadge.setForeground(UIHelper.SUCCESS);
        right.add(dbBadge);

        // Try connection to update badge
        new Thread(() -> {
            try {
                DBConnection.getConnection().close();
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    dbBadge.setText("● MySQL Not Connected");
                    dbBadge.setForeground(UIHelper.DANGER);
                    JOptionPane.showMessageDialog(null,
                        "Cannot connect to MySQL!\n\n"
                      + "  • Start MySQL service\n"
                      + "  • Run hospital_db.sql first\n"
                      + "  • Check DB_PASS in DBConnection.java\n\n"
                      + "Error: " + e.getMessage(),
                        "Connection Error", JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        panel.add(left,  BorderLayout.WEST);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    // ── Tabbed pane ───────────────────────────────────────────────────────────
    private JTabbedPane buildTabPane() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(UIHelper.BG_DARK);
        tabs.setForeground(UIHelper.TEXT_PRIMARY);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabs.addTab("👤  Patients",     null, new PatientPanel(),   "Manage patient records");
        tabs.addTab("💊  Treatments",   null, new TreatmentPanel(), "Add treatments for patients");
        tabs.addTab("💰  Billing",      null, new BillingPanel(),   "Calculate bills using stored function");
        tabs.addTab("📜  Audit Log",    null, new AuditPanel(),     "View trigger-generated audit entries");

        return tabs;
    }

    // ── Bottom status bar ─────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(10, 10, 26));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIHelper.BORDER_COLOR));
        bar.setPreferredSize(new Dimension(0, 26));

        JLabel left = new JLabel("  hospital_db @ localhost:3306   |   Java Swing + JDBC   |   DBMS Mini Project");
        left.setFont(UIHelper.FONT_SMALL);
        left.setForeground(UIHelper.TEXT_SECONDARY);

        JLabel right = new JLabel("Triggers: before_patient_update, before_patient_delete   |   Function: get_total_treatment_cost()   ");
        right.setFont(UIHelper.FONT_SMALL);
        right.setForeground(new Color(80, 85, 140));

        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        // Apply UIManager theme before any Swing components are created
        UIHelper.applyTheme();

        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            UIHelper.applyTheme(); // re-apply after L&F
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(MainApp::new);
    }
}

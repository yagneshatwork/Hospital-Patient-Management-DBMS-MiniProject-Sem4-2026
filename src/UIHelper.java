import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * UIHelper.java
 * Centralised colour palette, fonts, and reusable Swing factory methods
 * for a modern dark-themed Hospital Patient Management System.
 */
public class UIHelper {

    // ── Colour Palette ───────────────────────────────────────────────────────
    public static final Color BG_DARK          = new Color(11,  12,  30);
    public static final Color BG_CARD          = new Color(20,  22,  54);
    public static final Color BG_INPUT         = new Color(30,  32,  70);
    public static final Color ACCENT           = new Color(99,  102, 241);   // Indigo
    public static final Color ACCENT_HOVER     = new Color(79,  82,  221);
    public static final Color SUCCESS          = new Color(16,  185, 129);   // Emerald
    public static final Color SUCCESS_HOVER    = new Color(5,   150, 105);
    public static final Color DANGER           = new Color(239, 68,  68);    // Red
    public static final Color DANGER_HOVER     = new Color(220, 38,  38);
    public static final Color WARNING          = new Color(245, 158, 11);    // Amber
    public static final Color WARNING_HOVER    = new Color(217, 119, 6);
    public static final Color TEXT_PRIMARY     = new Color(235, 237, 255);
    public static final Color TEXT_SECONDARY   = new Color(140, 148, 178);
    public static final Color BORDER_COLOR     = new Color(45,  48,  100);
    public static final Color TABLE_EVEN       = new Color(20,  22,  54);
    public static final Color TABLE_ODD        = new Color(26,  28,  65);
    public static final Color TABLE_HEADER_BG  = new Color(33,  36,  85);

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_HEADER  = new Font("Segoe UI", Font.BOLD,  16);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_NUMBER  = new Font("Segoe UI", Font.BOLD,  36);

    // ── Global UIManager Theme ───────────────────────────────────────────────
    public static void applyTheme() {
        UIManager.put("Panel.background",                 BG_DARK);
        UIManager.put("OptionPane.background",            BG_CARD);
        UIManager.put("OptionPane.messageForeground",     TEXT_PRIMARY);

        UIManager.put("TabbedPane.background",            BG_DARK);
        UIManager.put("TabbedPane.foreground",            TEXT_PRIMARY);
        UIManager.put("TabbedPane.selected",              BG_CARD);
        UIManager.put("TabbedPane.tabAreaBackground",     BG_DARK);
        UIManager.put("TabbedPane.contentAreaColor",      BG_DARK);
        UIManager.put("TabbedPane.unselectedBackground",  BG_DARK);
        UIManager.put("TabbedPane.selectedForeground",    TEXT_PRIMARY);
        UIManager.put("TabbedPane.tabInsets",             new Insets(8, 18, 8, 18));

        UIManager.put("TextField.background",   BG_INPUT);
        UIManager.put("TextField.foreground",   TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
        UIManager.put("TextArea.background",    BG_INPUT);
        UIManager.put("TextArea.foreground",    TEXT_PRIMARY);
        UIManager.put("TextArea.caretForeground", TEXT_PRIMARY);

        UIManager.put("ComboBox.background",            BG_INPUT);
        UIManager.put("ComboBox.foreground",            TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground",   ACCENT);
        UIManager.put("ComboBox.selectionForeground",   Color.WHITE);
        UIManager.put("ComboBox.buttonBackground",      BG_INPUT);

        UIManager.put("Table.background",           TABLE_EVEN);
        UIManager.put("Table.foreground",           TEXT_PRIMARY);
        UIManager.put("Table.gridColor",            BORDER_COLOR);
        UIManager.put("Table.selectionBackground",  new Color(99, 102, 241, 170));
        UIManager.put("Table.selectionForeground",  Color.WHITE);
        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());

        UIManager.put("TableHeader.background",     TABLE_HEADER_BG);
        UIManager.put("TableHeader.foreground",     TEXT_PRIMARY);

        UIManager.put("ScrollPane.background",      BG_DARK);
        UIManager.put("Viewport.background",        BG_DARK);
        UIManager.put("ScrollBar.background",       BG_CARD);
        UIManager.put("ScrollBar.thumb",            new Color(80, 84, 160));
        UIManager.put("ScrollBar.track",            BG_DARK);
        UIManager.put("ScrollBar.width",            8);

        UIManager.put("Label.foreground",           TEXT_PRIMARY);
        UIManager.put("Button.focus",               new Color(0, 0, 0, 0));

        UIManager.put("ToolTip.background",  BG_CARD);
        UIManager.put("ToolTip.foreground",  TEXT_PRIMARY);
        UIManager.put("ToolTip.border",      BorderFactory.createLineBorder(BORDER_COLOR));
    }

    // ── Factory: Styled Button ───────────────────────────────────────────────
    public static JButton createButton(String text, Color bg, Color hoverBg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setOpaque(false);
                setContentAreaFilled(false);
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited (MouseEvent e) { hovered = false; repaint(); }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? hoverBg : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(9, 22, 9, 22));
        return btn;
    }

    // ── Factory: Styled TextField ────────────────────────────────────────────
    public static JTextField createTextField() {
        JTextField f = new JTextField();
        f.setFont(FONT_BODY);
        f.setForeground(TEXT_PRIMARY);
        f.setBackground(BG_INPUT);
        f.setCaretColor(TEXT_PRIMARY);
        f.setOpaque(true);
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundedLineBorder(BORDER_COLOR, 1, 8),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return f;
    }

    // ── Factory: Styled JComboBox ────────────────────────────────────────────
    public static JComboBox<String> createComboBox(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_BODY);
        cb.setBackground(BG_INPUT);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        return cb;
    }

    // ── Factory: Styled Label ────────────────────────────────────────────────
    public static JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_BODY);
        lbl.setForeground(TEXT_SECONDARY);
        return lbl;
    }

    // ── Factory: Section Title ───────────────────────────────────────────────
    public static JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADER);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    // ── Factory: JScrollPane ─────────────────────────────────────────────────
    public static JScrollPane createScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(BG_DARK);
        sp.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        sp.getHorizontalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    // ── Factory: Styled JTable with alternating row colours ──────────────────
    public static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_BODY);
        table.setForeground(TEXT_PRIMARY);
        table.setBackground(TABLE_EVEN);
        table.setRowHeight(34);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(99, 102, 241, 170));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setBackground(TABLE_HEADER_BG);
        table.getTableHeader().setForeground(TEXT_PRIMARY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, selected, focus, row, col);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? TABLE_EVEN : TABLE_ODD);
                    c.setForeground(TEXT_PRIMARY);
                } else {
                    c.setBackground(new Color(99, 102, 241, 170));
                    c.setForeground(Color.WHITE);
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
        return table;
    }

    // ── Helper: Card Panel (rounded dark card) ───────────────────────────────
    public static JPanel createCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setBackground(BG_CARD);
        card.setOpaque(false);
        return card;
    }

    // ── Helper: Accent horizontal separator ─────────────────────────────────
    public static JPanel createSeparator() {
        JPanel sep = new JPanel();
        sep.setBackground(BORDER_COLOR);
        sep.setPreferredSize(new Dimension(0, 1));
        return sep;
    }

    // ── Factory: Search Panel ────────────────────────────────────────────────
    public static JPanel createSearchPanel(JTable table, DefaultTableModel model) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        p.setBackground(BG_DARK);

        JLabel iconLbl = new JLabel("🔍");
        iconLbl.setForeground(TEXT_SECONDARY);

        JTextField searchField = createTextField();
        searchField.setPreferredSize(new Dimension(220, 32));
        searchField.setToolTipText("Search across all columns...");

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { search(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { search(); }
            private void search() {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });

        p.add(iconLbl);
        p.add(searchField);
        return p;
    }

    // ── Inner class: Rounded line border ────────────────────────────────────
    static class RoundedLineBorder extends AbstractBorder {
        private final Color color;
        private final int   thickness;
        private final int   radius;

        RoundedLineBorder(Color color, int thickness, int radius) {
            this.color     = color;
            this.thickness = thickness;
            this.radius    = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c)                    { return new Insets(4,4,4,4); }
        @Override public Insets getBorderInsets(Component c, Insets i)         { i.set(4,4,4,4); return i; }
    }
}

package Calendar;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicButtonUI;

// Central place for colours, fonts and styled widgets so every platform looks the same
public final class Theme {

    public static final Color GREEN = Color.decode("#0ecf78");
    public static final Color PINK = Color.decode("#f90069");
    public static final Color TEAL = Color.decode("#00d1e8");
    public static final Color RED = Color.decode("#ff4444");

    private static boolean dark;

    private Theme() {}

    public static void setDark(boolean value) { dark = value; }
    public static boolean isDark() { return dark; }

    public static Color background() { return dark ? Color.decode("#1e1f22") : Color.WHITE; }
    public static Color cell()       { return dark ? Color.decode("#2b2d31") : Color.decode("#f0f0f0"); }
    public static Color text()       { return dark ? Color.decode("#e6e6e6") : Color.BLACK; }
    public static Color border()     { return dark ? Color.decode("#3a3c42") : Color.LIGHT_GRAY; }
    public static Color neutralButton() { return dark ? Color.decode("#3a3c42") : Color.decode("#e4e4e4"); }

    // Logical font: maps to Helvetica/Arial/DejaVu Sans etc. depending on the OS
    public static Font font(int style, int size) {
        return new Font(Font.SANS_SERIF, style, size);
    }

    // Some Linux/Windows fonts lack symbols like ◀ or ✎, so fall back to plain text
    public static String glyph(Font font, String preferred, String fallback) {
        return font.canDisplayUpTo(preferred) == -1 ? preferred : fallback;
    }

    // BasicButtonUI paints the background colour on every look and feel (Aqua ignores it otherwise)
    public static JButton button(String text, Color background, Color foreground, Font font) {
        JButton button = new JButton(text);
        button.setUI(new BasicButtonUI());
        button.setFont(font);
        button.setBackground(background);
        button.setForeground(foreground);
        button.setOpaque(background != null);
        button.setContentAreaFilled(background != null);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static JTextField textField(String text) {
        JTextField field = new JTextField(text);
        field.setFont(font(Font.PLAIN, 20));
        field.setHorizontalAlignment(JTextField.CENTER);
        field.setBackground(cell());
        field.setForeground(text());
        field.setCaretColor(text());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border()),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return field;
    }

    public static <T extends JComponent> T paint(T component) {
        component.setBackground(background());
        component.setForeground(text());
        return component;
    }
}

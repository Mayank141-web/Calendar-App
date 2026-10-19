package Calendar;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.JLabel;

public class DayLabel extends JLabel {

    private static final long serialVersionUID = 1L;
    private static final Font FONT = Theme.font(Font.PLAIN, 20);

    public DayLabel(String text, Color background, Color foreground, boolean btn) {
        super(text, JLabel.CENTER);
        setFont(FONT);
        setOpaque(true);
        setBackground(background);
        setForeground(foreground);
        if (btn) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}

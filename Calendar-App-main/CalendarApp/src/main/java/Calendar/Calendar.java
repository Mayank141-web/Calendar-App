package Calendar;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Calendar extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final String[] WEEK_DAYS = {"Su", "Mo", "Tu", "We", "Th", "Fr", "Sa"};
    private static final int TOTAL_CELLS = 42;

    public Calendar(int year, int month, App app) {
        //Layout
        setLayout(new BorderLayout(30, 30));
        setBorder(BorderFactory.createEmptyBorder(40, 20, 30, 20));
        setBackground(Theme.background());

        YearMonth shown = YearMonth.of(year, month);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setOpaque(false);

        //Month
        JLabel date = new JLabel(shown.format(MONTH_FORMAT), JLabel.CENTER);
        date.setFont(Theme.font(Font.BOLD, 30));
        date.setForeground(Theme.GREEN);
        top.add(date, BorderLayout.CENTER);

        // Previous / next month arrows
        top.add(arrow("◀", "<", shown.minusMonths(1), app), BorderLayout.WEST);
        top.add(arrow("▶", ">", shown.plusMonths(1), app), BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        //Days
        JPanel days = new JPanel(new GridLayout(7, 7));
        days.setOpaque(false);

        for (String weekDay : WEEK_DAYS) {
            days.add(new DayLabel(weekDay, Theme.PINK, Color.WHITE, false));
        }

        // DayOfWeek runs Monday=1 .. Sunday=7; the grid starts on Sunday
        int offset = shown.atDay(1).getDayOfWeek().getValue() % 7;
        for (int i = 0; i < offset; i++) days.add(emptyCell());

        LocalDate selectedDay = app.getSelectedDay();
        TaskManager taskManager = app.getTaskManager();
        int daysNum = shown.lengthOfMonth();

        for (int i = 1; i <= daysNum; i++) {
            LocalDate currentDate = shown.atDay(i);

            Color background;
            Color foreground = Color.BLACK;
            if (selectedDay.equals(currentDate)) {
                background = Theme.GREEN;
            } else if (taskManager.hasTasksForDate(currentDate)) {
                background = Theme.TEAL;
            } else {
                background = Theme.cell();
                foreground = Theme.text();
            }

            DayLabel dayLabel = new DayLabel(String.valueOf(i), background, foreground, true);
            dayLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    app.selectDay(currentDate);
                }
            });
            days.add(dayLabel);
        }

        // Fill remaining cells
        for (int i = offset + daysNum; i < TOTAL_CELLS; i++) days.add(emptyCell());

        add(days, BorderLayout.CENTER);
    }

    private static DayLabel emptyCell() {
        return new DayLabel("", Theme.cell(), Theme.text(), false);
    }

    private static JLabel arrow(String symbol, String fallback, YearMonth target, App app) {
        Font font = Theme.font(Font.BOLD, 20);
        JLabel arrow = new JLabel(Theme.glyph(font, symbol, fallback));
        arrow.setFont(font);
        arrow.setForeground(Theme.GREEN);
        arrow.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        arrow.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                app.showMonth(target.getYear(), target.getMonthValue());
            }
        });
        return arrow;
    }
}

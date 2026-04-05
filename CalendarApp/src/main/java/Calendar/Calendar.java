package Calendar;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Calendar extends JPanel {

    private static final long serialVersionUID = 1L;
    public Calendar(int year, int month, LocalDate selectedDay, JPanel mainPanel, TaskManager taskManager) {
        //Layout
        setLayout(new BorderLayout(30, 30));
        setBorder(BorderFactory.createEmptyBorder(40, 20, 30, 20));
        setBackground(Color.white);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBackground(null);

        //Month
        JLabel date = new JLabel(LocalDate.of(year, month, 1).format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        date.setHorizontalAlignment(JLabel.CENTER);
        date.setFont(new Font("Helvetica", Font.BOLD, 30));
        date.setForeground(Color.decode("#0ecf78"));
        top.add(date, BorderLayout.CENTER);

        // Left arrow (previous month)
        JLabel left = new JLabel("◀");
        left.setFont(new Font("Arial", Font.BOLD, 20));
        left.setForeground(Color.decode("#0ecf78"));
        left.setCursor(new Cursor(Cursor.HAND_CURSOR));
        left.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseClicked(MouseEvent e) {
                mainPanel.removeAll();
                if (month != 1) {
                    mainPanel.add(new Calendar(year, month - 1, selectedDay, mainPanel, taskManager));
                } else {
                    mainPanel.add(new Calendar(year - 1, 12, selectedDay, mainPanel, taskManager));
                }
                mainPanel.add(new Events(selectedDay, mainPanel, taskManager));
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
        top.add(left, BorderLayout.WEST);

        // Right arrow (next month)
        JLabel right = new JLabel("▶");
        right.setFont(new Font("Arial", Font.BOLD, 20));
        right.setForeground(Color.decode("#0ecf78"));
        right.setCursor(new Cursor(Cursor.HAND_CURSOR));
        right.addMouseListener(new MouseListener() {
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseClicked(MouseEvent e) {
                mainPanel.removeAll();
                if (month != 12) {
                    mainPanel.add(new Calendar(year, month + 1, selectedDay, mainPanel, taskManager));
                } else {
                    mainPanel.add(new Calendar(year + 1, 1, selectedDay, mainPanel, taskManager));
                }
                mainPanel.add(new Events(selectedDay, mainPanel, taskManager));
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        });
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        //Days
        Color header = Color.decode("#f90069");
        JPanel days = new JPanel(new GridLayout(7,7));
        days.setBackground(null);

        days.add(new DayLabel("Su", header, Color.white, false));
        days.add(new DayLabel("Mo", header, Color.white, false));
        days.add(new DayLabel("Tu", header, Color.white, false));
        days.add(new DayLabel("We", header, Color.white, false));
        days.add(new DayLabel("Th", header, Color.white, false));
        days.add(new DayLabel("Fr", header, Color.white, false));
        days.add(new DayLabel("Sa", header, Color.white, false));

        String[] weekDays = new String[] {"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};

        LocalDate firstDay = LocalDate.of(year, month, 1);

        int j = 0;
        while(!firstDay.getDayOfWeek().toString().equals(weekDays[j])) {
            days.add(new DayLabel("", Color.decode("#f0f0f0"), Color.black, false));
            j++;
        }

        int daysNum = YearMonth.of(year, month).lengthOfMonth();

        for (int i = 1; i <= daysNum; i++) {
            final int day = i;

            DayLabel dayLabel;
            LocalDate currentDate = LocalDate.of(year, month, i);

            if (selectedDay.equals(currentDate)) {
                dayLabel = new DayLabel(i + "", Color.decode("#0ecf78"), Color.black, true);
            } else if (taskManager.hasTasksForDate(currentDate)) {
                dayLabel = new DayLabel(i + "", Color.decode("#00d1e8"), Color.black, true);
            } else {
                dayLabel = new DayLabel(i + "", Color.decode("#f0f0f0"), Color.black, true);
            }

            dayLabel.addMouseListener(new MouseListener() {
                @Override
                public void mouseReleased(MouseEvent e) {}
                @Override
                public void mousePressed(MouseEvent e) {}
                @Override
                public void mouseExited(MouseEvent e) {}
                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseClicked(MouseEvent e) {
                    mainPanel.removeAll();
                    LocalDate selected = LocalDate.of(year, month, day);
                    mainPanel.add(new Calendar(year, month, selected, mainPanel, taskManager));
                    mainPanel.add(new Events(selected, mainPanel, taskManager));
                    mainPanel.revalidate();
                    mainPanel.repaint();
                }
            });

            days.add(dayLabel);
        }

        // Fill remaining cells
        int totalCells = 42;
        int usedCells = j + daysNum;
        for (int i = usedCells; i < totalCells; i++) {
            days.add(new DayLabel("", Color.decode("#f0f0f0"), Color.black, false));
        }

        add(days, BorderLayout.CENTER);
    }
}

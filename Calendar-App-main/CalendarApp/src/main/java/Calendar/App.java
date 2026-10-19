package Calendar;

import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.JFrame;
import javax.swing.JPanel;

// Owns the window contents and rebuilds both panels whenever the view changes
public class App {

    private final JFrame frame;
    private final TaskManager taskManager;
    private final JPanel mainPanel = new JPanel(new GridLayout(1, 2));
    private LocalDate selectedDay;
    private int year;
    private int month;

    public App(JFrame frame, TaskManager taskManager) {
        this.frame = frame;
        this.taskManager = taskManager;
        Theme.setDark(taskManager.isDarkMode());
        LocalDate today = LocalDate.now();
        selectedDay = today;
        year = today.getYear();
        month = today.getMonthValue();

        frame.getContentPane().removeAll();
        frame.getContentPane().add(mainPanel);
        refresh();
    }

    public JFrame getFrame() { return frame; }
    public TaskManager getTaskManager() { return taskManager; }
    public LocalDate getSelectedDay() { return selectedDay; }

    public void showMonth(int year, int month) {
        this.year = year;
        this.month = month;
        refresh();
    }

    public void selectDay(LocalDate day) {
        selectedDay = day;
        year = day.getYear();
        month = day.getMonthValue();
        refresh();
    }

    public void toggleDarkMode() {
        taskManager.setDarkMode(!taskManager.isDarkMode());
        Theme.setDark(taskManager.isDarkMode());
        refresh();
    }

    public void refresh() {
        frame.getContentPane().setBackground(Theme.background());
        mainPanel.setBackground(Theme.background());
        mainPanel.removeAll();
        mainPanel.add(new Calendar(year, month, this));
        mainPanel.add(new Events(this));
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}

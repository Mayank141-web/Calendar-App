package Calendar;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Calendar - Task Manager");
            frame.setSize(1000, 600);
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(Color.white);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel mainPanel = new JPanel(new GridLayout(1, 2));
            mainPanel.setBackground(Color.white);

            LocalDate date = LocalDate.now();

            TaskManager taskManager = TaskManager.loadData();

            mainPanel.add(new Calendar(date.getYear(), date.getMonthValue(), date, mainPanel, taskManager));
            mainPanel.add(new Events(date, mainPanel, taskManager));

            frame.getContentPane().add(mainPanel);
            frame.setVisible(true);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> taskManager.saveData()));
        });
    }
}

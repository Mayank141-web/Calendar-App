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

            // Loading screen
            JLabel loadingLabel = new JLabel("Searching for calendar data...", JLabel.CENTER);
            loadingLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
            loadingLabel.setForeground(Color.GRAY);
            frame.getContentPane().add(loadingLabel);
            frame.setVisible(true);

            // Search and load on background thread, then populate UI
            SwingWorker<TaskManager, Void> worker = new SwingWorker<>() {
                @Override
                protected TaskManager doInBackground() {
                    String path = TaskManager.findFile();
                    return TaskManager.loadData(path);
                }

                @Override
                protected void done() {
                    try {
                        TaskManager taskManager = get();
                        LocalDate date = LocalDate.now();

                        JPanel mainPanel = new JPanel(new GridLayout(1, 2));
                        mainPanel.setBackground(Color.white);
                        mainPanel.add(new Calendar(date.getYear(), date.getMonthValue(), date, mainPanel, taskManager));
                        mainPanel.add(new Events(date, mainPanel, taskManager));

                        frame.getContentPane().removeAll();
                        frame.getContentPane().add(mainPanel);
                        frame.revalidate();
                        frame.repaint();

                        Runtime.getRuntime().addShutdownHook(new Thread(taskManager::saveData));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            worker.execute();
        });
    }
}

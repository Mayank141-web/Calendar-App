package Calendar;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Must be set before any AWT class loads
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac")) {
            System.setProperty("apple.awt.application.name", "MyCalendar");
        } else if (!os.contains("win")) {
            // Linux/BSD: smooth fonts even when the desktop doesn't advertise its settings
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MyCalendar");
            frame.setSize(1100, 620);
            frame.setMinimumSize(new Dimension(900, 500));
            frame.setLocationRelativeTo(null);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            URL icon = Main.class.getResource("/icon.png");
            if (icon != null) frame.setIconImage(new ImageIcon(icon).getImage());

            // Loading screen
            JLabel loadingLabel = new JLabel("Searching for calendar data...", JLabel.CENTER);
            loadingLabel.setFont(Theme.font(Font.PLAIN, 16));
            loadingLabel.setForeground(Color.GRAY);
            frame.getContentPane().add(loadingLabel);
            frame.setVisible(true);

            // Search and load on background thread, then populate UI
            new SwingWorker<TaskManager, Void>() {
                @Override
                protected TaskManager doInBackground() {
                    return TaskManager.loadData(TaskManager.findFile());
                }

                @Override
                protected void done() {
                    TaskManager taskManager;
                    try {
                        taskManager = get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        taskManager = new TaskManager();
                    }
                    new App(frame, taskManager);
                    // Saves everything, including the current Light/Dark mode, when the app closes
                    Runtime.getRuntime().addShutdownHook(new Thread(taskManager::saveData));
                }
            }.execute();
        });
    }
}

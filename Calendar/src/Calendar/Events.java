package Calendar;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;

public class Events extends JPanel {

    private final LocalDate currentDate;
    private final TaskManager taskManager;
    private final JPanel tasksPanel;

    public Events(LocalDate date, JPanel mainPanel, TaskManager taskManager) {
        this.currentDate = date;
        this.taskManager = taskManager;

        setLayout(new BorderLayout());
        setBackground(Color.white);

        // ---- Header (title + buttons) ----
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        headerPanel.setBackground(Color.white);

        JLabel titleLabel = new JLabel("Tasks for " + currentDate);
        titleLabel.setFont(new Font("Helvetica", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);

        // Add Task button (green)
        JButton addButton = new JButton("+ Add Task");
        addButton.setFont(new Font("Helvetica", Font.BOLD, 14));
        addButton.setBackground(Color.decode("#0ecf78"));
        addButton.setOpaque(true);
        addButton.setBorderPainted(false);
        addButton.setFocusPainted(false);
        addButton.setForeground(Color.WHITE);
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e ->
                new TaskEditor(new Task(currentDate), taskManager, mainPanel, currentDate));

        // Save Tasks button (green)
        JButton saveButton = new JButton("Save Tasks");
        saveButton.setFont(new Font("Helvetica", Font.BOLD, 14));
        saveButton.setBackground(Color.decode("#0ecf78")); // green
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setForeground(Color.WHITE);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> {
            taskManager.saveData();
            JOptionPane.showMessageDialog(
                    mainPanel,
                    "All tasks have been saved to calendar_data.ser",
                    "Save Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });

        // Left side: title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setBackground(Color.white);
        leftPanel.add(titleLabel);

        // Right side: buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.white);
        rightPanel.add(saveButton);
        rightPanel.add(addButton);

        // Combine
        headerPanel.setLayout(new BorderLayout());
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        // ---- Tasks list ----
        tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksPanel.setBackground(Color.white);

        refreshTasks();

        add(headerPanel, BorderLayout.NORTH);
        add(new JScrollPane(tasksPanel), BorderLayout.CENTER);
    }

    private void refreshTasks() {
        tasksPanel.removeAll();
        List<Task> tasks = taskManager.getTasksForDate(currentDate);

        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tasks for this date.");
            emptyLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setForeground(Color.decode("#00d1e8")); // light blue for tasks
            tasksPanel.add(emptyLabel);
        } else {
            for (Task task : tasks) {
                JPanel row = new JPanel(new BorderLayout());
                row.setBackground(Color.white);
                row.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

                JLabel taskLabel = new JLabel(task.toString());
                taskLabel.setFont(new Font("Helvetica", Font.PLAIN, 16));
                taskLabel.setForeground(Color.decode("#00d1e8")); // light blue for tasks

                JButton deleteButton = new JButton("×");
                deleteButton.setFont(new Font("Helvetica", Font.BOLD, 16));
                deleteButton.setForeground(Color.decode("#0ecf78")); // green buttons
                deleteButton.setOpaque(false);
                deleteButton.setContentAreaFilled(false);
                deleteButton.setBorderPainted(false);
                deleteButton.setFocusPainted(false);
                deleteButton.addActionListener(e -> {
                    taskManager.removeTask(task);
                    refreshTasks();
                });

                row.add(taskLabel, BorderLayout.CENTER);
                row.add(deleteButton, BorderLayout.EAST);
                tasksPanel.add(row);
            }
        }

        tasksPanel.revalidate();
        tasksPanel.repaint();
    }
}

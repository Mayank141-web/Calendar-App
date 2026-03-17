package Calendar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import javax.swing.*;

public class TaskEditor {

    private final TaskManager taskManager;
    private final Task task;
    private final JPanel mainPanel;
    private final LocalDate currentDate;
    private JFrame frame;

    public TaskEditor(Task task, TaskManager taskManager, JPanel mainPanel, LocalDate currentDate) {
        this.task = task;
        this.taskManager = taskManager;
        this.mainPanel = mainPanel;
        this.currentDate = currentDate;

        createEditor();
    }

    private void createEditor() {
        frame = new JFrame(task.getId() == 0 ? "Add New Task" : "Edit Task");
        frame.setSize(700, 350);
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Color.white);

        JPanel editorPanel = new JPanel(new BorderLayout(20, 20));
        editorPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        editorPanel.setBackground(Color.white);

        JPanel center = new JPanel(new GridLayout(3, 2, 20, 20));
        center.setBackground(Color.white);

        JLabel titleLabel = new JLabel("Title:");
        titleLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        center.add(titleLabel);

        JTextField titleField = new JTextField(task.getTitle() != null ? task.getTitle() : "");
        titleField.setFont(new Font("Helvetica", Font.PLAIN, 20));
        titleField.setHorizontalAlignment(JTextField.CENTER);
        center.add(titleField);

        JLabel timeLabel = new JLabel("Time (HH:mm):");
        timeLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
        timeLabel.setHorizontalAlignment(JLabel.CENTER);
        center.add(timeLabel);

        JTextField timeField = new JTextField(task.getFormattedTime());
        timeField.setFont(new Font("Helvetica", Font.PLAIN, 20));
        timeField.setHorizontalAlignment(JTextField.CENTER);
        center.add(timeField);

        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
        descLabel.setHorizontalAlignment(JLabel.CENTER);
        center.add(descLabel);

        JTextField descField = new JTextField(task.getDescription() != null ? task.getDescription() : "");
        descField.setFont(new Font("Helvetica", Font.PLAIN, 20));
        descField.setHorizontalAlignment(JTextField.CENTER);
        center.add(descField);

        editorPanel.add(center, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        buttonPanel.setBackground(Color.white);

        // Delete button (only if editing existing task)
        if (task.getId() != 0) {
            JButton deleteButton = new JButton("Delete");
            deleteButton.setFont(new Font("Helvetica", Font.PLAIN, 20));
            deleteButton.setBackground(Color.decode("#ff4444"));
            deleteButton.setForeground(Color.white);
            deleteButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            deleteButton.addActionListener((ActionEvent e) -> {
                taskManager.removeTask(task);
                refreshMainPanel();
                frame.dispose();
            });
            buttonPanel.add(deleteButton);
        } else {
            buttonPanel.add(new JLabel()); // Empty space
        }

        // Cancel button (fully black background + black text)
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Helvetica", Font.PLAIN, 20));
        cancelButton.setBackground(Color.BLACK);
        cancelButton.setForeground(Color.BLACK);
        cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        cancelButton.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        buttonPanel.add(cancelButton);

        // Save button (fully black background + black text)
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Helvetica", Font.PLAIN, 20));
        saveButton.setBackground(Color.BLACK);
        saveButton.setForeground(Color.BLACK);
        saveButton.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        saveButton.addActionListener((ActionEvent e) -> {
            saveTask(titleField.getText(), timeField.getText(), descField.getText());
        });
        buttonPanel.add(saveButton);

        editorPanel.add(buttonPanel, BorderLayout.SOUTH);

        frame.getContentPane().add(editorPanel);
        frame.setVisible(true);
    }

    private void saveTask(String title, String timeStr, String description) {
        if (title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a title for the task.");
            return;
        }

        try {
            LocalTime time = LocalTime.parse(timeStr.trim());

            task.setTitle(title.trim());
            task.setDescription(description.trim());
            task.setDateTime(LocalDateTime.of(currentDate, time));

            if (task.getId() == 0) {
                taskManager.addTask(task);
            } else {
                taskManager.updateTask(task);
            }

            refreshMainPanel();
            frame.dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid time in HH:mm format (e.g., 14:30)");
        }
    }

    private void refreshMainPanel() {
        mainPanel.removeAll();
        mainPanel.add(new Calendar(currentDate.getYear(), currentDate.getMonthValue(),
                currentDate, mainPanel, taskManager));
        mainPanel.add(new Events(currentDate, mainPanel, taskManager));
        mainPanel.revalidate();
        mainPanel.repaint();
    }
}

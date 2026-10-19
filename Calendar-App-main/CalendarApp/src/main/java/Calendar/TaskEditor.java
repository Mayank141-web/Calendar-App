package Calendar;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import javax.swing.*;

// Modal dialog for creating and editing tasks
public class TaskEditor {

    private static final Font FONT = Theme.font(Font.PLAIN, 20);

    private final Task task;
    private final App app;
    private final JDialog dialog;

    public TaskEditor(Task task, App app) {
        this.task = task;
        this.app = app;
        boolean isNew = task.getId() == 0;

        dialog = new JDialog(app.getFrame(), isNew ? "Add New Task" : "Edit Task", true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(700, 350);
        dialog.setLocationRelativeTo(app.getFrame());

        JPanel editorPanel = new JPanel(new BorderLayout(20, 20));
        editorPanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));
        editorPanel.setBackground(Theme.background());

        JPanel center = new JPanel(new GridLayout(3, 2, 20, 20));
        center.setOpaque(false);

        JTextField titleField = Theme.textField(task.getTitle() != null ? task.getTitle() : "");
        JTextField timeField = Theme.textField(task.getFormattedTime());
        JTextField descField = Theme.textField(task.getDescription() != null ? task.getDescription() : "");

        center.add(label("Title:"));
        center.add(titleField);
        center.add(label("Time (HH:mm):"));
        center.add(timeField);
        center.add(label("Description:"));
        center.add(descField);

        editorPanel.add(center, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        buttonPanel.setOpaque(false);

        // Delete button (only if editing existing task)
        if (!isNew) {
            JButton deleteButton = Theme.button("Delete", Theme.RED, Color.WHITE, FONT);
            deleteButton.addActionListener(e -> {
                app.getTaskManager().removeTask(task);
                close();
            });
            buttonPanel.add(deleteButton);
        } else {
            buttonPanel.add(new JLabel()); // Empty space
        }

        JButton cancelButton = Theme.button("Cancel", Theme.neutralButton(), Theme.text(), FONT);
        cancelButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(cancelButton);

        JButton saveButton = Theme.button("Save", Theme.GREEN, Color.WHITE, FONT);
        saveButton.addActionListener(e ->
                saveTask(titleField.getText(), timeField.getText(), descField.getText()));
        buttonPanel.add(saveButton);

        editorPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.getRootPane().setDefaultButton(saveButton); // Enter saves
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        dialog.setContentPane(editorPanel);
        dialog.setVisible(true);
    }

    private static JLabel label(String text) {
        JLabel label = Theme.paint(new JLabel(text, JLabel.CENTER));
        label.setFont(FONT);
        return label;
    }

    private void saveTask(String title, String timeStr, String description) {
        if (title.trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Please enter a title for the task.");
            return;
        }

        LocalTime time;
        try {
            time = LocalTime.parse(timeStr.trim());
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(dialog, "Please enter a valid time in HH:mm format (e.g., 14:30)");
            return;
        }

        task.setTitle(title.trim());
        task.setDescription(description.trim());
        task.setDateTime(LocalDateTime.of(task.getDate(), time));

        if (task.getId() == 0) {
            app.getTaskManager().addTask(task);
        } else {
            app.getTaskManager().updateTask(task);
        }
        close();
    }

    private void close() {
        dialog.dispose();
        app.refresh();
    }
}

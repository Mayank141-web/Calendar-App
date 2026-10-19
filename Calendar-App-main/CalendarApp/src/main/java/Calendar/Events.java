package Calendar;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;

public class Events extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final Font TASK_FONT = Theme.font(Font.PLAIN, 16);
    private static final Font ICON_FONT = Theme.font(Font.BOLD, 16);

    public Events(App app) {
        LocalDate currentDate = app.getSelectedDay();
        TaskManager taskManager = app.getTaskManager();

        setLayout(new BorderLayout());
        setBackground(Theme.background());

        // ---- Header: title on the left, buttons on the right ----
        JLabel titleLabel = Theme.paint(new JLabel("Tasks for " + currentDate));
        titleLabel.setFont(Theme.font(Font.BOLD, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);

        Font buttonFont = Theme.font(Font.BOLD, 14);

        // Light / Dark mode toggle
        JButton themeButton = Theme.button(Theme.isDark() ? "Light Mode" : "Dark Mode",
                Theme.neutralButton(), Theme.text(), buttonFont);
        themeButton.setToolTipText("Switch between Light Mode and Dark Mode");
        themeButton.addActionListener(e -> app.toggleDarkMode());

        // Add Task button (green)
        JButton addButton = Theme.button("+ Add Task", Theme.GREEN, Color.WHITE, buttonFont);
        addButton.addActionListener(e -> new TaskEditor(new Task(currentDate), app));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        rightPanel.setOpaque(false);
        rightPanel.add(themeButton);
        rightPanel.add(addButton);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        // ---- Tasks list ----
        JPanel tasksPanel = new JPanel();
        tasksPanel.setLayout(new BoxLayout(tasksPanel, BoxLayout.Y_AXIS));
        tasksPanel.setBackground(Theme.background());

        List<Task> tasks = taskManager.getTasksForDate(currentDate);
        if (tasks.isEmpty()) {
            JLabel emptyLabel = new JLabel("No tasks for this date.");
            emptyLabel.setFont(TASK_FONT);
            emptyLabel.setForeground(Theme.TEAL);
            emptyLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            tasksPanel.add(emptyLabel);
        } else {
            String editIcon = Theme.glyph(ICON_FONT, "✎", "Edit");
            String deleteIcon = Theme.glyph(ICON_FONT, "×", "X");
            for (Task task : tasks) {
                tasksPanel.add(taskRow(task, app, editIcon, deleteIcon));
            }
        }

        // Keep rows at the top instead of stretching to fill the panel
        JPanel listHolder = new JPanel(new BorderLayout());
        listHolder.setBackground(Theme.background());
        listHolder.add(tasksPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(listHolder);
        scrollPane.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.border()));
        scrollPane.getViewport().setBackground(Theme.background());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(headerPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private static JPanel taskRow(Task task, App app, String editIcon, String deleteIcon) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(Theme.background());
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.border()),
                BorderFactory.createEmptyBorder(4, 10, 4, 4)));

        JLabel taskLabel = new JLabel(task.toString());
        taskLabel.setFont(TASK_FONT);
        taskLabel.setForeground(Theme.TEAL);
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            taskLabel.setToolTipText(task.getDescription());
        }

        JButton editButton = Theme.button(editIcon, null, Theme.GREEN, ICON_FONT);
        editButton.setToolTipText("Edit task");
        editButton.addActionListener(e -> new TaskEditor(task, app));

        JButton deleteButton = Theme.button(deleteIcon, null, Theme.GREEN, ICON_FONT);
        deleteButton.setToolTipText("Delete task");
        deleteButton.addActionListener(e -> {
            app.getTaskManager().removeTask(task);
            app.refresh(); // also updates the calendar highlight
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        row.add(taskLabel, BorderLayout.CENTER);
        row.add(buttonPanel, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }
}

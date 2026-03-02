package Calendar;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TaskManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE = "calendar_data.ser";
    private final Map<String, List<Task>> tasksByDate;
    private int nextTaskId;

    public TaskManager() {
        tasksByDate = new HashMap<>();
        nextTaskId = 1;
    }

    public void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(this);
            System.out.println("Data saved successfully to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public static TaskManager loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return new TaskManager();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            return (TaskManager) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return new TaskManager();
        }
    }

    public void addTask(Task task) {
        task.setId(nextTaskId++);
        String key = task.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        tasksByDate.computeIfAbsent(key, k -> new ArrayList<>()).add(task);
        saveData();
    }

    public List<Task> getTasksForDate(LocalDate date) {
        String key = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return tasksByDate.getOrDefault(key, new ArrayList<>());
    }

    public boolean hasTasksForDate(LocalDate date) {
        String key = date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        List<Task> tasks = tasksByDate.get(key);
        return tasks != null && !tasks.isEmpty();
    }

    public void removeTask(Task task) {
        String key = task.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        List<Task> tasks = tasksByDate.get(key);
        if (tasks != null) {
            tasks.removeIf(t -> t.getId() == task.getId());
            if (tasks.isEmpty()) tasksByDate.remove(key);
            saveData();
        }
    }

    public void updateTask(Task updatedTask) {
        removeTaskById(updatedTask.getId());
        String key = updatedTask.getDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        tasksByDate.computeIfAbsent(key, k -> new ArrayList<>()).add(updatedTask);
        saveData();
    }

    private void removeTaskById(int taskId) {
        for (List<Task> tasks : tasksByDate.values()) tasks.removeIf(t -> t.getId() == taskId);
        tasksByDate.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    public Map<String, List<Task>> getAllTasks() { return new HashMap<>(tasksByDate); }
}

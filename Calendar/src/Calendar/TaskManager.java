package Calendar;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;

public class TaskManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILENAME = "calendar_data.ser";
    private static final String PREF_KEY = "calendar_data_path";
    private static final String DEFAULT_PATH = System.getProperty("user.home") + "/Downloads/" + FILENAME;
    private final Map<String, List<Task>> tasksByDate;
    private int nextTaskId;

    public TaskManager() {
        tasksByDate = new HashMap<>();
        nextTaskId = 1;
    }

    // Skips dirs that are inaccessible, system-level, or guaranteed irrelevant
    private static final Set<String> SKIP_DIRS = new HashSet<>(Arrays.asList(
        "proc", "sys", "dev", "run", "tmp", "private", "cores",
        "node_modules", ".git", ".Trash", "Trash",
        "Library", "System", "Volumes", "bin", "sbin", "usr", "etc", "var"
    ));

    public static String findFile() {
        Preferences prefs = Preferences.userNodeForPackage(TaskManager.class);

        // 1. Remembered path
        String saved = prefs.get(PREF_KEY, null);
        if (saved != null && new File(saved).exists()) return saved;

        // 2. Quick common locations
        String home = System.getProperty("user.home");
        for (String path : Arrays.asList(
            home + "/Downloads/" + FILENAME,
            home + "/Desktop/" + FILENAME,
            home + "/Documents/" + FILENAME,
            home + "/" + FILENAME
        )) {
            if (new File(path).exists()) {
                prefs.put(PREF_KEY, path);
                return path;
            }
        }

        // 3. Full recursive search from home directory
        String[] result = {null};
        try {
            Files.walkFileTree(Paths.get(home), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (SKIP_DIRS.contains(dir.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.getFileName().toString().equals(FILENAME)) {
                        result[0] = file.toAbsolutePath().toString();
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.SKIP_SUBTREE; // silently skip inaccessible files/dirs
                }
            });
        } catch (IOException e) {
            System.err.println("Search error: " + e.getMessage());
        }

        if (result[0] != null) {
            prefs.put(PREF_KEY, result[0]);
            return result[0];
        }

        // 4. Not found anywhere — use Downloads as default (created on next save)
        prefs.put(PREF_KEY, DEFAULT_PATH);
        return null;
    }

    public void saveData() {
        Preferences prefs = Preferences.userNodeForPackage(TaskManager.class);
        String path = prefs.get(PREF_KEY, DEFAULT_PATH);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            oos.writeObject(this);
            System.out.println("Data saved to " + path);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public static TaskManager loadData(String path) {
        if (path == null || !new File(path).exists()) return new TaskManager();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
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

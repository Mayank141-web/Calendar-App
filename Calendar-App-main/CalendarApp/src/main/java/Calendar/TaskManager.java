package Calendar;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.prefs.Preferences;

// Class name, field names and serialVersionUID must stay the same so existing calendar_data.ser files still load.
// New fields (like darkMode) are safe: older files simply load them with their default value.
public class TaskManager implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String FILENAME = "calendar_data.ser";
    private static final String PREF_KEY = "calendar_data_path";
    private static final DateTimeFormatter KEY_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final Comparator<Task> BY_TIME = Comparator.comparing(Task::getDateTime);
    private static final Path HOME = Paths.get(System.getProperty("user.home"));

    private final Map<String, List<Task>> tasksByDate;
    private int nextTaskId;
    private boolean darkMode;

    // Skips dirs that are inaccessible, system-level, or guaranteed irrelevant (macOS, Linux and Windows)
    private static final Set<String> SKIP_DIRS = new HashSet<>(Arrays.asList(
        "proc", "sys", "dev", "run", "tmp", "private", "cores",
        "node_modules", ".git", ".Trash", "Trash", ".cache", ".npm", ".gradle", ".m2",
        "Library", "System", "Volumes", "bin", "sbin", "usr", "etc", "var", "snap",
        "AppData", "Application Data", "Local Settings", "$Recycle.Bin"
    ));

    public TaskManager() {
        tasksByDate = new HashMap<>();
        nextTaskId = 1;
    }

    private static Preferences prefs() {
        return Preferences.userNodeForPackage(TaskManager.class);
    }

    // ~/Downloads isn't guaranteed to exist (e.g. minimal Linux installs), so fall back to Documents, then home
    private static Path defaultPath() {
        for (String dir : new String[] {"Downloads", "Documents"}) {
            Path candidate = HOME.resolve(dir);
            if (Files.isDirectory(candidate)) return candidate.resolve(FILENAME);
        }
        return HOME.resolve(FILENAME);
    }

    public static String findFile() {
        Preferences prefs = prefs();

        // 1. Remembered path
        String saved = prefs.get(PREF_KEY, null);
        if (saved != null && Files.exists(Paths.get(saved))) return saved;

        // 2. Quick common locations
        for (String dir : new String[] {"Downloads", "Desktop", "Documents", ""}) {
            Path path = HOME.resolve(dir).resolve(FILENAME);
            if (Files.exists(path)) {
                prefs.put(PREF_KEY, path.toString());
                return path.toString();
            }
        }

        // 3. Full recursive search from home directory
        String[] result = {null};
        try {
            Files.walkFileTree(HOME, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    Path name = dir.getFileName();
                    if (name != null && SKIP_DIRS.contains(name.toString())) return FileVisitResult.SKIP_SUBTREE;
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
                    return FileVisitResult.CONTINUE; // silently skip inaccessible files/dirs
                }
            });
        } catch (IOException e) {
            System.err.println("Search error: " + e.getMessage());
        }

        if (result[0] != null) {
            prefs.put(PREF_KEY, result[0]);
            return result[0];
        }

        // 4. Not found anywhere — use the default location (created on next save)
        prefs.put(PREF_KEY, defaultPath().toString());
        return null;
    }

    // Synchronized because the shutdown hook may save while the UI thread is saving.
    // Writes to a temp file first so a crash mid-save can't corrupt the existing data.
    public synchronized void saveData() {
        Path path = Paths.get(prefs().get(PREF_KEY, defaultPath().toString()));
        Path temp = path.resolveSibling(FILENAME + ".tmp");
        try {
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(temp)))) {
                oos.writeObject(this);
            }
            try {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.println("Data saved to " + path);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    public static TaskManager loadData(String path) {
        if (path == null || !Files.exists(Paths.get(path))) return new TaskManager();
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(Paths.get(path))))) {
            return (TaskManager) ois.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            System.err.println("Error loading data: " + e.getMessage());
            // Keep a copy so the unreadable file isn't lost when the next save overwrites it
            try {
                Files.copy(Paths.get(path), Paths.get(path + ".bak"), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {}
            return new TaskManager();
        }
    }

    private static String key(LocalDate date) {
        return date.format(KEY_FORMAT);
    }

    public boolean isDarkMode() { return darkMode; }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        saveData();
    }

    public void addTask(Task task) {
        task.setId(nextTaskId++);
        tasksByDate.computeIfAbsent(key(task.getDate()), k -> new ArrayList<>()).add(task);
        saveData();
    }

    // Returned sorted by time so the task list reads in order
    public List<Task> getTasksForDate(LocalDate date) {
        List<Task> tasks = tasksByDate.get(key(date));
        if (tasks == null) return Collections.emptyList();
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort(BY_TIME);
        return sorted;
    }

    public boolean hasTasksForDate(LocalDate date) {
        List<Task> tasks = tasksByDate.get(key(date));
        return tasks != null && !tasks.isEmpty();
    }

    public void removeTask(Task task) {
        String key = key(task.getDate());
        List<Task> tasks = tasksByDate.get(key);
        if (tasks != null && tasks.removeIf(t -> t.getId() == task.getId())) {
            if (tasks.isEmpty()) tasksByDate.remove(key);
            saveData();
        }
    }

    public void updateTask(Task updatedTask) {
        removeTaskById(updatedTask.getId());
        tasksByDate.computeIfAbsent(key(updatedTask.getDate()), k -> new ArrayList<>()).add(updatedTask);
        saveData();
    }

    private void removeTaskById(int taskId) {
        tasksByDate.values().removeIf(tasks -> {
            tasks.removeIf(t -> t.getId() == taskId);
            return tasks.isEmpty();
        });
    }

    public Map<String, List<Task>> getAllTasks() { return new HashMap<>(tasksByDate); }
}

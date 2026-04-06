# 📅 Calendar App

A desktop calendar and task manager built with **Java Swing**. Browse months, click on any day, and manage tasks with titles, times, and descriptions — all saved automatically between sessions.

---

## Features

- **Monthly calendar view** with previous/next month navigation
- **Per-day task panel** showing all tasks for the selected date
- **Add, edit, and delete tasks** via a clean modal editor (title, time, description)
- **Visual indicators** on the calendar: days with tasks are highlighted in teal
- **Automatic persistence** — data is serialized to `calendar_data.ser` and saved on exit via a JVM shutdown hook
- **Smart file discovery** — on first launch, searches common locations (Downloads, Desktop, Documents, home) before doing a full recursive scan, and remembers the path via Java Preferences for future runs

---

## Project Structure

```
CalendarApp/
└── src/main/java/Calendar/
    ├── Main.java          # Entry point; builds the JFrame and loads data on a background thread
    ├── Calendar.java      # Left panel: interactive monthly grid
    ├── Events.java        # Right panel: task list for the selected day
    ├── TaskEditor.java    # Modal dialog for creating and editing tasks
    ├── TaskManager.java   # Data layer: CRUD operations + file save/load/search
    ├── Task.java          # Serializable task model (title, description, dateTime)
    ├── Event.java         # Event model (legacy / unused in current UI)
    └── DayLabel.java      # Styled JLabel used for calendar day cells
```

---

## Requirements

- **Java 11** or later (uses `var`-free code, but relies on `java.time` and `SwingWorker`)
- No external dependencies — pure Java SE

---

## Building & Running

### From the command line

```bash
# Compile
javac -d out src/main/java/Calendar/*.java

# Run
java -cp out Calendar.Main
```

### From an IDE (IntelliJ / Eclipse)

1. Open the `CalendarApp` directory as a project.
2. Set `Calendar.Main` as the run configuration entry point.
3. Run.

### Packaging as a native app (macOS)

You can bundle it as a `.app` using `jpackage` (JDK 14+):

```bash
jpackage \
  --input out \
  --name CalendarApp \
  --main-jar CalendarApp.jar \
  --main-class Calendar.Main \
  --type app-image
```

---

## Data Storage

Tasks are persisted to a binary serialization file named `calendar_data.ser`. On launch, the app searches for this file in the following order:

1. The path remembered from the last session (stored via `java.util.prefs.Preferences`)
2. `~/Downloads/`, `~/Desktop/`, `~/Documents/`, `~/`
3. A full recursive walk of the home directory (skipping system directories)
4. If not found, defaults to `~/Downloads/calendar_data.ser` (created on first save)

Data is saved automatically on every add/edit/delete, and again on application exit via a shutdown hook.

---

## License

MIT

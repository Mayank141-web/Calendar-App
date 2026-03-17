# Calendar-App

A simple desktop calendar and task manager built in Java Swing. Create, edit, and delete tasks for any date — all data is saved automatically.

![Calendar App Screenshot](screenshot.png)

---

## Download & Install

1. Go to [Releases](../../releases)
2. Download `MyCalendar.dmg`
3. Open the `.dmg` and drag **MyCalendar** into your Applications folder
4. Open it from Applications — no Java installation required

> **Note:** On first launch macOS may warn you the app is from an unidentified developer. Go to **System Settings → Privacy & Security** and click **Open Anyway**.

---

## Usage

- Click any date on the calendar to view its tasks
- Click **+ Add Task** to create a new task for that date
- Click **✎** next to a task to edit it
- Click **×** next to a task to delete it
- Tasks are saved automatically to your Downloads folder

---

## Requirements

- macOS (the `.dmg` release bundles everything, no Java needed)

---

## Building from Source

Make sure you have JDK 14+ installed, then run:

```bash
cd Calendar
javac -d out src/Calendar/*.java
jar --create --file MyCalendar.jar --main-class Calendar.Main -C out .
jpackage --input . --main-jar MyCalendar.jar --name "MyCalendar" --type dmg --dest ~/Desktop
```

The `.dmg` will appear on your Desktop.

---

## License

MIT

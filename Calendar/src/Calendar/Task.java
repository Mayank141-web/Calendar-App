package Calendar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Task implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String title;
    private String description;
    private LocalDateTime dateTime;

    public Task() {
        this.dateTime = LocalDateTime.now();
    }

    public Task(String title, String description, LocalDateTime dateTime) {
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
    }

    public Task(LocalDate date) {
        this.dateTime = LocalDateTime.of(date, LocalTime.of(9, 0)); // Default to 9:00 AM
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public LocalDate getDate() { return dateTime.toLocalDate(); }

    public String getFormattedDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm"));
    }

    public String getFormattedTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public void setTimeFromString(String time) {
        try {
            LocalTime parsedTime = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            this.dateTime = LocalDateTime.of(dateTime.toLocalDate(), parsedTime);
        } catch (Exception e) {
            System.err.println("Error parsing time: " + time);
        }
    }

    @Override
    public String toString() {
        return title + " (" + getFormattedDateTime() + ")";
    }
}

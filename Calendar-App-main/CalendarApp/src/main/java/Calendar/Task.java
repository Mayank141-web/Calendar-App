package Calendar;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

// Field names and serialVersionUID must stay the same so existing calendar_data.ser files still load
public class Task implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private int id;
    private String title;
    private String description;
    private LocalDateTime dateTime;

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

    public String getFormattedDateTime() { return dateTime.format(DATE_TIME_FORMAT); }

    public String getFormattedTime() { return dateTime.format(TIME_FORMAT); }

    @Override
    public String toString() {
        return title + " (" + getFormattedDateTime() + ")";
    }
}

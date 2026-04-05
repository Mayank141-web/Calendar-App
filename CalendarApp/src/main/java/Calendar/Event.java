package Calendar;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Event {
    private int ID;
    private String title;
    private String description;
    private LocalDateTime dateTime;

    public Event(int ID, String title, String description, LocalDateTime dateTime) {
        this.ID = ID;
        this.title = title;
        this.description = description;
        this.dateTime = dateTime;
    }

    public Event(LocalDate date) {
        dateTime = LocalDateTime.of(date, LocalTime.now());
    }

    public Event() {}

    public void setID(int ID) { this.ID = ID; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public int getID() { return ID; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }

    public String getDateTime() {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm"));
    }

    public String getTimetoString() {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public void setDateTimeFromString(String dateTime) {
        this.dateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("dd-MM-yyyy | HH:mm"));
    }

    public void setTime(String time) {
        this.dateTime = LocalDateTime.of(dateTime.toLocalDate(),
                LocalTime.parse(time, DateTimeFormatter.ofPattern("HH: mm")));
    }

    public LocalDate getDate() { return dateTime.toLocalDate(); }

    public String getDateTimeToString() { return null; }
}

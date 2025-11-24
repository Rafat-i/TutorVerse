package models;

public class ScheduleMeeting {
    String tutorName;
    String subject;
    String time;
    boolean isBooked;

    public int durationHours = 1;
    public double rating = 0.0;

    String tutorUid;
    String day;
    String startTime;
    String takenBy;

    public ScheduleMeeting(String tutorName, String subject, String time, boolean isBooked) {
        this.tutorName = tutorName;
        this.subject = subject;
        this.time = time;
        this.isBooked = isBooked;
    }

    public String getTutorName() { return tutorName; }
    public String getSubject() { return subject; }
    public String getTime() { return time; }
    public boolean isBooked() { return isBooked; }
    public void setBooked(boolean booked) { isBooked = booked; }

    public String getTutorUid() { return tutorUid; }
    public void setTutorUid(String tutorUid) { this.tutorUid = tutorUid; }

    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getTakenBy() { return takenBy; }
    public void setTakenBy(String takenBy) { this.takenBy = takenBy; }
}
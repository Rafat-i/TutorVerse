package models;

import androidx.annotation.NonNull;

public class ScheduleMeeting {

    private String tutorName;
    private String subject;
    private String time;
    private boolean booked;

    public ScheduleMeeting() { }

    public ScheduleMeeting(String tutorName, String subject, String time, boolean booked) {
        this.tutorName = tutorName;
        this.subject = subject;
        this.time = time;
        this.booked = booked;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    @NonNull
    @Override
    public String toString() {
        return tutorName + " - " + subject + " (" + time + ")";
    }
}

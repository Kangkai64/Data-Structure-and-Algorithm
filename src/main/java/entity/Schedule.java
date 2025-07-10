package entity;

/*
 * @author Lee Yong Kang
 */

public class Schedule {
    private String scheduleId;
    private String doctorId;
    private DayOfWeek dayOfWeek;
    private String fromTime;
    private String toTime;
    private boolean isAvailable;

    public Schedule (String scheduleId, String doctorId, DayOfWeek dayOfWeek,
                     String fromTime, String toTime) {
        this.scheduleId = scheduleId;
        this.doctorId = doctorId;
        this.dayOfWeek = dayOfWeek;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.isAvailable = true;
    }

    public String getScheduleId() { return  scheduleId; }
    public void setScheduleId(String scheduleId) { this.scheduleId = scheduleId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public String getFromTime() { return fromTime; }
    public void setFromTime(String fromTime) { this.fromTime = fromTime; }

    public String getToTime() { return  toTime; }
    public void setToTime(String toTime) { this.toTime = toTime; }

    public boolean isAvailable() { return  isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public String toString() {
        return "Schedule ID = " + scheduleId + "\n"
                + ", Doctor ID = " + doctorId + "\n"
                + ", Day of week = " + dayOfWeek + "\n"
                + ", From time = " + fromTime + "\n"
                + ", To time = " + toTime + "\n"
                + ", Status = " + isAvailable;
    }
}

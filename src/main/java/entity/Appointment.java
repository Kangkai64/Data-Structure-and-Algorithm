package entity;

public class Appointment {
    private String patientId;
    private String doctorId;
    private String appointmentDate;
    private String appointmentTime;
    private String appointmentType;
    private String status;
    private String remarks;

    public Appointment (String patientId, String doctorId, String appointmentDate,
                        String appointmentTime, String appointmentType, String status, String remarks) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.appointmentType = appointmentType;
        this.status = "SCHEDULED";
        this.remarks = "";
    }

    public String getPatientId() { return  patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(String appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getAppointmentTime() { return appointmentTime; }
    public void setAppointmentTime(String appointmentTime) { this.appointmentTime = appointmentTime; }

    public String getAppointmentType() { return appointmentType; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return  remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public String toString() {
        return "Patient ID = " + patientId + "\n"
                + ", Doctor ID = " + doctorId + "\n"
                + ", Appointment Date = " + appointmentDate + "\n"
                + ", Appointment Time = " + appointmentTime + "\n"
                + ", Appointment Type = " + appointmentType + "\n"
                + ", Status = " + status + "\n"
                + ", Remarks = " + remarks;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(patientId.replaceAll("[^0-9]", ""));
    }
}

package entity;

/*
 * @author Lee Yong Kang
 */

import adt.ArrayBucketList;
import java.util.Date;

public class Doctor extends Person {
    private String doctorId;
    private String medicalSpecialty;
    private String licenseNumber;
    private int expYears;
    private boolean isAvailable;
    private ArrayBucketList<Patient> patients;
    private ArrayBucketList<Schedule> schedules;
    private ArrayBucketList<Appointment> appointments;

    public Doctor(String fullName, String ICNumber, String email, String phoneNumber,
                  Address address, Date registrationDate, String doctorId, String medicalSpecialty,
                  String licenseNumber, int expYears) {
        super(fullName, ICNumber, email, phoneNumber, address, registrationDate);
        this.doctorId = doctorId;
        this.medicalSpecialty = medicalSpecialty;
        this.licenseNumber = licenseNumber;
        this.expYears = expYears;
        this.isAvailable = true;
        this.patients = new ArrayBucketList<>();
        this.schedules = new ArrayBucketList<>();
        this.appointments = new ArrayBucketList<>();
    }

    // Accessor and mutator method for doctor
    public String getDoctorId() { return  doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getMedicalSpecialty() { return medicalSpecialty; }
    public void setMedicalSpecialty(String medicalSpecialty) { this.medicalSpecialty = medicalSpecialty; }

    public String getLicenseNumber() { return  licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public int getExpYears() { return  expYears; }
    public void setExpYears(int expYears) { this.expYears = expYears; }

    public boolean isAvailable() { return  isAvailable; }
    public void setAvailable (boolean available) { isAvailable = available; }

    // Patient Management
    public boolean addPatient(Patient patient) {
        return patients.add(patient.hashCode(), patient);
    }

    public Patient removePatient(int position) {
        return patients.removeByHash(position);
    }

    public Patient getPatient(int position) {
        return patients.getEntryByHash(position);
    }

    public boolean isPatientExist(Patient patient) {
        return patients.contains(patient);
    }

    public int getNumberOfPatients() {
        return patients.getNumberOfEntries();
    }

    // Appointment Management
    public boolean addAppointment(Appointment appointment) {
        return appointments.add(appointment.hashCode(), appointment);
    }

    public boolean addAppointment(int position, Appointment appointment) {
        return appointments.add(appointment.hashCode(), appointment);
    }

    public Appointment removeAppointment(int position) {
        return appointments.removeByHash(position);
    }

    public Appointment getAppointment(int position) {
        return appointments.getEntryByHash(position);
    }

    public boolean isAppointmentExist(Appointment appointment) {
        return appointments.contains(appointment);
    }

    public int getNumberOfAppointment() {
        return appointments.getNumberOfEntries();
    }

    // Schedule Management
    public boolean addSchedule(Schedule schedule) {
        return schedules.add(schedule.hashCode(), schedule);
    }

    public Schedule removeSchedule(int position) {
        return schedules.removeByHash(position);
    }

    public Schedule getSchedule(int position) {
        return schedules.getEntryByHash(position);
    }

    public boolean isScheduleExist(Schedule schedule) {
        return schedules.contains(schedule);
    }

    public int getNumberOfSchedule() {
        return schedules.getNumberOfEntries();
    }

    @Override
    public String toString() {
        return "Name = " + getFullName() + "\n"
                + ", Doctor ID = " + doctorId + "\n"
                + ", Medical Speciality = " + medicalSpecialty + "\n"
                + ", License Number = " + licenseNumber + "\n"
                + ", Experience Year(s) = " + expYears + "\n"
                + ", Availability = " + isAvailable;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(doctorId.replaceAll("[^0-9]", ""));
    }
}

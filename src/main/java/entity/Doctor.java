package entity;

/*
 * @author Lee Yong Kang
 */

import adt.ArrayList;
import java.util.Date;

public class Doctor extends Person {
    private String doctorId;
    private String medicalSpecialty;
    private String licenseNumber;
    private int expYears;
    private boolean isAvailable;
    private ArrayList<Patient> patients;
    private ArrayList<Schedule> schedules;
    private ArrayList<Appointment> appointments;

    public Doctor(String fullName, String ICNumber, String email, String phoneNumber,
                  Address address, Date registrationDate, String doctorId, String medicalSpecialty,
                  String licenseNumber, int expYears) {
        super(fullName, ICNumber, email, phoneNumber, address, registrationDate);
        this.doctorId = doctorId;
        this.medicalSpecialty = medicalSpecialty;
        this.licenseNumber = licenseNumber;
        this.expYears = expYears;
        this.isAvailable = true;
        this.patients = new ArrayList<>();
        this.schedules = new ArrayList<>();
        this.appointments = new ArrayList<>();
    }

    // Accessor and mutator method for doctor
    public String getDoctorId() { return  doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getMedicalSpecialty() { return medicalSpecialty; }
    public void setMedicalSpecialty(String medicalSpecialty) { this.medicalSpecialty = medicalSpecialty; }

    public String getLicenseNumber() { return  licenseNumber; }
    public void setLicenseNumber() { this.licenseNumber = licenseNumber; }

    public int getExpYears() { return  expYears; }
    public void setExpYears(int expYears) { this.expYears = expYears; }

    public boolean isAvailable() { return  isAvailable; }
    public void setAvailable (boolean available) { isAvailable = available; }

    // Patient Management
    public boolean addPatient(Patient patient) {
        return patients.add(patient);
    }

    public Patient removePatient(int position) {
        return patients.remove(position);
    }

    public Patient getPatient(int position) {
        return patients.getEntry(position);
    }

    public boolean isPatientExist(Patient patient) {
        return patients.contains(patient);
    }

    public int getNumberOfPatients() {
        return patients.getNumberOfEntries();
    }

    // Appointment Management
    public boolean addAppointment(Appointment appointment) {
        return appointments.add(appointment);
    }

    public boolean addAppointment(int position, Appointment appointment) {
        return appointments.add(appointment);
    }

    public Appointment removeAppointment(int position) {
        return appointments.remove(position);
    }

    public Appointment getAppointment(int position) {
        return appointments.getEntry(position);
    }

    public boolean isAppointmentExist(Appointment appointment) {
        return appointments.contains(appointment);
    }

    public int getNumberOfAppointment() {
        return appointments.getNumberOfEntries();
    }

    // Schedule Management
    public boolean addSchedule(Schedule schedule) {
        return schedules.add(schedule);
    }

    public Schedule removeSchedule(int position) {
        return schedules.remove(position);
    }

    public Schedule getSchedule(int position) {
        return schedules.getEntry(position);
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
}

package entity;

/*
 * @author Lee Yong Kang
 */

import adt.ArrayBucketList;
import java.time.LocalDate;

public class Doctor extends Person {
    private String doctorId;
    private String medicalSpecialty;
    private String licenseNumber;
    private int expYears;
    private boolean isAvailable;
    private ArrayBucketList<String, Patient> patients;
    private ArrayBucketList<String, Schedule> schedules;

    public Doctor(String fullName, String ICNumber, String email, String phoneNumber,
            Address address, LocalDate registrationDate, String doctorId, String medicalSpecialty,
            String licenseNumber, int expYears) {
        super(fullName, ICNumber, email, phoneNumber, address, registrationDate);
        this.doctorId = doctorId;
        this.medicalSpecialty = medicalSpecialty;
        this.licenseNumber = licenseNumber;
        this.expYears = expYears;
        this.isAvailable = true;
        this.patients = new ArrayBucketList<String, Patient>();
        this.schedules = new ArrayBucketList<String, Schedule>();
    }

    // Accessor and mutator method for doctor
    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getMedicalSpecialty() {
        return medicalSpecialty;
    }

    public void setMedicalSpecialty(String medicalSpecialty) {
        this.medicalSpecialty = medicalSpecialty;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public int getExpYears() {
        return expYears;
    }

    public void setExpYears(int expYears) {
        this.expYears = expYears;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    // Patient Management
    public boolean addPatient(Patient patient) {
        return patients.add(patient.getPatientId(), patient) != null;
    }

    public Patient removePatient(String patientId) {
        return patients.remove(patientId);
    }

    public Patient getPatient(String patientId) {
        return patients.getValue(patientId);
    }

    public boolean isPatientExist(String patientId) {
        return patients.contains(patientId);
    }

    public int getNumberOfPatients() {
        return patients.getSize();
    }

    // Schedule Management
    public boolean addSchedule(Schedule schedule) {
        return schedules.add(schedule.getScheduleId(), schedule) != null;
    }

    public Schedule removeSchedule(String scheduleId) {
        return schedules.remove(scheduleId);
    }

    public Schedule getSchedule(String scheduleId) {
        return schedules.getValue(scheduleId);
    }

    public boolean isScheduleExist(String scheduleId) {
        return schedules.contains(scheduleId);
    }

    public int getNumberOfSchedule() {
        return schedules.getSize();
    }

    public ArrayBucketList<String, Schedule> getSchedules() {
        return schedules;
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

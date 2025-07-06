package entity;

/*
 * @author Lee Yong Kang
 */

import java.util.ArrayList;
import java.util.Date;

import static utility.PatternChecker.PHONE_PATTERN;

public class Doctor extends Person {
    private String doctorId;
    private String medicalSpecialty;
    private String licenseNumber;
    private int expYears;
    private boolean isAvailable;
    //private ArrayList<Schedule> schedules;
    //private ArrayList<Appointment> appointments;

    public Doctor(String fullName, String ICNumber, String email, String phoneNumber,
                  Address address, Date registrationDate, String doctorId, String medicalSpecialty,
                  String licenseNumber, int expYears) {
        super(fullName, ICNumber, email, phoneNumber, address, registrationDate);
        this.doctorId = doctorId;
        this.medicalSpecialty = medicalSpecialty;
        this.licenseNumber = licenseNumber;
        this.expYears = expYears;
        //this.schedules = ;
        //this.appointments = ;
    }

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

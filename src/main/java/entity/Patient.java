package entity;

import java.time.LocalDate;

import static utility.PatternChecker.PHONE_PATTERN;

public class Patient extends Person {
    private String patientId;
    private String wardNumber;
    private BloodType bloodType;
    private String allergies;
    private Doctor dischargingDoctor;
    private Prescription currentPrescription;
    private boolean isActive;
    private String emergencyContact;

    public Patient(String fullName, String ICNumber, String email, String phoneNumber,
                   Address address, LocalDate registrationDate, String patientId, String wardNumber,
                   BloodType bloodType, String allergies, String emergencyContact) {
        super(fullName, ICNumber, email, phoneNumber, address, registrationDate);
        this.patientId = patientId;
        this.wardNumber = wardNumber;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.emergencyContact = emergencyContact;
        this.isActive = true;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getWardNumber() {
        return wardNumber;
    }

    public void setWardNumber(String wardNumber) {
        this.wardNumber = wardNumber;
    }

    public BloodType getBloodType() {
        return bloodType;
    }

    public void setBloodType(BloodType bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        if (emergencyContact == null || emergencyContact.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (!PHONE_PATTERN.matcher(emergencyContact.trim()).matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
        this.emergencyContact = emergencyContact.trim();
    }

    public Doctor getDischargingDoctor() {
        return dischargingDoctor;
    }

    public void setDischargingDoctor(Doctor dischargingDoctor) {
        this.dischargingDoctor = dischargingDoctor;
    }

    public Prescription getCurrentPrescription() {
        return currentPrescription;
    }

    public void setCurrentPrescription(Prescription currentPrescription) {
        this.currentPrescription = currentPrescription;
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(patientId.replaceAll("[^0-9]", ""));
    }
}

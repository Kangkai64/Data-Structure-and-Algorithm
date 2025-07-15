package entity;

import utility.ConsoleUtils;
import adt.ArrayList;
import java.util.Date;

public class Prescription {
    private String prescriptionId;
    private Patient patient;
    private Doctor doctor;
    private Consultation consultation;
    private Date prescriptionDate;
    private ArrayList<PrescribedMedicine> prescribedMedicines;
    private String instructions;
    private Date expiryDate;
    private PrescriptionStatus status;
    private double totalCost;

    public enum PrescriptionStatus {
        ACTIVE, DISPENSED, EXPIRED, CANCELLED
    }

    public Prescription(String prescriptionId, Patient patient, Doctor doctor, 
                       Consultation consultation, Date prescriptionDate, String instructions, Date expiryDate) {
        this.prescriptionId = prescriptionId;
        this.patient = patient;
        this.doctor = doctor;
        this.consultation = consultation;
        this.prescriptionDate = prescriptionDate;
        this.instructions = instructions;
        this.expiryDate = expiryDate;
        this.prescribedMedicines = new ArrayList<>();
        this.status = PrescriptionStatus.ACTIVE;
        this.totalCost = 0.0;
    }

    // Getters and Setters
    public String getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Consultation getConsultation() { return consultation; }
    public void setConsultation(Consultation consultation) { this.consultation = consultation; }

    public Date getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(Date prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public ArrayList<PrescribedMedicine> getPrescribedMedicines() { return prescribedMedicines; }
    public void setPrescribedMedicines(ArrayList<PrescribedMedicine> prescribedMedicines) { 
        this.prescribedMedicines = prescribedMedicines; 
    }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    // Business Logic Methods
    public boolean addPrescribedMedicine(PrescribedMedicine prescribedMedicine) {
        if (prescribedMedicine != null) {
            boolean added = prescribedMedicines.add(prescribedMedicine);
            if (added) {
                calculateTotalCost();
            }
            return added;
        }
        return false;
    }

    public boolean removePrescribedMedicine(PrescribedMedicine prescribedMedicine) {
        for (int index = 1; index <= prescribedMedicines.getNumberOfEntries(); index++) {
            PrescribedMedicine tempPrescribedMedicine = prescribedMedicines.getEntry(index);
            if (tempPrescribedMedicine.getPrescribedMedicineId().equals(prescribedMedicine.getPrescribedMedicineId())) {
                prescribedMedicines.remove(index);
                calculateTotalCost();
                return true;
            }
        }
        return false;
    }

    public boolean updatePrescribedMedicine(PrescribedMedicine prescribedMedicine, Medicine medicine, int quantity, String dosage, String frequency, int duration) {
        for (int index = 1; index <= prescribedMedicines.getNumberOfEntries(); index++) {
            PrescribedMedicine tempPrescribedMedicine = prescribedMedicines.getEntry(index);
            if (tempPrescribedMedicine.getPrescribedMedicineId().equals(prescribedMedicine.getPrescribedMedicineId())) {
                tempPrescribedMedicine.setMedicine(medicine);
                tempPrescribedMedicine.setQuantity(quantity);
                tempPrescribedMedicine.setDosage(dosage);
                tempPrescribedMedicine.setFrequency(frequency);
                tempPrescribedMedicine.setDuration(duration);
                return true;
            }
        }
        return false;
    }

    public int getNumberOfPrescribedMedicines() {
        return prescribedMedicines.getNumberOfEntries();
    }

    public boolean isExpired() {
        return new Date().after(expiryDate);
    }

    public boolean canBeDispensed() {
        return status == PrescriptionStatus.ACTIVE && !isExpired();
    }

    private void calculateTotalCost() {
        totalCost = 0.0;
        for (int index = 1; index <= prescribedMedicines.getNumberOfEntries(); index++) {
            PrescribedMedicine pm = prescribedMedicines.getEntry(index);
            totalCost += pm.getTotalCost();
        }
    }

    @Override
    public String toString() {
        return "Prescription ID: " + prescriptionId + "\n" +
               "Patient: " + patient.getFullName() + "\n" +
               "Doctor: " + doctor.getFullName() + "\n" +
               "Prescription Date: " + ConsoleUtils.dateTimeFormatter(prescriptionDate) + "\n" +
               "Status: " + status + "\n" +
               "Total Cost: RM " + String.format("%.2f", totalCost) + "\n";
    }

    // Inner class for prescribed medicine details
    public static class PrescribedMedicine {
        private String prescribedMedicineId;
        private String prescriptionId;
        private Medicine medicine;
        private int quantity;
        private String dosage;
        private String frequency;
        private int duration; // in days
        private double unitPrice;

        public PrescribedMedicine(String prescribedMedicineId, String prescriptionId, Medicine medicine, int quantity, String dosage, 
                                String frequency, int duration, double unitPrice) {
            this.prescribedMedicineId = prescribedMedicineId;
            this.prescriptionId = prescriptionId;
            this.medicine = medicine;
            this.quantity = quantity;
            this.dosage = dosage;
            this.frequency = frequency;
            this.duration = duration;
            this.unitPrice = unitPrice;
        }

        // Getters and Setter
        public String getPrescribedMedicineId() { return prescribedMedicineId; }
        public void setPrescribedMedicineId(String prescribedMedicineId) { this.prescribedMedicineId = prescribedMedicineId; }

        public String getPrescriptionId() { return prescriptionId; }
        public void setPrescriptionId(String prescriptionId) { this.prescriptionId = prescriptionId; }

        public Medicine getMedicine() { return medicine; }
        public void setMedicine(Medicine medicine) { this.medicine = medicine; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public String getDosage() { return dosage; }
        public void setDosage(String dosage) { this.dosage = dosage; }

        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }

        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }

        public double getUnitPrice() { return unitPrice; }
        public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

        public double getTotalCost() {
            return quantity * unitPrice;
        }

        @Override
        public String toString() {
            return "Prescribed Medicine ID: " + prescribedMedicineId + "\n" +
                   "Prescription ID: " + prescriptionId + "\n" +
                   "Medicine: " + medicine.getMedicineName() + "\n" +
                   "Quantity: " + quantity + "\n" +
                   "Dosage: " + dosage + "\n" +
                   "Frequency: " + frequency + "\n" +
                   "Total Cost: RM " + String.format("%.2f", getTotalCost()) + "\n";
        }
    }
} 
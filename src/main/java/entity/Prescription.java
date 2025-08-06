package entity;

import adt.ArrayBucketList;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.Iterator;

public class Prescription {
    private String prescriptionId;
    private Patient patient;
    private Doctor doctor;
    private Consultation consultation;
    private LocalDate prescriptionDate;
    private ArrayBucketList<String, PrescribedMedicine> prescribedMedicines;
    private String instructions;
    private LocalDate expiryDate;
    private PrescriptionStatus status;
    private double totalCost;

    public enum PrescriptionStatus {
        ACTIVE, DISPENSED, EXPIRED, CANCELLED
    }

    public Prescription(String prescriptionId, Patient patient, Doctor doctor, 
                       Consultation consultation, LocalDate prescriptionDate, String instructions, LocalDate expiryDate) {
        this.prescriptionId = prescriptionId;
        this.patient = patient;
        this.doctor = doctor;
        this.consultation = consultation;
        this.prescriptionDate = prescriptionDate;
        this.instructions = instructions;
        this.expiryDate = expiryDate;
        this.prescribedMedicines = new ArrayBucketList<String, PrescribedMedicine>();
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

    public LocalDate getPrescriptionDate() { return prescriptionDate; }
    public void setPrescriptionDate(LocalDate prescriptionDate) { this.prescriptionDate = prescriptionDate; }

    public ArrayBucketList<String, PrescribedMedicine> getPrescribedMedicines() { return prescribedMedicines; }
    public void setPrescribedMedicines(ArrayBucketList<String, PrescribedMedicine> prescribedMedicines) { 
        this.prescribedMedicines = prescribedMedicines; 
    }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public PrescriptionStatus getStatus() { return status; }
    public void setStatus(PrescriptionStatus status) { this.status = status; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    // Business Logic Methods
    public PrescribedMedicine findPrescribedMedicineById(String prescribedMedicineId) {
        return prescribedMedicines.getValue(prescribedMedicineId);
    }

    public boolean addPrescribedMedicine(PrescribedMedicine prescribedMedicine) {
        if (prescribedMedicine != null) {
            boolean added = prescribedMedicines.add(prescribedMedicine.getPrescribedMedicineId(), prescribedMedicine) != null;
            if (added) {
                calculateTotalCost();
            }
            return added;
        }
        return false;
    }

    public boolean removePrescribedMedicine(PrescribedMedicine prescribedMedicine) {
        Iterator<PrescribedMedicine> iterator = prescribedMedicines.iterator();
        while (iterator.hasNext()) {
            PrescribedMedicine tempPrescribedMedicine = iterator.next();
            if (tempPrescribedMedicine.getPrescribedMedicineId().equals(prescribedMedicine.getPrescribedMedicineId())) {
                prescribedMedicines.remove(tempPrescribedMedicine.getPrescribedMedicineId());
                calculateTotalCost();
                return true;
            }
        }
        return false;
    }

    public boolean updatePrescribedMedicine(PrescribedMedicine prescribedMedicine, Medicine medicine, int quantity, String dosage, String frequency, int duration) {
        PrescribedMedicine tempPrescribedMedicine = prescribedMedicines.getValue(prescribedMedicine.getPrescribedMedicineId());
        if (tempPrescribedMedicine != null) {
                tempPrescribedMedicine.setMedicine(medicine);
                tempPrescribedMedicine.setQuantity(quantity);
                tempPrescribedMedicine.setDosage(dosage);
                tempPrescribedMedicine.setFrequency(frequency);
                tempPrescribedMedicine.setDuration(duration);
                return true;
        }
        return false;
    }

    public int getNumberOfPrescribedMedicines() {
        return prescribedMedicines.getSize();
    }

    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public boolean canBeDispensed() {
        return status == PrescriptionStatus.ACTIVE && !isExpired();
    }

    private void calculateTotalCost() {
        totalCost = 0.0;
        Iterator<PrescribedMedicine> iterator = prescribedMedicines.iterator();
        while (iterator.hasNext()) {
            PrescribedMedicine pm = iterator.next();
            totalCost += pm.getTotalCost();
        }
    }

    @Override
    public String toString() {
        return "Prescription ID: " + prescriptionId + "\n" +
               "Patient: " + patient.getFullName() + "\n" +
               "Doctor: " + doctor.getFullName() + "\n" +
               "Prescription Date: " + prescriptionDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + "\n" +
               "Status: " + status + "\n" +
               "Total Cost: RM " + String.format("%.2f", totalCost) + "\n";
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(prescriptionId.replaceAll("[^0-9]", ""));
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

        // Constructor with prescriptionId only
        public PrescribedMedicine(String prescriptionId) {
            this.prescriptionId = prescriptionId;
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

        @Override
        public int hashCode() {
            return Integer.parseInt(prescribedMedicineId.replaceAll("[^0-9]", ""));
        }
    }
} 
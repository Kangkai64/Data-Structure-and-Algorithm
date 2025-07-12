package control;

import adt.ArrayList;
import entity.Medicine;
import entity.Prescription;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import java.util.Date;

/**
 * Pharmacy Management Control - Module 5
 * Manages medicine dispensing after doctor consultation and maintain medicine stock control
 */
public class PharmacyManagementControl {
    
    private ArrayList<Medicine> medicines;
    private ArrayList<Prescription> prescriptions;
    private ArrayList<Prescription> dispensedPrescriptions;
    
    public PharmacyManagementControl() {
        this.medicines = new ArrayList<>();
        this.prescriptions = new ArrayList<>();
        this.dispensedPrescriptions = new ArrayList<>();
    }
    
    // Medicine Management Methods
    public boolean addMedicine(String medicineName, String genericName, String manufacturer,
                             String description, String dosageForm, String strength,
                             int quantityInStock, int minimumStockLevel, double unitPrice,
                             Date expiryDate, String storageLocation, boolean requiresPrescription) {
        try {
            // Generate medicine ID
            String medicineId = generateMedicineId();
            
            // Create new medicine
            Medicine medicine = new Medicine(medicineId, medicineName, genericName, manufacturer,
                                           description, dosageForm, strength, quantityInStock,
                                           minimumStockLevel, unitPrice, expiryDate, storageLocation,
                                           requiresPrescription);
            
            // Add to medicines list
            medicines.add(medicine);
            
            return true;
        } catch (Exception exception) {
            System.err.println("Error adding medicine: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean updateMedicineStock(String medicineId, int quantity) {
        try {
            Medicine medicine = findMedicineById(medicineId);
            if (medicine != null) {
                if (quantity > 0) {
                    return medicine.addStock(quantity);
                } else {
                    return medicine.removeStock(Math.abs(quantity));
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating medicine stock: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean updateMedicinePrice(String medicineId, double newPrice) {
        try {
            Medicine medicine = findMedicineById(medicineId);
            if (medicine != null) {
                medicine.setUnitPrice(newPrice);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating medicine price: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean discontinueMedicine(String medicineId) {
        try {
            Medicine medicine = findMedicineById(medicineId);
            if (medicine != null) {
                medicine.setStatus(Medicine.MedicineStatus.DISCONTINUED);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error discontinuing medicine: " + exception.getMessage());
            return false;
        }
    }
    
    // Prescription Management Methods
    public boolean createPrescription(Patient patient, Doctor doctor, Consultation consultation,
                                    String instructions, Date expiryDate) {
        try {
            // Generate prescription ID
            String prescriptionId = generatePrescriptionId();
            
            // Create new prescription
            Prescription prescription = new Prescription(prescriptionId, patient, doctor, 
                                                       consultation, instructions, expiryDate);
            
            // Add to prescriptions list
            prescriptions.add(prescription);
            
            return true;
        } catch (Exception exception) {
            System.err.println("Error creating prescription: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean addMedicineToPrescription(String prescriptionId, Medicine medicine,
                                           int quantity, String dosage, String frequency, 
                                           int duration) {
        try {
            Prescription prescription = findPrescriptionById(prescriptionId);
            if (prescription != null && medicine != null) {
                Prescription.PrescribedMedicine prescribedMedicine = 
                    new Prescription.PrescribedMedicine(medicine, quantity, dosage, frequency, 
                                                       duration, medicine.getUnitPrice());
                
                boolean added = prescription.addPrescribedMedicine(prescribedMedicine);
                return added;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error adding medicine to prescription: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean dispensePrescription(String prescriptionId) {
        try {
            Prescription prescription = findPrescriptionById(prescriptionId);
            if (prescription != null && prescription.canBeDispensed()) {
                // Check if all medicines are available
                for (int index = 1; index <= prescription.getNumberOfPrescribedMedicines(); index++) {
                    Prescription.PrescribedMedicine prescribedMedicine = prescription.getPrescribedMedicine(index);
                    Medicine medicine = prescribedMedicine.getMedicine();
                    
                    if (medicine.getQuantityInStock() < prescribedMedicine.getQuantity()) {
                        System.err.println("Insufficient stock for medicine: " + medicine.getMedicineName());
                        return false;
                    }
                }
                
                // Dispense medicines
                for (int index = 1; index <= prescription.getNumberOfPrescribedMedicines(); index++) {
                    Prescription.PrescribedMedicine prescribedMedicine = prescription.getPrescribedMedicine(index);
                    Medicine medicine = prescribedMedicine.getMedicine();
                    
                    medicine.removeStock(prescribedMedicine.getQuantity());
                }
                
                // Update prescription status
                prescription.setStatus(Prescription.PrescriptionStatus.DISPENSED);
                
                // Move to dispensed prescriptions
                dispensedPrescriptions.add(prescription);
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error dispensing prescription: " + exception.getMessage());
            return false;
        }
    }
    
    // Search and Retrieval Methods
    public Medicine findMedicineById(String medicineId) {
        for (int index = 1; index <= medicines.getNumberOfEntries(); index++) {
            Medicine medicine = medicines.getEntry(index);
            if (medicine.getMedicineId().equals(medicineId)) {
                return medicine;
            }
        }
        return null;
    }
    
    public Medicine findMedicineByName(String medicineName) {
        for (int index = 1; index <= medicines.getNumberOfEntries(); index++) {
            Medicine medicine = medicines.getEntry(index);
            if (medicine.getMedicineName().equalsIgnoreCase(medicineName)) {
                return medicine;
            }
        }
        return null;
    }
    
    public ArrayList<Medicine> findMedicinesByManufacturer(String manufacturer) {
        ArrayList<Medicine> manufacturerMedicines = new ArrayList<>();
        for (int index = 1; index <= medicines.getNumberOfEntries(); index++) {
            Medicine medicine = medicines.getEntry(index);
            if (medicine.getManufacturer().equalsIgnoreCase(manufacturer)) {
                manufacturerMedicines.add(medicine);
            }
        }
        return manufacturerMedicines;
    }
    
    public ArrayList<Medicine> getLowStockMedicines() {
        ArrayList<Medicine> lowStockMedicines = new ArrayList<>();
        for (int index = 1; index <= medicines.getNumberOfEntries(); index++) {
            Medicine medicine = medicines.getEntry(index);
            if (medicine.isLowStock()) {
                lowStockMedicines.add(medicine);
            }
        }
        return lowStockMedicines;
    }
    
    public ArrayList<Medicine> getExpiredMedicines() {
        ArrayList<Medicine> expiredMedicines = new ArrayList<>();
        for (int index = 1; index <= medicines.getNumberOfEntries(); index++) {
            Medicine medicine = medicines.getEntry(index);
            if (medicine.isExpired()) {
                expiredMedicines.add(medicine);
            }
        }
        return expiredMedicines;
    }
    
    public Prescription findPrescriptionById(String prescriptionId) {
        for (int index = 1; index <= prescriptions.getNumberOfEntries(); index++) {
            Prescription prescription = prescriptions.getEntry(index);
            if (prescription.getPrescriptionId().equals(prescriptionId)) {
                return prescription;
            }
        }
        return null;
    }
    
    public ArrayList<Prescription> findPrescriptionsByPatient(String patientId) {
        ArrayList<Prescription> patientPrescriptions = new ArrayList<>();
        for (int index = 1; index <= prescriptions.getNumberOfEntries(); index++) {
            Prescription prescription = prescriptions.getEntry(index);
            if (prescription.getPatient().getPatientId().equals(patientId)) {
                patientPrescriptions.add(prescription);
            }
        }
        return patientPrescriptions;
    }
    
    public ArrayList<Prescription> getActivePrescriptions() {
        ArrayList<Prescription> activePrescriptions = new ArrayList<>();
        for (int index = 1; index <= prescriptions.getNumberOfEntries(); index++) {
            Prescription prescription = prescriptions.getEntry(index);
            if (prescription.getStatus() == Prescription.PrescriptionStatus.ACTIVE) {
                activePrescriptions.add(prescription);
            }
        }
        return activePrescriptions;
    }
    
    public ArrayList<Medicine> getAllMedicines() {
        return medicines;
    }
    
    public ArrayList<Prescription> getAllPrescriptions() {
        return prescriptions;
    }
    
    public int getTotalMedicines() {
        return medicines.getNumberOfEntries();
    }
    
    public int getTotalPrescriptions() {
        return prescriptions.getNumberOfEntries();
    }
    
    // Reporting Methods
    public String generateMedicineStockReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== MEDICINE STOCK REPORT ===\n");
        report.append("Total Medicines: ").append(getTotalMedicines()).append("\n");
        report.append("Low Stock Medicines: ").append(getLowStockMedicines().getNumberOfEntries()).append("\n");
        report.append("Expired Medicines: ").append(getExpiredMedicines().getNumberOfEntries()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= medicines.getNumberOfEntries(); index++) {
            Medicine medicine = medicines.getEntry(index);
            report.append("Medicine ID: ").append(medicine.getMedicineId()).append("\n");
            report.append("Name: ").append(medicine.getMedicineName()).append("\n");
            report.append("Generic Name: ").append(medicine.getGenericName()).append("\n");
            report.append("Manufacturer: ").append(medicine.getManufacturer()).append("\n");
            report.append("Stock: ").append(medicine.getQuantityInStock()).append(" units\n");
            report.append("Minimum Stock: ").append(medicine.getMinimumStockLevel()).append(" units\n");
            report.append("Unit Price: RM").append(medicine.getUnitPrice()).append("\n");
            report.append("Status: ").append(medicine.getStatus()).append("\n");
            report.append("Expiry Date: ").append(medicine.getExpiryDate()).append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public String generatePrescriptionReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== PRESCRIPTION REPORT ===\n");
        report.append("Total Prescriptions: ").append(getTotalPrescriptions()).append("\n");
        report.append("Active Prescriptions: ").append(getActivePrescriptions().getNumberOfEntries()).append("\n");
        report.append("Dispensed Prescriptions: ").append(dispensedPrescriptions.getNumberOfEntries()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= prescriptions.getNumberOfEntries(); index++) {
            Prescription prescription = prescriptions.getEntry(index);
            report.append("Prescription ID: ").append(prescription.getPrescriptionId()).append("\n");
            report.append("Patient: ").append(prescription.getPatient().getFullName()).append("\n");
            report.append("Doctor: ").append(prescription.getDoctor().getFullName()).append("\n");
            report.append("Date: ").append(prescription.getPrescriptionDate()).append("\n");
            report.append("Status: ").append(prescription.getStatus()).append("\n");
            report.append("Total Cost: RM").append(prescription.getTotalCost()).append("\n");
            report.append("Medicines: ").append(prescription.getNumberOfPrescribedMedicines()).append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    // Private Helper Methods
    private String generateMedicineId() {
        int nextNumber = getTotalMedicines() + 1;
        return String.format("M%09d", nextNumber);
    }
    
    private String generatePrescriptionId() {
        int nextNumber = getTotalPrescriptions() + 1;
        return String.format("PR%08d", nextNumber);
    }
} 
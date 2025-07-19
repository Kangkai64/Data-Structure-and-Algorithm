package boundary;

import control.PharmacyManagementControl;
import entity.Medicine;
import entity.Prescription;
import utility.ConsoleUtils;
import java.util.Date;
import java.util.Scanner;
import adt.ArrayBucketList;

/**
 * @author: Ho Kang Kai
 * Pharmacy Management User Interface
 * Handles all pharmacy management user interactions
 */
public class PharmacyManagementUI {
    private Scanner scanner;
    private PharmacyManagementControl pharmacyControl;

    public PharmacyManagementUI() {
        this.scanner = new Scanner(System.in);
        this.pharmacyControl = new PharmacyManagementControl();
    }

    public void displayPharmacyManagementMenu() {
        while (true) {
            ConsoleUtils.printHeader("Pharmacy Management Module");
            System.out.println("1. Add Medicine");
            System.out.println("2. Update Medicine");
            System.out.println("3. Create Prescription");
            System.out.println("4. Update Prescription");
            System.out.println("5. Dispense Prescription");
            System.out.println("6. Search Medicines/Prescriptions");
            System.out.println("7. Generate Pharmacy Reports");
            System.out.println("8. Back to Main Menu");
            
            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 8);
            System.out.println();
            switch (choice) {
                case 1:
                    addMedicine();
                    break;
                case 2:
                    updateMedicine();
                    break;
                case 3:
                    createPrescription();
                    break;
                case 4:
                    updatePrescription();
                    break;
                case 5:
                    dispensePrescription();
                    break;
                case 6:
                    searchPharmacyItems();
                    break;
                case 7:
                    generatePharmacyReports();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void addMedicine() {
        ConsoleUtils.printHeader("Add Medicine");
        String medicineName = ConsoleUtils.getStringInput(scanner, "Enter medicine name: ");
        String genericName = ConsoleUtils.getStringInput(scanner, "Enter generic name: ");
        String manufacturer = ConsoleUtils.getStringInput(scanner, "Enter manufacturer: ");
        String description = ConsoleUtils.getStringInput(scanner, "Enter description: ");
        String dosageForm = ConsoleUtils.getStringInput(scanner, "Enter dosage form: ");
        String strength = ConsoleUtils.getStringInput(scanner, "Enter strength: ");
        int quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity in stock: ", 0, 10000);
        int minStock = ConsoleUtils.getIntInput(scanner, "Enter minimum stock level: ", 0, 1000);
        double price = ConsoleUtils.getDoubleInput(scanner, "Enter unit price: ", 0.0, 10000.0);
        Date expiryDate = ConsoleUtils.getDateInput(scanner, "Enter expiry date (DD-MM-YYYY hh:mm:ss): ");
        String storageLocation = ConsoleUtils.getStringInput(scanner, "Enter storage location: ");
        String requiresPrescriptionInput = (ConsoleUtils.getStringInput(scanner, "Requires prescription (Y / N): "));
        boolean requiresPrescription = requiresPrescriptionInput.equalsIgnoreCase("Y");
        System.out.println();
        
        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println("Medicine Name: " + medicineName);
        System.out.println("Generic Name: " + genericName);
        System.out.println("Manufacturer: " + manufacturer);
        System.out.println("Description: " + description);
        System.out.println("Dosage Form: " + dosageForm);
        System.out.println("Strength: " + strength);
        System.out.println("Quantity: " + quantity);
        System.out.println("Minimum Stock Level: " + minStock);
        System.out.println("Unit Price: " + price);
        System.out.println("Expiry Date: " + ConsoleUtils.dateTimeFormatter(expiryDate));
        System.out.println("Storage Location: " + storageLocation);
        System.out.println("Requires Prescription: " + (requiresPrescription ? "Yes" : "No"));
        System.out.println();

        String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to add this medicine? (Y/N): ");
        if (confirm.charAt(0) == 'Y' || confirm.charAt(0) == 'y') {
            pharmacyControl.addMedicine(medicineName, genericName, manufacturer, description, dosageForm, strength, quantity, minStock, price, expiryDate, storageLocation, requiresPrescription);
            System.out.println("Medicine added.");
            ConsoleUtils.waitMessage();
        } else {
            System.out.println("Medicine not added.");
            ConsoleUtils.waitMessage();
        }
    }

    private void updateMedicine() {
        ConsoleUtils.printHeader("Update Medicine");
        String medicineId = ConsoleUtils.getStringInput(scanner, "Enter medicine ID: ");
        System.out.println();
        Medicine medicine = pharmacyControl.findMedicineById(medicineId);
        if (medicine == null) {
            System.out.println("Medicine not found.");
            return;
        } else {
            System.out.println(medicine);
        }
        System.out.println("1. Update Medicine Stock");
        System.out.println("2. Update Medicine Price");
        System.out.println("3. Discontinue Medicine");
        System.out.println("4. Update Medicine Details");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();
        switch (choice) {
            case 1:
                updateMedicineStock(medicineId, medicine);
                break;
            case 2:
                updateMedicinePrice(medicineId, medicine);
                break;
            case 3:
                discontinueMedicine(medicineId);
                break;
            case 4:
                updateMedicineDetails(medicineId, medicine);
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void updateMedicineStock(String medicineId, Medicine medicine) {
        ConsoleUtils.printHeader("Update Medicine Stock");
        System.out.println("1. Add stock");
        System.out.println("2. Remove stock");
        System.out.println("3. Set new quantity");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        System.out.println();
        switch (choice) {
            case 1:
                int addQuantity = ConsoleUtils.getIntInput(scanner, "Enter quantity to add: ", 1, 10000);
                pharmacyControl.updateMedicineStock(medicineId, medicine.getQuantityInStock() + addQuantity);
                System.out.println("Medicine stock updated.");
                break;
            case 2:
                int removeQuantity = ConsoleUtils.getIntInput(scanner, "Enter quantity to remove: ", 1, 10000);
                pharmacyControl.updateMedicineStock(medicineId, medicine.getQuantityInStock() - removeQuantity);
                System.out.println("Medicine stock updated.");
                break;
            case 3:
                int newQuantity = ConsoleUtils.getIntInput(scanner, "Enter new quantity: ", 0, 10000);
                pharmacyControl.updateMedicineStock(medicineId, newQuantity);
                System.out.println("Medicine stock updated.");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void updateMedicinePrice(String medicineId, Medicine medicine) {
        ConsoleUtils.printHeader("Update Medicine Price");
        double price = ConsoleUtils.getDoubleInput(scanner, "Enter price: ", 0.0, 10000.0);
        pharmacyControl.updateMedicinePrice(medicineId, price);
        System.out.println("Medicine price updated.");
        ConsoleUtils.waitMessage();
    }

    private void discontinueMedicine(String medicineId) {
        pharmacyControl.discontinueMedicine(medicineId);
        System.out.println("Medicine discontinued.");
        ConsoleUtils.waitMessage();
    }

    private void updateMedicineDetails(String medicineId, Medicine medicine) {
        ConsoleUtils.printHeader("Update Medicine Details");
        if (medicine == null) {
            System.out.println("Medicine not found.");
            return;
        } else {
            System.out.println(medicine);
        }
        String medicineName = ConsoleUtils.getStringInput(scanner, "Enter medicine name: ");
        String genericName = ConsoleUtils.getStringInput(scanner, "Enter generic name: ");
        String manufacturer = ConsoleUtils.getStringInput(scanner, "Enter manufacturer: ");
        String description = ConsoleUtils.getStringInput(scanner, "Enter description: ");
        String dosageForm = ConsoleUtils.getStringInput(scanner, "Enter dosage form: ");
        String strength = ConsoleUtils.getStringInput(scanner, "Enter strength: ");
        int quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity in stock: ", 0, 10000);
        int minStock = ConsoleUtils.getIntInput(scanner, "Enter minimum stock level: ", 0, 1000);
        double price = ConsoleUtils.getDoubleInput(scanner, "Enter unit price: ", 0.0, 10000.0);
        Date expiryDate = ConsoleUtils.getDateInput(scanner, "Enter expiry date (DD-MM-YYYY): ");
        System.out.println();

        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println("Leave blank if you don't want to update the field.");
        System.out.println("Medicine Name: " + medicineName);
        System.out.println("Generic Name: " + genericName);
        System.out.println("Manufacturer: " + manufacturer);
        System.out.println("Description: " + description);
        System.out.println("Dosage Form: " + dosageForm);
        System.out.println("Strength: " + strength);
        System.out.println("Quantity: " + quantity);
        System.out.println("Minimum Stock Level: " + minStock);
        System.out.println("Unit Price: " + String.format("%.2f", price));
        System.out.println("Expiry Date: " + ConsoleUtils.dateTimeFormatter(expiryDate));

        String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to update this medicine? (Y/N): ");
        if (confirm.charAt(0) == 'Y' || confirm.charAt(0) == 'y') {
            pharmacyControl.updateMedicineDetails(medicine);
            System.out.println("Medicine updated.");
            ConsoleUtils.waitMessage();
        } else {
            System.out.println("Medicine not updated.");
            ConsoleUtils.waitMessage();
        }
    }

    private void createPrescription() {
        ConsoleUtils.printHeader("Create Prescription");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID: ");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID (optional): ");
        String instructions = ConsoleUtils.getStringInput(scanner, "Enter instructions: ");
        Date expiryDate = ConsoleUtils.getDateInput(scanner, "Enter expiry date (DD-MM-YYYY): ");
        System.out.println();
        ConsoleUtils.printHeader("Prescription Overview");
        System.out.println("Patient ID: " + patientId + ", Doctor ID: " + doctorId);
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Instructions: " + instructions);
        System.out.println("Expiry Date: " + ConsoleUtils.dateTimeFormatter(expiryDate));
        System.out.println();

        String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to create this prescription? (Y/N): ");
        if (confirm.charAt(0) == 'Y' || confirm.charAt(0) == 'y') {
            pharmacyControl.createPrescription(patientId, doctorId, consultationId, instructions, expiryDate);
            System.out.println("Prescription created successfully.");
            ConsoleUtils.waitMessage();
        } else {
            System.out.println("Prescription not created.");
            ConsoleUtils.waitMessage();
        }
    }

    private void updatePrescription() {
        ConsoleUtils.printHeader("Update Prescription");
        System.out.println("1. Add Medicine to Prescription");
        System.out.println("2. Remove Medicine from Prescription");
        System.out.println("3. Update Medicine in Prescription");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        System.out.println();
        switch (choice) {
            case 1:
                addMedicineToPrescription();
                break;
            case 2:
                removeMedicineFromPrescription();
                break;
            case 3:
                updateMedicineInPrescription();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void addMedicineToPrescription() {
        ConsoleUtils.printHeader("Add Medicine to Prescription");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        String medicineId = ConsoleUtils.getStringInput(scanner, "Enter medicine ID: ");
        int quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity: ", 1, 100);
        String dosage = ConsoleUtils.getStringInput(scanner, "Enter dosage: ");
        String frequency = ConsoleUtils.getStringInput(scanner, "Enter frequency: ");
        int duration = ConsoleUtils.getIntInput(scanner, "Enter duration (days): ", 1, 365);
        System.out.println();

        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println("Prescription ID: " + prescriptionId);
        System.out.println("Medicine ID: " + medicineId);
        System.out.println("Quantity: " + quantity);
        System.out.println("Dosage: " + dosage);
        System.out.println("Frequency: " + frequency);
        System.out.println("Duration: " + duration + " days");
        System.out.println();

        String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to add this medicine to this prescription? (Y/N): ");
        if (confirm.charAt(0) == 'Y' || confirm.charAt(0) == 'y') {
            pharmacyControl.addMedicineToPrescription(prescriptionId, medicineId, quantity, dosage, frequency, duration);
            System.out.println("Medicine added to prescription successfully.");
            ConsoleUtils.waitMessage();
        } else {
            System.out.println("Medicine not added to prescription.");
            ConsoleUtils.waitMessage();
        }
    }

    private void removeMedicineFromPrescription() {
        ConsoleUtils.printHeader("Remove Medicine from Prescription");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        String prescribedMedicineId = ConsoleUtils.getStringInput(scanner, "Enter prescribed medicine ID: ");
        System.out.println();

        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println("Prescription ID: " + prescriptionId);
        System.out.println("Prescribed Medicine ID: " + prescribedMedicineId);
        System.out.println();

        String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to remove this medicine from this prescription? (Y/N): ");
        if (confirm.charAt(0) == 'Y' || confirm.charAt(0) == 'y') {
            pharmacyControl.removeMedicineFromPrescription(prescriptionId, prescribedMedicineId);
            System.out.println("Medicine removed from prescription successfully.");
            ConsoleUtils.waitMessage();
        } else {
            System.out.println("Medicine not removed from prescription.");
            ConsoleUtils.waitMessage();
        }
    }

    private void updateMedicineInPrescription() {
        ConsoleUtils.printHeader("Update Medicine in Prescription");
        System.out.println("Leave blank if you don't want to update the field.\n");
        String prescribedMedicineId = ConsoleUtils.getStringInput(scanner, "Enter prescribed medicine ID: ");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        String medicineId = ConsoleUtils.getStringInput(scanner, "Enter medicine ID: ");
        int quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity: ", 1, 100);
        String dosage = ConsoleUtils.getStringInput(scanner, "Enter dosage: ");
        String frequency = ConsoleUtils.getStringInput(scanner, "Enter frequency: ");
        int duration = ConsoleUtils.getIntInput(scanner, "Enter duration (days): ", 1, 365);
        System.out.println();
        
        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println("Prescribed Medicine ID: " + prescribedMedicineId);
        System.out.println("Prescription ID: " + prescriptionId);
        System.out.println("Medicine ID: " + medicineId);
        System.out.println("Quantity: " + quantity);
        System.out.println("Dosage: " + dosage);
        System.out.println("Frequency: " + frequency);
        System.out.println("Duration: " + duration + " days");
        System.out.println();

       String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to update this medicine in this prescription? (Y/N): ");
       if (confirm.charAt(0) == 'Y' || confirm.charAt(0) == 'y') {
        pharmacyControl.updateMedicineInPrescription(prescriptionId, prescribedMedicineId, medicineId, quantity, dosage, frequency, duration);
        System.out.println("Medicine updated in prescription successfully.");
        ConsoleUtils.waitMessage();
       } else {
        System.out.println("Medicine not updated in prescription.");
        ConsoleUtils.waitMessage();
       }
    }

    private void dispensePrescription() {
        ConsoleUtils.printHeader("Dispense Prescription");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        System.out.println();

        Prescription prescription = pharmacyControl.findPrescriptionById(prescriptionId);
        if (prescription == null) {
            System.out.println("Prescription not found.");
            return;
        } else {
            ConsoleUtils.printHeader("Prescription Overview");
            System.out.println(prescription);
        }

        String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to dispense this prescription? (Y/N): ");
        if (confirm.charAt(0) == 'Y' || confirm.charAt(0) == 'y') {
            pharmacyControl.dispensePrescription(prescriptionId);
            System.out.println("Prescription dispensed successfully.");
            ConsoleUtils.waitMessage();
        } else {
            System.out.println("Prescription not dispensed.");
            ConsoleUtils.waitMessage();
        }
    }

    private void searchPharmacyItems() {
        ConsoleUtils.printHeader("Search Pharmacy Items");
        System.out.println("1. Search Medicines");
        System.out.println("2. Search Prescriptions");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        System.out.println();

        switch (choice) {
            case 1:
                searchMedicines();
                break;
            case 2:
                searchPrescriptions();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void searchMedicines() {
        ConsoleUtils.printHeader("Search Medicines");
        System.out.println("1. Search by Medicine ID");
        System.out.println("2. Search by Medicine Name");
        System.out.println("3. Search by Generic Name");
        System.out.println("4. Search by Manufacturer");
        System.out.println("5. Search by Status");

        Medicine medicine = null;
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 5);
        System.out.println();

        switch (choice) {
            case 1:
                String medicineId = ConsoleUtils.getStringInput(scanner, "Enter Medicine ID: ");
                medicine = pharmacyControl.findMedicineById(medicineId);
                if (medicine == null) {
                    System.out.println("Medicine not found.");
                } else {
                    System.out.println(medicine);
                }
                break;
            case 2:
                String medicineName = ConsoleUtils.getStringInput(scanner, "Enter Medicine Name: ");
                medicine = pharmacyControl.findMedicineByName(medicineName);
                if (medicine == null) {
                    System.out.println("Medicine not found.");
                } else {
                    System.out.println(medicine);
                }
                break;
            case 3:
                String genericName = ConsoleUtils.getStringInput(scanner, "Enter Generic Name: ");
                medicine = pharmacyControl.findMedicineByGenericName(genericName);
                if (medicine == null) {
                    System.out.println("Medicine not found.");
                } else {
                    System.out.println(medicine);
                }
                break;
            case 4:
                String manufacturer = ConsoleUtils.getStringInput(scanner, "Enter Manufacturer: ");
                ArrayBucketList<Medicine> medicines = pharmacyControl.findMedicineByManufacturer(manufacturer);
                if (medicines.isEmpty()) {
                    System.out.println("No medicines found.");
                } 
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. AVAILABLE  2. LOW_STOCK  3. OUT_OF_STOCK  4. DISCONTINUED");
                int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
                medicine = pharmacyControl.findMedicineByStatus(statusChoice);
                if (medicine == null) {
                    System.out.println("Medicine not found.");
                } else {
                    System.out.println(medicine);
                }
                break;
            default:
                System.out.println("Invalid choice.");
        }
        ConsoleUtils.waitMessage();
    }

    private void searchPrescriptions() {
        ConsoleUtils.printHeader("Search Prescriptions");
        System.out.println("1. Search by Prescription ID");
        System.out.println("2. Search by Patient ID");
        System.out.println("3. Search by Doctor ID");
        System.out.println("4. Search by Status");
        System.out.println("5. Search by Date Range");

        Prescription prescription = null;
        ArrayBucketList<Prescription> prescriptions = null;
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 5);
        System.out.println();
        
        switch (choice) {
            case 1:
                String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter Prescription ID: ");
                prescription = pharmacyControl.findPrescriptionById(prescriptionId);
                if (prescription == null) {
                    System.out.println("Prescription not found.");
                } else {
                    System.out.println(prescription);
                }
                break;
            case 2:
                String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
                prescriptions = pharmacyControl.findPrescriptionsByPatient(patientId);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println(prescriptions);
                }
                break;
            case 3:
                String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
                prescriptions = pharmacyControl.findPrescriptionsByDoctor(doctorId);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println(prescriptions);
                }
                break;
            case 4:
                System.out.println("Select status:");
                System.out.println("1. ACTIVE  2. DISPENSED  3. EXPIRED  4. CANCELLED");
                int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
                prescriptions = pharmacyControl.findPrescriptionsByStatus(statusChoice);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println(prescriptions);
                }
                break;
            case 5:
                String startDate = ConsoleUtils.getStringInput(scanner, "Enter start date (DD-MM-YYYY): ");
                String endDate = ConsoleUtils.getStringInput(scanner, "Enter end date (DD-MM-YYYY): ");
                prescriptions = pharmacyControl.findPrescriptionsByDateRange(startDate, endDate);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println(prescriptions);
                }
                break;
            default:
                System.out.println("Invalid choice.");
        }
        ConsoleUtils.waitMessage();
    }

    private void generatePharmacyReports() {
        ConsoleUtils.printHeader("Pharmacy Reports");
        System.out.println(pharmacyControl.generateMedicineStockReport());
        ConsoleUtils.waitMessage();
        System.out.println(pharmacyControl.generatePrescriptionReport());
        ConsoleUtils.waitMessage();
    }
} 
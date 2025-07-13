package boundary;

import control.PharmacyManagementControl;
import java.util.Date;
import java.util.Scanner;

/**
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
            System.out.println("\n=== PHARMACY MANAGEMENT MODULE ===");
            System.out.println("1. Add Medicine");
            System.out.println("2. Update Medicine Stock");
            System.out.println("3. Create Prescription");
            System.out.println("4. Add Medicine to Prescription");
            System.out.println("5. Dispense Prescription");
            System.out.println("6. Search Medicines/Prescriptions");
            System.out.println("7. Generate Pharmacy Reports");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    addMedicine();
                    break;
                case 2:
                    updateMedicineStock();
                    break;
                case 3:
                    createPrescription();
                    break;
                case 4:
                    addMedicineToPrescription();
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
        System.out.println("\n=== ADD MEDICINE ===");
        System.out.print("Enter medicine name: ");
        String medicineName = scanner.nextLine();
        System.out.print("Enter generic name: ");
        String genericName = scanner.nextLine();
        System.out.print("Enter manufacturer: ");
        String manufacturer = scanner.nextLine();
        System.out.print("Enter description: ");
        String description = scanner.nextLine();
        System.out.print("Enter dosage form: ");
        String dosageForm = scanner.nextLine();
        System.out.print("Enter strength: ");
        String strength = scanner.nextLine();
        System.out.print("Enter quantity in stock: ");
        int quantity = getIntInput();
        System.out.print("Enter minimum stock level: ");
        int minStock = getIntInput();
        System.out.print("Enter unit price: ");
        double price = getDoubleInput();
        System.out.print("Enter expiry date (YYYY-MM-DD): ");
        String expiryDateStr = scanner.nextLine();
        System.out.print("Enter storage location: ");
        String storageLocation = scanner.nextLine();
        System.out.print("Requires prescription (true/false): ");
        boolean requiresPrescription = getBooleanInput();
        
        // For now, just show a placeholder
        System.out.println("Add Medicine - Implementation needed");
        System.out.println("Medicine Name: " + medicineName);
        System.out.println("Generic Name: " + genericName);
        System.out.println("Manufacturer: " + manufacturer);
        System.out.println("Quantity: " + quantity);
        System.out.println("Price: " + price);
        System.out.println("Expiry Date: " + expiryDateStr);
        System.out.println("Requires Prescription: " + requiresPrescription);
    }

    private void updateMedicineStock() {
        System.out.println("\n=== UPDATE MEDICINE STOCK ===");
        System.out.print("Enter medicine ID: ");
        String medicineId = scanner.nextLine();
        System.out.println("1. Add stock");
        System.out.println("2. Remove stock");
        System.out.println("3. Set new quantity");
        System.out.print("Enter choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("Enter quantity to add: ");
                int addQuantity = getIntInput();
                System.out.println("Add stock - Implementation needed");
                break;
            case 2:
                System.out.print("Enter quantity to remove: ");
                int removeQuantity = getIntInput();
                System.out.println("Remove stock - Implementation needed");
                break;
            case 3:
                System.out.print("Enter new quantity: ");
                int newQuantity = getIntInput();
                System.out.println("Set new quantity - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void createPrescription() {
        System.out.println("\n=== CREATE PRESCRIPTION ===");
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine();
        System.out.print("Enter doctor ID: ");
        String doctorId = scanner.nextLine();
        System.out.print("Enter consultation ID (optional): ");
        String consultationId = scanner.nextLine();
        System.out.print("Enter instructions: ");
        String instructions = scanner.nextLine();
        System.out.print("Enter expiry date (YYYY-MM-DD): ");
        String expiryDateStr = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Create Prescription - Implementation needed");
        System.out.println("Patient ID: " + patientId + ", Doctor ID: " + doctorId);
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Instructions: " + instructions);
        System.out.println("Expiry Date: " + expiryDateStr);
    }

    private void addMedicineToPrescription() {
        System.out.println("\n=== ADD MEDICINE TO PRESCRIPTION ===");
        System.out.print("Enter prescription ID: ");
        String prescriptionId = scanner.nextLine();
        System.out.print("Enter medicine ID: ");
        String medicineId = scanner.nextLine();
        System.out.print("Enter quantity: ");
        int quantity = getIntInput();
        System.out.print("Enter dosage: ");
        String dosage = scanner.nextLine();
        System.out.print("Enter frequency: ");
        String frequency = scanner.nextLine();
        System.out.print("Enter duration (days): ");
        int duration = getIntInput();
        
        // For now, just show a placeholder
        System.out.println("Add Medicine to Prescription - Implementation needed");
        System.out.println("Prescription ID: " + prescriptionId);
        System.out.println("Medicine ID: " + medicineId);
        System.out.println("Quantity: " + quantity);
        System.out.println("Dosage: " + dosage);
        System.out.println("Frequency: " + frequency);
        System.out.println("Duration: " + duration + " days");
    }

    private void dispensePrescription() {
        System.out.println("\n=== DISPENSE PRESCRIPTION ===");
        System.out.print("Enter prescription ID: ");
        String prescriptionId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Dispense Prescription - Implementation needed");
        System.out.println("Prescription ID: " + prescriptionId);
    }

    private void searchPharmacyItems() {
        System.out.println("\n=== SEARCH PHARMACY ITEMS ===");
        System.out.println("1. Search Medicines");
        System.out.println("2. Search Prescriptions");
        System.out.print("Enter choice: ");
        
        int choice = getIntInput();
        
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
        System.out.println("\n=== SEARCH MEDICINES ===");
        System.out.println("1. Search by Medicine ID");
        System.out.println("2. Search by Medicine Name");
        System.out.println("3. Search by Generic Name");
        System.out.println("4. Search by Manufacturer");
        System.out.println("5. Search by Status");
        System.out.print("Enter choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("Enter Medicine ID: ");
                String medicineId = scanner.nextLine();
                System.out.println("Search by Medicine ID - Implementation needed");
                break;
            case 2:
                System.out.print("Enter Medicine Name: ");
                String medicineName = scanner.nextLine();
                System.out.println("Search by Medicine Name - Implementation needed");
                break;
            case 3:
                System.out.print("Enter Generic Name: ");
                String genericName = scanner.nextLine();
                System.out.println("Search by Generic Name - Implementation needed");
                break;
            case 4:
                System.out.print("Enter Manufacturer: ");
                String manufacturer = scanner.nextLine();
                System.out.println("Search by Manufacturer - Implementation needed");
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. AVAILABLE  2. LOW_STOCK  3. OUT_OF_STOCK  4. DISCONTINUED");
                System.out.print("Enter choice: ");
                int statusChoice = getIntInput();
                System.out.println("Search by Status - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void searchPrescriptions() {
        System.out.println("\n=== SEARCH PRESCRIPTIONS ===");
        System.out.println("1. Search by Prescription ID");
        System.out.println("2. Search by Patient ID");
        System.out.println("3. Search by Doctor ID");
        System.out.println("4. Search by Status");
        System.out.println("5. Search by Date Range");
        System.out.print("Enter choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("Enter Prescription ID: ");
                String prescriptionId = scanner.nextLine();
                System.out.println("Search by Prescription ID - Implementation needed");
                break;
            case 2:
                System.out.print("Enter Patient ID: ");
                String patientId = scanner.nextLine();
                System.out.println("Search by Patient ID - Implementation needed");
                break;
            case 3:
                System.out.print("Enter Doctor ID: ");
                String doctorId = scanner.nextLine();
                System.out.println("Search by Doctor ID - Implementation needed");
                break;
            case 4:
                System.out.println("Select status:");
                System.out.println("1. ACTIVE  2. DISPENSED  3. EXPIRED  4. CANCELLED");
                System.out.print("Enter choice: ");
                int statusChoice = getIntInput();
                System.out.println("Search by Status - Implementation needed");
                break;
            case 5:
                System.out.print("Enter start date (YYYY-MM-DD): ");
                String startDate = scanner.nextLine();
                System.out.print("Enter end date (YYYY-MM-DD): ");
                String endDate = scanner.nextLine();
                System.out.println("Search by Date Range - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generatePharmacyReports() {
        System.out.println("\n=== PHARMACY REPORTS ===");
        System.out.println(pharmacyControl.generateMedicineStockReport());
        System.out.println(pharmacyControl.generatePrescriptionReport());
    }

    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException exception) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException exception) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }

    private boolean getBooleanInput() {
        while (true) {
            String input = scanner.nextLine().toLowerCase();
            if (input.equals("true") || input.equals("yes") || input.equals("1")) {
                return true;
            } else if (input.equals("false") || input.equals("no") || input.equals("0")) {
                return false;
            } else {
                System.out.print("Please enter true/false, yes/no, or 1/0: ");
            }
        }
    }
} 
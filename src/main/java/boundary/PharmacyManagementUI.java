package boundary;

import control.PharmacyManagementControl;
import entity.Medicine;
import entity.Prescription;
import utility.ConsoleUtils;
import utility.DateType;
import adt.ArrayBucketList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * @author: Ho Kang Kai
 *          Pharmacy Management User Interface
 *          Handles all pharmacy management user interactions
 */
public class PharmacyManagementUI {
    private final Scanner scanner;
    private final PharmacyManagementControl pharmacyControl;

    public PharmacyManagementUI() {
        this.scanner = new Scanner(System.in);
        this.pharmacyControl = new PharmacyManagementControl();
    }

    public void displayPharmacyManagementMenu() {
        while (true) {
            pharmacyControl.loadPhramacyData();
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
                    ConsoleUtils.waitMessage();
            }
        }
    }

    private void addMedicine() {
        ConsoleUtils.printHeader("Add Medicine");
        Medicine medicine = getMedicineDetailsFromUser();

        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println(medicine);
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to add this medicine? (Y/N): ");
        if (confirm) {
                if (pharmacyControl.addMedicine(medicine)) {
                    System.out.println("Medicine added.");
                } else {
                    System.out.println("Medicine not added.");
                }
        } else {
            System.out.println("Medicine not added.");
        }
        ConsoleUtils.waitMessage();
    }

    private Medicine getMedicineDetailsFromUser() {
        String medicineName = ConsoleUtils.getStringInput(scanner, "Enter medicine name: ");
        String genericName = ConsoleUtils.getStringInput(scanner, "Enter generic name: ");
        String manufacturer = ConsoleUtils.getStringInput(scanner, "Enter manufacturer: ");
        String description = ConsoleUtils.getStringInput(scanner, "Enter description: ");
        String dosageForm = ConsoleUtils.getStringInput(scanner, "Enter dosage form: ");
        String strength = ConsoleUtils.getStringInput(scanner, "Enter strength: ");
        int quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity in stock: ", 0, 10000);
        int minStock = ConsoleUtils.getIntInput(scanner, "Enter minimum stock level: ", 0, 300);
        double price = ConsoleUtils.getDoubleInput(scanner, "Enter unit price: ", 0.0, 10000.0);
        LocalDate expiryDate = ConsoleUtils.getDateInput(scanner, "Enter expiry date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY);
        String storageLocation = ConsoleUtils.getStringInput(scanner, "Enter storage location: ");
        boolean requiresPrescription = ConsoleUtils.getBooleanInput(scanner, "Requires prescription (Y / N): ");

        return new Medicine(null, medicineName, genericName, manufacturer, description, dosageForm, strength, quantity, minStock, price, expiryDate, storageLocation, requiresPrescription);
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
            ConsoleUtils.printHeader("Medicine Overview");
            System.out.println(medicine);
            System.out.println();
        }
        System.out.println("1. Update Medicine Stock");
        System.out.println("2. Update Medicine Price");
        System.out.println("3. Discontinue Medicine");
        System.out.println("4. Update Medicine Details");
        System.out.println("5. Main Menu");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 5);
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
            case 5:
                return;
            default:
                System.out.println("Invalid choice.");
        }
        ConsoleUtils.waitMessage();
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
                ConsoleUtils.waitMessage();
        }
    }

    private void updateMedicinePrice(String medicineId, Medicine medicine) {
        ConsoleUtils.printHeader("Update Medicine Price");
        double price = ConsoleUtils.getDoubleInput(scanner, "Enter price: ", 0.0, 10000.0);
        pharmacyControl.updateMedicinePrice(medicineId, price);
        System.out.println("Medicine price updated.");
    }

    private void discontinueMedicine(String medicineId) {
        pharmacyControl.discontinueMedicine(medicineId);
        System.out.println("Medicine discontinued.");
    }

    private void updateMedicineDetails(String medicineId, Medicine medicine) {
        ConsoleUtils.printHeader("Update Medicine Details");
        System.out.println("Leave blank if you don't want to update the field.");
        if (medicine == null) {
            System.out.println("Medicine not found.");
            return;
        } else {
            ConsoleUtils.printHeader("Medicine Overview");
            System.out.println(medicine);
            System.out.println();
        }

        medicine = getAndReplaceMedicineDetailsFromUser(medicine);

        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println(medicine);
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to update this medicine? (Y/N): ");
        if (confirm) {
                if (pharmacyControl.updateMedicineDetails(medicine)) {
                    System.out.println("Medicine updated.");
                } else {
                    System.out.println("Medicine not updated.");
                }
        } else {
            System.out.println("Medicine not updated.");
        }
    }

    private Medicine getAndReplaceMedicineDetailsFromUser(Medicine medicine) {
        int quantity = 0;
        int minStock = 0;
        double price = 0.0;

        String medicineName = ConsoleUtils.getStringInput(scanner, "Enter medicine name: ", medicine.getMedicineName());
        String genericName = ConsoleUtils.getStringInput(scanner, "Enter generic name: ", medicine.getGenericName());
        String manufacturer = ConsoleUtils.getStringInput(scanner, "Enter manufacturer: ", medicine.getManufacturer());
        String description = ConsoleUtils.getStringInput(scanner, "Enter description: ", medicine.getDescription());
        String dosageForm = ConsoleUtils.getStringInput(scanner, "Enter dosage form: ", medicine.getDosageForm());
        String strength = ConsoleUtils.getStringInput(scanner, "Enter strength: ", medicine.getStrength());
        quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity in stock: ", medicine.getQuantityInStock());
        minStock = ConsoleUtils.getIntInput(scanner, "Enter minimum stock level: ", medicine.getMinimumStockLevel());
        price = ConsoleUtils.getDoubleInput(scanner, "Enter unit price: ", medicine.getUnitPrice());
        LocalDate expiryDate = ConsoleUtils.getDateInput(scanner, "Enter expiry date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY, medicine.getExpiryDate());
        String storageLocation = ConsoleUtils.getStringInput(scanner, "Enter storage location: ",
                medicine.getStorageLocation());
        boolean requiresPrescription = ConsoleUtils.getBooleanInput(scanner, "Requires prescription (Y / N): ", medicine.getRequiresPrescription());
        System.out.println();

        // Create new medicine
        return new Medicine(medicine.getMedicineId() == null ? medicineName : medicine.getMedicineId(), medicineName,
                genericName, manufacturer, description, dosageForm, strength, quantity,
                minStock, price, expiryDate, storageLocation, requiresPrescription);
    }

    private void createPrescription() {
        ConsoleUtils.printHeader("Create Prescription");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID: ");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID (optional): ");
        String instructions = ConsoleUtils.getStringInput(scanner, "Enter instructions: ");
        LocalDate expiryDate = ConsoleUtils.getDateInput(scanner, "Enter expiry date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY);
        System.out.println();
        ConsoleUtils.printHeader("Prescription Overview");
        System.out.println("Patient ID: " + patientId + ", Doctor ID: " + doctorId);
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Instructions: " + instructions);
        System.out.println("Expiry Date: " + expiryDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to create this prescription? (Y/N): ");
        if (confirm) {
                if (pharmacyControl.createPrescription(patientId, doctorId, consultationId, instructions, expiryDate)) {
                    System.out.println("Prescription created successfully.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println("Prescription not created.");
                    ConsoleUtils.waitMessage();
                }
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
        System.out.println("4. Update Prescription Details");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
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
            case 4:
                updatePrescriptionDetails();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void addMedicineToPrescription() {
        ConsoleUtils.printHeader("Add Medicine to Prescription");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        System.out.println();
        Prescription prescription = pharmacyControl.findPrescriptionById(prescriptionId);
        if (prescription == null) {
            System.out.println("Prescription not found.");
            return;
        } else {
            ConsoleUtils.printHeader("Prescription Overview");
            System.out.println(prescription);
            for (Prescription.PrescribedMedicine prescribedMedicine : prescription.getPrescribedMedicines()) {
                System.out.println(prescribedMedicine);
            }
            System.out.println();
        }

        Prescription.PrescribedMedicine prescribedMedicine = getPrescribedMedicineDetailsFromUser(prescriptionId);
        System.out.println();

        if (prescribedMedicine == null) {
            System.out.println("Medicine not added to prescription.");
            ConsoleUtils.waitMessage();
            return;
        }

        ConsoleUtils.printHeader("Medicine Overview");
        displayPrescribedMedicineDetails(prescribedMedicine);
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to add this medicine to this prescription? (Y/N): ");
        if (confirm) {
                if (pharmacyControl.addMedicineToPrescription(prescribedMedicine)) {
                    System.out.println("Medicine added to prescription successfully.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println("Medicine not added to prescription.");
                    ConsoleUtils.waitMessage();
                }
        } else {
            System.out.println("Medicine not added to prescription.");
            ConsoleUtils.waitMessage();
        }
    }

    private Prescription.PrescribedMedicine getPrescribedMedicineDetailsFromUser(String prescriptionId) {
        String medicineId = ConsoleUtils.getStringInput(scanner, "Enter medicine ID: ");
        int quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity: ", 1, 100);
        String dosage = ConsoleUtils.getStringInput(scanner, "Enter dosage: ");
        String frequency = ConsoleUtils.getStringInput(scanner, "Enter frequency: ");
        int duration = ConsoleUtils.getIntInput(scanner, "Enter duration (days): ", 1, 365);

        Medicine medicine = pharmacyControl.findMedicineById(medicineId);
        if (medicine == null) {
            System.out.println("\nMedicine not found.");
            return null;
        }

        return new Prescription.PrescribedMedicine(
                null,
                prescriptionId,
                medicine,
                quantity,
                dosage,
                frequency,
                duration,
                medicine.getUnitPrice());
    }

    private void displayPrescribedMedicineDetails(Prescription.PrescribedMedicine prescribedMedicine) {
        System.out.println("Prescription ID: " + prescribedMedicine.getPrescriptionId());
        System.out.println("Medicine ID: " + prescribedMedicine.getMedicine().getMedicineId());
        System.out.println("Quantity: " + prescribedMedicine.getQuantity());
        System.out.println("Dosage: " + prescribedMedicine.getDosage());
        System.out.println("Frequency: " + prescribedMedicine.getFrequency());
        System.out.println("Duration: " + prescribedMedicine.getDuration() + " days");
    }

    private void removeMedicineFromPrescription() {
        ConsoleUtils.printHeader("Remove Medicine from Prescription");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        System.out.println();

        Prescription prescription = pharmacyControl.findPrescriptionById(prescriptionId);
        if (prescription == null) {
            System.out.println("Prescription not found.");
            return;
        } else {
            ConsoleUtils.printHeader("Prescription Overview");
            System.out.println(prescription);
            for (Prescription.PrescribedMedicine prescribedMedicine : prescription.getPrescribedMedicines()) {
                System.out.println(prescribedMedicine);
            }
            System.out.println();
        }

        String prescribedMedicineId = ConsoleUtils.getStringInput(scanner, "Enter prescribed medicine ID: ");
        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to remove this medicine from this prescription? (Y/N): ");
        if (confirm) {
                if (pharmacyControl.removeMedicineFromPrescription(prescriptionId, prescribedMedicineId)) {
                    System.out.println("Medicine removed from prescription successfully.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println("Medicine not removed from prescription.");
                    ConsoleUtils.waitMessage();
                }
        } else {
            System.out.println("Medicine not removed from prescription.");
            ConsoleUtils.waitMessage();
        }
    }

    private void updateMedicineInPrescription() {
        ConsoleUtils.printHeader("Update Medicine in Prescription");
        System.out.println("Leave blank if you don't want to update the field.\n");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        System.out.println();
        Prescription prescription = pharmacyControl.findPrescriptionById(prescriptionId);
        if (prescription == null) {
            System.out.println("Prescription not found.");
            return;
        } else {
            ConsoleUtils.printHeader("Prescription Overview");
            System.out.println(prescription);
            for (Prescription.PrescribedMedicine prescribedMedicine : prescription.getPrescribedMedicines()) {
                System.out.println(prescribedMedicine);
            }
            System.out.println();
        }

        String prescribedMedicineId = ConsoleUtils.getStringInput(scanner, "Enter prescribed medicine ID: ");
        System.out.println();

        Prescription.PrescribedMedicine prescribedMedicine = prescription
                .findPrescribedMedicineById(prescribedMedicineId);

        if (prescribedMedicine == null) {
            System.out.println("Prescribed medicine not found.");
            return;
        }

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Do you want to update the prescribed medicine? (Y/N): ");
        if (confirm) {
                System.out.println();
                prescribedMedicine = getAndReplacePrescribedMedicineDetailsFromUser(prescribedMedicine);
                if (prescribedMedicine == null) {
                    System.out.println("Medicine not updated in prescription.");
                    ConsoleUtils.waitMessage();
                    return;
                }
        } else {
            System.out.println("Medicine not updated in prescription.");
            ConsoleUtils.waitMessage();
        }
        System.out.println();

        ConsoleUtils.printHeader("Medicine Overview");
        System.out.println("Prescribed Medicine ID: " + prescribedMedicine.getPrescribedMedicineId());
        displayPrescribedMedicineDetails(prescribedMedicine);
        System.out.println();

        confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to update this medicine in this prescription? (Y/N): ");
        if (confirm) {
                if (pharmacyControl.updateMedicineInPrescription(prescribedMedicine)) {
                    System.out.println("Medicine updated in prescription successfully.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println("Medicine not updated in prescription.");
                    ConsoleUtils.waitMessage();
                }
        } else {
            System.out.println("Medicine not updated in prescription.");
            ConsoleUtils.waitMessage();
        }
    }

    private Prescription.PrescribedMedicine getAndReplacePrescribedMedicineDetailsFromUser(
            Prescription.PrescribedMedicine prescribedMedicine) {
        String medicineId = ConsoleUtils.getStringInput(scanner, "Enter medicine ID: ",
                prescribedMedicine.getMedicine().getMedicineId());
        int quantity = ConsoleUtils.getIntInput(scanner, "Enter quantity: ", prescribedMedicine.getQuantity());
        String dosage = ConsoleUtils.getStringInput(scanner, "Enter dosage: ", prescribedMedicine.getDosage());
        String frequency = ConsoleUtils.getStringInput(scanner, "Enter frequency: ", prescribedMedicine.getFrequency());
        int duration = ConsoleUtils.getIntInput(scanner, "Enter duration (days): ", prescribedMedicine.getDuration());

        Medicine medicine = pharmacyControl.findMedicineById(medicineId);
        if (medicine == null) {
            System.out.println("\nMedicine not found.");
            return null;
        }

        return new Prescription.PrescribedMedicine(
                prescribedMedicine.getPrescribedMedicineId() == null ? ""
                        : prescribedMedicine.getPrescribedMedicineId(),
                prescribedMedicine.getPrescriptionId(), medicine, quantity, dosage, frequency, duration,
                prescribedMedicine.getUnitPrice());
    }

    private void updatePrescriptionDetails() {
        ConsoleUtils.printHeader("Update Prescription Details");
        String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter prescription ID: ");
        System.out.println();
        Prescription prescription = pharmacyControl.findPrescriptionById(prescriptionId);
        if (prescription == null) {
            System.out.println("Prescription not found.");
            return;
        }
        else {
            ConsoleUtils.printHeader("Prescription Overview");
            System.out.println(prescription);
            System.out.println();
        }
        
        String prescriptionStatus = "";
        Prescription.PrescriptionStatus status = null;
        
        System.out.println("Leave blank if you don't want to update the field.");
        ConsoleUtils.printSeparator('=', 50);
        String instructions = ConsoleUtils.getStringInput(scanner, "Enter instructions: ", prescription.getInstructions());
        LocalDate expiryDate = ConsoleUtils.getDateInput(scanner, "Enter expiry date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY, prescription.getExpiryDate());

        // Validate prescription status
        while (true) {
            try {
                status = Prescription.PrescriptionStatus.valueOf(prescriptionStatus.toUpperCase());
                break;
            } catch (IllegalArgumentException exception) {
                System.out.println("Invalid prescription status. Please enter a valid prescription status.");
                ConsoleUtils.waitMessage();
                prescriptionStatus = ConsoleUtils.getStringInput(scanner, "Enter prescription status: ", prescription.getStatus().toString());
            }
        }

        System.out.println();

        ConsoleUtils.printHeader("Updated Prescription Details");
        System.out.println("Instructions: " + instructions);
        System.out.println("Expiry Date: " + expiryDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        System.out.println("Status: " + status);
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to update these prescription details? (Y/N): ");
        if (confirm) {
            prescription.setInstructions(instructions);
            prescription.setExpiryDate(expiryDate);
            prescription.setStatus(status);

            if (pharmacyControl.updatePrescription(prescription)) {
                System.out.println("Prescription details updated successfully.");
                ConsoleUtils.waitMessage();
            } else {
                System.out.println("Prescription details not updated.");
                ConsoleUtils.waitMessage();
            }
        } else {
            System.out.println("Prescription details not updated.");
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
            for (Prescription.PrescribedMedicine prescribedMedicine : prescription.getPrescribedMedicines()) {
                System.out.println(prescribedMedicine);
            }
            System.out.println();
        }

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to dispense this prescription? (Y/N): ");
        if (confirm) {
                if (pharmacyControl.dispensePrescription(prescriptionId)) {
                    System.out.println("Prescription dispensed successfully.");
                    ConsoleUtils.waitMessage();
                } else {
                    System.out.println("Prescription not dispensed.");
                    ConsoleUtils.waitMessage();
                }
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
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + medicine);
                }
                break;
            case 2:
                String medicineName = ConsoleUtils.getStringInput(scanner, "Enter Medicine Name: ");
                medicine = pharmacyControl.findMedicineByName(medicineName);
                if (medicine == null) {
                    System.out.println("Medicine not found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + medicine);
                }
                break;
            case 3:
                String genericName = ConsoleUtils.getStringInput(scanner, "Enter Generic Name: ");
                medicine = pharmacyControl.findMedicineByGenericName(genericName);
                if (medicine == null) {
                    System.out.println("Medicine not found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + medicine);
                }
                break;
            case 4:
                String manufacturer = ConsoleUtils.getStringInput(scanner, "Enter Manufacturer: ");
                ArrayBucketList<String, Medicine> medicines = pharmacyControl.findMedicineByManufacturer(manufacturer);
                if (medicines.isEmpty()) {
                    System.out.println("No medicines found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + medicines.parseElementsToString());
                }
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. AVAILABLE  2. LOW_STOCK  3. OUT_OF_STOCK  4. DISCONTINUED");
                int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
                medicines = pharmacyControl.findMedicineByStatus(statusChoice);
                if (medicines.isEmpty()) {
                    System.out.println("No medicines found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + medicines.parseElementsToString());
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
        ArrayBucketList<String, Prescription> prescriptions = null;

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 5);
        System.out.println();

        switch (choice) {
            case 1:
                String prescriptionId = ConsoleUtils.getStringInput(scanner, "Enter Prescription ID: ");
                prescription = pharmacyControl.findPrescriptionById(prescriptionId);
                if (prescription == null) {
                    System.out.println("Prescription not found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + prescription + "\n");
                    for (Prescription.PrescribedMedicine prescribedMedicine : prescription.getPrescribedMedicines()) {
                        System.out.println(prescribedMedicine);
                    }
                    System.out.println();
                }
                break;
            case 2:
                String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
                prescriptions = pharmacyControl.findPrescriptionsByPatient(patientId);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + prescriptions.parseElementsToString() + "\n");
                }
                break;
            case 3:
                String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
                prescriptions = pharmacyControl.findPrescriptionsByDoctor(doctorId);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + prescriptions.parseElementsToString());
                }
                break;
            case 4:
                System.out.println("Select status:");
                System.out.println("1. ACTIVE  2. DISPENSED  3. EXPIRED  4. CANCELLED");
                int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
                prescriptions = pharmacyControl.findPrescriptionsByStatus(statusChoice);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");  
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + prescriptions.parseElementsToString());
                }
                break;
            case 5:
                LocalDate startDate = ConsoleUtils.getDateInput(scanner, "Enter start date (DD-MM-YYYY): ", DateType.PAST_DATE_ONLY);
                LocalDate endDate = ConsoleUtils.getDateInput(scanner, "Enter end date (DD-MM-YYYY): ", DateType.PAST_DATE_ONLY);
                prescriptions = pharmacyControl.findPrescriptionsByDateRange(startDate, endDate);
                if (prescriptions.isEmpty()) {
                    System.out.println("No prescriptions found.");
                } else {
                    System.out.println();
                    ConsoleUtils.printHeader("Search Result");
                    System.out.println("\n" + prescriptions.parseElementsToString());
                }
                break;
            default:
                System.out.println("Invalid choice.");
        }
        ConsoleUtils.waitMessage();
    }

    private void generatePharmacyReports() {
        ConsoleUtils.printHeader("Generate Pharmacy Reports");
        System.out.println("1. Medicine Stock Report");
        System.out.println("2. Prescription Report");
        System.out.println("3. All Reports");
        System.out.println("4. Back to Pharmacy Menu");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                generateMedicineStockReport();
                break;
            case 2:
                generatePrescriptionReport();
                break;
            case 3:
                generateMedicineStockReport();
                generatePrescriptionReport();
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateMedicineStockReport() {
        ConsoleUtils.printHeader("Medicine Stock Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Medicine ID");
        System.out.println("2. Medicine Name");
        System.out.println("3. Generic Name");
        System.out.println("4. Stock Quantity");
        System.out.println("5. Unit Price");
        System.out.println("6. Expiry Date");
        System.out.println("7. Status");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
        
        System.out.println();
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);

        System.out.println();
        
        String sortBy = getMedicineSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(pharmacyControl.generateMedicineStockReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generatePrescriptionReport() {
        ConsoleUtils.printHeader("Prescription Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Prescription ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Prescription Date");
        System.out.println("5. Status");
        System.out.println("6. Total Cost");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);

        System.out.println();
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);

        System.out.println();
        
        String sortBy = getPrescriptionSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(pharmacyControl.generatePrescriptionReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private String getMedicineSortField(int choice) {
        return switch (choice) {
            case 1 -> "id";
            case 2 -> "name";
            case 3 -> "generic";
            case 4 -> "stock";
            case 5 -> "price";
            case 6 -> "expiry";
            case 7 -> "status";
            default -> "name";
        };
    }

    private String getPrescriptionSortField(int choice) {
        return switch (choice) {
            case 1 -> "id";
            case 2 -> "patient";
            case 3 -> "doctor";
            case 4 -> "date";
            case 5 -> "status";
            case 6 -> "cost";
            default -> "date";
        };
    }
}
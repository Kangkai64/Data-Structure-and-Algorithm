package boundary;

import control.PatientManagementControl;
import entity.Address;
import entity.BloodType;
import adt.ArrayBucketList;
import utility.ConsoleUtils;

import java.util.Scanner;

/**
 * Patient Management User Interface
 * Handles all patient management user interactions
 */
public class PatientManagementUI {
    private Scanner scanner;
    private PatientManagementControl patientControl;

    public PatientManagementUI() {
        this.scanner = new Scanner(System.in);
        this.patientControl = new PatientManagementControl();
    }

    public void displayPatientManagementMenu() {
        while (true) {
            System.out.println("\n=== PATIENT MANAGEMENT MODULE ===");
            System.out.println("1. Register New Patient");
            System.out.println("2. Update Patient Record");
            System.out.println("3. Deactivate Patient");
            System.out.println("4. Add Patient to Queue");
            System.out.println("5. Get Next Patient from Queue");
            System.out.println("6. Search Patient");
            System.out.println("7. Generate Patient Reports");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 8);
            
            switch (choice) {
                case 1:
                    registerNewPatient();
                    break;
                case 2:
                    updatePatientRecord();
                    break;
                case 3:
                    deactivatePatient();
                    break;
                case 4:
                    addPatientToQueue();
                    break;
                case 5:
                    getNextPatientFromQueue();
                    break;
                case 6:
                    searchPatient();
                    break;
                case 7:
                    generatePatientReports();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void registerNewPatient() {
        System.out.println("\n=== REGISTER NEW PATIENT ===");
        String fullName = ConsoleUtils.getStringInput(scanner, "Enter full name: ");
        String icNumber = ConsoleUtils.getStringInput(scanner, "Enter IC number (DDMMYY-XX-XXXX): ");
        String email = ConsoleUtils.getStringInput(scanner, "Enter email: ");
        String phoneNumber = ConsoleUtils.getStringInput(scanner, "Enter phone number (0XX-XXXXXXX): ");
        
        // Get address details
        String street = ConsoleUtils.getStringInput(scanner, "Enter street: ");
        String city = ConsoleUtils.getStringInput(scanner, "Enter city: ");
        String state = ConsoleUtils.getStringInput(scanner, "Enter state: ");
        String postalCode = ConsoleUtils.getStringInput(scanner, "Enter postal code: ");
        String country = ConsoleUtils.getStringInput(scanner, "Enter country: ");
        
        Address address = new Address(street, city, state, postalCode, country);
        
        String wardNumber = ConsoleUtils.getStringInput(scanner, "Enter ward number: ");
        
        System.out.println("Select blood type:");
        System.out.println("1. A_POSITIVE  2. A_NEGATIVE  3. B_POSITIVE  4. B_NEGATIVE");
        System.out.println("5. AB_POSITIVE 6. AB_NEGATIVE 7. O_POSITIVE  8. O_NEGATIVE  9. OTHERS");
        System.out.print("Enter choice: ");
        int bloodTypeChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 9);
        
        BloodType bloodType = getBloodTypeFromChoice(bloodTypeChoice);
        
        String allergiesInput = ConsoleUtils.getStringInput(scanner, "Enter allergies (comma-separated, or 'None'): ");
        String allergies = allergiesInput.equalsIgnoreCase("None") ? "None" : allergiesInput;
        
        String emergencyContact = ConsoleUtils.getStringInput(scanner, "Enter emergency contact (0XX-XXXXXXX): ");
        
        boolean success = patientControl.registerPatient(fullName, icNumber, email, phoneNumber, 
                                                       address, wardNumber, bloodType, allergiesInput, emergencyContact);
        
        if (success) {
            System.out.println("Patient registered successfully!");
        } else {
            System.out.println("Failed to register patient.");
        }
    }

    private void updatePatientRecord() {
        System.out.println("\n=== UPDATE PATIENT RECORD ===");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to update: ");
        
        // For now, just show a placeholder
        System.out.println("Update Patient Record - Implementation needed");
        System.out.println("Patient ID: " + patientId);
    }

    private void deactivatePatient() {
        System.out.println("\n=== DEACTIVATE PATIENT ===");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to deactivate: ");
        
        // For now, just show a placeholder
        System.out.println("Deactivate Patient - Implementation needed");
        System.out.println("Patient ID: " + patientId);
    }

    private void addPatientToQueue() {
        System.out.println("\n=== ADD PATIENT TO QUEUE ===");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to add to queue: ");
        
        // For now, just show a placeholder
        System.out.println("Add Patient to Queue - Implementation needed");
        System.out.println("Patient ID: " + patientId);
    }

    private void getNextPatientFromQueue() {
        System.out.println("\n=== GET NEXT PATIENT FROM QUEUE ===");
        
        // For now, just show a placeholder
        System.out.println("Get Next Patient from Queue - Implementation needed");
    }

    private void searchPatient() {
        System.out.println("\n=== SEARCH PATIENT ===");
        System.out.println("1. Search by Patient ID");
        System.out.println("2. Search by Full Name");
        System.out.println("3. Search by Email");
        System.out.println("4. Search by IC Number");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        
        switch (choice) {
            case 1:
                String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
                System.out.println("Search by Patient ID - Implementation needed");
                break;
            case 2:
                String fullName = ConsoleUtils.getStringInput(scanner, "Enter Full Name: ");
                System.out.println("Search by Full Name - Implementation needed");
                break;
            case 3:
                String email = ConsoleUtils.getStringInput(scanner, "Enter Email: ");
                System.out.println("Search by Email - Implementation needed");
                break;
            case 4:
                String icNumber = ConsoleUtils.getStringInput(scanner, "Enter IC Number: ");
                System.out.println("Search by IC Number - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generatePatientReports() {
        System.out.println("\n=== PATIENT REPORTS ===");
        System.out.println(patientControl.generatePatientRegistrationReport());
        System.out.println(patientControl.generateQueueStatusReport());
    }

    private BloodType getBloodTypeFromChoice(int choice) {
        switch (choice) {
            case 1: return BloodType.A_POSITIVE;
            case 2: return BloodType.A_NEGATIVE;
            case 3: return BloodType.B_POSITIVE;
            case 4: return BloodType.B_NEGATIVE;
            case 5: return BloodType.AB_POSITIVE;
            case 6: return BloodType.AB_NEGATIVE;
            case 7: return BloodType.O_POSITIVE;
            case 8: return BloodType.O_NEGATIVE;
            case 9: return BloodType.OTHERS;
            default: return BloodType.A_POSITIVE;
        }
    }
} 
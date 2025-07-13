package boundary;

import control.PatientManagementControl;
import entity.Address;
import entity.BloodType;
import adt.ArrayList;
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
            
            int choice = getIntInput();
            
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
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine();
        System.out.print("Enter IC number: ");
        String icNumber = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();
        
        // Get address details
        System.out.print("Enter street: ");
        String street = scanner.nextLine();
        System.out.print("Enter city: ");
        String city = scanner.nextLine();
        System.out.print("Enter state: ");
        String state = scanner.nextLine();
        System.out.print("Enter postal code: ");
        String postalCode = scanner.nextLine();
        System.out.print("Enter country: ");
        String country = scanner.nextLine();
        
        Address address = new Address("", "", street, city, state, postalCode, country);
        
        System.out.print("Enter ward number: ");
        String wardNumber = scanner.nextLine();
        
        System.out.println("Select blood type:");
        System.out.println("1. A_POSITIVE  2. A_NEGATIVE  3. B_POSITIVE  4. B_NEGATIVE");
        System.out.println("5. AB_POSITIVE 6. AB_NEGATIVE 7. O  8. OTHERS");
        System.out.print("Enter choice: ");
        int bloodTypeChoice = getIntInput();
        
        BloodType bloodType = getBloodTypeFromChoice(bloodTypeChoice);
        
        System.out.print("Enter allergies (comma-separated, or 'None'): ");
        String allergiesInput = scanner.nextLine();
        ArrayList<String> allergies = new ArrayList<>();
        if (!allergiesInput.equalsIgnoreCase("None")) {
            String[] allergyArray = allergiesInput.split(",");
            for (String allergy : allergyArray) {
                allergies.add(allergy.trim());
            }
        } else {
            allergies.add("None");
        }
        
        System.out.print("Enter emergency contact: ");
        String emergencyContact = scanner.nextLine();
        
        boolean success = patientControl.registerPatient(fullName, icNumber, email, phoneNumber, 
                                                       address, wardNumber, bloodType, 
                                                       allergies, emergencyContact);
        
        if (success) {
            System.out.println("Patient registered successfully!");
        } else {
            System.out.println("Failed to register patient.");
        }
    }

    private void updatePatientRecord() {
        System.out.println("\n=== UPDATE PATIENT RECORD ===");
        System.out.print("Enter patient ID to update: ");
        String patientId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Update Patient Record - Implementation needed");
        System.out.println("Patient ID: " + patientId);
    }

    private void deactivatePatient() {
        System.out.println("\n=== DEACTIVATE PATIENT ===");
        System.out.print("Enter patient ID to deactivate: ");
        String patientId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Deactivate Patient - Implementation needed");
        System.out.println("Patient ID: " + patientId);
    }

    private void addPatientToQueue() {
        System.out.println("\n=== ADD PATIENT TO QUEUE ===");
        System.out.print("Enter patient ID to add to queue: ");
        String patientId = scanner.nextLine();
        
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
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("Enter Patient ID: ");
                String patientId = scanner.nextLine();
                System.out.println("Search by Patient ID - Implementation needed");
                break;
            case 2:
                System.out.print("Enter Full Name: ");
                String fullName = scanner.nextLine();
                System.out.println("Search by Full Name - Implementation needed");
                break;
            case 3:
                System.out.print("Enter Email: ");
                String email = scanner.nextLine();
                System.out.println("Search by Email - Implementation needed");
                break;
            case 4:
                System.out.print("Enter IC Number: ");
                String icNumber = scanner.nextLine();
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
            case 7: return BloodType.O;
            case 8: return BloodType.OTHERS;
            default: return BloodType.A_POSITIVE;
        }
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
} 
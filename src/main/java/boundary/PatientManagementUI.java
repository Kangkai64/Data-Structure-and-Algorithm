package boundary;

import java.sql.SQLException;
import java.util.Scanner;

import control.AddressManagementControl;
import control.PatientManagementControl;
import entity.Address;
import entity.BloodType;
import entity.Patient;
import utility.ConsoleUtils;

/**
 * Patient Management User Interface
 * Handles all patient management user interactions
 */
public class PatientManagementUI {
    private Scanner scanner;
    private PatientManagementControl patientControl;
    private AddressManagementControl addressControl;

    public PatientManagementUI() {
        this.scanner = new Scanner(System.in);
        this.patientControl = new PatientManagementControl();
        this.addressControl = new AddressManagementControl();
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
        String icNumber = ConsoleUtils.getICInput(scanner, "Enter IC number (DDMMYY-XX-XXXX): ");
        String email = ConsoleUtils.getEmailInput(scanner, "Enter email: ");
        String phoneNumber = ConsoleUtils.getPhoneInput(scanner, "Enter phone number (0XX-XXXXXXX): ");
        String icNumber = ConsoleUtils.getICInput(scanner, "Enter IC number (YYMMDD-XX-XXXX): ");
        String email = ConsoleUtils.getStringInput(scanner, "Enter email: ");
        String phoneNumber = ConsoleUtils.getPhoneInput(scanner, "Enter phone number (0XX-XXXXXXX): ");
        
        // Get address details
        String street = ConsoleUtils.getStringInput(scanner, "Enter street: ");
        String city = ConsoleUtils.getStringInput(scanner, "Enter city: ");
        String state = ConsoleUtils.getStringInput(scanner, "Enter state: ");
        String postalCode = ConsoleUtils.getPostalCodeInput(scanner, "Enter postal code (5 digits): ");
        Integer postalCode = ConsoleUtils.getIntInput(scanner, "Enter postal code: ", 0, 99999);
        String country = ConsoleUtils.getStringInput(scanner, "Enter country: ");

        String wardNumber = ConsoleUtils.getWardNumberInput(scanner, "Enter ward number (WXXXX): ");
        
        System.out.println("Select blood type:");
        System.out.println("1. A_POSITIVE  2. A_NEGATIVE  3. B_POSITIVE  4. B_NEGATIVE");
        System.out.println("5. AB_POSITIVE 6. AB_NEGATIVE 7. O_POSITIVE  8. O_NEGATIVE  9. OTHERS");
        System.out.print("Enter choice: ");
        int bloodTypeChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 9);
        
        BloodType bloodType = getBloodTypeFromChoice(bloodTypeChoice);
        
        String allergiesInput = ConsoleUtils.getStringInput(scanner, "Enter allergies (comma-separated, or 'None'): ");
        String allergies = allergiesInput.equalsIgnoreCase("None") ? "None" : allergiesInput;
        
        String emergencyContact = ConsoleUtils.getPhoneInput(scanner, "Enter emergency contact (0XX-XXXXXXX): ");

        Address address = new Address(street, city, state, postalCode.toString(), country);
        try {
            addressControl.addAddress(address);
            address = addressControl.getAddressById(address.getAddressId());
            System.out.println("Address added successfully!");
        } catch (SQLException e) {
            System.out.println("Failed to add address: " + e.getMessage());
        }
        
        boolean success = patientControl.registerPatient(fullName, icNumber, email, phoneNumber, 
                                                       address, wardNumber, bloodType, allergiesInput, emergencyContact);
        
        if (success) {
            System.out.println("Patient registered successfully!");
            scanner.nextLine();
        } else {
            System.out.println("Failed to register patient.");
            scanner.nextLine();
        }
    }

    private void updatePatientRecord() {
        System.out.println("\n=== UPDATE PATIENT RECORD ===");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to update: ");
        
        entity.Patient patient = patientControl.findPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
            scanner.nextLine();
            return;
        }

        System.out.println("Press [Enter] to accept the default value");

        String fullName = ConsoleUtils.getStringInput(scanner, "Full name [" + patient.getFullName() + "]: ", patient.getFullName());
        String email = ConsoleUtils.getEmailInput(scanner, "Email [" + patient.getEmail() + "]: ", patient.getEmail());
        String phoneNumber = ConsoleUtils.getPhoneInput(scanner, "Phone [" + patient.getPhoneNumber() + "]: ", patient.getPhoneNumber());

        Address address = patient.getAddress();
        String street = ConsoleUtils.getStringInput(scanner, "Street [" + address.getStreet() + "]: ", address.getStreet());
        String city = ConsoleUtils.getStringInput(scanner, "City [" + address.getCity() + "]: ", address.getCity());
        String state = ConsoleUtils.getStringInput(scanner, "State [" + address.getState() + "]: ", address.getState());
        String postalCode = ConsoleUtils.getPostalCodeInput(scanner, "Postal code [" + address.getZipCode() + "]: ", address.getZipCode());
        String country = ConsoleUtils.getStringInput(scanner, "Country [" + address.getCountry() + "]: ", address.getCountry());

        Address updatedAddress = new Address(street, city, state, postalCode, country);
        updatedAddress.setAddressId(address.getAddressId());

        String wardNumber = ConsoleUtils.getWardNumberInput(scanner, "Ward number [" + patient.getWardNumber() + "]: ", patient.getWardNumber());

        System.out.println("Select blood type (Enter to keep current):");
        System.out.println("1. A_POSITIVE  2. A_NEGATIVE  3. B_POSITIVE  4. B_NEGATIVE");
        System.out.println("5. AB_POSITIVE 6. AB_NEGATIVE 7. O_POSITIVE  8. O_NEGATIVE  9. OTHERS");
        System.out.println("\n>>> Current: " + patient.getBloodType());
        int defaultChoice;
        switch (patient.getBloodType()) {
            case A_POSITIVE: defaultChoice = 1; break;
            case A_NEGATIVE: defaultChoice = 2; break;
            case B_POSITIVE: defaultChoice = 3; break;
            case B_NEGATIVE: defaultChoice = 4; break;
            case AB_POSITIVE: defaultChoice = 5; break;
            case AB_NEGATIVE: defaultChoice = 6; break;
            case O_POSITIVE: defaultChoice = 7; break;
            case O_NEGATIVE: defaultChoice = 8; break;
            case OTHERS: defaultChoice = 9; break;
            default: defaultChoice = 1;
        }
        int bloodTypeChoice = ConsoleUtils.getIntInput(scanner, "Enter choice (1-9): ", defaultChoice);
        BloodType bloodType = getBloodTypeFromChoice(bloodTypeChoice);

        String allergiesInput = ConsoleUtils.getStringInput(scanner, "Allergies [" + patient.getAllergies() + "]: ", patient.getAllergies());
        String emergencyContact = ConsoleUtils.getPhoneInput(scanner, "Emergency contact [" + patient.getEmergencyContact() + "]: ", patient.getEmergencyContact());

        try {
            addressControl.updateAddress(updatedAddress);
        } catch (SQLException e) {
            System.out.println("Failed to update address: " + e.getMessage());
            return;
        }

        boolean success = patientControl.updatePatientRecord(
            patient.getPatientId(),
            fullName,
            email,
            phoneNumber,
            updatedAddress,
            wardNumber,
            bloodType,
            allergiesInput,
            emergencyContact
        );

        if (success) {
            System.out.println("Patient updated successfully!");
            scanner.nextLine();
        } else {
            System.out.println("Failed to update patient.");
            scanner.nextLine();
        }
    }

    private void deactivatePatient() {
        System.out.println("\n=== DEACTIVATE PATIENT ===");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to deactivate: ");
        
            entity.Patient patient = patientControl.findPatientById(patientId);
            if (patient == null) {
                System.out.println("Patient not found.");
                scanner.nextLine();
                return;
            }

            System.out.println("Patient found: " + patient.getFullName() + " (ID: " + patient.getPatientId() + ")");
            String confirm = ConsoleUtils.getStringInput(scanner, "Are you sure you want to deactivate this patient? (Y/N): ").trim().toUpperCase();

            if (!confirm.equals("Y")) {
                System.out.println("Deactivation cancelled.");
                scanner.nextLine();
                return;
            }

            boolean success = patientControl.deactivatePatient(patientId);

            if (success) {
                System.out.println("Patient deactivated successfully!");
                scanner.nextLine();
            } else {
                System.out.println("Failed to deactivate patient.");
                scanner.nextLine();
            }
    }

    private void addPatientToQueue() {
        ConsoleUtils.printHeader("QUEUE - ADD PATIENT");
        while (true) {
            String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to add (Press [Enter] to exit): ", "");
            if (patientId.isEmpty()) {
                System.out.println("Returning to Patient Management menu.");
                break;
            }

            Patient patient = patientControl.findPatientById(patientId);
            if (patient == null) {
                System.out.println("Patient not found.");
                boolean tryAnother = ConsoleUtils.getBooleanInput(scanner, "Try another patient ID? (Y/N): ");
                if (!tryAnother) {
                    System.out.println("Stopping add-to-queue.");
                    break;
                }
                continue;
            }

            if (patientControl.isPatientInQueue(patient)) {
                System.out.println("Patient is already in the queue.");
                boolean addMore = ConsoleUtils.getBooleanInput(scanner, "Add another patient? (Y/N): ");
                if (!addMore) {
                    System.out.println("Stopping add-to-queue.");
                    break;
                }
                continue;
            }

            boolean success = patientControl.addPatientToQueue(patient);
            if (success) {
                System.out.println("Patient added to queue successfully!");
                System.out.println(">>> Current Queue Size: " + patientControl.getQueueSize());
            } else {
                System.out.println("Failed to add patient. Patient may be inactive.");
            }

            boolean addAnother = ConsoleUtils.getBooleanInput(scanner, "Add another patient to queue? (Y/N): ");
            if (!addAnother) {
                System.out.println("Stopping add-to-queue.");
                break;
            }
        }
        scanner.nextLine();
    }

    private void getNextPatientFromQueue() {
        ConsoleUtils.printHeader("QUEUE - CALL NEXT PATIENT");
        if (patientControl.getQueueSize() == 0) {
            System.out.println("No patients in queue. Please add patients first.");
            scanner.nextLine();
            return;
        }

        while (true) {
            Patient next = patientControl.getNextPatientFromQueue();
            if (next == null) {
                System.out.println("No patients in queue.");
                break;
            }

            System.out.println("Next Patient:");
            System.out.println("ID    : " + next.getPatientId());
            System.out.println("Name  : " + next.getFullName());
            System.out.println("IC    : " + next.getICNumber());
            int remaining = patientControl.getQueueSize();
            System.out.println(">>> Queue Size Remaining: " + remaining);

            if (remaining == 0) {
                System.out.println("No more patients in the queue.");
                break;
            }

            boolean callAnother = ConsoleUtils.getBooleanInput(scanner, "Call next patient? (Y/N): ");
            if (!callAnother) {
                System.out.println("Stopping queue processing.");
                break;
            }
        }
        scanner.nextLine();
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
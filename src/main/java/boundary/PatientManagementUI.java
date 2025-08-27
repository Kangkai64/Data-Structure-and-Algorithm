package boundary;

import java.sql.SQLException;
import java.util.Scanner;

import adt.ArrayBucketList;
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
            ConsoleUtils.printHeader("PATIENT MANAGEMENT MODULE");
            System.out.println("1. Register New Patient");
            System.out.println("2. Update Patient Record");
            System.out.println("3. Deactivate Patient");
            System.out.println("4. Add Patient to Queue");
            System.out.println("5. Get Next Patient from Queue");
            System.out.println("6. Search Patient");
            System.out.println("7. Generate Patient Reports");
            System.out.println("8. Back to Main Menu");

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
        
        // Get address details
        String street = ConsoleUtils.getStringInput(scanner, "Enter street: ");
        String city = ConsoleUtils.getStringInput(scanner, "Enter city: ");
        String state = ConsoleUtils.getStringInput(scanner, "Enter state: ");
        String postalCode = ConsoleUtils.getPostalCodeInput(scanner, "Enter postal code (5 digits): ");
        
        String country = ConsoleUtils.getStringInput(scanner, "Enter country: ");

        System.out.println("Select blood type:");
        System.out.println("1. A_POSITIVE  2. A_NEGATIVE  3. B_POSITIVE  4. B_NEGATIVE");
        System.out.println("5. AB_POSITIVE 6. AB_NEGATIVE 7. O_POSITIVE  8. O_NEGATIVE  9. OTHERS");
        int bloodTypeChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 9);
        
        BloodType bloodType = patientControl.getBloodTypeFromChoice(bloodTypeChoice);
        
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
                                                       address, bloodType, allergiesInput, emergencyContact);
        
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
        BloodType bloodType = patientControl.getBloodTypeFromChoice(bloodTypeChoice);

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

            // Check if patient is already inactive
            if (!patient.isActive()) {
                System.out.println("Patient " + patient.getFullName() + " (ID: " + patient.getPatientId() + ") is already inactive.");
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
        ConsoleUtils.printHeader("Search Patients");
        System.out.println("1. Search by Patient ID");
        System.out.println("2. Search by IC Number");
        System.out.println("3. Search by Full Name");
        System.out.println("4. Search by Email");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                searchPatientById();
                break;
            case 2:
                searchPatientByIcNumber();
                break;
            case 3:
                searchPatientsByName();
                break;
            case 4:
                searchPatientByEmail();
                break;
            default:
                System.out.println("Invalid choice.");
        }
        ConsoleUtils.waitMessage();
    }
    
    private void searchPatientById() {
        ConsoleUtils.printHeader("Search Patient by ID (Patient Details)");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
        Patient patient = patientControl.findPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
        } else {
            System.out.println(patientControl.displayPatientSearchResult(patient, "Patient ID: " + patientId));
        }
    }
    
    private void searchPatientsByName() {
        ConsoleUtils.printHeader("Search Patients by Name(Patient List)");
        String name = ConsoleUtils.getStringInput(scanner, "Enter patient name (partial match): ");
        ArrayBucketList<String, Patient> patients = patientControl.findPatientsByName(name);
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
        } else {
            System.out.println();
            System.out.println(patientControl.displayPatientSearchResults(patients, "Name: " + name));
        }
    }
    
    private void searchPatientByEmail() {
        ConsoleUtils.printHeader("Search Patients by Email (Patient List)");
        String email = ConsoleUtils.getStringInput(scanner, "Enter email (partial match): ");
        ArrayBucketList<String, Patient> patients = patientControl.findPatientsByEmail(email);
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
        } else {
            System.out.println();
            System.out.println(patientControl.displayPatientSearchResults(patients, "Email: " + email));
        }
    }
    
    private void searchPatientByIcNumber() {
        ConsoleUtils.printHeader("Search Patient by IC Number (Patient Details)");
        String icNumber = ConsoleUtils.getStringInput(scanner, "Enter IC Number (XXXXXX-XX-XXXX): ");
        
        // Validate IC number format
        if (!icNumber.matches("\\d{6}-\\d{2}-\\d{4}")) {
            System.out.println("Invalid IC number format. Please use format: XXXXXX-XX-XXXX");
            return;
        }
        
        // Search for exact IC number match
        Patient patient = patientControl.findPatientByIcNumber(icNumber);
        if (patient == null) {
            System.out.println("Patient not found.");
        } else {
            System.out.println(patientControl.displayPatientSearchResult(patient, "IC Number: " + icNumber));
        }
    }
    
    private void generatePatientReports() {
        ConsoleUtils.printHeader("Generate Patient Record Summary");
        System.out.println("Select field to sort by:");
        System.out.println("1. Patient ID");
        System.out.println("2. Full Name");
        System.out.println("3. IC Number");
        System.out.println("4. Email");
        System.out.println("5. Phone Number");
        System.out.println("6. Registration Date");
        System.out.println("7. Status");
        System.out.println("8. Blood Type");
        System.out.println("9. Allergies");
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 9);

        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);

        String sortBy;
        switch (sortFieldChoice) {
            case 1: sortBy = "id"; break;
            case 2: sortBy = "name"; break;
            case 3: sortBy = "ic"; break;
            case 4: sortBy = "email"; break;
            case 5: sortBy = "phone"; break;
            case 6: sortBy = "regdate"; break;
            case 7: sortBy = "status"; break;
            case 8: sortBy = "blood"; break;
            case 9: sortBy = "allergies"; break;
            default: sortBy = "name"; break;
        }
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";

        System.out.println(patientControl.generatePatientRecordSummaryReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }
} 
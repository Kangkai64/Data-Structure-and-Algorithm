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
            ConsoleUtils.clearScreen();
            patientControl.loadActivePatients();
            ConsoleUtils.printHeader("Patient Management Module");
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
                    ConsoleUtils.waitMessage();
            }
        }
    }

    private void registerNewPatient() {
        ConsoleUtils.printHeader("Register New Patient");
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
            ConsoleUtils.waitMessage();
        }

        boolean success = patientControl.registerPatient(fullName, icNumber, email, phoneNumber,
                address, bloodType, allergiesInput, emergencyContact);

        if (success) {
            System.out.println("Patient registered successfully!");
        } else {
            System.out.println("Failed to register patient.");
        }
        ConsoleUtils.waitMessage();
    }

    private void updatePatientRecord() {
        ConsoleUtils.printHeader("Update Patient Record");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to update: ");

        entity.Patient patient = patientControl.findPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        System.out.println("Press [Enter] to accept the default value");

        String fullName = ConsoleUtils.getStringInput(scanner, "Full name [" + patient.getFullName() + "]: ",
                patient.getFullName());
        String email = ConsoleUtils.getEmailInput(scanner, "Email [" + patient.getEmail() + "]: ", patient.getEmail());
        String phoneNumber = ConsoleUtils.getPhoneInput(scanner, "Phone [" + patient.getPhoneNumber() + "]: ",
                patient.getPhoneNumber());

        Address address = patient.getAddress();
        String street = ConsoleUtils.getStringInput(scanner, "Street [" + address.getStreet() + "]: ",
                address.getStreet());
        String city = ConsoleUtils.getStringInput(scanner, "City [" + address.getCity() + "]: ", address.getCity());
        String state = ConsoleUtils.getStringInput(scanner, "State [" + address.getState() + "]: ", address.getState());
        String postalCode = ConsoleUtils.getPostalCodeInput(scanner, "Postal code [" + address.getZipCode() + "]: ",
                address.getZipCode());
        String country = ConsoleUtils.getStringInput(scanner, "Country [" + address.getCountry() + "]: ",
                address.getCountry());

        Address updatedAddress = new Address(street, city, state, postalCode, country);
        updatedAddress.setAddressId(address.getAddressId());

        System.out.println("Select blood type (Enter to keep current):");
        System.out.println("1. A_POSITIVE  2. A_NEGATIVE  3. B_POSITIVE  4. B_NEGATIVE");
        System.out.println("5. AB_POSITIVE 6. AB_NEGATIVE 7. O_POSITIVE  8. O_NEGATIVE  9. OTHERS");
        System.out.println("\n>>> Current: " + patient.getBloodType());
        System.out.println("[Notice] If value is out of range. Defaulting to A_POSITIVE.");
        int defaultChoice;
        switch (patient.getBloodType()) {
            case A_POSITIVE:
                defaultChoice = 1;
                break;
            case A_NEGATIVE:
                defaultChoice = 2;
                break;
            case B_POSITIVE:
                defaultChoice = 3;
                break;
            case B_NEGATIVE:
                defaultChoice = 4;
                break;
            case AB_POSITIVE:
                defaultChoice = 5;
                break;
            case AB_NEGATIVE:
                defaultChoice = 6;
                break;
            case O_POSITIVE:
                defaultChoice = 7;
                break;
            case O_NEGATIVE:
                defaultChoice = 8;
                break;
            case OTHERS:
                defaultChoice = 9;
                break;
            default:
                defaultChoice = 1;
        }
        int bloodTypeChoice = ConsoleUtils.getIntInput(scanner, "Enter choice (1-9): ", defaultChoice);
        BloodType bloodType = patientControl.getBloodTypeFromChoice(bloodTypeChoice);

        String allergiesInput = ConsoleUtils.getStringInput(scanner, "Allergies [" + patient.getAllergies() + "]: ",
                patient.getAllergies());
        String emergencyContact = ConsoleUtils.getPhoneInput(scanner,
                "Emergency contact [" + patient.getEmergencyContact() + "]: ", patient.getEmergencyContact());

        try {
            addressControl.updateAddress(updatedAddress);
        } catch (SQLException e) {
            System.out.println("Failed to update address: " + e.getMessage());
            ConsoleUtils.waitMessage();
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
                emergencyContact);

        if (success) {
            System.out.println("Patient updated successfully!");
        } else {
            System.out.println("Failed to update patient.");
        }
        ConsoleUtils.waitMessage();
    }

    private void deactivatePatient() {
        ConsoleUtils.printHeader("Deactivate Patient");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to deactivate: ");

        entity.Patient patient = patientControl.findPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        if (!patient.isActive()) {
            System.out.println(
                    "Patient " + patient.getFullName() + " (ID: " + patient.getPatientId() + ") is already inactive.");
            ConsoleUtils.waitMessage();
            return;
        }

        System.out.println("Patient found: " + patient.getFullName() + " (ID: " + patient.getPatientId() + ")");
        String confirm = ConsoleUtils
                .getStringInput(scanner, "Are you sure you want to deactivate this patient? (Y/N): ").trim()
                .toUpperCase();

        if (!confirm.equals("Y")) {
            System.out.println("Deactivation cancelled.");
            ConsoleUtils.waitMessage();
            return;
        }

        boolean success = patientControl.deactivatePatient(patientId);

        if (success) {
            System.out.println("Patient deactivated successfully!");
        } else {
            System.out.println("Failed to deactivate patient.");
        }
        ConsoleUtils.waitMessage();
    }

    private void addPatientToQueue() {
        ConsoleUtils.printHeader("Add Patient to Queue");
        while (true) {
            String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID to add (Press [Enter] to exit): ",
                    "");
            if (patientId.isEmpty()) {
                System.out.println("Stopping add-to-queue.");
                System.out.println("Returning to Patient Management menu.");
                break;
            }

            Patient patient = patientControl.findPatientById(patientId);
            if (patient == null) {
                System.out.println("Patient not found.");
                ConsoleUtils.waitMessage();
                if (!getYesNoInput("Try another patient ID? (Y = Yes, Enter = Exit): ")) {
                    System.out.println("Stopping add-to-queue.");
                    break;
                }
                continue;
            }

            if (patientControl.isPatientInQueue(patient)) {
                System.out.println("Patient is already in the queue.");
                ConsoleUtils.waitMessage();
                if (!getYesNoInput("Add another patient? (Y = Yes, Enter = Exit): ")) {
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
            ConsoleUtils.waitMessage();

            if (!getYesNoInput("Add another patient to queue? (Y = Yes, Enter = Exit): ")) {
                System.out.println("Stopping add-to-queue.");
                break;
            }
        }
    }

    private boolean getYesNoInput(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim().toUpperCase();
        return input.equals("Y");
    }

    private void getNextPatientFromQueue() {
        ConsoleUtils.printHeader("Get Next Patient from Queue");
        if (patientControl.getQueueSize() == 0) {
            System.out.println("No patients in queue. Please add patients first.");
            ConsoleUtils.waitMessage();
            return;
        }

        while (true) {
            Patient next = patientControl.getNextPatientFromQueue();
            if (next == null) {
                System.out.println("No patients in queue.");
                ConsoleUtils.waitMessage();
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
                ConsoleUtils.waitMessage();
                break;
            }

            if (!getYesNoInput("Call next patient? (Y/N): ")) {
                System.out.println("Stopping queue processing.");
                break;
            }
        }
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
                ConsoleUtils.waitMessage();
        }
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
        ConsoleUtils.waitMessage();
    }

    private void searchPatientsByName() {
        ConsoleUtils.printHeader("Search Patients by Name (Patient List)");
        String name = ConsoleUtils.getStringInput(scanner, "Enter patient name (partial match): ");
        ArrayBucketList<String, Patient> patients = patientControl.findPatientsByName(name);
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
        } else {
            System.out.println();
            if (patients.getSize() > 1) {
                System.out.println("Found " + patients.getSize() + " patients.");
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                System.out.println();
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getPatientSortField();
                    System.out.println();
                    String sortOrder = getSortOrder();
                    System.out.println(patientControl.displaySortedPatientSearchResults(patients, "Name: " + name, sortBy, sortOrder));
                } else {
                    System.out.println(patientControl.displayPatientSearchResults(patients, "Name: " + name));
                }
            } else {
                System.out.println(patientControl.displayPatientSearchResults(patients, "Name: " + name));
            }
        }
        ConsoleUtils.waitMessage();
    }

    private void searchPatientByEmail() {
        ConsoleUtils.printHeader("Search Patients by Email (Patient List)");
        String email = ConsoleUtils.getStringInput(scanner, "Enter email (partial match): ");
        ArrayBucketList<String, Patient> patients = patientControl.findPatientsByEmail(email);
        if (patients.isEmpty()) {
            System.out.println("No patients found.");
        } else {
            System.out.println();
            if (patients.getSize() > 1) {
                System.out.println("Found " + patients.getSize() + " patients.");
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                System.out.println();
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getPatientSortField();
                    System.out.println();
                    String sortOrder = getSortOrder();
                    System.out.println(patientControl.displaySortedPatientSearchResults(patients, "Email: " + email, sortBy, sortOrder));
                } else {
                    System.out.println(patientControl.displayPatientSearchResults(patients, "Email: " + email));
                }
            } else {
                System.out.println(patientControl.displayPatientSearchResults(patients, "Email: " + email));
            }
        }
        ConsoleUtils.waitMessage();
    }

    private void searchPatientByIcNumber() {
        ConsoleUtils.printHeader("Search Patients by IC Number");
        String icNumber = ConsoleUtils.getICInput(scanner, "Enter IC number (YYMMDD-XX-XXXX): ");

        Patient patient = patientControl.findPatientsByIcNumber(icNumber);
        if (patient == null) {
            System.out.println("No patients found with IC number: " + icNumber);
        } else {
            System.out.println();
            System.out.println(patientControl.displayPatientSearchResult(patient, "IC Number: " + icNumber));
        }
        ConsoleUtils.waitMessage();
    }

    /**
     * Get the sort field for patient search results
     */
    private String getPatientSortField() {
        System.out.println("Select field to sort by:");
        System.out.println("1. Patient ID");
        System.out.println("2. Full Name");
        System.out.println("3. IC Number");
        System.out.println("4. Email");
        System.out.println("5. Phone Number");
        System.out.println("6. Registration Date");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
        
        return switch (choice) {
            case 1 -> "id";
            case 2 -> "name";
            case 3 -> "ic";
            case 4 -> "email";
            case 5 -> "phone";
            case 6 -> "regdate";
            default -> "name";
        };
    }

    /**
     * Get the sort order for search results
     */
    private String getSortOrder() {
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, 0-9, oldest first)");
        System.out.println("2. Descending (Z-A, 9-0, newest first)");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        return choice == 1 ? "asc" : "desc";
    }

    private void generatePatientReports() {
        ConsoleUtils.printHeader("Patient Reports");
        System.out.println("1. Patient Record Summary Report");
        System.out.println("2. Patient Demographics Report");
        System.out.println("3. Patient Visit History Report");
        System.out.println("4. All Reports");
        System.out.print("Enter choice: ");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);

        switch (choice) {
            case 1:
                generatePatientRecordSummaryReport();
                break;
            case 2:
                generatePatientDemographicsReport();
                break;
            case 3:
                generatePatientVisitHistoryReport();
                break;
            case 4:
                generatePatientRecordSummaryReport();
                generatePatientDemographicsReport();
                generatePatientVisitHistoryReport();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generatePatientRecordSummaryReport() {
        ConsoleUtils.printHeader("Patient Record Summary Report");
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
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);

        String sortBy;
        switch (sortFieldChoice) {
            case 1:
                sortBy = "id";
                break;
            case 2:
                sortBy = "name";
                break;
            case 3:
                sortBy = "ic";
                break;
            case 4:
                sortBy = "email";
                break;
            case 5:
                sortBy = "phone";
                break;
            case 6:
                sortBy = "regdate";
                break;
            case 7:
                sortBy = "status";
                break;
            case 8:
                sortBy = "blood";
                break;
            case 9:
                sortBy = "allergies";
                break;
            default:
                sortBy = "name";
                break;
        }

        System.out.println(patientControl.generatePatientRecordSummaryReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generatePatientDemographicsReport() {
        ConsoleUtils.printHeader("Patient Demographics Report");
        System.out.println("Select field to sort by:");
        System.out.println("1. Age");
        System.out.println("2. Gender");
        System.out.println("3. Blood Type");
        System.out.println("4. Allergies");
        System.out.println("5. Registration Date");
        System.out.println("6. Patient Name");
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);

        String sortBy;
        switch (sortFieldChoice) {
            case 1:
                sortBy = "age";
                break;
            case 2:
                sortBy = "gender";
                break;
            case 3:
                sortBy = "blood";
                break;
            case 4:
                sortBy = "allergies";
                break;
            case 5:
                sortBy = "regdate";
                break;
            case 6:
                sortBy = "name";
                break;
            default:
                sortBy = "age";
                break;
        }

        System.out.println(patientControl.generatePatientDemographicsReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generatePatientVisitHistoryReport() {
        ConsoleUtils.printHeader("Patient Visit History Report");
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
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);

        String sortBy;
        switch (sortFieldChoice) {
            case 1:
                sortBy = "id";
                break;
            case 2:
                sortBy = "name";
                break;
            case 3:
                sortBy = "ic";
                break;
            case 4:
                sortBy = "email";
                break;
            case 5:
                sortBy = "phone";
                break;
            case 6:
                sortBy = "regdate";
                break;
            case 7:
                sortBy = "status";
                break;
            case 8:
                sortBy = "blood";
                break;
            case 9:
                sortBy = "allergies";
                break;
            default:
                sortBy = "name";
                break;
        }

        System.out.println(patientControl.generatePatientVisitHistoryReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }
}
package boundary;

import java.util.Scanner;

import control.DoctorManagementControl;
import entity.Address;
import entity.DayOfWeek;
import entity.Doctor;
import utility.ConsoleUtils;
import utility.PatternChecker;

/**
 * Doctor Management User Interface
 * Handles all doctor management user interactions
 */
public class DoctorManagementUI {
    private Scanner scanner;
    private DoctorManagementControl doctorControl;

    public DoctorManagementUI() {
        this.scanner = new Scanner(System.in);
        this.doctorControl = new DoctorManagementControl();
    }

    public void displayDoctorManagementMenu() {
        while (true) {
            System.out.println("\n=== DOCTOR MANAGEMENT MODULE ===");
            System.out.println("1. Register New Doctor");
            System.out.println("2. Update Doctor Information");
            System.out.println("3. Add Doctor Schedule");
            System.out.println("4. Set Doctor Availability");
            System.out.println("5. Search Doctor");
            System.out.println("6. Generate Doctor Reports");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
            
            switch (choice) {
                case 1:
                    registerNewDoctor();
                    break;
                case 2:
                    updateDoctorInfo();
                    break;
                case 3:
                    addDoctorSchedule();
                    break;
                case 4:
                    setDoctorAvailability();
                    break;
                case 5:
                    searchDoctor();
                    break;
                case 6:
                    generateDoctorReports();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void registerNewDoctor() {
        System.out.println("\n=== REGISTER NEW DOCTOR ===");
        String fullName = ConsoleUtils.getStringInput(scanner, "Enter full name: ");
        String icNumber = ConsoleUtils.getICInput(scanner, "Enter IC number (YYMMDD-XX-XXXX): ");
        String email = ConsoleUtils.getStringInput(scanner, "Enter email: ");
        String phoneNumber = ConsoleUtils.getPhoneInput(scanner, "Enter phone number (0XX-XXXXXXX): ");
        String specialty = ConsoleUtils.getStringInput(scanner, "Enter medical specialty: ");
        String licenseNumber = ConsoleUtils.getStringInput(scanner, "Enter license number: ");
        int expYears = ConsoleUtils.getIntInput(scanner, "Enter experience years: ", 0, 50);
        
        // Get address details
        String street = ConsoleUtils.getStringInput(scanner, "Enter street: ");
        String city = ConsoleUtils.getStringInput(scanner, "Enter city: ");
        String state = ConsoleUtils.getStringInput(scanner, "Enter state: ");
        Integer postalCode = ConsoleUtils.getIntInput(scanner, "Enter postal code: ", 0, 99999);
        String country = ConsoleUtils.getStringInput(scanner, "Enter country: ");
        
        Address address = new Address(street, city, state, postalCode.toString(), country);
        
        boolean success = doctorControl.registerDoctor(fullName, icNumber, email, phoneNumber, 
                                                     address, specialty, licenseNumber, expYears);
        
        if (success) {
            System.out.println("Doctor registered successfully!");
        } else {
            System.out.println("Failed to register doctor.");
        }
    }
    
    private void updateDoctorInfo() {
        System.out.println("\n=== UPDATE DOCTOR INFORMATION ===");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID to update: ");
        
        // First, find the doctor to display current information
        Doctor currentDoctor = doctorControl.findDoctorById(doctorId);
        if (currentDoctor == null) {
            System.out.println("Doctor not found with ID: " + doctorId);
            return;
        }
        
        // Display current doctor information
        System.out.println("\nCurrent Doctor Information:");
        System.out.println("1.Name: " + currentDoctor.getFullName());
        System.out.println("2.Email: " + currentDoctor.getEmail());
        System.out.println("3.Phone: " + currentDoctor.getPhoneNumber());
        System.out.println("4.Specialty: " + currentDoctor.getMedicalSpecialty());
        System.out.println("5.Experience: " + currentDoctor.getExpYears() + " years");
        System.out.println("6.Address: " + currentDoctor.getAddress());

        int choice = ConsoleUtils.getIntInput(scanner, "\nEnter the number of information to update: ", 1, 6);

        String fullNameToUpdate = null;
        String emailToUpdate = null;    
        String phoneToUpdate = null;
        String specialtyToUpdate = null;
        int expYearsToUpdate = -1;
        Address newAddress = null;

        switch (choice) {
            case 1:
                System.out.print("Enter new full name: ");
                String newFullName = scanner.nextLine().trim();
                fullNameToUpdate = (newFullName.trim().isEmpty()) ? null : newFullName;
                break;
            case 2:
                System.out.print("Enter new email: ");
                String newEmail = scanner.nextLine().trim();
                emailToUpdate = (newEmail.trim().isEmpty()) ? null : newEmail;
                break;
            case 3:
                System.out.print("Enter new phone number: ");
                String newPhoneNumber = scanner.nextLine().trim();
                // Validate phone number if provided
                if (!newPhoneNumber.isEmpty() && !PatternChecker.PHONE_PATTERN.matcher(newPhoneNumber).matches()) {
                    System.out.println("Invalid phone number format. Must be in format: 0XX-XXXXXXX");
                    System.out.println("Skipping phone number update.");
                    newPhoneNumber = "";
                }
                phoneToUpdate = (newPhoneNumber.trim().isEmpty()) ? null : newPhoneNumber;
                break;
            case 4:
                System.out.print("Enter new medical specialty: ");
                String newSpecialty = scanner.nextLine().trim();
                specialtyToUpdate = (newSpecialty.trim().isEmpty()) ? null : newSpecialty;
                break;
            case 5:
                System.out.print("Enter new experience years: ");
                String expYearsInput = scanner.nextLine();
                int newExpYears = -1; 
                if (!expYearsInput.trim().isEmpty()) {
                    try {
                        newExpYears = Integer.parseInt(expYearsInput.trim());
                        if (newExpYears < 0 || newExpYears > 50) {
                            System.out.println("Invalid experience years. Must be between 0-50. Skipping this field.");
                            newExpYears = -1;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid experience years format. Skipping this field.");
                        newExpYears = -1;
                    }
                }
                expYearsToUpdate = newExpYears; 
                break;
            case 6:
                String newStreet = ConsoleUtils.getStringInput(scanner, "Enter new street: ");
                String newCity = ConsoleUtils.getStringInput(scanner, "Enter new city: ");
                String newState = ConsoleUtils.getStringInput(scanner, "Enter new state: ");
                Integer newPostalCode = ConsoleUtils.getIntInput(scanner, "Enter new postal code: ", 0, 99999);
                String newCountry = ConsoleUtils.getStringInput(scanner, "Enter new country: ");
                newAddress = new Address(newStreet, newCity, newState, newPostalCode.toString(), newCountry);
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
        
        boolean success = doctorControl.updateDoctorInfo(doctorId, fullNameToUpdate, emailToUpdate, 
                                                       phoneToUpdate, specialtyToUpdate, expYearsToUpdate, newAddress);
        
        if (success) {
            System.out.println("Doctor information updated successfully!");
        } else {
            System.out.println("Failed to update doctor information.");
        }
    }

    private void addDoctorSchedule() {
        System.out.println("\n=== ADD DOCTOR SCHEDULE ===");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        String dayInput = ConsoleUtils.getStringInput(scanner, "Enter day of week (e.g., MONDAY): ");
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(dayInput.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid day. Please enter MONDAY to SUNDAY.");
            return;
        }
        String startTime = ConsoleUtils.getTimeInput(scanner, "Enter start time (HH:mm:ss): ");
        String endTime = ConsoleUtils.getTimeInput(scanner, "Enter end time (HH:mm:ss): ");
        boolean added = doctorControl.addSchedule(doctorId, dayOfWeek, startTime, endTime);
        System.out.println(added ? "Schedule added successfully." : "Failed to add schedule.");
    }

    private void setDoctorAvailability() {
        String avail;
        System.out.println("\n=== SET DOCTOR AVAILABILITY ===");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        System.out.print("Set availability (true/false): ");
        boolean isAvailable = getBooleanInput();
        if (isAvailable) {
            avail = "Available";
        }
        else {
            avail = "Not available";
        }
        doctorControl.setDoctorAvailability(doctorId, isAvailable);
        System.out.println("\nDoctor ID: " + doctorId + "\nAvailable: " + avail);
    }

    private void searchDoctor() {
        System.out.println("\n=== SEARCH DOCTOR ===");
        System.out.println("1. Search by Doctor ID");
        System.out.println("2. Search by Full Name");
        System.out.println("3. Search by Email");
        System.out.println("4. Search by License Number");
        System.out.println("5. Search by Specialty");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 5);
        
        switch (choice) {
            case 1:
                String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
                System.out.println("Search by Doctor ID - Implementation needed");
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
                String licenseNumber = ConsoleUtils.getStringInput(scanner, "Enter License Number: ");
                System.out.println("Search by License Number - Implementation needed");
                break;
            case 5:
                String specialty = ConsoleUtils.getStringInput(scanner, "Enter Specialty: ");
                System.out.println("Search by Specialty - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateDoctorReports() {
        System.out.println("\n=== DOCTOR REPORTS ===");
        System.out.println(doctorControl.generateDoctorInformationReport());
        System.out.println(doctorControl.generateScheduleReport());
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
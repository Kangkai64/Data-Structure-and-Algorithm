package boundary;

import control.DoctorManagementControl;
import entity.Address;
import java.util.Scanner;

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
            
            int choice = getIntInput();
            
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
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine();
        System.out.print("Enter IC number: ");
        String icNumber = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();
        System.out.print("Enter medical specialty: ");
        String specialty = scanner.nextLine();
        System.out.print("Enter license number: ");
        String licenseNumber = scanner.nextLine();
        System.out.print("Enter experience years: ");
        int expYears = getIntInput();
        
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
        System.out.print("Enter doctor ID to update: ");
        String doctorId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Update Doctor Info - Implementation needed");
        System.out.println("Doctor ID: " + doctorId);
    }

    private void addDoctorSchedule() {
        System.out.println("\n=== ADD DOCTOR SCHEDULE ===");
        System.out.print("Enter doctor ID: ");
        String doctorId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Add Doctor Schedule - Implementation needed");
        System.out.println("Doctor ID: " + doctorId);
    }

    private void setDoctorAvailability() {
        System.out.println("\n=== SET DOCTOR AVAILABILITY ===");
        System.out.print("Enter doctor ID: ");
        String doctorId = scanner.nextLine();
        System.out.print("Set availability (true/false): ");
        boolean isAvailable = getBooleanInput();
        
        // For now, just show a placeholder
        System.out.println("Set Doctor Availability - Implementation needed");
        System.out.println("Doctor ID: " + doctorId + ", Available: " + isAvailable);
    }

    private void searchDoctor() {
        System.out.println("\n=== SEARCH DOCTOR ===");
        System.out.println("1. Search by Doctor ID");
        System.out.println("2. Search by Full Name");
        System.out.println("3. Search by Email");
        System.out.println("4. Search by License Number");
        System.out.println("5. Search by Specialty");
        System.out.print("Enter choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("Enter Doctor ID: ");
                String doctorId = scanner.nextLine();
                System.out.println("Search by Doctor ID - Implementation needed");
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
                System.out.print("Enter License Number: ");
                String licenseNumber = scanner.nextLine();
                System.out.println("Search by License Number - Implementation needed");
                break;
            case 5:
                System.out.print("Enter Specialty: ");
                String specialty = scanner.nextLine();
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

    private int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
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
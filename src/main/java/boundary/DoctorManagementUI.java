package boundary;

import java.util.Scanner;
import java.util.Iterator;
import adt.ArrayBucketList;
import control.DoctorManagementControl;
import entity.Address;
import entity.DayOfWeek;
import entity.Doctor;
import utility.ConsoleUtils;

/**
 * Author: Lee Yong Kang
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
            ConsoleUtils.clearScreen();
            doctorControl.loadDoctorData();
            System.out.println("\n=== DOCTOR MANAGEMENT MODULE ===");
            System.out.println("1. Register New Doctor");
            System.out.println("2. Update Doctor Information");
            System.out.println("3. Set Doctor as Unavailable");
            System.out.println("4. Manage Doctor Schedule");
            System.out.println("5. Set Availability");
            System.out.println("6. Search Doctor");
            System.out.println("7. View Doctor Statistics");
            System.out.println("8. Generate Reports");
            System.out.println("9. Back to Main Menu");

            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 9);

            switch (choice) {
                case 1:
                    registerNewDoctor();
                    break;
                case 2:
                    updateDoctorInfo();
                    break;
                case 3:
                    deactivateDoctor();
                    break;
                case 4:
                    manageDoctorSchedule();
                    break;
                case 5:
                    setAvailabilityMenu();
                    break;
                case 6:
                    searchDoctor();
                    break;
                case 7:
                    viewDoctorStatistics();
                    break;
                case 8:
                    generateReportsMenu();
                    break;
                case 9:
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
        String email = ConsoleUtils.getEmailInput(scanner, "Enter email: ");
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
        ConsoleUtils.waitMessage();
    }

    private void updateDoctorInfo() {
        System.out.println("\n=== UPDATE DOCTOR INFORMATION ===");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID to update: ");

        // First, find the doctor to display current information
        Doctor currentDoctor = doctorControl.findDoctorById(doctorId);
        if (currentDoctor == null) {
            System.out.println("Doctor not found with ID: " + doctorId);
            ConsoleUtils.waitMessage();
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
                fullNameToUpdate = ConsoleUtils.getStringInput(scanner, "Enter new full name: ", currentDoctor.getFullName());
                break;
            case 2:
                emailToUpdate = ConsoleUtils.getEmailInput(scanner, "Enter new email: ", currentDoctor.getEmail());
                break;
            case 3:
                phoneToUpdate = ConsoleUtils.getPhoneInput(scanner, "Enter new phone number: ", currentDoctor.getPhoneNumber());
                break;
            case 4:
                specialtyToUpdate = ConsoleUtils.getStringInput(scanner, "Enter new medical specialty: ", currentDoctor.getMedicalSpecialty());
                break;
            case 5:
                expYearsToUpdate = ConsoleUtils.getIntInput(scanner, "Enter new experience years: ", currentDoctor.getExpYears());
                break;
            case 6:
                String curStreet = currentDoctor.getAddress() == null ? "" : currentDoctor.getAddress().getStreet();
                String curCity = currentDoctor.getAddress() == null ? "" : currentDoctor.getAddress().getCity();
                String curState = currentDoctor.getAddress() == null ? "" : currentDoctor.getAddress().getState();
                String curPostal = currentDoctor.getAddress() == null ? "" : currentDoctor.getAddress().getZipCode();
                String curCountry = currentDoctor.getAddress() == null ? "" : currentDoctor.getAddress().getCountry();
                String newStreet = ConsoleUtils.getStringInput(scanner, "Enter new street: ", curStreet);
                String newCity = ConsoleUtils.getStringInput(scanner, "Enter new city: ", curCity);
                String newState = ConsoleUtils.getStringInput(scanner, "Enter new state: ", curState);
                String newPostal = ConsoleUtils.getPostalCodeInput(scanner, "Enter new postal code: ", curPostal == null ? "" : curPostal);
                String newCountry = ConsoleUtils.getStringInput(scanner, "Enter new country: ", curCountry);
                newAddress = new Address(newStreet, newCity, newState, newPostal, newCountry);
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
        ConsoleUtils.waitMessage();
    }

    private void deactivateDoctor() {
        System.out.println("\n=== SET DOCTOR AS UNAVAILABLE ===");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID to set as unavailable: ");

        // First, find the doctor to display current information
        Doctor currentDoctor = doctorControl.findDoctorById(doctorId);
        if (currentDoctor == null) {
            System.out.println("Doctor not found with ID: " + doctorId);
            ConsoleUtils.waitMessage();
            return;
        }

        // Display current doctor information
        System.out.println("\nDoctor to be set as unavailable:");
        displayDoctorDetails(currentDoctor);

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to set this doctor as unavailable? (Y/N): ");
        if (confirm) {
            boolean success = doctorControl.deactivateDoctor(doctorId);
            if (success) {
                System.out.println("Doctor set as unavailable successfully!");
            } else {
                System.out.println("Failed to set doctor as unavailable.");
            }
        } else {
            System.out.println("Operation cancelled.");
        }
        ConsoleUtils.waitMessage();
    }

    private void viewDoctorStatistics() {
        System.out.println("\n=== DOCTOR STATISTICS ===");
        
        // Load data first
        doctorControl.loadDoctorData();
        
        System.out.println("DOCTOR STATISTICS OVERVIEW");
        System.out.println("=" .repeat(50));
        System.out.println("Total Doctors: " + doctorControl.getTotalDoctors());
        System.out.println("Available Doctors: " + doctorControl.getAvailableDoctors().getSize());
        System.out.println("Unavailable Doctors: " + (doctorControl.getTotalDoctors() - doctorControl.getAvailableDoctors().getSize()));
        
        System.out.println("\nDOCTOR LISTS");
        System.out.println("=" .repeat(50));
        
        System.out.println("1. View All Doctors");
        System.out.println("2. View Available Doctors");
        System.out.println("3. View Unavailable Doctors");
        System.out.println("4. Back to Main Menu");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        
        switch (choice) {
            case 1:
                displayDoctorList("All Doctors", doctorControl.getAllDoctors());
                break;
            case 2:
                displayDoctorList("Available Doctors", doctorControl.getAvailableDoctors());
                break;
            case 3:
                displayDoctorList("Unavailable Doctors", doctorControl.getInactiveDoctors());
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void displayDoctorList(String title, ArrayBucketList<String, Doctor> doctors) {
        System.out.println("\n=== " + title.toUpperCase() + " ===");
        
        if (doctors.isEmpty()) {
            System.out.println("No doctors found.");
            ConsoleUtils.waitMessage();
            return;
        }
        
        System.out.println("Found " + doctors.getSize() + " doctor(s)");
        System.out.println();
        
        System.out.println("Sort results?\n1. Yes\n2. No");
        int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        if (sortChoice == 1) {
            String sortBy = getDoctorSortField();
            System.out.println();
            String sortOrder = ConsoleUtils.getSortOrder(scanner);
            System.out.println(doctorControl.displaySortedDoctorSearchResults(doctors, title, sortBy, sortOrder));
        } else {
            displayDoctorsList(doctors);
        }
        
        ConsoleUtils.waitMessage();
    }

    private void manageDoctorSchedule() {
        System.out.println("\n=== MANAGE DOCTOR SCHEDULE ===");
        
        // First, get doctor ID and verify doctor exists
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        Doctor doctor = doctorControl.findDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found with ID: " + doctorId);
            ConsoleUtils.waitMessage();
            return;
        }
        
        System.out.println("Managing schedules for Doctor: " + doctor.getFullName());
        
        // Get all current schedules for the doctor (already sorted by day of week)
        entity.Schedule[] schedulesArray = doctorControl.getDoctorSchedulesOrderedArray(doctorId);
        
        if (schedulesArray.length == 0) {
            System.out.println("\nNo schedules found for this doctor.");
            System.out.println("1. Add New Schedule");
            System.out.println("2. Back to Main Menu");
            
            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
            if (choice == 1) {
                addDoctorSchedule(doctorId, doctor.getFullName());
            }
            return;
        }
        
        // Display all schedules
        System.out.println("\nCurrent Schedules:");
        System.out.println("-".repeat(80));
        
        for (int i = 0; i < schedulesArray.length; i++) {
            entity.Schedule s = schedulesArray[i];
            System.out.printf("%d. [%s] %s %s-%s | %s%n", 
                (i + 1), 
                s.getScheduleId(), 
                s.getDayOfWeek(), 
                s.getFromTime(), 
                s.getToTime(), 
                (s.isAvailable() ? "Available" : "Unavailable"));
        }
        System.out.println("-".repeat(80));
        
        // Show management options
        System.out.println("\nManagement Options:");
        System.out.println("1. Add New Schedule");
        System.out.println("2. Update Existing Schedule");
        System.out.println("3. Back to Main Menu");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);

        switch (choice) {
            case 1:
                addDoctorSchedule(doctorId, doctor.getFullName());
                break;
            case 2:
                updateDoctorSchedule(doctorId, doctor.getFullName(), schedulesArray);
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void addDoctorSchedule(String doctorId, String doctorName) {
        System.out.println("\n=== ADD DOCTOR SCHEDULE ===");
        System.out.println("Adding schedule for Doctor: " + doctorName);
        
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
        ConsoleUtils.waitMessage();
    }

    private void updateDoctorSchedule(String doctorId, String doctorName, entity.Schedule[] schedules) {
        System.out.println("\n=== UPDATE DOCTOR SCHEDULE ===");
        System.out.println("Updating schedule for Doctor: " + doctorName);
        
        // Display current schedules
        System.out.println("\nCurrent Schedules:");
        System.out.println("-".repeat(80));
        for (int index = 0; index < schedules.length; index++) {
            entity.Schedule s = schedules[index];
            System.out.printf("%d. [%s] %s %s-%s | %s%n", 
                (index + 1), 
                s.getScheduleId(), 
                s.getDayOfWeek(), 
                s.getFromTime(), 
                s.getToTime(), 
                (s.isAvailable() ? "Available" : "Unavailable"));
        }
        System.out.println("-".repeat(80));
        
        // Let user choose by number instead of entering schedule ID
        int scheduleChoice = ConsoleUtils.getIntInput(scanner, "Enter the number of schedule to update: ", 1, schedules.length);
        entity.Schedule selectedSchedule = schedules[scheduleChoice - 1];
        
        System.out.println("Updating Schedule: [" + selectedSchedule.getScheduleId() + "] " + 
                          selectedSchedule.getDayOfWeek() + " " + selectedSchedule.getFromTime() + "-" + selectedSchedule.getToTime());
        
        String dayInput = ConsoleUtils.getStringInput(scanner, "Enter new day of week (e.g., MONDAY): ");
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(dayInput.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            System.out.println("Invalid day. Please enter MONDAY to SUNDAY.");
            return;
        }
        String startTime = ConsoleUtils.getTimeInput(scanner, "Enter new start time (HH:mm:ss): ");
        String endTime = ConsoleUtils.getTimeInput(scanner, "Enter new end time (HH:mm:ss): ");
        
        boolean updated = doctorControl.updateSchedule(selectedSchedule.getScheduleId(), dayOfWeek, startTime, endTime);
        System.out.println(updated ? "Schedule updated successfully." : "Failed to update schedule.");
        ConsoleUtils.waitMessage();
    }

    

    private void setAvailabilityMenu() {
        System.out.println("\n=== DOCTOR AVAILABILITY MANAGEMENT ===");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        Doctor doctor = doctorControl.findDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        // Show current doctor information
        System.out.println("Doctor: " + doctor.getFullName());
        System.out.println("Current Availability: " + (doctor.isAvailable() ? "Available" : "Unavailable"));

        // Load and display schedules for the doctor
        entity.Schedule[] schedulesArray = doctorControl.getDoctorSchedulesOrderedArray(doctorId);
        
        if (schedulesArray.length == 0) {
            System.out.println("No schedules found for this doctor.");
        } else {
            System.out.println("\nDoctor Schedules:");
            System.out.println("-".repeat(80));
            
            for (int i = 0; i < schedulesArray.length; i++) {
                entity.Schedule s = schedulesArray[i];
                System.out.printf("%d. [%s] %s %s-%s | %s%n", 
                    (i + 1), 
                    s.getScheduleId(), 
                    s.getDayOfWeek(), 
                    s.getFromTime(), 
                    s.getToTime(), 
                    (s.isAvailable() ? "Available" : "Unavailable"));
            }
            System.out.println("-".repeat(80));
        }

        System.out.println("\nManagement Options:");
        System.out.println("1. Set Doctor Availability (Available/Unavailable)");
        System.out.println("2. Set Schedule Availability");
        System.out.println("3. Back to Main Menu");
        int choice = ConsoleUtils.getIntInput(scanner, "Choose option (1-3): ", 1, 3);

        switch (choice) {
            case 1:
                handleDoctorAvailability(doctorId, doctor.getFullName());
                break;
            case 2:
                handleScheduleAvailability(doctorId, schedulesArray);
                break;
            case 3:
                return;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void handleDoctorAvailability(String doctorId, String doctorName) {
        System.out.println("\n=== SET DOCTOR AVAILABILITY ===");
        System.out.println("Doctor: " + doctorName);
        System.out.println("Note: This will set the doctor's availability status.");
        
        boolean isAvailable = ConsoleUtils.getBooleanInput(scanner, "Set doctor as available? (Y/N): ");
        
        boolean ok = doctorControl.setDoctorAvailability(doctorId, isAvailable);
        if (ok) {
            System.out.println("Doctor availability updated successfully.");
            System.out.println("Status: " + (isAvailable ? "Available" : "Unavailable"));
        } else {
            System.out.println("Failed to update doctor availability.");
        }
        ConsoleUtils.waitMessage();
    }

    private void handleScheduleAvailability(String doctorId, entity.Schedule[] schedules) {
        System.out.println("\n=== SET SCHEDULE AVAILABILITY ===");
        
        if (schedules.length == 0) {
            System.out.println("No schedules found for this doctor.");
            return;
        }

        // Display schedules with numbers
        System.out.println("Available Schedules:");
        System.out.println("-".repeat(80));
        for (int index = 0; index < schedules.length; index++) {
            entity.Schedule s = schedules[index];
            System.out.printf("%d. [%s] %s %s-%s | %s%n", 
                (index + 1), 
                s.getScheduleId(), 
                s.getDayOfWeek(), 
                s.getFromTime(), 
                s.getToTime(), 
                (s.isAvailable() ? "Available" : "Unavailable"));
        }
        System.out.println("-".repeat(80));

        int scheduleChoice = ConsoleUtils.getIntInput(scanner, "Enter the number of schedule to update: ", 1, schedules.length);
        entity.Schedule selectedSchedule = schedules[scheduleChoice - 1];
        
        System.out.println("Updating Schedule: [" + selectedSchedule.getScheduleId() + "] " + 
                          selectedSchedule.getDayOfWeek() + " " + selectedSchedule.getFromTime() + "-" + selectedSchedule.getToTime());
        
        boolean isAvailable = ConsoleUtils.getBooleanInput(scanner, "Set schedule availability (Y/N): ");
        boolean ok = doctorControl.setScheduleAvailability(selectedSchedule.getScheduleId(), isAvailable);
        System.out.println(ok ? "Schedule availability updated successfully." : "Failed to update schedule availability.");
        ConsoleUtils.waitMessage();
    }

    private void searchDoctor() {
        System.out.println("\n=== SEARCH DOCTOR ===");
        System.out.println("1. Search by Doctor ID");
        System.out.println("2. Search by IC Number");
        System.out.println("3. Search by Full Name");
        System.out.println("4. Search by Email");
        System.out.println("5. Search by Phone Number");
        System.out.println("6. Search by License Number");
        System.out.println("7. Search by Specialty");
        System.out.println("8. Search by Experience");
        System.out.println("9. Search by Availability");
        System.out.println("10. Advanced Multi-Criteria Search");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 10);

        switch (choice) {
            case 1:
                searchByDoctorId();
                break;
            case 2:
                searchByIC();
                break;
            case 3:
                searchByFullName();
                break;
            case 4:
                searchByEmail();
                break;
            case 5:
                searchByPhoneNumber();
                break;
            case 6:
                searchByLicenseNumber();
                break;
            case 7:
                searchBySpecialty();
                break;
            case 8:
                searchByExperience();
                break;
            case 9:
                searchByAvailability();
                break;
            case 10:
                searchByMultipleCriteria();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void searchByDoctorId() {
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
        Doctor doctor = doctorControl.findDoctorById(doctorId);

        if (doctor != null) {
            System.out.println("\n=== DOCTOR FOUND ===");
            displayDoctorDetails(doctor);
        } else {
            System.out.println("Doctor not found with ID: " + doctorId);
        }
        ConsoleUtils.waitMessage();
    }

    private void searchByIC() {
        String icNumber = ConsoleUtils.getICInput(scanner, "Enter doctor IC number: ");

        Doctor doctor = doctorControl.findDoctorByIcNumber(icNumber);

        if (doctor != null) {
            System.out.println();
            displayDoctorDetails(doctor);
        } else {
            System.out.println("No doctors found with IC number: " + icNumber);
        }
        System.out.println();
        ConsoleUtils.waitMessage();
    }
    
    private void searchByFullName() {
        String fullName = ConsoleUtils.getStringInput(scanner, "Enter Full Name (or partial name): ");
        ArrayBucketList<String, Doctor> doctors = doctorControl.findDoctorsByName(fullName);

        if (!doctors.isEmpty()) {
            System.out.println("\n=== DOCTORS FOUND ===");
            System.out.println();
            
            if (doctors.getSize() > 1) {
                System.out.println("Found " + doctors.getSize() + " doctor(s) with name containing: " + fullName);
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getDoctorSortField();
                    System.out.println();
                    String sortOrder = ConsoleUtils.getSortOrder(scanner);
                    System.out.println(doctorControl.displaySortedDoctorSearchResults(doctors, "Name: " + fullName, sortBy, sortOrder));
                } else {
                    displayDoctorsList(doctors);
                }
            } else {
                displayDoctorsList(doctors);
            }
        } else {
            System.out.println("No doctors found with name containing: " + fullName);
        }
        ConsoleUtils.waitMessage();
    }

    private void searchByEmail() {
        String email = ConsoleUtils.getStringInput(scanner, "Enter Email: ");
        ArrayBucketList<String, Doctor> foundDoctors = doctorControl.findDoctorsByEmail(email);

        if (!foundDoctors.isEmpty()) {
            System.out.println("\n=== DOCTORS FOUND ===");
            System.out.println();
            
            if (foundDoctors.getSize() > 1) {
                System.out.println("Found " + foundDoctors.getSize() + " doctor(s) with email containing: " + email);
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getDoctorSortField();
                    System.out.println();
                    String sortOrder = ConsoleUtils.getSortOrder(scanner);
                    System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "Email: " + email, sortBy, sortOrder));
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                displayDoctorsList(foundDoctors);
            }
        } else {
            System.out.println("No doctors found with email containing: " + email);
        }
        ConsoleUtils.waitMessage();
    }

    private void searchByLicenseNumber() {
        String licenseNumber = ConsoleUtils.getStringInput(scanner, "Enter License Number: ");
        ArrayBucketList<String, Doctor> foundDoctors = doctorControl.findDoctorsByLicense(licenseNumber);

        if (!foundDoctors.isEmpty()) {
            System.out.println("\n=== DOCTORS FOUND ===");
            System.out.println();
            
            if (foundDoctors.getSize() > 1) {
                System.out.println("Found " + foundDoctors.getSize() + " doctor(s) with license number containing: " + licenseNumber);
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getDoctorSortField();
                    System.out.println();
                    String sortOrder = ConsoleUtils.getSortOrder(scanner);
                    System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "License: " + licenseNumber, sortBy, sortOrder));
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                displayDoctorsList(foundDoctors);
            }
        } else {
            System.out.println("No doctors found with license number containing: " + licenseNumber);
        }
        ConsoleUtils.waitMessage();
    }

    private void searchBySpecialty() {
        String specialty = ConsoleUtils.getStringInput(scanner, "Enter Specialty: ");
        ArrayBucketList<String, Doctor> foundDoctors = doctorControl.getDoctorsBySpecialty(specialty);

        if (!foundDoctors.isEmpty()) {
            System.out.println("\n=== DOCTORS FOUND ===");
            System.out.println();
            
            if (foundDoctors.getSize() > 1) {
                System.out.println("Found " + foundDoctors.getSize() + " doctor(s) with specialty containing: " + specialty);
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getDoctorSortField();
                    System.out.println();
                    String sortOrder = ConsoleUtils.getSortOrder(scanner);
                    System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "Specialty: " + specialty, sortBy, sortOrder));
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                displayDoctorsList(foundDoctors);
            }
        } else {
            System.out.println("No doctors found with specialty containing: " + specialty);
        }
        ConsoleUtils.waitMessage();
    }

    private void searchByPhoneNumber() {
        String phoneNumber = ConsoleUtils.getStringInput(scanner, "Enter Phone Number: ");
        ArrayBucketList<String, Doctor> foundDoctors = doctorControl.findDoctorsByPhone(phoneNumber);

        if (!foundDoctors.isEmpty()) {
            System.out.println("\n=== DOCTORS FOUND ===");
            System.out.println();
            
            if (foundDoctors.getSize() > 1) {
                System.out.println("Found " + foundDoctors.getSize() + " doctor(s) with phone number containing: " + phoneNumber);
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getDoctorSortField();
                    System.out.println();
                    String sortOrder = ConsoleUtils.getSortOrder(scanner);
                    System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "Phone: " + phoneNumber, sortBy, sortOrder));
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                displayDoctorsList(foundDoctors);
            }
        } else {
            System.out.println("No doctors found with phone number containing: " + phoneNumber);
        }
        ConsoleUtils.waitMessage();
    }

    private void searchByExperience() {
        System.out.println("\n=== SEARCH BY EXPERIENCE ===");
        System.out.println("1. Search by Minimum Experience");
        System.out.println("2. Search by Experience Range");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        if (choice == 1) {
            int minYears = ConsoleUtils.getIntInput(scanner, "Enter minimum experience years: ", 0, 50);
            ArrayBucketList<String, Doctor> foundDoctors = doctorControl.findDoctorsByExperience(minYears);
            
            if (!foundDoctors.isEmpty()) {
                System.out.println("\n=== DOCTORS FOUND ===");
                System.out.println();
                
                if (foundDoctors.getSize() > 1) {
                    System.out.println("Found " + foundDoctors.getSize() + " doctor(s) with " + minYears + "+ years experience");
                    System.out.println();
                    System.out.println("Sort results?\n1. Yes\n2. No");
                    int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                    System.out.println();
                    
                    if (sortChoice == 1) {
                        String sortBy = getDoctorSortField();
                        System.out.println();
                        String sortOrder = ConsoleUtils.getSortOrder(scanner);
                        System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "Experience: " + minYears + "+ years", sortBy, sortOrder));
                    } else {
                        displayDoctorsList(foundDoctors);
                    }
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                System.out.println("No doctors found with " + minYears + "+ years experience");
            }
        } else {
            int minYears = ConsoleUtils.getIntInput(scanner, "Enter minimum experience years: ", 0, 50);
            int maxYears = ConsoleUtils.getIntInput(scanner, "Enter maximum experience years: ", minYears, 50);
            ArrayBucketList<String, Doctor> foundDoctors = doctorControl.findDoctorsByExperienceRange(minYears, maxYears);
            
            if (!foundDoctors.isEmpty()) {
                System.out.println("\n=== DOCTORS FOUND ===");
                System.out.println();
                
                if (foundDoctors.getSize() > 1) {
                    System.out.println("Found " + foundDoctors.getSize() + " doctor(s) with " + minYears + "-" + maxYears + " years experience");
                    System.out.println();
                    System.out.println("Sort results?\n1. Yes\n2. No");
                    int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                    System.out.println();
                    
                    if (sortChoice == 1) {
                        String sortBy = getDoctorSortField();
                        System.out.println();
                        String sortOrder = ConsoleUtils.getSortOrder(scanner);
                        System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "Experience: " + minYears + "-" + maxYears + " years", sortBy, sortOrder));
                    } else {
                        displayDoctorsList(foundDoctors);
                    }
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                System.out.println("No doctors found with " + minYears + "-" + maxYears + " years experience");
            }
        }
        ConsoleUtils.waitMessage();
    }

    private void searchByAvailability() {
        System.out.println("\n=== SEARCH BY AVAILABILITY ===");
        System.out.println("1. Search Available Doctors");
        System.out.println("2. Search Unavailable Doctors");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        boolean isAvailable = (choice == 1);
        
        ArrayBucketList<String, Doctor> foundDoctors = doctorControl.findDoctorsByAvailability(isAvailable);
        
        if (!foundDoctors.isEmpty()) {
            System.out.println("\n=== DOCTORS FOUND ===");
            System.out.println();
            
            if (foundDoctors.getSize() > 1) {
                System.out.println("Found " + foundDoctors.getSize() + " " + (isAvailable ? "available" : "unavailable") + " doctor(s)");
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getDoctorSortField();
                    System.out.println();
                    String sortOrder = ConsoleUtils.getSortOrder(scanner);
                    System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "Availability: " + (isAvailable ? "Available" : "Unavailable"), sortBy, sortOrder));
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                displayDoctorsList(foundDoctors);
            }
        } else {
            System.out.println("No " + (isAvailable ? "available" : "unavailable") + " doctors found");
        }
        ConsoleUtils.waitMessage();
    }

    private void searchByMultipleCriteria() {
        System.out.println("\n=== ADVANCED MULTI-CRITERIA SEARCH ===");
        System.out.println("Enter search criteria (leave blank to skip):");
        
        String name = ConsoleUtils.getStringInput(scanner, "Enter name (or partial name): ", "");
        String specialty = ConsoleUtils.getStringInput(scanner, "Enter specialty: ", "");
        
        System.out.println("Availability:");
        System.out.println("1. Available doctors only");
        System.out.println("2. Unavailable doctors only");
        System.out.println("3. Any availability");
        int availChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        boolean isAvailable = (availChoice == 1);
        boolean checkAvailability = (availChoice != 3);
        
        int minExperience = ConsoleUtils.getIntInput(scanner, "Enter minimum experience years (0 to skip): ", 0, 50);
        
        ArrayBucketList<String, Doctor> foundDoctors = doctorControl.findDoctorsByMultipleCriteria(
            name.isEmpty() ? null : name,
            specialty.isEmpty() ? null : specialty,
            checkAvailability ? isAvailable : true,
            minExperience
        );
        
        if (!foundDoctors.isEmpty()) {
            System.out.println("\n=== DOCTORS FOUND ===");
            System.out.println();
            
            if (foundDoctors.getSize() > 1) {
                System.out.println("Found " + foundDoctors.getSize() + " doctor(s) matching criteria");
                System.out.println();
                System.out.println("Sort results?\n1. Yes\n2. No");
                int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                
                if (sortChoice == 1) {
                    String sortBy = getDoctorSortField();
                    System.out.println();
                    String sortOrder = ConsoleUtils.getSortOrder(scanner);
                    System.out.println(doctorControl.displaySortedDoctorSearchResults(foundDoctors, "Multi-Criteria Search", sortBy, sortOrder));
                } else {
                    displayDoctorsList(foundDoctors);
                }
            } else {
                displayDoctorsList(foundDoctors);
            }
        } else {
            System.out.println("No doctors found matching the specified criteria");
        }
        ConsoleUtils.waitMessage();
    }

    /**
     * Display doctors list in a formatted way
     */
    private void displayDoctorsList(ArrayBucketList<String, Doctor> doctors) {
        Iterator<Doctor> foundIterator = doctors.iterator();
        int count = 1;
        while (foundIterator.hasNext()) {
            Doctor doctor = foundIterator.next();
            System.out.println("--- Doctor " + count + " ---");
            displayDoctorDetails(doctor);
            System.out.println();
            count++;
        }
    }

    /**
     * Get the sort field for doctor search results
     */
    private String getDoctorSortField() {
        System.out.println("Select field to sort by:");
        System.out.println("1. Doctor ID");
        System.out.println("2. Full Name");
        System.out.println("3. License Number");
        System.out.println("4. Specialty");
        System.out.println("5. Experience");
        System.out.println("6. Email");
        System.out.println("7. Phone Number");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
        
        return switch (choice) {
            case 1 -> "id";
            case 2 -> "name";
            case 3 -> "license";
            case 4 -> "specialty";
            case 5 -> "experience";
            case 6 -> "email";
            case 7 -> "phone";
            default -> "name";
        };
    }

    private void displayDoctorDetails(Doctor doctor) {
        System.out.println("Doctor ID: " + doctor.getDoctorId());
        System.out.println("Full Name: " + doctor.getFullName());
        System.out.println("IC Number: " + doctor.getICNumber());
        System.out.println("Email: " + doctor.getEmail());
        System.out.println("Phone Number: " + doctor.getPhoneNumber());
        System.out.println("Medical Specialty: " + doctor.getMedicalSpecialty());
        System.out.println("License Number: " + doctor.getLicenseNumber());
        System.out.println("Experience Years: " + doctor.getExpYears());
        System.out.println("Availability: " + (doctor.isAvailable() ? "Available" : "Not Available"));
        System.out.println("Registration Date: " + doctor.getRegistrationDate());

        if (doctor.getAddress() != null) {
            System.out.println("Address: " + doctor.getAddress());
        }

        System.out.println("Number of Consultations: " + getConsultationCountForDoctor(doctor.getDoctorId()));
    }

    private int getConsultationCountForDoctor(String doctorId) {
        try {
            // Get all consultations and count those for the specific doctor
            dao.ConsultationDao consultationDao = new dao.ConsultationDao();
            ArrayBucketList<String, entity.Consultation> allConsultations = consultationDao.findAll();

            int count = 0;
            Iterator<entity.Consultation> iterator = allConsultations.iterator();
            while (iterator.hasNext()) {
                entity.Consultation consultation = iterator.next();
                if (consultation != null && consultation.getDoctor() != null &&
                        doctorId.equals(consultation.getDoctor().getDoctorId())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("Error counting consultations for doctor " + doctorId + ": " + e.getMessage());
            return 0;
        }
    }

    private void generateDoctorInformationReportUI() {
        ConsoleUtils.printHeader("Doctor Information Report");
        System.out.println("1. Sort by Name");
        System.out.println("2. Sort by Specialty");
        System.out.println("3. Sort by Experience");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        System.out.println();

        String sortBy = switch (choice) {
            case 1 -> "name";
            case 2 -> "specialty";
            case 3 -> "experience";
            default -> "name";
        };

        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();
        System.out.println(doctorControl.generateDoctorInformationReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generateScheduleReportUI() {
        ConsoleUtils.printHeader("Doctor Workload Report");
        System.out.println("1. Sort by Name");
        System.out.println("2. Sort by Specialty");
        System.out.println("3. Sort by Weekly Hours");
        System.out.println("4. Sort by Annual Hours");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        String sortBy = switch (choice) {
            case 1 -> "name";
            case 2 -> "specialty";
            case 3 -> "weekly";
            case 4 -> "annual";
            default -> "name";
        };

        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();
        boolean ascending = sortOrder.equals("asc");
        System.out.println(doctorControl.generateDoctorWorkloadReport(sortBy, ascending));
        ConsoleUtils.waitMessage();
    }

    private void generateReportsMenu() {
        ConsoleUtils.printHeader("Doctor Reports");
        System.out.println("1. Doctor Information Report");
        System.out.println("2. Doctor Workload Report");
        System.out.println("3. Doctor Performance Report");
        System.out.println("4. All Reports");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                generateDoctorInformationReportUI();
                break;
            case 2:
                generateScheduleReportUI();
                break;
            case 3:
                generateDoctorPerformanceReportUI();
                break;
            case 4:
                generateDoctorInformationReportUI();
                generateScheduleReportUI();
                generateDoctorPerformanceReportUI();
                System.out.println("\nAll doctor reports have been generated successfully!");
                ConsoleUtils.waitMessage();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateDoctorPerformanceReportUI() {
        ConsoleUtils.printHeader("Doctor Performance Report");
        System.out.println("1. Sort by Consultations");
        System.out.println("2. Sort by Success Rate");
        System.out.println("3. Sort by Patient Satisfaction");
        System.out.println("4. Sort by Revenue");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        String sortBy = switch (choice) {
            case 1 -> "consultations";
            case 2 -> "success";
            case 3 -> "satisfaction";
            case 4 -> "revenue";
            default -> "consultations";
        };

        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();
        boolean ascending = sortOrder.equals("asc");

        System.out.println(doctorControl.generateDoctorPerformanceReport(sortBy, ascending));
        ConsoleUtils.waitMessage();
    }

    
}
package boundary;

import java.util.Scanner;
import java.util.Iterator;

import adt.ArrayBucketList;
import control.DoctorManagementControl;
import entity.Address;
import entity.DayOfWeek;
import entity.Doctor;
import utility.ConsoleUtils;
import utility.PatternChecker;

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
            System.out.println("\n=== DOCTOR MANAGEMENT MODULE ===");
            System.out.println("1. Register New Doctor");
            System.out.println("2. Update Doctor Information");
            System.out.println("3. Add Doctor Schedule");
            System.out.println("4. Set Availability");
            System.out.println("5. Search Doctor");
            System.out.println("6. Generate Reports");
            System.out.println("7. Back to Main Menu");

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
                    setAvailabilityMenu();
                    break;
                case 5:
                    searchDoctor();
                    break;
                case 6:
                    generateReportsMenu();
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

    private void setAvailabilityMenu() {
        System.out.println("\n=== SET AVAILABILITY ===");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        Doctor doctor = doctorControl.findDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found.");
            return;
        }

        // Show current doctor availability
        System.out.println("Current Doctor Availability: " + (doctor.isAvailable() ? "Available" : "Not Available"));

        // Load and display schedules for the doctor
        entity.Schedule[] schedules = doctorControl.getDoctorSchedulesOrderedArray(doctorId);
        if (schedules.length == 0) {
            System.out.println("No schedules found for this doctor.");
        } else {
            System.out.println("\nDoctor Schedules:");
            for (int index = 0; index < schedules.length; index++) {
                entity.Schedule s = schedules[index];
                System.out.println(
                        (index + 1) + ". [" + s.getScheduleId() + "] " + s.getDayOfWeek() + " " + s.getFromTime() + "-"
                                + s.getToTime() + " | " + (s.isAvailable() ? "Available" : "Not Available"));
            }
        }

        System.out.println("\n1. Set Doctor Availability");
        System.out.println("2. Set Schedule Availability");
        int choice = ConsoleUtils.getIntInput(scanner, "Choose option (1-2): ", 1, 2);

        if (choice == 1) {
            System.out.print("Set doctor availability (true/false): ");
            boolean isAvailable = getBooleanInput();
            boolean ok = doctorControl.setDoctorAvailability(doctorId, isAvailable);
            if (ok) {
                System.out.println("Doctor availability updated. " + (isAvailable ? "Available" : "Not Available"));
                if (!isAvailable) {
                    System.out.println("All schedules have been set to Not Available.");
                }
            } else {
                System.out.println("Failed to update doctor availability.");
            }
        } else {
            if (schedules.length == 0) {
                System.out.println("No schedules to update.");
                return;
            }
            String scheduleId = ConsoleUtils.getStringInput(scanner, "Enter schedule ID to update: ");
            System.out.print("Set schedule availability (true/false): ");
            boolean isAvailable = getBooleanInput();
            boolean ok = doctorControl.setScheduleAvailability(scheduleId, isAvailable);
            System.out.println(ok ? "Schedule availability updated." : "Failed to update schedule availability.");
        }
    }

    private void searchDoctor() {
        System.out.println("\n=== SEARCH DOCTOR ===");
        System.out.println("1. Search by Doctor ID");
        System.out.println("2. Search by IC Number (Last 4 digits)");
        System.out.println("3. Search by Full Name");
        System.out.println("4. Search by Email");
        System.out.println("5. Search by License Number");
        System.out.println("6. Search by Specialty");
        System.out.print("Enter choice: ");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);

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
                searchByLicenseNumber();
                break;
            case 6:
                searchBySpecialty();
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
    }

    private void searchByIC() {
        String lastFourDigits = ConsoleUtils.getStringInput(scanner, "Enter last 4 digits of IC number: ");

        // Validate input - should be exactly 4 digits
        if (lastFourDigits.length() != 4 || !lastFourDigits.matches("\\d{4}")) {
            System.out.println("Invalid input. Please enter exactly 4 digits.");
            return;
        }

        // Use hash function to find doctors with matching IC last 4 digits
        ArrayBucketList<String, Doctor> allDoctors = doctorControl.getAllActiveDoctors();
        ArrayBucketList<String, Doctor> foundDoctors = new ArrayBucketList<String, Doctor>();

        // Calculate hash for the search input
        int searchHash = hashLastFourDigits(lastFourDigits);

        Iterator<Doctor> iterator = allDoctors.iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            if (doctor.getICNumber() != null) {
                // Extract last 4 digits from IC number (format: YYMMDD-XX-XXXX)
                String icNumber = doctor.getICNumber();
                if (icNumber.length() >= 4) {
                    String doctorLastFour = icNumber.substring(icNumber.length() - 4);
                    // Calculate hash for doctor's last 4 digits
                    int doctorHash = hashLastFourDigits(doctorLastFour);

                    // Compare hashes - if they match, then compare the actual strings
                    if (doctorHash == searchHash && doctorLastFour.equals(lastFourDigits)) {
                        foundDoctors.add(doctor.getDoctorId(), doctor);
                    }
                }
            }
        }

        if (!foundDoctors.isEmpty()) {
            System.out.println("\n=== DOCTOR FOUND ===");
            System.out.println();

            Iterator<Doctor> foundIterator = foundDoctors.iterator();
            int count = 1;
            while (foundIterator.hasNext()) {
                Doctor doctor = foundIterator.next();
                System.out.println("--- Doctor " + count + " ---");
                displayDoctorDetails(doctor);
                System.out.println();
                count++;
            }
        } else {
            System.out.println("No doctors found with IC number ending in: " + lastFourDigits);
        }
    }

    /**
     * Hash function for the last 4 digits of IC number
     * Uses the same algorithm as ArrayBucketList's hashEntity method
     */
    private int hashLastFourDigits(String lastFourDigits) {
        if (lastFourDigits == null) {
            return 0;
        }

        int hash = 0;

        // Use a prime multiplier and process each character
        // This helps break up patterns in sequential IDs
        for (int i = 0; i < lastFourDigits.length(); i++) {
            hash = hash * 31 + lastFourDigits.charAt(i);
        }

        // Additional mixing to improve distribution
        hash ^= (hash >>> 16); // XOR with upper bits
        hash *= 0x85ebca6b; // Multiply by a large prime-like number
        hash ^= (hash >>> 13); // More mixing

        return Math.abs(hash);
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
    }

    private void searchByEmail() {
        String email = ConsoleUtils.getStringInput(scanner, "Enter Email: ");
        ArrayBucketList<String, Doctor> allDoctors = doctorControl.getAllActiveDoctors();
        ArrayBucketList<String, Doctor> foundDoctors = new ArrayBucketList<String, Doctor>();

        Iterator<Doctor> iterator = allDoctors.iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            if (doctor.getEmail() != null && doctor.getEmail().toLowerCase().contains(email.toLowerCase())) {
                foundDoctors.add(doctor.getDoctorId(), doctor);
            }
        }

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
    }

    private void searchByLicenseNumber() {
        String licenseNumber = ConsoleUtils.getStringInput(scanner, "Enter License Number: ");
        ArrayBucketList<String, Doctor> allDoctors = doctorControl.getAllActiveDoctors();
        ArrayBucketList<String, Doctor> foundDoctors = new ArrayBucketList<String, Doctor>();

        Iterator<Doctor> iterator = allDoctors.iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            if (doctor.getLicenseNumber() != null
                    && doctor.getLicenseNumber().toLowerCase().contains(licenseNumber.toLowerCase())) {
                foundDoctors.add(doctor.getDoctorId(), doctor);
            }
        }

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
    }

    private void searchBySpecialty() {
        String specialty = ConsoleUtils.getStringInput(scanner, "Enter Specialty: ");
        ArrayBucketList<String, Doctor> allDoctors = doctorControl.getAllActiveDoctors();
        ArrayBucketList<String, Doctor> foundDoctors = new ArrayBucketList<String, Doctor>();

        Iterator<Doctor> iterator = allDoctors.iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            if (doctor.getMedicalSpecialty() != null
                    && doctor.getMedicalSpecialty().toLowerCase().contains(specialty.toLowerCase())) {
                foundDoctors.add(doctor.getDoctorId(), doctor);
            }
        }

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
        System.out.println("\n=== DOCTOR ACTIVITY REPORT ===");
        System.out.println("Sort report?");
        System.out.print("Enter yes/no: ");
        boolean wantSort = getBooleanInput();

        String sortBy = "";
        boolean ascending = true;
        if (wantSort) {
            System.out.println("Select sort field: ");
            System.out.println("1. Name");
            System.out.println("2. Specialty");
            System.out.println("3. Experience");
            int fieldChoice = ConsoleUtils.getIntInput(scanner, "Enter choice (1-3): ", 1, 3);
            if (fieldChoice == 1) {
                sortBy = "name";
            } else if (fieldChoice == 2) {
                sortBy = "specialty";
            } else {
                sortBy = "experience";
            }

            System.out.println("Order: ");
            System.out.println("1. Ascending");
            System.out.println("2. Descending");
            int orderChoice = ConsoleUtils.getIntInput(scanner, "Enter choice (1-2): ", 1, 2);
            ascending = (orderChoice == 1);

            System.out.println(doctorControl.generateDoctorInformationReport(sortBy, ascending));
        } else {
            System.out.println(doctorControl.generateDoctorInformationReport());
        }
    }

    private void generateScheduleReportUI() {
        System.out.println("\n=== DOCTOR WORKLOAD REPORT ===");
        System.out.println("Sort report?");
        System.out.print("Enter yes/no: ");
        boolean wantSort = getBooleanInput();

        String sortBy = "";
        boolean ascending = true;
        if (wantSort) {
            System.out.println("Select sort field: ");
            System.out.println("1. Name");
            System.out.println("2. Specialty");
            System.out.println("3. Weekly Hours");
            System.out.println("4. Annual Hours");
            int fieldChoice = ConsoleUtils.getIntInput(scanner, "Enter choice (1-4): ", 1, 4);
            if (fieldChoice == 1) {
                sortBy = "name";
            } else if (fieldChoice == 2) {
                sortBy = "specialty";
            } else if (fieldChoice == 3) {
                sortBy = "weekly";
            } else {
                sortBy = "annual";
            }

            System.out.println("Order: ");
            System.out.println("1. Ascending");
            System.out.println("2. Descending");
            int orderChoice = ConsoleUtils.getIntInput(scanner, "Enter choice (1-2): ", 1, 2);
            ascending = (orderChoice == 1);

            System.out.println(doctorControl.generateDoctorWorkloadReport(sortBy, ascending));
        } else {
            System.out.println(doctorControl.generateDoctorWorkloadReport());
        }
    }

    private void generateReportsMenu() {
        System.out.println("\n=== GENERATE REPORTS ===");
        System.out.println("1. Doctor Activity Report");
        System.out.println("2. Doctor Workload Report");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        if (choice == 1) {
            generateDoctorInformationReportUI();
        } else {
            generateScheduleReportUI();
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
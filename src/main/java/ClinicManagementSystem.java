import control.*;
import entity.*;
import adt.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * Main Application Class for Clinic Management System
 * Demonstrates all 5 modules with sample operations
 */
public class ClinicManagementSystem {
    
    private static PatientManagementControl patientControl;
    private static DoctorManagementControl doctorControl;
    private static ConsultationManagementControl consultationControl;
    private static MedicalTreatmentControl treatmentControl;
    private static PharmacyManagementControl pharmacyControl;
    private static Scanner scanner;
    
    public static void main(String[] args) {
        initializeSystem();
        displayMainMenu();
    }
    
    private static void initializeSystem() {
        patientControl = new PatientManagementControl();
        doctorControl = new DoctorManagementControl();
        consultationControl = new ConsultationManagementControl();
        treatmentControl = new MedicalTreatmentControl();
        pharmacyControl = new PharmacyManagementControl();
        scanner = new Scanner(System.in);
        
        System.out.println("=== TAR UMT CLINIC MANAGEMENT SYSTEM ===");
        System.out.println("System initialized successfully!");
        System.out.println("All 5 modules are ready for operation.\n");
    }
    
    private static void displayMainMenu() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Patient Management Module");
            System.out.println("2. Doctor Management Module");
            System.out.println("3. Consultation Management Module");
            System.out.println("4. Medical Treatment Management Module");
            System.out.println("5. Pharmacy Management Module");
            System.out.println("6. Generate All Reports");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    patientManagementMenu();
                    break;
                case 2:
                    doctorManagementMenu();
                    break;
                case 3:
                    consultationManagementMenu();
                    break;
                case 4:
                    treatmentManagementMenu();
                    break;
                case 5:
                    pharmacyManagementMenu();
                    break;
                case 6:
                    generateAllReports();
                    break;
                case 7:
                    System.out.println("Thank you for using TAR UMT Clinic Management System!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void patientManagementMenu() {
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
    
    private static void doctorManagementMenu() {
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
    
    private static void consultationManagementMenu() {
        while (true) {
            System.out.println("\n=== CONSULTATION MANAGEMENT MODULE ===");
            System.out.println("1. Schedule Consultation");
            System.out.println("2. Start Consultation");
            System.out.println("3. Complete Consultation");
            System.out.println("4. Cancel Consultation");
            System.out.println("5. Search Consultations");
            System.out.println("6. Generate Consultation Reports");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    scheduleConsultation();
                    break;
                case 2:
                    startConsultation();
                    break;
                case 3:
                    completeConsultation();
                    break;
                case 4:
                    cancelConsultation();
                    break;
                case 5:
                    searchConsultations();
                    break;
                case 6:
                    generateConsultationReports();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void treatmentManagementMenu() {
        while (true) {
            System.out.println("\n=== MEDICAL TREATMENT MANAGEMENT MODULE ===");
            System.out.println("1. Create Treatment");
            System.out.println("2. Update Treatment");
            System.out.println("3. Start Treatment");
            System.out.println("4. Complete Treatment");
            System.out.println("5. Search Treatments");
            System.out.println("6. Generate Treatment Reports");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    createTreatment();
                    break;
                case 2:
                    updateTreatment();
                    break;
                case 3:
                    startTreatment();
                    break;
                case 4:
                    completeTreatment();
                    break;
                case 5:
                    searchTreatments();
                    break;
                case 6:
                    generateTreatmentReports();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }
    
    private static void pharmacyManagementMenu() {
        while (true) {
            System.out.println("\n=== PHARMACY MANAGEMENT MODULE ===");
            System.out.println("1. Add Medicine");
            System.out.println("2. Update Medicine Stock");
            System.out.println("3. Create Prescription");
            System.out.println("4. Add Medicine to Prescription");
            System.out.println("5. Dispense Prescription");
            System.out.println("6. Search Medicines/Prescriptions");
            System.out.println("7. Generate Pharmacy Reports");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = getIntInput();
            
            switch (choice) {
                case 1:
                    addMedicine();
                    break;
                case 2:
                    updateMedicineStock();
                    break;
                case 3:
                    createPrescription();
                    break;
                case 4:
                    addMedicineToPrescription();
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
            }
        }
    }
    
    // Sample implementation methods for demonstration
    private static void registerNewPatient() {
        System.out.println("\n=== REGISTER NEW PATIENT ===");
        System.out.print("Enter full name: ");
        String fullName = scanner.nextLine();
        System.out.print("Enter IC number: ");
        String icNumber = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter phone number: ");
        String phoneNumber = scanner.nextLine();
        
        // Create sample address
        Address address = new Address("123 Sample Street", "Kuala Lumpur", "WP Kuala Lumpur", "50000", "Malaysia");
        ArrayList<String> allergies = new ArrayList<>();
        allergies.add("None");
        
        boolean success = patientControl.registerPatient(fullName, icNumber, email, phoneNumber, 
                                                       address, "W001", BloodType.A_POSITIVE, 
                                                       allergies, "0198765432");
        
        if (success) {
            System.out.println("Patient registered successfully!");
        } else {
            System.out.println("Failed to register patient.");
        }
    }
    
    private static void registerNewDoctor() {
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
        
        // Create sample address
        Address address = new Address("456 Doctor Street", "Petaling Jaya", "Selangor", "47400", "Malaysia");
        
        boolean success = doctorControl.registerDoctor(fullName, icNumber, email, phoneNumber, 
                                                     address, specialty, licenseNumber, expYears);
        
        if (success) {
            System.out.println("Doctor registered successfully!");
        } else {
            System.out.println("Failed to register doctor.");
        }
    }
    
    private static void addMedicine() {
        System.out.println("\n=== ADD MEDICINE ===");
        System.out.print("Enter medicine name: ");
        String medicineName = scanner.nextLine();
        System.out.print("Enter generic name: ");
        String genericName = scanner.nextLine();
        System.out.print("Enter manufacturer: ");
        String manufacturer = scanner.nextLine();
        System.out.print("Enter dosage form: ");
        String dosageForm = scanner.nextLine();
        System.out.print("Enter strength: ");
        String strength = scanner.nextLine();
        System.out.print("Enter quantity in stock: ");
        int quantity = getIntInput();
        System.out.print("Enter unit price: ");
        double price = getDoubleInput();
        
        Date expiryDate = new Date(System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000); // 1 year from now
        
        boolean success = pharmacyControl.addMedicine(medicineName, genericName, manufacturer,
                                                     "Sample description", dosageForm, strength,
                                                     quantity, 10, price, expiryDate, "Shelf A1", true);
        
        if (success) {
            System.out.println("Medicine added successfully!");
        } else {
            System.out.println("Failed to add medicine.");
        }
    }
    
    private static void generateAllReports() {
        System.out.println("\n=== GENERATING ALL REPORTS ===");
        
        System.out.println("\n" + patientControl.generatePatientRegistrationReport());
        System.out.println("\n" + patientControl.generateQueueStatusReport());
        System.out.println("\n" + doctorControl.generateDoctorInformationReport());
        System.out.println("\n" + doctorControl.generateScheduleReport());
        System.out.println("\n" + consultationControl.generateConsultationReport());
        System.out.println("\n" + consultationControl.generateScheduledConsultationsReport());
        System.out.println("\n" + treatmentControl.generateTreatmentReport());
        System.out.println("\n" + treatmentControl.generateTreatmentHistoryReport());
        System.out.println("\n" + pharmacyControl.generateMedicineStockReport());
        System.out.println("\n" + pharmacyControl.generatePrescriptionReport());
        
        System.out.println("All reports generated successfully!");
    }
    
    // Helper methods for input validation
    private static int getIntInput() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException exception) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    private static double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException exception) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
    
    // Placeholder methods for other operations
    private static void updatePatientRecord() {
        System.out.println("Update Patient Record - Implementation needed");
    }
    
    private static void deactivatePatient() {
        System.out.println("Deactivate Patient - Implementation needed");
    }
    
    private static void addPatientToQueue() {
        System.out.println("Add Patient to Queue - Implementation needed");
    }
    
    private static void getNextPatientFromQueue() {
        System.out.println("Get Next Patient from Queue - Implementation needed");
    }
    
    private static void searchPatient() {
        System.out.println("Search Patient - Implementation needed");
    }
    
    private static void generatePatientReports() {
        System.out.println(patientControl.generatePatientRegistrationReport());
        System.out.println(patientControl.generateQueueStatusReport());
    }
    
    private static void updateDoctorInfo() {
        System.out.println("Update Doctor Info - Implementation needed");
    }
    
    private static void addDoctorSchedule() {
        System.out.println("Add Doctor Schedule - Implementation needed");
    }
    
    private static void setDoctorAvailability() {
        System.out.println("Set Doctor Availability - Implementation needed");
    }
    
    private static void searchDoctor() {
        System.out.println("Search Doctor - Implementation needed");
    }
    
    private static void generateDoctorReports() {
        System.out.println(doctorControl.generateDoctorInformationReport());
        System.out.println(doctorControl.generateScheduleReport());
    }
    
    private static void scheduleConsultation() {
        System.out.println("Schedule Consultation - Implementation needed");
    }
    
    private static void startConsultation() {
        System.out.println("Start Consultation - Implementation needed");
    }
    
    private static void completeConsultation() {
        System.out.println("Complete Consultation - Implementation needed");
    }
    
    private static void cancelConsultation() {
        System.out.println("Cancel Consultation - Implementation needed");
    }
    
    private static void searchConsultations() {
        System.out.println("Search Consultations - Implementation needed");
    }
    
    private static void generateConsultationReports() {
        System.out.println(consultationControl.generateConsultationReport());
        System.out.println(consultationControl.generateScheduledConsultationsReport());
    }
    
    private static void createTreatment() {
        System.out.println("Create Treatment - Implementation needed");
    }
    
    private static void updateTreatment() {
        System.out.println("Update Treatment - Implementation needed");
    }
    
    private static void startTreatment() {
        System.out.println("Start Treatment - Implementation needed");
    }
    
    private static void completeTreatment() {
        System.out.println("Complete Treatment - Implementation needed");
    }
    
    private static void searchTreatments() {
        System.out.println("Search Treatments - Implementation needed");
    }
    
    private static void generateTreatmentReports() {
        System.out.println(treatmentControl.generateTreatmentReport());
        System.out.println(treatmentControl.generateTreatmentHistoryReport());
    }
    
    private static void updateMedicineStock() {
        System.out.println("Update Medicine Stock - Implementation needed");
    }
    
    private static void createPrescription() {
        System.out.println("Create Prescription - Implementation needed");
    }
    
    private static void addMedicineToPrescription() {
        System.out.println("Add Medicine to Prescription - Implementation needed");
    }
    
    private static void dispensePrescription() {
        System.out.println("Dispense Prescription - Implementation needed");
    }
    
    private static void searchPharmacyItems() {
        System.out.println("Search Pharmacy Items - Implementation needed");
    }
    
    private static void generatePharmacyReports() {
        System.out.println(pharmacyControl.generateMedicineStockReport());
        System.out.println(pharmacyControl.generatePrescriptionReport());
    }
} 
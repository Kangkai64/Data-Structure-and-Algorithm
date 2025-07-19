package boundary;

import control.ConsultationManagementControl;
import utility.ConsoleUtils;
import java.util.Scanner;

/**
 * Consultation Management User Interface
 * Handles all consultation management user interactions
 */
public class ConsultationManagementUI {
    private Scanner scanner;
    private ConsultationManagementControl consultationControl;

    public ConsultationManagementUI() {
        this.scanner = new Scanner(System.in);
        this.consultationControl = new ConsultationManagementControl();
    }

    public void displayConsultationManagementMenu() {
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
            
            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
            
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

    private void scheduleConsultation() {
        System.out.println("\n=== SCHEDULE CONSULTATION ===");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID: ");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        String dateStr = ConsoleUtils.getStringInput(scanner, "Enter consultation date (YYYY-MM-DD): ");
        String timeStr = ConsoleUtils.getStringInput(scanner, "Enter consultation time (HH:MM): ");
        String symptoms = ConsoleUtils.getStringInput(scanner, "Enter symptoms: ");
        double fee = ConsoleUtils.getDoubleInput(scanner, "Enter consultation fee: ", 0.0, 10000.0);
        
        // For now, just show a placeholder
        System.out.println("Schedule Consultation - Implementation needed");
        System.out.println("Patient ID: " + patientId + ", Doctor ID: " + doctorId);
        System.out.println("Date: " + dateStr + ", Time: " + timeStr);
        System.out.println("Symptoms: " + symptoms + ", Fee: " + fee);
    }

    private void startConsultation() {
        System.out.println("\n=== START CONSULTATION ===");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID: ");
        
        // For now, just show a placeholder
        System.out.println("Start Consultation - Implementation needed");
        System.out.println("Consultation ID: " + consultationId);
    }

    private void completeConsultation() {
        System.out.println("\n=== COMPLETE CONSULTATION ===");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID: ");
        String diagnosis = ConsoleUtils.getStringInput(scanner, "Enter diagnosis: ");
        String treatment = ConsoleUtils.getStringInput(scanner, "Enter treatment: ");
        String notes = ConsoleUtils.getStringInput(scanner, "Enter notes: ");
        
        // For now, just show a placeholder
        System.out.println("Complete Consultation - Implementation needed");
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Diagnosis: " + diagnosis);
        System.out.println("Treatment: " + treatment);
        System.out.println("Notes: " + notes);
    }

    private void cancelConsultation() {
        System.out.println("\n=== CANCEL CONSULTATION ===");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID: ");
        String reason = ConsoleUtils.getStringInput(scanner, "Enter cancellation reason: ");
        
        // For now, just show a placeholder
        System.out.println("Cancel Consultation - Implementation needed");
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Reason: " + reason);
    }

    private void searchConsultations() {
        System.out.println("\n=== SEARCH CONSULTATIONS ===");
        System.out.println("1. Search by Consultation ID");
        System.out.println("2. Search by Patient ID");
        System.out.println("3. Search by Doctor ID");
        System.out.println("4. Search by Date Range");
        System.out.println("5. Search by Status");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 5);
        
        switch (choice) {
            case 1:
                String consultationId = ConsoleUtils.getStringInput(scanner, "Enter Consultation ID: ");
                System.out.println("Search by Consultation ID - Implementation needed");
                break;
            case 2:
                String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
                System.out.println("Search by Patient ID - Implementation needed");
                break;
            case 3:
                String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
                System.out.println("Search by Doctor ID - Implementation needed");
                break;
            case 4:
                String startDate = ConsoleUtils.getStringInput(scanner, "Enter start date (YYYY-MM-DD): ");
                String endDate = ConsoleUtils.getStringInput(scanner, "Enter end date (YYYY-MM-DD): ");
                System.out.println("Search by Date Range - Implementation needed");
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. SCHEDULED  2. IN_PROGRESS  3. COMPLETED  4. CANCELLED");
                System.out.print("Enter choice: ");
                int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
                System.out.println("Search by Status - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateConsultationReports() {
        System.out.println("\n=== CONSULTATION REPORTS ===");
        System.out.println(consultationControl.generateConsultationReport());
        System.out.println(consultationControl.generateConsultationHistoryReport());
    }
} 
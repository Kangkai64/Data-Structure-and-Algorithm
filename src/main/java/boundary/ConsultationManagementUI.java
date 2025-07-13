package boundary;

import control.ConsultationManagementControl;
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

    private void scheduleConsultation() {
        System.out.println("\n=== SCHEDULE CONSULTATION ===");
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine();
        System.out.print("Enter doctor ID: ");
        String doctorId = scanner.nextLine();
        System.out.print("Enter consultation date (YYYY-MM-DD): ");
        String dateStr = scanner.nextLine();
        System.out.print("Enter consultation time (HH:MM): ");
        String timeStr = scanner.nextLine();
        System.out.print("Enter symptoms: ");
        String symptoms = scanner.nextLine();
        System.out.print("Enter consultation fee: ");
        double fee = getDoubleInput();
        
        // For now, just show a placeholder
        System.out.println("Schedule Consultation - Implementation needed");
        System.out.println("Patient ID: " + patientId + ", Doctor ID: " + doctorId);
        System.out.println("Date: " + dateStr + ", Time: " + timeStr);
        System.out.println("Symptoms: " + symptoms + ", Fee: " + fee);
    }

    private void startConsultation() {
        System.out.println("\n=== START CONSULTATION ===");
        System.out.print("Enter consultation ID: ");
        String consultationId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Start Consultation - Implementation needed");
        System.out.println("Consultation ID: " + consultationId);
    }

    private void completeConsultation() {
        System.out.println("\n=== COMPLETE CONSULTATION ===");
        System.out.print("Enter consultation ID: ");
        String consultationId = scanner.nextLine();
        System.out.print("Enter diagnosis: ");
        String diagnosis = scanner.nextLine();
        System.out.print("Enter treatment: ");
        String treatment = scanner.nextLine();
        System.out.print("Enter notes: ");
        String notes = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Complete Consultation - Implementation needed");
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Diagnosis: " + diagnosis);
        System.out.println("Treatment: " + treatment);
        System.out.println("Notes: " + notes);
    }

    private void cancelConsultation() {
        System.out.println("\n=== CANCEL CONSULTATION ===");
        System.out.print("Enter consultation ID: ");
        String consultationId = scanner.nextLine();
        System.out.print("Enter cancellation reason: ");
        String reason = scanner.nextLine();
        
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
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("Enter Consultation ID: ");
                String consultationId = scanner.nextLine();
                System.out.println("Search by Consultation ID - Implementation needed");
                break;
            case 2:
                System.out.print("Enter Patient ID: ");
                String patientId = scanner.nextLine();
                System.out.println("Search by Patient ID - Implementation needed");
                break;
            case 3:
                System.out.print("Enter Doctor ID: ");
                String doctorId = scanner.nextLine();
                System.out.println("Search by Doctor ID - Implementation needed");
                break;
            case 4:
                System.out.print("Enter start date (YYYY-MM-DD): ");
                String startDate = scanner.nextLine();
                System.out.print("Enter end date (YYYY-MM-DD): ");
                String endDate = scanner.nextLine();
                System.out.println("Search by Date Range - Implementation needed");
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. SCHEDULED  2. IN_PROGRESS  3. COMPLETED  4. CANCELLED");
                System.out.print("Enter choice: ");
                int statusChoice = getIntInput();
                System.out.println("Search by Status - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateConsultationReports() {
        System.out.println("\n=== CONSULTATION REPORTS ===");
        System.out.println(consultationControl.generateConsultationReport());
        System.out.println(consultationControl.generateScheduledConsultationsReport());
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

    private double getDoubleInput() {
        while (true) {
            try {
                return Double.parseDouble(scanner.nextLine());
            } catch (NumberFormatException exception) {
                System.out.print("Please enter a valid number: ");
            }
        }
    }
} 
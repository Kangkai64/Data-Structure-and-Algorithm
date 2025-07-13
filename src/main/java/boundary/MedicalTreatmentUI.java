package boundary;

import control.MedicalTreatmentControl;
import java.util.Scanner;

/**
 * Medical Treatment Management User Interface
 * Handles all medical treatment management user interactions
 */
public class MedicalTreatmentUI {
    private Scanner scanner;
    private MedicalTreatmentControl treatmentControl;

    public MedicalTreatmentUI() {
        this.scanner = new Scanner(System.in);
        this.treatmentControl = new MedicalTreatmentControl();
    }

    public void displayTreatmentManagementMenu() {
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

    private void createTreatment() {
        System.out.println("\n=== CREATE TREATMENT ===");
        System.out.print("Enter patient ID: ");
        String patientId = scanner.nextLine();
        System.out.print("Enter doctor ID: ");
        String doctorId = scanner.nextLine();
        System.out.print("Enter consultation ID (optional): ");
        String consultationId = scanner.nextLine();
        System.out.print("Enter diagnosis: ");
        String diagnosis = scanner.nextLine();
        System.out.print("Enter treatment plan: ");
        String treatmentPlan = scanner.nextLine();
        System.out.print("Enter prescribed medications: ");
        String medications = scanner.nextLine();
        System.out.print("Enter treatment notes: ");
        String notes = scanner.nextLine();
        System.out.print("Enter treatment cost: ");
        double cost = getDoubleInput();
        
        // For now, just show a placeholder
        System.out.println("Create Treatment - Implementation needed");
        System.out.println("Patient ID: " + patientId + ", Doctor ID: " + doctorId);
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Diagnosis: " + diagnosis);
        System.out.println("Treatment Plan: " + treatmentPlan);
        System.out.println("Medications: " + medications);
        System.out.println("Notes: " + notes);
        System.out.println("Cost: " + cost);
    }

    private void updateTreatment() {
        System.out.println("\n=== UPDATE TREATMENT ===");
        System.out.print("Enter treatment ID: ");
        String treatmentId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Update Treatment - Implementation needed");
        System.out.println("Treatment ID: " + treatmentId);
    }

    private void startTreatment() {
        System.out.println("\n=== START TREATMENT ===");
        System.out.print("Enter treatment ID: ");
        String treatmentId = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Start Treatment - Implementation needed");
        System.out.println("Treatment ID: " + treatmentId);
    }

    private void completeTreatment() {
        System.out.println("\n=== COMPLETE TREATMENT ===");
        System.out.print("Enter treatment ID: ");
        String treatmentId = scanner.nextLine();
        System.out.print("Enter follow-up date (YYYY-MM-DD, optional): ");
        String followUpDate = scanner.nextLine();
        
        // For now, just show a placeholder
        System.out.println("Complete Treatment - Implementation needed");
        System.out.println("Treatment ID: " + treatmentId);
        System.out.println("Follow-up Date: " + followUpDate);
    }

    private void searchTreatments() {
        System.out.println("\n=== SEARCH TREATMENTS ===");
        System.out.println("1. Search by Treatment ID");
        System.out.println("2. Search by Patient ID");
        System.out.println("3. Search by Doctor ID");
        System.out.println("4. Search by Consultation ID");
        System.out.println("5. Search by Status");
        System.out.println("6. Search by Date Range");
        System.out.print("Enter choice: ");
        
        int choice = getIntInput();
        
        switch (choice) {
            case 1:
                System.out.print("Enter Treatment ID: ");
                String treatmentId = scanner.nextLine();
                System.out.println("Search by Treatment ID - Implementation needed");
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
                System.out.print("Enter Consultation ID: ");
                String consultationId = scanner.nextLine();
                System.out.println("Search by Consultation ID - Implementation needed");
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. PRESCRIBED  2. IN_PROGRESS  3. COMPLETED  4. CANCELLED");
                System.out.print("Enter choice: ");
                int statusChoice = getIntInput();
                System.out.println("Search by Status - Implementation needed");
                break;
            case 6:
                System.out.print("Enter start date (YYYY-MM-DD): ");
                String startDate = scanner.nextLine();
                System.out.print("Enter end date (YYYY-MM-DD): ");
                String endDate = scanner.nextLine();
                System.out.println("Search by Date Range - Implementation needed");
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateTreatmentReports() {
        System.out.println("\n=== TREATMENT REPORTS ===");
        System.out.println(treatmentControl.generateTreatmentReport());
        System.out.println(treatmentControl.generateTreatmentHistoryReport());
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
package boundary;

import control.MedicalTreatmentControl;
import utility.ConsoleUtils;
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
            
            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
            
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
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID: ");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID (optional): ");
        String diagnosis = ConsoleUtils.getStringInput(scanner, "Enter diagnosis: ");
        String treatmentPlan = ConsoleUtils.getStringInput(scanner, "Enter treatment plan: ");
        String medications = ConsoleUtils.getStringInput(scanner, "Enter prescribed medications: ");
        String notes = ConsoleUtils.getStringInput(scanner, "Enter treatment notes: ");
        double cost = ConsoleUtils.getDoubleInput(scanner, "Enter treatment cost: ", 0.0, 100000.0);
        
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
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        
        // For now, just show a placeholder
        System.out.println("Update Treatment - Implementation needed");
        System.out.println("Treatment ID: " + treatmentId);
    }

    private void startTreatment() {
        System.out.println("\n=== START TREATMENT ===");
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        
        // For now, just show a placeholder
        System.out.println("Start Treatment - Implementation needed");
        System.out.println("Treatment ID: " + treatmentId);
    }

    private void completeTreatment() {
        System.out.println("\n=== COMPLETE TREATMENT ===");
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        String followUpDate = ConsoleUtils.getStringInput(scanner, "Enter follow-up date (YYYY-MM-DD, optional): ");
        
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
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
        
        switch (choice) {
            case 1:
                String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter Treatment ID: ");
                System.out.println("Search by Treatment ID - Implementation needed");
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
                String consultationId = ConsoleUtils.getStringInput(scanner, "Enter Consultation ID: ");
                System.out.println("Search by Consultation ID - Implementation needed");
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. PRESCRIBED  2. IN_PROGRESS  3. COMPLETED  4. CANCELLED");
                System.out.print("Enter choice: ");
                int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
                System.out.println("Search by Status - Implementation needed");
                break;
            case 6:
                String startDate = ConsoleUtils.getStringInput(scanner, "Enter start date (YYYY-MM-DD): ");
                String endDate = ConsoleUtils.getStringInput(scanner, "Enter end date (YYYY-MM-DD): ");
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
} 
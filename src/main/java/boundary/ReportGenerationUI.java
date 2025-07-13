package boundary;

import control.*;
import utility.ConsoleUtils;
import java.util.Scanner;

/**
 * Report Generation User Interface
 * Handles all report generation functionality
 */
public class ReportGenerationUI {
    private Scanner scanner;
    private PatientManagementControl patientControl;
    private DoctorManagementControl doctorControl;
    private ConsultationManagementControl consultationControl;
    private MedicalTreatmentControl treatmentControl;
    private PharmacyManagementControl pharmacyControl;

    public ReportGenerationUI() {
        this.scanner = new Scanner(System.in);
        this.patientControl = new PatientManagementControl();
        this.doctorControl = new DoctorManagementControl();
        this.consultationControl = new ConsultationManagementControl();
        this.treatmentControl = new MedicalTreatmentControl();
        this.pharmacyControl = new PharmacyManagementControl();
    }

    public void generateAllReports() {
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

    public void displayReportMenu() {
        while (true) {
            System.out.println("\n=== REPORT GENERATION MENU ===");
            System.out.println("1. Patient Reports");
            System.out.println("2. Doctor Reports");
            System.out.println("3. Consultation Reports");
            System.out.println("4. Treatment Reports");
            System.out.println("5. Pharmacy Reports");
            System.out.println("6. Generate All Reports");
            System.out.println("7. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
            
            switch (choice) {
                case 1:
                    generatePatientReports();
                    break;
                case 2:
                    generateDoctorReports();
                    break;
                case 3:
                    generateConsultationReports();
                    break;
                case 4:
                    generateTreatmentReports();
                    break;
                case 5:
                    generatePharmacyReports();
                    break;
                case 6:
                    generateAllReports();
                    break;
                case 7:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void generatePatientReports() {
        System.out.println("\n=== PATIENT REPORTS ===");
        System.out.println("1. Patient Registration Report");
        System.out.println("2. Queue Status Report");
        System.out.println("3. Both Reports");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        
        switch (choice) {
            case 1:
                System.out.println(patientControl.generatePatientRegistrationReport());
                break;
            case 2:
                System.out.println(patientControl.generateQueueStatusReport());
                break;
            case 3:
                System.out.println(patientControl.generatePatientRegistrationReport());
                System.out.println(patientControl.generateQueueStatusReport());
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateDoctorReports() {
        System.out.println("\n=== DOCTOR REPORTS ===");
        System.out.println("1. Doctor Information Report");
        System.out.println("2. Schedule Report");
        System.out.println("3. Both Reports");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        
        switch (choice) {
            case 1:
                System.out.println(doctorControl.generateDoctorInformationReport());
                break;
            case 2:
                System.out.println(doctorControl.generateScheduleReport());
                break;
            case 3:
                System.out.println(doctorControl.generateDoctorInformationReport());
                System.out.println(doctorControl.generateScheduleReport());
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateConsultationReports() {
        System.out.println("\n=== CONSULTATION REPORTS ===");
        System.out.println("1. Consultation Report");
        System.out.println("2. Scheduled Consultations Report");
        System.out.println("3. Both Reports");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        
        switch (choice) {
            case 1:
                System.out.println(consultationControl.generateConsultationReport());
                break;
            case 2:
                System.out.println(consultationControl.generateScheduledConsultationsReport());
                break;
            case 3:
                System.out.println(consultationControl.generateConsultationReport());
                System.out.println(consultationControl.generateScheduledConsultationsReport());
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateTreatmentReports() {
        System.out.println("\n=== TREATMENT REPORTS ===");
        System.out.println("1. Treatment Report");
        System.out.println("2. Treatment History Report");
        System.out.println("3. Both Reports");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        
        switch (choice) {
            case 1:
                System.out.println(treatmentControl.generateTreatmentReport());
                break;
            case 2:
                System.out.println(treatmentControl.generateTreatmentHistoryReport());
                break;
            case 3:
                System.out.println(treatmentControl.generateTreatmentReport());
                System.out.println(treatmentControl.generateTreatmentHistoryReport());
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generatePharmacyReports() {
        System.out.println("\n=== PHARMACY REPORTS ===");
        System.out.println("1. Medicine Stock Report");
        System.out.println("2. Prescription Report");
        System.out.println("3. Both Reports");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        
        switch (choice) {
            case 1:
                System.out.println(pharmacyControl.generateMedicineStockReport());
                break;
            case 2:
                System.out.println(pharmacyControl.generatePrescriptionReport());
                break;
            case 3:
                System.out.println(pharmacyControl.generateMedicineStockReport());
                System.out.println(pharmacyControl.generatePrescriptionReport());
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
} 
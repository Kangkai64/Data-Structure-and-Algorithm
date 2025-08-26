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
        ConsoleUtils.printHeader("Generating All Reports");
        
        System.out.println("\n" + patientControl.generatePatientRegistrationReport());
        System.out.println("\n" + patientControl.generateQueueStatusReport());
        System.out.println("\n" + doctorControl.generateDoctorInformationReport());
        System.out.println("\n" + doctorControl.generateScheduleReport());
        System.out.println("\n" + consultationControl.generateConsultationReport("date", "desc"));
        System.out.println("\n" + consultationControl.generateConsultationHistoryReport("date", "desc"));
        System.out.println("\n" + treatmentControl.generateTreatmentReport());
        System.out.println("\n" + treatmentControl.generateTreatmentHistoryReport());
        System.out.println("\n" + pharmacyControl.generateMedicineStockReport("name", "asc"));
        System.out.println("\n" + pharmacyControl.generatePrescriptionReport("date", "desc"));
        
        System.out.println("All reports generated successfully!");
    }

    public void displayReportMenu() {
        while (true) {
            ConsoleUtils.printHeader("Report Generation Menu");
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
        ConsoleUtils.printHeader("Patient Reports");
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
        ConsoleUtils.printHeader("Doctor Reports");
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
        ConsoleUtils.printHeader("Consultation Reports");
        System.out.println("1. Consultation Report");
        System.out.println("2. Consultation History Report");
        System.out.println("3. Both Reports");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);
        
        switch (choice) {
            case 1:
                generateConsultationReport();
                break;
            case 2:
                generateConsultationHistoryReport();
                break;
            case 3:
                generateConsultationReport();
                generateConsultationHistoryReport();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }
    
    private void generateConsultationReport() {
        ConsoleUtils.printHeader("Consultation Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Consultation ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Consultation Date");
        System.out.println("5. Status");
        System.out.println("6. Consultation Fee");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        String sortBy = getConsultationSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(consultationControl.generateConsultationReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }
    
    private void generateConsultationHistoryReport() {
        ConsoleUtils.printHeader("Consultation History Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Consultation ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Consultation Date");
        System.out.println("5. Status");
        System.out.println("6. Consultation Fee");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        String sortBy = getConsultationSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(consultationControl.generateConsultationHistoryReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }
    
    private String getConsultationSortField(int choice) {
        switch (choice) {
            case 1: return "id";
            case 2: return "patient";
            case 3: return "doctor";
            case 4: return "date";
            case 5: return "status";
            case 6: return "fee";
            default: return "date";
        }
    }

    private void generateTreatmentReports() {
        ConsoleUtils.printHeader("Treatment Reports");
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
        ConsoleUtils.printHeader("Pharmacy Reports");
        System.out.println("1. Medicine Stock Report");
        System.out.println("2. Prescription Report");
        System.out.println("3. Both Reports");
        System.out.println("4. Back to Report Menu");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        
        switch (choice) {
            case 1:
                generateMedicineStockReportFromReportUI();
                break;
            case 2:
                generatePrescriptionReportFromReportUI();
                break;
            case 3:
                generateMedicineStockReportFromReportUI();
                ConsoleUtils.waitMessage();
                generatePrescriptionReportFromReportUI();
                break;
            case 4:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateMedicineStockReportFromReportUI() {
        ConsoleUtils.printHeader("Medicine Stock Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Medicine ID");
        System.out.println("2. Medicine Name");
        System.out.println("3. Generic Name");
        System.out.println("4. Stock Quantity");
        System.out.println("5. Unit Price");
        System.out.println("6. Expiry Date");
        System.out.println("7. Status");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        String sortBy = getMedicineSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(pharmacyControl.generateMedicineStockReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generatePrescriptionReportFromReportUI() {
        ConsoleUtils.printHeader("Prescription Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Prescription ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Prescription Date");
        System.out.println("5. Status");
        System.out.println("6. Total Cost");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        String sortBy = getPrescriptionSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(pharmacyControl.generatePrescriptionReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private String getMedicineSortField(int choice) {
        switch (choice) {
            case 1: return "id";
            case 2: return "name";
            case 3: return "generic";
            case 4: return "stock";
            case 5: return "price";
            case 6: return "expiry";
            case 7: return "status";
            default: return "name";
        }
    }

    private String getPrescriptionSortField(int choice) {
        switch (choice) {
            case 1: return "id";
            case 2: return "patient";
            case 3: return "doctor";
            case 4: return "date";
            case 5: return "status";
            case 6: return "cost";
            default: return "date";
        }
    }
} 
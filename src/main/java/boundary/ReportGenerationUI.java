package boundary;

import java.util.Scanner;

import control.ConsultationManagementControl;
import control.DoctorManagementControl;
import control.MedicalTreatmentControl;
import control.PatientManagementControl;
import control.PharmacyManagementControl;
import utility.ConsoleUtils;

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
        this.patientControl.loadActivePatients();
        this.doctorControl.loadDoctorData();
        this.consultationControl.loadConsultationData();
        this.treatmentControl.loadTreatmentData();
        this.pharmacyControl.loadPharmacyData();
    }

    public void generateAllReports() {
        ConsoleUtils.printHeader("Generating All Reports");

        System.out.println("\n" + patientControl.generatePatientRecordSummaryReport("id", "desc"));
        System.out.println("\n" + patientControl.generatePatientDemographicsReport("age", "desc"));
        System.out.println("\n" + patientControl.generatePatientVisitHistoryReport("name", "asc"));
        System.out.println("\n" + doctorControl.generateDoctorInformationReport("name", "asc"));
        System.out.println("\n" + doctorControl.generateDoctorWorkloadReport("name", true));
        System.out.println("\n" + doctorControl.generateDoctorPerformanceReport("consultations", false));
        System.out.println("\n" + consultationControl.generateConsultationReport("date", "desc"));
        System.out.println("\n" + consultationControl.generateConsultationHistoryReport("date", "desc"));
        System.out.println("\n" + consultationControl.generateConsultationEfficiencyReport("efficiency", "desc"));
        System.out.println("\n" + treatmentControl.generateTreatmentAnalysisReport("id", "asc"));
        System.out.println("\n" + treatmentControl.generateTreatmentStatusReport("id", "asc"));
        System.out.println("\n" + treatmentControl.generateTreatmentOutcomeReport("success", "desc"));
        System.out.println("\n" + pharmacyControl.generateMedicineStockReport("name", "asc"));
        System.out.println("\n" + pharmacyControl.generatePrescriptionReport("date", "desc"));
        System.out.println("\n" + pharmacyControl.generateMedicineUsageReport("prescriptions", "desc"));

        System.out.println("All reports generated successfully!");
    }

    public void displayReportMenu() {
        while (true) {
            ConsoleUtils.clearScreen();
            ConsoleUtils.printHeader("Report Generation Menu");
            System.out.println("1. Patient Reports");
            System.out.println("2. Doctor Reports");
            System.out.println("3. Consultation Reports");
            System.out.println("4. Treatment Reports");
            System.out.println("5. Pharmacy Reports");
            System.out.println("6. Generate All Reports");
            System.out.println("7. Back to Main Menu");

            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
            System.out.println();

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
        System.out.println("1. Patient Record Summary Report");
        System.out.println("2. Patient Demographics Report");
        System.out.println("3. Patient Visit History Report");
        System.out.println("4. All Reports");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                generatePatientRecordSummaryReport();
                break;
            case 2:
                generatePatientDemographicsReport();
                break;
            case 3:
                generatePatientVisitHistoryReport();
                break;
            case 4:
                generatePatientRecordSummaryReport();
                generatePatientDemographicsReport();
                generatePatientVisitHistoryReport();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generatePatientRecordSummaryReport() {
        ConsoleUtils.printHeader("Patient Record Summary Report Options");
        System.out.println("Sort by options:");
        System.out.println("1. Name (default)");
        System.out.println("2. Patient ID");
        System.out.println("3. IC Number");
        System.out.println("4. Email");
        System.out.println("5. Phone");
        System.out.println("6. Blood Type");
        System.out.println("7. Allergies");
        System.out.println("8. Registration Date");
        System.out.println("9. Status");

        int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter sort field choice: ", 1, 9);
        System.out.println();
        
        System.out.println("Sort order:");
        System.out.println("1. Ascending (A-Z, 0-9)");
        System.out.println("2. Descending (Z-A, 9-0)");
        
        int orderChoice = ConsoleUtils.getIntInput(scanner, "Enter sort order choice: ", 1, 2);
        System.out.println();

        String sortBy = getSortField(sortChoice);
        String sortOrder = orderChoice == 1 ? "asc" : "desc";
        
        System.out.println(patientControl.generatePatientRecordSummaryReport(sortBy, sortOrder));
    }

    private void generatePatientDemographicsReport() {
        ConsoleUtils.printHeader("Patient Demographics Report");
        System.out.println("Select field to sort by:");
        System.out.println("1. Age");
        System.out.println("2. Gender");
        System.out.println("3. Blood Type");
        System.out.println("4. Allergies");
        System.out.println("5. Registration Date");
        System.out.println("6. Patient Name");
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);

        String sortBy;
        switch (sortFieldChoice) {
            case 1:
                sortBy = "age";
                break;
            case 2:
                sortBy = "gender";
                break;
            case 3:
                sortBy = "blood";
                break;
            case 4:
                sortBy = "allergies";
                break;
            case 5:
                sortBy = "regdate";
                break;
            case 6:
                sortBy = "name";
                break;
            default:
                sortBy = "age";
                break;
        }

        System.out.println(patientControl.generatePatientDemographicsReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generatePatientVisitHistoryReport() {
        ConsoleUtils.printHeader("Patient Visit History Report");
        System.out.println("Sort by options:");
        System.out.println("1. Name (default)");
        System.out.println("2. Patient ID");
        System.out.println("3. IC Number");
        System.out.println("4. Email");
        System.out.println("5. Phone");
        System.out.println("6. Blood Type");
        System.out.println("7. Allergies");
        System.out.println("8. Registration Date");
        System.out.println("9. Status");

        int sortChoice = ConsoleUtils.getIntInput(scanner, "Enter sort field choice: ", 1, 9);
        System.out.println();
        
        System.out.println("Sort order:");
        System.out.println("1. Ascending (A-Z, 0-9)");
        System.out.println("2. Descending (Z-A, 9-0)");
        
        int orderChoice = ConsoleUtils.getIntInput(scanner, "Enter sort order choice: ", 1, 2);
        System.out.println();

        String sortBy = getSortField(sortChoice);
        String sortOrder = orderChoice == 1 ? "asc" : "desc";
        
        System.out.println(patientControl.generatePatientVisitHistoryReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private String getSortField(int choice) {
        switch (choice) {
            case 1: return "name";
            case 2: return "id";
            case 3: return "ic";
            case 4: return "email";
            case 5: return "phone";
            case 6: return "blood";
            case 7: return "allergies";
            case 8: return "regdate";
            case 9: return "status";
            default: return "name";
        }
    }

    private void generateDoctorReports() {
        ConsoleUtils.printHeader("Doctor Reports");
        System.out.println("1. Doctor Information Report");
        System.out.println("2. Doctor Workload Report");
        System.out.println("3. Doctor Performance Report");
        System.out.println("4. All Reports");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                System.out.println(doctorControl.generateDoctorInformationReport("name", "asc"));
                break;
            case 2:
                System.out.println(doctorControl.generateDoctorWorkloadReport("name", true));
                break;
            case 3:
                generateDoctorPerformanceReport();
                break;
            case 4:
                System.out.println(doctorControl.generateDoctorInformationReport("name", "asc"));
                System.out.println(doctorControl.generateDoctorWorkloadReport("name", true));
                generateDoctorPerformanceReport();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateDoctorPerformanceReport() {
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

        System.out.println(doctorControl.generateDoctorPerformanceReport(sortBy, false));
        ConsoleUtils.waitMessage();
    }

    private void generateConsultationReports() {
        ConsoleUtils.printHeader("Consultation Reports");
        System.out.println("1. Consultation Report");
        System.out.println("2. Consultation History Report");
        System.out.println("3. Consultation Efficiency Report");
        System.out.println("4. All Reports");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                generateConsultationReport();
                break;
            case 2:
                generateConsultationHistoryReport();
                break;
            case 3:
                generateConsultationEfficiencyReport();
                break;
            case 4:
                generateConsultationReport();
                generateConsultationHistoryReport();
                generateConsultationEfficiencyReport();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateConsultationEfficiencyReport() {
        ConsoleUtils.printHeader("Consultation Efficiency Report");
        System.out.println("1. Sort by Efficiency Score");
        System.out.println("2. Sort by Wait Time");
        System.out.println("3. Sort by Duration");
        System.out.println("4. Sort by Date");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        String sortBy = switch (choice) {
            case 1 -> "efficiency";
            case 2 -> "wait";
            case 3 -> "duration";
            case 4 -> "date";
            default -> "efficiency";
        };

        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();
        System.out.println(consultationControl.generateConsultationEfficiencyReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generateConsultationReport() {
        ConsoleUtils.printHeader("Consultation Report");

        String sortBy = getConsultationSortField();
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();

        System.out.println(consultationControl.generateConsultationReport(sortBy, sortOrder));
    }

    private void generateConsultationHistoryReport() {
        ConsoleUtils.printHeader("Consultation History Report");

        String sortBy = getConsultationSortField();
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();

        System.out.println(consultationControl.generateConsultationHistoryReport(sortBy, sortOrder));
    }

    private String getConsultationSortField() {
        System.out.println("Select field to sort by:");
        System.out.println("1. Consultation ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Consultation Date");
        System.out.println("5. Status");
        System.out.println("6. Consultation Fee");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);

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
        System.out.println("1. Treatment Analysis Report");
        System.out.println("2. Treatment Status Report");
        System.out.println("3. Treatment Outcome Report");
        System.out.println("4. All Reports");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                System.out.println(treatmentControl.generateTreatmentAnalysisReport("id", "asc"));
                break;
            case 2:
                System.out.println(treatmentControl.generateTreatmentStatusReport("id", "asc"));
                break;
            case 3:
                System.out.println(treatmentControl.generateTreatmentOutcomeReport("success", "desc"));
                break;
            case 4:
                System.out.println(treatmentControl.generateTreatmentAnalysisReport("id", "asc"));
                System.out.println(treatmentControl.generateTreatmentStatusReport("id", "asc"));
                System.out.println(treatmentControl.generateTreatmentOutcomeReport("success", "desc"));
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generatePharmacyReports() {
        ConsoleUtils.printHeader("Pharmacy Reports");
        System.out.println("1. Medicine Stock Report");
        System.out.println("2. Prescription Report");
        System.out.println("3. Medicine Usage Report");
        System.out.println("4. All Reports");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        System.out.println();

        switch (choice) {
            case 1:
                generateMedicineStockReportFromReportUI();
                break;
            case 2:
                generatePrescriptionReportFromReportUI();
                break;
            case 3:
                generateMedicineUsageReportFromReportUI();
                break;
            case 4:
                generateMedicineStockReportFromReportUI();
                generatePrescriptionReportFromReportUI();
                generateMedicineUsageReportFromReportUI();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateMedicineUsageReportFromReportUI() {
        ConsoleUtils.printHeader("Medicine Usage Report");

        String sortBy = getMedicineUsageSortField();
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();
        System.out.println(pharmacyControl.generateMedicineUsageReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generateMedicineStockReportFromReportUI() {
        ConsoleUtils.printHeader("Medicine Stock Report");

        String sortBy = getMedicineSortField();
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();

        System.out.println(pharmacyControl.generateMedicineStockReport(sortBy, sortOrder));
    }

    private void generatePrescriptionReportFromReportUI() {
        ConsoleUtils.printHeader("Prescription Report");

        String sortBy = getPrescriptionSortField();
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);
        System.out.println();

        System.out.println(pharmacyControl.generatePrescriptionReport(sortBy, sortOrder));
    }

    private String getMedicineSortField() {
        System.out.println("Select field to sort by:");
        System.out.println("1. Medicine ID");
        System.out.println("2. Medicine Name");
        System.out.println("3. Generic Name");
        System.out.println("4. Stock Quantity");
        System.out.println("5. Unit Price");
        System.out.println("6. Expiry Date");
        System.out.println("7. Status");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);

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

    private String getPrescriptionSortField() {
        System.out.println("Select field to sort by:");
        System.out.println("1. Prescription ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Prescription Date");
        System.out.println("5. Status");
        System.out.println("6. Total Cost");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);

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

    private String getMedicineUsageSortField() {
        System.out.println("Select field to sort by:");
        System.out.println("1. Prescription Count");
        System.out.println("2. Revenue");
        System.out.println("3. Stock Level");
        System.out.println("4. Category");
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);

        return switch (choice) {
            case 1 -> "prescriptions";
            case 2 -> "revenue";
            case 3 -> "stock";
            case 4 -> "category";
            default -> "prescriptions";
        };
    }
}
 package boundary;

import java.util.Scanner;
import utility.ConsoleUtils;

/**
 * Main Menu User Interface
 * Handles the main menu display and navigation to other modules
 */
public class MainMenuUI {
    private Scanner scanner;
    private PatientManagementUI patientUI;
    private DoctorManagementUI doctorUI;
    private ConsultationManagementUI consultationUI;
    private MedicalTreatmentUI treatmentUI;
    private PharmacyManagementUI pharmacyUI;
    private ReportGenerationUI reportUI;

    public MainMenuUI() {
        this.scanner = new Scanner(System.in);
        this.patientUI = new PatientManagementUI();
        this.doctorUI = new DoctorManagementUI();
        this.consultationUI = new ConsultationManagementUI();
        this.treatmentUI = new MedicalTreatmentUI();
        this.pharmacyUI = new PharmacyManagementUI();
        this.reportUI = new ReportGenerationUI();
    }

    public void displayMainMenu() {
        while (true) {
            System.out.println("\n=== MAIN MENU ===");
            System.out.println("1. Patient Management Module");
            System.out.println("2. Doctor Management Module");
            System.out.println("3. Consultation Management Module");
            System.out.println("4. Medical Treatment Management Module");
            System.out.println("5. Pharmacy Management Module");
            System.out.println("6. Report Management Module");
            System.out.println("7. Generate All Reports");
            System.out.println("8. Exit");

            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 8);
            System.out.println();

            switch (choice) {
                case 1:
                    patientUI.displayPatientManagementMenu();
                    break;
                case 2:
                    doctorUI.displayDoctorManagementMenu();
                    break;
                case 3:
                    consultationUI.displayConsultationManagementMenu();
                    break;
                case 4:
                    treatmentUI.displayTreatmentManagementMenu();
                    break;
                case 5:
                    pharmacyUI.displayPharmacyManagementMenu();
                    break;
                case 6:
                    reportUI.displayReportMenu();
                    break;
                case 7:
                    reportUI.generateAllReports();
                    break;
                case 8:
                    System.out.println("Thank you for using TAR UMT Clinic Management System!");
                    scanner.close();
                    System.exit(0);
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    public static void main(String[] args) {
        MainMenuUI mainMenu = new MainMenuUI();
        mainMenu.displayMainMenu();
    }
}
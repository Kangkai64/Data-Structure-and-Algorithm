package boundary;

import control.ConsultationManagementControl;
import entity.Consultation;
import entity.Patient;
import entity.Doctor;
import dao.PatientDao;
import dao.DoctorDao;
import utility.ConsoleUtils;
import utility.DateType;
import adt.ArrayBucketList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Consultation Management User Interface
 * Handles all consultation management user interactions
 */
public class ConsultationManagementUI {
    private Scanner scanner;
    private ConsultationManagementControl consultationControl;
    private PatientDao patientDao;
    private DoctorDao doctorDao;

    public ConsultationManagementUI() {
        this.scanner = new Scanner(System.in);
        this.consultationControl = new ConsultationManagementControl();
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
    }

    public void displayConsultationManagementMenu() {
        while (true) {
            consultationControl.loadConsultationData();
            ConsoleUtils.printHeader("Consultation Management Module");
            System.out.println("1. Schedule Consultation");
            System.out.println("2. Start Consultation");
            System.out.println("3. Complete Consultation");
            System.out.println("4. Cancel Consultation");
            System.out.println("5. Search Consultations");
            System.out.println("6. Generate Consultation Reports");
            System.out.println("7. Back to Main Menu");

            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
            System.out.println();
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
                    ConsoleUtils.waitMessage();
            }
        }
    }

    private void scheduleConsultation() {
        ConsoleUtils.printHeader("Schedule Consultation");
        
        // Get patient details
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID: ");
        Patient patient = null;
        try {
            patient = patientDao.findById(patientId);
            if (patient == null) {
                System.out.println("Patient not found.");
                ConsoleUtils.waitMessage();
                return;
            }
        } catch (Exception e) {
            System.out.println("Error finding patient: " + e.getMessage());
            ConsoleUtils.waitMessage();
            return;
        }

        // Get doctor details
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        Doctor doctor = null;
        try {
            doctor = doctorDao.findById(doctorId);
            if (doctor == null) {
                System.out.println("Doctor not found.");
                ConsoleUtils.waitMessage();
                return;
            }
        } catch (Exception e) {
            System.out.println("Error finding doctor: " + e.getMessage());
            ConsoleUtils.waitMessage();
            return;
        }

        // Get consultation date and time
        LocalDate consultationDate = ConsoleUtils.getDateInput(scanner, "Enter consultation date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY);
        System.out.println("Enter consultation time (24-hour format):");
        int hour = ConsoleUtils.getIntInput(scanner, "Enter hour (0-23): ", 0, 23);
        int minute = ConsoleUtils.getIntInput(scanner, "Enter minute (0-59): ", 0, 59);
        LocalDateTime consultationDateTime = LocalDateTime.of(consultationDate, LocalTime.of(hour, minute));
        
        String symptoms = ConsoleUtils.getStringInput(scanner, "Enter symptoms: ");
        double consultationFee = ConsoleUtils.getDoubleInput(scanner, "Enter consultation fee: ", 0.0, 10000.0);
        
        // Display consultation overview
        ConsoleUtils.printHeader("Consultation Overview");
        System.out.println("Patient: " + patient.getFullName() + " (ID: " + patientId + ")");
        System.out.println("Doctor: " + doctor.getFullName() + " (ID: " + doctorId + ")");
        System.out.println("Date & Time: " + consultationDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        System.out.println("Symptoms: " + symptoms);
        System.out.println("Fee: RM" + consultationFee);
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to schedule this consultation? (Y/N): ");
        if (confirm) {
            if (consultationControl.scheduleConsultation(patient, doctor, consultationDateTime, symptoms, consultationFee)) {
                System.out.println("Consultation scheduled successfully.");
            } else {
                System.out.println("Consultation not scheduled.");
            }
        } else {
            System.out.println("Consultation not scheduled.");
        }
        ConsoleUtils.waitMessage();
    }

    private void startConsultation() {
        ConsoleUtils.printHeader("Start Consultation");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID: ");
        
        Consultation consultation = consultationControl.findConsultationById(consultationId);
        if (consultation == null) {
            System.out.println("Consultation not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        ConsoleUtils.printHeader("Consultation Overview");
        System.out.println(consultation);
        System.out.println();

        if (consultation.getStatus() != Consultation.ConsultationStatus.SCHEDULED) {
            System.out.println("Consultation cannot be started. Current status: " + consultation.getStatus());
            ConsoleUtils.waitMessage();
            return;
        }

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to start this consultation? (Y/N): ");
        if (confirm) {
            if (consultationControl.startConsultation(consultationId)) {
                System.out.println("Consultation started successfully.");
            } else {
                System.out.println("Consultation not started.");
            }
        } else {
            System.out.println("Consultation not started.");
        }
        ConsoleUtils.waitMessage();
    }

    private void completeConsultation() {
        ConsoleUtils.printHeader("Complete Consultation");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID: ");
        
        Consultation consultation = consultationControl.findConsultationById(consultationId);
        if (consultation == null) {
            System.out.println("Consultation not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        ConsoleUtils.printHeader("Consultation Overview");
        System.out.println(consultation);
        System.out.println();

        if (consultation.getStatus() != Consultation.ConsultationStatus.IN_PROGRESS) {
            System.out.println("Consultation cannot be completed. Current status: " + consultation.getStatus());
            ConsoleUtils.waitMessage();
            return;
        }

        String diagnosis = ConsoleUtils.getStringInput(scanner, "Enter diagnosis: ");
        String treatment = ConsoleUtils.getStringInput(scanner, "Enter treatment: ");
        String notes = ConsoleUtils.getStringInput(scanner, "Enter notes: ");
        
        // Ask for next visit date (optional)
        LocalDateTime nextVisitDate = null;
        boolean hasNextVisit = ConsoleUtils.getBooleanInput(scanner, "Schedule next visit? (Y/N): ");
        if (hasNextVisit) {
            LocalDate nextVisitDateOnly = ConsoleUtils.getDateInput(scanner, "Enter next visit date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY);
            System.out.println("Enter next visit time (24-hour format):");
            int hour = ConsoleUtils.getIntInput(scanner, "Enter hour (0-23): ", 0, 23);
            int minute = ConsoleUtils.getIntInput(scanner, "Enter minute (0-59): ", 0, 59);
            nextVisitDate = LocalDateTime.of(nextVisitDateOnly, LocalTime.of(hour, minute));
        }

        ConsoleUtils.printHeader("Consultation Summary");
        System.out.println("Diagnosis: " + diagnosis);
        System.out.println("Treatment: " + treatment);
        System.out.println("Notes: " + notes);
        if (nextVisitDate != null) {
            System.out.println("Next Visit: " + nextVisitDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        }
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to complete this consultation? (Y/N): ");
        if (confirm) {
            if (consultationControl.completeConsultation(consultationId, diagnosis, treatment, notes, nextVisitDate)) {
                System.out.println("Consultation completed successfully.");
            } else {
                System.out.println("Consultation not completed.");
            }
        } else {
            System.out.println("Consultation not completed.");
        }
        ConsoleUtils.waitMessage();
    }

    private void cancelConsultation() {
        ConsoleUtils.printHeader("Cancel Consultation");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID: ");
        
        Consultation consultation = consultationControl.findConsultationById(consultationId);
        if (consultation == null) {
            System.out.println("Consultation not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        ConsoleUtils.printHeader("Consultation Overview");
        System.out.println(consultation);
        System.out.println();

        if (consultation.getStatus() != Consultation.ConsultationStatus.SCHEDULED) {
            System.out.println("Consultation cannot be cancelled. Current status: " + consultation.getStatus());
            ConsoleUtils.waitMessage();
            return;
        }

        String reason = ConsoleUtils.getStringInput(scanner, "Enter cancellation reason: ");
        
        ConsoleUtils.printHeader("Cancellation Details");
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Reason: " + reason);
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to cancel this consultation? (Y/N): ");
        if (confirm) {
            if (consultationControl.cancelConsultation(consultationId)) {
                System.out.println("Consultation cancelled successfully.");
            } else {
                System.out.println("Consultation not cancelled.");
            }
        } else {
            System.out.println("Consultation not cancelled.");
        }
        ConsoleUtils.waitMessage();
    }

    private void searchConsultations() {
        ConsoleUtils.printHeader("Search Consultations");
        System.out.println("1. Search by Consultation ID");
        System.out.println("2. Search by Patient ID");
        System.out.println("3. Search by Doctor ID");
        System.out.println("4. Search by Date Range");
        System.out.println("5. Search by Status");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 5);
        System.out.println();

        switch (choice) {
            case 1:
                searchConsultationById();
                break;
            case 2:
                searchConsultationsByPatient();
                break;
            case 3:
                searchConsultationsByDoctor();
                break;
            case 4:
                searchConsultationsByDateRange();
                break;
            case 5:
                searchConsultationsByStatus();
                break;
            default:
                System.out.println("Invalid choice.");
        }
        ConsoleUtils.waitMessage();
    }

    private void searchConsultationById() {
        ConsoleUtils.printHeader("Search by Consultation ID");
        String consultationId = ConsoleUtils.getStringInput(scanner, "Enter Consultation ID: ");
        
        Consultation consultation = consultationControl.findConsultationById(consultationId);
        if (consultation == null) {
            System.out.println("Consultation not found.");
        } else {
            System.out.println();
            ConsoleUtils.printHeader("Search Result");
            System.out.println("\n" + consultation);
        }
    }

    private void searchConsultationsByPatient() {
        ConsoleUtils.printHeader("Search by Patient ID");
        String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
        
        ArrayBucketList<String, Consultation> consultations = consultationControl.findConsultationsByPatient(patientId);
        if (consultations.isEmpty()) {
            System.out.println("No consultations found for this patient.");
        } else {
            System.out.println();
            ConsoleUtils.printHeader("Search Result");
            System.out.println("\n" + consultations.parseElementsToString());
        }
    }

    private void searchConsultationsByDoctor() {
        ConsoleUtils.printHeader("Search by Doctor ID");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
        
        ArrayBucketList<String, Consultation> consultations = consultationControl.findConsultationsByDoctor(doctorId);
        if (consultations.isEmpty()) {
            System.out.println("No consultations found for this doctor.");
        } else {
            System.out.println();
            ConsoleUtils.printHeader("Search Result");
            System.out.println("\n" + consultations.parseElementsToString());
        }
    }

    private void searchConsultationsByDateRange() {
        ConsoleUtils.printHeader("Search by Date Range");
        LocalDate startDate = ConsoleUtils.getDateInput(scanner, "Enter start date (DD-MM-YYYY): ", DateType.PAST_DATE_ONLY);
        LocalDate endDate = ConsoleUtils.getDateInput(scanner, "Enter end date (DD-MM-YYYY): ", DateType.PAST_DATE_ONLY);
        
        // Validate date range
        if (startDate.isAfter(endDate)) {
            System.out.println("Start date cannot be after end date.");
            return;
        }
        
        // Get all consultations and filter by date range
        ArrayBucketList<String, Consultation> allConsultations = consultationControl.getAllConsultations();
        ArrayBucketList<String, Consultation> filteredConsultations = new ArrayBucketList<String, Consultation>();
        
        for (Consultation consultation : allConsultations) {
            LocalDate consultationDate = consultation.getConsultationDate().toLocalDate();
            if (!consultationDate.isBefore(startDate) && !consultationDate.isAfter(endDate)) {
                filteredConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        
        if (filteredConsultations.isEmpty()) {
            System.out.println("No consultations found for this date range.");
        } else {
            System.out.println();
            ConsoleUtils.printHeader("Search Result");
            System.out.println("Date Range: " + startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + 
                             " to " + endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            System.out.println("Found " + filteredConsultations.getSize() + " consultation(s):\n");
            System.out.println(filteredConsultations.parseElementsToString());
        }
    }

    private void searchConsultationsByStatus() {
        ConsoleUtils.printHeader("Search by Status");
        System.out.println("Select status:");
        System.out.println("1. SCHEDULED  2. IN_PROGRESS  3. COMPLETED  4. CANCELLED");
        
        int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        
        Consultation.ConsultationStatus status = null;
        switch (statusChoice) {
            case 1:
                status = Consultation.ConsultationStatus.SCHEDULED;
                break;
            case 2:
                status = Consultation.ConsultationStatus.IN_PROGRESS;
                break;
            case 3:
                status = Consultation.ConsultationStatus.COMPLETED;
                break;
            case 4:
                status = Consultation.ConsultationStatus.CANCELLED;
                break;
        }
        
        // Get all consultations and filter by status
        ArrayBucketList<String, Consultation> allConsultations = consultationControl.getAllConsultations();
        ArrayBucketList<String, Consultation> filteredConsultations = new ArrayBucketList<String, Consultation>();
        
        for (Consultation consultation : allConsultations) {
            if (consultation.getStatus() == status) {
                filteredConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        
        if (filteredConsultations.isEmpty()) {
            System.out.println("No consultations found with status: " + status);
        } else {
            System.out.println();
            ConsoleUtils.printHeader("Search Result");
            System.out.println("\n" + filteredConsultations.parseElementsToString());
        }
    }

    private void generateConsultationReports() {
        ConsoleUtils.printHeader("Consultation Reports");
        System.out.println(consultationControl.generateConsultationReport());
        ConsoleUtils.waitMessage();
        System.out.println(consultationControl.generateConsultationHistoryReport());
        ConsoleUtils.waitMessage();
    }
} 
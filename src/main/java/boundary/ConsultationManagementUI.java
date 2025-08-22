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
            System.out.println("5. View Queue Status");
            System.out.println("6. Search Consultations");
            System.out.println("7. Generate Consultation Reports");
            System.out.println("8. Back to Main Menu");

            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 8);
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
                    viewQueueStatus();
                    break;
                case 6:
                    searchConsultations();
                    break;
                case 7:
                    generateConsultationReports();
                    break;
                case 8:
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

        // Choose date (DD-MM-YYYY)
        LocalDate consultationDate = ConsoleUtils.getDateInput(scanner, "Enter consultation date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY);

        // Show available doctors and operating hours for the selected day
        ArrayBucketList<String, entity.Schedule> schedulesForDate = consultationControl.getAvailableSchedulesByDate(consultationDate);
        if (schedulesForDate.isEmpty()) {
            System.out.println("No doctor schedules are available on this date.");
            ConsoleUtils.waitMessage();
            return;
        }

        ConsoleUtils.printHeader("Available Doctors and Operating Hours");
        for (entity.Schedule schedule : schedulesForDate) {
            printScheduleDetails(schedule);
        }
        System.out.println();

        // Select doctor from the list
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID from the list above: ");
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

        // Show available 1-hour slots (breaks excluded) and choose one
        String[] slotOptions = consultationControl.getAvailableSlotTimes(doctorId, consultationDate);
        if (slotOptions.length == 0) {
            System.out.println("No available 1-hour slots for this doctor on the selected date.");
            ConsoleUtils.waitMessage();
            return;
        }

        ConsoleUtils.printHeader("Available 1-hour Slots (breaks excluded)");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (int index = 0; index < slotOptions.length; index++) {
            LocalTime t = LocalTime.parse(slotOptions[index]);
            System.out.println((index + 1) + ". " + t.format(timeFormatter));
        }
        int chosenIndex = ConsoleUtils.getIntInput(scanner, "Choose a slot (1-" + slotOptions.length + "): ", 1, slotOptions.length) - 1;
        LocalTime selectedTime = LocalTime.parse(slotOptions[chosenIndex]);

        LocalDateTime consultationDateTime = LocalDateTime.of(consultationDate, selectedTime);
        
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
        printConsultationDetails(consultation);
        System.out.println();

        if (consultation.getStatus() != Consultation.ConsultationStatus.IN_PROGRESS) {
            System.out.println("Consultation cannot be completed. Current status: " + consultation.getStatus());
            ConsoleUtils.waitMessage();
            return;
        }

        String diagnosis = ConsoleUtils.getStringInput(scanner, "Enter diagnosis: ");
        String treatment = ConsoleUtils.getStringInput(scanner, "Enter treatment: ");
        String notes = ConsoleUtils.getStringInput(scanner, "Enter notes: ");
        
        // Ask for next visit scheduling (optional)
        LocalDateTime nextVisitDate = null;
        boolean hasNextVisit = ConsoleUtils.getBooleanInput(scanner, "Schedule next visit? (Y/N): ");
        if (hasNextVisit) {
            nextVisitDate = scheduleNextVisit(consultation);
            if (nextVisitDate == null) {
                System.out.println("Next visit scheduling cancelled.");
            }
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
        printConsultationDetails(consultation);
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
            printConsultationDetails(consultation);
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
            for (Consultation consultation : consultations) {
                printConsultationDetails(consultation);
            }
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
            for (Consultation consultation : consultations) {
                printConsultationDetails(consultation);
            }
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
            for (Consultation consultation : filteredConsultations) {
                printConsultationDetails(consultation);
            }
        }
    }

    private void searchConsultationsByStatus() {
        ConsoleUtils.printHeader("Search by Status");
        System.out.println("Select status:");
        System.out.println("1. SCHEDULED\n  2. IN_PROGRESS\n  3. COMPLETED\n  4. CANCELLED");
        
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
            for (Consultation consultation : filteredConsultations) {
                printConsultationDetails(consultation);
            }
        }
    }

    private void generateConsultationReports() {
        ConsoleUtils.printHeader("Consultation Reports");
        System.out.println(consultationControl.generateConsultationReport());
        ConsoleUtils.waitMessage();
        System.out.println(consultationControl.generateConsultationHistoryReport());
        ConsoleUtils.waitMessage();
    }

    // Helpers for consistent, report-like output
    private void printConsultationDetails(Consultation consultation) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        System.out.println("Consultation ID: " + consultation.getConsultationId());
        System.out.println("Patient: " + consultation.getPatient().getFullName() + " (" + consultation.getPatient().getPatientId() + ")");
        System.out.println("Doctor: " + consultation.getDoctor().getFullName() + " (" + consultation.getDoctor().getDoctorId() + ")");
        System.out.println("Date: " + consultation.getConsultationDate().format(dateTimeFormatter));
        System.out.println("Status: " + consultation.getStatus());
        System.out.println("Fee: RM" + consultation.getConsultationFee());
        System.out.println("----------------------------------------");
    }

    private void printScheduleDetails(entity.Schedule schedule) {
        Doctor doctor = null;
        try {
            doctor = doctorDao.findById(schedule.getDoctorId());
        } catch (Exception ignored) {}
        String doctorName = doctor != null ? doctor.getFullName() : "Unknown";
        System.out.println("Doctor ID: " + schedule.getDoctorId());
        System.out.println("Doctor Name: " + doctorName);
        System.out.println("Day: " + schedule.getDayOfWeek());
        System.out.println("From: " + schedule.getFromTime());
        System.out.println("To: " + schedule.getToTime());
        System.out.println("Available: " + (schedule.isAvailable() ? "Yes" : "No"));
        System.out.println("----------------------------------------");
    }
    
    /**
     * Start consultation using queue system
     * Enqueues booked consultations in ascending order of slot number according to system date
     */
    private void startConsultation() {
        ConsoleUtils.printHeader("Start Consultation (Queue-based)");
        
        // Get doctor ID
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
        
        // Check if doctor exists
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
        
        // Show current status
        System.out.println("Doctor: " + doctor.getFullName());
        System.out.println("Status: " + consultationControl.getDoctorConsultationStatus(doctorId));
        System.out.println();
        
        // Start consultation using queue
        String result = consultationControl.startConsultation(doctorId);
        System.out.println(result);
        
        ConsoleUtils.waitMessage();
    }
    
    /**
     * View queue status for all doctors
     */
    private void viewQueueStatus() {
        ConsoleUtils.printHeader("Consultation Queue Status");
        System.out.println(consultationControl.getQueueStatus());
        ConsoleUtils.waitMessage();
    }
    
    /**
     * Schedule next visit using the same slot selection system as scheduleConsultation
     */
    private LocalDateTime scheduleNextVisit(Consultation currentConsultation) {
        ConsoleUtils.printHeader("Schedule Next Visit");
        
        // Use the same doctor as the current consultation
        Doctor doctor = currentConsultation.getDoctor();
        Patient patient = currentConsultation.getPatient();
        
        System.out.println("Patient: " + patient.getFullName() + " (ID: " + patient.getPatientId() + ")");
        System.out.println("Doctor: " + doctor.getFullName() + " (ID: " + doctor.getDoctorId() + ")");
        System.out.println();
        
        // Choose date (DD-MM-YYYY) - must be in the future
        LocalDate nextVisitDate = ConsoleUtils.getDateInput(scanner, "Enter next visit date (DD-MM-YYYY): ", DateType.FUTURE_DATE_ONLY);
        
        // Show available doctors and operating hours for the selected day
        ArrayBucketList<String, entity.Schedule> schedulesForDate = consultationControl.getAvailableSchedulesByDate(nextVisitDate);
        if (schedulesForDate.isEmpty()) {
            System.out.println("No doctor schedules are available on this date.");
            ConsoleUtils.waitMessage();
            return null;
        }
        
        // Check if the current doctor has schedule on the selected date
        boolean doctorAvailable = false;
        for (entity.Schedule schedule : schedulesForDate) {
            if (schedule.getDoctorId().equals(doctor.getDoctorId())) {
                doctorAvailable = true;
                break;
            }
        }
        
        if (!doctorAvailable) {
            System.out.println("The current doctor is not available on the selected date.");
            System.out.println("Available doctors for " + nextVisitDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ":");
            for (entity.Schedule schedule : schedulesForDate) {
                printScheduleDetails(schedule);
            }
            
            boolean useDifferentDoctor = ConsoleUtils.getBooleanInput(scanner, "Would you like to schedule with a different doctor? (Y/N): ");
            if (!useDifferentDoctor) {
                return null;
            }
            
            // Let user select a different doctor
            String newDoctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID from the list above: ");
            try {
                doctor = doctorDao.findById(newDoctorId);
                if (doctor == null) {
                    System.out.println("Doctor not found.");
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error finding doctor: " + e.getMessage());
                return null;
            }
        }
        
        // Show available 1-hour slots (breaks excluded) and choose one
        String[] slotOptions = consultationControl.getAvailableSlotTimes(doctor.getDoctorId(), nextVisitDate);
        if (slotOptions.length == 0) {
            System.out.println("No available 1-hour slots for this doctor on the selected date.");
            ConsoleUtils.waitMessage();
            return null;
        }
        
        ConsoleUtils.printHeader("Available 1-hour Slots (breaks excluded)");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        for (int index = 0; index < slotOptions.length; index++) {
            LocalTime t = LocalTime.parse(slotOptions[index]);
            System.out.println((index + 1) + ". " + t.format(timeFormatter));
        }
        
        int chosenIndex = ConsoleUtils.getIntInput(scanner, "Choose a slot (1-" + slotOptions.length + "): ", 1, slotOptions.length) - 1;
        LocalTime selectedTime = LocalTime.parse(slotOptions[chosenIndex]);
        
        LocalDateTime nextVisitDateTime = LocalDateTime.of(nextVisitDate, selectedTime);
        
        // Display next visit overview
        ConsoleUtils.printHeader("Next Visit Overview");
        System.out.println("Patient: " + patient.getFullName() + " (ID: " + patient.getPatientId() + ")");
        System.out.println("Doctor: " + doctor.getFullName() + " (ID: " + doctor.getDoctorId() + ")");
        System.out.println("Date & Time: " + nextVisitDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        System.out.println();
        
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to schedule this next visit? (Y/N): ");
        if (confirm) {
            // Schedule the next consultation
            String symptoms = ConsoleUtils.getStringInput(scanner, "Enter symptoms for next visit: ");
            double consultationFee = ConsoleUtils.getDoubleInput(scanner, "Enter consultation fee: ", 0.0, 10000.0);
            
            if (consultationControl.scheduleConsultation(patient, doctor, nextVisitDateTime, symptoms, consultationFee)) {
                System.out.println("Next visit scheduled successfully.");
                return nextVisitDateTime;
            } else {
                System.out.println("Failed to schedule next visit.");
                return null;
            }
        } else {
            System.out.println("Next visit scheduling cancelled.");
            return null;
        }
    }
} 
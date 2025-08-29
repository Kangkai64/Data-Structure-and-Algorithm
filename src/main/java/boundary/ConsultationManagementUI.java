package boundary;

import control.ConsultationManagementControl;
import entity.Consultation;
import entity.Patient;
import entity.Doctor;
import utility.ConsoleUtils;
import utility.DateType;
import adt.ArrayBucketList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * @author: Poh Qi Xuan
 *          Consultation Management User Interface
 *          Handles all consultation management user interactions
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
        Patient patient = consultationControl.getPatientById(patientId);
        if (patient == null) {
            System.out.println("Patient not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        // Choose date (DD-MM-YYYY)
        LocalDate consultationDate = ConsoleUtils.getDateInput(scanner, "Enter consultation date (DD-MM-YYYY): ",
                DateType.FUTURE_DATE_ONLY);

        // Show available doctors and operating hours for the selected day
        ArrayBucketList<String, entity.Schedule> schedulesForDate = consultationControl
                .getAvailableSchedulesByDate(consultationDate);
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
        Doctor doctor = consultationControl.getDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found.");
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
        int chosenIndex = ConsoleUtils.getIntInput(scanner, "Choose a slot (1-" + slotOptions.length + "): ", 1,
                slotOptions.length) - 1;
        LocalTime selectedTime = LocalTime.parse(slotOptions[chosenIndex]);

        LocalDateTime consultationDateTime = LocalDateTime.of(consultationDate, selectedTime);

        String symptoms = ConsoleUtils.getStringInput(scanner, "Enter symptoms: ");
        double consultationFee = ConsoleUtils.getDoubleInput(scanner, "Enter consultation fee: ", 0.0, 10000.0);

        // Display consultation overview
        ConsoleUtils.printHeader("Consultation Overview");
        System.out.println("Patient: " + patient.getFullName() + " (ID: " + patientId + ")");
        System.out.println("Doctor: " + doctor.getFullName() + " (ID: " + doctorId + ")");
        System.out.println(
                "Date & Time: " + consultationDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        System.out.println("Symptoms: " + symptoms);
        System.out.println("Fee: RM%.2f".formatted(consultationFee));
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to schedule this consultation? (Y/N): ");
        if (confirm) {
            if (consultationControl.scheduleConsultation(patient, doctor, consultationDateTime, symptoms,
                    consultationFee)) {
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

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to complete this consultation? (Y/N): ");
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

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to cancel this consultation? (Y/N): ");
        if (confirm) {
            if (consultationControl.cancelConsultation(consultationId, reason)) {
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
        System.out.println("6. Search by Payment Status");

        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 6);
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
            case 6:
                searchConsultationsByPaymentStatus();
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
        System.out.println();
        ConsoleUtils.printHeader("Search Result");

        if (consultations.isEmpty()) {
            System.out.println("No consultations found for patient ID: " + patientId);
        } else {
            System.out.println("Found " + consultations.getSize() + " consultation(s) for patient ID: " + patientId);
            System.out.println();
            if (consultations.getSize() > 1) {
                System.out.println("Sort results?\n1. Yes\n2. No");
                int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                if (choice == 1) {
                    String sortBy = getConsultationSortField();
                    System.out.println();
                    String sortOrder = getSortOrder();
                    System.out.println(consultationControl.displaySortedConsultationSearchResults(consultations,
                            "Patient ID: " + patientId, sortBy, sortOrder));
                } else {
                    for (Consultation consultation : consultations) {
                        printConsultationDetails(consultation);
                    }
                }
            } else {
                for (Consultation consultation : consultations) {
                    printConsultationDetails(consultation);
                }
            }
        }
    }

    private void searchConsultationsByDoctor() {
        ConsoleUtils.printHeader("Search by Doctor ID");
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");

        ArrayBucketList<String, Consultation> consultations = consultationControl.findConsultationsByDoctor(doctorId);
        System.out.println();
        ConsoleUtils.printHeader("Search Result");

        if (consultations.isEmpty()) {
            System.out.println("No consultations found for doctor ID: " + doctorId);
        } else {
            System.out.println("Found " + consultations.getSize() + " consultation(s) for doctor ID: " + doctorId);
            System.out.println();
            if (consultations.getSize() > 1) {
                System.out.println("Sort results?\n1. Yes\n2. No");
                int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                if (choice == 1) {
                    String sortBy = getConsultationSortField();
                    System.out.println();
                    String sortOrder = getSortOrder();
                    System.out.println(consultationControl.displaySortedConsultationSearchResults(consultations,
                            "Doctor ID: " + doctorId, sortBy, sortOrder));
                } else {
                    for (Consultation consultation : consultations) {
                        printConsultationDetails(consultation);
                    }
                }
            } else {
                for (Consultation consultation : consultations) {
                    printConsultationDetails(consultation);
                }
            }
        }
    }

    private void searchConsultationsByDateRange() {
        ConsoleUtils.printHeader("Search by Date Range");
        LocalDate startDate = ConsoleUtils.getDateInput(scanner, "Enter start date (DD-MM-YYYY): ",
                DateType.PAST_DATE_ONLY);
        LocalDate endDate = ConsoleUtils.getDateInput(scanner, "Enter end date (DD-MM-YYYY): ",
                DateType.PAST_DATE_ONLY);

        // Validate date range
        if (startDate.isAfter(endDate)) {
            System.out.println("Start date cannot be after end date.");
            return;
        }

        ArrayBucketList<String, Consultation> consultations = consultationControl
                .findConsultationsByDateRange(startDate, endDate);
        System.out.println();
        ConsoleUtils.printHeader("Search Result");
        System.out.println("Date Range: " + startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
                " to " + endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        if (consultations.isEmpty()) {
            System.out.println("No consultations found in the specified date range.");
        } else {
            System.out.println("Found " + consultations.getSize() + " consultation(s) in the specified date range:");
            System.out.println();
            if (consultations.getSize() > 1) {
                System.out.println("Sort results?\n1. Yes\n2. No");
                int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                if (choice == 1) {
                    String sortBy = getConsultationSortField();
                    System.out.println();
                    String sortOrder = getSortOrder();
                    System.out.println(consultationControl.displaySortedConsultationSearchResults(consultations,
                            String.format("Date Range: %s to %s",
                                    startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                                    endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))),
                            sortBy, sortOrder));
                } else {
                    for (Consultation consultation : consultations) {
                        printConsultationDetails(consultation);
                    }
                }
            } else {
                for (Consultation consultation : consultations) {
                    printConsultationDetails(consultation);
                }
            }
        }
    }

    private void searchConsultationsByStatus() {
        ConsoleUtils.printHeader("Search by Status");
        System.out.println("Select status:");
        System.out.println("1. SCHEDULED\n2. IN_PROGRESS\n3. COMPLETED\n4. CANCELLED");

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

        ArrayBucketList<String, Consultation> consultations = consultationControl.findConsultationsByStatus(status);
        System.out.println();
        ConsoleUtils.printHeader("Search Result");

        if (consultations.isEmpty()) {
            System.out.println("No consultations found with status: " + status);
        } else {
            System.out.println("Found " + consultations.getSize() + " consultation(s) with status: " + status);
            System.out.println();
            if (consultations.getSize() > 1) {
                System.out.println("Sort results?\n1. Yes\n2. No");
                int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                if (choice == 1) {
                    String sortBy = getConsultationSortField();
                    System.out.println();
                    String sortOrder = getSortOrder();
                    System.out.println(consultationControl.displaySortedConsultationSearchResults(consultations,
                            "Status: " + status, sortBy, sortOrder));
                } else {
                    for (Consultation consultation : consultations) {
                        printConsultationDetails(consultation);
                    }
                }
            } else {
                for (Consultation consultation : consultations) {
                    printConsultationDetails(consultation);
                }
            }
        }
    }

    private void searchConsultationsByPaymentStatus() {
        ConsoleUtils.printHeader("Search by Payment Status");
        System.out.println("Select payment status:");
        System.out.println("1. PAID\n2. PENDING\n3. CANCELLED");

        int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 3);

        Consultation.PaymentStatus paymentStatus = null;
        switch (statusChoice) {
            case 1:
                paymentStatus = Consultation.PaymentStatus.PAID;
                break;
            case 2:
                paymentStatus = Consultation.PaymentStatus.PENDING;
                break;
            case 3:
                paymentStatus = Consultation.PaymentStatus.CANCELLED;
                break;
        }

        ArrayBucketList<String, Consultation> consultations = consultationControl
                .findConsultationsByPaymentStatus(paymentStatus);
        System.out.println();
        ConsoleUtils.printHeader("Search Result");

        if (consultations.isEmpty()) {
            System.out.println("No consultations found with payment status: " + paymentStatus);
        } else {
            System.out.println("Found " + consultations.getSize() + " consultation(s) with payment status: "
                    + paymentStatus);
            System.out.println();
            if (consultations.getSize() > 1) {
                System.out.println("Sort results?\n1. Yes\n2. No");
                int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
                System.out.println();
                if (choice == 1) {
                    String sortBy = getConsultationSortField();
                    System.out.println();
                    String sortOrder = getSortOrder();
                    System.out.println(consultationControl.displaySortedConsultationSearchResults(consultations,
                            "Payment Status: " + paymentStatus, sortBy, sortOrder));
                } else {
                    for (Consultation consultation : consultations) {
                        printConsultationDetails(consultation);
                    }
                }
            } else {
                for (Consultation consultation : consultations) {
                    printConsultationDetails(consultation);
                }
            }
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
        String sortBy = getConsultationSortField();
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);

        System.out.println(consultationControl.generateConsultationReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generateConsultationHistoryReport() {
        ConsoleUtils.printHeader("Consultation History Report");
        String sortBy = getConsultationSortField();
        System.out.println();
        String sortOrder = ConsoleUtils.getSortOrder(scanner);

        System.out.println(consultationControl.generateConsultationHistoryReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
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

    private String getSortOrder() {
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, 0-9, oldest first)");
        System.out.println("2. Descending (Z-A, 9-0, newest first)");
        int sortOrder = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        return sortOrder == 1 ? "asc" : "desc";
    }

    private void printConsultationDetails(Consultation consultation) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        System.out.println("Consultation ID: " + consultation.getConsultationId());
        System.out.println("Patient: " + consultation.getPatient().getFullName() + " ("
                + consultation.getPatient().getPatientId() + ")");
        System.out.println("Doctor: " + consultation.getDoctor().getFullName() + " ("
                + consultation.getDoctor().getDoctorId() + ")");
        System.out.println("Date: " + consultation.getConsultationDate().format(dateTimeFormatter));
        System.out.println("Status: " + consultation.getStatus());
        System.out.println("Symptoms: " + consultation.getSymptoms());
        if (consultation.getStatus() == Consultation.ConsultationStatus.COMPLETED) {
            System.out.println("Diagnosis: " + consultation.getDiagnosis());
            System.out.println("Treatment: " + consultation.getTreatment());
            System.out.println("Notes: " + consultation.getNotes());
        }
        if (consultation.getStatus() == Consultation.ConsultationStatus.CANCELLED && 
            consultation.getCancellationReason() != null && !consultation.getCancellationReason().trim().isEmpty()) {
            System.out.println("Cancellation Reason: " + consultation.getCancellationReason());
        }
        System.out.println("Fee: RM%.2f".formatted(consultation.getConsultationFee()));
        System.out.println("----------------------------------------");
    }

    private void printScheduleDetails(entity.Schedule schedule) {
        Doctor doctor = consultationControl.getDoctorById(schedule.getDoctorId());
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
     * Enqueues booked consultations in ascending order of slot number according to
     * system date
     */
    private void startConsultation() {
        ConsoleUtils.printHeader("Start Consultation (Queue-based)");

        // Get doctor ID
        String doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");

        // Check if doctor exists
        Doctor doctor = consultationControl.getDoctorById(doctorId);
        if (doctor == null) {
            System.out.println("Doctor not found.");
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
     * Schedule next visit using the same slot selection system as
     * scheduleConsultation
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
        LocalDate nextVisitDate = ConsoleUtils.getDateInput(scanner, "Enter next visit date (DD-MM-YYYY): ",
                DateType.FUTURE_DATE_ONLY);

        // Show available doctors and operating hours for the selected day
        ArrayBucketList<String, entity.Schedule> schedulesForDate = consultationControl
                .getAvailableSchedulesByDate(nextVisitDate);
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
            System.out.println(
                    "Available doctors for " + nextVisitDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + ":");
            for (entity.Schedule schedule : schedulesForDate) {
                printScheduleDetails(schedule);
            }

            boolean useDifferentDoctor = ConsoleUtils.getBooleanInput(scanner,
                    "Would you like to schedule with a different doctor? (Y/N): ");
            if (!useDifferentDoctor) {
                return null;
            }

            // Let user select a different doctor
            String newDoctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID from the list above: ");
            doctor = consultationControl.getDoctorById(newDoctorId);
            if (doctor == null) {
                System.out.println("Doctor not found.");
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

        int chosenIndex = ConsoleUtils.getIntInput(scanner, "Choose a slot (1-" + slotOptions.length + "): ", 1,
                slotOptions.length) - 1;
        LocalTime selectedTime = LocalTime.parse(slotOptions[chosenIndex]);

        LocalDateTime nextVisitDateTime = LocalDateTime.of(nextVisitDate, selectedTime);

        // Display next visit overview
        ConsoleUtils.printHeader("Next Visit Overview");
        System.out.println("Patient: " + patient.getFullName() + " (ID: " + patient.getPatientId() + ")");
        System.out.println("Doctor: " + doctor.getFullName() + " (ID: " + doctor.getDoctorId() + ")");
        System.out.println("Date & Time: " + nextVisitDateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner,
                "Are you sure you want to schedule this next visit? (Y/N): ");
        if (confirm) {
            // Schedule the next consultation
            String symptoms = ConsoleUtils.getStringInput(scanner, "Enter symptoms for next visit: ");
            double consultationFee = ConsoleUtils.getDoubleInput(scanner, "Enter consultation fee: ", 0.0, 10000.0);

            if (consultationControl.scheduleConsultation(patient, doctor, nextVisitDateTime, symptoms,
                    consultationFee)) {
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
package boundary;

import control.MedicalTreatmentControl;
import control.PatientManagementControl;
import control.DoctorManagementControl;
import control.ConsultationManagementControl;
import entity.MedicalTreatment;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import utility.ConsoleUtils;
import utility.DateType;
import adt.ArrayBucketList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.Iterator;



/**
 * Medical Treatment Management User Interface
 * Handles all medical treatment management user interactions
 */
public class MedicalTreatmentUI {
    private Scanner scanner;
    private PatientManagementControl patientControl;
    private DoctorManagementControl doctorControl;
    private ConsultationManagementControl consultationControl;
    private MedicalTreatmentControl treatmentControl;

    public MedicalTreatmentUI() {
        this.scanner = new Scanner(System.in);
        this.treatmentControl = new MedicalTreatmentControl();
        this.patientControl = new PatientManagementControl();
        this.doctorControl = new DoctorManagementControl();
        this.consultationControl = new ConsultationManagementControl();
    }

    

    public void displayTreatmentManagementMenu() {
        while (true) {
            consultationControl.loadConsultationData();
            treatmentControl.loadTreatmentData();
            System.out.println("\n=== MEDICAL TREATMENT MANAGEMENT MODULE ===");
            System.out.println("1. Create Treatment");
            System.out.println("2. Update Treatment");
            System.out.println("3. Start Treatment");
            System.out.println("4. Complete Treatment");
            System.out.println("5. Cancel Treatment");
            System.out.println("6. Search Treatments");
            System.out.println("7. Generate Treatment Reports");
            System.out.println("8. Back to Main Menu");
            System.out.print("Enter your choice: ");
            
            int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 8);
            
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
                    cancelTreatment();
                    break;
                case 6:
                    searchTreatments();
                    break;
                case 7:
                    generateTreatmentReports();
                    break;
                case 8:
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private void createTreatment() {
        consultationControl.loadConsultationData();
        treatmentControl.loadTreatmentData();
        ConsoleUtils.printHeader("Create Treatment");

        // First, show all available completed consultations
        System.out.println("\n=== AVAILABLE COMPLETED CONSULTATIONS ===");
        ArrayBucketList<String, Consultation> completedConsultations = consultationControl.getCompletedConsultations();
        
        if (completedConsultations.getSize() == 0) {
            System.out.println("No completed consultations available for treatment creation.");
            ConsoleUtils.waitMessage();
            return;
        }
        
        // Display all completed consultations
        displayCompletedConsultations(completedConsultations);
        
        // Get consultation ID from user
        Consultation consultation = null;
        String consultationId = null;
        while (true) {
            consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID from the list above: ");
            consultation = consultationControl.findConsultationById(consultationId);
            
            if (consultation == null) {
                System.out.println("Error: Consultation not found with ID: " + consultationId);
                return;
            }
            if (consultation.getStatus() != Consultation.ConsultationStatus.COMPLETED) {
                System.out.println("Error: Consultation must be COMPLETED before creating treatment.");
                return;
            }
            if (treatmentControl.hasTreatmentForConsultation(consultationId)) {
                System.out.println("Error: A treatment already exists for this consultation.");
                return;
            }
            break;
        }
        
        // Get patient and doctor from the selected consultation
        Patient patient = consultation.getPatient();
        Doctor doctor = consultation.getDoctor();

        String diagnosis = ConsoleUtils.getStringInput(scanner, "Enter diagnosis: ");
        String treatmentPlan = ConsoleUtils.getStringInput(scanner, "Enter treatment plan: ");
        String medications = ConsoleUtils.getStringInput(scanner, "Enter prescribed medications: ");
        String notes = ConsoleUtils.getStringInput(scanner, "Enter treatment notes: ");
        double cost = ConsoleUtils.getDoubleInput(scanner, "Enter treatment cost: ", 0.0, 100000.0);
        
        // Preview and confirmation
        System.out.println("\nPlease confirm the treatment details:");
        System.out.println("----------------------------------------");
        System.out.println("Patient: " + patient.getFullName() + " (" + patient.getPatientId() + ")");
        System.out.println("Doctor: " + doctor.getFullName() + " (" + doctor.getDoctorId() + ")");
        System.out.println("Consultation ID: " + consultationId);
        System.out.println("Diagnosis: " + diagnosis);
        System.out.println("Treatment Plan: " + treatmentPlan);
        System.out.println("Medications: " + medications);
        System.out.println("Notes: " + notes);
        System.out.println("Cost: " + cost);
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to add this treatment? (Y/N): ");
        if (!confirm) {
            System.out.println("Action cancelled. Treatment was not created.");
            return;
        }

        String createdTreatmentId = treatmentControl.createTreatment(
            patient,
            doctor,
            consultation,
            diagnosis,
            treatmentPlan,
            medications,
            notes,
            cost
        );
        
        if (createdTreatmentId != null) {
            System.out.println("== Treatment created successfully! ==");
            System.out.println("Treatment ID: " + createdTreatmentId);
            System.out.println("Patient: " + patient.getFullName());
            System.out.println("Doctor: " + doctor.getFullName());
            System.out.println("Diagnosis: " + diagnosis);
            System.out.println("Consultation ID: " + consultationId);
            System.out.println("Diagnosis: " + diagnosis);
            System.out.println("Treatment Plan: " + treatmentPlan);
            System.out.println("Medications: " + medications);
            System.out.println("Notes: " + notes);
            System.out.println("Cost: " + cost);
        } else {
            System.out.println("✗ Failed to create treatment. Please try again.");
        }
       ConsoleUtils.waitMessage();
    }


    
    private void updateTreatment() {
        ConsoleUtils.printHeader("Update Treatment");
        MedicalTreatment treatment = null;
        String treatmentId = null;
        while (true) {
            treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
            treatment = treatmentControl.findTreatmentById(treatmentId);
            if (treatment == null) {
                System.out.println("Error: Treatment not found with ID: " + treatmentId);
                return;
            }
            break;
        }
        
        System.out.println("Current Treatment Details:");
        System.out.println("Patient: " + treatment.getPatient().getFullName());
        System.out.println("Doctor: " + treatment.getDoctor().getFullName());
        System.out.println("Diagnosis: " + treatment.getDiagnosis());
        System.out.println("Status: " + treatment.getStatus());
        System.out.println("Treatment Date: " + treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        if (treatment.getStatus() != MedicalTreatment.TreatmentStatus.PRESCRIBED) {
            System.out.println("Error: Only treatments in PRESCRIBED status can be updated.");
            return;
        }
        
        System.out.println("\nEnter new details (press Enter to keep current value):");
        
        String diagnosis = ConsoleUtils.getStringInput(scanner, "New diagnosis [" + treatment.getDiagnosis() + "]: ", treatment.getDiagnosis());
        
        String treatmentPlan = ConsoleUtils.getStringInput(scanner, "New treatment plan [" + treatment.getTreatmentPlan() + "]: ", treatment.getTreatmentPlan());
        
        String medications = ConsoleUtils.getStringInput(scanner, "New medications [" + treatment.getPrescribedMedications() + "]: ", treatment.getPrescribedMedications());
        
        String notes = ConsoleUtils.getStringInput(scanner, "New notes [" + treatment.getTreatmentNotes() + "]: ", treatment.getTreatmentNotes());
        
        LocalDate existingFollowUp = treatment.getFollowUpDate() != null ? treatment.getFollowUpDate().toLocalDate() : null;
        LocalDate followUpDate = ConsoleUtils.getDateInput(scanner, "New follow-up date (DD-MM-YYYY, press Enter to keep): ", DateType.FUTURE_DATE_ONLY, existingFollowUp);
        
        double cost = ConsoleUtils.getDoubleInput(scanner, "New cost [" + treatment.getTreatmentCost() + "]: ", treatment.getTreatmentCost());
        // Convert LocalDate to LocalDateTime for the control layer
        LocalDateTime followUpDateTime = followUpDate != null ? followUpDate.atStartOfDay() : null;
        
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to update this treatment? (Y/N): ");
        if (!confirm) {
            System.out.println("Action cancelled. Treatment was not updated.");
            return;
        }
        // Update treatment
        boolean success = treatmentControl.updateTreatment(treatmentId, diagnosis, treatmentPlan, medications, notes, followUpDateTime, cost);
        
        if (success) {
            System.out.println("✓ Treatment updated successfully!");
        } else {
            System.out.println("No changes detected or update failed.");
            boolean retryUpdate = ConsoleUtils.getBooleanInput(scanner, "Do you want to try updating with different values? (Y/N): ");
            if (retryUpdate) {
                return;
            }else {
                System.out.println("Update cancelled.");
            }
        }
        ConsoleUtils.waitMessage();
    }
    

    
    private void startTreatment() {
        ConsoleUtils.printHeader("Start Treatment");
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        
        MedicalTreatment treatment = treatmentControl.findTreatmentById(treatmentId);
        if (treatment == null) {
            System.out.println("Error: Treatment not found with ID: " + treatmentId);
            return;
        }
        
        if (treatment.getStatus() != MedicalTreatment.TreatmentStatus.PRESCRIBED) {
            System.out.println("Error: Treatment cannot be started. Current status: " + treatment.getStatus());
            return;
        }
        
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to start this treatment? (Y/N): ");
        if (!confirm) {
            System.out.println("Action cancelled. Treatment was not started.");
            return;
        }

        boolean success = treatmentControl.startTreatment(treatmentId);
        
        if (success) {
            System.out.println("✓ Treatment started successfully!");
            System.out.println("----------------------------------------");
            System.out.println("Treatment ID: " + treatment.getTreatmentId());
            System.out.println("Patient: " + treatment.getPatient().getFullName() + " (" + treatment.getPatient().getPatientId() + ")");
            System.out.println("Doctor: " + treatment.getDoctor().getFullName() + " (" + treatment.getDoctor().getDoctorId() + ")");
            System.out.println("Diagnosis: " + treatment.getDiagnosis());
            System.out.println("Treatment Plan: " + treatment.getTreatmentPlan());
            System.out.println("Prescribed Medications: " + treatment.getPrescribedMedications());
            System.out.println("Notes: " + treatment.getTreatmentNotes());
            System.out.println("Started: " + treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            System.out.println("Status: IN_PROGRESS");
            System.out.println("----------------------------------------");
        } else {
            System.out.println("✗ Failed to start treatment. Please try again.");
        }
        ConsoleUtils.waitMessage();
    }


    private void completeTreatment() {
        ConsoleUtils.printHeader("Complete Treatment");
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        
        MedicalTreatment treatment = treatmentControl.findTreatmentById(treatmentId);
        if (treatment == null) {
            System.out.println("Error: Treatment not found with ID: " + treatmentId);
            return;
        }
        
        if (treatment.getStatus() != MedicalTreatment.TreatmentStatus.IN_PROGRESS) {
            System.out.println("Error: Treatment cannot be completed. Current status: " + treatment.getStatus());
            return;
        }
        
        LocalDate followUpDate = ConsoleUtils.getDateInput(scanner, "Enter follow-up date (DD-MM-YYYY, optional, press Enter to skip): ", DateType.FUTURE_DATE_ONLY);
        
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to complete this treatment? (Y/N): ");
        if (!confirm) {
            System.out.println("Action cancelled. Treatment was not completed.");
            return;
        }
        // Complete treatment and persist optional follow-up date
        boolean success = treatmentControl.completeTreatment(treatmentId, followUpDate != null ? followUpDate.atStartOfDay() : null);
        
        if (success) {
            System.out.println("Treatment completed successfully!");
            System.out.println("----------------------------------------");
            System.out.println("Treatment ID: " + treatment.getTreatmentId());
            System.out.println("Patient: " + treatment.getPatient().getFullName() + " (" + treatment.getPatient().getPatientId() + ")");
            System.out.println("Doctor: " + treatment.getDoctor().getFullName() + " (" + treatment.getDoctor().getDoctorId() + ")");
            System.out.println("Diagnosis: " + treatment.getDiagnosis());
            System.out.println("Treatment Plan: " + treatment.getTreatmentPlan());
            System.out.println("Prescribed Medications: " + treatment.getPrescribedMedications());
            System.out.println("Notes: " + treatment.getTreatmentNotes());
            System.out.println("Started: " + treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            if (followUpDate != null) {
                System.out.println("Follow-up Date: " + followUpDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            } else if (treatment.getFollowUpDate() != null) {
                System.out.println("Follow-up Date: " + treatment.getFollowUpDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            }
            System.out.println("Status: COMPLETED");
            System.out.println("----------------------------------------");
        } else {
            System.out.println("Failed to complete treatment. Please try again.");
        }
        ConsoleUtils.waitMessage();
    }

    private void cancelTreatment() {
        ConsoleUtils.printHeader("Cancel Treatment");
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        
        MedicalTreatment treatment = treatmentControl.findTreatmentById(treatmentId);
        if (treatment == null) {
            System.out.println("Treatment not found.");
            ConsoleUtils.waitMessage();
            return;
        }

        ConsoleUtils.printHeader("Treatment Overview");
        displayTreatmentDetails(treatment);
        System.out.println();

        if (treatment.getStatus() != MedicalTreatment.TreatmentStatus.PRESCRIBED) {
            System.out.println("Treatment cannot be cancelled. Current status: " + treatment.getStatus());
            ConsoleUtils.waitMessage();
            return;
        }

        String reason = ConsoleUtils.getStringInput(scanner, "Enter cancellation reason: ");
        
        ConsoleUtils.printHeader("Cancellation Details");
        System.out.println("Treatment ID: " + treatmentId);
        System.out.println("Patient: " + treatment.getPatient().getFullName() + " (" + treatment.getPatient().getPatientId() + ")");
        System.out.println("Doctor: " + treatment.getDoctor().getFullName() + " (" + treatment.getDoctor().getDoctorId() + ")");
        System.out.println("Diagnosis: " + treatment.getDiagnosis());
        System.out.println("Reason: " + reason);
        System.out.println();

        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "Are you sure you want to cancel this treatment? (Y/N): ");
        if (confirm) {
            if (treatmentControl.cancelTreatment(treatmentId)) {
                System.out.println("Treatment cancelled successfully.");
            } else {
                System.out.println("Treatment not cancelled.");
            }
        } else {
            System.out.println("Treatment not cancelled.");
        }
        ConsoleUtils.waitMessage();
    }

    private void searchTreatments() {
        ConsoleUtils.printHeader("Search Treatments");
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
                MedicalTreatment treatment = treatmentControl.findTreatmentById(treatmentId);
                if (treatment != null) {
                    displayTreatmentDetails(treatment);
                } else {
                    System.out.println("Treatment not found with ID: " + treatmentId);
                }
                ConsoleUtils.waitMessage();
                break;
            case 2:
                String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
                ArrayBucketList<String, MedicalTreatment> patientTreatments = treatmentControl.findTreatmentsByPatient(patientId);
                if (patientTreatments.getSize() > 0) {
                    displayTreatmentList(patientTreatments, "Treatments for Patient ID: " + patientId);
                } else {
                    System.out.println("No treatments found for Patient ID: " + patientId);
                }
                ConsoleUtils.waitMessage();
                break;
            case 3:
                String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
                ArrayBucketList<String, MedicalTreatment> doctorTreatments = treatmentControl.findTreatmentsByDoctor(doctorId);
                if (doctorTreatments.getSize() > 0) {
                    displayTreatmentList(doctorTreatments, "Treatments by Doctor ID: " + doctorId);
                } else {
                    System.out.println("No treatments found for Doctor ID: " + doctorId);
                }
                ConsoleUtils.waitMessage();
                break;
            case 4:
                String consultationIdSearch = ConsoleUtils.getStringInput(scanner, "Enter Consultation ID: ");
                ArrayBucketList<String, MedicalTreatment> consultationTreatments = treatmentControl.findTreatmentsByConsultationId(consultationIdSearch);
                if (consultationTreatments.getSize() > 0) {
                    displayTreatmentList(consultationTreatments, "Treatments for Consultation ID: " + consultationIdSearch);
                } else {
                    System.out.println("No treatments found for Consultation ID: " + consultationIdSearch);
                }
                ConsoleUtils.waitMessage();
                break;
            case 5:
                System.out.println("Select status:");
                System.out.println("1. PRESCRIBED  2. IN_PROGRESS  3. COMPLETED  4. CANCELLED");
                int statusChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
                MedicalTreatment.TreatmentStatus status = MedicalTreatment.TreatmentStatus.values()[statusChoice - 1];
                
                var statusTreatments = new ArrayBucketList<String, MedicalTreatment>();
                Iterator<MedicalTreatment> allTreatments = treatmentControl.getAllTreatments().iterator();
                while (allTreatments.hasNext()) {
                    MedicalTreatment t = allTreatments.next();
                    if (t.getStatus() == status) {
                        statusTreatments.add(t.getTreatmentId(), t);
                    }
                }
                
                if (statusTreatments.getSize() > 0) {
                    displayTreatmentList(statusTreatments, "Treatments with Status: " + status);
                } else {
                    System.out.println("No treatments found with status: " + status);
                }
                ConsoleUtils.waitMessage();
                break;
            case 6:
                LocalDate startDate = ConsoleUtils.getDateInput(scanner, "Enter start date (DD-MM-YYYY): ", DateType.NO_RESTRICTION);
                LocalDate endDate = ConsoleUtils.getDateInput(scanner, "Enter end date (DD-MM-YYYY): ", DateType.NO_RESTRICTION);
                if (endDate.isBefore(startDate)) {
                    // Swap to ensure valid range
                    LocalDate temp = startDate;
                    startDate = endDate;
                    endDate = temp;
                }
                ArrayBucketList<String, MedicalTreatment> rangeTreatments = treatmentControl.findTreatmentsByDateRange(startDate, endDate);
                if (rangeTreatments.getSize() > 0) {
                    displayTreatmentList(rangeTreatments, "Treatments from " + startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) + " to " + endDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
                } else {
                    System.out.println("No treatments found in the given date range.");
                }
                ConsoleUtils.waitMessage();
                break;
            default:
                System.out.println("Invalid choice.");
        }
    }


    private void generateTreatmentReports() {
        System.out.println("\n=== Generate Treatment Reports ===");
        System.out.println("1. Treatment Analysis Report");
        System.out.println("2. Treatment Status Report");
        System.out.println("3. Both Reports");
        System.out.println("4. Back to Medical Treatment");
        System.out.print("Enter choice: ");
        
        int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);
        
        switch (choice) {
            case 1:
                generateTreatmentAnalysisReport();
                break;

            case 2:
                generateTreatmentStatusReport();
                break;

            case 3:
                generateTreatmentAnalysisReport();
                generateTreatmentStatusReport();
                break;

            case 4:
                return;
                
            default:
                System.out.println("Invalid choice.");
        }
    }

    private void generateTreatmentAnalysisReport() {
        ConsoleUtils.printHeader("Treatment Analysis Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Treatment ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Diagnosis");
        System.out.println("5. Status");
        System.out.println("6. Treatment Cost");
        System.out.println("7. Treatment Date");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        String sortBy = getTreatmentSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(treatmentControl.generateTreatmentAnalysisReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private void generateTreatmentStatusReport() {
        ConsoleUtils.printHeader("Treatment Status Report");
        
        System.out.println("Select field to sort by:");
        System.out.println("1. Treatment ID");
        System.out.println("2. Patient Name");
        System.out.println("3. Doctor Name");
        System.out.println("4. Diagnosis");
        System.out.println("5. Status");
        System.out.println("6. Treatment Cost");
        System.out.println("7. Treatment Date");
        
        int sortFieldChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 7);
        
        System.out.println("Select sort order:");
        System.out.println("1. Ascending (A-Z, Low to High)");
        System.out.println("2. Descending (Z-A, High to Low)");
        
        int sortOrderChoice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 2);
        
        String sortBy = getTreatmentSortField(sortFieldChoice);
        String sortOrder = sortOrderChoice == 1 ? "asc" : "desc";
        
        System.out.println(treatmentControl.generateTreatmentStatusReport(sortBy, sortOrder));
        ConsoleUtils.waitMessage();
    }

    private String getTreatmentSortField(int choice) {
        switch (choice) {
            case 1: return "id";
            case 2: return "patient";
            case 3: return "doctor";
            case 4: return "diagnosis";
            case 5: return "status";
            case 6: return "cost";
            case 7: return "date";
            default: return "id";
        }
    }

    private void displayTreatmentDetails(MedicalTreatment treatment) {
        System.out.println("\n=== TREATMENT DETAILS ===");
        System.out.println("Treatment ID: " + treatment.getTreatmentId());
        System.out.println("Patient: " + treatment.getPatient().getFullName() + " (" + treatment.getPatient().getPatientId() + ")");
        System.out.println("Doctor: " + treatment.getDoctor().getFullName() + " (" + treatment.getDoctor().getDoctorId() + ")");
        System.out.println("Diagnosis: " + treatment.getDiagnosis());
        System.out.println("Treatment Plan: " + treatment.getTreatmentPlan());
        System.out.println("Medications: " + treatment.getPrescribedMedications());
        System.out.println("Notes: " + treatment.getTreatmentNotes());
        System.out.println("Treatment Date: " + treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        System.out.println("Status: " + treatment.getStatus());
        System.out.println("Cost: RM" + treatment.getTreatmentCost());
        if (treatment.getFollowUpDate() != null) {
            System.out.println("Follow-up Date: " + treatment.getFollowUpDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        }
    }

    private void displayTreatmentList(ArrayBucketList<String, MedicalTreatment> treatments, String title) {
        System.out.println("\n=== " + title + " ===");
        System.out.println("Total treatments: " + treatments.getSize());
        System.out.println("----------------------------------------");
        
        Iterator<MedicalTreatment> iterator = treatments.iterator();
        while (iterator.hasNext()) {
            MedicalTreatment treatment = iterator.next();
            System.out.println("ID: " + treatment.getTreatmentId());
            System.out.println("Patient: " + treatment.getPatient().getFullName());
            System.out.println("Doctor: " + treatment.getDoctor().getFullName());
            System.out.println("Diagnosis: " + treatment.getDiagnosis());
            System.out.println("Status: " + treatment.getStatus());
            System.out.println("Cost: RM" + treatment.getTreatmentCost());
            System.out.println("----------------------------------------");
        }
    }
    
    private void displayCompletedConsultations(ArrayBucketList<String, Consultation> consultations) {
        System.out.println("Total completed consultations: " + consultations.getSize());
        System.out.println("----------------------------------------");
        
        // Copy to array for deterministic sorting and iteration
        int size = consultations.getSize();
        Consultation[] consultationArray = new Consultation[size];
        Iterator<Consultation> iterator = consultations.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            consultationArray[index++] = iterator.next();
        }
        
        // Sort by date (most recent first)
        sortConsultationsByDate(consultationArray);
        
        // Display sorted consultations from array
        for (int i = 0; i < consultationArray.length; i++) {
            Consultation consultation = consultationArray[i];
            System.out.println("Consultation ID: " + consultation.getConsultationId());
            System.out.println("Doctor ID: " + consultation.getDoctor().getDoctorId());
            System.out.println("Patient ID: " + consultation.getPatient().getPatientId());
            System.out.println("Patient Name: " + consultation.getPatient().getFullName());
            System.out.println("Doctor Name: " + consultation.getDoctor().getFullName());
            System.out.println("Date: " + consultation.getConsultationDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
            System.out.println("----------------------------------------");
        }
    }
    
    /**
     * Sort consultations by date (most recent first) using bubble sort
     */
    private void sortConsultationsByDate(Consultation[] consultationArray) {
        int size = consultationArray.length;
        if (size <= 1) return;
        // Bubble sort by date (most recent first)
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - i - 1; j++) {
                if (consultationArray[j].getConsultationDate().isBefore(consultationArray[j + 1].getConsultationDate())) {
                    Consultation temp = consultationArray[j];
                    consultationArray[j] = consultationArray[j + 1];
                    consultationArray[j + 1] = temp;
                }
            }
        }
    }
    

} 
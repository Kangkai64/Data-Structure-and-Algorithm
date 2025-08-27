package boundary;

import control.MedicalTreatmentControl;
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
    private ConsultationManagementControl consultationControl;
    private MedicalTreatmentControl treatmentControl;

    public MedicalTreatmentUI() {
        this.scanner = new Scanner(System.in);
        this.treatmentControl = new MedicalTreatmentControl();
        this.consultationControl = new ConsultationManagementControl();
    }

    

    public void displayTreatmentManagementMenu() {
        while (true) {
            consultationControl.loadConsultationData();
            treatmentControl.loadTreatmentData();
            ConsoleUtils.printHeader("MEDICAL TREATMENT MANAGEMENT MODULE");
            System.out.println("1. Create Treatment");
            System.out.println("2. Update Treatment");
            System.out.println("3. Start Treatment");
            System.out.println("4. Complete Treatment");
            System.out.println("5. Cancel Treatment");
            System.out.println("6. Search Treatments");
            System.out.println("7. Generate Treatment Reports");
            System.out.println("8. Back to Main Menu");
            
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
        
        // Get treatment ID and validate
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        MedicalTreatment treatment = treatmentControl.findTreatmentById(treatmentId);
        
        if (treatment == null) {
            System.out.println("Error: Treatment not found with ID: " + treatmentId);
            ConsoleUtils.waitMessage();
            return;
        }
        
        // Display current details
        System.out.println("Current Treatment Details:");
        System.out.println("Patient: " + treatment.getPatient().getFullName());
        System.out.println("Doctor: " + treatment.getDoctor().getFullName());
        System.out.println("Diagnosis: " + treatment.getDiagnosis());
        System.out.println("Status: " + treatment.getStatus());
        System.out.println("Treatment Date: " + treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")));
        
        // Validate if treatment can be updated
        if (!treatmentControl.canUpdateTreatment(treatmentId)) {
            System.out.println("Error: Only treatments in PRESCRIBED or COMPLETED status can be updated.");
            ConsoleUtils.waitMessage();
            return;
        }
        
        // Initialize update data
        UpdateData updateData = new UpdateData(treatment);
        boolean isCompleted = !treatmentControl.canUpdateTreatmentFully(treatmentId);
        
        // Main update loop
        while (true) {
            if (!showUpdateMenu(updateData, isCompleted)) {
                return; // User cancelled
            }
            
            // Attempt to save changes using control layer validation
            if (attemptUpdateWithValidation(treatmentId, updateData)) {
                break; // Success
            }
            
            // Ask if user wants to retry
            if (!ConsoleUtils.getBooleanInput(scanner, "Do you want to try updating with different values? (Y/N): ")) {
                System.out.println("Update cancelled.");
                return;
            }
            
            // Reset for retry
            updateData.resetToOriginal(treatment);
        }
        
        ConsoleUtils.waitMessage();
    }
    
    private boolean showUpdateMenu(UpdateData updateData, boolean isCompleted) {
        while (true) {
            System.out.println("\n=== UPDATE OPTIONS ===");

            if (isCompleted) {
                System.out.println("Note: Completed treatments can only update notes and follow-up date.");
                System.out.println("1. Update Notes");
                System.out.println("2. Update Follow-up Date");
                System.out.println("3. Confirm and Save Changes");
                System.out.println("4. Cancel Update");

                int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 4);

                switch (choice) {
                    case 1:
                        System.out.println("Original Notes: " + updateData.originalNotes);
                        updateData.notes = ConsoleUtils.getStringInput(scanner, "New notes (leave blank to keep original): ", updateData.originalNotes);
                        if (updateData.notes.isEmpty()) updateData.notes = updateData.originalNotes;
                        System.out.println("✓ Notes updated to: " + updateData.notes);
                        break;
                    case 2:
                        System.out.println("Original Follow-up Date: " + 
                            (updateData.originalFollowUpDate != null ? updateData.originalFollowUpDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "None"));
                        updateData.followUpDate = ConsoleUtils.getDateInput(scanner, "New follow-up date (DD-MM-YYYY, press Enter to keep): ", 
                            DateType.FUTURE_DATE_ONLY, updateData.originalFollowUpDate);
                        if (updateData.followUpDate == null) updateData.followUpDate = updateData.originalFollowUpDate;
                        System.out.println("✓ Follow-up date updated to: " + 
                            (updateData.followUpDate != null ? updateData.followUpDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "None"));
                        break;
                    case 3:
                        return true; // Continue to save
                    case 4:
                        System.out.println("Update cancelled.");
                        return false; // Cancel
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } else {
                // Full update options for PRESCRIBED treatments
                System.out.println("1. Update Diagnosis");
                System.out.println("2. Update Treatment Plan");
                System.out.println("3. Update Medications");
                System.out.println("4. Update Cost");
                System.out.println("5. Update Notes");
                System.out.println("6. Update Follow-up Date");
                System.out.println("7. Confirm and Save Changes");
                System.out.println("8. Cancel Update");

                int choice = ConsoleUtils.getIntInput(scanner, "Enter your choice: ", 1, 8);

                switch (choice) {
                    case 1:
                        System.out.println("Original Diagnosis: " + updateData.originalDiagnosis);
                        updateData.diagnosis = ConsoleUtils.getStringInput(scanner, "New diagnosis (leave blank to keep original): ", updateData.originalDiagnosis);
                        if (updateData.diagnosis.isEmpty()) updateData.diagnosis = updateData.originalDiagnosis;
                        System.out.println("✓ Diagnosis updated to: " + updateData.diagnosis);
                        break;
                    case 2:
                        System.out.println("Original Treatment Plan: " + updateData.originalTreatmentPlan);
                        updateData.treatmentPlan = ConsoleUtils.getStringInput(scanner, "New treatment plan (leave blank to keep original): ", updateData.originalTreatmentPlan);
                        if (updateData.treatmentPlan.isEmpty()) updateData.treatmentPlan = updateData.originalTreatmentPlan;
                        System.out.println("✓ Treatment plan updated to: " + updateData.treatmentPlan);
                        break;
                    case 3:
                        System.out.println("Original Medications: " + updateData.originalMedications);
                        updateData.medications = ConsoleUtils.getStringInput(scanner, "New medications (leave blank to keep original): ", updateData.originalMedications);
                        if (updateData.medications.isEmpty()) updateData.medications = updateData.originalMedications;
                        System.out.println("✓ Medications updated to: " + updateData.medications);
                        break;
                    case 4:
                        System.out.println("Original Cost: RM" + updateData.originalCost);
                        double newCost = ConsoleUtils.getDoubleInput(scanner, "New cost (enter -1 to keep original): ", -1, 100000.0);
                        updateData.cost = (newCost == -1) ? updateData.originalCost : newCost;
                        System.out.println("✓ Cost updated to: RM" + updateData.cost);
                        break;
                    case 5:
                        System.out.println("Original Notes: " + updateData.originalNotes);
                        updateData.notes = ConsoleUtils.getStringInput(scanner, "New notes (leave blank to keep original): ", updateData.originalNotes);
                        if (updateData.notes.isEmpty()) updateData.notes = updateData.originalNotes;
                        System.out.println("✓ Notes updated to: " + updateData.notes);
                        break;
                    case 6:
                        System.out.println("Original Follow-up Date: " + 
                            (updateData.originalFollowUpDate != null ? updateData.originalFollowUpDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "None"));
                        updateData.followUpDate = ConsoleUtils.getDateInput(scanner, "New follow-up date (DD-MM-YYYY, press Enter to keep): ", 
                            DateType.FUTURE_DATE_ONLY, updateData.originalFollowUpDate);
                        if (updateData.followUpDate == null) updateData.followUpDate = updateData.originalFollowUpDate;
                        System.out.println("✓ Follow-up date updated to: " + 
                            (updateData.followUpDate != null ? updateData.followUpDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : "None"));
                        break;
                    case 7:
                        return true; // Continue to save
                    case 8:
                        System.out.println("Update cancelled.");
                        return false; // Cancel
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }
    
    private boolean attemptUpdateWithValidation(String treatmentId, UpdateData updateData) {
        // Final confirmation
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to update this treatment? (Y/N): ");
        if (!confirm) {
            System.out.println("Action cancelled. Treatment was not updated.");
            return false;
        }
        
        // Convert LocalDate to LocalDateTime for the control layer
        LocalDateTime followUpDateTime = updateData.followUpDate != null ? updateData.followUpDate.atStartOfDay() : null;
        
        // Attempt update using control layer validation
        boolean success = treatmentControl.updateTreatmentWithValidation(treatmentId, updateData.diagnosis, 
            updateData.treatmentPlan, updateData.medications, updateData.notes, followUpDateTime, updateData.cost);
        
        if (success) {
            System.out.println("✓ Treatment updated successfully!");
            return true;
        } else {
            System.out.println("No changes detected or update failed.");
            return false;
        }
    }
    
    // Helper class to manage update data
    private static class UpdateData {
        String diagnosis, treatmentPlan, medications, notes;
        double cost;
        LocalDate followUpDate;
        
        // Original values for reset
        String originalDiagnosis, originalTreatmentPlan, originalMedications, originalNotes;
        double originalCost;
        LocalDate originalFollowUpDate;
        
        UpdateData(MedicalTreatment treatment) {
            // Current values
            this.diagnosis = treatment.getDiagnosis();
            this.treatmentPlan = treatment.getTreatmentPlan();
            this.medications = treatment.getPrescribedMedications();
            this.notes = treatment.getTreatmentNotes();
            this.cost = treatment.getTreatmentCost();
            this.followUpDate = treatment.getFollowUpDate() != null ? treatment.getFollowUpDate().toLocalDate() : null;
            
            // Store original values
            this.originalDiagnosis = treatment.getDiagnosis();
            this.originalTreatmentPlan = treatment.getTreatmentPlan();
            this.originalMedications = treatment.getPrescribedMedications();
            this.originalNotes = treatment.getTreatmentNotes();
            this.originalCost = treatment.getTreatmentCost();
            this.originalFollowUpDate = treatment.getFollowUpDate() != null ? treatment.getFollowUpDate().toLocalDate() : null;
        }
        
        void resetToOriginal(MedicalTreatment treatment) {
            this.diagnosis = originalDiagnosis;
            this.treatmentPlan = originalTreatmentPlan;
            this.medications = originalMedications;
            this.notes = originalNotes;
            this.cost = originalCost;
            this.followUpDate = originalFollowUpDate;
        }
    }
    

    
    private void startTreatment() {
        ConsoleUtils.printHeader("Start Treatment");
        String treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
        
        MedicalTreatment treatment = treatmentControl.findTreatmentById(treatmentId);
        if (treatment == null) {
            System.out.println("Error: Treatment not found with ID: " + treatmentId);
            ConsoleUtils.waitMessage();
            return;
        }
        
        // Use control layer validation
        if (!treatmentControl.canStartTreatment(treatmentId)) {
            System.out.println("Error: Treatment cannot be started. Current status: " + treatment.getStatus());
            ConsoleUtils.waitMessage();
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
            ConsoleUtils.waitMessage();
            return;
        }
        
        // Use control layer validation
        if (!treatmentControl.canCompleteTreatment(treatmentId)) {
            System.out.println("Error: Treatment cannot be completed. Current status: " + treatment.getStatus());
            ConsoleUtils.waitMessage();
            return;
        }
        
        // Prompt for follow-up date, allow skipping
        
        LocalDate followUpDate = ConsoleUtils.getDateInput(scanner, "Enter follow-up date (DD-MM-YYYY, press Enter to skip): ", DateType.FUTURE_DATE_ONLY, null);
       
        
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to complete this treatment? (Y/N): ");
        if (!confirm) {
            System.out.println("Action cancelled. Treatment was not completed.");
            return;
        }
        
        // Complete treatment and persist optional follow-up date
        boolean success = treatmentControl.completeTreatment(treatmentId, followUpDate != null ? followUpDate.atStartOfDay() : null);
        
        if (success) {
            System.out.println("✓ Treatment completed successfully!");
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

        // Use control layer validation
        if (!treatmentControl.canCancelTreatment(treatmentId)) {
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
        ConsoleUtils.printHeader("Generate Treatment Reports");
        System.out.println("1. Treatment Analysis Report");
        System.out.println("2. Treatment Status Report");
        System.out.println("3. Both Reports");
        System.out.println("4. Back to Medical Treatment");
        
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
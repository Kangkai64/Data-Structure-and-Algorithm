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

    private String getOptionalString(Scanner scanner, String prompt, String defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            return input;
        }
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

        Patient patient = null;
        String patientId = null;
        while (true) {
            patientId = ConsoleUtils.getStringInput(scanner, "Enter patient ID: ");
            patient = patientControl.findPatientById(patientId);
            if (patient == null) {
                System.out.println("Error: Patient not found with ID: " + patientId);
                continue;
            }
            break;
        }
   
        Doctor doctor = null;
        String doctorId = null;
        while (true) {
            doctorId = ConsoleUtils.getStringInput(scanner, "Enter doctor ID: ");
            doctor = doctorControl.findDoctorById(doctorId);
            if (doctor == null) {
                System.out.println("Error: Doctor not found with ID: " + doctorId);
                continue;
            }
            break;
        }
        
        Consultation consultation = null;
        String consultationId = null;
        while (true) {
            consultationId = ConsoleUtils.getStringInput(scanner, "Enter consultation ID: ");
            consultation = consultationControl.findConsultationById(consultationId);
            
            if (consultation == null) {
                System.out.println("Error: Consultation not found with ID: " + consultationId);
                continue;
            }
            if (!consultation.getPatient().getPatientId().equals(patientId) ||
                !consultation.getDoctor().getDoctorId().equals(doctorId)) {
                System.out.println("Error: Consultation does not belong to the specified patient/doctor.");
                continue;
            }
            if (consultation.getStatus() != Consultation.ConsultationStatus.COMPLETED) {
                System.out.println("Error: Consultation must be COMPLETED before creating treatment.");
                continue;
            }
            if (treatmentControl.hasTreatmentForConsultation(consultationId)) {
                System.out.println("Error: A treatment already exists for this consultation.");
                continue;
            }
            break;
        }

        String diagnosis = ConsoleUtils.getStringInput(scanner, "Enter diagnosis: ");
        String treatmentPlan = ConsoleUtils.getStringInput(scanner, "Enter treatment plan: ");
        String medications = ConsoleUtils.getStringInput(scanner, "Enter prescribed medications: ");
        String notes = ConsoleUtils.getStringInput(scanner, "Enter treatment notes: ");
        double cost = ConsoleUtils.getDoubleInput(scanner, "Enter treatment cost: ", 0.0, 100000.0);
        
        // Preview and confirmation
        System.out.println("\nPlease confirm the treatment details:");
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
        System.out.println("\n=== UPDATE TREATMENT ===");
        MedicalTreatment treatment = null;
        String treatmentId = null;
        while (true) {
            treatmentId = ConsoleUtils.getStringInput(scanner, "Enter treatment ID: ");
            treatment = treatmentControl.findTreatmentById(treatmentId);
            if (treatment == null) {
                System.out.println("Error: Treatment not found with ID: " + treatmentId);
                continue;
            }
            break;
        }
        
        System.out.println("Current Treatment Details:");
        System.out.println("Patient: " + treatment.getPatient().getFullName());
        System.out.println("Doctor: " + treatment.getDoctor().getFullName());
        System.out.println("Diagnosis: " + treatment.getDiagnosis());
        System.out.println("Status: " + treatment.getStatus());
        if (treatment.getStatus() != MedicalTreatment.TreatmentStatus.PRESCRIBED) {
            System.out.println("Error: Only treatments in PRESCRIBED status can be updated.");
            return;
        }
        
        System.out.println("\nEnter new details (press Enter to keep current value):");
        
        String diagnosis = getOptionalString(scanner, "New diagnosis [" + treatment.getDiagnosis() + "]: ", treatment.getDiagnosis());
        
        String treatmentPlan = getOptionalString(scanner, "New treatment plan [" + treatment.getTreatmentPlan() + "]: ", treatment.getTreatmentPlan());
        
        String medications = getOptionalString(scanner, "New medications [" + treatment.getPrescribedMedications() + "]: ", treatment.getPrescribedMedications());
        
        String notes = getOptionalString(scanner, "New notes [" + treatment.getTreatmentNotes() + "]: ", treatment.getTreatmentNotes());
        
        LocalDate existingFollowUp = treatment.getFollowUpDate() != null ? treatment.getFollowUpDate().toLocalDate() : null;
        LocalDate followUpDate = ConsoleUtils.getDateInput(scanner, "New follow-up date (DD-MM-YYYY, press Enter to keep): ", DateType.FUTURE_DATE_ONLY, existingFollowUp);
        
        double cost = ConsoleUtils.getDoubleInput(scanner, "New cost [" + treatment.getTreatmentCost() + "]: ", treatment.getTreatmentCost());
        // Convert LocalDate to LocalDateTime for the control layer
        LocalDateTime followUpDateTime = followUpDate != null ? followUpDate.atStartOfDay() : null;
        
        boolean confirm = ConsoleUtils.getBooleanInput(scanner, "\nAre you sure you want to add this treatment? (Y/N): ");
        if (!confirm) {
            System.out.println("Action cancelled. Treatment was not created.");
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
        System.out.println("\n=== START TREATMENT ===");
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
        
        boolean success = treatmentControl.startTreatment(treatmentId);
        
        if (success) {
            System.out.println("✓ Treatment started successfully!");
            System.out.println("Treatment ID: " + treatmentId);
            System.out.println("Status changed to: IN_PROGRESS");
        } else {
            System.out.println("✗ Failed to start treatment. Please try again.");
        }
        ConsoleUtils.waitMessage();
    }


    private void completeTreatment() {
        System.out.println("\n=== COMPLETE TREATMENT ===");
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
        
        // Complete treatment
        boolean success = treatmentControl.completeTreatment(treatmentId);
        
        if (success) {
            System.out.println("✓ Treatment completed successfully!");
            System.out.println("Treatment ID: " + treatmentId);
            System.out.println("Status changed to: COMPLETED");
            if (followUpDate != null) {
                System.out.println("Follow-up Date: " + followUpDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
            }
        } else {
            System.out.println("✗ Failed to complete treatment. Please try again.");
        }
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
                MedicalTreatment treatment = treatmentControl.findTreatmentById(treatmentId);
                if (treatment != null) {
                    displayTreatmentDetails(treatment);
                } else {
                    System.out.println("Treatment not found with ID: " + treatmentId);
                }
                break;
            case 2:
                String patientId = ConsoleUtils.getStringInput(scanner, "Enter Patient ID: ");
                ArrayBucketList<String, MedicalTreatment> patientTreatments = treatmentControl.findTreatmentsByPatient(patientId);
                if (patientTreatments.getSize() > 0) {
                    displayTreatmentList(patientTreatments, "Treatments for Patient ID: " + patientId);
                } else {
                    System.out.println("No treatments found for Patient ID: " + patientId);
                }
                break;
            case 3:
                String doctorId = ConsoleUtils.getStringInput(scanner, "Enter Doctor ID: ");
                ArrayBucketList<String, MedicalTreatment> doctorTreatments = treatmentControl.findTreatmentsByDoctor(doctorId);
                if (doctorTreatments.getSize() > 0) {
                    displayTreatmentList(doctorTreatments, "Treatments by Doctor ID: " + doctorId);
                } else {
                    System.out.println("No treatments found for Doctor ID: " + doctorId);
                }
                break;
            case 4:
                ConsoleUtils.getStringInput(scanner, "Enter Consultation ID: ");
                // This would need to be implemented in the control layer
                System.out.println("Search by Consultation ID - Implementation needed");
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
                break;
            case 6:
                ConsoleUtils.getStringInput(scanner, "Enter start date (DD-MM-YYYY): ");
                ConsoleUtils.getStringInput(scanner, "Enter end date (DD-MM-YYYY): ");
                // This would need to be implemented in the control layer
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
    

} 
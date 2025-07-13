package control;

import adt.ArrayList;
import entity.MedicalTreatment;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import java.util.Date;

/**
 * Medical Treatment Control - Module 4
 * Manages patient diagnosis and maintain treatment history records
 */
public class MedicalTreatmentControl {
    
    private ArrayList<MedicalTreatment> treatments;
    private ArrayList<MedicalTreatment> activeTreatments;
    
    public MedicalTreatmentControl() {
        this.treatments = new ArrayList<>();
        this.activeTreatments = new ArrayList<>();
    }
    
    // Treatment Management Methods
    public boolean createTreatment(Patient patient, Doctor doctor, Consultation consultation,
                                 String diagnosis, String treatmentPlan, double treatmentCost) {
        try {
            // Generate treatment ID
            String treatmentId = generateTreatmentId();
            
            // Create new treatment
            MedicalTreatment treatment = new MedicalTreatment(treatmentId, patient, doctor, 
                                                            consultation, diagnosis, treatmentPlan, new Date(), treatmentCost);
            
            // Add to lists
            treatments.add(treatment);
            activeTreatments.add(treatment);
            
            return true;
        } catch (Exception exception) {
            System.err.println("Error creating treatment: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean updateTreatment(String treatmentId, String diagnosis, String treatmentPlan,
                                 String prescribedMedications, String treatmentNotes, 
                                 Date followUpDate, double treatmentCost) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null) {
                treatment.setDiagnosis(diagnosis);
                treatment.setTreatmentPlan(treatmentPlan);
                treatment.setPrescribedMedications(prescribedMedications);
                treatment.setTreatmentNotes(treatmentNotes);
                treatment.setFollowUpDate(followUpDate);
                treatment.setTreatmentCost(treatmentCost);
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating treatment: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean completeTreatment(String treatmentId) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null && treatment.getStatus() == MedicalTreatment.TreatmentStatus.IN_PROGRESS) {
                treatment.setStatus(MedicalTreatment.TreatmentStatus.COMPLETED);
                
                // Remove from active treatments
                removeFromActiveTreatments(treatment);
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error completing treatment: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean startTreatment(String treatmentId) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null && treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED) {
                treatment.setStatus(MedicalTreatment.TreatmentStatus.IN_PROGRESS);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error starting treatment: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean cancelTreatment(String treatmentId) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null && treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED) {
                treatment.setStatus(MedicalTreatment.TreatmentStatus.CANCELLED);
                
                // Remove from active treatments
                removeFromActiveTreatments(treatment);
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error cancelling treatment: " + exception.getMessage());
            return false;
        }
    }
    
    // Search and Retrieval Methods
    public MedicalTreatment findTreatmentById(String treatmentId) {
        for (int index = 1; index <= treatments.getNumberOfEntries(); index++) {
            MedicalTreatment treatment = treatments.getEntry(index);
            if (treatment.getTreatmentId().equals(treatmentId)) {
                return treatment;
            }
        }
        return null;
    }
    
    public ArrayList<MedicalTreatment> findTreatmentsByPatient(String patientId) {
        ArrayList<MedicalTreatment> patientTreatments = new ArrayList<>();
        for (int index = 1; index <= treatments.getNumberOfEntries(); index++) {
            MedicalTreatment treatment = treatments.getEntry(index);
            if (treatment.getPatient().getPatientId().equals(patientId)) {
                patientTreatments.add(treatment);
            }
        }
        return patientTreatments;
    }
    
    public ArrayList<MedicalTreatment> findTreatmentsByDoctor(String doctorId) {
        ArrayList<MedicalTreatment> doctorTreatments = new ArrayList<>();
        for (int index = 1; index <= treatments.getNumberOfEntries(); index++) {
            MedicalTreatment treatment = treatments.getEntry(index);
            if (treatment.getDoctor().getDoctorId().equals(doctorId)) {
                doctorTreatments.add(treatment);
            }
        }
        return doctorTreatments;
    }
    
    public ArrayList<MedicalTreatment> findTreatmentsByDiagnosis(String diagnosis) {
        ArrayList<MedicalTreatment> diagnosisTreatments = new ArrayList<>();
        for (int index = 1; index <= treatments.getNumberOfEntries(); index++) {
            MedicalTreatment treatment = treatments.getEntry(index);
            if (treatment.getDiagnosis().toLowerCase().contains(diagnosis.toLowerCase())) {
                diagnosisTreatments.add(treatment);
            }
        }
        return diagnosisTreatments;
    }
    
    public ArrayList<MedicalTreatment> getActiveTreatments() {
        return activeTreatments;
    }
    
    public ArrayList<MedicalTreatment> getCompletedTreatments() {
        ArrayList<MedicalTreatment> completedTreatments = new ArrayList<>();
        for (int index = 1; index <= treatments.getNumberOfEntries(); index++) {
            MedicalTreatment treatment = treatments.getEntry(index);
            if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.COMPLETED) {
                completedTreatments.add(treatment);
            }
        }
        return completedTreatments;
    }
    
    public ArrayList<MedicalTreatment> getAllTreatments() {
        return treatments;
    }
    
    public int getTotalTreatments() {
        return treatments.getNumberOfEntries();
    }
    
    public int getActiveTreatmentsCount() {
        return activeTreatments.getNumberOfEntries();
    }
    
    // Reporting Methods
    public String generateTreatmentReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== MEDICAL TREATMENT REPORT ===\n");
        report.append("Total Treatments: ").append(getTotalTreatments()).append("\n");
        report.append("Active Treatments: ").append(getActiveTreatmentsCount()).append("\n");
        report.append("Completed Treatments: ").append(getCompletedTreatments().getNumberOfEntries()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= treatments.getNumberOfEntries(); index++) {
            MedicalTreatment treatment = treatments.getEntry(index);
            report.append("Treatment ID: ").append(treatment.getTreatmentId()).append("\n");
            report.append("Patient: ").append(treatment.getPatient().getFullName()).append("\n");
            report.append("Doctor: ").append(treatment.getDoctor().getFullName()).append("\n");
            report.append("Diagnosis: ").append(treatment.getDiagnosis()).append("\n");
            report.append("Treatment Date: ").append(treatment.getTreatmentDate()).append("\n");
            report.append("Status: ").append(treatment.getStatus()).append("\n");
            report.append("Cost: RM").append(treatment.getTreatmentCost()).append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public String generateTreatmentHistoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== TREATMENT HISTORY REPORT ===\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        // Group by patient
        ArrayList<String> processedPatients = new ArrayList<>();
        
        for (int index = 1; index <= treatments.getNumberOfEntries(); index++) {
            MedicalTreatment treatment = treatments.getEntry(index);
            String patientId = treatment.getPatient().getPatientId();
            
            if (!processedPatients.contains(patientId)) {
                processedPatients.add(patientId);
                
                report.append("Patient: ").append(treatment.getPatient().getFullName())
                      .append(" (").append(patientId).append(")\n");
                
                // Get all treatments for this patient
                ArrayList<MedicalTreatment> patientTreatments = findTreatmentsByPatient(patientId);
                for (int treatmentIndex = 1; treatmentIndex <= patientTreatments.getNumberOfEntries(); treatmentIndex++) {
                    MedicalTreatment patientTreatment = patientTreatments.getEntry(treatmentIndex);
                    report.append("  - Treatment ID: ").append(patientTreatment.getTreatmentId()).append("\n");
                    report.append("    Diagnosis: ").append(patientTreatment.getDiagnosis()).append("\n");
                    report.append("    Date: ").append(patientTreatment.getTreatmentDate()).append("\n");
                    report.append("    Status: ").append(patientTreatment.getStatus()).append("\n");
                }
                report.append("----------------------------------------\n");
            }
        }
        
        return report.toString();
    }
    
    // Private Helper Methods
    private String generateTreatmentId() {
        int nextNumber = getTotalTreatments() + 1;
        return String.format("T%09d", nextNumber);
    }
    
    private void removeFromActiveTreatments(MedicalTreatment treatment) {
        for (int index = 1; index <= activeTreatments.getNumberOfEntries(); index++) {
            MedicalTreatment activeTreatment = activeTreatments.getEntry(index);
            if (activeTreatment.getTreatmentId().equals(treatment.getTreatmentId())) {
                activeTreatments.remove(index);
                break;
            }
        }
    }
} 
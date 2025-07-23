package control;

import adt.ArrayBucketList;
import entity.MedicalTreatment;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import dao.MedicalTreatmentDao;
import java.util.Date;
import java.util.Iterator;

/**
 * @author: Benjamin Yee Jun Yi
 * Medical Treatment Control - Module 4
 * Manages patient diagnosis and maintain treatment history records
 */
public class MedicalTreatmentControl {
    
    private ArrayBucketList<String, MedicalTreatment> treatments;
    private ArrayBucketList<String, MedicalTreatment> activeTreatments;
    private MedicalTreatmentDao treatmentDao;
    
    public MedicalTreatmentControl() {
        this.treatments = new ArrayBucketList<String, MedicalTreatment>();
        this.activeTreatments = new ArrayBucketList<String, MedicalTreatment>();
        this.treatmentDao = new MedicalTreatmentDao();
        loadTreatmentData();
    }
    
    public void loadTreatmentData() {
        try {
            treatments = treatmentDao.findAll();
            Iterator<MedicalTreatment> treatmentIterator = treatments.iterator();
            while (treatmentIterator.hasNext()) {
                MedicalTreatment treatment = treatmentIterator.next();
                if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.IN_PROGRESS) {
                    activeTreatments.add(treatment.getTreatmentId(), treatment);
                }
            }
        } catch (Exception exception) {
            System.err.println("Error loading treatment data: " + exception.getMessage());
        }
    }
    
    // Treatment Management Methods
    public boolean createTreatment(Patient patient, Doctor doctor, Consultation consultation,
                                 String diagnosis, String treatmentPlan, double treatmentCost) {
        try {
            // Get new treatment ID from database
            String treatmentId = treatmentDao.getNewId();
            
            // Create new treatment
            MedicalTreatment treatment = new MedicalTreatment(treatmentId, patient, doctor, 
                                                            consultation, diagnosis, treatmentPlan, new Date(), treatmentCost);
            
            // Add to lists
            treatments.add(treatment.getTreatmentId(), treatment);
            activeTreatments.add(treatment.getTreatmentId(), treatment);
            
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
        return treatments.getValue(treatmentId);
    }
    
    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByPatient(String patientId) {
        ArrayBucketList<String, MedicalTreatment> patientTreatments = new ArrayBucketList<String, MedicalTreatment>();
        Iterator<MedicalTreatment> treatmentIterator = treatments.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            if (treatment.getPatient().getPatientId().equals(patientId)) {
                patientTreatments.add(treatment.getTreatmentId(), treatment);
            }
        }
        return patientTreatments;
    }
    
    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByDoctor(String doctorId) {
        ArrayBucketList<String, MedicalTreatment> doctorTreatments = new ArrayBucketList<String, MedicalTreatment>();
        Iterator<MedicalTreatment> treatmentIterator = treatments.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            if (treatment.getDoctor().getDoctorId().equals(doctorId)) {
                doctorTreatments.add(treatment.getTreatmentId(), treatment);
            }
        }
        return doctorTreatments;
    }
    
    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByDiagnosis(String diagnosis) {
        ArrayBucketList<String, MedicalTreatment> diagnosisTreatments = new ArrayBucketList<String, MedicalTreatment>();
        Iterator<MedicalTreatment> treatmentIterator = treatments.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            if (treatment.getDiagnosis().toLowerCase().contains(diagnosis.toLowerCase())) {
                diagnosisTreatments.add(treatment.getTreatmentId(), treatment);
            }
        }
        return diagnosisTreatments;
    }
    
    public ArrayBucketList<String, MedicalTreatment> getActiveTreatments() {
        return activeTreatments;
    }
    
    public ArrayBucketList<String, MedicalTreatment> getCompletedTreatments() {
        ArrayBucketList<String, MedicalTreatment> completedTreatments = new ArrayBucketList<String, MedicalTreatment>();
        Iterator<MedicalTreatment> treatmentIterator = treatments.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.COMPLETED) {
                completedTreatments.add(treatment.getTreatmentId(), treatment);
            }
        }
        return completedTreatments;
    }
    
    public ArrayBucketList<String, MedicalTreatment> getAllTreatments() {
        return treatments;
    }
    
    public int getTotalTreatments() {
        return treatments.getSize();
    }
    
    public int getActiveTreatmentsCount() {
        return activeTreatments.getSize();
    }
    
    // Reporting Methods
    public String generateTreatmentReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== MEDICAL TREATMENT REPORT ===\n");
        report.append("Total Treatments: ").append(getTotalTreatments()).append("\n");
        report.append("Active Treatments: ").append(getActiveTreatmentsCount()).append("\n");
        report.append("Completed Treatments: ").append(getCompletedTreatments().getSize()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        Iterator<MedicalTreatment> treatmentIterator = treatments.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            report.append("Treatment ID: ").append(treatment.getTreatmentId()).append("\n");
            report.append("Patient: ").append(treatment.getPatient().getFullName()).append("\n");
            report.append("Doctor: ").append(treatment.getDoctor().getFullName()).append("\n");
            report.append("Diagnosis: ").append(treatment.getDiagnosis()).append("\n");
            report.append("Status: ").append(treatment.getStatus()).append("\n");
            report.append("Cost: RM").append(treatment.getTreatmentCost()).append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public String generateTreatmentHistoryReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== TREATMENT HISTORY REPORT ===\n");
        report.append("Total Treatments: ").append(getTotalTreatments()).append("\n");
        report.append("Completed Treatments: ").append(getCompletedTreatments().getSize()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        Iterator<MedicalTreatment> treatmentIterator = treatments.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.COMPLETED) {
                report.append("Treatment ID: ").append(treatment.getTreatmentId()).append("\n");
                report.append("Patient: ").append(treatment.getPatient().getFullName()).append("\n");
                report.append("Doctor: ").append(treatment.getDoctor().getFullName()).append("\n");
                report.append("Diagnosis: ").append(treatment.getDiagnosis()).append("\n");
                report.append("Treatment Plan: ").append(treatment.getTreatmentPlan()).append("\n");
                report.append("Start Date: ").append(treatment.getTreatmentDate()).append("\n");
                report.append("Cost: RM").append(treatment.getTreatmentCost()).append("\n");
                report.append("----------------------------------------\n");
            }
        }
        
        return report.toString();
    }
    
    private void removeFromActiveTreatments(MedicalTreatment treatment) {
        Iterator<MedicalTreatment> activeIterator = activeTreatments.iterator();
        while (activeIterator.hasNext()) {
            MedicalTreatment activeTreatment = activeIterator.next();
            if (activeTreatment.getTreatmentId().equals(treatment.getTreatmentId())) {
                activeTreatments.remove(activeTreatment.getTreatmentId());
                break;
            }
        }
    }
} 
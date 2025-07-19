package control;

import adt.ArrayBucketList;
import entity.Consultation;
import entity.Patient;
import entity.Doctor;
import dao.ConsultationDao;
import java.util.Date;

/**
 * Consultation Management Control - Module 3
 * Manages patient consultations and arrange subsequent visit appointments
 */
public class ConsultationManagementControl {
    
    private ArrayBucketList<Consultation> consultations;
    private ArrayBucketList<Consultation> scheduledConsultations;
    private ConsultationDao consultationDao;
    
    public ConsultationManagementControl() {
        this.consultations = new ArrayBucketList<>();
        this.scheduledConsultations = new ArrayBucketList<>();
        this.consultationDao = new ConsultationDao();
    }
    
    // Consultation Management Methods
    public boolean scheduleConsultation(Patient patient, Doctor doctor, 
                                      Date consultationDate, String symptoms, 
                                      double consultationFee) {
        try {
            // Get new consultation ID from database
            String consultationId = consultationDao.getNewId();
            
            // Create new consultation
            Consultation consultation = new Consultation(consultationId, patient, doctor, 
                                                       consultationDate, symptoms, consultationFee);
            
            // Add to lists
            consultations.add(consultation);
            scheduledConsultations.add(consultation);
            
            // Add to doctor's appointment list
            doctor.addAppointment(null); // We'll need to create an Appointment entity
            
            return true;
        } catch (Exception exception) {
            System.err.println("Error scheduling consultation: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean startConsultation(String consultationId) {
        try {
            Consultation consultation = findConsultationById(consultationId);
            if (consultation != null && consultation.getStatus() == Consultation.ConsultationStatus.SCHEDULED) {
                consultation.setStatus(Consultation.ConsultationStatus.IN_PROGRESS);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error starting consultation: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean completeConsultation(String consultationId, String diagnosis, 
                                      String treatment, String notes, Date nextVisitDate) {
        try {
            Consultation consultation = findConsultationById(consultationId);
            if (consultation != null && consultation.getStatus() == Consultation.ConsultationStatus.IN_PROGRESS) {
                consultation.setDiagnosis(diagnosis);
                consultation.setTreatment(treatment);
                consultation.setNotes(notes);
                consultation.setNextVisitDate(nextVisitDate);
                consultation.setStatus(Consultation.ConsultationStatus.COMPLETED);
                
                // Remove from scheduled consultations
                removeFromScheduledConsultations(consultation);
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error completing consultation: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean cancelConsultation(String consultationId) {
        try {
            Consultation consultation = findConsultationById(consultationId);
            if (consultation != null && consultation.getStatus() == Consultation.ConsultationStatus.SCHEDULED) {
                consultation.setStatus(Consultation.ConsultationStatus.CANCELLED);
                
                // Remove from scheduled consultations
                removeFromScheduledConsultations(consultation);
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error cancelling consultation: " + exception.getMessage());
            return false;
        }
    }
    
    // Search and Retrieval Methods
    public Consultation findConsultationById(String consultationId) {
        for (int index = 1; index <= consultations.getNumberOfEntries(); index++) {
            Consultation consultation = consultations.getEntry(index);
            if (consultation.getConsultationId().equals(consultationId)) {
                return consultation;
            }
        }
        return null;
    }
    
    public ArrayBucketList<Consultation> findConsultationsByPatient(String patientId) {
        ArrayBucketList<Consultation> patientConsultations = new ArrayBucketList<>();
        for (int index = 1; index <= consultations.getNumberOfEntries(); index++) {
            Consultation consultation = consultations.getEntry(index);
            if (consultation.getPatient().getPatientId().equals(patientId)) {
                patientConsultations.add(consultation);
            }
        }
        return patientConsultations;
    }
    
    public ArrayBucketList<Consultation> findConsultationsByDoctor(String doctorId) {
        ArrayBucketList<Consultation> doctorConsultations = new ArrayBucketList<>();
        for (int index = 1; index <= consultations.getNumberOfEntries(); index++) {
            Consultation consultation = consultations.getEntry(index);
            if (consultation.getDoctor().getDoctorId().equals(doctorId)) {
                doctorConsultations.add(consultation);
            }
        }
        return doctorConsultations;
    }
    
    public ArrayBucketList<Consultation> findConsultationsByDate(Date date) {
        ArrayBucketList<Consultation> dateConsultations = new ArrayBucketList<>();
        for (int index = 1; index <= consultations.getNumberOfEntries(); index++) {
            Consultation consultation = consultations.getEntry(index);
            if (consultation.getConsultationDate().equals(date)) {
                dateConsultations.add(consultation);
            }
        }
        return dateConsultations;
    }
    
    public ArrayBucketList<Consultation> getScheduledConsultations() {
        return scheduledConsultations;
    }
    
    public ArrayBucketList<Consultation> getCompletedConsultations() {
        ArrayBucketList<Consultation> completedConsultations = new ArrayBucketList<>();
        for (int index = 1; index <= consultations.getNumberOfEntries(); index++) {
            Consultation consultation = consultations.getEntry(index);
            if (consultation.getStatus() == Consultation.ConsultationStatus.COMPLETED) {
                completedConsultations.add(consultation);
            }
        }
        return completedConsultations;
    }
    
    public ArrayBucketList<Consultation> getAllConsultations() {
        return consultations;
    }
    
    public int getTotalConsultations() {
        return consultations.getNumberOfEntries();
    }
    
    public int getScheduledConsultationsCount() {
        return scheduledConsultations.getNumberOfEntries();
    }
    
    // Reporting Methods
    public String generateConsultationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== CONSULTATION REPORT ===\n");
        report.append("Total Consultations: ").append(getTotalConsultations()).append("\n");
        report.append("Scheduled Consultations: ").append(getScheduledConsultationsCount()).append("\n");
        report.append("Completed Consultations: ").append(getCompletedConsultations().getNumberOfEntries()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= consultations.getNumberOfEntries(); index++) {
            Consultation consultation = consultations.getEntry(index);
            report.append("Consultation ID: ").append(consultation.getConsultationId()).append("\n");
            report.append("Patient: ").append(consultation.getPatient().getFullName()).append("\n");
            report.append("Doctor: ").append(consultation.getDoctor().getFullName()).append("\n");
            report.append("Date: ").append(consultation.getConsultationDate()).append("\n");
            report.append("Status: ").append(consultation.getStatus()).append("\n");
            report.append("Fee: RM").append(consultation.getConsultationFee()).append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public String generateScheduledConsultationsReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== SCHEDULED CONSULTATIONS REPORT ===\n");
        report.append("Total Scheduled: ").append(getScheduledConsultationsCount()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= scheduledConsultations.getNumberOfEntries(); index++) {
            Consultation consultation = scheduledConsultations.getEntry(index);
            report.append("Consultation ID: ").append(consultation.getConsultationId()).append("\n");
            report.append("Patient: ").append(consultation.getPatient().getFullName()).append("\n");
            report.append("Doctor: ").append(consultation.getDoctor().getFullName()).append("\n");
            report.append("Date: ").append(consultation.getConsultationDate()).append("\n");
            report.append("Symptoms: ").append(consultation.getSymptoms()).append("\n");
            report.append("Fee: RM").append(consultation.getConsultationFee()).append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    // Private Helper Methods
    
    private void removeFromScheduledConsultations(Consultation consultation) {
        for (int index = 1; index <= scheduledConsultations.getNumberOfEntries(); index++) {
            Consultation scheduledConsultation = scheduledConsultations.getEntry(index);
            if (scheduledConsultation.getConsultationId().equals(consultation.getConsultationId())) {
                scheduledConsultations.remove(index);
                break;
            }
        }
    }
} 
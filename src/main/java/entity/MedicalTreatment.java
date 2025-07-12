package entity;

import java.util.Date;

public class MedicalTreatment {
    private String treatmentId;
    private Patient patient;
    private Doctor doctor;
    private Consultation consultation;
    private String diagnosis;
    private String treatmentPlan;
    private String prescribedMedications;
    private String treatmentNotes;
    private Date treatmentDate;
    private Date followUpDate;
    private TreatmentStatus status;
    private double treatmentCost;

    public enum TreatmentStatus {
        PRESCRIBED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public MedicalTreatment(String treatmentId, Patient patient, Doctor doctor, 
                           Consultation consultation, String diagnosis, 
                           String treatmentPlan, double treatmentCost) {
        this.treatmentId = treatmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.consultation = consultation;
        this.diagnosis = diagnosis;
        this.treatmentPlan = treatmentPlan;
        this.treatmentCost = treatmentCost;
        this.treatmentDate = new Date();
        this.status = TreatmentStatus.PRESCRIBED;
    }

    // Getters and Setters
    public String getTreatmentId() { return treatmentId; }
    public void setTreatmentId(String treatmentId) { this.treatmentId = treatmentId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public Consultation getConsultation() { return consultation; }
    public void setConsultation(Consultation consultation) { this.consultation = consultation; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public String getPrescribedMedications() { return prescribedMedications; }
    public void setPrescribedMedications(String prescribedMedications) { this.prescribedMedications = prescribedMedications; }

    public String getTreatmentNotes() { return treatmentNotes; }
    public void setTreatmentNotes(String treatmentNotes) { this.treatmentNotes = treatmentNotes; }

    public Date getTreatmentDate() { return treatmentDate; }
    public void setTreatmentDate(Date treatmentDate) { this.treatmentDate = treatmentDate; }

    public Date getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(Date followUpDate) { this.followUpDate = followUpDate; }

    public TreatmentStatus getStatus() { return status; }
    public void setStatus(TreatmentStatus status) { this.status = status; }

    public double getTreatmentCost() { return treatmentCost; }
    public void setTreatmentCost(double treatmentCost) { this.treatmentCost = treatmentCost; }

    @Override
    public String toString() {
        return "MedicalTreatment{" +
                "treatmentId='" + treatmentId + '\'' +
                ", patient=" + patient.getFullName() +
                ", doctor=" + doctor.getFullName() +
                ", diagnosis='" + diagnosis + '\'' +
                ", treatmentDate=" + treatmentDate +
                ", status=" + status +
                ", treatmentCost=" + treatmentCost +
                '}';
    }
} 
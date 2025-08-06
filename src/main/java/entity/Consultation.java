package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Consultation {
    private String consultationId;
    private Patient patient;
    private Doctor doctor;
    private LocalDate consultationDate;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String notes;
    private ConsultationStatus status;
    private LocalDate nextVisitDate;
    private double consultationFee;

    public enum ConsultationStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Consultation(String consultationId, Patient patient, Doctor doctor, 
                       LocalDate consultationDate, String symptoms, double consultationFee) {
        this.consultationId = consultationId;
        this.patient = patient;
        this.doctor = doctor;
        this.consultationDate = consultationDate;
        this.symptoms = symptoms;
        this.consultationFee = consultationFee;
        this.status = ConsultationStatus.SCHEDULED;
    }

    // Getters and Setters
    public String getConsultationId() { return consultationId; }
    public void setConsultationId(String consultationId) { this.consultationId = consultationId; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDate getConsultationDate() { return consultationDate; }
    public void setConsultationDate(LocalDate consultationDate) { this.consultationDate = consultationDate; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = treatment; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public ConsultationStatus getStatus() { return status; }
    public void setStatus(ConsultationStatus status) { this.status = status; }

    public LocalDate getNextVisitDate() { return nextVisitDate; }
    public void setNextVisitDate(LocalDate nextVisitDate) { this.nextVisitDate = nextVisitDate; }

    public double getConsultationFee() { return consultationFee; }
    public void setConsultationFee(double consultationFee) { this.consultationFee = consultationFee; }

    @Override
    public String toString() {
        return "Consultation{" +
                "consultationId='" + consultationId + '\'' +
                ", patient=" + patient.getFullName() +
                ", doctor=" + doctor.getFullName() +
                ", consultationDate=" + consultationDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) +
                ", status=" + status +
                ", consultationFee=" + consultationFee +
                '}';
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(consultationId.replaceAll("[^0-9]", ""));
    }
} 
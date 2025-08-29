package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Consultation {
    private String consultationId;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime consultationDate;
    private String symptoms;
    private String diagnosis;
    private String treatment;
    private String notes;
    private ConsultationStatus status;
    private String cancellationReason;
    private LocalDateTime nextVisitDate;
    private double consultationFee;
    private PaymentStatus paymentStatus;

    public enum ConsultationStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public enum PaymentStatus {
        PAID, PENDING, CANCELLED
    }

    public Consultation(String consultationId, Patient patient, Doctor doctor,
            LocalDateTime consultationDate, String symptoms, double consultationFee, PaymentStatus paymentStatus) {
        this.consultationId = consultationId;
        this.patient = patient;
        this.doctor = doctor;
        this.consultationDate = consultationDate;
        this.symptoms = symptoms;
        this.consultationFee = consultationFee;
        this.status = ConsultationStatus.SCHEDULED;
        this.paymentStatus = paymentStatus;
    }

    // Getters and Setters
    public String getConsultationId() {
        return consultationId;
    }

    public void setConsultationId(String consultationId) {
        this.consultationId = consultationId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDateTime getConsultationDate() {
        return consultationDate;
    }

    public void setConsultationDate(LocalDateTime consultationDate) {
        this.consultationDate = consultationDate;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getTreatment() {
        return treatment;
    }

    public void setTreatment(String treatment) {
        this.treatment = treatment;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ConsultationStatus getStatus() {
        return status;
    }

    public void setStatus(ConsultationStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getNextVisitDate() {
        return nextVisitDate;
    }

    public void setNextVisitDate(LocalDateTime nextVisitDate) {
        this.nextVisitDate = nextVisitDate;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    @Override
    public String toString() {
        return "Consultation{" +
                "consultationId='" + consultationId + '\'' +
                ", patient=" + patient.getFullName() +
                ", doctor=" + doctor.getFullName() +
                ", consultationDate=" + consultationDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) +
                ", status=" + status +
                ", consultationFee=" + consultationFee +
                ", paymentStatus=" + paymentStatus +
                '}';
    }

    @Override
    public int hashCode() {
        return Integer.parseInt(consultationId.replaceAll("[^0-9]", ""));
    }
}
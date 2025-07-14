package control;

import adt.ArrayList;
import adt.Queue;
import entity.Patient;
import entity.Address;
import entity.BloodType;
import dao.PatientDao;
import java.util.Date;
import java.sql.SQLException;

/**
 * Patient Management Control - Module 1
 * Manages patient registration, record maintenance and queuing management
 */
public class PatientManagementControl {
    
    private PatientDao patientDao;
    private Queue<Patient> patientQueue;
    private ArrayList<Patient> activePatients;
    
    public PatientManagementControl() {
        this.patientDao = new PatientDao();
        this.patientQueue = new Queue<>();
        this.activePatients = new ArrayList<>();
        loadActivePatients();
    }
    
    // Patient Registration Methods
    public boolean registerPatient(String fullName, String icNumber, String email, 
                                 String phoneNumber, Address address, String wardNumber,
                                 BloodType bloodType, ArrayList<String> allergies, 
                                 String emergencyContact) {
        try {
            // Get new patient ID from database
            String patientId = patientDao.getNewId();
            
            // Create new patient
            Patient patient = new Patient(fullName, icNumber, email, phoneNumber, 
                                        address, new Date(), patientId, wardNumber, 
                                        bloodType, allergies, emergencyContact);
            
            // Save to database
            boolean saved = patientDao.insert(patient);
            if (saved) {
                activePatients.add(patient);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error registering patient: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean updatePatientRecord(String patientId, String fullName, String email, 
                                     String phoneNumber, Address address, String wardNumber,
                                     BloodType bloodType, ArrayList<String> allergies, 
                                     String emergencyContact) {
        try {
            Patient patient = patientDao.findById(patientId);
            if (patient != null) {
                patient.setFullName(fullName);
                patient.setEmail(email);
                patient.setPhoneNumber(phoneNumber);
                patient.setAddress(address);
                patient.setWardNumber(wardNumber);
                patient.setBloodType(bloodType);
                patient.setAllergies(allergies);
                patient.setEmergencyContact(emergencyContact);
                
                boolean updated = patientDao.update(patient);
                if (updated) {
                    updateActivePatientsList(patient);
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating patient record: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean deactivatePatient(String patientId) {
        try {
            Patient patient = patientDao.findById(patientId);
            if (patient != null) {
                patient.setActive(false);
                boolean updated = patientDao.update(patient);
                if (updated) {
                    removeFromActivePatients(patient);
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error deactivating patient: " + exception.getMessage());
            return false;
        }
    }
    
    // Queuing Management Methods
    public boolean addPatientToQueue(Patient patient) {
        if (patient != null && patient.isActive()) {
            return patientQueue.enqueue(patient);
        }
        return false;
    }
    
    public Patient getNextPatientFromQueue() {
        return patientQueue.dequeue();
    }
    
    public Patient peekNextPatient() {
        return patientQueue.getFront();
    }
    
    public int getQueueSize() {
        return patientQueue.getNumberOfEntries();
    }
    
    public boolean isPatientInQueue(Patient patient) {
        return patientQueue.contains(patient);
    }
    
    public void clearQueue() {
        patientQueue.clear();
    }
    
    // Search and Retrieval Methods
    public Patient findPatientById(String patientId) {
        try {
            return patientDao.findById(patientId);
        } catch (SQLException exception) {
            System.err.println("Error finding patient by ID: " + exception.getMessage());
            return null;
        }
    }
    
    public Patient findPatientByIcNumber(String icNumber) {
        try {
            // Since findByIcNumber doesn't exist, we'll search through all patients
            ArrayList<Patient> allPatients = patientDao.findAll();
            for (int index = 1; index <= allPatients.getNumberOfEntries(); index++) {
                Patient patient = allPatients.getEntry(index);
                if (patient.getICNumber().equals(icNumber)) {
                    return patient;
                }
            }
            return null;
        } catch (Exception exception) {
            System.err.println("Error finding patient by IC number: " + exception.getMessage());
            return null;
        }
    }
    
    public ArrayList<Patient> findPatientsByName(String name) {
        ArrayList<Patient> results = new ArrayList<>();
        for (int index = 1; index <= activePatients.getNumberOfEntries(); index++) {
            Patient patient = activePatients.getEntry(index);
            if (patient.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(patient);
            }
        }
        return results;
    }
    
    public ArrayList<Patient> getAllActivePatients() {
        return activePatients;
    }
    
    public int getTotalActivePatients() {
        return activePatients.getNumberOfEntries();
    }
    
    // Reporting Methods
    public String generatePatientRegistrationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== PATIENT REGISTRATION REPORT ===\n");
        report.append("Total Active Patients: ").append(getTotalActivePatients()).append("\n");
        report.append("Patients in Queue: ").append(getQueueSize()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= activePatients.getNumberOfEntries(); index++) {
            Patient patient = activePatients.getEntry(index);
            report.append("Patient ID: ").append(patient.getPatientId()).append("\n");
            report.append("Name: ").append(patient.getFullName()).append("\n");
            report.append("IC Number: ").append(patient.getICNumber()).append("\n");
            report.append("Registration Date: ").append(patient.getRegistrationDate()).append("\n");
            report.append("Status: ").append(patient.isActive() ? "Active" : "Inactive").append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public String generateQueueStatusReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== PATIENT QUEUE STATUS REPORT ===\n");
        report.append("Total Patients in Queue: ").append(getQueueSize()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        if (!patientQueue.isEmpty()) {
            report.append("Next Patient: ").append(peekNextPatient().getFullName()).append("\n");
            report.append("Queue Position: 1\n\n");
        }
        
        report.append("Queue Status: ").append(patientQueue.isEmpty() ? "Empty" : "Has Patients").append("\n");
        
        return report.toString();
    }
    
    // Private Helper Methods
    
    private void loadActivePatients() {
        try {
            ArrayList<Patient> allPatients = patientDao.findAll();
            for (int index = 1; index <= allPatients.getNumberOfEntries(); index++) {
                Patient patient = allPatients.getEntry(index);
                if (patient.isActive()) {
                    activePatients.add(patient);
                }
            }
        } catch (SQLException exception) {
            System.err.println("Error loading active patients: " + exception.getMessage());
        }
    }
    
    private void updateActivePatientsList(Patient updatedPatient) {
        for (int index = 1; index <= activePatients.getNumberOfEntries(); index++) {
            Patient patient = activePatients.getEntry(index);
            if (patient.getPatientId().equals(updatedPatient.getPatientId())) {
                activePatients.replace(index, updatedPatient);
                break;
            }
        }
    }
    
    private void removeFromActivePatients(Patient patient) {
        for (int index = 1; index <= activePatients.getNumberOfEntries(); index++) {
            Patient currentPatient = activePatients.getEntry(index);
            if (currentPatient.getPatientId().equals(patient.getPatientId())) {
                activePatients.remove(index);
                break;
            }
        }
    }
} 
package control;

import adt.ArrayBucketList;
import entity.Patient;
import entity.Address;
import entity.BloodType;
import dao.PatientDao;
import java.util.Date;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author: Lai Yoke Hong
 * Patient Management Control - Module 1
 * Manages patient registration, record maintenance and queuing management
 */
public class PatientManagementControl {
    
    private PatientDao patientDao;
    private ArrayBucketList<Patient> patientList;
    private ArrayBucketList<Patient> activePatients;
    
    public PatientManagementControl() {
        this.patientDao = new PatientDao();
        this.patientList = new ArrayBucketList<>();
        this.activePatients = new ArrayBucketList<>();
        loadActivePatients();
    }
    
    public void loadActivePatients() {
        try {
            activePatients = patientDao.findAll();
        } catch (Exception exception) {
            System.err.println("Error loading active patients: " + exception.getMessage());
        }
    }
    
    // Patient Registration Methods
    public boolean registerPatient(String fullName, String icNumber, String email, 
                                 String phoneNumber, Address address, String wardNumber,
                                 BloodType bloodType, String allergies, 
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
                activePatients.add(patient.hashCode(), patient);
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
                                     BloodType bloodType, String allergies, 
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
            return patientList.add(patient.hashCode(), patient);
        }
        return false;
    }
    
    public Patient getNextPatientFromQueue() {
        return patientList.remove(patientList.getFirstEntry());
    }
    
    public Patient peekNextPatient() {
        return patientList.getFirstEntry();
    }
    
    public int getQueueSize() {
        return patientList.getNumberOfEntries();
    }
    
    public boolean isPatientInQueue(Patient patient) {
        return patientList.contains(patient);
    }
    
    public void clearQueue() {
        patientList.clear();
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
            Iterator<Patient> patientIterator = activePatients.iterator();
            while (patientIterator.hasNext()) {
                Patient patient = patientIterator.next();
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
    
    public ArrayBucketList<Patient> findPatientsByName(String name) {
        ArrayBucketList<Patient> results = new ArrayBucketList<>();
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(patient.hashCode(), patient);
            }
        }
        return results;
    }
    
    public ArrayBucketList<Patient> getAllActivePatients() {
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
        
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
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
        report.append("=== QUEUE STATUS REPORT ===\n");
        report.append("Patients in Queue: ").append(getQueueSize()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        if (getQueueSize() > 0) {
            Iterator<Patient> queueIterator = patientList.iterator();
            while (queueIterator.hasNext()) {
                Patient patient = queueIterator.next();
                report.append("Patient ID: ").append(patient.getPatientId()).append("\n");
                report.append("Name: ").append(patient.getFullName()).append("\n");
                report.append("IC Number: ").append(patient.getICNumber()).append("\n");
                report.append("----------------------------------------\n");
            }
        } else {
            report.append("No patients in queue.\n");
        }
        
        return report.toString();
    }
    
    private void updateActivePatientsList(Patient updatedPatient) {
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient.getPatientId().equals(updatedPatient.getPatientId())) {
                // Remove old entry and add updated one
                activePatients.remove(patient);
                activePatients.add(updatedPatient.hashCode(), updatedPatient);
                break;
            }
        }
    }
    
    private void removeFromActivePatients(Patient patient) {
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient currentPatient = patientIterator.next();
            if (currentPatient.getPatientId().equals(patient.getPatientId())) {
                activePatients.remove(currentPatient);
                break;
            }
        }
    }
} 
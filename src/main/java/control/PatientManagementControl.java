package control;

import adt.ArrayBucketList;
import entity.Patient;
import entity.Address;
import entity.BloodType;
import dao.PatientDao;
import dao.AddressDao;
import java.time.LocalDate;
import java.sql.SQLException;
import java.util.Iterator;

/**
 * @author: Lai Yoke Hong
 * Patient Management Control - Module 1
 * Manages patient registration, record maintenance and queuing management
 */
public class PatientManagementControl {
    
    private PatientDao patientDao;
    private AddressDao addressDao;
    private ArrayBucketList<String, Patient> patientList;
    private ArrayBucketList<String, Patient> activePatients;
    
    public PatientManagementControl() {
        this.patientDao = new PatientDao();
        this.addressDao = new AddressDao();
        this.patientList = new ArrayBucketList<String, Patient>();
        this.activePatients = new ArrayBucketList<String, Patient>();
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
            // First, insert the address and get the generated address ID
            boolean addressInserted = addressDao.insertAndReturnId(address);
            if (!addressInserted) {
                System.err.println("Failed to insert address");
                return false;
            }
            
            // Create new patient with the generated address ID
            Patient patient = new Patient(fullName, icNumber, email, phoneNumber, 
                                        address, LocalDate.now(), null, wardNumber, 
                                        bloodType, allergies, emergencyContact);
            
            // Insert patient and get the generated patient ID
            boolean patientInserted = patientDao.insertAndReturnId(patient);
            if (!patientInserted) {
                System.err.println("Failed to insert patient");
                return false;
            }
            
            // Add to active patients list
            activePatients.add(patient.getPatientId(), patient);
            return true;
            
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
            patientList.addToQueue(patient.getPatientId(), patient);
            return true;
        }
        return false;
    }
    
    public Patient getNextPatientFromQueue() {
        return patientList.removeFront();
    }
    
    public Patient peekNextPatient() {
        return patientList.peekFront();
    }
    
    public int getQueueSize() {
        return patientList.getSize();
    }
    
    public boolean isPatientInQueue(Patient patient) {
        return patientList.contains(patient.getPatientId());
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
    
    public ArrayBucketList<String, Patient> findPatientsByName(String name) {
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
    }
    
    public ArrayBucketList<String, Patient> getAllActivePatients() {
        return activePatients;
    }
    
    public int getTotalActivePatients() {
        return activePatients.getSize();
    }
    
    // Reporting Methods
    public String generatePatientRegistrationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== PATIENT REGISTRATION REPORT ===\n");
        report.append("Total Active Patients: ").append(getTotalActivePatients()).append("\n");
        report.append("Patients in Queue: ").append(getQueueSize()).append("\n");
        report.append("Report Generated: ").append(LocalDate.now()).append("\n\n");
        
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
        report.append("Report Generated: ").append(LocalDate.now()).append("\n\n");
        
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
                activePatients.remove(patient.getPatientId());
                activePatients.add(updatedPatient.getPatientId(), updatedPatient);
                break;
            }
        }
    }
    
    private void removeFromActivePatients(Patient patient) {
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient currentPatient = patientIterator.next();
            if (currentPatient.getPatientId().equals(patient.getPatientId())) {
                activePatients.remove(currentPatient.getPatientId());
                break;
            }
        }
    }
} 
package control;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Iterator;

import adt.ArrayBucketList;
import dao.AddressDao;
import dao.PatientDao;
import entity.Address;
import entity.BloodType;
import entity.Patient;

/**
 * @author: Lai Yoke Hong
 *          Patient Management Control - Module 1
 *          Manages patient registration, record maintenance and queuing
 *          management
 */
public class PatientManagementControl {

    private PatientDao patientDao;
    private AddressDao addressDao;
    private ArrayBucketList<String, Patient> patienQueue;
    private ArrayBucketList<String, Patient> activePatients;

    public PatientManagementControl() {
        this.patientDao = new PatientDao();
        this.addressDao = new AddressDao();
        this.patienQueue = new ArrayBucketList<String, Patient>();
        this.activePatients = new ArrayBucketList<String, Patient>();
    }

    // Load all active patients from persistent storage into the in-memory cachea
    public void loadActivePatients() {
        try {
            activePatients = patientDao.findAll();
        } catch (Exception exception) {
            System.err.println("Error loading active patients: " + exception.getMessage());
            System.err.println("Initializing with empty patient list...");
            activePatients = new ArrayBucketList<String, Patient>();
        }
    }

    // Ensure data is loaded before performing operations
    private void ensureDataLoaded() {
        if (activePatients.getSize() == 0) {
            loadActivePatients();
        }
    }

    // Register a new patient
    public boolean registerPatient(String fullName, String icNumber, String email,
            String phoneNumber, Address address,
            BloodType bloodType, String allergies,
            String emergencyContact) {
        try {
            boolean addressInserted = addressDao.insertAndReturnId(address);
            if (!addressInserted) {
                System.err.println("Failed to insert address");
                return false;
            }

            Patient patient = new Patient(fullName, icNumber, email, phoneNumber,
                    address, LocalDate.now(), null,
                    bloodType, allergies, emergencyContact);

            boolean patientInserted = patientDao.insertAndReturnId(patient);
            if (!patientInserted) {
                System.err.println("Failed to insert patient");
                return false;
            }

            activePatients.add(patient.getPatientId(), patient);
            return true;

        } catch (Exception exception) {
            System.err.println("Error registering patient: " + exception.getMessage());
            return false;
        }
    }

    // Blood type menu choice
    public BloodType getBloodTypeFromChoice(int choice) {
        switch (choice) {
            case 1:
                return BloodType.A_POSITIVE;
            case 2:
                return BloodType.A_NEGATIVE;
            case 3:
                return BloodType.B_POSITIVE;
            case 4:
                return BloodType.B_NEGATIVE;
            case 5:
                return BloodType.AB_POSITIVE;
            case 6:
                return BloodType.AB_NEGATIVE;
            case 7:
                return BloodType.O_POSITIVE;
            case 8:
                return BloodType.O_NEGATIVE;
            case 9:
                return BloodType.OTHERS;
            default:
                return BloodType.A_POSITIVE;
        }
    }

    // Update patient record
    public boolean updatePatientRecord(String patientId, String fullName, String email,
            String phoneNumber, Address address,
            BloodType bloodType, String allergies,
            String emergencyContact) {
        try {
            Patient patient = patientDao.findById(patientId);
            if (patient != null) {
                patient.setFullName(fullName);
                patient.setEmail(email);
                patient.setPhoneNumber(phoneNumber);
                patient.setAddress(address);
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

    // Deactivates a patient
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
        if (patient == null || !patient.isActive()) {
            return false;
        }
        if (patienQueue.queueContains(patient.getPatientId())) {
            return false;
        }
        patienQueue.addToQueue(patient.getPatientId(), patient);
        return true;
    }

    // Removes and returns the next patient from the queue
    public Patient getNextPatientFromQueue() {
        return patienQueue.removeFront();
    }

    public Patient peekNextPatient() {
        return patienQueue.peekFront();
    }

    public int getQueueSize() {
        return patienQueue.getQueueSize();
    }

    public boolean isPatientInQueue(Patient patient) {
        if (patient == null)
            return false;
        return patienQueue.queueContains(patient.getPatientId());
    }

    public void clearQueue() {
        patienQueue.clear();
    }

    // Search and Retrieval Methods
    public Patient findPatientById(String patientId) {
        Patient patient = null;
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient p = patientIterator.next();
            if (p.getPatientId().equals(patientId)) {
                patient = p;
            }
        }
        return patient;
    }

    // Finds a patient by exact IC number
    public Patient findPatientByIcNumber(String icNumber) {
        try {
            ensureDataLoaded();

            if (icNumber == null) {
                return null;
            }

            Iterator<Patient> patientIterator = activePatients.iterator();
            while (patientIterator.hasNext()) {
                Patient patient = patientIterator.next();
                if (patient.getICNumber() != null && patient.getICNumber().equals(icNumber)) {
                    return patient;
                }
            }

            return null;
        } catch (Exception exception) {
            System.err.println("Error finding patient by IC number: " + exception.getMessage());
            return null;
        }
    }

    // Finds a patient by name
    public ArrayBucketList<String, Patient> findPatientsByName(String name) {
        ensureDataLoaded();
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

    // Finds a patient by email
    public Patient findPatientByEmail(String email) {
        try {
            ensureDataLoaded();
            Iterator<Patient> patientIterator = activePatients.iterator();
            while (patientIterator.hasNext()) {
                Patient patient = patientIterator.next();
                if (patient.getEmail().equalsIgnoreCase(email)) {
                    return patient;
                }
            }
            return null;
        } catch (Exception exception) {
            System.err.println("Error finding patient by email: " + exception.getMessage());
            return null;
        }
    }

    // Finds a patient by email
    public ArrayBucketList<String, Patient> findPatientsByEmail(String email) {
        ensureDataLoaded();
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient.getEmail().toLowerCase().contains(email.toLowerCase())) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
    }

    // Finds a patient by IC number
    public Patient findPatientsByIcNumber(String icNumber) {
       return activePatients.getValue(icNumber);
    }

    // Returns the active patients
    public ArrayBucketList<String, Patient> getAllActivePatients() {
        ensureDataLoaded();
        return activePatients;
    }

    public int getTotalActivePatients() {
        ensureDataLoaded();
        return activePatients.getSize();
    }

    // Reporting Methods
    public String generatePatientRegistrationReport() {
        ensureDataLoaded();
        StringBuilder report = new StringBuilder();
        report.append("=== PATIENT REGISTRATION REPORT ===\n");
        report.append("Total Active Patients: ").append(getTotalActivePatients()).append("\n");
        report.append("Patients in Queue: ").append(getQueueSize()).append("\n");
        report.append("Report Generated: ").append(LocalDate.now()).append("\n\n");

        int patientCount = 0;
        int maxPatients = 1000;

        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext() && patientCount < maxPatients) {
            Patient patient = patientIterator.next();
            if (patient != null) {
                report.append("Patient ID: ").append(patient.getPatientId()).append("\n");
                report.append("Name: ").append(patient.getFullName()).append("\n");
                report.append("IC Number: ").append(patient.getICNumber()).append("\n");
                report.append("Registration Date: ").append(patient.getRegistrationDate()).append("\n");
                report.append("Status: ").append(patient.isActive() ? "Active" : "Inactive").append("\n");
                report.append("----------------------------------------\n");
                patientCount++;
            }
        }

        if (patientCount >= maxPatients) {
            report.append("WARNING: Report truncated due to safety limit.\n");
        }

        return report.toString();
    }

    // Generates patient record summary that can be sorted
    public String generatePatientRecordSummaryReport(String sortBy, String sortOrder) {
        ensureDataLoaded();

        int size = activePatients.getSize();
        Patient[] items = new Patient[size];
        int index = 0;
        Iterator<Patient> it = activePatients.iterator();
        while (it.hasNext() && index < size) {
            items[index++] = it.next();
        }

        final boolean ascending = sortOrder == null || !sortOrder.equalsIgnoreCase("desc");

        java.util.Comparator<Patient> comparator = new java.util.Comparator<Patient>() {
            @Override
            public int compare(Patient a, Patient b) {
                if (a == null && b == null)
                    return 0;
                if (a == null)
                    return ascending ? -1 : 1;
                if (b == null)
                    return ascending ? 1 : -1;

                int result = 0;
                String key = sortBy == null ? "name" : sortBy.toLowerCase();
                switch (key) {
                    case "id":
                        result = safeStr(a.getPatientId()).compareToIgnoreCase(safeStr(b.getPatientId()));
                        break;
                    case "ic":
                        result = safeStr(a.getICNumber()).compareToIgnoreCase(safeStr(b.getICNumber()));
                        break;
                    case "email":
                        result = safeStr(a.getEmail()).compareToIgnoreCase(safeStr(b.getEmail()));
                        break;
                    case "phone":
                        result = safeStr(a.getPhoneNumber()).compareToIgnoreCase(safeStr(b.getPhoneNumber()));
                        break;
                    case "blood":
                        String ba = a.getBloodType() == null ? "" : a.getBloodType().toString();
                        String bb = b.getBloodType() == null ? "" : b.getBloodType().toString();
                        result = ba.compareToIgnoreCase(bb);
                        break;
                    case "allergies":
                        result = safeStr(a.getAllergies()).compareToIgnoreCase(safeStr(b.getAllergies()));
                        break;
                    case "regdate":
                        java.time.LocalDate da = a.getRegistrationDate();
                        java.time.LocalDate db = b.getRegistrationDate();
                        if (da == null && db == null)
                            result = 0;
                        else if (da == null)
                            result = -1;
                        else if (db == null)
                            result = 1;
                        else
                            result = da.compareTo(db);
                        break;
                    case "status":
                        String sa = a.isActive() ? "Active" : "Inactive";
                        String sb = b.isActive() ? "Active" : "Inactive";
                        result = sa.compareToIgnoreCase(sb);
                        break;
                    case "name":
                        result = safeStr(a.getFullName()).compareToIgnoreCase(safeStr(b.getFullName()));
                        break;
                    default:
                        result = safeStr(a.getFullName()).compareToIgnoreCase(safeStr(b.getFullName()));
                        break;
                }
                return ascending ? result : -result;
            }
        };

        utility.QuickSort.sort(items, comparator);

        StringBuilder report = new StringBuilder();
        report.append("\n=== Patient Record Summary ===\n");
        report.append("Total Patients: ").append(items.length).append("\n");
        report.append("Sorted By: ").append(sortBy).append(" | Order: ").append(ascending ? "Ascending" : "Descending")
                .append("\n");
        report.append("Generated: ").append(java.time.LocalDate.now()).append("\n\n");

        report.append("-".repeat(182)).append("\n");
        report.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-12s | %-8s | %-12s | %-33s |\n",
                "Patient ID", "Full Name", "IC Number", "Email", "Phone", "Reg Date", "Status", "Blood Type",
                "Allergies"));
        report.append("-".repeat(182)).append("\n");

        for (int i = 0; i < items.length; i++) {
            Patient p = items[i];
            if (p == null)
                continue;
            String id = valueOrNA(p.getPatientId());
            String name = valueOrNA(p.getFullName());
            String ic = valueOrNA(p.getICNumber());
            String email = valueOrNA(p.getEmail());
            String phone = valueOrNA(p.getPhoneNumber());
            String reg = p.getRegistrationDate() == null ? "N/A"
                    : p.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String status = p.isActive() ? "Active" : "Inactive";

            if (name.length() > 25)
                name = name.substring(0, 22) + "...";
            if (email.length() > 25)
                email = email.substring(0, 22) + "...";

            String blood = p.getBloodType() == null ? "N/A" : p.getBloodType().toString();
            String allergiesOut = valueOrNA(p.getAllergies());
            if (allergiesOut.length() > 20)
                allergiesOut = allergiesOut.substring(0, 17) + "...";

            report.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-12s | %-8s | %-12s | %-33s |\n",
                    id, name, ic, email, phone, reg, status, blood, allergiesOut));
        }

        report.append("-".repeat(182)).append("\n");
        report.append(">>> End of Report <<<\n");

        return report.toString();
    }

    private String safeStr(String s) {
        return s == null ? "" : s;
    }

    private String valueOrNA(String s) {
        return s == null ? "N/A" : s;
    }

    private void updateActivePatientsList(Patient updatedPatient) {
        activePatients.remove(updatedPatient.getPatientId());
        activePatients.add(updatedPatient.getPatientId(), updatedPatient);
    }

    private void removeFromActivePatients(Patient patient) {
        activePatients.remove(patient.getPatientId());
    }

    public String displayPatientSearchResult(Patient patient, String searchCriteria) {
        StringBuilder result = new StringBuilder();
        result.append("\n=== Patient Search Result ===\n");
        result.append(searchCriteria).append("\n\n");

        result.append("--- PATIENT DETAILS ---\n");
        result.append("Patient ID        : ").append(patient.getPatientId() != null ? patient.getPatientId() : "N/A")
                .append("\n");
        result.append("Full Name         : ").append(patient.getFullName() != null ? patient.getFullName() : "N/A")
                .append("\n");
        result.append("IC Number         : ").append(patient.getICNumber() != null ? patient.getICNumber() : "N/A")
                .append("\n");
        result.append("Email             : ").append(patient.getEmail() != null ? patient.getEmail() : "N/A")
                .append("\n");
        result.append("Phone Number      : ")
                .append(patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A").append("\n");
        result.append("Blood Type        : ")
                .append(patient.getBloodType() != null ? patient.getBloodType().toString() : "N/A").append("\n");
        result.append("Allergies         : ").append(patient.getAllergies() != null ? patient.getAllergies() : "N/A")
                .append("\n");
        result.append("Emergency Contact : ")
                .append(patient.getEmergencyContact() != null ? patient.getEmergencyContact() : "N/A").append("\n");
        result.append("Registration Date : ").append(patient.getRegistrationDate() != null
                ? patient.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-uuuu"))
                : "N/A").append("\n");
        result.append("Status            : ").append(patient.isActive() ? "Active" : "Inactive").append("\n");

        // Address details
        if (patient.getAddress() != null) {
            result.append("\n--- ADDRESS DETAILS ---\n");
            result.append("Street            : ")
                    .append(patient.getAddress().getStreet() != null ? patient.getAddress().getStreet() : "N/A")
                    .append("\n");
            result.append("City              : ")
                    .append(patient.getAddress().getCity() != null ? patient.getAddress().getCity() : "N/A")
                    .append("\n");
            result.append("State             : ")
                    .append(patient.getAddress().getState() != null ? patient.getAddress().getState() : "N/A")
                    .append("\n");
            result.append("Postal Code       : ")
                    .append(patient.getAddress().getZipCode() != null ? patient.getAddress().getZipCode() : "N/A")
                    .append("\n");
            result.append("Country           : ")
                    .append(patient.getAddress().getCountry() != null ? patient.getAddress().getCountry() : "N/A")
                    .append("\n");
        }

        return result.toString();
    }

    public String displayPatientSearchResults(ArrayBucketList<String, Patient> patients, String searchCriteria) {
        StringBuilder result = new StringBuilder();
        result.append("\n=== Patient Search Results ===\n");
        result.append("Search Criteria: ").append(searchCriteria).append("\n");
        result.append("Search Date: ").append(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")))
                .append("\n");
        result.append("Total Results: ").append(patients.getSize()).append(" patient(s) found\n\n");

        result.append("--- Patient List ---\n");
        result.append(
                "------------------------------------------------------------------------------------------------------------\n");
        result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-15s |\n", "Patient ID", "Full Name",
                "IC Number", "Email", "Phone Number"));
        result.append(
                "------------------------------------------------------------------------------------------------------------\n");

        java.util.Iterator<Patient> patientIterator = patients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            String id = patient.getPatientId() != null ? patient.getPatientId() : "N/A";
            String name = patient.getFullName() != null ? patient.getFullName() : "N/A";
            String ic = patient.getICNumber() != null ? patient.getICNumber() : "N/A";
            String email = patient.getEmail() != null ? patient.getEmail() : "N/A";
            String phoneNumber = patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A";

            // Truncate long names and emails
            if (name.length() > 25)
                name = name.substring(0, 22) + "...";
            if (email.length() > 25)
                email = email.substring(0, 22) + "...";

            result.append(
                    String.format("| %-12s | %-25s | %-15s | %-25s | %-15s |\n", id, name, ic, email, phoneNumber));
        }

        result.append(
                "------------------------------------------------------------------------------------------------------------\n");
        result.append(">>> End of Search <<<\n");

        return result.toString();
    }

    /**
     * Displays sorted patient search results with sorting options
     */
    public String displaySortedPatientSearchResults(ArrayBucketList<String, Patient> patients, String searchCriteria, String sortBy, String sortOrder) {
        if (patients.isEmpty()) {
            return "No patients found.";
        }

        // Convert to array for sorting
        Patient[] patientArray = patients.toArray(Patient.class);
        
        // Create comparator for sorting
        final boolean ascending = sortOrder == null || !sortOrder.equalsIgnoreCase("desc");
        java.util.Comparator<Patient> comparator = new java.util.Comparator<Patient>() {
            @Override
            public int compare(Patient a, Patient b) {
                if (a == null && b == null) return 0;
                if (a == null) return ascending ? -1 : 1;
                if (b == null) return ascending ? 1 : -1;

                int result = 0;
                String key = sortBy == null ? "name" : sortBy.toLowerCase();
                switch (key) {
                    case "id":
                        result = safeStr(a.getPatientId()).compareToIgnoreCase(safeStr(b.getPatientId()));
                        break;
                    case "ic":
                        result = safeStr(a.getICNumber()).compareToIgnoreCase(safeStr(b.getICNumber()));
                        break;
                    case "email":
                        result = safeStr(a.getEmail()).compareToIgnoreCase(safeStr(b.getEmail()));
                        break;
                    case "phone":
                        result = safeStr(a.getPhoneNumber()).compareToIgnoreCase(safeStr(b.getPhoneNumber()));
                        break;
                    case "regdate":
                        java.time.LocalDate da = a.getRegistrationDate();
                        java.time.LocalDate db = b.getRegistrationDate();
                        if (da == null && db == null) result = 0;
                        else if (da == null) result = -1;
                        else if (db == null) result = 1;
                        else result = da.compareTo(db);
                        break;
                    case "name":
                    default:
                        result = safeStr(a.getFullName()).compareToIgnoreCase(safeStr(b.getFullName()));
                        break;
                }
                return ascending ? result : -result;
            }
        };

        // Sort the array
        utility.QuickSort.sort(patientArray, comparator);

        StringBuilder result = new StringBuilder();
        result.append("\n=== Patient Search Results ===\n");
        result.append("Search Criteria: ").append(searchCriteria).append("\n");
        result.append("Sorted By: ").append(sortBy).append(" | Order: ").append(ascending ? "Ascending" : "Descending").append("\n");
        result.append("Search Date: ").append(
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")))
                .append("\n");
        result.append("Total Results: ").append(patients.getSize()).append(" patient(s) found\n\n");

        result.append("--- Patient List ---\n");
        result.append(
                "------------------------------------------------------------------------------------------------------------\n");
        result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-15s |\n", "Patient ID", "Full Name",
                "IC Number", "Email", "Phone Number"));
        result.append(
                "------------------------------------------------------------------------------------------------------------\n");

        for (Patient patient : patientArray) {
            if (patient == null) continue;
            
            String id = patient.getPatientId() != null ? patient.getPatientId() : "N/A";
            String name = patient.getFullName() != null ? patient.getFullName() : "N/A";
            String ic = patient.getICNumber() != null ? patient.getICNumber() : "N/A";
            String email = patient.getEmail() != null ? patient.getEmail() : "N/A";
            String phoneNumber = patient.getPhoneNumber() != null ? patient.getPhoneNumber() : "N/A";

            // Truncate long names and emails
            if (name.length() > 25)
                name = name.substring(0, 22) + "...";
            if (email.length() > 25)
                email = email.substring(0, 22) + "...";

            result.append(
                    String.format("| %-12s | %-25s | %-15s | %-25s | %-15s |\n", id, name, ic, email, phoneNumber));
        }

        result.append(
                "------------------------------------------------------------------------------------------------------------\n");
        result.append(">>> End of Search <<<\n");

        return result.toString();
    }
}
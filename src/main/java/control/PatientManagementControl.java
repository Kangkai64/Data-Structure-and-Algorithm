package control;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import adt.ArrayBucketList;
import dao.AddressDao;
import dao.PatientDao;
import entity.Address;
import entity.BloodType;
import entity.Patient;
import utility.ConsoleUtils;
import utility.QuickSort;

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

    // Finds patients by age range (inclusive)
    public ArrayBucketList<String, Patient> findPatientsByAgeRange(int minAge, int maxAge) {
        ensureDataLoaded();
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        if (minAge > maxAge) {
            int temp = minAge;
            minAge = maxAge;
            maxAge = temp;
        }
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            int age = patient.getAge();
            if (age >= minAge && age <= maxAge) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
    }

    // Finds patients who visited within the past N days (based on registration date)
    public ArrayBucketList<String, Patient> findPatientsVisitedWithinDays(int days) {
        ensureDataLoaded();
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        if (days <= 0) return results;
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusDays(days);
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            java.time.LocalDate reg = patient.getRegistrationDate();
            if (reg != null && (reg.isAfter(cutoff) || reg.isEqual(cutoff))) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
    }

    // Finds patients who visited within the past N months (based on registration date)
    public ArrayBucketList<String, Patient> findPatientsVisitedWithinMonths(int months) {
        ensureDataLoaded();
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        if (months <= 0) return results;
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusMonths(months);
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            java.time.LocalDate reg = patient.getRegistrationDate();
            if (reg != null && (reg.isAfter(cutoff) || reg.isEqual(cutoff))) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
    }

    // Finds patients who visited within the past N years (based on registration date)
    public ArrayBucketList<String, Patient> findPatientsVisitedWithinYears(int years) {
        ensureDataLoaded();
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        if (years <= 0) return results;
        java.time.LocalDate cutoff = java.time.LocalDate.now().minusYears(years);
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            java.time.LocalDate reg = patient.getRegistrationDate();
            if (reg != null && (reg.isAfter(cutoff) || reg.isEqual(cutoff))) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
    }


    // Finds patients by address fields using a single keyword (matches any field or full combined address)
    public ArrayBucketList<String, Patient> findPatientsByAddress(String keyword) {
        ensureDataLoaded();
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        if (keyword == null) return results;
        String query = normalizeAddress(keyword);
        if (query.isEmpty()) return results;
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient.getAddress() == null) continue;
            Address address = patient.getAddress();
            String street = normalizeAddress(address.getStreet());
            String city = normalizeAddress(address.getCity());
            String state = normalizeAddress(address.getState());
            String zip = normalizeAddress(address.getZipCode());
            String country = normalizeAddress(address.getCountry());

            // Match any field
            boolean fieldMatch = street.contains(query) || city.contains(query) || state.contains(query)
                    || zip.contains(query) || country.contains(query);

            // Match combined full address
            String combined = String.join(" ", street, city, state, zip, country).trim();
            boolean combinedMatch = !combined.isEmpty() && (combined.contains(query) || query.contains(combined));

            if (fieldMatch || combinedMatch) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
    }

    // Normalizes address strings for comparison
    private String normalizeAddress(String input) {
        if (input == null) return "";
        String s = input.toLowerCase();
        s = s.replaceAll("[\\p{Punct}]", " "); // remove punctuation
        s = s.replaceAll("\\s+", " ").trim();
        return s;
    }

    // Finds patients by specific blood type
    public ArrayBucketList<String, Patient> findPatientsByBloodType(BloodType bloodType) {
        ensureDataLoaded();
        ArrayBucketList<String, Patient> results = new ArrayBucketList<String, Patient>();
        if (bloodType == null) return results;
        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient.getBloodType() != null && patient.getBloodType() == bloodType) {
                results.add(patient.getPatientId(), patient);
            }
        }
        return results;
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
        report.append("=".repeat(182)).append("\n");
        report.append(ConsoleUtils.centerText("PATIENT RECORD SUMMARY", 182) ).append("\n");
        report.append("=".repeat(182)).append("\n");
        report.append("Total Patients: ").append(items.length).append("\n");
        report.append("Sorted By: ").append(sortBy).append(" | Order: ").append(ascending ? "Ascending" : "Descending")
                .append("\n");
        report.append("Generated: ").append(java.time.LocalDate.now()).append("\n\n");

        report.append("-".repeat(182)).append("\n");
        report.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-12s | %-8s | %-12s | %-33s |\n",
                "Patient ID", "Full Name", "IC Number", "Email", "Phone", "Reg Date", "Status", "Blood Type",
                "Allergies"));
        report.append("-".repeat(182)).append("\n");

        for (int itemIndex = 0; itemIndex < items.length; itemIndex++) {
            Patient p = items[itemIndex];
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

        report.append("=".repeat(182)).append("\n");
        report.append(ConsoleUtils.centerText("END OF PATIENT RECORD SUMMARY REPORT", 182)).append("\n");
        report.append("=".repeat(182)).append("\n");

        return report.toString();
    }

    // Generates patient demographics report
    public String generatePatientDemographicsReport(String sortBy, String sortOrder) {
        ensureDataLoaded();
        StringBuilder report = new StringBuilder();

        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("PATIENT MANAGEMENT SYSTEM - PATIENT DEMOGRAPHICS REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n");

        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("DEMOGRAPHICS SUMMARY", 120) ).append("\n");
        report.append("=".repeat(120)).append("\n");
        report.append(String.format("Total Active Patients: %d\n", getTotalActivePatients()));
        report.append(String.format("Patients in Queue: %d\n", getQueueSize()));

        int[] ageGroups = { 0, 18, 25, 35, 50, 65, 100 }; 
        String[] ageGroupLabels = { "Under 18", "18-24", "25-34", "35-49", "50-64", "65+" };
        int[] ageGroupCounts = new int[6];
        int[] genderCounts = { 0, 0 }; 
        String[] bloodTypeCounts = new String[10];
        int[] bloodTypeCountsNum = new int[10];
        int bloodTypeCount = 0;

        Iterator<Patient> patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient != null) {
                int age = patient.getAge();
                for (int ageIndex = 0; ageIndex < ageGroups.length - 1; ageIndex++) {
                    if (age >= ageGroups[ageIndex] && age < ageGroups[ageIndex + 1]) {
                        ageGroupCounts[ageIndex]++;
                        break;
                    }
                }

                String icNumber = patient.getICNumber();
                if (icNumber != null && icNumber.length() >= 12) {
                    char lastDigit = icNumber.charAt(11);
                    if (Character.isDigit(lastDigit)) {
                        int digit = Character.getNumericValue(lastDigit);
                        if (digit % 2 == 1) { // Odd = Male
                            genderCounts[0]++;
                        } else { // Even = Female
                            genderCounts[1]++;
                        }
                    }
                }

                if (patient.getBloodType() != null) {
                    String bloodType = patient.getBloodType().toString();
                    boolean found = false;
                    for (int bloodIndex = 0; bloodIndex < bloodTypeCount; bloodIndex++) {
                        if (bloodTypeCounts[bloodIndex].equals(bloodType)) {
                            bloodTypeCountsNum[bloodIndex]++;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        bloodTypeCounts[bloodTypeCount] = bloodType;
                        bloodTypeCountsNum[bloodTypeCount] = 1;
                        bloodTypeCount++;
                    }
                }
            }
        }

        report.append("\nAGE DISTRIBUTION:\n");
        for (int ageLabelIndex = 0; ageLabelIndex < ageGroupLabels.length; ageLabelIndex++) {
            double percentage = getTotalActivePatients() > 0 ? (double) ageGroupCounts[ageLabelIndex] / getTotalActivePatients() * 100 : 0;
            report.append(String.format("%-8s: %3d patients (%.1f%%)\n", ageGroupLabels[ageLabelIndex], ageGroupCounts[ageLabelIndex], percentage));
        }

        report.append("\nGENDER DISTRIBUTION:\n");
        double malePercentage = getTotalActivePatients() > 0 ? (double) genderCounts[0] / getTotalActivePatients() * 100 : 0;
        double femalePercentage = getTotalActivePatients() > 0 ? (double) genderCounts[1] / getTotalActivePatients() * 100 : 0;
        report.append(String.format("Male  : %3d patients (%.1f%%)\n", genderCounts[0], malePercentage));
        report.append(String.format("Female: %3d patients (%.1f%%)\n", genderCounts[1], femalePercentage));

        report.append("\nBLOOD TYPE DISTRIBUTION:\n");
        for (int bloodTypeIndex = 0; bloodTypeIndex < bloodTypeCount; bloodTypeIndex++) {
            double percentage = getTotalActivePatients() > 0 ? (double) bloodTypeCountsNum[bloodTypeIndex] / getTotalActivePatients() * 100 : 0;
            report.append(String.format("%-4s: %3d patients (%.1f%%)\n", bloodTypeCounts[bloodTypeIndex], bloodTypeCountsNum[bloodTypeIndex], percentage));
        }

        int currentYear = LocalDate.now().getYear();
        int[] monthlyRegistrations = new int[13]; 

        patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            Patient patient = patientIterator.next();
            if (patient != null && patient.getRegistrationDate() != null &&
                    patient.getRegistrationDate().getYear() == currentYear) {
                int month = patient.getRegistrationDate().getMonthValue();
                monthlyRegistrations[month]++;
            }
        }

        report.append(String.format("\nREGISTRATION TREND FOR %d:\n", currentYear));
        String[] months = { "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
        for (int i = 1; i <= 12; i++) {
            if (monthlyRegistrations[i] > 0) {
                report.append(String.format("%-3s: %d registrations\n", months[i], monthlyRegistrations[i]));
            }
        }
        report.append("\n");
        report.append("=".repeat(111)).append("\n");
        report.append(ConsoleUtils.centerText("DETAILED PATIENT DEMOGRAPHICS", 111)).append("\n");
        report.append("=".repeat(111)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append("-".repeat(111)).append("\n");
        report.append(String.format("| %-10s | %-25s | %-8s | %-6s | %-13s | %-15s | %-12s |\n",
                "ID", "Name", "Age", "Gender", "Blood Type", "Allergies", "Registration"));
        report.append("-".repeat(111)).append("\n");

        // Convert to array for sorting
        Patient[] patientArray = new Patient[activePatients.getSize()];
        int index = 0;
        patientIterator = activePatients.iterator();
        while (patientIterator.hasNext()) {
            patientArray[index++] = patientIterator.next();
        }

        // Sort the patient array
        sortPatientArray(patientArray, sortBy, sortOrder);

        // Generate sorted table
        for (Patient patient : patientArray) {
            if (patient == null)
                continue;
            String id = patient.getPatientId() == null ? "-" : patient.getPatientId();
            String name = patient.getFullName() == null ? "-" : patient.getFullName();
            String age = String.valueOf(patient.getAge());
            String gender = getGenderFromIC(patient.getICNumber());
            String bloodType = patient.getBloodType() == null ? "-" : patient.getBloodType().toString();
            String allergies = patient.getAllergies() == null ? "-" : patient.getAllergies();
            String regDate = patient.getRegistrationDate() == null ? "-"
                    : patient.getRegistrationDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));

            if (name.length() > 25)
                name = name.substring(0, 24) + "…";
            if (allergies.length() > 15)
                allergies = allergies.substring(0, 14) + "…";

            report.append(String.format("| %-10s | %-25s | %-8s | %-6s | %-13s | %-15s | %-12s |\n",
                    id, name, age, gender, bloodType, allergies, regDate));
        }

        report.append("-".repeat(111)).append("\n");
        report.append("*".repeat(111)).append("\n");
        report.append(ConsoleUtils.centerText("END OF PATIENT DEMOGRAPHICS REPORT", 111)).append("\n");
        report.append("=".repeat(111)).append("\n");

        return report.toString();
    }

    // Helper methods for demographics report
    private String getGenderFromIC(String icNumber) {
        if (icNumber == null || icNumber.length() < 12) {
            return "-";
        }
        char lastDigit = icNumber.charAt(11);
        if (Character.isDigit(lastDigit)) {
            int digit = Character.getNumericValue(lastDigit);
            return digit % 2 == 1 ? "Male" : "Female";
        }
        return "-";
    }

    private String getSortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> "Patient Name";
            case "age" -> "Age";
            case "gender" -> "Gender";
            case "blood" -> "Blood Type";
            case "allergies" -> "Allergies";
            case "regdate" -> "Registration Date";
            case "status" -> "Status";
            case "id" -> "ID";
            default -> "Default";
        };
    }

    private void sortPatientArray(Patient[] patientArray, String sortBy, String sortOrder) {
        if (patientArray == null || patientArray.length < 2)
            return;

        java.util.Comparator<Patient> comparator = getPatientComparator(sortBy);

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        QuickSort.sort(patientArray, comparator);
    }

    private java.util.Comparator<Patient> getPatientComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> java.util.Comparator.comparing(p -> p.getFullName() != null ? p.getFullName() : "");
            case "age" -> java.util.Comparator.comparing(Patient::getAge);
            case "gender" -> java.util.Comparator.comparing(p -> getGenderFromIC(p.getICNumber()));
            case "blood" -> java.util.Comparator.comparing(p -> p.getBloodType() != null ? p.getBloodType().toString() : "");
            case "allergies" -> java.util.Comparator.comparing(p -> p.getAllergies() != null ? p.getAllergies() : "");
            case "regdate" -> java.util.Comparator.comparing(p -> p.getRegistrationDate() != null ? p.getRegistrationDate() : LocalDate.MAX);
            case "status" -> java.util.Comparator.comparing(Patient::isActive);
            case "id" -> java.util.Comparator.comparing(p -> p.getPatientId() != null ? p.getPatientId() : "");
            default -> java.util.Comparator.comparing(p -> p.getFullName() != null ? p.getFullName() : "");
        };
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

    // Detailed table for age range (adds Age column explicitly)
    public String displayPatientsByAgeRangeDetailed(int minAge, int maxAge, String sortBy, String sortOrder) {
        ArrayBucketList<String, Patient> patients = findPatientsByAgeRange(minAge, maxAge);
        if (patients.isEmpty()) {
            return "No patients found.";
        }

        Patient[] patientArray = patients.toArray(Patient.class);
        sortPatientArray(patientArray, sortBy, sortOrder);

        StringBuilder result = new StringBuilder();
        result.append("\n=== Patient Search Results ===\n");
        result.append("Search Criteria: Age Range: ").append(minAge).append(" - ").append(maxAge).append("\n");
        result.append("Sorted By: ").append(sortBy)
                .append(" | Order: ").append(sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? "Descending" : "Ascending").append("\n");
        result.append("Search Date: ")
                .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")))
                .append("\n");
        result.append("Total Results: ").append(patients.getSize()).append(" patient(s) found\n\n");

        result.append("--- Patient List ---\n");
        result.append("-".repeat(116)).append("\n");
        result.append(String.format("| %-12s | %-25s | %-8s | %-15s | %-25s | %-12s |\n",
                "Patient ID", "Full Name", "Age", "IC Number", "Email", "Phone"));
        result.append("-".repeat(116)).append("\n");

        for (Patient patient : patientArray) {
            if (patient == null) continue;
            String id = patient.getPatientId() == null ? "-" : patient.getPatientId();
            String name = patient.getFullName() == null ? "-" : patient.getFullName();
            String ageStr = String.valueOf(patient.getAge());
            String ic = patient.getICNumber() == null ? "-" : patient.getICNumber();
            String email = patient.getEmail() == null ? "-" : patient.getEmail();
            String phone = patient.getPhoneNumber() == null ? "-" : patient.getPhoneNumber();

            if (name.length() > 25) name = name.substring(0, 22) + "...";
            if (email.length() > 25) email = email.substring(0, 22) + "...";

            result.append(String.format("| %-12s | %-25s | %-8s | %-15s | %-25s | %-12s |\n",
                    id, name, ageStr, ic, email, phone));
        }

        result.append("-".repeat(116)).append("\n");
        result.append(">>> End of Search <<<\n");
        return result.toString();
    }

    // Detailed display for recent visits (with Registration Date column)
    public String displayRecentPatientsDetailed(ArrayBucketList<String, Patient> patients, String criteria, String sortBy, String sortOrder) {
        if (patients.isEmpty()) {
            return "No patients found.";
        }

        Patient[] patientArray = patients.toArray(Patient.class);
        sortPatientArray(patientArray, sortBy, sortOrder);

        StringBuilder result = new StringBuilder();
        result.append("\n=== Patient Search Results ===\n");
        result.append("Search Criteria: ").append(criteria).append("\n");
        result.append("Sorted By: ").append(sortBy)
                .append(" | Order: ").append(sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? "Descending" : "Ascending").append("\n");
        result.append("Search Date: ")
                .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")))
                .append("\n");
        result.append("Total Results: ").append(patients.getSize()).append(" patient(s) found\n\n");

        result.append("--- Patient List ---\n");
        result.append("-".repeat(120)).append("\n");
        result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-12s |\n",
                "Patient ID", "Full Name", "IC Number", "Email", "Phone", "Reg Date"));
        result.append("-".repeat(120)).append("\n");

        for (Patient patient : patientArray) {
            if (patient == null) continue;
            String id = patient.getPatientId() == null ? "-" : patient.getPatientId();
            String name = patient.getFullName() == null ? "-" : patient.getFullName();
            String ic = patient.getICNumber() == null ? "-" : patient.getICNumber();
            String email = patient.getEmail() == null ? "-" : patient.getEmail();
            String phone = patient.getPhoneNumber() == null ? "-" : patient.getPhoneNumber();
            String regDate = patient.getRegistrationDate() == null ? "-"
                    : patient.getRegistrationDate().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-uuuu"));

            if (name.length() > 25) name = name.substring(0, 22) + "...";
            if (email.length() > 25) email = email.substring(0, 22) + "...";

            result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-12s |\n",
                    id, name, ic, email, phone, regDate));
        }

        result.append("-".repeat(120)).append("\n");
        result.append(">>> End of Search <<<\n");
        return result.toString();
    }

    // Detailed address table with separate columns
    public String displayPatientsByAddressDetailed(String keyword, String sortBy, String sortOrder) {
        ArrayBucketList<String, Patient> patients = findPatientsByAddress(keyword);
        if (patients.isEmpty()) {
            return "No patients found.";
        }

        // Convert to array and sort using the same mechanism
        Patient[] patientArray = patients.toArray(Patient.class);
        sortPatientArray(patientArray, sortBy, sortOrder);

        StringBuilder result = new StringBuilder();
        result.append("\n=== Patient Search Results ===\n");
        result.append("Search Criteria: Address contains: '")
                .append(keyword == null ? "" : keyword.trim()).append("'\n");
        result.append("Sorted By: ").append(sortBy)
                .append(" | Order: ").append(sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? "Descending" : "Ascending").append("\n");
        result.append("Search Date: ")
                .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")))
                .append("\n");
        result.append("Total Results: ").append(patients.getSize()).append(" patient(s) found\n\n");

        result.append("--- Patient List ---\n");
        result.append("-".repeat(203)).append("\n");
        result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-28s | %-18s | %-15s | %-10s | %-12s |\n",
                "Patient ID", "Full Name", "IC Number", "Email", "Phone", "Street", "City", "State", "Postcode", "Country"));
        result.append("-".repeat(203)).append("\n");

        for (Patient patient : patientArray) {
            if (patient == null) continue;
            Address addr = patient.getAddress();
            String street = addr != null && addr.getStreet() != null ? addr.getStreet() : "-";
            String city = addr != null && addr.getCity() != null ? addr.getCity() : "-";
            String state = addr != null && addr.getState() != null ? addr.getState() : "-";
            String zip = addr != null && addr.getZipCode() != null ? addr.getZipCode() : "-";
            String country = addr != null && addr.getCountry() != null ? addr.getCountry() : "-";

            String id = patient.getPatientId() == null ? "-" : patient.getPatientId();
            String name = patient.getFullName() == null ? "-" : patient.getFullName();
            String ic = patient.getICNumber() == null ? "-" : patient.getICNumber();
            String email = patient.getEmail() == null ? "-" : patient.getEmail();
            String phone = patient.getPhoneNumber() == null ? "-" : patient.getPhoneNumber();

            if (name.length() > 25) name = name.substring(0, 22) + "...";
            if (email.length() > 25) email = email.substring(0, 22) + "...";
            if (street.length() > 28) street = street.substring(0, 25) + "...";
            if (city.length() > 18) city = city.substring(0, 15) + "...";

            result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-28s | %-18s | %-12s | %-10s | %-12s |\n",
                    id, name, ic, email, phone, street, city, state, zip, country));
        }

        result.append("-".repeat(203)).append("\n");
        result.append(">>> End of Search <<<\n");
        return result.toString();
    }

    // Detailed table for blood type search (adds Blood Type column explicitly)
    public String displayPatientsByBloodTypeDetailed(BloodType bloodType, String sortBy, String sortOrder) {
        ArrayBucketList<String, Patient> patients = findPatientsByBloodType(bloodType);
        if (patients.isEmpty()) {
            return "No patients found.";
        }

        Patient[] patientArray = patients.toArray(Patient.class);
        sortPatientArray(patientArray, sortBy, sortOrder);

        StringBuilder result = new StringBuilder();
        result.append("\n=== Patient Search Results ===\n");
        result.append("Search Criteria: Blood Type: ")
                .append(bloodType == null ? "-" : bloodType.toString()).append("\n");
        result.append("Sorted By: ").append(sortBy)
                .append(" | Order: ").append(sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? "Descending" : "Ascending").append("\n");
        result.append("Search Date: ")
                .append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/uuuu HH:mm")))
                .append("\n");
        result.append("Total Results: ").append(patients.getSize()).append(" patient(s) found\n\n");

        result.append("--- Patient List ---\n");
        result.append("-".repeat(121)).append("\n");
        result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-13s |\n",
                "Patient ID", "Full Name", "IC Number", "Email", "Phone", "Blood Type"));
        result.append("-".repeat(121)).append("\n");

        for (Patient patient : patientArray) {
            if (patient == null) continue;
            String id = patient.getPatientId() == null ? "-" : patient.getPatientId();
            String name = patient.getFullName() == null ? "-" : patient.getFullName();
            String ic = patient.getICNumber() == null ? "-" : patient.getICNumber();
            String email = patient.getEmail() == null ? "-" : patient.getEmail();
            String phone = patient.getPhoneNumber() == null ? "-" : patient.getPhoneNumber();
            String blood = patient.getBloodType() == null ? "-" : patient.getBloodType().toString();

            if (name.length() > 25) name = name.substring(0, 22) + "...";
            if (email.length() > 25) email = email.substring(0, 22) + "...";

            result.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-13s |\n",
                    id, name, ic, email, phone, blood));
        }

        result.append("-".repeat(121)).append("\n");
        result.append(">>> End of Search <<<\n");
        return result.toString();
    }

    // Generates patient visit history report with last visit tracking
    public String generatePatientVisitHistoryReport(String sortBy, String sortOrder) {
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
        report.append("=".repeat(207)).append("\n");
        report.append(ConsoleUtils.centerText("PATIENT VISIT HISTORY", 207) ).append("\n");
        report.append("=".repeat(207)).append("\n");
        report.append("Total Patients: ").append(items.length).append("\n");
        report.append("Sorted By: ").append(sortBy).append(" | Order: ").append(ascending ? "Ascending" : "Descending")
                .append("\n");
        report.append("Generated: ").append(java.time.LocalDate.now()).append("\n");
        report.append("Note: Last Visit shows time since patient registration date\n\n");

        report.append("-".repeat(207)).append("\n");
        report.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-12s | %-8s | %-12s | %-33s | %-22s |\n",
                "Patient ID", "Full Name", "IC Number", "Email", "Phone", "Reg Date", "Status", "Blood Type",
                "Allergies", "Last Visit"));
        report.append("-".repeat(207)).append("\n");

        for (int itemIndex = 0; itemIndex < items.length; itemIndex++) {
            Patient p = items[itemIndex];
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

            String lastVisit;
            if (p.getRegistrationDate() != null) {
                java.time.Period period = java.time.Period.between(p.getRegistrationDate(), java.time.LocalDate.now());
                int years = period.getYears();
                int months = period.getMonths();
                
                if (years == 0 && months == 0) {
                    lastVisit = "This month";
                } else if (years == 0 && months == 1) {
                    lastVisit = "1 month ago";
                } else if (years == 0 && months > 1) {
                    lastVisit = months + " months ago";
                } else if (years == 1 && months == 0) {
                    lastVisit = "1 year ago";
                } else if (years == 1 && months > 0) {
                    lastVisit = "1 year, " + months + " month" + (months == 1 ? "" : "s") + " ago";
                } else if (years > 1 && months == 0) {
                    lastVisit = years + " years ago";
                } else {
                    lastVisit = years + " years, " + months + " month" + (months == 1 ? "" : "s") + " ago";
                }
            } else {
                lastVisit = "N/A";
            }

            report.append(String.format("| %-12s | %-25s | %-15s | %-25s | %-12s | %-12s | %-8s | %-12s | %-33s | %-22s |\n",
                    id, name, ic, email, phone, reg, status, blood, allergiesOut, lastVisit));
        }

        report.append("-".repeat(207)).append("\n");
        report.append("=".repeat(207)).append("\n");
        report.append(ConsoleUtils.centerText("END OF PATIENT VISIT HISTORY REPORT", 207)).append("\n");
        report.append("=".repeat(207)).append("\n");

        return report.toString();
    }
}
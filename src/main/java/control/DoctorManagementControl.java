package control;

import adt.ArrayBucketList;
import entity.Doctor;
import entity.Schedule;
import entity.Consultation;
import entity.DayOfWeek;
import entity.Address;
import dao.DoctorDao;
import dao.AddressDao;
import dao.ScheduleDao;
import dao.ConsultationDao;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Comparator;
import utility.QuickSort;

/**
 * @author: Lee Yong Kang
 *          Doctor Management Control - Module 2
 *          Manages doctor information, duty schedules and availability tracking
 */
public class DoctorManagementControl {

    private DoctorDao doctorDao;
    private AddressDao addressDao;
    private ScheduleDao scheduleDao;
    private ArrayBucketList<String, Doctor> activeDoctors;
    private ConsultationDao consultationDao;

    public DoctorManagementControl() {
        this.doctorDao = new DoctorDao();
        this.addressDao = new AddressDao();
        this.scheduleDao = new ScheduleDao();
        this.activeDoctors = new ArrayBucketList<String, Doctor>();
        this.consultationDao = new ConsultationDao();
        loadActiveDoctors();
    }

    public void loadActiveDoctors() {
        try {
            activeDoctors = doctorDao.findAll();
        } catch (Exception exception) {
            System.err.println("Error loading active doctors: " + exception.getMessage());
        }
    }

    // Doctor Registration Methods
    public boolean registerDoctor(String fullName, String icNumber, String email,
            String phoneNumber, Address address, String medicalSpecialty,
            String licenseNumber, int expYears) {
        try {
            // First, insert the address and get the generated address ID
            boolean addressInserted = addressDao.insertAndReturnId(address);
            if (!addressInserted) {
                System.err.println("Failed to insert address");
                return false;
            }

            // Create new doctor with the generated address ID
            Doctor doctor = new Doctor(fullName, icNumber, email, phoneNumber,
                    address, LocalDate.now(), null, medicalSpecialty,
                    licenseNumber, expYears);

            // Insert doctor and get the generated doctor ID
            boolean doctorInserted = doctorDao.insertAndReturnId(doctor);
            if (!doctorInserted) {
                System.err.println("Failed to insert doctor");
                return false;
            }

            // Add to active doctors list
            activeDoctors.add(doctor.getDoctorId(), doctor);
            return true;

        } catch (Exception exception) {
            System.err.println("Error registering doctor: " + exception.getMessage());
            return false;
        }
    }

    public boolean updateDoctorInfo(String doctorId, String fullName, String email,
            String phoneNumber, String medicalSpecialty,
            int expYears, Address address) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                if (fullName != null && !fullName.trim().isEmpty()) {
                    doctor.setFullName(fullName);
                }
                if (email != null && !email.trim().isEmpty()) {
                    doctor.setEmail(email);
                }
                if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                    doctor.setPhoneNumber(phoneNumber);
                }
                if (medicalSpecialty != null && !medicalSpecialty.trim().isEmpty()) {
                    doctor.setMedicalSpecialty(medicalSpecialty);
                }
                if (expYears >= 0) {
                    doctor.setExpYears(expYears);
                }
                // Note: expYears = -1 means no update needed (from UI)
                if (address != null) {
                    Address currentAddress = doctor.getAddress();
                    boolean addressPersisted;
                    if (currentAddress != null && currentAddress.getAddressId() != null) {
                        // Update existing address record
                        address.setAddressId(currentAddress.getAddressId());
                        addressPersisted = addressDao.update(address);
                    } else {
                        // Insert new address record and assign generated ID
                        addressPersisted = addressDao.insertAndReturnId(address);
                    }
                    if (!addressPersisted) {
                        return false;
                    }
                    doctor.setAddress(address);
                }

                boolean updated = doctorDao.update(doctor);
                if (updated) {
                    updateActiveDoctorsList(doctor);
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating doctor info: " + exception.getMessage());
            return false;
        }
    }

    public boolean deactivateDoctor(String doctorId) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                doctor.setAvailable(false);
                boolean updated = doctorDao.update(doctor);
                if (updated) {
                    removeFromActiveDoctors(doctor);
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error deactivating doctor: " + exception.getMessage());
            return false;
        }
    }

    // Schedule Management Methods
    public boolean addSchedule(String doctorId, DayOfWeek dayOfWeek,
            String startTime, String endTime) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                boolean scheduleAvailability = doctor.isAvailable();
                Schedule schedule = new Schedule(null, doctorId, dayOfWeek, startTime, endTime, scheduleAvailability);
                boolean inserted = scheduleDao.insertAndReturnId(schedule);
                if (!inserted)
                    return false;
                // keep in-memory cache in sync for reports
                doctor.addSchedule(schedule);
                updateActiveDoctorsList(doctor);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error adding schedule: " + exception.getMessage());
            return false;
        }
    }

    public boolean removeSchedule(String doctorId, String scheduleId) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                Schedule removed = doctor.removeSchedule(scheduleId);
                if (removed != null) {
                    boolean updated = doctorDao.update(doctor);
                    if (updated) {
                        updateActiveDoctorsList(doctor);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error removing schedule: " + exception.getMessage());
            return false;
        }
    }

    public ArrayBucketList<String, Schedule> getDoctorSchedules(String doctorId) {
        try {
            return scheduleDao.findByDoctorId(doctorId);
        } catch (Exception exception) {
            System.err.println("Error getting doctor schedules: " + exception.getMessage());
            return new ArrayBucketList<String, Schedule>();
        }
    }

    public ArrayBucketList<String, Schedule> getDoctorSchedulesOrdered(String doctorId) {
        try {
            ArrayBucketList<String, Schedule> schedules = scheduleDao.findByDoctorId(doctorId);
            int size = schedules.getSize();
            Schedule[] arr = new Schedule[size];
            int idx = 0;
            Iterator<Schedule> it = schedules.iterator();
            while (it.hasNext()) {
                arr[idx++] = it.next();
            }

            java.util.Comparator<Schedule> byDay = new java.util.Comparator<Schedule>() {
                @Override
                public int compare(Schedule a, Schedule b) {
                    return Integer.compare(a.getDayOfWeek().ordinal(), b.getDayOfWeek().ordinal());
                }
            };

            QuickSort.sort(arr, byDay);

            ArrayBucketList<String, Schedule> sorted = new ArrayBucketList<String, Schedule>();
            // Note: returning ArrayBucketList here will not preserve iteration order due to
            // hashing.
            // Prefer getDoctorSchedulesOrderedArray() when display order must be
            // guaranteed.
            for (int i = 0; i < arr.length; i++) {
                Schedule s = arr[i];
                sorted.add(s.getScheduleId(), s);
            }
            return sorted;
        } catch (Exception exception) {
            System.err.println("Error ordering doctor schedules: " + exception.getMessage());
            return getDoctorSchedules(doctorId);
        }
    }

    public Schedule[] getDoctorSchedulesOrderedArray(String doctorId) {
        try {
            ArrayBucketList<String, Schedule> schedules = scheduleDao.findByDoctorId(doctorId);
            int size = schedules.getSize();
            Schedule[] arr = new Schedule[size];
            int idx = 0;
            Iterator<Schedule> it = schedules.iterator();
            while (it.hasNext()) {
                arr[idx++] = it.next();
            }

            java.util.Comparator<Schedule> byDay = new java.util.Comparator<Schedule>() {
                @Override
                public int compare(Schedule a, Schedule b) {
                    return Integer.compare(a.getDayOfWeek().ordinal(), b.getDayOfWeek().ordinal());
                }
            };

            QuickSort.sort(arr, byDay);
            return arr;
        } catch (Exception exception) {
            System.err.println("Error building ordered schedule array: " + exception.getMessage());
            return new Schedule[0];
        }
    }

    // Availability Management Methods
    public boolean setDoctorAvailability(String doctorId, boolean isAvailable) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor == null)
                return false;

            boolean updated = doctorDao.updateAvailability(doctorId, isAvailable);
            if (!updated)
                return false;

            doctor.setAvailable(isAvailable);

            // Cascade schedules to match doctor availability
            ArrayBucketList<String, Schedule> schedules = getDoctorSchedules(doctorId);
            Iterator<Schedule> it = schedules.iterator();
            while (it.hasNext()) {
                Schedule s = it.next();
                try {
                    scheduleDao.updateAvailability(s.getScheduleId(), isAvailable);
                } catch (Exception e) {
                    System.err.println("Failed to update schedule availability for schedule " + s.getScheduleId() + ": "
                            + e.getMessage());
                }
            }

            updateActiveDoctorsList(doctor);
            return true;
        } catch (Exception exception) {
            System.err.println("Error setting doctor availability: " + exception.getMessage());
            return false;
        }
    }

    public boolean setScheduleAvailability(String scheduleId, boolean isAvailable) {
        try {
            return scheduleDao.updateAvailability(scheduleId, isAvailable);
        } catch (Exception exception) {
            System.err.println("Error setting schedule availability: " + exception.getMessage());
            return false;
        }
    }

    public ArrayBucketList<String, Doctor> getAvailableDoctors() {
        ArrayBucketList<String, Doctor> availableDoctors = new ArrayBucketList<String, Doctor>();
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.isAvailable()) {
                availableDoctors.add(doctor.getDoctorId(), doctor);
            }
        }
        return availableDoctors;
    }

    public ArrayBucketList<String, Doctor> getDoctorsBySpecialty(String specialty) {
        ArrayBucketList<String, Doctor> specialtyDoctors = new ArrayBucketList<String, Doctor>();
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.getMedicalSpecialty().equalsIgnoreCase(specialty) && doctor.isAvailable()) {
                specialtyDoctors.add(doctor.getDoctorId(), doctor);
            }
        }
        return specialtyDoctors;
    }

    // Search and Retrieval Methods
    public Doctor findDoctorById(String doctorId) {
        try {
            return doctorDao.findById(doctorId);
        } catch (Exception exception) {
            System.err.println("Error finding doctor by ID: " + exception.getMessage());
            return null;
        }
    }

    public ArrayBucketList<String, Doctor> findDoctorsByName(String name) {
        ArrayBucketList<String, Doctor> foundDoctors = new ArrayBucketList<String, Doctor>();
        Iterator<Doctor> iterator = getAllActiveDoctors().iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            if (doctor.getFullName() != null
                    && doctor.getFullName().toLowerCase().contains(name.toLowerCase())) {
                foundDoctors.add(doctor.getDoctorId(), doctor);
            }
        }
        return foundDoctors;
    }

    /**
     * Displays sorted doctor search results with sorting options
     */
    public String displaySortedDoctorSearchResults(ArrayBucketList<String, Doctor> doctors, String searchCriteria, String sortBy, String sortOrder) {
        if (doctors.isEmpty()) {
            return "No doctors found.";
        }

        // Convert to array for sorting
        Doctor[] doctorArray = doctors.toArray(Doctor.class);
        
        // Create comparator for sorting
        final boolean ascending = sortOrder == null || !sortOrder.equalsIgnoreCase("desc");
        java.util.Comparator<Doctor> comparator = new java.util.Comparator<Doctor>() {
            @Override
            public int compare(Doctor a, Doctor b) {
                if (a == null && b == null) return 0;
                if (a == null) return ascending ? -1 : 1;
                if (b == null) return ascending ? 1 : -1;

                int result = 0;
                String key = sortBy == null ? "name" : sortBy.toLowerCase();
                switch (key) {
                    case "id":
                        result = safeStr(a.getDoctorId()).compareToIgnoreCase(safeStr(b.getDoctorId()));
                        break;
                    case "license":
                        result = safeStr(a.getLicenseNumber()).compareToIgnoreCase(safeStr(b.getLicenseNumber()));
                        break;
                    case "specialty":
                        result = safeStr(a.getMedicalSpecialty()).compareToIgnoreCase(safeStr(b.getMedicalSpecialty()));
                        break;
                    case "experience":
                        int expA = a.getExpYears();
                        int expB = b.getExpYears();
                        result = Integer.compare(expA, expB);
                        break;
                    case "email":
                        result = safeStr(a.getEmail()).compareToIgnoreCase(safeStr(b.getEmail()));
                        break;
                    case "phone":
                        result = safeStr(a.getPhoneNumber()).compareToIgnoreCase(safeStr(b.getPhoneNumber()));
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
        utility.QuickSort.sort(doctorArray, comparator);

        StringBuilder result = new StringBuilder();
        result.append("\n=== Doctor Search Results ===\n");
        result.append("Search Criteria: ").append(searchCriteria).append("\n");
        result.append("Sorted By: ").append(sortBy).append(" | Order: ").append(ascending ? "Ascending" : "Descending").append("\n");
        result.append("Total Results: ").append(doctors.getSize()).append(" doctor(s) found\n\n");

        result.append("--- Doctor List ---\n");
        result.append("-".repeat(120)).append("\n");
        result.append(String.format("| %-12s | %-25s | %-20s | %-15s | %-12s | %-25s |\n", 
                "Doctor ID", "Full Name", "Specialty", "Experience", "License", "Email"));
        result.append("-".repeat(120)).append("\n");

        for (Doctor doctor : doctorArray) {
            if (doctor == null) continue;
            
            String id = doctor.getDoctorId() != null ? doctor.getDoctorId() : "N/A";
            String name = doctor.getFullName() != null ? doctor.getFullName() : "N/A";
            String specialty = doctor.getMedicalSpecialty() != null ? doctor.getMedicalSpecialty() : "N/A";
            String experience = doctor.getExpYears() + " years";
            String license = doctor.getLicenseNumber() != null ? doctor.getLicenseNumber() : "N/A";
            String email = doctor.getEmail() != null ? doctor.getEmail() : "N/A";

            // Truncate long names and emails
            if (name.length() > 25) name = name.substring(0, 22) + "...";
            if (specialty.length() > 20) specialty = specialty.substring(0, 17) + "...";
            if (email.length() > 25) email = email.substring(0, 22) + "...";

            result.append(String.format("| %-12s | %-25s | %-20s | %-15s | %-12s | %-25s |\n", 
                    id, name, specialty, experience, license, email));
        }

        result.append("-".repeat(120)).append("\n");
        result.append(">>> End of Search <<<\n");

        return result.toString();
    }

    private String safeStr(String str) {
        return str == null ? "" : str;
    }

    public ArrayBucketList<String, Doctor> getAllActiveDoctors() {
        return activeDoctors;
    }

    public int getTotalActiveDoctors() {
        return activeDoctors.getSize();
    }

    // Reporting Methods
    private static final int REPORT_WIDTH = 120;

    private String repeatChar(char ch, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++)
            builder.append(ch);
        return builder.toString();
    }

    private String centerText(String text, int width) {
        if (text == null)
            text = "";
        if (text.length() >= width)
            return text;
        int padding = (width - text.length()) / 2;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < padding; i++)
            builder.append(' ');
        builder.append(text);
        while (builder.length() < width)
            builder.append(' ');
        return builder.toString();
    }

    private String padRight(String text, int width) {
        if (text == null)
            text = "";
        StringBuilder builder = new StringBuilder(text);
        while (builder.length() < width)
            builder.append(' ');
        if (builder.length() > width)
            return builder.substring(0, width);
        return builder.toString();
    }

    private String padLeft(String text, int width) {
        if (text == null)
            text = "";
        StringBuilder builder = new StringBuilder();
        while (builder.length() + text.length() < width)
            builder.append(' ');
        builder.append(text);
        if (builder.length() > width)
            return builder.substring(builder.length() - width);
        return builder.toString();
    }

    public String generateDoctorInformationReport() {
        StringBuilder report = new StringBuilder();
        String title = "DOCTOR ACTIVITY REPORT";
        String line = repeatChar('=', REPORT_WIDTH);
        report.append("\n").append(line).append("\n");
        report.append(centerText(title, REPORT_WIDTH)).append("\n\n");

        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .append("\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");

        String h1 = padRight("Doctor ID", 12);
        String h2 = padRight("Name", 28);
        String h3 = padRight("Specialty", 22);
        String h4 = padRight("License", 22);
        String h5 = padRight("Consultations", 16);
        report.append(" ")
                .append(h1).append(" | ")
                .append(h2).append(" | ")
                .append(h3).append(" | ")
                .append(h4).append(" | ")
                .append(h5)
                .append("\n");
        report.append(line).append("\n");

        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            String c1 = padRight(doctor.getDoctorId(), 12);
            String c2 = padRight(doctor.getFullName(), 28);
            String c3 = padRight((doctor.getMedicalSpecialty() == null ? "" : doctor.getMedicalSpecialty()), 22);
            String c4 = padRight((doctor.getLicenseNumber() == null ? "" : doctor.getLicenseNumber()), 22);
            int consultCount = getConsultationCountForDoctor(doctor.getDoctorId());
            String c5 = padLeft(String.valueOf(consultCount), 16);
            report.append(" ")
                    .append(c1).append(" | ")
                    .append(c2).append(" | ")
                    .append(c3).append(" | ")
                    .append(c4).append(" | ")
                    .append(c5)
                    .append("\n");
        }

        report.append("\n");
        report.append("Total active doctors : ").append(getTotalActiveDoctors()).append("\n");
        report.append("Total available      : ").append(getAvailableDoctors().getSize()).append("\n");

        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        report.append(centerText("END OF THE REPORT", REPORT_WIDTH)).append("\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        return report.toString();
    }

    /**
     *
     * @param sortBy
     * @param ascending true for ascending order, false for descending.
     * @return formatted report string
     */
    public String generateDoctorInformationReport(String sortBy, boolean ascending) {
        String field = sortBy == null ? "" : sortBy.trim().toLowerCase();
        boolean doSort = field.equals("name") || field.equals("specialty") || field.equals("experience");

        StringBuilder report = new StringBuilder();
        String title = "DOCTOR ACTIVITY REPORT";
        String line = repeatChar('=', REPORT_WIDTH);
        report.append("\n").append(line).append("\n");
        report.append(centerText(title, REPORT_WIDTH)).append("\n\n");

        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .append("\n");
        report.append(line).append("\n");

        String h1 = padRight("Doctor ID", 12);
        String h2 = padRight("Name", 28);
        String h3 = padRight("Specialty", 22);
        String h4 = padRight("License", 22);
        String h5 = padRight("Consultations", 16);
        report.append(" ")
                .append(h1).append(" | ")
                .append(h2).append(" | ")
                .append(h3).append(" | ")
                .append(h4).append(" | ")
                .append(h5)
                .append("\n");
        report.append(line).append("\n");

        if (doSort) {
            int size = activeDoctors.getSize();
            Doctor[] doctors = new Doctor[size];
            int index = 0;
            Iterator<Doctor> it = activeDoctors.iterator();
            while (it.hasNext()) {
                doctors[index++] = it.next();
            }
            Comparator<Doctor> comparator = new Comparator<Doctor>() {
                @Override
                public int compare(Doctor a, Doctor b) {
                    return compareDoctors(a, b, field, ascending);
                }
            };
            QuickSort.sort(doctors, comparator);
            for (int i = 0; i < size; i++) {
                Doctor doctor = doctors[i];
                String c1 = padRight(doctor.getDoctorId(), 12);
                String c2 = padRight(doctor.getFullName(), 28);
                String c3 = padRight((doctor.getMedicalSpecialty() == null ? "" : doctor.getMedicalSpecialty()), 22);
                String c4 = padRight((doctor.getLicenseNumber() == null ? "" : doctor.getLicenseNumber()), 22);
                int consultCount = getConsultationCountForDoctor(doctor.getDoctorId());
                String c5 = padLeft(String.valueOf(consultCount), 16);
                report.append(" ")
                        .append(c1).append(" | ")
                        .append(c2).append(" | ")
                        .append(c3).append(" | ")
                        .append(c4).append(" | ")
                        .append(c5)
                        .append("\n");
            }
        } else {
            Iterator<Doctor> it = activeDoctors.iterator();
            while (it.hasNext()) {
                Doctor doctor = it.next();
                String c1 = padRight(doctor.getDoctorId(), 12);
                String c2 = padRight(doctor.getFullName(), 28);
                String c3 = padRight((doctor.getMedicalSpecialty() == null ? "" : doctor.getMedicalSpecialty()), 22);
                String c4 = padRight((doctor.getLicenseNumber() == null ? "" : doctor.getLicenseNumber()), 22);
                int consultCount = getConsultationCountForDoctor(doctor.getDoctorId());
                String c5 = padLeft(String.valueOf(consultCount), 16);
                report.append(" ")
                        .append(c1).append(" | ")
                        .append(c2).append(" | ")
                        .append(c3).append(" | ")
                        .append(c4).append(" | ")
                        .append(c5)
                        .append("\n");
            }
        }

        report.append("\n");
        report.append("Total active doctors : ").append(getTotalActiveDoctors()).append("\n");
        report.append("Total available      : ").append(getAvailableDoctors().getSize()).append("\n");

        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        report.append(centerText("END OF THE REPORT", REPORT_WIDTH)).append("\n");
        report.append(repeatChar('=', REPORT_WIDTH));
        return report.toString();
    }

    private void appendDoctorLineItems(StringBuilder report, Doctor doctor) {
        report.append("Doctor ID: ").append(doctor.getDoctorId()).append("\n");
        report.append("Name: ").append(doctor.getFullName()).append("\n");
        report.append("Specialty: ").append(doctor.getMedicalSpecialty()).append("\n");
        report.append("License: ").append(doctor.getLicenseNumber()).append("\n");
        report.append("Experience: ").append(doctor.getExpYears()).append(" years\n");
        report.append("Status: ").append(doctor.isAvailable() ? "Available" : "Unavailable").append("\n");
        report.append("----------------------------------------\n");
    }

    private int compareDoctors(Doctor a, Doctor b, String sortBy, boolean ascending) {
        int result;
        if ("name".equals(sortBy)) {
            String an = a.getFullName() == null ? "" : a.getFullName();
            String bn = b.getFullName() == null ? "" : b.getFullName();
            result = an.compareToIgnoreCase(bn);
        } else if ("specialty".equals(sortBy)) {
            String as = a.getMedicalSpecialty() == null ? "" : a.getMedicalSpecialty();
            String bs = b.getMedicalSpecialty() == null ? "" : b.getMedicalSpecialty();
            result = as.compareToIgnoreCase(bs);
        } else {
            int ae = getConsultationCountForDoctor(a.getDoctorId());
            int be = getConsultationCountForDoctor(b.getDoctorId());
            result = Integer.compare(ae, be);
        }
        if (!ascending) {
            result = -result;
        }

        if (result == 0) {
            String aid = a.getDoctorId() == null ? "" : a.getDoctorId();
            String bid = b.getDoctorId() == null ? "" : b.getDoctorId();
            result = aid.compareTo(bid);
        }
        return result;
    }

    private int getConsultationCountForDoctor(String doctorId) {
        try {
            ArrayBucketList<String, Consultation> allConsultations = consultationDao.findAll();
            int count = 0;
            Iterator<Consultation> iterator = allConsultations.iterator();
            while (iterator.hasNext()) {
                Consultation c = iterator.next();
                if (c != null && c.getDoctor() != null && doctorId.equals(c.getDoctor().getDoctorId())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("Error counting consultations for doctor " + doctorId + ": " + e.getMessage());
            return 0;
        }
    }

    public String generateDoctorWorkloadReport() {
        return generateDoctorWorkloadReport(null, true);
    }

    public String generateDoctorWorkloadReport(String sortBy, boolean ascending) {
        StringBuilder report = new StringBuilder();
        String title = "DOCTOR WORKLOAD REPORT (Estimated Annual Hours)";
        String line = repeatChar('=', REPORT_WIDTH);
        report.append(centerText(title, REPORT_WIDTH)).append("\n");
        report.append(line).append("\n\n");

        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .append("\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n\n");

        // Aggregate weekly hours per doctor from schedules in database
        adt.ArrayBucketList<String, Schedule> allSchedules;
        try {
            allSchedules = scheduleDao.findAll();
        } catch (Exception e) {
            allSchedules = new adt.ArrayBucketList<String, Schedule>();
        }

        // Totals per doctorId (weekly hours)
        adt.ArrayBucketList<String, Double> weeklyTotals = new adt.ArrayBucketList<String, Double>();
        Iterator<Schedule> it = allSchedules.iterator();
        while (it.hasNext()) {
            Schedule s = it.next();
            try {
                java.time.LocalTime from = java.time.LocalTime.parse(s.getFromTime());
                java.time.LocalTime to = java.time.LocalTime.parse(s.getToTime());
                long minutes = java.time.Duration.between(from, to).toMinutes();
                if (minutes < 0) {
                    // guard if times are inverted; skip
                    continue;
                }
                double hours = minutes / 60.0;
                Double current = weeklyTotals.getValue(s.getDoctorId());
                weeklyTotals.add(s.getDoctorId(), (current == null ? 0.0 : current) + hours);
            } catch (Exception ignore) {
                // skip error time format rows
            }
        }

        // Build rows for all doctors
        class Row {
            String doctorId;
            String name;
            String specialty;
            double weekly;
            double annual;
        }
        int docCount = activeDoctors.getSize();
        Row[] rows = new Row[docCount];
        int pos = 0;
        Iterator<Doctor> docIt = activeDoctors.iterator();
        while (docIt.hasNext()) {
            Doctor d = docIt.next();
            Row r = new Row();
            r.doctorId = d.getDoctorId();
            r.name = d.getFullName();
            r.specialty = d.getMedicalSpecialty();
            Double w = weeklyTotals.getValue(d.getDoctorId());
            r.weekly = w == null ? 0.0 : w.doubleValue();
            r.annual = r.weekly * 52.0;
            rows[pos++] = r;
        }

        // Sorting
        String field = sortBy == null ? "" : sortBy.trim().toLowerCase();
        boolean doSort = field.equals("name") || field.equals("specialty") || field.equals("weekly")
                || field.equals("annual");
        java.util.Comparator<Row> comparator = new java.util.Comparator<Row>() {
            @Override
            public int compare(Row a, Row b) {
                int result;
                if (field.equals("name")) {
                    String an = a.name == null ? "" : a.name;
                    String bn = b.name == null ? "" : b.name;
                    result = an.compareToIgnoreCase(bn);
                } else if (field.equals("specialty")) {
                    String as = a.specialty == null ? "" : a.specialty;
                    String bs = b.specialty == null ? "" : b.specialty;
                    result = as.compareToIgnoreCase(bs);
                } else if (field.equals("weekly")) {
                    result = Double.compare(a.weekly, b.weekly);
                } else if (field.equals("annual")) {
                    result = Double.compare(a.annual, b.annual);
                } else {
                    // default: annual desc
                    result = -Double.compare(a.annual, b.annual);
                }
                if (!ascending)
                    result = -result;
                // Tie-breaker by doctorId
                if (result == 0) {
                    String aid = a.doctorId == null ? "" : a.doctorId;
                    String bid = b.doctorId == null ? "" : b.doctorId;
                    result = aid.compareTo(bid);
                }
                return result;
            }
        };
        if (doSort || true) { // always sort (default annual desc if
            quickSortRows(rows, 0, pos - 1, comparator);
        }

        String h1 = padRight("Doctor ID", 12);
        String h2 = padRight("Name", 28);
        String h3 = padRight("Specialty", 22);
        String h4 = padRight("Weekly Hours", 14);
        String h5 = padRight("Annual Hours", 14);
        report.append(" ").append(h1).append(" | ").append(h2).append(" | ")
                .append(h3).append(" | ").append(h4).append(" | ").append(h5).append("\n");
        report.append(line).append("\n");

        double totalAnnual = 0.0;
        for (int i = 0; i < pos; i++) {
            Row r = rows[i];
            String c1 = padRight(r.doctorId, 12);
            String c2 = padRight(r.name == null ? "" : r.name, 28);
            String c3 = padRight(r.specialty == null ? "" : r.specialty, 22);
            String c4 = padLeft(String.format(java.util.Locale.US, "%.2f", r.weekly), 14);
            String c5 = padLeft(String.format(java.util.Locale.US, "%.2f", r.annual), 14);
            report.append(" ").append(c1).append(" | ").append(c2).append(" | ")
                    .append(c3).append(" | ").append(c4).append(" | ").append(c5).append("\n");
            totalAnnual += r.annual;
        }

        report.append("\nTotal doctors : ").append(docCount).append("\n");
        report.append("Total annual hours (all doctors) : ")
                .append(String.format(java.util.Locale.US, "%.2f", totalAnnual)).append("\n\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        report.append(centerText("END OF THE REPORT", REPORT_WIDTH)).append("\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        return report.toString();
    }

    // Quicksort helper for Row[] using comparator
    private void quickSortRows(Object[] arr, int low, int high, java.util.Comparator comparator) {
        if (low >= high)
            return;
        int i = low, j = high;
        Object pivot = arr[low + (high - low) / 2];
        while (i <= j) {
            while (comparator.compare(arr[i], pivot) < 0)
                i++;
            while (comparator.compare(arr[j], pivot) > 0)
                j--;
            if (i <= j) {
                Object tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }
        if (low < j)
            quickSortRows(arr, low, j, comparator);
        if (i < high)
            quickSortRows(arr, i, high, comparator);
    }

    private void updateActiveDoctorsList(Doctor updatedDoctor) {
        boolean found = false;
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.getDoctorId().equals(updatedDoctor.getDoctorId())) {
                activeDoctors.remove(doctor.getDoctorId());
                activeDoctors.add(updatedDoctor.getDoctorId(), updatedDoctor);
                found = true;
                break;
            }
        }
        if (!found) {
            activeDoctors.add(updatedDoctor.getDoctorId(), updatedDoctor);
        }
    }

    private void removeFromActiveDoctors(Doctor doctor) {
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor currentDoctor = doctorIterator.next();
            if (currentDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                activeDoctors.remove(currentDoctor.getDoctorId());
                break;
            }
        }
    }
}
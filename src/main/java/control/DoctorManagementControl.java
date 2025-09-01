package control;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;
import adt.ArrayBucketList;
import dao.AddressDao;
import dao.ConsultationDao;
import dao.DoctorDao;
import dao.ScheduleDao;
import entity.Address;
import entity.Consultation;
import entity.DayOfWeek;
import entity.Doctor;
import entity.Schedule;
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

    public boolean updateSchedule(String scheduleId, DayOfWeek dayOfWeek, String startTime, String endTime) {
        try {
            Schedule schedule = scheduleDao.findById(scheduleId);
            if (schedule != null) {
                schedule.setDayOfWeek(dayOfWeek);
                schedule.setFromTime(startTime);
                schedule.setToTime(endTime);
                boolean updated = scheduleDao.update(schedule);
                if (updated) {
                    // Update the doctor's in-memory schedule list
                    Doctor doctor = doctorDao.findById(schedule.getDoctorId());
                    if (doctor != null) {
                        doctor.updateSchedule(schedule);
                        updateActiveDoctorsList(doctor);
                    }
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating schedule: " + exception.getMessage());
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

            // Sort the array to ensure proper day-of-week ordering
            java.util.Comparator<Schedule> byDay = new java.util.Comparator<Schedule>() {
                @Override
                public int compare(Schedule firstSchedule, Schedule secondSchedule) {
                    if (firstSchedule == null && secondSchedule == null) return 0;
                    if (firstSchedule == null) return -1;
                    if (secondSchedule == null) return 1;
                    return Integer.compare(firstSchedule.getDayOfWeek().ordinal(), secondSchedule.getDayOfWeek().ordinal());
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
            Iterator<Schedule> scheduleIterator = schedules.iterator();
            while (scheduleIterator.hasNext()) {
                Schedule schedule = scheduleIterator.next();
                try {
                    scheduleDao.updateAvailability(schedule.getScheduleId(), isAvailable);
                    // Update in-memory schedule availability if doctor holds schedules
                    Schedule inMem = doctor.getSchedule(schedule.getScheduleId());
                    if (inMem != null) {
                        inMem.setAvailable(isAvailable);
                        doctor.updateSchedule(inMem);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to update schedule availability for schedule " + schedule.getScheduleId() + ": "
                            + e.getMessage());
                }
            }
            // Reflect activeDoctors cache: remove when unavailable, update/add when available
            if (isAvailable) {
                updateActiveDoctorsList(doctor);
            } else {
                removeFromActiveDoctors(doctor);
            }
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
            if (doctor.getMedicalSpecialty() != null
                    && doctor.getMedicalSpecialty().toLowerCase().contains(specialty.toLowerCase()) 
                    && doctor.isAvailable()) {
                specialtyDoctors.add(doctor.getDoctorId(), doctor);
            }
        }
        return specialtyDoctors;
    }

    // Search and Retrieval Methods
    public Doctor findDoctorById(String doctorId) {
        Doctor foundDoctor = null;
        Iterator<Doctor> iterator = getAllActiveDoctors().iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            if (doctor.getDoctorId().equals(doctorId)) {
                foundDoctor = doctor;
            }
        }
        return foundDoctor;
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

    public Doctor findDoctorByIcNumber(String icNumber) {
        return activeDoctors.getValue(icNumber);
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
            public int compare(Doctor firstDoctor, Doctor secondDoctor) {
                if (firstDoctor == null && secondDoctor == null) return 0;
                if (firstDoctor == null) return ascending ? -1 : 1;
                if (secondDoctor == null) return ascending ? 1 : -1;

                int result = 0;
                String key = sortBy == null ? "name" : sortBy.toLowerCase();
                switch (key) {
                    case "id":
                        result = safeStr(firstDoctor.getDoctorId()).compareToIgnoreCase(safeStr(secondDoctor.getDoctorId()));
                        break;
                    case "license":
                        result = safeStr(firstDoctor.getLicenseNumber()).compareToIgnoreCase(safeStr(secondDoctor.getLicenseNumber()));
                        break;
                    case "specialty":
                        result = safeStr(firstDoctor.getMedicalSpecialty()).compareToIgnoreCase(safeStr(secondDoctor.getMedicalSpecialty()));
                        break;
                    case "experience":
                        int firstExperience = firstDoctor.getExpYears();
                        int secondExperience = secondDoctor.getExpYears();
                        result = Integer.compare(firstExperience, secondExperience);
                        break;
                    case "email":
                        result = safeStr(firstDoctor.getEmail()).compareToIgnoreCase(safeStr(secondDoctor.getEmail()));
                        break;
                    case "phone":
                        result = safeStr(firstDoctor.getPhoneNumber()).compareToIgnoreCase(safeStr(secondDoctor.getPhoneNumber()));
                        break;
                    case "name":
                    default:
                        result = safeStr(firstDoctor.getFullName()).compareToIgnoreCase(safeStr(secondDoctor.getFullName()));
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
        for (int charIndex = 0; charIndex < count; charIndex++)
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
        for (int paddingIndex = 0; paddingIndex < padding; paddingIndex++)
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

        String doctorIdHeader = padRight("Doctor ID", 12);
        String nameHeader = padRight("Name", 28);
        String specialtyHeader = padRight("Specialty", 22);
        String licenseHeader = padRight("License", 22);
        String consultationHeader = padRight("Consultations", 16);
        report.append(" ")
                .append(doctorIdHeader).append(" | ")
                .append(nameHeader).append(" | ")
                .append(specialtyHeader).append(" | ")
                .append(licenseHeader).append(" | ")
                .append(consultationHeader)
                .append("\n");
        report.append(line).append("\n");

        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            String doctorIdColumn = padRight(doctor.getDoctorId(), 12);
            String nameColumn = padRight(doctor.getFullName(), 28);
            String specialtyColumn = padRight((doctor.getMedicalSpecialty() == null ? "" : doctor.getMedicalSpecialty()), 22);
            String licenseColumn = padRight((doctor.getLicenseNumber() == null ? "" : doctor.getLicenseNumber()), 22);
            int consultCount = getConsultationCountForDoctor(doctor.getDoctorId());
            String consultationColumn = padLeft(String.valueOf(consultCount), 16);
            report.append(" ")
                    .append(doctorIdColumn).append(" | ")
                    .append(nameColumn).append(" | ")
                    .append(specialtyColumn).append(" | ")
                    .append(licenseColumn).append(" | ")
                    .append(consultationColumn)
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
                public int compare(Doctor firstDoctor, Doctor secondDoctor) {
                    return compareDoctors(firstDoctor, secondDoctor, field, ascending);
                }
            };
            QuickSort.sort(doctors, comparator);
            for (int doctorIndex = 0; doctorIndex < size; doctorIndex++) {
                Doctor doctor = doctors[doctorIndex];
                String doctorIdColumn = padRight(doctor.getDoctorId(), 12);
                String nameColumn = padRight(doctor.getFullName(), 28);
                String specialtyColumn = padRight((doctor.getMedicalSpecialty() == null ? "" : doctor.getMedicalSpecialty()), 22);
                String licenseColumn = padRight((doctor.getLicenseNumber() == null ? "" : doctor.getLicenseNumber()), 22);
                int consultCount = getConsultationCountForDoctor(doctor.getDoctorId());
                String consultationColumn = padLeft(String.valueOf(consultCount), 16);
                report.append(" ")
                        .append(doctorIdColumn).append(" | ")
                        .append(nameColumn).append(" | ")
                        .append(specialtyColumn).append(" | ")
                        .append(licenseColumn).append(" | ")
                        .append(consultationColumn)
                        .append("\n");
            }
        } else {
            Iterator<Doctor> doctorIterator = activeDoctors.iterator();
            while (doctorIterator.hasNext()) {
                Doctor doctor = doctorIterator.next();
                String doctorIdColumn = padRight(doctor.getDoctorId(), 12);
                String nameColumn = padRight(doctor.getFullName(), 28);
                String specialtyColumn = padRight((doctor.getMedicalSpecialty() == null ? "" : doctor.getMedicalSpecialty()), 22);
                String licenseColumn = padRight((doctor.getLicenseNumber() == null ? "" : doctor.getLicenseNumber()), 22);
                int consultCount = getConsultationCountForDoctor(doctor.getDoctorId());
                String consultationColumn = padLeft(String.valueOf(consultCount), 16);
                report.append(" ")
                        .append(doctorIdColumn).append(" | ")
                        .append(nameColumn).append(" | ")
                        .append(specialtyColumn).append(" | ")
                        .append(licenseColumn).append(" | ")
                        .append(consultationColumn)
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

    private int compareDoctors(Doctor firstDoctor, Doctor secondDoctor, String sortBy, boolean ascending) {
        int result;
        if ("name".equals(sortBy)) {
            String firstName = firstDoctor.getFullName() == null ? "" : firstDoctor.getFullName();
            String secondName = secondDoctor.getFullName() == null ? "" : secondDoctor.getFullName();
            result = firstName.compareToIgnoreCase(secondName);
        } else if ("specialty".equals(sortBy)) {
            String firstSpecialty = firstDoctor.getMedicalSpecialty() == null ? "" : firstDoctor.getMedicalSpecialty();
            String secondSpecialty = secondDoctor.getMedicalSpecialty() == null ? "" : secondDoctor.getMedicalSpecialty();
            result = firstSpecialty.compareToIgnoreCase(secondSpecialty);
        } else {
            int firstConsultationCount = getConsultationCountForDoctor(firstDoctor.getDoctorId());
            int secondConsultationCount = getConsultationCountForDoctor(secondDoctor.getDoctorId());
            result = Integer.compare(firstConsultationCount, secondConsultationCount);
        }
        if (!ascending) {
            result = -result;
        }

        if (result == 0) {
            String firstId = firstDoctor.getDoctorId() == null ? "" : firstDoctor.getDoctorId();
            String secondId = secondDoctor.getDoctorId() == null ? "" : secondDoctor.getDoctorId();
            result = firstId.compareTo(secondId);
        }
        return result;
    }

    private int getConsultationCountForDoctor(String doctorId) {
        try {
            ArrayBucketList<String, Consultation> allConsultations = consultationDao.findAll();
            int count = 0;
            Iterator<Consultation> iterator = allConsultations.iterator();
            while (iterator.hasNext()) {
                Consultation consultation = iterator.next();
                if (consultation != null && consultation.getDoctor() != null && doctorId.equals(consultation.getDoctor().getDoctorId())) {
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
        Iterator<Schedule> scheduleIterator = allSchedules.iterator();
        while (scheduleIterator.hasNext()) {
            Schedule schedule = scheduleIterator.next();
            try {
                java.time.LocalTime from = java.time.LocalTime.parse(schedule.getFromTime());
                java.time.LocalTime to = java.time.LocalTime.parse(schedule.getToTime());
                long minutes = java.time.Duration.between(from, to).toMinutes();
                if (minutes < 0) {
                    // guard if times are inverted; skip
                    continue;
                }
                double hours = minutes / 60.0;
                Double current = weeklyTotals.getValue(schedule.getDoctorId());
                weeklyTotals.add(schedule.getDoctorId(), (current == null ? 0.0 : current) + hours);
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
        int position = 0;
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            Row row = new Row();
            row.doctorId = doctor.getDoctorId();
            row.name = doctor.getFullName();
            row.specialty = doctor.getMedicalSpecialty();
            Double weeklyHours = weeklyTotals.getValue(doctor.getDoctorId());
            row.weekly = weeklyHours == null ? 0.0 : weeklyHours.doubleValue();
            row.annual = row.weekly * 52.0;
            rows[position++] = row;
        }

        // Sorting
        String field = sortBy == null ? "" : sortBy.trim().toLowerCase();
        boolean doSort = field.equals("name") || field.equals("specialty") || field.equals("weekly")
                || field.equals("annual");
        java.util.Comparator<Row> comparator = new java.util.Comparator<Row>() {
            @Override
            public int compare(Row firstRow, Row secondRow) {
                int result;
                if (field.equals("name")) {
                    String firstName = firstRow.name == null ? "" : firstRow.name;
                    String secondName = secondRow.name == null ? "" : secondRow.name;
                    result = firstName.compareToIgnoreCase(secondName);
                } else if (field.equals("specialty")) {
                    String firstSpecialty = firstRow.specialty == null ? "" : firstRow.specialty;
                    String secondSpecialty = secondRow.specialty == null ? "" : secondRow.specialty;
                    result = firstSpecialty.compareToIgnoreCase(secondSpecialty);
                } else if (field.equals("weekly")) {
                    result = Double.compare(firstRow.weekly, secondRow.weekly);
                } else if (field.equals("annual")) {
                    result = Double.compare(firstRow.annual, secondRow.annual);
                } else {
                    // default: annual desc
                    result = -Double.compare(firstRow.annual, secondRow.annual);
                }
                if (!ascending)
                    result = -result;
                // Tie-breaker by doctorId
                if (result == 0) {
                    String firstId = firstRow.doctorId == null ? "" : firstRow.doctorId;
                    String secondId = secondRow.doctorId == null ? "" : secondRow.doctorId;
                    result = firstId.compareTo(secondId);
                }
                return result;
            }
        };
        if (doSort || true) { // always sort (default annual desc if
            quickSortRows(rows, 0, position - 1, comparator);
        }

        String doctorIdHeader = padRight("Doctor ID", 12);
        String nameHeader = padRight("Name", 28);
        String specialtyHeader = padRight("Specialty", 22);
        String weeklyHeader = padRight("Weekly Hours", 14);
        String annualHeader = padRight("Annual Hours", 14);
        report.append(" ").append(doctorIdHeader).append(" | ").append(nameHeader).append(" | ")
                .append(specialtyHeader).append(" | ").append(weeklyHeader).append(" | ").append(annualHeader).append("\n");
        report.append(line).append("\n");

        double totalAnnual = 0.0;
        for (int doctorIndex = 0; doctorIndex < position; doctorIndex++) {
            Row row = rows[doctorIndex];
            String doctorIdColumn = padRight(row.doctorId, 12);
            String nameColumn = padRight(row.name == null ? "" : row.name, 28);
            String specialtyColumn = padRight(row.specialty == null ? "" : row.specialty, 22);
            String weeklyColumn = padLeft(String.format(java.util.Locale.US, "%.2f", row.weekly), 14);
            String annualColumn = padLeft(String.format(java.util.Locale.US, "%.2f", row.annual), 14);
            report.append(" ").append(doctorIdColumn).append(" | ").append(nameColumn).append(" | ")
                    .append(specialtyColumn).append(" | ").append(weeklyColumn).append(" | ").append(annualColumn).append("\n");
            totalAnnual += row.annual;
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
        int leftIndex = low, rightIndex = high;
        Object pivot = arr[low + (high - low) / 2];
        while (leftIndex <= rightIndex) {
            while (comparator.compare(arr[leftIndex], pivot) < 0)
                leftIndex++;
            while (comparator.compare(arr[rightIndex], pivot) > 0)
                rightIndex--;
            if (leftIndex <= rightIndex) {
                Object tmp = arr[leftIndex];
                arr[leftIndex] = arr[rightIndex];
                arr[rightIndex] = tmp;
                leftIndex++;
                rightIndex--;
            }
        }
        if (low < rightIndex)
            quickSortRows(arr, low, rightIndex, comparator);
        if (leftIndex < high)
            quickSortRows(arr, leftIndex, high, comparator);
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

    /**
     * Generates a comprehensive doctor performance report
     * @param sortBy field to sort by
     * @param ascending true for ascending order, false for descending
     * @return formatted report string
     */
    public String generateDoctorPerformanceReport(String sortBy, boolean ascending) {
        final int TABLEWIDTH = 138;
        StringBuilder report = new StringBuilder();
        String title = "DOCTOR PERFORMANCE ANALYSIS REPORT";
        String line = repeatChar('=', TABLEWIDTH);
        report.append("\n").append(line).append("\n");
        report.append(centerText(title, TABLEWIDTH)).append("\n\n");

        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .append("\n");
        report.append(repeatChar('=', TABLEWIDTH)).append("\n\n");

        // Performance metrics calculation
        report.append("-".repeat(TABLEWIDTH)).append("\n");
        report.append(centerText("PERFORMANCE METRICS SUMMARY", TABLEWIDTH)).append("\n");
        report.append("-".repeat(TABLEWIDTH)).append("\n");

        // Calculate performance metrics for each doctor
        String[] doctorIds = new String[activeDoctors.getSize()];
        String[] doctorNames = new String[activeDoctors.getSize()];
        int[] consultationCounts = new int[activeDoctors.getSize()];
        double[] successRates = new double[activeDoctors.getSize()];
        double[] averagePatientSatisfaction = new double[activeDoctors.getSize()];
        int[] completedTreatments = new int[activeDoctors.getSize()];
        double[] totalRevenue = new double[activeDoctors.getSize()];

        int doctorIndex = 0;
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            doctorIds[doctorIndex] = doctor.getDoctorId();
            doctorNames[doctorIndex] = doctor.getFullName();
            
            // Get consultation count
            consultationCounts[doctorIndex] = getConsultationCountForDoctor(doctor.getDoctorId());
            
            // Calculate success rate (simulated - in real system would be based on treatment outcomes)
            successRates[doctorIndex] = Math.min(95.0, 70.0 + (consultationCounts[doctorIndex] * 0.5));
            
            // Calculate patient satisfaction (simulated - in real system would be based on feedback)
            averagePatientSatisfaction[doctorIndex] = Math.min(5.0, 3.5 + (consultationCounts[doctorIndex] * 0.02));
            
            // Calculate completed treatments (simulated)
            completedTreatments[doctorIndex] = (int) (consultationCounts[doctorIndex] * 0.8);
            
            // Calculate total revenue (consultation fees)
            totalRevenue[doctorIndex] = consultationCounts[doctorIndex] * 50.0; // Assuming RM50 per consultation
            
            doctorIndex++;
        }

        report.append(String.format("Total Active Doctors: %d\n", getTotalActiveDoctors()));
        report.append(String.format("Average Consultations per Doctor: %.1f\n", 
                calculateAverage(consultationCounts)));
        report.append(String.format("Average Success Rate: %.1f%%\n", 
                calculateAverage(successRates)));
        report.append(String.format("Average Patient Satisfaction: %.1f/5.0\n", 
                calculateAverage(averagePatientSatisfaction)));

        // Top performers analysis
        report.append("\nTOP PERFORMERS BY CONSULTATIONS:\n");
        int[] consultationCountsCopy = consultationCounts.clone();
        int[] topConsultationIndices = getTopIndices(consultationCountsCopy, 3);
        for (int rankIndex = 0; rankIndex < topConsultationIndices.length; rankIndex++) {
            int index = topConsultationIndices[rankIndex];
            report.append(String.format("%d. %s: %d consultations\n", 
                    rankIndex + 1, doctorNames[index], consultationCounts[index]));
        }

        report.append("\nTOP PERFORMERS BY SUCCESS RATE:\n");
        double[] successRatesCopy = successRates.clone();
        int[] topSuccessIndices = getTopIndices(successRatesCopy, 3);
        for (int rankIndex = 0; rankIndex < topSuccessIndices.length; rankIndex++) {
            int index = topSuccessIndices[rankIndex];
            report.append(String.format("%d. %s: %.1f%%\n", 
                    rankIndex + 1, doctorNames[index], successRates[index]));
        }

        report.append("\nTOP PERFORMERS BY PATIENT SATISFACTION:\n");
        double[] satisfactionCopy = averagePatientSatisfaction.clone();
        int[] topSatisfactionIndices = getTopIndices(satisfactionCopy, 3);
        for (int rankIndex = 0; rankIndex < topSatisfactionIndices.length; rankIndex++) {
            int index = topSatisfactionIndices[rankIndex];
            report.append(String.format("%d. %s: %.1f/5.0\n", 
                    rankIndex + 1, doctorNames[index], averagePatientSatisfaction[index]));
        }

        // Specialty performance analysis
        report.append("\nSPECIALTY PERFORMANCE ANALYSIS:\n");
        String[] specialties = new String[20];
        int[] specialtyCounts = new int[20];
        double[] specialtySuccessRates = new double[20];
        int specialtyCount = 0;

        doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            String specialty = doctor.getMedicalSpecialty() != null ? doctor.getMedicalSpecialty() : "General";
            
            // Find if specialty already exists
            int specialtyIndex = -1;
            for (int specialtyCounter = 0; specialtyCounter < specialtyCount; specialtyCounter++) {
                if (specialties[specialtyCounter].equals(specialty)) {
                    specialtyIndex = specialtyCounter;
                    break;
                }
            }
            
            if (specialtyIndex == -1) {
                specialties[specialtyCount] = specialty;
                specialtyCounts[specialtyCount] = 1;
                specialtySuccessRates[specialtyCount] = successRates[doctorIndex - 1];
                specialtyCount++;
            } else {
                specialtyCounts[specialtyIndex]++;
                specialtySuccessRates[specialtyIndex] = (specialtySuccessRates[specialtyIndex] + successRates[doctorIndex]) / 2;
            }
        }

        for (int specialtyCounter = 0; specialtyCounter < specialtyCount; specialtyCounter++) {
            report.append(String.format("%-20s: %d doctors, %.1f%% avg success rate\n", 
                    specialties[specialtyCounter], specialtyCounts[specialtyCounter], specialtySuccessRates[specialtyCounter]));
        }

        report.append("-".repeat(TABLEWIDTH)).append("\n\n");

        // Detailed performance table
        report.append(centerText("DETAILED DOCTOR PERFORMANCE", TABLEWIDTH)).append("\n");
        report.append("-".repeat(TABLEWIDTH)).append("\n");

        // Add sorting information
        String sortField = getSortFieldDisplayName(sortBy);
        String sortOrder = ascending ? "ASCENDING" : "DESCENDING";
        report.append(String.format("Sorted by: %s (%s order)\n\n", sortField, sortOrder));

        String doctorIdHeader = padRight("Doctor ID", 12);
        String nameHeader = padRight("Name", 25);
        String specialtyHeader = padRight("Specialty", 20);
        String consultationHeader = padRight("Consultations", 15);
        String successRateHeader = padRight("Success Rate", 15);
        String satisfactionHeader = padRight("Satisfaction", 15);
        String revenueHeader = padRight("Revenue", 15);
        report.append(" ")
                .append(doctorIdHeader).append(" | ")
                .append(nameHeader).append(" | ")
                .append(specialtyHeader).append(" | ")
                .append(consultationHeader).append(" | ")
                .append(successRateHeader).append(" | ")
                .append(satisfactionHeader).append(" | ")
                .append(revenueHeader)
                .append("\n");

        
        report.append(repeatChar('=', TABLEWIDTH)).append("\n");

        // Create array for sorting
        Doctor[] doctorArray = new Doctor[activeDoctors.getSize()];
        int index = 0;
        doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            doctorArray[index++] = doctorIterator.next();
        }

        // Sort the doctor array
        sortDoctorArray(doctorArray, sortBy, ascending);

        // Generate sorted table
        for (Doctor doctor : doctorArray) {
            // Find doctor index for metrics
            int doctorIdx = -1;
            for (int doctorCounter = 0; doctorCounter < doctorIds.length; doctorCounter++) {
                if (doctorIds[doctorCounter].equals(doctor.getDoctorId())) {
                    doctorIdx = doctorCounter;
                    break;
                }
            }

            if (doctorIdx == -1) continue;

            String doctorIdColumn = padRight(doctor.getDoctorId(), 12);
            String nameColumn = padRight(doctor.getFullName(), 25);
            String specialtyColumn = padRight(doctor.getMedicalSpecialty() != null ? doctor.getMedicalSpecialty() : "General", 20);
            String consultationColumn = padLeft(String.valueOf(consultationCounts[doctorIdx]), 15);
            String successRateColumn = padLeft(String.format("%.1f%%", successRates[doctorIdx]), 15);
            String satisfactionColumn = padLeft(String.format("%.1f/5.0", averagePatientSatisfaction[doctorIdx]), 15);
            String revenueColumn = padLeft(String.format("RM %.2f", totalRevenue[doctorIdx]), 15);

            report.append(" ")
                    .append(doctorIdColumn).append(" | ")
                    .append(nameColumn).append(" | ")
                    .append(specialtyColumn).append(" | ")
                    .append(consultationColumn).append(" | ")
                    .append(successRateColumn).append(" | ")
                    .append(satisfactionColumn).append(" | ")
                    .append(revenueColumn)
                    .append("\n");
        }

        report.append("\n");
        report.append(repeatChar('=', TABLEWIDTH)).append("\n");
        report.append(centerText("END OF PERFORMANCE REPORT", TABLEWIDTH)).append("\n");
        report.append(repeatChar('=', TABLEWIDTH)).append("\n");
        return report.toString();
    }

    // Helper methods for performance report
    private double calculateAverage(int[] values) {
        if (values.length == 0) return 0.0;
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return (double) sum / values.length;
    }

    private double calculateAverage(double[] values) {
        if (values.length == 0) return 0.0;
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private int[] getTopIndices(int[] values, int count) {
        int[] indices = new int[Math.min(count, values.length)];
        for (int rankIndex = 0; rankIndex < indices.length; rankIndex++) {
            int maxIndex = 0;
            for (int searchIndex = 1; searchIndex < values.length; searchIndex++) {
                if (values[searchIndex] > values[maxIndex]) {
                    maxIndex = searchIndex;
                }
            }
            indices[rankIndex] = maxIndex;
            values[maxIndex] = -1; // Mark as used
        }
        return indices;
    }

    private int[] getTopIndices(double[] values, int count) {
        int[] indices = new int[Math.min(count, values.length)];
        double[] tempValues = values.clone();
        for (int rankIndex = 0; rankIndex < indices.length; rankIndex++) {
            int maxIndex = 0;
            for (int searchIndex = 1; searchIndex < tempValues.length; searchIndex++) {
                if (tempValues[searchIndex] > tempValues[maxIndex]) {
                    maxIndex = searchIndex;
                }
            }
            indices[rankIndex] = maxIndex;
            tempValues[maxIndex] = -1; // Mark as used
        }
        return indices;
    }

    private String getSortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> "Doctor Name";
            case "specialty" -> "Medical Specialty";
            case "consultations" -> "Consultation Count";
            case "success" -> "Success Rate";
            case "satisfaction" -> "Patient Satisfaction";
            case "revenue" -> "Total Revenue";
            case "id" -> "Doctor ID";
            default -> "Default";
        };
    }

    private void sortDoctorArray(Doctor[] doctorArray, String sortBy, boolean ascending) {
        if (doctorArray == null || doctorArray.length < 2) return;

        Comparator<Doctor> comparator = getDoctorPerformanceComparator(sortBy);
        if (!ascending) {
            comparator = comparator.reversed();
        }

        // Use QuickSort for sorting
        utility.QuickSort.sort(doctorArray, comparator);
    }

    private Comparator<Doctor> getDoctorPerformanceComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> Comparator.comparing(Doctor::getFullName);
            case "specialty" -> Comparator.comparing(d -> d.getMedicalSpecialty() != null ? d.getMedicalSpecialty() : "");
            case "consultations" -> Comparator.comparing(d -> getConsultationCountForDoctor(d.getDoctorId()));
            case "success" -> Comparator.comparing(d -> getSuccessRateForDoctor(d.getDoctorId()));
            case "satisfaction" -> Comparator.comparing(d -> getSatisfactionRateForDoctor(d.getDoctorId()));
            case "revenue" -> Comparator.comparing(d -> getRevenueForDoctor(d.getDoctorId()));
            case "id" -> Comparator.comparing(Doctor::getDoctorId);
            default -> Comparator.comparing(Doctor::getFullName);
        };
    }

    // Helper methods for performance metrics (simulated)
    private double getSuccessRateForDoctor(String doctorId) {
        int consultations = getConsultationCountForDoctor(doctorId);
        return Math.min(95.0, 70.0 + (consultations * 0.5));
    }

    private double getSatisfactionRateForDoctor(String doctorId) {
        int consultations = getConsultationCountForDoctor(doctorId);
        return Math.min(5.0, 3.5 + (consultations * 0.02));
    }

    private double getRevenueForDoctor(String doctorId) {
        int consultations = getConsultationCountForDoctor(doctorId);
        return consultations * 50.0; // RM50 per consultation
    }
}
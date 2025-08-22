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
 * Doctor Management Control - Module 2
 * Manages doctor information, duty schedules and availability tracking
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
                if (!inserted) return false;
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
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                ArrayBucketList<String, Schedule> schedules = new ArrayBucketList<String, Schedule>();
                Iterator<Schedule> scheduleIterator = doctor.getSchedules().iterator();
                while (scheduleIterator.hasNext()) {
                    Schedule schedule = scheduleIterator.next();
                    schedules.add(schedule.getScheduleId(), schedule);
                }
                return schedules;
            }
            return new ArrayBucketList<String, Schedule>();
        } catch (Exception exception) {
            System.err.println("Error getting doctor schedules: " + exception.getMessage());
            return new ArrayBucketList<String, Schedule>();
        }
    }
    
    // Availability Management Methods
    public boolean setDoctorAvailability(String doctorId, boolean isAvailable) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                doctor.setAvailable(isAvailable);
                boolean updated = doctorDao.update(doctor);
                if (updated) {
                    updateActiveDoctorsList(doctor);
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error setting doctor availability: " + exception.getMessage());
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
        ArrayBucketList<String, Doctor> results = new ArrayBucketList<String, Doctor>();
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(doctor.getDoctorId(), doctor);
            }
        }
        return results;
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
        for (int i = 0; i < count; i++) builder.append(ch);
        return builder.toString();
    }

    private String centerText(String text, int width) {
        if (text == null) text = "";
        if (text.length() >= width) return text;
        int padding = (width - text.length()) / 2;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < padding; i++) builder.append(' ');
        builder.append(text);
        while (builder.length() < width) builder.append(' ');
        return builder.toString();
    }

    private String padRight(String text, int width) {
        if (text == null) text = "";
        StringBuilder builder = new StringBuilder(text);
        while (builder.length() < width) builder.append(' ');
        if (builder.length() > width) return builder.substring(0, width);
        return builder.toString();
    }

    private String padLeft(String text, int width) {
        if (text == null) text = "";
        StringBuilder builder = new StringBuilder();
        while (builder.length() + text.length() < width) builder.append(' ');
        builder.append(text);
        if (builder.length() > width) return builder.substring(builder.length() - width);
        return builder.toString();
    }

    public String generateDoctorInformationReport() {
        StringBuilder report = new StringBuilder();
        String title = "DOCTOR INFORMATION REPORT";
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
     * Generates the Doctor Information Report with optional quick sort.
     *
     * @param sortBy    Accepts "name", "specialty", or "experience" (case-insensitive). Any other value disables sorting.
     * @param ascending true for ascending order, false for descending.
     * @return formatted report string
     */
    public String generateDoctorInformationReport(String sortBy, boolean ascending) {
        String field = sortBy == null ? "" : sortBy.trim().toLowerCase();
        boolean doSort = field.equals("name") || field.equals("specialty") || field.equals("experience");

        StringBuilder report = new StringBuilder();
        String title = "DOCTOR INFORMATION REPORT";
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
        } else { // "experience" now maps to consultation count in the report context
            int ae = getConsultationCountForDoctor(a.getDoctorId());
            int be = getConsultationCountForDoctor(b.getDoctorId());
            result = Integer.compare(ae, be);
        }
        if (!ascending) {
            result = -result;
        }
        // Tie-breaker by Doctor ID to make ordering deterministic
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

    
    
    public String generateScheduleReport() {
        StringBuilder report = new StringBuilder();
        String title = "DOCTOR SCHEDULE REPORT";
        String line = repeatChar('=', REPORT_WIDTH);
        report.append(centerText(title, REPORT_WIDTH)).append("\n");
        report.append(line).append("\n\n");

        report.append("Generated at: ")
            .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
            .append("\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n\n");

        String h1 = padRight("Doctor", 30);
        String h2 = padRight("Specialty", 26);
        String h3 = padRight("Day", 14);
        String h4 = padRight("From", 12);
        String h5 = padRight("To", 12);
        report.append(" " + h1 + "| " + h2 + "| " + h3 + "| " + h4 + "| " + h5 + "\n");
        report.append(line).append("\n");

        int totalSchedules = 0;
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            Iterator<Schedule> scheduleIterator = doctor.getSchedules().iterator();
            while (scheduleIterator.hasNext()) {
                Schedule schedule = scheduleIterator.next();
                String c1 = padRight(doctor.getFullName(), 30);
                String c2 = padRight(doctor.getMedicalSpecialty(), 26);
                String c3 = padRight(String.valueOf(schedule.getDayOfWeek()), 14);
                String c4 = padRight(schedule.getFromTime(), 12);
                String c5 = padRight(schedule.getToTime(), 12);
                report.append(" ").append(c1).append("| ").append(c2).append("| ")
                      .append(c3).append("| ").append(c4).append("| ").append(c5).append("\n");
                totalSchedules++;
            }
        }

        report.append("\nTotal schedules listed : ").append(totalSchedules).append("\n\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        report.append(centerText("END OF THE REPORT", REPORT_WIDTH)).append("\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        return report.toString();
    }
    
    private void updateActiveDoctorsList(Doctor updatedDoctor) {
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.getDoctorId().equals(updatedDoctor.getDoctorId())) {
                // Remove old entry and add updated one
                activeDoctors.remove(doctor.getDoctorId());
                activeDoctors.add(updatedDoctor.getDoctorId(), updatedDoctor);
                break;
            }
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
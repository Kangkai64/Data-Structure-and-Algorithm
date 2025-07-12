package control;

import adt.ArrayList;
import entity.Doctor;
import entity.Schedule;
import entity.DayOfWeek;
import entity.Address;
import dao.DoctorDao;
import java.util.Date;

/**
 * Doctor Management Control - Module 2
 * Manages doctor information, duty schedules and availability tracking
 */
public class DoctorManagementControl {
    
    private DoctorDao doctorDao;
    private ArrayList<Doctor> activeDoctors;
    
    public DoctorManagementControl() {
        this.doctorDao = new DoctorDao();
        this.activeDoctors = new ArrayList<>();
        loadActiveDoctors();
    }
    
    // Doctor Registration Methods
    public boolean registerDoctor(String fullName, String icNumber, String email, 
                                String phoneNumber, Address address, String medicalSpecialty,
                                String licenseNumber, int expYears) {
        try {
            // Generate doctor ID
            String doctorId = generateDoctorId();
            
            // Create new doctor
            Doctor doctor = new Doctor(fullName, icNumber, email, phoneNumber, 
                                     address, new Date(), doctorId, medicalSpecialty, 
                                     licenseNumber, expYears);
            
            // Save to database
            boolean saved = doctorDao.insert(doctor);
            if (saved) {
                activeDoctors.add(doctor);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error registering doctor: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean updateDoctorInfo(String doctorId, String fullName, String email, 
                                  String phoneNumber, Address address, String medicalSpecialty,
                                  int expYears) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                doctor.setFullName(fullName);
                doctor.setEmail(email);
                doctor.setPhoneNumber(phoneNumber);
                doctor.setAddress(address);
                doctor.setMedicalSpecialty(medicalSpecialty);
                doctor.setExpYears(expYears);
                
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
                String scheduleId = "SCH" + System.currentTimeMillis();
                Schedule schedule = new Schedule(scheduleId, doctorId, dayOfWeek, startTime, endTime);
                boolean added = doctor.addSchedule(schedule);
                if (added) {
                    boolean updated = doctorDao.update(doctor);
                    if (updated) {
                        updateActiveDoctorsList(doctor);
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error adding schedule: " + exception.getMessage());
            return false;
        }
    }
    
    public boolean removeSchedule(String doctorId, int schedulePosition) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                Schedule removed = doctor.removeSchedule(schedulePosition);
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
    
    public ArrayList<Schedule> getDoctorSchedules(String doctorId) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                ArrayList<Schedule> schedules = new ArrayList<>();
                for (int index = 1; index <= doctor.getNumberOfSchedule(); index++) {
                    schedules.add(doctor.getSchedule(index));
                }
                return schedules;
            }
            return new ArrayList<>();
        } catch (Exception exception) {
            System.err.println("Error getting doctor schedules: " + exception.getMessage());
            return new ArrayList<>();
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
    
    public ArrayList<Doctor> getAvailableDoctors() {
        ArrayList<Doctor> availableDoctors = new ArrayList<>();
        for (int index = 1; index <= activeDoctors.getNumberOfEntries(); index++) {
            Doctor doctor = activeDoctors.getEntry(index);
            if (doctor.isAvailable()) {
                availableDoctors.add(doctor);
            }
        }
        return availableDoctors;
    }
    
    public ArrayList<Doctor> getDoctorsBySpecialty(String specialty) {
        ArrayList<Doctor> specialtyDoctors = new ArrayList<>();
        for (int index = 1; index <= activeDoctors.getNumberOfEntries(); index++) {
            Doctor doctor = activeDoctors.getEntry(index);
            if (doctor.getMedicalSpecialty().equalsIgnoreCase(specialty) && doctor.isAvailable()) {
                specialtyDoctors.add(doctor);
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
    
    public ArrayList<Doctor> findDoctorsByName(String name) {
        ArrayList<Doctor> results = new ArrayList<>();
        for (int index = 1; index <= activeDoctors.getNumberOfEntries(); index++) {
            Doctor doctor = activeDoctors.getEntry(index);
            if (doctor.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(doctor);
            }
        }
        return results;
    }
    
    public ArrayList<Doctor> getAllActiveDoctors() {
        return activeDoctors;
    }
    
    public int getTotalActiveDoctors() {
        return activeDoctors.getNumberOfEntries();
    }
    
    // Reporting Methods
    public String generateDoctorInformationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== DOCTOR INFORMATION REPORT ===\n");
        report.append("Total Active Doctors: ").append(getTotalActiveDoctors()).append("\n");
        report.append("Available Doctors: ").append(getAvailableDoctors().getNumberOfEntries()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= activeDoctors.getNumberOfEntries(); index++) {
            Doctor doctor = activeDoctors.getEntry(index);
            report.append("Doctor ID: ").append(doctor.getDoctorId()).append("\n");
            report.append("Name: ").append(doctor.getFullName()).append("\n");
            report.append("Specialty: ").append(doctor.getMedicalSpecialty()).append("\n");
            report.append("License: ").append(doctor.getLicenseNumber()).append("\n");
            report.append("Experience: ").append(doctor.getExpYears()).append(" years\n");
            report.append("Availability: ").append(doctor.isAvailable() ? "Available" : "Unavailable").append("\n");
            report.append("Schedules: ").append(doctor.getNumberOfSchedule()).append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public String generateScheduleReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== DOCTOR SCHEDULE REPORT ===\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        for (int index = 1; index <= activeDoctors.getNumberOfEntries(); index++) {
            Doctor doctor = activeDoctors.getEntry(index);
            report.append("Doctor: ").append(doctor.getFullName()).append(" (").append(doctor.getDoctorId()).append(")\n");
            report.append("Specialty: ").append(doctor.getMedicalSpecialty()).append("\n");
            
            if (doctor.getNumberOfSchedule() > 0) {
                for (int scheduleIndex = 1; scheduleIndex <= doctor.getNumberOfSchedule(); scheduleIndex++) {
                    Schedule schedule = doctor.getSchedule(scheduleIndex);
                    report.append("  - ").append(schedule.getDayOfWeek()).append(": ")
                          .append(schedule.getFromTime()).append(" - ").append(schedule.getToTime()).append("\n");
                }
            } else {
                report.append("  No schedules assigned\n");
            }
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    // Private Helper Methods
    private String generateDoctorId() {
        int nextNumber = getTotalActiveDoctors() + 1;
        return String.format("D%09d", nextNumber);
    }
    
    private void loadActiveDoctors() {
        try {
            ArrayList<Doctor> allDoctors = doctorDao.findAll();
            for (int index = 1; index <= allDoctors.getNumberOfEntries(); index++) {
                Doctor doctor = allDoctors.getEntry(index);
                if (doctor.isAvailable()) {
                    activeDoctors.add(doctor);
                }
            }
        } catch (Exception exception) {
            System.err.println("Error loading active doctors: " + exception.getMessage());
        }
    }
    
    private void updateActiveDoctorsList(Doctor updatedDoctor) {
        for (int index = 1; index <= activeDoctors.getNumberOfEntries(); index++) {
            Doctor doctor = activeDoctors.getEntry(index);
            if (doctor.getDoctorId().equals(updatedDoctor.getDoctorId())) {
                activeDoctors.replace(index, updatedDoctor);
                break;
            }
        }
    }
    
    private void removeFromActiveDoctors(Doctor doctor) {
        for (int index = 1; index <= activeDoctors.getNumberOfEntries(); index++) {
            Doctor currentDoctor = activeDoctors.getEntry(index);
            if (currentDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                activeDoctors.remove(index);
                break;
            }
        }
    }
} 
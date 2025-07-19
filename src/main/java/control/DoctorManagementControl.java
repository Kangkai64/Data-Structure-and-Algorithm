package control;

import adt.ArrayBucketList;
import entity.Doctor;
import entity.Schedule;
import entity.DayOfWeek;
import entity.Address;
import dao.DoctorDao;
import java.util.Date;
import java.util.Iterator;

/**
 * @author: Lee Yong Kang
 * Doctor Management Control - Module 2
 * Manages doctor information, duty schedules and availability tracking
 */
public class DoctorManagementControl {
    
    private DoctorDao doctorDao;
    private ArrayBucketList<Doctor> activeDoctors;
    
    public DoctorManagementControl() {
        this.doctorDao = new DoctorDao();
        this.activeDoctors = new ArrayBucketList<>();
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
            // Get new doctor ID from database
            String doctorId = doctorDao.getNewId();
            
            // Create new doctor
            Doctor doctor = new Doctor(fullName, icNumber, email, phoneNumber, 
                                     address, new Date(), doctorId, medicalSpecialty, 
                                     licenseNumber, expYears);
            
            // Save to database
            boolean saved = doctorDao.insert(doctor);
            if (saved) {
                activeDoctors.add(doctor.hashCode(), doctor);
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
                Schedule schedule = new Schedule(scheduleId, doctorId, dayOfWeek, startTime, endTime, true);
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
    
    public ArrayBucketList<Schedule> getDoctorSchedules(String doctorId) {
        try {
            Doctor doctor = doctorDao.findById(doctorId);
            if (doctor != null) {
                ArrayBucketList<Schedule> schedules = new ArrayBucketList<>();
                for (int index = 1; index <= doctor.getNumberOfSchedule(); index++) {
                    schedules.add(doctor.getSchedule(index).hashCode(), doctor.getSchedule(index));
                }
                return schedules;
            }
            return new ArrayBucketList<>();
        } catch (Exception exception) {
            System.err.println("Error getting doctor schedules: " + exception.getMessage());
            return new ArrayBucketList<>();
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
    
    public ArrayBucketList<Doctor> getAvailableDoctors() {
        ArrayBucketList<Doctor> availableDoctors = new ArrayBucketList<>();
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.isAvailable()) {
                availableDoctors.add(doctor.hashCode(), doctor);
            }
        }
        return availableDoctors;
    }
    
    public ArrayBucketList<Doctor> getDoctorsBySpecialty(String specialty) {
        ArrayBucketList<Doctor> specialtyDoctors = new ArrayBucketList<>();
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.getMedicalSpecialty().equalsIgnoreCase(specialty) && doctor.isAvailable()) {
                specialtyDoctors.add(doctor.hashCode(), doctor);
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
    
    public ArrayBucketList<Doctor> findDoctorsByName(String name) {
        ArrayBucketList<Doctor> results = new ArrayBucketList<>();
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.getFullName().toLowerCase().contains(name.toLowerCase())) {
                results.add(doctor.hashCode(), doctor);
            }
        }
        return results;
    }
    
    public ArrayBucketList<Doctor> getAllActiveDoctors() {
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
        
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            report.append("Doctor ID: ").append(doctor.getDoctorId()).append("\n");
            report.append("Name: ").append(doctor.getFullName()).append("\n");
            report.append("Specialty: ").append(doctor.getMedicalSpecialty()).append("\n");
            report.append("License: ").append(doctor.getLicenseNumber()).append("\n");
            report.append("Experience: ").append(doctor.getExpYears()).append(" years\n");
            report.append("Status: ").append(doctor.isAvailable() ? "Available" : "Unavailable").append("\n");
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    public String generateScheduleReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== DOCTOR SCHEDULE REPORT ===\n");
        report.append("Total Active Doctors: ").append(getTotalActiveDoctors()).append("\n");
        report.append("Report Generated: ").append(new Date()).append("\n\n");
        
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            report.append("Doctor: ").append(doctor.getFullName()).append("\n");
            report.append("Specialty: ").append(doctor.getMedicalSpecialty()).append("\n");
            report.append("Schedules:\n");
            
            for (int index = 1; index <= doctor.getNumberOfSchedule(); index++) {
                Schedule schedule = doctor.getSchedule(index);
                report.append("  - ").append(schedule.getDayOfWeek()).append(": ")
                      .append(schedule.getFromTime()).append(" - ").append(schedule.getToTime()).append("\n");
            }
            report.append("----------------------------------------\n");
        }
        
        return report.toString();
    }
    
    private void updateActiveDoctorsList(Doctor updatedDoctor) {
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.getDoctorId().equals(updatedDoctor.getDoctorId())) {
                // Remove old entry and add updated one
                activeDoctors.remove(doctor);
                activeDoctors.add(updatedDoctor.hashCode(), updatedDoctor);
                break;
            }
        }
    }
    
    private void removeFromActiveDoctors(Doctor doctor) {
        Iterator<Doctor> doctorIterator = activeDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor currentDoctor = doctorIterator.next();
            if (currentDoctor.getDoctorId().equals(doctor.getDoctorId())) {
                activeDoctors.remove(currentDoctor);
                break;
            }
        }
    }
} 
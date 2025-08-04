package control;

import adt.ArrayBucketList;
import entity.Doctor;
import entity.Schedule;
import entity.DayOfWeek;
import entity.Address;
import dao.DoctorDao;
import dao.AddressDao;
import java.util.Date;
import java.util.Iterator;

/**
 * @author: Lee Yong Kang
 * Doctor Management Control - Module 2
 * Manages doctor information, duty schedules and availability tracking
 */
public class DoctorManagementControl {
    
    private DoctorDao doctorDao;
    private AddressDao addressDao;
    private ArrayBucketList<String, Doctor> activeDoctors;
    
    public DoctorManagementControl() {
        this.doctorDao = new DoctorDao();
        this.addressDao = new AddressDao();
        this.activeDoctors = new ArrayBucketList<String, Doctor>();
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
                                     address, new Date(), null, medicalSpecialty, 
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
    public String generateDoctorInformationReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== DOCTOR INFORMATION REPORT ===\n");
        report.append("Total Active Doctors: ").append(getTotalActiveDoctors()).append("\n");
        report.append("Available Doctors: ").append(getAvailableDoctors().getSize()).append("\n");
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
            
            Iterator<Schedule> scheduleIterator = doctor.getSchedules().iterator();
            while (scheduleIterator.hasNext()) {
                Schedule schedule = scheduleIterator.next();
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
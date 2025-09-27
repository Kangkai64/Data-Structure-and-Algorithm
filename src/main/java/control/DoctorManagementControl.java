package control;

import adt.ArrayBucketList;
import adt.ArrayBucketListFactory;
import adt.IndexingUtility;
import utility.ConsoleUtils;
import entity.Doctor;
import entity.Address;
import entity.Schedule;
import entity.Consultation;
import entity.DayOfWeek;
import dao.DoctorDao;
import dao.AddressDao;
import dao.ScheduleDao;
import dao.ConsultationDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;

import utility.QuickSort;

/**
 * @author: Lee Yong Kang
 *          Doctor Management Control - Module 2
 *          Manages doctor information, duty schedules and availability tracking
 */
public class DoctorManagementControl {

    private ArrayBucketList<String, Doctor> doctors;
    private ArrayBucketList<String, Doctor> activeDoctors;
    private ArrayBucketList<String, Doctor> inactiveDoctors;
    
    // Indices for efficient searching
    private ArrayBucketList<String, Doctor> doctorIndexById;
    private ArrayBucketList<String, ArrayBucketList<String, Doctor>> doctorIndexByName;
    private ArrayBucketList<String, ArrayBucketList<String, Doctor>> doctorIndexBySpecialty;
    private ArrayBucketList<String, ArrayBucketList<String, Doctor>> doctorIndexByLicense;
    private ArrayBucketList<String, ArrayBucketList<String, Doctor>> doctorIndexByEmail;
    private ArrayBucketList<String, ArrayBucketList<String, Doctor>> doctorIndexByPhone;
    private ArrayBucketList<String, ArrayBucketList<String, Doctor>> doctorIndexByIcNumber;
    private ArrayBucketList<Boolean, ArrayBucketList<String, Doctor>> doctorIndexByAvailability;
    private ArrayBucketList<Integer, ArrayBucketList<String, Doctor>> doctorIndexByExperience;

    private DoctorDao doctorDao;
    private AddressDao addressDao;
    private ScheduleDao scheduleDao;
    private ConsultationDao consultationDao;

    public DoctorManagementControl() {
        this.doctors = new ArrayBucketList<String, Doctor>();
        this.activeDoctors = new ArrayBucketList<String, Doctor>();
        this.inactiveDoctors = new ArrayBucketList<String, Doctor>();
        
        // Initialize indices
        this.doctorIndexById = ArrayBucketListFactory.createForStringIds(256);
        this.doctorIndexByName = ArrayBucketListFactory.createForNamePrefix(26);
        this.doctorIndexBySpecialty = ArrayBucketListFactory.createForNamePrefix(26);
        this.doctorIndexByLicense = ArrayBucketListFactory.createForStringIds(128);
        this.doctorIndexByEmail = ArrayBucketListFactory.createForNamePrefix(26);
        this.doctorIndexByPhone = ArrayBucketListFactory.createForStringIds(128);
        this.doctorIndexByIcNumber = ArrayBucketListFactory.createForStringIds(128);
        this.doctorIndexByAvailability = new ArrayBucketList<>();
        this.doctorIndexByExperience = new ArrayBucketList<>();
        
        this.doctorDao = new DoctorDao();
        this.addressDao = new AddressDao();
        this.scheduleDao = new ScheduleDao();
        this.consultationDao = new ConsultationDao();
    }

    public void loadDoctorData() {
        try {
            doctors = doctorDao.findAll();
            categorizeDoctors();
            
            // Build indices
            Iterator<Doctor> doctorIterator = doctors.iterator();
            while (doctorIterator.hasNext()) {
                Doctor doctor = doctorIterator.next();
                doctorIndexById.add(doctor.getDoctorId(), doctor);
                indexDoctor(doctor);
            }
        } catch (Exception exception) {
            System.err.println("Error loading doctor data: " + exception.getMessage());
        }
    }

    /**
     * Categorize doctors into status-specific collections for efficient
     * in-memory processing
     */
    private void categorizeDoctors() {
        // Clear existing categorized collections
        activeDoctors.clear();
        inactiveDoctors.clear();

        Iterator<Doctor> doctorIterator = doctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            if (doctor.isAvailable()) {
                activeDoctors.add(doctor.getDoctorId(), doctor);
            } else {
                inactiveDoctors.add(doctor.getDoctorId(), doctor);
            }
        }
    }

    // Indexing helpers
    private void indexDoctor(Doctor doctor) {
        if (doctor == null) {
            return;
        }
        IndexingUtility.addToIndexGroup(doctorIndexByName, doctor.getFullName(), doctor.getDoctorId(), doctor);
        IndexingUtility.addToIndexGroup(doctorIndexBySpecialty, doctor.getMedicalSpecialty(), doctor.getDoctorId(), doctor);
        IndexingUtility.addToIndexGroup(doctorIndexByLicense, doctor.getLicenseNumber(), doctor.getDoctorId(), doctor);
        IndexingUtility.addToIndexGroup(doctorIndexByEmail, doctor.getEmail(), doctor.getDoctorId(), doctor);
        IndexingUtility.addToIndexGroup(doctorIndexByPhone, doctor.getPhoneNumber(), doctor.getDoctorId(), doctor);
        IndexingUtility.addToIndexGroup(doctorIndexByIcNumber, doctor.getICNumber(), doctor.getDoctorId(), doctor);
        IndexingUtility.addToIndexGroup(doctorIndexByAvailability, doctor.isAvailable(), doctor.getDoctorId(), doctor);
        IndexingUtility.addToIndexGroup(doctorIndexByExperience, doctor.getExpYears(), doctor.getDoctorId(), doctor);
    }

    private void reindexDoctor(Doctor oldDoctor, Doctor newDoctor) {
        if (newDoctor == null) {
            return;
        }
        if (oldDoctor != null) {
            IndexingUtility.removeFromIndexGroup(doctorIndexByName, oldDoctor.getFullName(), oldDoctor.getDoctorId());
            IndexingUtility.removeFromIndexGroup(doctorIndexBySpecialty, oldDoctor.getMedicalSpecialty(), oldDoctor.getDoctorId());
            IndexingUtility.removeFromIndexGroup(doctorIndexByLicense, oldDoctor.getLicenseNumber(), oldDoctor.getDoctorId());
            IndexingUtility.removeFromIndexGroup(doctorIndexByEmail, oldDoctor.getEmail(), oldDoctor.getDoctorId());
            IndexingUtility.removeFromIndexGroup(doctorIndexByPhone, oldDoctor.getPhoneNumber(), oldDoctor.getDoctorId());
            IndexingUtility.removeFromIndexGroup(doctorIndexByIcNumber, oldDoctor.getICNumber(), oldDoctor.getDoctorId());
            IndexingUtility.removeFromIndexGroup(doctorIndexByAvailability, oldDoctor.isAvailable(), oldDoctor.getDoctorId());
            IndexingUtility.removeFromIndexGroup(doctorIndexByExperience, oldDoctor.getExpYears(), oldDoctor.getDoctorId());
        }
        indexDoctor(newDoctor);
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

            // Add to collections and indices
            doctors.add(doctor.getDoctorId(), doctor);
            if (doctor.isAvailable()) {
            activeDoctors.add(doctor.getDoctorId(), doctor);
            } else {
                inactiveDoctors.add(doctor.getDoctorId(), doctor);
            }
            doctorIndexById.add(doctor.getDoctorId(), doctor);
            indexDoctor(doctor);

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
            Doctor oldDoctor = findDoctorById(doctorId);
            if (oldDoctor == null) {
                return false;
            }
            
            // Create updated doctor from existing one
            Doctor doctor = new Doctor(
                fullName != null && !fullName.trim().isEmpty() ? fullName : oldDoctor.getFullName(),
                oldDoctor.getICNumber(),
                email != null && !email.trim().isEmpty() ? email : oldDoctor.getEmail(),
                phoneNumber != null && !phoneNumber.trim().isEmpty() ? phoneNumber : oldDoctor.getPhoneNumber(),
                address != null ? address : oldDoctor.getAddress(),
                oldDoctor.getRegistrationDate(),
                oldDoctor.getDoctorId(),
                medicalSpecialty != null && !medicalSpecialty.trim().isEmpty() ? medicalSpecialty : oldDoctor.getMedicalSpecialty(),
                oldDoctor.getLicenseNumber(),
                expYears >= 0 ? expYears : oldDoctor.getExpYears()
            );
            doctor.setAvailable(oldDoctor.isAvailable());

            // Update address if provided
            if (address != null) {
                Address currentAddress = oldDoctor.getAddress();
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

            // Update in database
            boolean updated = doctorDao.update(doctor);
            if (updated) {
                // Update in-memory collections
                doctors.add(doctor.getDoctorId(), doctor);
                categorizeDoctors();
                doctorIndexById.add(doctor.getDoctorId(), doctor);
                reindexDoctor(oldDoctor, doctor);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating doctor info: " + exception.getMessage());
            return false;
        }
    }

    public boolean deactivateDoctor(String doctorId) {
        try {
            Doctor oldDoctor = findDoctorById(doctorId);
            if (oldDoctor == null) {
                return false;
            }
            
            // Create updated doctor with availability set to false
            Doctor doctor = new Doctor(
                oldDoctor.getFullName(),
                oldDoctor.getICNumber(),
                oldDoctor.getEmail(),
                oldDoctor.getPhoneNumber(),
                oldDoctor.getAddress(),
                oldDoctor.getRegistrationDate(),
                oldDoctor.getDoctorId(),
                oldDoctor.getMedicalSpecialty(),
                oldDoctor.getLicenseNumber(),
                oldDoctor.getExpYears()
            );
            doctor.setAvailable(false);

            // Update in database
            boolean updated = doctorDao.update(doctor);
            if (updated) {
                // Update in-memory collections
                doctors.add(doctor.getDoctorId(), doctor);
                categorizeDoctors();
                doctorIndexById.add(doctor.getDoctorId(), doctor);
                reindexDoctor(oldDoctor, doctor);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error deactivating doctor: " + exception.getMessage());
            return false;
        }
    }

    // Search and Retrieval Methods
    public Doctor findDoctorById(String doctorId) {
        return doctorIndexById.getValue(doctorId);
    }

    public ArrayBucketList<String, Doctor> findDoctorsByName(String name) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(16);
        if (name == null || name.trim().isEmpty()) {
            return results;
        }
        String query = name.trim();
        
        ArrayBucketList<String, ArrayBucketList<String, Doctor>> groups = doctorIndexByName.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Doctor>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Doctor> group = groupIterator.next();
            Iterator<Doctor> iterator = group.iterator();
            while (iterator.hasNext()) {
                Doctor doctor = iterator.next();
                results.add(doctor.getDoctorId(), doctor);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Doctor> findDoctorsBySpecialty(String specialty) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(16);
        if (specialty == null || specialty.trim().isEmpty()) {
            return results;
        }
        String query = specialty.trim();
        
        ArrayBucketList<String, ArrayBucketList<String, Doctor>> groups = doctorIndexBySpecialty.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Doctor>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Doctor> group = groupIterator.next();
            Iterator<Doctor> iterator = group.iterator();
            while (iterator.hasNext()) {
                Doctor doctor = iterator.next();
                results.add(doctor.getDoctorId(), doctor);
            }
        }
        return results;
    }
    
    public ArrayBucketList<String, Doctor> findDoctorsByLicense(String licenseNumber) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(16);
        if (licenseNumber == null || licenseNumber.trim().isEmpty()) {
            return results;
        }
        String query = licenseNumber.trim();
        
        ArrayBucketList<String, ArrayBucketList<String, Doctor>> groups = doctorIndexByLicense.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Doctor>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Doctor> group = groupIterator.next();
            Iterator<Doctor> iterator = group.iterator();
            while (iterator.hasNext()) {
                Doctor doctor = iterator.next();
                results.add(doctor.getDoctorId(), doctor);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Doctor> findDoctorsByEmail(String email) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(16);
        if (email == null || email.trim().isEmpty()) {
            return results;
        }
        String query = email.trim();
        
        ArrayBucketList<String, ArrayBucketList<String, Doctor>> groups = doctorIndexByEmail.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Doctor>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Doctor> group = groupIterator.next();
            Iterator<Doctor> iterator = group.iterator();
            while (iterator.hasNext()) {
                Doctor doctor = iterator.next();
                results.add(doctor.getDoctorId(), doctor);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Doctor> findDoctorsByPhone(String phoneNumber) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(16);
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return results;
        }
        String query = phoneNumber.trim();
        
        ArrayBucketList<String, ArrayBucketList<String, Doctor>> groups = doctorIndexByPhone.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, Doctor>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Doctor> group = groupIterator.next();
            Iterator<Doctor> iterator = group.iterator();
            while (iterator.hasNext()) {
                Doctor doctor = iterator.next();
                results.add(doctor.getDoctorId(), doctor);
            }
        }
        return results;
    }

    public ArrayBucketList<String, Doctor> findDoctorsByAvailability(boolean isAvailable) {
        ArrayBucketList<String, Doctor> group = doctorIndexByAvailability.getValue(isAvailable);
        return group != null ? group : new ArrayBucketList<String, Doctor>();
    }

    public ArrayBucketList<String, Doctor> findDoctorsByExperience(int minYears) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(16);
        if (minYears < 0) {
            return results;
        }
        
        // Use the experience index for efficient range queries
        Iterator<ArrayBucketList<String, Doctor>> groupIterator = doctorIndexByExperience.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Doctor> group = groupIterator.next();
            // Get the experience level for this group by checking the first doctor
            Iterator<Doctor> doctorIterator = group.iterator();
            if (doctorIterator.hasNext()) {
                Doctor firstDoctor = doctorIterator.next();
                int groupExperience = firstDoctor.getExpYears();
                
                // If this group's experience level meets the minimum requirement
                if (groupExperience >= minYears) {
                    // Add all doctors from this group
                    Iterator<Doctor> groupDoctorIterator = group.iterator();
                    while (groupDoctorIterator.hasNext()) {
                        Doctor doctor = groupDoctorIterator.next();
                        results.add(doctor.getDoctorId(), doctor);
                    }
                }
            }
        }
        return results;
    }

    public ArrayBucketList<String, Doctor> findDoctorsByExperienceRange(int minYears, int maxYears) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(16);
        if (minYears < 0 || maxYears < 0 || minYears > maxYears) {
            return results;
        }
        
        // Use the experience index for efficient range queries
        Iterator<ArrayBucketList<String, Doctor>> groupIterator = doctorIndexByExperience.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, Doctor> group = groupIterator.next();
            // Get the experience level for this group by checking the first doctor
            Iterator<Doctor> doctorIterator = group.iterator();
            if (doctorIterator.hasNext()) {
                Doctor firstDoctor = doctorIterator.next();
                int groupExperience = firstDoctor.getExpYears();
                
                // If this group's experience level is within the range
                if (groupExperience >= minYears && groupExperience <= maxYears) {
                    // Add all doctors from this group
                    Iterator<Doctor> groupDoctorIterator = group.iterator();
                    while (groupDoctorIterator.hasNext()) {
                        Doctor doctor = groupDoctorIterator.next();
                        results.add(doctor.getDoctorId(), doctor);
                    }
                }
            }
        }
        return results;
    }

    public ArrayBucketList<String, Doctor> getInactiveDoctors() {
        return inactiveDoctors;
    }

    public ArrayBucketList<String, Doctor> getAllDoctors() {
        return doctors;
    }

    public int getTotalDoctors() {
        return doctors.getSize();
    }

    public int getActiveDoctorsCount() {
        return activeDoctors.getSize();
    }

    public int getInactiveDoctorsCount() {
        return inactiveDoctors.getSize();
    }

    // Display Methods for Search Results
    public String displaySortedDoctorSearchResults(ArrayBucketList<String, Doctor> doctors, String searchCriteria, String sortBy, String sortOrder) {
        if (doctors == null || doctors.isEmpty()) {
            return "No doctors found.";
        }

        Doctor[] doctorArray = doctors.toArray(Doctor.class);

        Comparator<Doctor> comparator = getDoctorComparator(sortBy);
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        utility.QuickSort.sort(doctorArray, comparator);

        StringBuilder result = new StringBuilder();
        result.append("\n=== Doctor Search Results ===\n");
        result.append("Criteria: ").append(searchCriteria).append("\n");
        result.append(String.format("Sorted by: %s (%s)\n\n", getSortFieldDisplayName(sortBy),
                (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) ? "DESC" : "ASC"));
        result.append(String.format("%-12s | %-25s | %-20s | %-15s | %-12s | %-25s | %-15s\n",
                "Doctor ID", "Full Name", "Specialty", "Experience", "License", "Email", "Phone"));
        result.append("-".repeat(130)).append("\n");

        for (Doctor doctor : doctorArray) {
            if (doctor == null) continue;
            
            String id = doctor.getDoctorId() != null ? doctor.getDoctorId() : "N/A";
            String name = doctor.getFullName() != null ? doctor.getFullName() : "N/A";
            String specialty = doctor.getMedicalSpecialty() != null ? doctor.getMedicalSpecialty() : "N/A";
            String experience = doctor.getExpYears() + " years";
            String license = doctor.getLicenseNumber() != null ? doctor.getLicenseNumber() : "N/A";
            String email = doctor.getEmail() != null ? doctor.getEmail() : "N/A";
            String phone = doctor.getPhoneNumber() != null ? doctor.getPhoneNumber() : "N/A";

            // Truncate long names and emails
            if (name.length() > 25) name = name.substring(0, 22) + "...";
            if (specialty.length() > 20) specialty = specialty.substring(0, 17) + "...";
            if (email.length() > 25) email = email.substring(0, 22) + "...";
            if (phone.length() > 15) phone = phone.substring(0, 12) + "...";

            result.append(String.format("%-12s | %-25s | %-20s | %-15s | %-12s | %-25s | %-15s\n",
                    id, name, specialty, experience, license, email, phone));
        }

        result.append("-".repeat(130)).append("\n");
        result.append("Total Results: ").append(doctors.getSize()).append(" doctor(s) found\n");
        return result.toString();
    }

    private String getSortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "id" -> "Doctor ID";
            case "name" -> "Full Name";
            case "specialty" -> "Medical Specialty";
            case "experience" -> "Experience Years";
            case "license" -> "License Number";
            case "email" -> "Email Address";
            case "phone" -> "Phone Number";
            case "availability" -> "Availability Status";
            default -> "Default";
        };
    }

    private Comparator<Doctor> getDoctorComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "id" -> Comparator.comparing(d -> d.getDoctorId() != null ? d.getDoctorId() : "");
            case "name" -> Comparator.comparing(d -> d.getFullName() != null ? d.getFullName() : "");
            case "specialty" -> Comparator.comparing(d -> d.getMedicalSpecialty() != null ? d.getMedicalSpecialty() : "");
            case "experience" -> Comparator.comparing(Doctor::getExpYears);
            case "license" -> Comparator.comparing(d -> d.getLicenseNumber() != null ? d.getLicenseNumber() : "");
            case "email" -> Comparator.comparing(d -> d.getEmail() != null ? d.getEmail() : "");
            case "phone" -> Comparator.comparing(d -> d.getPhoneNumber() != null ? d.getPhoneNumber() : "");
            case "availability" -> Comparator.comparing(Doctor::isAvailable);
            default -> Comparator.comparing(d -> d.getFullName() != null ? d.getFullName() : "");
        };
    }

    // Schedule Management Methods
    public boolean addSchedule(String doctorId, DayOfWeek dayOfWeek,
            String startTime, String endTime) {
        try {
            Doctor doctor = findDoctorById(doctorId);
            if (doctor != null) {
                boolean scheduleAvailability = doctor.isAvailable();
                Schedule schedule = new Schedule(null, doctorId, dayOfWeek, startTime, endTime, scheduleAvailability);
                boolean inserted = scheduleDao.insertAndReturnId(schedule);
                if (!inserted)
                    return false;
                // keep in-memory cache in sync for reports
                doctor.addSchedule(schedule);
                doctors.add(doctor.getDoctorId(), doctor);
                doctorIndexById.add(doctor.getDoctorId(), doctor);
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
                    Doctor doctor = findDoctorById(schedule.getDoctorId());
                    if (doctor != null) {
                        doctor.updateSchedule(schedule);
                        doctors.add(doctor.getDoctorId(), doctor);
                        doctorIndexById.add(doctor.getDoctorId(), doctor);
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
            Doctor oldDoctor = findDoctorById(doctorId);
            if (oldDoctor == null) {
                return false;
            }

            // Update in database
            boolean updated = doctorDao.updateAvailability(doctorId, isAvailable);
            if (!updated) {
                return false;
            }

            // Create updated doctor with new availability
            Doctor doctor = new Doctor(
                oldDoctor.getFullName(),
                oldDoctor.getICNumber(),
                oldDoctor.getEmail(),
                oldDoctor.getPhoneNumber(),
                oldDoctor.getAddress(),
                oldDoctor.getRegistrationDate(),
                oldDoctor.getDoctorId(),
                oldDoctor.getMedicalSpecialty(),
                oldDoctor.getLicenseNumber(),
                oldDoctor.getExpYears()
            );
            doctor.setAvailable(isAvailable);

            // Cascade schedules to match doctor availability
            ArrayBucketList<String, Schedule> schedules = scheduleDao.findByDoctorId(doctorId);
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
            
            // Update in-memory collections
            doctors.add(doctor.getDoctorId(), doctor);
            categorizeDoctors();
            doctorIndexById.add(doctor.getDoctorId(), doctor);
            reindexDoctor(oldDoctor, doctor);
            
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
        return findDoctorsByAvailability(true);
    }

    public ArrayBucketList<String, Doctor> getDoctorsBySpecialty(String specialty) {
        ArrayBucketList<String, Doctor> results = findDoctorsBySpecialty(specialty);
        ArrayBucketList<String, Doctor> availableResults = ArrayBucketListFactory.createForStringIds(16);
        Iterator<Doctor> iterator = results.iterator();
        while (iterator.hasNext()) {
            Doctor doctor = iterator.next();
            if (doctor.isAvailable()) {
                availableResults.add(doctor.getDoctorId(), doctor);
            }
        }
        return availableResults;
    }

    public Doctor findDoctorByIcNumber(String icNumber) {
        if (icNumber == null || icNumber.trim().isEmpty()) {
            return null;
        }
        // Use direct hash-based lookup since doctors are stored with IC number as key
        return doctors.getValue(icNumber.trim());
    }


    public ArrayBucketList<String, Doctor> findDoctorsByMultipleCriteria(String name, String specialty, 
            boolean isAvailable, int minExperience) {
        ArrayBucketList<String, Doctor> results = ArrayBucketListFactory.createForStringIds(32);
        
        // Start with all doctors
        Iterator<Doctor> doctorIterator = doctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            boolean matches = true;
            
            // Check name criteria
            if (name != null && !name.trim().isEmpty()) {
                String doctorName = doctor.getFullName();
                if (doctorName == null || !doctorName.toLowerCase().contains(name.toLowerCase().trim())) {
                    matches = false;
                }
            }
            
            // Check specialty criteria
            if (matches && specialty != null && !specialty.trim().isEmpty()) {
                String doctorSpecialty = doctor.getMedicalSpecialty();
                if (doctorSpecialty == null || !doctorSpecialty.toLowerCase().contains(specialty.toLowerCase().trim())) {
                    matches = false;
                }
            }
            
            // Check availability criteria
            if (matches && doctor.isAvailable() != isAvailable) {
                matches = false;
            }
            
            // Check experience criteria
            if (matches && doctor.getExpYears() < minExperience) {
                matches = false;
            }
            
            if (matches) {
                results.add(doctor.getDoctorId(), doctor);
            }
        }
        
        return results;
    }

    // Reporting Methods
    public String generateDoctorInformationReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("DOCTOR MANAGEMENT SYSTEM - DOCTOR INFORMATION REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("SUMMARY STATISTICS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("Total Doctors: %d\n", getTotalDoctors()));
        report.append(String.format("Active Doctors: %d\n", getActiveDoctorsCount()));
        report.append(String.format("Inactive Doctors: %d\n", getInactiveDoctorsCount()));
        report.append(String.format("Available Doctors: %d\n", getAvailableDoctors().getSize()));

        // Specialty distribution analysis using arrays
        String[] specialties = new String[20];
        int[] specialtyCounts = new int[20];
        int specialtyCount = 0;

        Iterator<Doctor> doctorIterator = doctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            String specialty = doctor.getMedicalSpecialty() != null ? doctor.getMedicalSpecialty() : "General";

            // Find if specialty already exists
            int specialtyIndex = -1;
            for (int index = 0; index < specialtyCount; index++) {
                if (specialties[index].equals(specialty)) {
                    specialtyIndex = index;
                    break;
                }
            }

            // If specialty doesn't exist, add new entry
            if (specialtyIndex == -1) {
                specialties[specialtyCount] = specialty;
                specialtyCounts[specialtyCount] = 1;
                specialtyCount++;
            } else {
                // Update existing specialty count
                specialtyCounts[specialtyIndex]++;
            }
        }

        report.append("\nSPECIALTY DISTRIBUTION:\n");
        for (int index = 0; index < specialtyCount; index++) {
            report.append(String.format("%-20s: %d doctors\n", specialties[index], specialtyCounts[index]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed doctor table with sorting
        report.append(ConsoleUtils.centerText("DETAILED DOCTOR INFORMATION", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-12s | %-25s | %-20s | %-15s | %-12s | %-25s | %-15s\n",
                "Doctor ID", "Full Name", "Specialty", "Experience", "License", "Email", "Phone"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        Doctor[] doctorArray = doctors.toArray(Doctor.class);
        
        // Sort the doctor array
        sortDoctorArray(doctorArray, sortBy, sortOrder);

        // Generate sorted table
        for (Doctor doctor : doctorArray) {
            String id = doctor.getDoctorId() != null ? doctor.getDoctorId() : "-";
            String name = doctor.getFullName() != null ? doctor.getFullName() : "-";
            String specialty = doctor.getMedicalSpecialty() != null ? doctor.getMedicalSpecialty() : "-";
            String experience = doctor.getExpYears() + " years";
            String license = doctor.getLicenseNumber() != null ? doctor.getLicenseNumber() : "-";
            String email = doctor.getEmail() != null ? doctor.getEmail() : "-";
            String phone = doctor.getPhoneNumber() != null ? doctor.getPhoneNumber() : "-";

            // Truncate long names and emails
            if (name.length() > 25) name = name.substring(0, 22) + "...";
            if (specialty.length() > 20) specialty = specialty.substring(0, 17) + "...";
            if (email.length() > 25) email = email.substring(0, 22) + "...";
            if (phone.length() > 15) phone = phone.substring(0, 12) + "...";

            report.append(String.format("%-12s | %-25s | %-20s | %-15s | %-12s | %-25s | %-15s\n",
                    id, name, specialty, experience, license, email, phone));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF DOCTOR INFORMATION REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }


    // Helper methods for reports



    private void sortDoctorArray(Doctor[] doctorArray, String sortBy, String sortOrder) {
        if (doctorArray == null || doctorArray.length < 2) return;

        Comparator<Doctor> comparator = getDoctorComparator(sortBy);
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(doctorArray, comparator);
    }

    // Reporting Methods
    private static final int REPORT_WIDTH = 120;

    private String repeatChar(char ch, int count) {
        StringBuilder builder = new StringBuilder();
        for (int charIndex = 0; charIndex < count; charIndex++)
            builder.append(ch);
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

    public String generateDoctorWorkloadReport(String sortBy, boolean ascending) {
        StringBuilder report = new StringBuilder();
        String title = "DOCTOR WORKLOAD REPORT (Estimated Annual Hours)";
        String line = repeatChar('=', REPORT_WIDTH);
        report.append(ConsoleUtils.centerText(title, REPORT_WIDTH)).append("\n");
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
        report.append(ConsoleUtils.centerText("END OF THE REPORT", REPORT_WIDTH)).append("\n");
        report.append(repeatChar('=', REPORT_WIDTH)).append("\n");
        return report.toString();
    }

    // Quicksort helper for Row[] using comparator
    private <T> void quickSortRows(T[] arr, int low, int high, java.util.Comparator<T> comparator) {
        if (low >= high)
            return;
        int leftIndex = low, rightIndex = high;
        T pivot = arr[low + (high - low) / 2];
        while (leftIndex <= rightIndex) {
            while (comparator.compare(arr[leftIndex], pivot) < 0)
                leftIndex++;
            while (comparator.compare(arr[rightIndex], pivot) > 0)
                rightIndex--;
            if (leftIndex <= rightIndex) {
                T tmp = arr[leftIndex];
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
        report.append(ConsoleUtils.centerText(title, TABLEWIDTH)).append("\n\n");

        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")))
                .append("\n");
        report.append(repeatChar('=', TABLEWIDTH)).append("\n\n");

        // Performance metrics calculation
        report.append("-".repeat(TABLEWIDTH)).append("\n");
        report.append(ConsoleUtils.centerText("PERFORMANCE METRICS SUMMARY", TABLEWIDTH)).append("\n");
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

        report.append(String.format("Total Active Doctors: %d\n", getActiveDoctorsCount()));
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
        report.append(ConsoleUtils.centerText("DETAILED DOCTOR PERFORMANCE", TABLEWIDTH)).append("\n");
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
        report.append(ConsoleUtils.centerText("END OF PERFORMANCE REPORT", TABLEWIDTH)).append("\n");
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

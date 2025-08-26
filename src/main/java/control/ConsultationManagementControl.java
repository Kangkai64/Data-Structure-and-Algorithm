package control;

import adt.ArrayBucketList;
import utility.ConsoleUtils;
import utility.QuickSort;
import entity.Consultation;
import entity.Patient;
import entity.Doctor;
import entity.Schedule;
import dao.ConsultationDao;
import dao.PatientDao;
import dao.DoctorDao;
import dao.ScheduleDao;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Iterator;

/**
 * @author: Poh Qi Xuan
 * Consultation Management Control - Module 3
 * Manages patient consultations and arrange subsequent visit appointments
 */
public class ConsultationManagementControl {
    
    private ArrayBucketList<String, Consultation> consultations;
    private ArrayBucketList<String, Consultation> scheduledConsultations;
    // Consultation queues for each doctor (key: doctorId, value: queue of consultations)
    private ArrayBucketList<String, ArrayBucketList<String, Consultation>> doctorQueues;
    // Track which doctor is currently in consultation (key: doctorId, value: consultationId)
    private ArrayBucketList<String, String> activeConsultations;
    private ConsultationDao consultationDao;
    private ScheduleDao scheduleDao;
    
    public ConsultationManagementControl() {
        this.consultations = new ArrayBucketList<String, Consultation>();
        this.scheduledConsultations = new ArrayBucketList<String, Consultation>();
        this.doctorQueues = new ArrayBucketList<String, ArrayBucketList<String, Consultation>>();
        this.activeConsultations = new ArrayBucketList<String, String>();
        this.consultationDao = new ConsultationDao();
        this.scheduleDao = new ScheduleDao();
        loadConsultationData();
    }
    
    public void loadConsultationData() {
        try {
            consultations = consultationDao.findAll();
            Iterator<Consultation> consultationIterator = consultations.iterator();
            while (consultationIterator.hasNext()) {
                Consultation consultation = consultationIterator.next();
                if (consultation.getStatus() == Consultation.ConsultationStatus.SCHEDULED) {
                    scheduledConsultations.add(consultation.getConsultationId(), consultation);
                    // Add to doctor's queue
                    addToDoctorQueue(consultation);
                } else if (consultation.getStatus() == Consultation.ConsultationStatus.IN_PROGRESS) {
                    // Mark doctor as having active consultation
                    activeConsultations.add(consultation.getDoctor().getDoctorId(), consultation.getConsultationId());
                }
            }
        } catch (Exception exception) {
            System.err.println("Error loading consultation data: " + exception.getMessage());
        }
    }
    
    /**
     * Add consultation to the appropriate doctor's queue based on slot time
     */
    private void addToDoctorQueue(Consultation consultation) {
        String doctorId = consultation.getDoctor().getDoctorId();
        
        // Get or create doctor's queue
        ArrayBucketList<String, Consultation> doctorQueue = doctorQueues.getValue(doctorId);
        if (doctorQueue == null) {
            doctorQueue = new ArrayBucketList<String, Consultation>();
            doctorQueues.add(doctorId, doctorQueue);
        }
        
        // Add to queue with slot-based key for ordering
        String slotKey = generateSlotKey(consultation.getConsultationDate());
        doctorQueue.add(slotKey, consultation);
    }
    
    /**
     * Generate slot key for ordering consultations by time
     */
    private String generateSlotKey(LocalDateTime consultationDate) {
        // Format: YYYYMMDDHHMM for proper sorting
        return String.format("%04d%02d%02d%02d%02d", 
            consultationDate.getYear(),
            consultationDate.getMonthValue(),
            consultationDate.getDayOfMonth(),
            consultationDate.getHour(),
            consultationDate.getMinute());
    }
    
    /**
     * Remove consultation from doctor's queue
     */
    private void removeFromDoctorQueue(Consultation consultation) {
        String doctorId = consultation.getDoctor().getDoctorId();
        ArrayBucketList<String, Consultation> doctorQueue = doctorQueues.getValue(doctorId);
        if (doctorQueue != null) {
            String slotKey = generateSlotKey(consultation.getConsultationDate());
            doctorQueue.remove(slotKey);
        }
    }
    
    /**
     * Get sorted consultations for a specific doctor on a specific date
     */
    private ArrayBucketList<String, Consultation> getSortedConsultationsForDoctor(String doctorId, LocalDate date) {
        ArrayBucketList<String, Consultation> doctorQueue = doctorQueues.getValue(doctorId);
        if (doctorQueue == null) {
            return new ArrayBucketList<String, Consultation>();
        }
        
        ArrayBucketList<String, Consultation> sortedConsultations = new ArrayBucketList<String, Consultation>();
        Iterator<Consultation> consultationIterator = doctorQueue.iterator();
        
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getConsultationDate().toLocalDate().equals(date) && 
                consultation.getStatus() == Consultation.ConsultationStatus.SCHEDULED) {
                String slotKey = generateSlotKey(consultation.getConsultationDate());
                sortedConsultations.add(slotKey, consultation);
            }
        }
        
        return sortedConsultations;
    }
    
    /**
     * Start consultation using queue system
     * Enqueues booked consultations in ascending order of slot number according to system date
     */
    public String startConsultation(String doctorId) {
        try {
            LocalDate today = LocalDate.now();
            
            // Check if doctor is already in consultation
            if (activeConsultations.contains(doctorId)) {
                return "Doctor is already in consultation. Please complete the current consultation first.";
            }
            
            // Get sorted consultations for the doctor today
            ArrayBucketList<String, Consultation> todayConsultations = getSortedConsultationsForDoctor(doctorId, today);
            
            if (todayConsultations.getSize() == 0) {
                return "No scheduled consultations for today.";
            }
            
            // Get the first consultation (earliest slot)
            Iterator<Consultation> consultationIterator = todayConsultations.iterator();
            if (consultationIterator.hasNext()) {
                Consultation nextConsultation = consultationIterator.next();
                
                // Start the consultation
                nextConsultation.setStatus(Consultation.ConsultationStatus.IN_PROGRESS);
                boolean updated = consultationDao.updateStatus(nextConsultation.getConsultationId(), 
                                                             Consultation.ConsultationStatus.IN_PROGRESS);
                if (!updated) {
                    return "Failed to update consultation status in database.";
                }
                
                // Mark doctor as having active consultation
                activeConsultations.add(doctorId, nextConsultation.getConsultationId());
                
                // Remove from scheduled consultations
                removeFromScheduledConsultations(nextConsultation);
                
                // Remove from doctor's queue
                removeFromDoctorQueue(nextConsultation);
                
                return "Consultation started successfully for: " + nextConsultation.getPatient().getFullName() + 
                       " (Slot: " + nextConsultation.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm")) + ")";
            }
            
            return "No scheduled consultations for today.";
            
        } catch (Exception exception) {
            System.err.println("Error starting consultation: " + exception.getMessage());
            return "Error starting consultation: " + exception.getMessage();
        }
    }
    
    /**
     * Complete consultation and free up the doctor
     */
    public boolean completeConsultation(String consultationId, String diagnosis, 
                                      String treatment, String notes, LocalDateTime nextVisitDate) {
        try {
            Consultation consultation = findConsultationById(consultationId);
            if (consultation != null && consultation.getStatus() == Consultation.ConsultationStatus.IN_PROGRESS) {
                consultation.setDiagnosis(diagnosis);
                consultation.setTreatment(treatment);
                consultation.setNotes(notes);
                consultation.setNextVisitDate(nextVisitDate);
                consultation.setStatus(Consultation.ConsultationStatus.COMPLETED);
                
                // Update in database
                boolean updated = consultationDao.update(consultation);
                if (!updated) {
                    System.err.println("Failed to update consultation in database");
                    return false;
                }
                
                // Remove from active consultations
                activeConsultations.remove(consultation.getDoctor().getDoctorId());
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error completing consultation: " + exception.getMessage());
            return false;
        }
    }
    
    /**
     * Get current consultation status for a doctor
     */
    public String getDoctorConsultationStatus(String doctorId) {
        if (activeConsultations.contains(doctorId)) {
            String activeConsultationId = activeConsultations.getValue(doctorId);
            Consultation activeConsultation = findConsultationById(activeConsultationId);
            if (activeConsultation != null) {
                return "Doctor is currently in consultation with: " + activeConsultation.getPatient().getFullName() +
                       " (Started at: " + activeConsultation.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm")) + ")";
            }
        }
        
        LocalDate today = LocalDate.now();
        ArrayBucketList<String, Consultation> todayConsultations = getSortedConsultationsForDoctor(doctorId, today);
        
        if (todayConsultations.getSize() == 0) {
            return "No scheduled consultations for today.";
        } else {
            return "Doctor has " + todayConsultations.getSize() + " scheduled consultations for today.";
        }
    }
    
    /**
     * Get queue status for all doctors working today
     */
    public String getQueueStatus() {
        StringBuilder status = new StringBuilder();
        LocalDate today = LocalDate.now();
        
        status.append("=== CONSULTATION QUEUE STATUS ===\n");
        status.append("Date: ").append(today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("\n\n");
        
        // Since we can't easily iterate over keys in ArrayBucketList, we'll use a different approach
        // We'll check for doctors who have consultations today by looking at scheduled consultations
        Iterator<Consultation> scheduledIterator = scheduledConsultations.iterator();
        ArrayBucketList<String, String> processedDoctors = new ArrayBucketList<String, String>();
        
        while (scheduledIterator.hasNext()) {
            Consultation consultation = scheduledIterator.next();
            if (consultation.getConsultationDate().toLocalDate().equals(today)) {
                String doctorId = consultation.getDoctor().getDoctorId();
                
                // Only process each doctor once
                if (!processedDoctors.contains(doctorId)) {
                    processedDoctors.add(doctorId, doctorId);
                    
                    ArrayBucketList<String, Consultation> todayConsultations = getSortedConsultationsForDoctor(doctorId, today);
                    
                    if (todayConsultations.getSize() > 0) {
                        status.append("Doctor ID: ").append(doctorId).append("\n");
                        status.append("Scheduled consultations: ").append(todayConsultations.getSize()).append("\n");
                        
                        if (activeConsultations.contains(doctorId)) {
                            status.append("Status: IN CONSULTATION\n");
                        } else {
                            status.append("Status: AVAILABLE\n");
                        }
                        
                        status.append("Next consultation: ");
                        Iterator<Consultation> consultationIterator = todayConsultations.iterator();
                        if (consultationIterator.hasNext()) {
                            Consultation next = consultationIterator.next();
                            status.append(next.getPatient().getFullName())
                                  .append(" at ")
                                  .append(next.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm")));
                        }
                        status.append("\n\n");
                    }
                }
            }
        }
        
        if (processedDoctors.getSize() == 0) {
            status.append("No doctors have scheduled consultations for today.\n");
        }
        
        return status.toString();
    }
    
    // Consultation Management Methods
    public boolean scheduleConsultation(Patient patient, Doctor doctor, 
                                      LocalDateTime consultationDate, String symptoms, 
                                      double consultationFee) {
        try {
            // Create new consultation without ID (will be generated by database)
            Consultation consultation = new Consultation(null, patient, doctor, 
                                                       consultationDate, symptoms, consultationFee);
            
            // Insert consultation and get the generated ID
            boolean consultationInserted = consultationDao.insertAndReturnId(consultation);
            if (!consultationInserted) {
                System.err.println("Failed to insert consultation");
                return false;
            }
            
            // Add to lists
            consultations.add(consultation.getConsultationId(), consultation);
            scheduledConsultations.add(consultation.getConsultationId(), consultation);
            addToDoctorQueue(consultation); // Add to doctor's queue
            
            return true;
        } catch (Exception exception) {
            System.err.println("Error scheduling consultation: " + exception.getMessage());
            return false;
        }
    }
    
    
    public boolean cancelConsultation(String consultationId) {
        try {
            Consultation consultation = findConsultationById(consultationId);
            if (consultation != null && consultation.getStatus() == Consultation.ConsultationStatus.SCHEDULED) {
                consultation.setStatus(Consultation.ConsultationStatus.CANCELLED);
                
                // Update in database
                boolean updated = consultationDao.updateStatus(consultationId, Consultation.ConsultationStatus.CANCELLED);
                if (!updated) {
                    System.err.println("Failed to update consultation status in database");
                    return false;
                }
                
                // Remove from scheduled consultations
                removeFromScheduledConsultations(consultation);
                
                // Remove from doctor's queue
                removeFromDoctorQueue(consultation);
                
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error cancelling consultation: " + exception.getMessage());
            return false;
        }
    }
    
    // Search and Retrieval Methods
    public Consultation findConsultationById(String consultationId) {
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getConsultationId().equals(consultationId)) {
                return consultation;
            }
        }
        return null;
    }
    
    public ArrayBucketList<String, Consultation> findConsultationsByPatient(String patientId) {
        ArrayBucketList<String, Consultation> patientConsultations = new ArrayBucketList<String, Consultation>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getPatient().getPatientId().equals(patientId)) {
                patientConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        return patientConsultations;
    }
    
    public ArrayBucketList<String, Consultation> findConsultationsByDoctor(String doctorId) {
        ArrayBucketList<String, Consultation> doctorConsultations = new ArrayBucketList<String, Consultation>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getDoctor().getDoctorId().equals(doctorId)) {
                doctorConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        return doctorConsultations;
    }
    
    public ArrayBucketList<String, Consultation> findConsultationsByDate(LocalDateTime date) {
        ArrayBucketList<String, Consultation> dateConsultations = new ArrayBucketList<String, Consultation>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getConsultationDate().toLocalDate().equals(date.toLocalDate())) {
                dateConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        return dateConsultations;
    }
    
    public ArrayBucketList<String, Consultation> getScheduledConsultations() {
        return scheduledConsultations;
    }
    
    public ArrayBucketList<String, Consultation> getCompletedConsultations() {
        ArrayBucketList<String, Consultation> completedConsultations = new ArrayBucketList<String, Consultation>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getStatus() == Consultation.ConsultationStatus.COMPLETED) {
                completedConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        return completedConsultations;
    }
    
    public ArrayBucketList<String, Consultation> getInProgressConsultations() {
        ArrayBucketList<String, Consultation> inProgressConsultations = new ArrayBucketList<String, Consultation>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getStatus() == Consultation.ConsultationStatus.IN_PROGRESS) {
                inProgressConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        return inProgressConsultations;
    }
    
    public ArrayBucketList<String, Consultation> getCancelledConsultations() {
        ArrayBucketList<String, Consultation> cancelledConsultations = new ArrayBucketList<String, Consultation>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getStatus() == Consultation.ConsultationStatus.CANCELLED) {
                cancelledConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        return cancelledConsultations;
    }
    
    public ArrayBucketList<String, Consultation> getAllConsultations() {
        return consultations;
    }
    
    public int getTotalConsultations() {
        return consultations.getSize();
    }
    
    public int getScheduledConsultationsCount() {
        return scheduledConsultations.getSize();
    }
    
    // Reporting Methods
    public String generateConsultationReport() {
        return generateConsultationReport("date", "desc");
    }
    
    public String generateConsultationReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("CONSULTATION MANAGEMENT SYSTEM - CONSULTATION ANALYSIS REPORT", 120))
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
        report.append(String.format("Total Consultations: %d\n", getTotalConsultations()));
        report.append(String.format("Scheduled Consultations: %d\n", getScheduledConsultationsCount()));
        report.append(String.format("Completed Consultations: %d\n", getCompletedConsultations().getSize()));
        report.append(String.format("In Progress Consultations: %d\n", getInProgressConsultations().getSize()));
        report.append(String.format("Cancelled Consultations: %d\n", getCancelledConsultations().getSize()));

        // Calculate completion rate
        double completionRate = getTotalConsultations() > 0 ? 
            (double) getCompletedConsultations().getSize() / getTotalConsultations() * 100 : 0;
        report.append(String.format("Completion Rate: %.1f%%\n", completionRate));

        // Calculate total revenue
        double totalRevenue = 0;
        Iterator<Consultation> revenueIterator = consultations.iterator();
        while (revenueIterator.hasNext()) {
            Consultation consultation = revenueIterator.next();
            totalRevenue += consultation.getConsultationFee();
        }
        report.append(String.format("Total Revenue: RM %,.2f\n", totalRevenue));

        // Consultations by status analysis
        report.append("\nCONSULTATION STATUS DISTRIBUTION:\n");
        report.append(String.format("SCHEDULED    : %d consultations\n", getScheduledConsultationsCount()));
        report.append(String.format("IN_PROGRESS  : %d consultations\n", getInProgressConsultations().getSize()));
        report.append(String.format("COMPLETED    : %d consultations\n", getCompletedConsultations().getSize()));
        report.append(String.format("CANCELLED    : %d consultations\n", getCancelledConsultations().getSize()));

        // Consultations by year analysis
        int[] consultationYears = new int[50];
        int[] consultationsByYear = new int[50];
        double[] revenueByYear = new double[50];
        int yearCount = 0;

        Iterator<Consultation> yearIterator = consultations.iterator();
        while (yearIterator.hasNext()) {
            Consultation consultation = yearIterator.next();
            int year = consultation.getConsultationDate().getYear();

            // Find if year already exists
            int yearIndex = -1;
            for (int i = 0; i < yearCount; i++) {
                if (consultationYears[i] == year) {
                    yearIndex = i;
                    break;
                }
            }

            // If year doesn't exist, add new entry
            if (yearIndex == -1) {
                consultationYears[yearCount] = year;
                consultationsByYear[yearCount] = 1;
                revenueByYear[yearCount] = consultation.getConsultationFee();
                yearCount++;
            } else {
                // Update existing year data
                consultationsByYear[yearIndex]++;
                revenueByYear[yearIndex] += consultation.getConsultationFee();
            }
        }

        report.append("\nCONSULTATIONS BY YEAR:\n");
        for (int i = 0; i < yearCount; i++) {
            report.append(String.format("Year %d: %d consultations (RM %,.2f revenue)\n",
                    consultationYears[i], consultationsByYear[i], revenueByYear[i]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed consultation table with sorting
        report.append(ConsoleUtils.centerText("DETAILED CONSULTATION RECORDS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        
        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-12s | %-22s | %-22s | %-16s | %-12s | %12s\n",
                "ID", "Patient", "Doctor", "Date & Time", "Status", "Fee"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        Consultation[] consultationArray = new Consultation[consultations.getSize()];
        int index = 0;
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            consultationArray[index++] = consultationIterator.next();
        }

        // Sort the consultation array
        sortConsultationArray(consultationArray, sortBy, sortOrder);

        // Generate sorted table
        for (Consultation consultation : consultationArray) {
            String id = consultation.getConsultationId() == null ? "-" : consultation.getConsultationId();
            String patientName = consultation.getPatient() == null ? "-" : consultation.getPatient().getFullName();
            String doctorName = consultation.getDoctor() == null ? "-" : consultation.getDoctor().getFullName();
            String dateTime = consultation.getConsultationDate() == null ? "-" :
                    consultation.getConsultationDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm"));
            String status = consultation.getStatus() == null ? "-" : consultation.getStatus().toString();

            // Truncate long names
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";

            report.append(String.format("%-12s | %-22s | %-22s | %-16s | %-12s | RM %10.2f\n",
                    id, patientName, doctorName, dateTime, status, consultation.getConsultationFee()));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF CONSULTATION REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }
    
    public String generateConsultationHistoryReport() {
        return generateConsultationHistoryReport("date", "desc");
    }
    
    public String generateConsultationHistoryReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("CONSULTATION MANAGEMENT SYSTEM - CONSULTATION HISTORY REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("COMPLETED CONSULTATIONS SUMMARY", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("Total Consultations: %d\n", getTotalConsultations()));
        report.append(String.format("Completed Consultations: %d\n", getCompletedConsultations().getSize()));
        report.append(String.format("Completion Rate: %.1f%%\n", 
                (double) getCompletedConsultations().getSize() / getTotalConsultations() * 100));

        // Calculate total revenue from completed consultations
        double totalRevenue = 0;
        Iterator<Consultation> revenueIterator = consultations.iterator();
        while (revenueIterator.hasNext()) {
            Consultation consultation = revenueIterator.next();
            if (consultation.getStatus() == Consultation.ConsultationStatus.COMPLETED) {
                totalRevenue += consultation.getConsultationFee();
            }
        }
        report.append(String.format("Total Revenue from Completed: RM %,.2f\n", totalRevenue));

        // Completed consultations by year analysis
        int[] consultationYears = new int[50];
        int[] consultationsByYear = new int[50];
        double[] revenueByYear = new double[50];
        int yearCount = 0;

        Iterator<Consultation> yearIterator = consultations.iterator();
        while (yearIterator.hasNext()) {
            Consultation consultation = yearIterator.next();
            if (consultation.getStatus() == Consultation.ConsultationStatus.COMPLETED) {
                int year = consultation.getConsultationDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int i = 0; i < yearCount; i++) {
                    if (consultationYears[i] == year) {
                        yearIndex = i;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    consultationYears[yearCount] = year;
                    consultationsByYear[yearCount] = 1;
                    revenueByYear[yearCount] = consultation.getConsultationFee();
                    yearCount++;
                } else {
                    // Update existing year data
                    consultationsByYear[yearIndex]++;
                    revenueByYear[yearIndex] += consultation.getConsultationFee();
                }
            }
        }

        report.append("\nCOMPLETED CONSULTATIONS BY YEAR:\n");
        for (int i = 0; i < yearCount; i++) {
            report.append(String.format("Year %d: %d consultations (RM %,.2f revenue)\n",
                    consultationYears[i], consultationsByYear[i], revenueByYear[i]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed completed consultation table with sorting
        report.append(ConsoleUtils.centerText("DETAILED COMPLETED CONSULTATION RECORDS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        
        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-12s | %-22s | %-22s | %-16s | %-12s | %12s\n",
                "ID", "Patient", "Doctor", "Date & Time", "Status", "Fee"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting (only completed consultations)
        ArrayBucketList<String, Consultation> completedConsultations = getCompletedConsultations();
        Consultation[] consultationArray = new Consultation[completedConsultations.getSize()];
        int index = 0;
        Iterator<Consultation> consultationIterator = completedConsultations.iterator();
        while (consultationIterator.hasNext()) {
            consultationArray[index++] = consultationIterator.next();
        }

        // Sort the consultation array
        sortConsultationArray(consultationArray, sortBy, sortOrder);

        // Generate sorted table
        for (Consultation consultation : consultationArray) {
            String id = consultation.getConsultationId() == null ? "-" : consultation.getConsultationId();
            String patientName = consultation.getPatient() == null ? "-" : consultation.getPatient().getFullName();
            String doctorName = consultation.getDoctor() == null ? "-" : consultation.getDoctor().getFullName();
            String dateTime = consultation.getConsultationDate() == null ? "-" :
                    consultation.getConsultationDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm"));
            String status = consultation.getStatus() == null ? "-" : consultation.getStatus().toString();

            // Truncate long names
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";

            report.append(String.format("%-12s | %-22s | %-22s | %-16s | %-12s | RM %10.2f\n",
                    id, patientName, doctorName, dateTime, status, consultation.getConsultationFee()));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF CONSULTATION HISTORY REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }
    
    private void removeFromScheduledConsultations(Consultation consultation) {
        Iterator<Consultation> scheduledIterator = scheduledConsultations.iterator();
        while (scheduledIterator.hasNext()) {
            Consultation scheduledConsultation = scheduledIterator.next();
            if (scheduledConsultation.getConsultationId().equals(consultation.getConsultationId())) {
                scheduledConsultations.remove(scheduledConsultation.getConsultationId());
                break;
            }
        }
    }

    // Scheduling utilities
    public ArrayBucketList<String, Schedule> getAvailableSchedulesByDate(LocalDate date) {
        ArrayBucketList<String, Schedule> available = new ArrayBucketList<String, Schedule>();
        try {
            ArrayBucketList<String, Schedule> all = scheduleDao.findAll();
            entity.DayOfWeek target = entity.DayOfWeek.valueOf(date.getDayOfWeek().name());
            for (Schedule schedule : all) {
                if (schedule.isAvailable() && schedule.getDayOfWeek() == target) {
                    available.add(schedule.getScheduleId(), schedule);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading schedules: " + e.getMessage());
        }
        return available;
    }

    public boolean isTimeWithinDoctorSchedule(String doctorId, LocalDate date, LocalTime time) {
        try {
            ArrayBucketList<String, Schedule> all = scheduleDao.findAll();
            entity.DayOfWeek target = entity.DayOfWeek.valueOf(date.getDayOfWeek().name());
            boolean within = false;
            for (Schedule schedule : all) {
                if (schedule.isAvailable() && schedule.getDoctorId().equals(doctorId) && schedule.getDayOfWeek() == target) {
                    LocalTime from = LocalTime.parse(schedule.getFromTime());
                    LocalTime to = LocalTime.parse(schedule.getToTime());
                    if (!time.isBefore(from) && !time.isAfter(to)) {
                        within = true;
                        break;
                    }
                }
            }
            return within;
        } catch (Exception e) {
            System.err.println("Error validating time within schedule: " + e.getMessage());
            return false;
        }
    }

    public boolean hasDoctorConsultationAt(String doctorId, LocalDateTime dateTime) {
        Iterator<Consultation> it = consultations.iterator();
        while (it.hasNext()) {
            Consultation c = it.next();
            if (c.getDoctor().getDoctorId().equals(doctorId) && c.getConsultationDate().equals(dateTime)) {
                return true;
            }
        }
        return false;
    }

    // Slot generation with two 1-hour breaks at +3h and +6h from shift start, 1-hour slots, max 6
    public ArrayBucketList<String, String> getAvailableSlots(String doctorId, LocalDate date) {
        ArrayBucketList<String, String> slots = new ArrayBucketList<String, String>();
        try {
            // Identify working window from schedule(s)
            LocalTime windowStart = null;
            LocalTime windowEnd = null;
            ArrayBucketList<String, Schedule> all = scheduleDao.findAll();
            entity.DayOfWeek target = entity.DayOfWeek.valueOf(date.getDayOfWeek().name());
            for (Schedule schedule : all) {
                if (schedule.isAvailable() && schedule.getDoctorId().equals(doctorId) && schedule.getDayOfWeek() == target) {
                    LocalTime from = LocalTime.parse(schedule.getFromTime());
                    LocalTime to = LocalTime.parse(schedule.getToTime());
                    windowStart = (windowStart == null || from.isBefore(windowStart)) ? from : windowStart;
                    windowEnd = (windowEnd == null || to.isAfter(windowEnd)) ? to : windowEnd;
                }
            }
            if (windowStart == null || windowEnd == null) {
                return slots; // empty
            }

            // Enforce up to 8 hours from start (if schedule is longer)
            LocalTime enforcedEnd = windowStart.plusHours(8);
            if (enforcedEnd.isBefore(windowEnd)) {
                windowEnd = enforcedEnd;
            }

            // Define breaks
            LocalTime break1Start = windowStart.plusHours(3);
            LocalTime break1End = break1Start.plusHours(1);
            LocalTime break2Start = windowStart.plusHours(6);
            LocalTime break2End = break2Start.plusHours(1);

            // Generate 1-hour slots
            LocalTime cursor = windowStart;
            int slotIndex = 1;
            while (cursor.plusHours(1).compareTo(windowEnd) <= 0 && slotIndex <= 6) {
                // exclude if within break
                boolean inBreak1 = !cursor.isBefore(break1Start) && cursor.isBefore(break1End);
                boolean inBreak2 = !cursor.isBefore(break2Start) && cursor.isBefore(break2End);
                if (!inBreak1 && !inBreak2) {
                    LocalDateTime candidate = LocalDateTime.of(date, cursor);
                    if (!hasDoctorConsultationAt(doctorId, candidate)) {
                        String key = (slotIndex < 10 ? "SLOT_0" : "SLOT_") + slotIndex;
                        slots.add(key, cursor.toString());
                        slotIndex++;
                    }
                }
                cursor = cursor.plusHours(1);
            }
        } catch (Exception e) {
            System.err.println("Error generating available slots: " + e.getMessage());
        }
        return slots;
    }

    // Safer variant for UI: returns plain array to avoid ADT iterator quirks
    public String[] getAvailableSlotTimes(String doctorId, LocalDate date) {
        String[] temp = new String[6];
        int count = 0;
        try {
            // Identify working window from schedule(s)
            LocalTime windowStart = null;
            LocalTime windowEnd = null;
            ArrayBucketList<String, Schedule> all = scheduleDao.findAll();
            entity.DayOfWeek target = entity.DayOfWeek.valueOf(date.getDayOfWeek().name());
            for (Schedule schedule : all) {
                if (schedule.isAvailable() && schedule.getDoctorId().equals(doctorId) && schedule.getDayOfWeek() == target) {
                    LocalTime from = LocalTime.parse(schedule.getFromTime());
                    LocalTime to = LocalTime.parse(schedule.getToTime());
                    windowStart = (windowStart == null || from.isBefore(windowStart)) ? from : windowStart;
                    windowEnd = (windowEnd == null || to.isAfter(windowEnd)) ? to : windowEnd;
                }
            }
            if (windowStart == null || windowEnd == null) {
                return new String[0];
            }

            // Enforce up to 8 hours from start (if schedule is longer)
            LocalTime enforcedEnd = windowStart.plusHours(8);
            if (enforcedEnd.isBefore(windowEnd)) {
                windowEnd = enforcedEnd;
            }

            // Define breaks
            LocalTime break1Start = windowStart.plusHours(3);
            LocalTime break1End = break1Start.plusHours(1);
            LocalTime break2Start = windowStart.plusHours(6);
            LocalTime break2End = break2Start.plusHours(1);

            // Generate 1-hour slots
            LocalTime cursor = windowStart;
            int slotIndex = 1;
            while (cursor.plusHours(1).compareTo(windowEnd) <= 0 && slotIndex <= 6) {
                boolean inBreak1 = !cursor.isBefore(break1Start) && cursor.isBefore(break1End);
                boolean inBreak2 = !cursor.isBefore(break2Start) && cursor.isBefore(break2End);
                if (!inBreak1 && !inBreak2) {
                    LocalDateTime candidate = LocalDateTime.of(date, cursor);
                    if (!hasDoctorConsultationAt(doctorId, candidate)) {
                        temp[count++] = cursor.toString();
                        slotIndex++;
                        if (count == temp.length) {
                            break;
                        }
                    }
                }
                cursor = cursor.plusHours(1);
            }
        } catch (Exception e) {
            System.err.println("Error generating available slot times: " + e.getMessage());
        }
        String[] result = new String[count];
        if (count > 0) {
            System.arraycopy(temp, 0, result, 0, count);
        }
        return result;
    }

    // Helper methods for sorting
    private String getSortFieldDisplayName(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "id":
                return "Consultation ID";
            case "patient":
                return "Patient Name";
            case "doctor":
                return "Doctor Name";
            case "date":
                return "Consultation Date";
            case "status":
                return "Status";
            case "fee":
                return "Consultation Fee";
            default:
                return "Consultation Date";
        }
    }

    private void sortConsultationArray(Consultation[] consultationArray, String sortBy, String sortOrder) {
        if (consultationArray == null || consultationArray.length < 2)
            return;

        Comparator<Consultation> comparator = getConsultationComparator(sortBy);
        if (comparator == null)
            return;

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(consultationArray, comparator);
    }

    private Comparator<Consultation> getConsultationComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "id":
                return Comparator.comparing(c -> c.getConsultationId() != null ? c.getConsultationId() : "");
            case "patient":
                return Comparator.comparing(c -> c.getPatient() != null ? c.getPatient().getFullName() : "");
            case "doctor":
                return Comparator.comparing(c -> c.getDoctor() != null ? c.getDoctor().getFullName() : "");
            case "date":
                return Comparator.comparing(c -> c.getConsultationDate() != null ? c.getConsultationDate() : LocalDateTime.MAX);
            case "status":
                return Comparator.comparing(c -> c.getStatus() != null ? c.getStatus().toString() : "");
            case "fee":
                return Comparator.comparing(Consultation::getConsultationFee);
            default:
                return Comparator.comparing(c -> c.getConsultationDate() != null ? c.getConsultationDate() : LocalDateTime.MAX);
        }
    }

} 
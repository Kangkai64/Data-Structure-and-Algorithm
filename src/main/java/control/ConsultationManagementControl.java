package control;

import adt.ArrayBucketList;
import utility.ConsoleUtils;
import entity.Consultation;
import entity.Patient;
import entity.Doctor;
import entity.Schedule;
import dao.ConsultationDao;
import dao.ScheduleDao;
import dao.PatientDao;
import dao.DoctorDao;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
// Removed unused Comparator import after simplifying sorting

/**
 * @author: Poh Qi Xuan
 * Consultation Management Control - Module 3
 * Manages patient consultations and arrange subsequent visit appointments
 */
public class ConsultationManagementControl {
    
    private ArrayBucketList<String, Consultation> consultations;
    private ConsultationDao consultationDao;
    private ScheduleDao scheduleDao;
    private PatientDao patientDao;
    private DoctorDao doctorDao;
    
    public ConsultationManagementControl() {
        this.consultations = new ArrayBucketList<String, Consultation>();
        this.consultationDao = new ConsultationDao();
        this.scheduleDao = new ScheduleDao();
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        loadConsultationData();
    }
    
    public void loadConsultationData() {
        try {
            // Auto-cancel past-due consultations that never started or are stuck in progress
            try {
                consultationDao.cancelExpiredConsultations();
            } catch (Exception ignored) {}
            consultations = consultationDao.findAll();
        } catch (Exception exception) {
            System.err.println("Error loading consultation data: " + exception.getMessage());
        }
    }
    

    
    /**
     * Start consultation using simple search approach
     */
    public String startConsultation(String doctorId) {
        try {
            LocalDate today = LocalDate.now();
            
            // Check if doctor is already in consultation via DAO
            Consultation active = consultationDao.findInProgressByDoctor(doctorId);
            if (active != null) {
                return "Doctor is already in consultation. Please complete the current consultation first.";
            }
            
            // Find the earliest scheduled consultation for this doctor today via DAO
            Consultation nextConsultation = consultationDao.findEarliestScheduledByDoctorOnDate(doctorId, today);
            
            if (nextConsultation == null) {
                return "No scheduled consultations for today.";
            }
            
            // Start the consultation
            nextConsultation.setStatus(Consultation.ConsultationStatus.IN_PROGRESS);
            boolean updated = consultationDao.updateStatus(nextConsultation.getConsultationId(), 
                                                         Consultation.ConsultationStatus.IN_PROGRESS);
            if (!updated) {
                return "Failed to update consultation status in database.";
            }
            
            return "Consultation started successfully for: " + nextConsultation.getPatient().getFullName() + 
                   " (Slot: " + nextConsultation.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm")) + ")";
            
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
        LocalDate today = LocalDate.now();
        try {
            // Check if doctor is currently in consultation
            Consultation active = consultationDao.findInProgressByDoctor(doctorId);
            if (active != null) {
                return "Doctor is currently in consultation with: " + active.getPatient().getFullName() +
                       " (Started at: " + active.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm")) + ")";
            }
            
            // Count scheduled consultations for today
            int scheduledCount = consultationDao.countScheduledByDoctorOnDate(doctorId, today);
        
            if (scheduledCount == 0) {
                return "No scheduled consultations for today.";
            } else {
                return "Doctor has " + scheduledCount + " scheduled consultations for today.";
            }
        } catch (Exception e) {
            System.err.println("Error getting doctor consultation status: " + e.getMessage());
            return "Unable to retrieve consultation status.";
        }
    }
    
    /**
     * Get queue status for all doctors working today
     */
    public String getQueueStatus() {
        LocalDate today = LocalDate.now();
        try {
            return consultationDao.getQueueStatusForDate(today);
        } catch (Exception e) {
            System.err.println("Error generating queue status: " + e.getMessage());
            return "Unable to generate queue status.";
        }
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
            
            // Add to consultations list
            consultations.add(consultation.getConsultationId(), consultation);
            
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
        return consultations.getValue(consultationId);
    }
    
    public ArrayBucketList<String, Consultation> findConsultationsByPatient(String patientId) {
        // Deprecated in UI: use text builder to avoid ADT iteration issues
        ArrayBucketList<String, Consultation> empty = new ArrayBucketList<String, Consultation>();
        return empty;
    }
    
    public ArrayBucketList<String, Consultation> findConsultationsByDoctor(String doctorId) {
        ArrayBucketList<String, Consultation> empty = new ArrayBucketList<String, Consultation>();
        return empty;
    }
    
    public ArrayBucketList<String, Consultation> findConsultationsByDate(LocalDateTime date) {
        ArrayBucketList<String, Consultation> empty = new ArrayBucketList<String, Consultation>();
        return empty;
    }
    
    public ArrayBucketList<String, Consultation> getScheduledConsultations() {
        return new ArrayBucketList<String, Consultation>();
    }
    
    public ArrayBucketList<String, Consultation> getCompletedConsultations() {
        return new ArrayBucketList<String, Consultation>();
    }
    
    public ArrayBucketList<String, Consultation> getInProgressConsultations() {
        return new ArrayBucketList<String, Consultation>();
    }
    
    public ArrayBucketList<String, Consultation> getCancelledConsultations() {
        return new ArrayBucketList<String, Consultation>();
    }
    
    public ArrayBucketList<String, Consultation> getAllConsultations() {
        return consultations;
    }
    
    public int getTotalConsultations() {
        return consultations.getSize();
    }
    
    public int getScheduledConsultationsCount() {
        try {
            return consultationDao.getConsultationCountByStatus(Consultation.ConsultationStatus.SCHEDULED);
        } catch (Exception e) {
            return 0;
        }
    }

    // ECB helpers so boundary never touches DAOs directly
    public Patient getPatientById(String patientId) {
        try {
            return patientDao.findById(patientId);
        } catch (Exception e) {
            System.err.println("Error finding patient: " + e.getMessage());
            return null;
        }
    }

    public Doctor getDoctorById(String doctorId) {
        try {
            return doctorDao.findById(doctorId);
        } catch (Exception e) {
            System.err.println("Error finding doctor: " + e.getMessage());
            return null;
        }
    }

    public String searchByPatientText(String patientId) {
        try {
            return consultationDao.getConsultationsByPatientText(patientId);
        } catch (Exception e) {
            System.err.println("Error searching by patient: " + e.getMessage());
            return "Unable to search by patient.";
        }
    }

    public String searchByDoctorText(String doctorId) {
        try {
            return consultationDao.getConsultationsByDoctorText(doctorId);
        } catch (Exception e) {
            System.err.println("Error searching by doctor: " + e.getMessage());
            return "Unable to search by doctor.";
        }
    }

    public String searchByDateRangeText(java.time.LocalDate start, java.time.LocalDate end) {
        try {
            return consultationDao.getConsultationsByDateRangeText(start, end);
        } catch (Exception e) {
            System.err.println("Error searching by date range: " + e.getMessage());
            return "Unable to search by date range.";
        }
    }

    public String searchByStatusText(Consultation.ConsultationStatus status) {
        try {
            return consultationDao.getConsultationsByStatusText(status);
        } catch (Exception e) {
            System.err.println("Error searching by status: " + e.getMessage());
            return "Unable to search by status.";
        }
    }
    
    // Reporting Methods
    public String generateConsultationReport() {
        return generateConsultationReport("date", "desc");
    }
    
    public String generateConsultationReport(String sortBy, String sortOrder) {
        try {
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(120)).append("\n");
            report.append(ConsoleUtils.centerText("CONSULTATION MANAGEMENT SYSTEM - CONSULTATION ANALYSIS REPORT", 120))
                    .append("\n");
            report.append("=".repeat(120)).append("\n\n");
            report.append("Generated at: ")
                    .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                    .append("\n");
            report.append("*".repeat(120)).append("\n\n");
            report.append("-".repeat(120)).append("\n");
            report.append(ConsoleUtils.centerText("SUMMARY STATISTICS", 120)).append("\n");
            report.append("-".repeat(120)).append("\n");
            report.append(String.format("Total Consultations: %d\n", getTotalConsultations()));
            report.append(String.format("Scheduled Consultations: %d\n", getScheduledConsultationsCount()));
            report.append(String.format("Completed Consultations: %d\n", consultationDao.getConsultationCountByStatus(Consultation.ConsultationStatus.COMPLETED)));
            report.append(String.format("In Progress Consultations: %d\n", consultationDao.getConsultationCountByStatus(Consultation.ConsultationStatus.IN_PROGRESS)));
            report.append(String.format("Cancelled Consultations: %d\n", consultationDao.getConsultationCountByStatus(Consultation.ConsultationStatus.CANCELLED)));
            // Details table from DB
            String reportBody = consultationDao.getConsultationReportText(sortBy, sortOrder);
            report.append("-".repeat(120)).append("\n\n");
            report.append(ConsoleUtils.centerText("DETAILED CONSULTATION RECORDS", 120)).append("\n");
            report.append("-".repeat(120)).append("\n");
            report.append(String.format("Sorted by: %s (%s order)\n\n",
                    getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));
            report.append(reportBody);
            report.append("-".repeat(120)).append("\n");
            report.append("*".repeat(120)).append("\n");
            report.append(ConsoleUtils.centerText("END OF CONSULTATION REPORT", 120)).append("\n");
            report.append("=".repeat(120)).append("\n");
            return report.toString();
        } catch (Exception e) {
            System.err.println("Error generating consultation report: " + e.getMessage());
            return "Unable to generate report.";
        }
    }
    
    public String generateConsultationHistoryReport() {
        return generateConsultationHistoryReport("date", "desc");
    }
    
    public String generateConsultationHistoryReport(String sortBy, String sortOrder) {
        try {
            StringBuilder report = new StringBuilder();
            report.append("=".repeat(120)).append("\n");
            report.append(ConsoleUtils.centerText("CONSULTATION MANAGEMENT SYSTEM - CONSULTATION HISTORY REPORT", 120))
                    .append("\n");
            report.append("=".repeat(120)).append("\n\n");
            report.append("Generated at: ")
                    .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                    .append("\n");
            report.append("*".repeat(120)).append("\n\n");
            report.append("-".repeat(120)).append("\n");
            report.append(ConsoleUtils.centerText("COMPLETED CONSULTATIONS SUMMARY", 120)).append("\n");
            report.append("-".repeat(120)).append("\n");
            report.append(String.format("Total Consultations: %d\n", getTotalConsultations()));
            int completed = consultationDao.getConsultationCountByStatus(Consultation.ConsultationStatus.COMPLETED);
            report.append(String.format("Completed Consultations: %d\n", completed));
            double completionRate = getTotalConsultations() > 0 ? (double) completed / getTotalConsultations() * 100 : 0;
            report.append(String.format("Completion Rate: %.1f%%\n", completionRate));
            report.append("-".repeat(120)).append("\n\n");
            report.append(ConsoleUtils.centerText("DETAILED COMPLETED CONSULTATION RECORDS", 120)).append("\n");
            report.append("-".repeat(120)).append("\n");
            report.append(String.format("Sorted by: %s (%s order)\n\n",
                    getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));
            String reportBody = consultationDao.getConsultationHistoryReportText(sortBy, sortOrder);
            report.append(reportBody);
            report.append("-".repeat(120)).append("\n");
            report.append("*".repeat(120)).append("\n");
            report.append(ConsoleUtils.centerText("END OF CONSULTATION HISTORY REPORT", 120)).append("\n");
            report.append("=".repeat(120)).append("\n");
            return report.toString();
        } catch (Exception e) {
            System.err.println("Error generating consultation history report: " + e.getMessage());
            return "Unable to generate history report.";
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
        try {
            return consultationDao.existsConsultationAt(doctorId, dateTime);
        } catch (Exception e) {
            System.err.println("Error checking consultation at time: " + e.getMessage());
            return false;
        }
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

    // Sorting handled by SQL ORDER BY in DAO

} 
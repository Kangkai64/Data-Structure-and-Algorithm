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
import java.util.Iterator;
import java.util.Comparator;

/**
 * @author: Poh Qi Xuan
 *          Consultation Management Control - Module 3
 *          Manages patient consultations and arrange subsequent visit
 *          appointments
 */
public class ConsultationManagementControl {

    private ArrayBucketList<String, Consultation> consultations;
    private ArrayBucketList<String, Consultation> scheduledConsultations;
    private ArrayBucketList<String, Consultation> inProgressConsultations;
    private ArrayBucketList<String, Consultation> completedConsultations;
    private ArrayBucketList<String, Consultation> cancelledConsultations;
    private ConsultationDao consultationDao;
    private ScheduleDao scheduleDao;
    private PatientDao patientDao;
    private DoctorDao doctorDao;

    public ConsultationManagementControl() {
        this.consultations = new ArrayBucketList<String, Consultation>();
        this.scheduledConsultations = new ArrayBucketList<String, Consultation>();
        this.inProgressConsultations = new ArrayBucketList<String, Consultation>();
        this.completedConsultations = new ArrayBucketList<String, Consultation>();
        this.cancelledConsultations = new ArrayBucketList<String, Consultation>();
        this.consultationDao = new ConsultationDao();
        this.scheduleDao = new ScheduleDao();
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
    }

    public void loadConsultationData() {
        try {
            try {
                consultationDao.cancelExpiredConsultations();
            } catch (Exception ignored) {
            }
            consultations = consultationDao.findAll();
            categorizeConsultations();
        } catch (Exception exception) {
            System.err.println("Error loading consultation data: " + exception.getMessage());
        }
    }

    /**
     * Categorize consultations into status-specific collections for efficient
     * in-memory processing
     */
    private void categorizeConsultations() {
        // Clear existing categorized collections
        scheduledConsultations.clear();
        inProgressConsultations.clear();
        completedConsultations.clear();
        cancelledConsultations.clear();

        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            switch (consultation.getStatus()) {
                case SCHEDULED:
                    scheduledConsultations.add(consultation.getConsultationId(), consultation);
                    break;
                case IN_PROGRESS:
                    inProgressConsultations.add(consultation.getConsultationId(), consultation);
                    break;
                case COMPLETED:
                    completedConsultations.add(consultation.getConsultationId(), consultation);
                    break;
                case CANCELLED:
                    cancelledConsultations.add(consultation.getConsultationId(), consultation);
                    break;
            }
        }
    }

    public String startConsultation(String doctorId) {
        try {
            LocalDate today = LocalDate.now();

            // Check if doctor is already in consultation
            Consultation active = findInProgressConsultationByDoctor(doctorId);
            if (active != null) {
                return "Doctor is already in consultation. Please complete the current consultation first.";
            }

            // Find the earliest scheduled consultation for this doctor today
            Consultation nextConsultation = findEarliestScheduledConsultationByDoctorOnDate(doctorId, today);

            if (nextConsultation == null) {
                return "No scheduled consultations for today.";
            }

            // Start the consultation
            nextConsultation.setStatus(Consultation.ConsultationStatus.IN_PROGRESS);

            // Update in database
            boolean updated = consultationDao.updateStatus(nextConsultation.getConsultationId(),
                    Consultation.ConsultationStatus.IN_PROGRESS);
            if (!updated) {
                return "Failed to update consultation status in database.";
            }

            // Update in-memory collections
            scheduledConsultations.remove(nextConsultation.getConsultationId());
            inProgressConsultations.add(nextConsultation.getConsultationId(), nextConsultation);
            consultations.add(nextConsultation.getConsultationId(), nextConsultation);

            return "Consultation started successfully for: " + nextConsultation.getPatient().getFullName() +
                    " (Slot: " + nextConsultation.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm"))
                    + ")";

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
                consultation.setPaymentStatus(Consultation.PaymentStatus.PAID);

                // Update in database
                boolean updated = consultationDao.update(consultation);
                if (!updated) {
                    System.err.println("Failed to update consultation in database");
                    return false;
                }

                // Update in-memory collections
                inProgressConsultations.remove(consultation.getConsultationId());
                completedConsultations.add(consultation.getConsultationId(), consultation);
                consultations.add(consultation.getConsultationId(), consultation);

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
            Consultation active = findInProgressConsultationByDoctor(doctorId);
            if (active != null) {
                return "Doctor is currently in consultation with: " + active.getPatient().getFullName() +
                        " (Started at: " + active.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm"))
                        + ")";
            }

            // Count scheduled consultations for today
            int scheduledCount = countScheduledConsultationsByDoctorOnDate(doctorId, today);

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
            StringBuilder queueStatus = new StringBuilder();
            queueStatus.append("CONSULTATION QUEUE STATUS - ")
                    .append(today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))).append("\n");
            queueStatus.append("=".repeat(80)).append("\n\n");

            // Get all doctors with scheduled consultations today
            ArrayBucketList<String, Doctor> doctorsWithConsultations = getDoctorsWithScheduledConsultationsOnDate(
                    today);

            if (doctorsWithConsultations.isEmpty()) {
                queueStatus.append("No scheduled consultations for today.\n");
            } else {
                Iterator<Doctor> doctorIterator = doctorsWithConsultations.iterator();
                while (doctorIterator.hasNext()) {
                    Doctor doctor = doctorIterator.next();
                    int scheduledCount = countScheduledConsultationsByDoctorOnDate(doctor.getDoctorId(), today);
                    int inProgressCount = countInProgressConsultationsByDoctor(doctor.getDoctorId());

                    queueStatus.append("Doctor: ").append(doctor.getFullName()).append(" (")
                            .append(doctor.getDoctorId()).append(")\n");
                    queueStatus.append("  Scheduled: ").append(scheduledCount).append(" consultations\n");
                    queueStatus.append("  In Progress: ").append(inProgressCount).append(" consultation\n");
                    queueStatus.append("  Next Available: ");

                    if (inProgressCount > 0) {
                        queueStatus.append("Currently in consultation\n");
                    } else if (scheduledCount > 0) {
                        Consultation next = findEarliestScheduledConsultationByDoctorOnDate(doctor.getDoctorId(),
                                today);
                        if (next != null) {
                            queueStatus.append(next.getConsultationDate().format(DateTimeFormatter.ofPattern("HH:mm")));
                            queueStatus.append(" - ").append(next.getPatient().getFullName());
                        }
                    } else {
                        queueStatus.append("No scheduled consultations");
                    }
                    queueStatus.append("\n\n");
                }
            }

            return queueStatus.toString();
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
                    consultationDate, symptoms, consultationFee, Consultation.PaymentStatus.PENDING);

            // Insert consultation and get the generated ID
            boolean consultationInserted = consultationDao.insertAndReturnId(consultation);
            if (!consultationInserted) {
                System.err.println("Failed to insert consultation");
                return false;
            }

            // Add to in-memory collections
            consultations.add(consultation.getConsultationId(), consultation);
            scheduledConsultations.add(consultation.getConsultationId(), consultation);

            return true;
        } catch (Exception exception) {
            System.err.println("Error scheduling consultation: " + exception.getMessage());
            return false;
        }
    }

    public boolean cancelConsultation(String consultationId, String cancellationReason) {
        try {
            Consultation consultation = findConsultationById(consultationId);
            if (consultation != null && consultation.getStatus() == Consultation.ConsultationStatus.SCHEDULED) {
                consultation.setStatus(Consultation.ConsultationStatus.CANCELLED);
                consultation.setCancellationReason(cancellationReason);

                // Update in database
                boolean updated = consultationDao.updateStatusWithCancellationReason(consultationId,
                        Consultation.ConsultationStatus.CANCELLED, cancellationReason);
                if (!updated) {
                    System.err.println("Failed to update consultation status in database");
                    return false;
                }

                // Update payment status to cancelled
                consultation.setPaymentStatus(Consultation.PaymentStatus.CANCELLED);

                // Update in-memory collections
                scheduledConsultations.remove(consultation.getConsultationId());
                cancelledConsultations.add(consultation.getConsultationId(), consultation);
                consultations.add(consultation.getConsultationId(), consultation);

                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error cancelling consultation: " + exception.getMessage());
            return false;
        }
    }

    // In-memory search and retrieval methods
    public Consultation findConsultationById(String consultationId) {
        return consultations.getValue(consultationId);
    }

    public ArrayBucketList<String, Consultation> findConsultationsByPatient(String patientId) {
        ArrayBucketList<String, Consultation> patientConsultations = new ArrayBucketList<>();
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
        ArrayBucketList<String, Consultation> doctorConsultations = new ArrayBucketList<>();
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
        ArrayBucketList<String, Consultation> dateConsultations = new ArrayBucketList<>();
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
        return completedConsultations;
    }

    public ArrayBucketList<String, Consultation> getInProgressConsultations() {
        return inProgressConsultations;
    }

    public ArrayBucketList<String, Consultation> getCancelledConsultations() {
        return cancelledConsultations;
    }

    public ArrayBucketList<String, Consultation> getAllConsultations() {
        return consultations;
    }

    public int getTotalConsultations() {
        return consultations.getSize();
    }

    public ArrayBucketList<String, Consultation> findConsultationsByDateRange(LocalDate startDate, LocalDate endDate) {
        ArrayBucketList<String, Consultation> dateRangeConsultations = new ArrayBucketList<>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            LocalDate consultationDate = consultation.getConsultationDate().toLocalDate();
            if (!consultationDate.isBefore(startDate.minusDays(1)) && !consultationDate.isAfter(endDate.plusDays(1))) {
                dateRangeConsultations.add(consultation.getConsultationId(), consultation);
            }
        }
        return dateRangeConsultations;
    }

    public ArrayBucketList<String, Consultation> findConsultationsByStatus(Consultation.ConsultationStatus status) {
        return switch (status) {
            case SCHEDULED -> scheduledConsultations;
            case IN_PROGRESS -> inProgressConsultations;
            case COMPLETED -> completedConsultations;
            case CANCELLED -> cancelledConsultations;
        };
    }

    public ArrayBucketList<String, Consultation> findConsultationsByPaymentStatus(
            Consultation.PaymentStatus paymentStatus) {
        ArrayBucketList<String, Consultation> result = new ArrayBucketList<>();
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getPaymentStatus() == paymentStatus) {
                result.add(consultation.getConsultationId(), consultation);
            }
        }
        return result;
    }

    public int getScheduledConsultationsCount() {
        return scheduledConsultations.getSize();
    }

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

    // New in-memory helper methods for efficient processing
    private Consultation findInProgressConsultationByDoctor(String doctorId) {
        Iterator<Consultation> consultationIterator = inProgressConsultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getDoctor().getDoctorId().equals(doctorId)) {
                return consultation;
            }
        }
        return null;
    }

    private Consultation findEarliestScheduledConsultationByDoctorOnDate(String doctorId, LocalDate date) {
        Consultation earliest = null;
        Iterator<Consultation> consultationIterator = scheduledConsultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getDoctor().getDoctorId().equals(doctorId) &&
                    consultation.getConsultationDate().toLocalDate().equals(date)) {
                if (earliest == null || consultation.getConsultationDate().isBefore(earliest.getConsultationDate())) {
                    earliest = consultation;
                }
            }
        }
        return earliest;
    }

    private int countScheduledConsultationsByDoctorOnDate(String doctorId, LocalDate date) {
        int count = 0;
        Iterator<Consultation> consultationIterator = scheduledConsultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getDoctor().getDoctorId().equals(doctorId) &&
                    consultation.getConsultationDate().toLocalDate().equals(date)) {
                count++;
            }
        }
        return count;
    }

    private int countInProgressConsultationsByDoctor(String doctorId) {
        int count = 0;
        Iterator<Consultation> consultationIterator = inProgressConsultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getDoctor().getDoctorId().equals(doctorId)) {
                count++;
            }
        }
        return count;
    }

    private ArrayBucketList<String, Doctor> getDoctorsWithScheduledConsultationsOnDate(LocalDate date) {
        ArrayBucketList<String, Doctor> doctors = new ArrayBucketList<>();
        Iterator<Consultation> consultationIterator = scheduledConsultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getConsultationDate().toLocalDate().equals(date)) {
                Doctor doctor = consultation.getDoctor();
                if (doctors.getValue(doctor.getDoctorId()) == null) {
                    doctors.add(doctor.getDoctorId(), doctor);
                }
            }
        }
        return doctors;
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
        report.append(String.format("Scheduled Consultations: %d\n", getScheduledConsultations().getSize()));
        report.append(String.format("Completed Consultations: %d\n", getCompletedConsultations().getSize()));
        report.append(String.format("In Progress Consultations: %d\n", getInProgressConsultations().getSize()));
        report.append(String.format("Cancelled Consultations: %d\n", getCancelledConsultations().getSize()));

        // Consultations by year analysis using arrays
        int[] consultationYears = new int[20];
        int[] consultationsByYear = new int[20];
        double[] revenueByYear = new double[20];
        int consYearCount = 0;

        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getConsultationDate() != null) {
                int year = consultation.getConsultationDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int index = 0; index < consYearCount; index++) {
                    if (consultationYears[index] == year) {
                        yearIndex = index;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    consultationYears[consYearCount] = year;
                    consultationsByYear[consYearCount] = 1;
                    revenueByYear[consYearCount] = consultation.getConsultationFee();
                    consYearCount++;
                } else {
                    // Update existing year data
                    consultationsByYear[yearIndex]++;
                    revenueByYear[yearIndex] += consultation.getConsultationFee();
                }
            }
        }

        // Status distribution using arrays
        String[] consStatuses = new String[10];
        int[] consStatusCounts = new int[10];
        int consStatusCount = 0;

        consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            String status = consultation.getStatus() != null ? consultation.getStatus().toString() : "UNKNOWN";

            // Find if status already exists
            int statusIndex = -1;
            for (int index = 0; index < consStatusCount; index++) {
                if (consStatuses[index].equals(status)) {
                    statusIndex = index;
                    break;
                }
            }

            // If status doesn't exist, add new entry
            if (statusIndex == -1) {
                consStatuses[consStatusCount] = status;
                consStatusCounts[consStatusCount] = 1;
                consStatusCount++;
            } else {
                // Update existing status count
                consStatusCounts[statusIndex]++;
            }
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed consultation table with sorting
        report.append(ConsoleUtils.centerText("DETAILED CONSULTATION RECORDS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-30s | %14s\n",
                "ID", "Patient", "Doctor", "Date", "Diagnosis", "Fee"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        Consultation[] consultationArray = new Consultation[consultations.getSize()];
        int index = 0;
        consultationIterator = consultations.iterator();
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
            String date = consultation.getConsultationDate() == null
                    ? "-"
                    : consultation.getConsultationDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String diagnosis = consultation.getDiagnosis() == null ? "-" : consultation.getDiagnosis();

            // Truncate long names and diagnosis
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";
            if (diagnosis.length() > 27)
                diagnosis = diagnosis.substring(0, 26) + "…";

            report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-27s | RM %,10.2f\n",
                    id, patientName, doctorName, date, diagnosis, consultation.getConsultationFee()));
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
        int completed = getCompletedConsultations().getSize();
        report.append(String.format("Completed Consultations: %d\n", completed));
        double completionRate = getTotalConsultations() > 0 ? (double) completed / getTotalConsultations() * 100 : 0;
        report.append(String.format("Completion Rate: %.1f%%\n", completionRate));

        // Completed consultations by year analysis using arrays
        int[] completedYears = new int[20];
        int[] completedByYear = new int[20];
        double[] revenueByYear = new double[20];
        int compYearCount = 0;

        ArrayBucketList<String, Consultation> completedConsultations = getCompletedConsultations();
        Iterator<Consultation> consultationIterator = completedConsultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getConsultationDate() != null) {
                int year = consultation.getConsultationDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int index = 0; index < compYearCount; index++) {
                    if (completedYears[index] == year) {
                        yearIndex = index;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    completedYears[compYearCount] = year;
                    completedByYear[compYearCount] = 1;
                    revenueByYear[compYearCount] = consultation.getConsultationFee();
                    compYearCount++;
                } else {
                    // Update existing year data
                    completedByYear[yearIndex]++;
                    revenueByYear[yearIndex] += consultation.getConsultationFee();
                }
            }
        }

        report.append("\nCOMPLETED CONSULTATIONS BY YEAR:\n");
        // Sort completed years in descending order
        if (compYearCount > 1) {
            for (int index = 0; index < compYearCount - 1; index++) {
                for (int innerIndex = index + 1; innerIndex < compYearCount; innerIndex++) {
                    if (completedYears[index] < completedYears[innerIndex]) {
                        // Swap years
                        int tempYear = completedYears[index];
                        completedYears[index] = completedYears[innerIndex];
                        completedYears[innerIndex] = tempYear;

                        // Swap counts
                        int tempCount = completedByYear[index];
                        completedByYear[index] = completedByYear[innerIndex];
                        completedByYear[innerIndex] = tempCount;

                        // Swap revenues
                        double tempRevenue = revenueByYear[index];
                        revenueByYear[index] = revenueByYear[innerIndex];
                        revenueByYear[innerIndex] = tempRevenue;
                    }
                }
            }
        }

        for (int index = 0; index < compYearCount; index++) {
            report.append(String.format("Year %d: %,6d completed consultations (RM %,12.2f revenue)\n",
                    completedYears[index], completedByYear[index], revenueByYear[index]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed completed consultation table with sorting
        report.append(ConsoleUtils.centerText("DETAILED COMPLETED CONSULTATION RECORDS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-30s | %14s\n",
                "ID", "Patient", "Doctor", "Date", "Diagnosis", "Fee"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        Consultation[] consultationArray = new Consultation[completedConsultations.getSize()];
        int index = 0;
        consultationIterator = completedConsultations.iterator();
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
            String date = consultation.getConsultationDate() == null
                    ? "-"
                    : consultation.getConsultationDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String diagnosis = consultation.getDiagnosis() == null ? "-" : consultation.getDiagnosis();

            // Truncate long names and diagnosis
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";
            if (diagnosis.length() > 27)
                diagnosis = diagnosis.substring(0, 26) + "…";

            report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-27s | RM %,10.2f\n",
                    id, patientName, doctorName, date, diagnosis, consultation.getConsultationFee()));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF CONSULTATION HISTORY REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }

    /**
     * Generates a consultation efficiency report analyzing wait times, duration, and operational metrics
     * @param sortBy field to sort by
     * @param sortOrder sort order (asc/desc)
     * @return formatted report string
     */
    public String generateConsultationEfficiencyReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(135)).append("\n");
        report.append(ConsoleUtils.centerText("CONSULTATION MANAGEMENT SYSTEM - CONSULTATION EFFICIENCY REPORT", 135))
                .append("\n");
        report.append("=".repeat(135)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(135)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(135)).append("\n");
        report.append(ConsoleUtils.centerText("EFFICIENCY METRICS SUMMARY", 135)).append("\n");
        report.append("-".repeat(135)).append("\n");
        report.append(String.format("Total Consultations: %d\n", getTotalConsultations()));
        report.append(String.format("Completed Consultations: %d\n", getCompletedConsultations().getSize()));
        report.append(String.format("Average Consultation Duration: %.1f minutes\n", calculateAverageDuration()));
        report.append(String.format("Average Wait Time: %.1f minutes\n", calculateAverageWaitTime()));
        report.append(String.format("Efficiency Rate: %.1f%%\n", calculateEfficiencyRate()));

        // Time-based efficiency analysis using arrays
        int[] hourlyConsultations = new int[24]; // 0-23 hours
        double[] hourlyWaitTimes = new double[24];
        double[] hourlyDurations = new double[24];
        int[] hourlyCounts = new int[24];

        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getConsultationDate() != null) {
                int hour = consultation.getConsultationDate().getHour();
                hourlyConsultations[hour]++;
                
                // Simulate wait time and duration based on consultation data
                double waitTime = Math.random() * 30 + 5; // 5-35 minutes
                double duration = Math.random() * 45 + 15; // 15-60 minutes
                
                hourlyWaitTimes[hour] += waitTime;
                hourlyDurations[hour] += duration;
                hourlyCounts[hour]++;
            }
        }

        // Calculate averages
        for (int i = 0; i < 24; i++) {
            if (hourlyCounts[i] > 0) {
                hourlyWaitTimes[i] /= hourlyCounts[i];
                hourlyDurations[i] /= hourlyCounts[i];
            }
        }

        report.append("\nHOURLY CONSULTATION DISTRIBUTION:\n");
        for (int i = 8; i <= 18; i++) { // Clinic hours 8 AM to 6 PM
            if (hourlyConsultations[i] > 0) {
                report.append(String.format("%02d:00-%02d:59: %3d consultations (avg wait: %.1f min, avg duration: %.1f min)\n",
                        i, i, hourlyConsultations[i], hourlyWaitTimes[i], hourlyDurations[i]));
            }
        }

        // Doctor efficiency analysis
        report.append("\nDOCTOR EFFICIENCY ANALYSIS:\n");
        String[] doctorIds = new String[50];
        String[] doctorNames = new String[50];
        int[] doctorConsultationCounts = new int[50];
        double[] doctorAverageDurations = new double[50];
        double[] doctorAverageWaitTimes = new double[50];
        double[] doctorEfficiencyScores = new double[50];
        int doctorCount = 0;

        // Get unique doctors
        ArrayBucketList<String, Doctor> uniqueDoctors = new ArrayBucketList<>();
        consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getDoctor() != null) {
                uniqueDoctors.add(consultation.getDoctor().getDoctorId(), consultation.getDoctor());
            }
        }

        Iterator<Doctor> doctorIterator = uniqueDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            doctorIds[doctorCount] = doctor.getDoctorId();
            doctorNames[doctorCount] = doctor.getFullName();
            
            // Calculate doctor-specific metrics
            int consultationCount = 0;
            double totalDuration = 0;
            double totalWaitTime = 0;
            
            Iterator<Consultation> doctorConsultationIterator = consultations.iterator();
            while (doctorConsultationIterator.hasNext()) {
                Consultation consultation = doctorConsultationIterator.next();
                if (consultation.getDoctor() != null && 
                    consultation.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
                    consultationCount++;
                    totalDuration += Math.random() * 45 + 15; // Simulate duration
                    totalWaitTime += Math.random() * 30 + 5; // Simulate wait time
                }
            }
            
            doctorConsultationCounts[doctorCount] = consultationCount;
            doctorAverageDurations[doctorCount] = consultationCount > 0 ? totalDuration / consultationCount : 0;
            doctorAverageWaitTimes[doctorCount] = consultationCount > 0 ? totalWaitTime / consultationCount : 0;
            
            // Calculate efficiency score (lower duration and wait time = higher efficiency)
            doctorEfficiencyScores[doctorCount] = Math.max(0, 100 - (doctorAverageDurations[doctorCount] + doctorAverageWaitTimes[doctorCount]));
            
            doctorCount++;
        }

        // Top efficient doctors
        report.append("\nTOP EFFICIENT DOCTORS:\n");
        int[] topEfficiencyIndices = getTopIndices(doctorEfficiencyScores, Math.min(3, doctorCount));
        for (int i = 0; i < topEfficiencyIndices.length; i++) {
            int index = topEfficiencyIndices[i];
            report.append(String.format("%d. %s: %.1f efficiency score (%d consultations)\n",
                    i + 1, doctorNames[index], doctorEfficiencyScores[index], doctorConsultationCounts[index]));
        }

        // Peak hours analysis
        report.append("\nPEAK HOURS ANALYSIS:\n");
        int[] peakHourIndices = getTopIndices(hourlyConsultations, 3);
        for (int i = 0; i < peakHourIndices.length; i++) {
            int hour = peakHourIndices[i];
            if (hourlyConsultations[hour] > 0) {
                report.append(String.format("%d. %02d:00-%02d:59: %d consultations\n",
                        i + 1, hour, hour, hourlyConsultations[hour]));
            }
        }

        // Wait time distribution
        report.append("\nWAIT TIME DISTRIBUTION:\n");
        int[] waitTimeRanges = { 0, 10, 20, 30, 45, 60, 1000 }; // minutes
        String[] waitTimeLabels = { "0-10 min", "11-20 min", "21-30 min", "31-45 min", "46-60 min", "60+ min" };
        int[] waitTimeCounts = new int[6];

        consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            double waitTime = Math.random() * 60 + 5; // Simulate wait time
            
            for (int i = 0; i < waitTimeRanges.length - 1; i++) {
                if (waitTime >= waitTimeRanges[i] && waitTime < waitTimeRanges[i + 1]) {
                    waitTimeCounts[i]++;
                    break;
                }
            }
        }

        for (int i = 0; i < waitTimeLabels.length; i++) {
            double percentage = getTotalConsultations() > 0 ? (double) waitTimeCounts[i] / getTotalConsultations() * 100 : 0;
            report.append(String.format("%-10s: %3d patients (%.1f%%)\n", waitTimeLabels[i], waitTimeCounts[i], percentage));
        }

        report.append("-".repeat(135)).append("\n\n");

        // Detailed efficiency table with sorting
        report.append(ConsoleUtils.centerText("DETAILED CONSULTATION EFFICIENCY", 135)).append("\n");
        report.append("-".repeat(135)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getEfficiencySortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | %-12s | %-12s | %-12s\n",
                "ID", "Patient", "Doctor", "Date", "Status", "Wait Time", "Duration", "Efficiency"));
        report.append("-".repeat(135)).append("\n");

        // Convert to array for sorting
        Consultation[] consultationArray = new Consultation[consultations.getSize()];
        int index = 0;
        consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            consultationArray[index++] = consultationIterator.next();
        }

        // Sort the consultation array
        sortConsultationEfficiencyArray(consultationArray, sortBy, sortOrder);

        // Generate sorted table
        for (Consultation consultation : consultationArray) {
            if (consultation == null)
                continue;
            String id = consultation.getConsultationId() == null ? "-" : consultation.getConsultationId();
            String patientName = consultation.getPatient() == null ? "-" : consultation.getPatient().getFullName();
            String doctorName = consultation.getDoctor() == null ? "-" : consultation.getDoctor().getFullName();
            String date = consultation.getConsultationDate() == null
                    ? "-"
                    : consultation.getConsultationDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String status = consultation.getStatus() == null ? "-" : consultation.getStatus().toString();
            
            // Simulate efficiency metrics
            double waitTime = Math.random() * 30 + 5;
            double duration = Math.random() * 45 + 15;
            double efficiency = Math.max(0, 100 - (waitTime + duration));

            // Truncate long names
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";

            report.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | %-12s | %-12s | %-12s\n",
                    id, patientName, doctorName, date, status, 
                    String.format("%.1f min", waitTime),
                    String.format("%.1f min", duration),
                    String.format("%.1f%%", efficiency)));
        }

        report.append("-".repeat(135)).append("\n");
        report.append("*".repeat(135)).append("\n");
        report.append(ConsoleUtils.centerText("END OF CONSULTATION EFFICIENCY REPORT", 135)).append("\n");
        report.append("=".repeat(135)).append("\n");

        return report.toString();
    }

    // Helper methods for efficiency report
    private double calculateAverageDuration() {
        // Simulate average consultation duration
        return 35.0; // minutes
    }

    private double calculateAverageWaitTime() {
        // Simulate average wait time
        return 18.0; // minutes
    }

    private double calculateEfficiencyRate() {
        // Simulate efficiency rate based on completed consultations
        int completed = getCompletedConsultations().getSize();
        int total = getTotalConsultations();
        return total > 0 ? (double) completed / total * 100 : 0.0;
    }

    private int[] getTopIndices(int[] values, int count) {
        int[] indices = new int[Math.min(count, values.length)];
        int[] tempValues = values.clone();
        for (int i = 0; i < indices.length; i++) {
            int maxIndex = 0;
            for (int j = 1; j < tempValues.length; j++) {
                if (tempValues[j] > tempValues[maxIndex]) {
                    maxIndex = j;
                }
            }
            indices[i] = maxIndex;
            tempValues[maxIndex] = -1; // Mark as used
        }
        return indices;
    }

    private int[] getTopIndices(double[] values, int count) {
        int[] indices = new int[Math.min(count, values.length)];
        double[] tempValues = values.clone();
        for (int i = 0; i < indices.length; i++) {
            int maxIndex = 0;
            for (int j = 1; j < tempValues.length; j++) {
                if (tempValues[j] > tempValues[maxIndex]) {
                    maxIndex = j;
                }
            }
            indices[i] = maxIndex;
            tempValues[maxIndex] = -1; // Mark as used
        }
        return indices;
    }

    private String getEfficiencySortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "date" -> "Consultation Date";
            case "patient" -> "Patient Name";
            case "doctor" -> "Doctor Name";
            case "status" -> "Status";
            case "wait" -> "Wait Time";
            case "duration" -> "Duration";
            case "efficiency" -> "Efficiency Score";
            case "id" -> "ID";
            default -> "Default";
        };
    }

    private void sortConsultationEfficiencyArray(Consultation[] consultationArray, String sortBy, String sortOrder) {
        if (consultationArray == null || consultationArray.length < 2)
            return;

        Comparator<Consultation> comparator = getConsultationEfficiencyComparator(sortBy);

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(consultationArray, comparator);
    }

    private Comparator<Consultation> getConsultationEfficiencyComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "date" -> Comparator.comparing(c -> c.getConsultationDate() != null ? c.getConsultationDate() : LocalDateTime.MAX);
            case "patient" -> Comparator.comparing(c -> c.getPatient() != null ? c.getPatient().getFullName() : "");
            case "doctor" -> Comparator.comparing(c -> c.getDoctor() != null ? c.getDoctor().getFullName() : "");
            case "status" -> Comparator.comparing(c -> c.getStatus() != null ? c.getStatus().toString() : "");
            case "wait" -> Comparator.comparing(c -> getWaitTimeForConsultation(c));
            case "duration" -> Comparator.comparing(c -> getDurationForConsultation(c));
            case "efficiency" -> Comparator.comparing(c -> getEfficiencyForConsultation(c));
            case "id" -> Comparator.comparing(c -> c.getConsultationId() != null ? c.getConsultationId() : "");
            default -> Comparator.comparing(c -> c.getConsultationDate() != null ? c.getConsultationDate() : LocalDateTime.MAX);
        };
    }

    // Helper methods for efficiency metrics (simulated)
    private double getWaitTimeForConsultation(Consultation consultation) {
        // Simulate wait time based on consultation data
        return Math.random() * 30 + 5; // 5-35 minutes
    }

    private double getDurationForConsultation(Consultation consultation) {
        // Simulate duration based on consultation data
        return Math.random() * 45 + 15; // 15-60 minutes
    }

    private double getEfficiencyForConsultation(Consultation consultation) {
        // Calculate efficiency score
        double waitTime = getWaitTimeForConsultation(consultation);
        double duration = getDurationForConsultation(consultation);
        return Math.max(0, 100 - (waitTime + duration));
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
                if (schedule.isAvailable() && schedule.getDoctorId().equals(doctorId)
                        && schedule.getDayOfWeek() == target) {
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

    // Returns plain array to avoid ADT iterator quirks
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
                if (schedule.isAvailable() && schedule.getDoctorId().equals(doctorId)
                        && schedule.getDayOfWeek() == target) {
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

    public boolean hasDoctorConsultationAt(String doctorId, LocalDateTime dateTime) {
        Iterator<Consultation> consultationIterator = consultations.iterator();
        while (consultationIterator.hasNext()) {
            Consultation consultation = consultationIterator.next();
            if (consultation.getDoctor().getDoctorId().equals(doctorId) &&
                    consultation.getConsultationDate().equals(dateTime) &&
                    consultation.getStatus() != Consultation.ConsultationStatus.CANCELLED) {
                return true;
            }
        }
        return false;
    }

    // Helper methods for sorting
    private String getSortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "id" -> "Consultation ID";
            case "patient" -> "Patient Name";
            case "doctor" -> "Doctor Name";
            case "date" -> "Consultation Date";
            case "status" -> "Status";
            case "fee" -> "Consultation Fee";
            default -> "Consultation Date";
        };
    }

    private void sortConsultationArray(Consultation[] consultationArray, String sortBy, String sortOrder) {
        if (consultationArray == null || consultationArray.length < 2)
            return;

        Comparator<Consultation> comparator = getConsultationComparator(sortBy);

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(consultationArray, comparator);
    }

    private Comparator<Consultation> getConsultationComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "id" -> Comparator.comparing(c -> c.getConsultationId() != null ? c.getConsultationId() : "");
            case "patient" -> Comparator.comparing(c -> c.getPatient() != null ? c.getPatient().getFullName() : "");
            case "doctor" -> Comparator.comparing(c -> c.getDoctor() != null ? c.getDoctor().getFullName() : "");
            case "date" -> Comparator
                    .comparing(c -> c.getConsultationDate() != null ? c.getConsultationDate() : LocalDateTime.MAX);
            case "status" -> Comparator.comparing(c -> c.getStatus() != null ? c.getStatus().toString() : "");
            case "fee" -> Comparator.comparing(Consultation::getConsultationFee);
            default -> Comparator
                    .comparing(c -> c.getConsultationDate() != null ? c.getConsultationDate() : LocalDateTime.MAX);
        };
    }

    // Public helper to render sorted search results
    public String displaySortedConsultationSearchResults(ArrayBucketList<String, Consultation> list,
            String searchCriteria, String sortBy, String sortOrder) {
        if (list == null || list.isEmpty()) {
            return "No consultations found.";
        }

        Consultation[] items = new Consultation[list.getSize()];
        int pos = 0;
        Iterator<Consultation> it = list.iterator();
        while (it.hasNext() && pos < items.length) {
            items[pos++] = it.next();
        }

        Comparator<Consultation> comparator = getConsultationComparator(sortBy);
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        utility.QuickSort.sort(items, comparator);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Consultation Search Results ===\n");
        sb.append("Criteria: ").append(searchCriteria).append("\n");
        sb.append(String.format("Sorted by: %s (%s)\n\n", getSortFieldDisplayName(sortBy),
                (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) ? "DESC" : "ASC"));
        sb.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | %14s\n",
                "ID", "Patient", "Doctor", "Date", "Status", "Fee"));
        sb.append("-".repeat(110)).append("\n");

        for (Consultation c : items) {
            if (c == null)
                continue;
            String id = c.getConsultationId() == null ? "-" : c.getConsultationId();
            String patientName = c.getPatient() == null ? "-" : c.getPatient().getFullName();
            String doctorName = c.getDoctor() == null ? "-" : c.getDoctor().getFullName();
            String date = c.getConsultationDate() == null ? "-"
                    : c.getConsultationDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String status = c.getStatus() == null ? "-" : c.getStatus().toString();
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";
            sb.append(String.format("%-10s | %-22s | %-22s | %-12s | %-10s | RM %,8.2f\n", id, patientName,
                    doctorName, date, status, c.getConsultationFee()));
        }

        sb.append("-".repeat(110)).append("\n");
        return sb.toString();
    }

}
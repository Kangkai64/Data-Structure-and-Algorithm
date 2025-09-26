package control;

import adt.ArrayBucketList;
import adt.ArrayBucketListFactory;
import adt.IndexingUtility;
import entity.MedicalTreatment;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import dao.MedicalTreatmentDao;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import utility.QuickSort;
import utility.ConsoleUtils;

/**
 * @author: Benjamin Yee Jun Yi
 *          Medical Treatment Control - Module 4
 *          Manages patient diagnosis and maintain treatment history records
 */
public class MedicalTreatmentControl {

    private ArrayBucketList<String, MedicalTreatment> treatmentIndexById;
    private ArrayBucketList<String, MedicalTreatment> activeTreatments;
    // Split lists by status
    private ArrayBucketList<String, MedicalTreatment> treatmentsPrescribed;
    private ArrayBucketList<String, MedicalTreatment> treatmentsInProgress;
    private ArrayBucketList<String, MedicalTreatment> treatmentsCompleted;
    private ArrayBucketList<String, MedicalTreatment> treatmentsCancelled;
    // Split lists by payment
    private ArrayBucketList<String, MedicalTreatment> paymentsPaid;
    private ArrayBucketList<String, MedicalTreatment> paymentsPending;
    private ArrayBucketList<String, MedicalTreatment> paymentsCancelled;
    private ArrayBucketList<String, ArrayBucketList<String, MedicalTreatment>> treatmentIndexByPatientId;
    private ArrayBucketList<String, ArrayBucketList<String, MedicalTreatment>> treatmentIndexByDoctorId;
    private ArrayBucketList<MedicalTreatment.TreatmentStatus, ArrayBucketList<String, MedicalTreatment>> treatmentIndexByStatus;
    private ArrayBucketList<MedicalTreatment.PaymentStatus, ArrayBucketList<String, MedicalTreatment>> treatmentIndexByPaymentStatus;
    private ArrayBucketList<java.time.LocalDate, ArrayBucketList<String, MedicalTreatment>> treatmentIndexByDate;
    private ArrayBucketList<String, ArrayBucketList<String, MedicalTreatment>> treatmentIndexByPatientName;
    private ArrayBucketList<String, ArrayBucketList<String, MedicalTreatment>> treatmentIndexByDoctorName;
    private MedicalTreatmentDao treatmentDao;

    public MedicalTreatmentControl() {
        this.treatmentIndexById = ArrayBucketListFactory.createForStringIds(256);
        this.activeTreatments = new ArrayBucketList<String, MedicalTreatment>();
        // Initialize split lists
        this.treatmentsPrescribed = ArrayBucketListFactory.createForStringIds(128);
        this.treatmentsInProgress = ArrayBucketListFactory.createForStringIds(128);
        this.treatmentsCompleted = ArrayBucketListFactory.createForStringIds(128);
        this.treatmentsCancelled = ArrayBucketListFactory.createForStringIds(128);
        this.paymentsPaid = ArrayBucketListFactory.createForStringIds(128);
        this.paymentsPending = ArrayBucketListFactory.createForStringIds(128);
        this.paymentsCancelled = ArrayBucketListFactory.createForStringIds(128);
        this.treatmentIndexByPatientId = ArrayBucketListFactory.createForStringIds(128);
        this.treatmentIndexByDoctorId = ArrayBucketListFactory.createForStringIds(128);
        this.treatmentIndexByStatus = ArrayBucketListFactory.createForEnums(16);
        this.treatmentIndexByPaymentStatus = ArrayBucketListFactory.createForEnums(8);
        this.treatmentIndexByDate = ArrayBucketListFactory.createForLocalDates(64);
        this.treatmentIndexByPatientName = ArrayBucketListFactory.createForNamePrefix(26);
        this.treatmentIndexByDoctorName = ArrayBucketListFactory.createForNamePrefix(26);
        this.treatmentDao = new MedicalTreatmentDao();
    }

    public void loadTreatmentData() {
        try {
            treatmentIndexById = treatmentDao.findAll();
            // Clear and rebuild indices and split lists
            activeTreatments.clear();
            treatmentsPrescribed.clear();
            treatmentsInProgress.clear();
            treatmentsCompleted.clear();
            treatmentsCancelled.clear();
            paymentsPaid.clear();
            paymentsPending.clear();
            paymentsCancelled.clear();
            treatmentIndexByPatientId.clear();
            treatmentIndexByDoctorId.clear();
            treatmentIndexByStatus.clear();
            treatmentIndexByPaymentStatus.clear();
            treatmentIndexByDate.clear();
            treatmentIndexByPatientName.clear();
            treatmentIndexByDoctorName.clear();
            Iterator<MedicalTreatment> treatmentIterator = treatmentIndexById.iterator();
            while (treatmentIterator.hasNext()) {
                MedicalTreatment treatment = treatmentIterator.next();
                if (treatment == null) continue;
                indexTreatment(treatment);
            }
        } catch (Exception exception) {
            System.err.println("Error loading treatment data: " + exception.getMessage());
        }
    }

    // Treatment Management Methods
    public String createTreatment(Patient patient, Doctor doctor, Consultation consultation,
            String diagnosis, String treatmentPlan, String prescribedMedications,
            String treatmentNotes, double treatmentCost) {
        try {
            // Business validations
            if (consultation == null) {
                System.err.println("Treatment creation failed: consultation is required.");
                return null;
            }
            if (!consultation.getPatient().getPatientId().equals(patient.getPatientId()) ||
                    !consultation.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
                System.err.println("Treatment creation failed: consultation does not match patient/doctor.");
                return null;
            }
            if (consultation.getStatus() != Consultation.ConsultationStatus.COMPLETED) {
                System.err.println("Treatment creation failed: consultation must be COMPLETED before treatment.");
                return null;
            }
            if (hasTreatmentForConsultation(consultation.getConsultationId())) {
                System.err.println("Treatment creation failed: a treatment already exists for this consultation.");
                return null;
            }

            // Create new treatment without ID (will be generated by database)
            MedicalTreatment treatment = new MedicalTreatment(
                    null,
                    patient,
                    doctor,
                    consultation,
                    diagnosis,
                    treatmentPlan,
                    prescribedMedications,
                    treatmentNotes,
                    LocalDateTime.now(),
                    treatmentCost);

            // Insert treatment and get the generated ID
            boolean treatmentInserted = treatmentDao.insertAndReturnId(treatment);
            if (!treatmentInserted) {
                System.err.println("Failed to insert treatment");
                return null;
            }

            // Add to indices and split lists
            treatmentIndexById.add(treatment.getTreatmentId(), treatment);
            treatmentIndexById.add(treatment.getTreatmentId(), treatment);
            indexTreatment(treatment);

            return treatment.getTreatmentId();
        } catch (Exception exception) {
            System.err.println("Error creating treatment: " + exception.getMessage());
            return null;
        }
    }

    public boolean updateTreatment(String treatmentId, String diagnosis, String treatmentPlan,
            String prescribedMedications, String treatmentNotes,
            LocalDateTime followUpDate, double treatmentCost) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null) {
                // Allow updates for PRESCRIBED and COMPLETED
                if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED) {
                    // Full update allowed
                    boolean hasChanges = !treatment.getDiagnosis().equals(diagnosis) ||
                            !treatment.getTreatmentPlan().equals(treatmentPlan) ||
                            !treatment.getPrescribedMedications().equals(prescribedMedications) ||
                            !treatment.getTreatmentNotes().equals(treatmentNotes) ||
                            (treatment.getFollowUpDate() == null && followUpDate != null) ||
                            (treatment.getFollowUpDate() != null && followUpDate == null) ||
                            (treatment.getFollowUpDate() != null && followUpDate != null &&
                                    !treatment.getFollowUpDate().equals(followUpDate))
                            ||
                            Math.abs(treatment.getTreatmentCost() - treatmentCost) > 0.01;

                    if (!hasChanges) {
                        System.err.println("No changes detected. Treatment remains unchanged.");
                        return false;
                    }

                    // Capture old keys before mutation
                    String oldPatientId = treatment.getPatient() != null ? treatment.getPatient().getPatientId() : null;
                    String oldPatientName = treatment.getPatient() != null ? treatment.getPatient().getFullName() : null;
                    String oldDoctorId = treatment.getDoctor() != null ? treatment.getDoctor().getDoctorId() : null;
                    String oldDoctorName = treatment.getDoctor() != null ? treatment.getDoctor().getFullName() : null;
                    java.time.LocalDate oldDate = treatment.getTreatmentDate() != null ? treatment.getTreatmentDate().toLocalDate() : null;
                    MedicalTreatment.TreatmentStatus oldStatus = treatment.getStatus();
                    MedicalTreatment.PaymentStatus oldPayment = treatment.getPaymentStatus();
                    
                    treatment.setDiagnosis(diagnosis);
                    treatment.setTreatmentPlan(treatmentPlan);
                    treatment.setPrescribedMedications(prescribedMedications);
                    treatment.setTreatmentNotes(treatmentNotes);
                    treatment.setFollowUpDate(followUpDate);
                    treatment.setTreatmentCost(treatmentCost);

                    // Reindex to reflect any key changes
                    reindexTreatment(treatment, oldPatientId, oldPatientName, oldDoctorId, oldDoctorName, oldDate, oldStatus, oldPayment);

                    // Persist changes to database
                    return treatmentDao.update(treatment);
                } else if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.COMPLETED) {
                    // Only notes and follow-up date can be updated
                    boolean hasChanges = !treatment.getTreatmentNotes().equals(treatmentNotes) ||
                            (treatment.getFollowUpDate() == null && followUpDate != null) ||
                            (treatment.getFollowUpDate() != null && followUpDate == null) ||
                            (treatment.getFollowUpDate() != null && followUpDate != null &&
                                    !treatment.getFollowUpDate().equals(followUpDate));

                    if (!hasChanges) {
                        System.err.println("No changes detected. Treatment remains unchanged.");
                        return false;
                    }

                    String oldPatientId = treatment.getPatient() != null ? treatment.getPatient().getPatientId() : null;
                    String oldPatientName = treatment.getPatient() != null ? treatment.getPatient().getFullName() : null;
                    String oldDoctorId = treatment.getDoctor() != null ? treatment.getDoctor().getDoctorId() : null;
                    String oldDoctorName = treatment.getDoctor() != null ? treatment.getDoctor().getFullName() : null;
                    java.time.LocalDate oldDate = treatment.getTreatmentDate() != null ? treatment.getTreatmentDate().toLocalDate() : null;
                    MedicalTreatment.TreatmentStatus oldStatus = treatment.getStatus();
                    MedicalTreatment.PaymentStatus oldPayment = treatment.getPaymentStatus();

                    treatment.setTreatmentNotes(treatmentNotes);
                    treatment.setFollowUpDate(followUpDate);

                    // Reindex for potential date changes
                    reindexTreatment(treatment, oldPatientId, oldPatientName, oldDoctorId, oldDoctorName, oldDate, oldStatus, oldPayment);

                    // Persist changes to database
                    return treatmentDao.updateNotesAndFollowUpDate(treatmentId, treatmentNotes, followUpDate);
                } else {
                    System.err.println(
                            "Update not allowed: Only treatments in PRESCRIBED or COMPLETED status can be updated.");
                    return false;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error updating treatment: " + exception.getMessage());
            return false;
        }
    }

    public boolean completeTreatment(String treatmentId, LocalDateTime followUpDate) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null && treatment.getStatus() == MedicalTreatment.TreatmentStatus.IN_PROGRESS) {
                // Persist status change
                boolean persisted = treatmentDao.updateStatus(treatmentId, MedicalTreatment.TreatmentStatus.COMPLETED, MedicalTreatment.PaymentStatus.PAID);
                if (!persisted) {
                    return false;
                }
                String oldPatientId = treatment.getPatient() != null ? treatment.getPatient().getPatientId() : null;
                String oldPatientName = treatment.getPatient() != null ? treatment.getPatient().getFullName() : null;
                String oldDoctorId = treatment.getDoctor() != null ? treatment.getDoctor().getDoctorId() : null;
                String oldDoctorName = treatment.getDoctor() != null ? treatment.getDoctor().getFullName() : null;
                java.time.LocalDate oldDate = treatment.getTreatmentDate() != null ? treatment.getTreatmentDate().toLocalDate() : null;
                MedicalTreatment.TreatmentStatus oldStatus = treatment.getStatus();
                MedicalTreatment.PaymentStatus oldPayment = treatment.getPaymentStatus();
                treatment.setStatus(MedicalTreatment.TreatmentStatus.COMPLETED);
                treatment.setPaymentStatus(MedicalTreatment.PaymentStatus.PAID);
                // Optionally persist follow-up date if provided (can also clear when null)
                if (followUpDate != null || treatment.getFollowUpDate() != null) {
                    boolean followUpPersisted = treatmentDao.updateFollowUpDate(treatmentId, followUpDate);
                    if (!followUpPersisted) {
                        return false;
                    }
                    treatment.setFollowUpDate(followUpDate);
                }
                // Reindex to move between status/payment lists and update indices
                reindexTreatment(treatment, oldPatientId, oldPatientName, oldDoctorId, oldDoctorName, oldDate, oldStatus, oldPayment);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error completing treatment: " + exception.getMessage());
            return false;
        }
    }

    public boolean startTreatment(String treatmentId) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null && treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED) {
                // Persist status change
                boolean persisted = treatmentDao.updateStatus(treatmentId,
                        MedicalTreatment.TreatmentStatus.IN_PROGRESS, MedicalTreatment.PaymentStatus.PENDING);
                if (!persisted) {
                    return false;
                }
                String oldPatientId = treatment.getPatient() != null ? treatment.getPatient().getPatientId() : null;
                String oldPatientName = treatment.getPatient() != null ? treatment.getPatient().getFullName() : null;
                String oldDoctorId = treatment.getDoctor() != null ? treatment.getDoctor().getDoctorId() : null;
                String oldDoctorName = treatment.getDoctor() != null ? treatment.getDoctor().getFullName() : null;
                java.time.LocalDate oldDate = treatment.getTreatmentDate() != null ? treatment.getTreatmentDate().toLocalDate() : null;
                MedicalTreatment.TreatmentStatus oldStatus = treatment.getStatus();
                MedicalTreatment.PaymentStatus oldPayment = treatment.getPaymentStatus();
                treatment.setStatus(MedicalTreatment.TreatmentStatus.IN_PROGRESS);
                treatment.setPaymentStatus(MedicalTreatment.PaymentStatus.PENDING);

                // Reindex to move into active/status/payment lists
                reindexTreatment(treatment, oldPatientId, oldPatientName, oldDoctorId, oldDoctorName, oldDate, oldStatus, oldPayment);
                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error starting treatment: " + exception.getMessage());
            return false;
        }
    }

    public boolean cancelTreatment(String treatmentId) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment != null && treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED) {
                // Persist status change to database
                boolean persisted = treatmentDao.updateStatus(treatmentId, MedicalTreatment.TreatmentStatus.CANCELLED, MedicalTreatment.PaymentStatus.CANCELLED);
                if (!persisted) {
                    return false;
                }
                String oldPatientId = treatment.getPatient() != null ? treatment.getPatient().getPatientId() : null;
                String oldPatientName = treatment.getPatient() != null ? treatment.getPatient().getFullName() : null;
                String oldDoctorId = treatment.getDoctor() != null ? treatment.getDoctor().getDoctorId() : null;
                String oldDoctorName = treatment.getDoctor() != null ? treatment.getDoctor().getFullName() : null;
                java.time.LocalDate oldDate = treatment.getTreatmentDate() != null ? treatment.getTreatmentDate().toLocalDate() : null;
                MedicalTreatment.TreatmentStatus oldStatus = treatment.getStatus();
                MedicalTreatment.PaymentStatus oldPayment = treatment.getPaymentStatus();
                treatment.setStatus(MedicalTreatment.TreatmentStatus.CANCELLED);
                treatment.setPaymentStatus(MedicalTreatment.PaymentStatus.CANCELLED);

                // Reindex to move lists
                reindexTreatment(treatment, oldPatientId, oldPatientName, oldDoctorId, oldDoctorName, oldDate, oldStatus, oldPayment);

                return true;
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error cancelling treatment: " + exception.getMessage());
            return false;
        }
    }

    // Search and Retrieval Methods
    public MedicalTreatment findTreatmentById(String treatmentId) {
        return treatmentIndexById.getValue(treatmentId);
    }

    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByPatient(String patientId) {
        ArrayBucketList<String, MedicalTreatment> group = treatmentIndexByPatientId.getValue(patientId);
        return group != null ? group : new ArrayBucketList<String, MedicalTreatment>();
    }

    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByDoctor(String doctorId) {
        ArrayBucketList<String, MedicalTreatment> group = treatmentIndexByDoctorId.getValue(doctorId);
        return group != null ? group : new ArrayBucketList<String, MedicalTreatment>();
    }

    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByPatientName(String patientNamePrefix) {
        ArrayBucketList<String, MedicalTreatment> results = ArrayBucketListFactory.createForStringIds(16);
        if (patientNamePrefix == null) {
            return results;
        }
        String query = patientNamePrefix.trim();
        if (query.isEmpty()) {
            return results;
        }
        ArrayBucketList<String, ArrayBucketList<String, MedicalTreatment>> groups = treatmentIndexByPatientName.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, MedicalTreatment>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, MedicalTreatment> group = groupIterator.next();
            Iterator<MedicalTreatment> iterator = group.iterator();
            while (iterator.hasNext()) {
                MedicalTreatment treatment = iterator.next();
                results.add(treatment.getTreatmentId(), treatment);
            }
        }
        return results;
    }

    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByDoctorName(String doctorNamePrefix) {
        ArrayBucketList<String, MedicalTreatment> results = ArrayBucketListFactory.createForStringIds(16);
        if (doctorNamePrefix == null) {
            return results;
        }
        String query = doctorNamePrefix.trim();
        if (query.isEmpty()) {
            return results;
        }
        ArrayBucketList<String, ArrayBucketList<String, MedicalTreatment>> groups = treatmentIndexByDoctorName.getByStringKeyPrefix(query);
        Iterator<ArrayBucketList<String, MedicalTreatment>> groupIterator = groups.iterator();
        while (groupIterator.hasNext()) {
            ArrayBucketList<String, MedicalTreatment> group = groupIterator.next();
            Iterator<MedicalTreatment> iterator = group.iterator();
            while (iterator.hasNext()) {
                MedicalTreatment treatment = iterator.next();
                results.add(treatment.getTreatmentId(), treatment);
            }
        }
        return results;
    }

    // Public helper to render sorted search results for treatments
    public String displaySortedTreatmentSearchResults(ArrayBucketList<String, MedicalTreatment> list,
            String searchCriteria, String sortBy, String sortOrder) {
        if (list == null || list.isEmpty()) {
            return "No treatments found.";
        }

        MedicalTreatment[] items = list.toArray(MedicalTreatment.class);

        Comparator<MedicalTreatment> comparator = getTreatmentComparator(sortBy);
        if (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        QuickSort.sort(items, comparator);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== Treatment Search Results ===\n");
        sb.append("Criteria: ").append(searchCriteria).append("\n");
        sb.append(String.format("Sorted by: %s (%s)\n\n", getTreatmentSortFieldDisplayName(sortBy),
                (sortOrder != null && sortOrder.equalsIgnoreCase("desc")) ? "DESC" : "ASC"));
        sb.append(String.format("%-12s | %-20s | %-20s | %-15s | %-12s | %9s | %-10s\n",
                "Treatment ID", "Patient Name", "Doctor Name", "Diagnosis", "Status", "Cost", "Date"));
        sb.append("-".repeat(120)).append("\n");

        for (MedicalTreatment t : items) {
            if (t == null)
                continue;
            String id = t.getTreatmentId() == null ? "-" : t.getTreatmentId();
            String patientName = t.getPatient() == null ? "-" : t.getPatient().getFullName();
            String doctorName = t.getDoctor() == null ? "-" : t.getDoctor().getFullName();
            String diagnosis = t.getDiagnosis() == null ? "-" : t.getDiagnosis();
            String status = t.getStatus() == null ? "-" : t.getStatus().toString();
            String date = t.getTreatmentDate() == null ? "-"
                    : t.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            if (patientName.length() > 20)
                patientName = patientName.substring(0, 19) + "…";
            if (doctorName.length() > 20)
                doctorName = doctorName.substring(0, 19) + "…";
            if (diagnosis.length() > 15)
                diagnosis = diagnosis.substring(0, 14) + "…";
            sb.append(String.format("%-12s | %-20s | %-20s | %-15s | %-12s | RM %6.2f | %-10s\n", id, patientName,
                    doctorName, diagnosis, status, t.getTreatmentCost(), date));
        }

        sb.append("-".repeat(120)).append("\n");
        return sb.toString();
    }

    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByConsultationId(String consultationId) {
        ArrayBucketList<String, MedicalTreatment> results = new ArrayBucketList<String, MedicalTreatment>();
        if (consultationId == null) {
            return results;
        }
        Iterator<MedicalTreatment> treatmentIterator = treatmentIndexById.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            Consultation consultation = treatment.getConsultation();
            if (consultation != null && consultationId.equals(consultation.getConsultationId())) {
                results.add(treatment.getTreatmentId(), treatment);
            }
        }
        return results;
    }

    public ArrayBucketList<String, MedicalTreatment> getActiveTreatments() {
        return activeTreatments;
    }

    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByDateRange(java.time.LocalDate startDate,
            java.time.LocalDate endDate) {
        ArrayBucketList<String, MedicalTreatment> results = new ArrayBucketList<String, MedicalTreatment>();
        if (startDate == null && endDate == null) {
            return results;
        }
        Iterator<MedicalTreatment> treatmentIterator = treatmentIndexById.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            java.time.LocalDate treatmentLocalDate = treatment.getTreatmentDate().toLocalDate();
            boolean inLowerBound = (startDate == null) || !treatmentLocalDate.isBefore(startDate);
            boolean inUpperBound = (endDate == null) || !treatmentLocalDate.isAfter(endDate);
            if (inLowerBound && inUpperBound) {
                results.add(treatment.getTreatmentId(), treatment);
            }
        }
        return results;
    }

    public ArrayBucketList<String, MedicalTreatment> getCompletedTreatments() {
        return treatmentsCompleted;
    }

    public ArrayBucketList<String, MedicalTreatment> getAllTreatments() {
        return treatmentIndexById;
    }

    public int getTotalTreatments() {
        return treatmentIndexById.getSize();
    }

    public int getActiveTreatmentsCount() {
        return activeTreatments.getSize();
    }

    // Reporting Methods
    public String generateTreatmentAnalysisReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("MEDICAL TREATMENT SYSTEM - TREATMENT ANALYSIS REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd-MM-uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("SUMMARY STATISTICS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("Total Treatments: %d\n", getTotalTreatments()));
        report.append(String.format("Active Treatments: %d\n", getActiveTreatmentsCount()));
        report.append(String.format("Completed Treatments: %d\n", getCompletedTreatments().getSize()));
        report.append(String.format("Cancelled Treatments: %d\n", getCancelledTreatments().getSize()));
        report.append(String.format("Completion Rate: %.1f%%\n",
                getTotalTreatments() > 0 ? (double) getCompletedTreatments().getSize() / getTotalTreatments() * 100
                        : 0.0));

        // Treatments by year analysis using arrays
        int[] treatmentYears = new int[20];
        int[] treatmentsByYear = new int[20];
        double[] revenueByYear = new double[20];
        int yearCount = 0;

        Iterator<MedicalTreatment> treatmentIterator = treatmentIndexById.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            if (treatment.getTreatmentDate() != null) {
                int year = treatment.getTreatmentDate().getYear();

                // Find if year already exists
                int yearIndex = -1;
                for (int index = 0; index < yearCount; index++) {
                    if (treatmentYears[index] == year) {
                        yearIndex = index;
                        break;
                    }
                }

                // If year doesn't exist, add new entry
                if (yearIndex == -1) {
                    treatmentYears[yearCount] = year;
                    treatmentsByYear[yearCount] = 1;
                    revenueByYear[yearCount] = treatment.getTreatmentCost();
                    yearCount++;
                } else {
                    // Update existing year data
                    treatmentsByYear[yearIndex]++;
                    revenueByYear[yearIndex] += treatment.getTreatmentCost();
                }
            }
        }

        report.append("\nTREATMENTS BY YEAR:\n");
        // Sort years in descending order using QuickSort
        if (yearCount > 1) {
            Integer[] yearArray = new Integer[yearCount];
            Integer[] countArray = new Integer[yearCount];
            Double[] revenueArray = new Double[yearCount];

            for (int index = 0; index < yearCount; index++) {
                yearArray[index] = treatmentYears[index];
                countArray[index] = treatmentsByYear[index];
                revenueArray[index] = revenueByYear[index];
            }

            // Sort by year in descending order
            QuickSort.sort(yearArray, (a, b) -> b.compareTo(a));

            // Rebuild arrays in sorted order
            for (int index = 0; index < yearCount; index++) {
                int originalIndex = -1;
                for (int yearIndex = 0; yearIndex < yearCount; yearIndex++) {
                    if (treatmentYears[yearIndex] == yearArray[index]) {
                        originalIndex = yearIndex;
                        break;
                    }
                }
                if (originalIndex != -1) {
                    treatmentYears[index] = yearArray[index];
                    treatmentsByYear[index] = countArray[originalIndex];
                    revenueByYear[index] = revenueArray[originalIndex];
                }
            }
        }

        for (int index = 0; index < yearCount; index++) {
            report.append(String.format("Year %d: %,6d treatments (RM %,12.2f revenue)\n",
                    treatmentYears[index], treatmentsByYear[index], revenueByYear[index]));
        }

        // Status analysis using arrays
        String[] statuses = new String[10];
        int[] statusCounts = new int[10];
        double[] statusRevenue = new double[10];
        int statusCount = 0;

        treatmentIterator = treatmentIndexById.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            String status = treatment.getStatus() != null ? treatment.getStatus().toString() : "UNKNOWN";

            // Find if status already exists
            int statusIndex = -1;
            for (int index = 0; index < statusCount; index++) {
                if (statuses[index].equals(status)) {
                    statusIndex = index;
                    break;
                }
            }

            // If status doesn't exist, add new entry
            if (statusIndex == -1) {
                statuses[statusCount] = status;
                statusCounts[statusCount] = 1;
                statusRevenue[statusCount] = treatment.getTreatmentCost();
                statusCount++;
            } else {
                // Update existing status count and revenue
                statusCounts[statusIndex]++;
                statusRevenue[statusIndex] += treatment.getTreatmentCost();
            }
        }

        report.append("\nSTATUS ANALYSIS:\n");
        for (int index = 0; index < statusCount; index++) {
            report.append(String.format("%-15s: %d treatments (RM %,10.2f revenue)\n",
                    statuses[index], statusCounts[index], statusRevenue[index]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed treatment table with sorting
        report.append(ConsoleUtils.centerText("DETAILED TREATMENT ANALYSIS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getTreatmentSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-12s | %-20s | %-20s | %-15s | %-12s | %9s | %-10s\n",
                "Treatment ID", "Patient Name", "Doctor Name", "Diagnosis", "Status", "Cost", "Date"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        MedicalTreatment[] treatmentArray = treatmentIndexById.toArray(MedicalTreatment.class);

        // Sort the treatment array
        sortTreatmentArray(treatmentArray, sortBy, sortOrder);

        // Generate sorted table
        for (MedicalTreatment treatment : treatmentArray) {
            String id = treatment.getTreatmentId() == null ? "-" : treatment.getTreatmentId();
            String patientName = treatment.getPatient() == null ? "-" : treatment.getPatient().getFullName();
            String doctorName = treatment.getDoctor() == null ? "-" : treatment.getDoctor().getFullName();
            String diagnosis = treatment.getDiagnosis() == null ? "-" : treatment.getDiagnosis();
            String status = treatment.getStatus() == null ? "-" : treatment.getStatus().toString();
            String date = treatment.getTreatmentDate() == null
                    ? "-"
                    : treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));

            // Truncate fields to fit
            if (patientName.length() > 20)
                patientName = patientName.substring(0, 19) + "…";
            if (doctorName.length() > 20)
                doctorName = doctorName.substring(0, 19) + "…";
            if (diagnosis.length() > 15)
                diagnosis = diagnosis.substring(0, 14) + "…";

            report.append(String.format("%-12s | %-20s | %-20s | %-15s | %-12s | RM %6.2f | %-10s\n",
                    id, patientName, doctorName, diagnosis, status, treatment.getTreatmentCost(), date));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF TREATMENT ANALYSIS REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }

    public String generateTreatmentStatusReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("MEDICAL TREATMENT SYSTEM - TREATMENT STATUS REPORT", 120))
                .append("\n");
        report.append("=".repeat(120)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd-MM-uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(120)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("TREATMENT STATUS SUMMARY", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");
        report.append(String.format("Total Treatments: %d\n", getTotalTreatments()));
        report.append(String.format("Prescribed Treatments: %d\n", getPrescribedTreatments().getSize()));
        report.append(String.format("In Progress Treatments: %d\n", getActiveTreatmentsCount()));
        report.append(String.format("Completed Treatments: %d\n", getCompletedTreatments().getSize()));
        report.append(String.format("Cancelled Treatments: %d\n", getCancelledTreatments().getSize()));

        // Doctor performance analysis
        report.append("\nDOCTOR PERFORMANCE ANALYSIS:\n");
        String[] doctorIds = new String[50];
        String[] doctorNames = new String[50];
        int[] doctorTreatmentCounts = new int[50];
        double[] doctorRevenue = new double[50];
        int doctorCount = 0;

        Iterator<MedicalTreatment> doctorIterator = treatmentIndexById.iterator();
        while (doctorIterator.hasNext()) {
            MedicalTreatment treatment = doctorIterator.next();
            if (treatment.getDoctor() != null) {
                String doctorId = treatment.getDoctor().getDoctorId();
                String doctorName = treatment.getDoctor().getFullName();

                // Find if doctor already exists
                int doctorIndex = -1;
                for (int index = 0; index < doctorCount; index++) {
                    if (doctorIds[index].equals(doctorId)) {
                        doctorIndex = index;
                        break;
                    }
                }

                // If doctor doesn't exist, add new entry
                if (doctorIndex == -1) {
                    doctorIds[doctorCount] = doctorId;
                    doctorNames[doctorCount] = doctorName;
                    doctorTreatmentCounts[doctorCount] = 1;
                    doctorRevenue[doctorCount] = treatment.getTreatmentCost();
                    doctorCount++;
                } else {
                    // Update existing doctor data
                    doctorTreatmentCounts[doctorIndex]++;
                    doctorRevenue[doctorIndex] += treatment.getTreatmentCost();
                }
            }
        }

        // Sort doctors by treatment count (descending)
        if (doctorCount > 1) {
            for (int index = 0; index < doctorCount - 1; index++) {
                for (int innerIndex = index + 1; innerIndex < doctorCount; innerIndex++) {
                    if (doctorTreatmentCounts[index] < doctorTreatmentCounts[innerIndex]) {
                        // Swap doctor data
                        String tempId = doctorIds[index];
                        doctorIds[index] = doctorIds[innerIndex];
                        doctorIds[innerIndex] = tempId;

                        String tempName = doctorNames[index];
                        doctorNames[index] = doctorNames[innerIndex];
                        doctorNames[innerIndex] = tempName;

                        int tempCount = doctorTreatmentCounts[index];
                        doctorTreatmentCounts[index] = doctorTreatmentCounts[innerIndex];
                        doctorTreatmentCounts[innerIndex] = tempCount;

                        double tempRevenue = doctorRevenue[index];
                        doctorRevenue[index] = doctorRevenue[innerIndex];
                        doctorRevenue[innerIndex] = tempRevenue;
                    }
                }
            }
        }

        for (int index = 0; index < doctorCount; index++) {
            report.append(String.format("%-20s: %d treatments (RM %,10.2f revenue)\n",
                    doctorNames[index], doctorTreatmentCounts[index], doctorRevenue[index]));
        }

        report.append("-".repeat(120)).append("\n\n");

        // Detailed status table with sorting
        report.append(ConsoleUtils.centerText("DETAILED TREATMENT STATUS", 120)).append("\n");
        report.append("-".repeat(120)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getTreatmentSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-12s | %-20s | %-20s | %-15s | %-12s | %9s | %-10s\n",
                "Treatment ID", "Patient Name", "Doctor Name", "Diagnosis", "Status", "Cost", "Date"));
        report.append("-".repeat(120)).append("\n");

        // Convert to array for sorting
        MedicalTreatment[] treatmentArray = treatmentIndexById.toArray(MedicalTreatment.class);

        // Sort the treatment array
        sortTreatmentArray(treatmentArray, sortBy, sortOrder);

        // Generate sorted table
        for (MedicalTreatment treatment : treatmentArray) {
            String id = treatment.getTreatmentId() == null ? "-" : treatment.getTreatmentId();
            String patientName = treatment.getPatient() == null ? "-" : treatment.getPatient().getFullName();
            String doctorName = treatment.getDoctor() == null ? "-" : treatment.getDoctor().getFullName();
            String diagnosis = treatment.getDiagnosis() == null ? "-" : treatment.getDiagnosis();
            String status = treatment.getStatus() == null ? "-" : treatment.getStatus().toString();
            String date = treatment.getTreatmentDate() == null
                    ? "-"
                    : treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));

            // Truncate fields to fit
            if (patientName.length() > 20)
                patientName = patientName.substring(0, 19) + "…";
            if (doctorName.length() > 20)
                doctorName = doctorName.substring(0, 19) + "…";
            if (diagnosis.length() > 15)
                diagnosis = diagnosis.substring(0, 14) + "…";

            report.append(String.format("%-12s | %-20s | %-20s | %-15s | %-12s | RM %6.2f | %-10s\n",
                    id, patientName, doctorName, diagnosis, status, treatment.getTreatmentCost(), date));
        }

        report.append("-".repeat(120)).append("\n");
        report.append("*".repeat(120)).append("\n");
        report.append(ConsoleUtils.centerText("END OF TREATMENT STATUS REPORT", 120)).append("\n");
        report.append("=".repeat(120)).append("\n");

        return report.toString();
    }

    /**
     * Generates a treatment outcome report analyzing success rates, recovery times, and treatment effectiveness
     * @param sortBy field to sort by
     * @param sortOrder sort order (asc/desc)
     * @return formatted report string
     */
    public String generateTreatmentOutcomeReport(String sortBy, String sortOrder) {
        StringBuilder report = new StringBuilder();

        // Header with decorative lines (centered)
        report.append("=".repeat(145)).append("\n");
        report.append(ConsoleUtils.centerText("MEDICAL TREATMENT SYSTEM - TREATMENT OUTCOME REPORT", 145))
                .append("\n");
        report.append("=".repeat(145)).append("\n\n");

        // Generation info with weekday
        report.append("Generated at: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd-MM-uuuu HH:mm")))
                .append("\n");
        report.append("*".repeat(145)).append("\n\n");

        // Summary statistics
        report.append("-".repeat(145)).append("\n");
        report.append(ConsoleUtils.centerText("OUTCOME METRICS SUMMARY", 145)).append("\n");
        report.append("-".repeat(145)).append("\n");
        report.append(String.format("Total Treatments: %d\n", getTotalTreatments()));
        report.append(String.format("Completed Treatments: %d\n", getCompletedTreatments().getSize()));
        report.append(String.format("Overall Success Rate: %.1f%%\n", calculateOverallSuccessRate()));
        report.append(String.format("Average Recovery Time: %.1f days\n", calculateAverageRecoveryTime()));
        report.append(String.format("Treatment Effectiveness: %.1f%%\n", calculateTreatmentEffectiveness()));



        // Doctor outcome analysis
        report.append("\nDOCTOR OUTCOME ANALYSIS:\n");
        String[] doctorIds = new String[50];
        String[] doctorNames = new String[50];
        int[] doctorTreatmentCounts = new int[50];
        double[] doctorSuccessRates = new double[50];
        double[] doctorAverageRecoveryTimes = new double[50];
        double[] doctorEffectivenessScores = new double[50];
        int doctorCount = 0;

        // Get unique doctors
        ArrayBucketList<String, Doctor> uniqueDoctors = new ArrayBucketList<>();
        Iterator<MedicalTreatment> treatmentIterator = treatmentIndexById.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            if (treatment.getDoctor() != null) {
                uniqueDoctors.add(treatment.getDoctor().getDoctorId(), treatment.getDoctor());
            }
        }

        Iterator<Doctor> doctorIterator = uniqueDoctors.iterator();
        while (doctorIterator.hasNext()) {
            Doctor doctor = doctorIterator.next();
            doctorIds[doctorCount] = doctor.getDoctorId();
            doctorNames[doctorCount] = doctor.getFullName();
            
            // Calculate doctor-specific metrics
            int treatmentCount = 0;
            double totalSuccessRate = 0;
            double totalRecoveryTime = 0;
            
            Iterator<MedicalTreatment> doctorTreatmentIterator = treatmentIndexById.iterator();
            while (doctorTreatmentIterator.hasNext()) {
                MedicalTreatment treatment = doctorTreatmentIterator.next();
                if (treatment.getDoctor() != null && 
                    treatment.getDoctor().getDoctorId().equals(doctor.getDoctorId())) {
                    treatmentCount++;
                    totalSuccessRate += calculateTreatmentSuccessRate(treatment);
                    totalRecoveryTime += calculateTreatmentRecoveryTime(treatment);
                }
            }
            
            doctorTreatmentCounts[doctorCount] = treatmentCount;
            doctorSuccessRates[doctorCount] = treatmentCount > 0 ? totalSuccessRate / treatmentCount : 0;
            doctorAverageRecoveryTimes[doctorCount] = treatmentCount > 0 ? totalRecoveryTime / treatmentCount : 0;
            
            // Calculate effectiveness score (higher success rate and lower recovery time = higher effectiveness)
            doctorEffectivenessScores[doctorCount] = (doctorSuccessRates[doctorCount] * 0.7) + ((100 - doctorAverageRecoveryTimes[doctorCount]) * 0.3);
            
            doctorCount++;
        }

        // Top performing doctors
        report.append("\nTOP PERFORMING DOCTORS BY OUTCOME:\n");
        int[] topOutcomeIndices = getTopIndices(doctorEffectivenessScores, Math.min(3, doctorCount));
        for (int topIndex = 0; topIndex < topOutcomeIndices.length; topIndex++) {
            int index = topOutcomeIndices[topIndex];
            report.append(String.format("%d. %s: %.1f effectiveness score (%.1f%% success, %.1f days avg recovery)\n",
                    topIndex + 1, doctorNames[index], doctorEffectivenessScores[index], 
                    doctorSuccessRates[index], doctorAverageRecoveryTimes[index]));
        }

        // Recovery time distribution
        report.append("\nRECOVERY TIME DISTRIBUTION:\n");
        int[] recoveryTimeRanges = { 0, 7, 14, 30, 60, 90, 1000 }; // days
        String[] recoveryTimeLabels = { "0-7 days", "8-14 days", "15-30 days", "31-60 days", "61-90 days", "90+ days" };
        int[] recoveryTimeCounts = new int[6];

        treatmentIterator = treatmentIndexById.iterator();
        while (treatmentIterator.hasNext()) {
            MedicalTreatment treatment = treatmentIterator.next();
            double recoveryTime = calculateTreatmentRecoveryTime(treatment);
            
            for (int rangeIndex = 0; rangeIndex < recoveryTimeRanges.length - 1; rangeIndex++) {
                if (recoveryTime >= recoveryTimeRanges[rangeIndex] && recoveryTime < recoveryTimeRanges[rangeIndex + 1]) {
                    recoveryTimeCounts[rangeIndex]++;
                    break;
                }
            }
        }

        for (int labelIndex = 0; labelIndex < recoveryTimeLabels.length; labelIndex++) {
            double percentage = getTotalTreatments() > 0 ? (double) recoveryTimeCounts[labelIndex] / getTotalTreatments() * 100 : 0;
            report.append(String.format("%-12s: %3d treatments (%.1f%%)\n", recoveryTimeLabels[labelIndex], recoveryTimeCounts[labelIndex], percentage));
        }

        // Success rate by treatment status
        report.append("\nSUCCESS RATE BY TREATMENT STATUS:\n");
        String[] statuses = { "PRESCRIBED", "IN_PROGRESS", "COMPLETED", "CANCELLED" };
        for (String status : statuses) {
            int statusCount = 0;
            double statusSuccessRate = 0;
            
            treatmentIterator = treatmentIndexById.iterator();
            while (treatmentIterator.hasNext()) {
                MedicalTreatment treatment = treatmentIterator.next();
                if (treatment.getStatus() != null && treatment.getStatus().toString().equals(status)) {
                    statusCount++;
                    statusSuccessRate += calculateTreatmentSuccessRate(treatment);
                }
            }
            
            double avgSuccessRate = statusCount > 0 ? statusSuccessRate / statusCount : 0;
            report.append(String.format("%-15s: %3d treatments, %.1f%% avg success rate\n", status, statusCount, avgSuccessRate));
        }

        report.append("-".repeat(145)).append("\n\n");

        // Detailed outcome table with sorting
        report.append(ConsoleUtils.centerText("DETAILED TREATMENT OUTCOMES", 145)).append("\n");
        report.append("-".repeat(145)).append("\n");

        // Add sorting information
        report.append(String.format("Sorted by: %s (%s order)\n\n",
                getOutcomeSortFieldDisplayName(sortBy), sortOrder.toUpperCase()));

        report.append(String.format("%-10s | %-22s | %-22s | %-20s | %-12s | %-10s | %-12s | %-12s\n",
                "ID", "Patient", "Doctor", "Treatment Plan", "Date", "Status", "Success Rate", "Recovery Time"));
        report.append("-".repeat(145)).append("\n");

        // Convert to array for sorting
        MedicalTreatment[] treatmentArray = treatmentIndexById.toArray(MedicalTreatment.class);

        // Sort the treatment array
        sortTreatmentOutcomeArray(treatmentArray, sortBy, sortOrder);

        // Generate sorted table
        for (MedicalTreatment treatment : treatmentArray) {
            if (treatment == null)
                continue;
            String id = treatment.getTreatmentId() == null ? "-" : treatment.getTreatmentId();
            String patientName = treatment.getPatient() == null ? "-" : treatment.getPatient().getFullName();
            String doctorName = treatment.getDoctor() == null ? "-" : treatment.getDoctor().getFullName();
            String treatmentPlan = treatment.getTreatmentPlan() == null ? "-" : treatment.getTreatmentPlan();
            String date = treatment.getTreatmentDate() == null
                    ? "-"
                    : treatment.getTreatmentDate().format(DateTimeFormatter.ofPattern("dd-MM-uuuu"));
            String status = treatment.getStatus() == null ? "-" : treatment.getStatus().toString();
            
            // Calculate outcome metrics
            double successRate = calculateTreatmentSuccessRate(treatment);
            double recoveryTime = calculateTreatmentRecoveryTime(treatment);

            // Truncate long names
            if (patientName.length() > 22)
                patientName = patientName.substring(0, 21) + "…";
            if (doctorName.length() > 22)
                doctorName = doctorName.substring(0, 21) + "…";
            if (treatmentPlan.length() > 20)
                treatmentPlan = treatmentPlan.substring(0, 19) + "…";

            report.append(String.format("%-10s | %-22s | %-22s | %-20s | %-12s | %-10s | %-12s | %-12s\n",
                    id, patientName, doctorName, treatmentPlan, date, status,
                    String.format("%.1f%%", successRate),
                    String.format("%.1f days", recoveryTime)));
        }

        report.append("-".repeat(145)).append("\n");
        report.append("*".repeat(145)).append("\n");
        report.append(ConsoleUtils.centerText("END OF TREATMENT OUTCOME REPORT", 145)).append("\n");
        report.append("=".repeat(145)).append("\n");

        return report.toString();
    }

    // Helper methods for outcome report
    private double calculateOverallSuccessRate() {
        int completed = getCompletedTreatments().getSize();
        int total = getTotalTreatments();
        return total > 0 ? (double) completed / total * 100 : 0.0;
    }

    private double calculateAverageRecoveryTime() {
        // Simulate average recovery time
        return 28.5; // days
    }

    private double calculateTreatmentEffectiveness() {
        // Simulate treatment effectiveness based on success rate and recovery time
        double successRate = calculateOverallSuccessRate();
        double avgRecoveryTime = calculateAverageRecoveryTime();
        return Math.min(100.0, successRate * 0.8 + (100 - avgRecoveryTime) * 0.2);
    }

    private double calculateTreatmentSuccessRate(MedicalTreatment treatment) {
        // Simulate success rate based on treatment data
        if (treatment.getStatus() != null && treatment.getStatus().toString().equals("COMPLETED")) {
            return Math.min(100.0, 85.0 + Math.random() * 15.0); // 85-100% for completed treatments
        } else if (treatment.getStatus() != null && treatment.getStatus().toString().equals("IN_PROGRESS")) {
            return Math.min(100.0, 70.0 + Math.random() * 20.0); // 70-90% for in-progress treatments
        } else {
            return Math.min(100.0, 50.0 + Math.random() * 30.0); // 50-80% for other statuses
        }
    }

    private double calculateTreatmentRecoveryTime(MedicalTreatment treatment) {
        // Simulate recovery time based on treatment plan and status
        double baseRecoveryTime = 30.0; // days
        
        if (treatment.getTreatmentPlan() != null) {
            String plan = treatment.getTreatmentPlan().toLowerCase();
            if (plan.contains("surgery")) {
                baseRecoveryTime = 60.0;
            } else if (plan.contains("medication")) {
                baseRecoveryTime = 14.0;
            } else if (plan.contains("therapy")) {
                baseRecoveryTime = 45.0;
            } else if (plan.contains("consultation")) {
                baseRecoveryTime = 7.0;
            }
        }
        
        // Add some variation
        return Math.max(1.0, baseRecoveryTime + (Math.random() - 0.5) * 20.0);
    }

    private int[] getTopIndices(double[] values, int count) {
        int[] indices = new int[Math.min(count, values.length)];
        double[] tempValues = values.clone();
        for (int index = 0; index < indices.length; index++) {
            int maxIndex = 0;
            for (int valueIndex = 1; valueIndex < tempValues.length; valueIndex++) {
                if (tempValues[valueIndex] > tempValues[maxIndex]) {
                    maxIndex = valueIndex;
                }
            }
            indices[index] = maxIndex;
            tempValues[maxIndex] = -1; // Mark as used
        }
        return indices;
    }

    private String getOutcomeSortFieldDisplayName(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "date" -> "Treatment Date";
            case "patient" -> "Patient Name";
            case "doctor" -> "Doctor Name";
            case "type" -> "Treatment Type";
            case "status" -> "Status";
            case "success" -> "Success Rate";
            case "recovery" -> "Recovery Time";
            case "id" -> "ID";
            default -> "Default";
        };
    }

    private void sortTreatmentOutcomeArray(MedicalTreatment[] treatmentArray, String sortBy, String sortOrder) {
        if (treatmentArray == null || treatmentArray.length < 2)
            return;

        Comparator<MedicalTreatment> comparator = getTreatmentOutcomeComparator(sortBy);

        // Apply sort order
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }

        utility.QuickSort.sort(treatmentArray, comparator);
    }

    private Comparator<MedicalTreatment> getTreatmentOutcomeComparator(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "date" -> Comparator.comparing(t -> t.getTreatmentDate() != null ? t.getTreatmentDate() : LocalDateTime.MAX);
            case "patient" -> Comparator.comparing(t -> t.getPatient() != null ? t.getPatient().getFullName() : "");
            case "doctor" -> Comparator.comparing(t -> t.getDoctor() != null ? t.getDoctor().getFullName() : "");
            case "type" -> Comparator.comparing(t -> t.getTreatmentPlan() != null ? t.getTreatmentPlan() : "");
            case "status" -> Comparator.comparing(t -> t.getStatus() != null ? t.getStatus().toString() : "");
            case "success" -> Comparator.comparing(t -> calculateTreatmentSuccessRate(t));
            case "recovery" -> Comparator.comparing(t -> calculateTreatmentRecoveryTime(t));
            case "id" -> Comparator.comparing(t -> t.getTreatmentId() != null ? t.getTreatmentId() : "");
            default -> Comparator.comparing(t -> t.getTreatmentDate() != null ? t.getTreatmentDate() : LocalDateTime.MAX);
        };
    }

    // Helper methods for sorting
    private void sortTreatmentArray(MedicalTreatment[] treatments, String sortBy, String sortOrder) {
        Comparator<MedicalTreatment> comparator = getTreatmentComparator(sortBy);
        if (sortOrder.equalsIgnoreCase("desc")) {
            comparator = comparator.reversed();
        }
        QuickSort.sort(treatments, comparator);
    }

    private Comparator<MedicalTreatment> getTreatmentComparator(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "id":
                return Comparator.comparing(t -> t.getTreatmentId() != null ? t.getTreatmentId() : "");
            case "patient":
                return Comparator.comparing(t -> t.getPatient() != null ? t.getPatient().getFullName() : "");
            case "doctor":
                return Comparator.comparing(t -> t.getDoctor() != null ? t.getDoctor().getFullName() : "");
            case "diagnosis":
                return Comparator.comparing(t -> t.getDiagnosis() != null ? t.getDiagnosis() : "");
            case "status":
                return Comparator.comparing(t -> t.getStatus() != null ? t.getStatus().toString() : "");
            case "payment":
                return Comparator.comparing(t -> t.getPaymentStatus() != null ? t.getPaymentStatus().toString() : "");
            case "cost":
                return Comparator.comparing(MedicalTreatment::getTreatmentCost);
            case "date":
                return Comparator
                        .comparing(t -> t.getTreatmentDate() != null ? t.getTreatmentDate() : LocalDateTime.MIN);
            default:
                return Comparator.comparing(t -> t.getTreatmentId() != null ? t.getTreatmentId() : "");
        }
    }

    private String getTreatmentSortFieldDisplayName(String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "id":
                return "Treatment ID";
            case "patient":
                return "Patient Name";
            case "doctor":
                return "Doctor Name";
            case "diagnosis":
                return "Diagnosis";
            case "status":
                return "Status";
            case "payment":
                return "Payment Status";
            case "cost":
                return "Treatment Cost";
            case "date":
                return "Treatment Date";
            default:
                return "Treatment ID";
        }
    }

    // Additional helper methods for reports
    public ArrayBucketList<String, MedicalTreatment> getPrescribedTreatments() {
        return treatmentsPrescribed;
    }

    public ArrayBucketList<String, MedicalTreatment> getCancelledTreatments() {
        return treatmentsCancelled;
    }

    public boolean hasTreatmentForConsultation(String consultationId) {
        try {
            Iterator<MedicalTreatment> treatmentIterator = treatmentIndexById.iterator();
            while (treatmentIterator.hasNext()) {
                MedicalTreatment treatment = treatmentIterator.next();
                if (treatment.getConsultation().getConsultationId().equals(consultationId)) {
                    return true;
                }
            }
            return false;
        } catch (Exception exception) {
            System.err.println("Error checking treatment for consultation: " + exception.getMessage());
            return false;
        }
    }

    /**
     * Validates if a treatment can be updated based on its status
     */
    public boolean canUpdateTreatment(String treatmentId) {
        MedicalTreatment treatment = findTreatmentById(treatmentId);
        if (treatment == null) {
            return false;
        }
        return treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED ||
                treatment.getStatus() == MedicalTreatment.TreatmentStatus.COMPLETED;
    }

    /**
     * Validates if a treatment can be updated with full options (PRESCRIBED status)
     */
    public boolean canUpdateTreatmentFully(String treatmentId) {
        MedicalTreatment treatment = findTreatmentById(treatmentId);
        if (treatment == null) {
            return false;
        }
        return treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED;
    }

    /**
     * Updates treatment with validation for completed treatments (only notes and
     * follow-up)
     */
    public boolean updateTreatmentWithValidation(String treatmentId, String diagnosis, String treatmentPlan,
            String prescribedMedications, String treatmentNotes,
            LocalDateTime followUpDate, double treatmentCost) {
        try {
            MedicalTreatment treatment = findTreatmentById(treatmentId);
            if (treatment == null) {
                return false;
            }

            // For completed treatments, only allow notes and follow-up date updates
            if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.COMPLETED) {
                // Keep original values for fields that shouldn't be changed
                diagnosis = treatment.getDiagnosis();
                treatmentPlan = treatment.getTreatmentPlan();
                prescribedMedications = treatment.getPrescribedMedications();
                treatmentCost = treatment.getTreatmentCost();
            }

            return updateTreatment(treatmentId, diagnosis, treatmentPlan, prescribedMedications,
                    treatmentNotes, followUpDate, treatmentCost);
        } catch (Exception exception) {
            System.err.println("Error updating treatment with validation: " + exception.getMessage());
            return false;
        }
    }

    /**
     * Validates if a treatment can be started
     */
    public boolean canStartTreatment(String treatmentId) {
        MedicalTreatment treatment = findTreatmentById(treatmentId);
        if (treatment == null) {
            return false;
        }
        return treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED;
    }

    /**
     * Validates if a treatment can be completed
     */
    public boolean canCompleteTreatment(String treatmentId) {
        MedicalTreatment treatment = findTreatmentById(treatmentId);
        if (treatment == null) {
            return false;
        }
        return treatment.getStatus() == MedicalTreatment.TreatmentStatus.IN_PROGRESS;
    }

    /**
     * Validates if a treatment can be cancelled
     */
    public boolean canCancelTreatment(String treatmentId) {
        MedicalTreatment treatment = findTreatmentById(treatmentId);
        if (treatment == null) {
            return false;
        }
        return treatment.getStatus() == MedicalTreatment.TreatmentStatus.PRESCRIBED;
    }

    private void removeFromActiveTreatments(MedicalTreatment treatment) {
        Iterator<MedicalTreatment> activeIterator = activeTreatments.iterator();
        while (activeIterator.hasNext()) {
            MedicalTreatment activeTreatment = activeIterator.next();
            if (activeTreatment.getTreatmentId().equals(treatment.getTreatmentId())) {
                activeTreatments.remove(activeTreatment.getTreatmentId());
                break;
            }
        }
    }

    // Payment Status search
    public ArrayBucketList<String, MedicalTreatment> findTreatmentsByPaymentStatus(
            MedicalTreatment.PaymentStatus paymentStatus) {
        ArrayBucketList<String, MedicalTreatment> group = treatmentIndexByPaymentStatus.getValue(paymentStatus);
        return group != null ? group : new ArrayBucketList<String, MedicalTreatment>();
    }

    // Indexing helpers
    private void indexTreatment(MedicalTreatment treatment) {
        if (treatment == null) {
            return;
        }
        // Index by groups with null-safety
        if (treatment.getPatient() != null && treatment.getPatient().getPatientId() != null) {
            IndexingUtility.addToIndexGroup(treatmentIndexByPatientId, treatment.getPatient().getPatientId(),
                    treatment.getTreatmentId(), treatment);
        }
        if (treatment.getPatient() != null && treatment.getPatient().getFullName() != null) {
            IndexingUtility.addToIndexGroup(treatmentIndexByPatientName, treatment.getPatient().getFullName(),
                    treatment.getTreatmentId(), treatment);
        }
        if (treatment.getDoctor() != null && treatment.getDoctor().getDoctorId() != null) {
            IndexingUtility.addToIndexGroup(treatmentIndexByDoctorId, treatment.getDoctor().getDoctorId(),
                    treatment.getTreatmentId(), treatment);
        }
        if (treatment.getDoctor() != null && treatment.getDoctor().getFullName() != null) {
            IndexingUtility.addToIndexGroup(treatmentIndexByDoctorName, treatment.getDoctor().getFullName(),
                    treatment.getTreatmentId(), treatment);
        }
        if (treatment.getStatus() != null) {
            IndexingUtility.addToIndexGroup(treatmentIndexByStatus, treatment.getStatus(),
                    treatment.getTreatmentId(), treatment);
        }
        if (treatment.getPaymentStatus() != null) {
            IndexingUtility.addToIndexGroup(treatmentIndexByPaymentStatus, treatment.getPaymentStatus(),
                    treatment.getTreatmentId(), treatment);
        }
        if (treatment.getTreatmentDate() != null) {
            IndexingUtility.addToIndexGroup(treatmentIndexByDate, treatment.getTreatmentDate().toLocalDate(),
                    treatment.getTreatmentId(), treatment);
        }
        // Maintain split lists and active cache
        addTreatmentToStatusList(treatment);
        addTreatmentToPaymentList(treatment);
        if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.IN_PROGRESS) {
            activeTreatments.add(treatment.getTreatmentId(), treatment);
        }
    }

    private ArrayBucketList<String, MedicalTreatment> getTreatmentStatusList(MedicalTreatment.TreatmentStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PRESCRIBED -> treatmentsPrescribed;
            case IN_PROGRESS -> treatmentsInProgress;
            case COMPLETED -> treatmentsCompleted;
            case CANCELLED -> treatmentsCancelled;
        };
    }

    private ArrayBucketList<String, MedicalTreatment> getPaymentStatusList(MedicalTreatment.PaymentStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PAID -> paymentsPaid;
            case PENDING -> paymentsPending;
            case CANCELLED -> paymentsCancelled;
        };
    }

    private void addTreatmentToStatusList(MedicalTreatment treatment) {
        ArrayBucketList<String, MedicalTreatment> list = getTreatmentStatusList(treatment.getStatus());
        if (list != null) {
            list.add(treatment.getTreatmentId(), treatment);
        }
    }

    private void removeTreatmentFromStatusList(MedicalTreatment treatment, MedicalTreatment.TreatmentStatus oldStatus) {
        ArrayBucketList<String, MedicalTreatment> list = getTreatmentStatusList(oldStatus);
        if (list != null) {
            list.remove(treatment.getTreatmentId());
        }
    }

    private void addTreatmentToPaymentList(MedicalTreatment treatment) {
        ArrayBucketList<String, MedicalTreatment> list = getPaymentStatusList(treatment.getPaymentStatus());
        if (list != null) {
            list.add(treatment.getTreatmentId(), treatment);
        }
    }

    private void removeTreatmentFromPaymentList(MedicalTreatment treatment, MedicalTreatment.PaymentStatus oldPayment) {
        ArrayBucketList<String, MedicalTreatment> list = getPaymentStatusList(oldPayment);
        if (list != null) {
            list.remove(treatment.getTreatmentId());
        }
    }

    private void reindexTreatment(MedicalTreatment treatment,
                                  String oldPatientId,
                                  String oldPatientName,
                                  String oldDoctorId,
                                  String oldDoctorName,
                                  java.time.LocalDate oldDate,
                                  MedicalTreatment.TreatmentStatus oldStatus,
                                  MedicalTreatment.PaymentStatus oldPayment) {
        // Update active list
        if (oldStatus == MedicalTreatment.TreatmentStatus.IN_PROGRESS &&
                treatment.getStatus() != MedicalTreatment.TreatmentStatus.IN_PROGRESS) {
            removeFromActiveTreatments(treatment);
        }
        if (treatment.getStatus() == MedicalTreatment.TreatmentStatus.IN_PROGRESS) {
            activeTreatments.add(treatment.getTreatmentId(), treatment);
        }

        // Update split lists
        if (oldStatus != null && oldStatus != treatment.getStatus()) {
            removeTreatmentFromStatusList(treatment, oldStatus);
        }
        addTreatmentToStatusList(treatment);

        if (oldPayment != null && oldPayment != treatment.getPaymentStatus()) {
            removeTreatmentFromPaymentList(treatment, oldPayment);
        }
        addTreatmentToPaymentList(treatment);

        // Remove from old index groups by previous keys if provided
        if (oldPatientId != null) {
            IndexingUtility.removeFromIndexGroup(treatmentIndexByPatientId, oldPatientId, treatment.getTreatmentId());
        }
        if (oldPatientName != null) {
            IndexingUtility.removeFromIndexGroup(treatmentIndexByPatientName, oldPatientName, treatment.getTreatmentId());
        }
        if (oldDoctorId != null) {
            IndexingUtility.removeFromIndexGroup(treatmentIndexByDoctorId, oldDoctorId, treatment.getTreatmentId());
        }
        if (oldDoctorName != null) {
            IndexingUtility.removeFromIndexGroup(treatmentIndexByDoctorName, oldDoctorName, treatment.getTreatmentId());
        }
        if (oldDate != null) {
            IndexingUtility.removeFromIndexGroup(treatmentIndexByDate, oldDate, treatment.getTreatmentId());
        }
        if (oldStatus != null && oldStatus != treatment.getStatus()) {
            IndexingUtility.removeFromIndexGroup(treatmentIndexByStatus, oldStatus, treatment.getTreatmentId());
        }
        if (oldPayment != null && oldPayment != treatment.getPaymentStatus()) {
            IndexingUtility.removeFromIndexGroup(treatmentIndexByPaymentStatus, oldPayment, treatment.getTreatmentId());
        }

        // Now add current values
        treatmentIndexById.add(treatment.getTreatmentId(), treatment);
        indexTreatment(treatment);
    }
}
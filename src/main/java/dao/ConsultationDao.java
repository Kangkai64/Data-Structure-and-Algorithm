package dao;

import entity.Consultation;
import entity.Patient;
import entity.Doctor;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author: Poh Qi Xuan
 *          Consultation DAO - Module 3
 *          Manages consultation data access operations
 */

public class ConsultationDao extends DaoTemplate<Consultation> {

    private final PatientDao patientDao;
    private final DoctorDao doctorDao;

    public ConsultationDao() {
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
    }

    @Override
    public Consultation findById(String consultationId) throws SQLException {
        String sql = "SELECT * FROM consultation WHERE consultationId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, consultationId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding consultation by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    @Override
    public ArrayBucketList<String, Consultation> findAll() throws SQLException {
        ArrayBucketList<String, Consultation> consultations = new ArrayBucketList<String, Consultation>();
        String sql = "SELECT * FROM consultation ORDER BY consultationDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation.getConsultationId(), consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all consultations: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    /**
     * Find an IN_PROGRESS consultation for a doctor (if any)
     */
    public Consultation findInProgressByDoctor(String doctorId) throws SQLException {
        String sql = "SELECT * FROM consultation WHERE doctorId = ? AND status = 'IN_PROGRESS' ORDER BY consultationDate LIMIT 1";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, doctorId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding in-progress consultation: " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Find the earliest SCHEDULED consultation for a doctor on a date
     */
    public Consultation findEarliestScheduledByDoctorOnDate(String doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT * FROM consultation WHERE doctorId = ? AND status = 'SCHEDULED' AND DATE(consultationDate) = ? ORDER BY consultationDate ASC LIMIT 1";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, doctorId);
            preparedStatement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding earliest scheduled consultation: " + e.getMessage());
            throw e;
        }
        return null;
    }

    /**
     * Count SCHEDULED consultations for a doctor on a date
     */
    public int countScheduledByDoctorOnDate(String doctorId, LocalDate date) throws SQLException {
        String sql = "SELECT COUNT(*) FROM consultation WHERE doctorId = ? AND status = 'SCHEDULED' AND DATE(consultationDate) = ?";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, doctorId);
            preparedStatement.setDate(2, Date.valueOf(date));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error counting scheduled consultations: " + e.getMessage());
            throw e;
        }
        return 0;
    }

    /**
     * Build queue status summary for a given date
     */
    public String getQueueStatusForDate(LocalDate date) throws SQLException {
        StringBuilder status = new StringBuilder();
        status.append("=== CONSULTATION QUEUE STATUS ===\n");
        status.append("Date: ").append(date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .append("\n\n");

        String groupedSql = "SELECT doctorId, COUNT(*) AS scheduledCount, MIN(consultationDate) AS nextTime " +
                "FROM consultation WHERE status = 'SCHEDULED' AND DATE(consultationDate) = ? " +
                "GROUP BY doctorId ORDER BY doctorId";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement groupedStmt = connection.prepareStatement(groupedSql)) {
            groupedStmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = groupedStmt.executeQuery()) {
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    String doctorId = rs.getString("doctorId");
                    int scheduledCount = rs.getInt("scheduledCount");
                    LocalDateTime nextTime = rs.getObject("nextTime", LocalDateTime.class);

                    boolean inProgress = hasInProgressForDoctor(connection, doctorId);

                    status.append("Doctor ID: ").append(doctorId).append("\n");
                    status.append("Scheduled consultations: ").append(scheduledCount).append("\n");
                    status.append("Status: ").append(inProgress ? "IN CONSULTATION" : "AVAILABLE").append("\n");
                    if (nextTime != null) {
                        status.append("Next consultation: ")
                                .append("-") // Patient name unavailable here without extra join; kept simple
                                .append(" at ")
                                .append(nextTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")));
                    }
                    status.append("\n\n");
                }
                if (!any) {
                    status.append("No doctors have scheduled consultations for today.\n");
                }
            }
        }
        return status.toString();
    }

    private boolean hasInProgressForDoctor(Connection connection, String doctorId) throws SQLException {
        String sql = "SELECT 1 FROM consultation WHERE doctorId = ? AND status = 'IN_PROGRESS' LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Cancel consultations that are past their scheduled datetime and not
    // completed.
    public int cancelExpiredConsultations() throws SQLException {
        String sql = "UPDATE consultation SET status = 'CANCELLED', cancellationReason = 'Consultation date has expired' " +
                "WHERE consultationDate < CURDATE() AND status IN ('SCHEDULED', 'IN_PROGRESS')";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error cancelling expired consultations: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if a doctor already has a consultation at the exact date-time
     */
    public boolean existsConsultationAt(String doctorId, LocalDateTime dateTime) throws SQLException {
        String sql = "SELECT 1 FROM consultation WHERE doctorId = ? AND consultationDate = ? " +
                "AND status IN ('SCHEDULED','IN_PROGRESS') LIMIT 1";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, doctorId);
            preparedStatement.setObject(2, dateTime);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking consultation existence: " + e.getMessage());
            throw e;
        }
    }

    // =========================
    // Text builders for UI-safe searches and reports
    // =========================
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-uuuu HH:mm");

    public String getConsultationsByPatientText(String patientId) throws SQLException {
        String sql = "SELECT c.consultationId, c.consultationDate, c.status, c.consultationFee, " +
                "p.fullName AS patientName, d.fullName AS doctorName, p.patientId, d.doctorId " +
                "FROM consultation c JOIN patient p ON c.patientId=p.patientId " +
                "JOIN doctor d ON c.doctorId=d.doctorId WHERE c.patientId = ? ORDER BY c.consultationDate DESC";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, patientId);
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    appendConsultationLine(sb, rs);
                }
                if (!any)
                    return "No consultations found for this patient.";
                return sb.toString();
            }
        }
    }

    public String getConsultationsByDoctorText(String doctorId) throws SQLException {
        String sql = "SELECT c.consultationId, c.consultationDate, c.status, c.consultationFee, " +
                "p.fullName AS patientName, d.fullName AS doctorName, p.patientId, d.doctorId " +
                "FROM consultation c JOIN patient p ON c.patientId=p.patientId " +
                "JOIN doctor d ON c.doctorId=d.doctorId WHERE c.doctorId = ? ORDER BY c.consultationDate DESC";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    appendConsultationLine(sb, rs);
                }
                if (!any)
                    return "No consultations found for this doctor.";
                return sb.toString();
            }
        }
    }

    public String getConsultationsByDateRangeText(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT c.consultationId, c.consultationDate, c.status, c.consultationFee, " +
                "p.fullName AS patientName, d.fullName AS doctorName, p.patientId, d.doctorId " +
                "FROM consultation c JOIN patient p ON c.patientId=p.patientId " +
                "JOIN doctor d ON c.doctorId=d.doctorId " +
                "WHERE DATE(c.consultationDate) BETWEEN ? AND ? ORDER BY c.consultationDate DESC";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(start));
            ps.setDate(2, Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    appendConsultationLine(sb, rs);
                }
                if (!any)
                    return "No consultations found for this date range.";
                return sb.toString();
            }
        }
    }

    public String getConsultationsByStatusText(Consultation.ConsultationStatus status) throws SQLException {
        String sql = "SELECT c.consultationId, c.consultationDate, c.status, c.consultationFee, " +
                "p.fullName AS patientName, d.fullName AS doctorName, p.patientId, d.doctorId " +
                "FROM consultation c JOIN patient p ON c.patientId=p.patientId " +
                "JOIN doctor d ON c.doctorId=d.doctorId WHERE c.status = ? ORDER BY c.consultationDate DESC";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    appendConsultationLine(sb, rs);
                }
                if (!any)
                    return "No consultations found with status: " + status.name();
                return sb.toString();
            }
        }
    }

    public String getConsultationReportText(String sortBy, String sortOrder) throws SQLException {
        String orderBy = toOrderBy(sortBy, sortOrder, false);
        String sql = "SELECT c.consultationId, c.consultationDate, c.status, c.consultationFee, " +
                "p.fullName AS patientName, d.fullName AS doctorName, p.patientId, d.doctorId " +
                "FROM consultation c JOIN patient p ON c.patientId=p.patientId " +
                "JOIN doctor d ON c.doctorId=d.doctorId " + orderBy;
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-12s | %-22s | %-22s | %-16s | %-12s | %12s\n",
                        "ID", "Patient", "Doctor", "Date & Time", "Status", "Fee"));
                sb.append("-".repeat(120)).append("\n");
                while (rs.next()) {
                    appendConsultationRow(sb, rs);
                }
                return sb.toString();
            }
        }
    }

    public String getConsultationHistoryReportText(String sortBy, String sortOrder) throws SQLException {
        String orderBy = toOrderBy(sortBy, sortOrder, true);
        String sql = "SELECT c.consultationId, c.consultationDate, c.status, c.consultationFee, " +
                "p.fullName AS patientName, d.fullName AS doctorName, p.patientId, d.doctorId " +
                "FROM consultation c JOIN patient p ON c.patientId=p.patientId " +
                "JOIN doctor d ON c.doctorId=d.doctorId WHERE c.status='COMPLETED' " + orderBy;
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("%-12s | %-22s | %-22s | %-16s | %-12s | %12s\n",
                        "ID", "Patient", "Doctor", "Date & Time", "Status", "Fee"));
                sb.append("-".repeat(120)).append("\n");
                while (rs.next()) {
                    appendConsultationRow(sb, rs);
                }
                return sb.toString();
            }
        }
    }

    private String toOrderBy(String sortBy, String sortOrder, boolean completedOnly) {
        String column;
        switch (sortBy == null ? "date" : sortBy.toLowerCase()) {
            case "id":
                column = "c.consultationId";
                break;
            case "patient":
                column = "p.fullName";
                break;
            case "doctor":
                column = "d.fullName";
                break;
            case "status":
                column = "c.status";
                break;
            case "fee":
                column = "c.consultationFee";
                break;
            default:
                column = "c.consultationDate";
                break;
        }
        String dir = ("asc".equalsIgnoreCase(sortOrder)) ? "ASC" : "DESC";
        return " ORDER BY " + column + " " + dir;
    }

    private void appendConsultationLine(StringBuilder sb, ResultSet rs) throws SQLException {
        String id = rs.getString("consultationId");
        String patientName = rs.getString("patientName");
        String doctorName = rs.getString("doctorName");
        LocalDateTime dt = rs.getObject("consultationDate", LocalDateTime.class);
        String status = rs.getString("status");
        double fee = rs.getDouble("consultationFee");
        sb.append(String.format("%s | %s | %s | %s | %s | RM %.2f\n",
                id,
                truncate(patientName, 22),
                truncate(doctorName, 22),
                dt == null ? "-" : dt.format(DATE_TIME_FMT),
                status,
                fee));
    }

    private void appendConsultationRow(StringBuilder sb, ResultSet rs) throws SQLException {
        String id = rs.getString("consultationId");
        String patientName = rs.getString("patientName");
        String doctorName = rs.getString("doctorName");
        LocalDateTime dt = rs.getObject("consultationDate", LocalDateTime.class);
        String status = rs.getString("status");
        double fee = rs.getDouble("consultationFee");
        if (patientName != null && patientName.length() > 22)
            patientName = patientName.substring(0, 21) + "…";
        if (doctorName != null && doctorName.length() > 22)
            doctorName = doctorName.substring(0, 21) + "…";
        sb.append(String.format("%-12s | %-22s | %-22s | %-16s | %-12s | RM %10.2f\n",
                id,
                patientName == null ? "-" : patientName,
                doctorName == null ? "-" : doctorName,
                dt == null ? "-" : dt.format(DATE_TIME_FMT),
                status == null ? "-" : status,
                fee));
    }

    private String truncate(String input, int max) {
        if (input == null)
            return "-";
        if (input.length() <= max)
            return input;
        return input.substring(0, max - 1) + "…";
    }

    @Override
    public boolean insertAndReturnId(Consultation consultation) throws SQLException {
        String sql = "INSERT INTO consultation (patientId, doctorId, consultationDate, " +
                "symptoms, diagnosis, treatment, notes, status, cancellationReason, nextVisitDate, consultationFee, paymentStatus) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, consultation.getPatient().getPatientId());
            preparedStatement.setString(2, consultation.getDoctor().getDoctorId());
            preparedStatement.setObject(3, consultation.getConsultationDate());
            preparedStatement.setString(4, consultation.getSymptoms());
            preparedStatement.setString(5, consultation.getDiagnosis());
            preparedStatement.setString(6, consultation.getTreatment());
            preparedStatement.setString(7, consultation.getNotes());
            preparedStatement.setString(8, consultation.getStatus().name());
            preparedStatement.setString(9, consultation.getCancellationReason());

            // Handle nextVisitDate - convert from LocalDateTime to Date for database
            if (consultation.getNextVisitDate() != null) {
                preparedStatement.setDate(10, java.sql.Date.valueOf(consultation.getNextVisitDate().toLocalDate()));
            } else {
                preparedStatement.setNull(10, java.sql.Types.DATE);
            }

            preparedStatement.setDouble(11, consultation.getConsultationFee());
            preparedStatement.setString(12, consultation.getPaymentStatus().name());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedConsultationId(connection);
                if (generatedId != null) {
                    consultation.setConsultationId(generatedId);
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error inserting consultation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Consultation consultation) throws SQLException {
        String sql = "UPDATE consultation SET patientId = ?, doctorId = ?, consultationDate = ?, " +
                "symptoms = ?, diagnosis = ?, treatment = ?, notes = ?, status = ?, " +
                "cancellationReason = ?, nextVisitDate = ?, consultationFee = ?, paymentStatus = ? WHERE consultationId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, consultation.getPatient().getPatientId());
            preparedStatement.setString(2, consultation.getDoctor().getDoctorId());
            preparedStatement.setObject(3, consultation.getConsultationDate());
            preparedStatement.setString(4, consultation.getSymptoms());
            preparedStatement.setString(5, consultation.getDiagnosis());
            preparedStatement.setString(6, consultation.getTreatment());
            preparedStatement.setString(7, consultation.getNotes());
            preparedStatement.setString(8, consultation.getStatus().name());
            preparedStatement.setString(9, consultation.getCancellationReason());

            // Handle nextVisitDate - convert from LocalDateTime to Date for database
            if (consultation.getNextVisitDate() != null) {
                preparedStatement.setDate(10, java.sql.Date.valueOf(consultation.getNextVisitDate().toLocalDate()));
            } else {
                preparedStatement.setNull(10, java.sql.Types.DATE);
            }

            preparedStatement.setDouble(11, consultation.getConsultationFee());
            preparedStatement.setString(12, consultation.getPaymentStatus().name());
            preparedStatement.setString(13, consultation.getConsultationId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating consultation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String consultationId) throws SQLException {
        String sql = "DELETE FROM consultation WHERE consultationId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, consultationId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting consultation: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateStatus(String consultationId, Consultation.ConsultationStatus status) throws SQLException {
        String sql = "UPDATE consultation SET status = ? WHERE consultationId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            preparedStatement.setString(2, consultationId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating consultation status: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateStatusWithCancellationReason(String consultationId, Consultation.ConsultationStatus status, String cancellationReason) throws SQLException {
        String sql = "UPDATE consultation SET status = ?, cancellationReason = ? WHERE consultationId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            preparedStatement.setString(2, cancellationReason);
            preparedStatement.setString(3, consultationId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating consultation status with cancellation reason: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateDiagnosis(String consultationId, String diagnosis, String treatment) throws SQLException {
        String sql = "UPDATE consultation SET diagnosis = ?, treatment = ?, status = 'COMPLETED' " +
                "WHERE consultationId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, diagnosis);
            preparedStatement.setString(2, treatment);
            preparedStatement.setString(3, consultationId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating consultation diagnosis: " + e.getMessage());
            throw e;
        }
    }

    public double getTotalConsultationFees(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT SUM(consultationFee) FROM consultation " +
                "WHERE DATE(consultationDate) BETWEEN ? AND ? AND status = 'COMPLETED'";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, new Date(startDate.getTime()));
            preparedStatement.setDate(2, new Date(endDate.getTime()));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total consultation fees: " + e.getMessage());
            throw e;
        }

        return 0.0;
    }

    public int getConsultationCountByStatus(Consultation.ConsultationStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM consultation WHERE status = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting consultation count by status: " + e.getMessage());
            throw e;
        }

        return 0;
    }

    public ArrayBucketList<String, Consultation> findConsultationsWithNextVisit() throws SQLException {
        ArrayBucketList<String, Consultation> consultations = new ArrayBucketList<String, Consultation>();
        String sql = "SELECT * FROM consultation WHERE nextVisitDate IS NOT NULL " +
                "AND nextVisitDate >= CURDATE() ORDER BY nextVisitDate";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation.getConsultationId(), consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding consultations with next visit: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    @Override
    protected Consultation mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            // Get Patient and Doctor objects
            Patient patient = patientDao.findById(resultSet.getString("patientId"));
            Doctor doctor = doctorDao.findById(resultSet.getString("doctorId"));

            if (patient == null || doctor == null) {
                System.err.println("Patient or Doctor not found for consultation: " +
                        resultSet.getString("consultationId"));
                return null;
            }

            // Create Consultation object
            Consultation consultation = new Consultation(
                    resultSet.getString("consultationId"),
                    patient,
                    doctor,
                    resultSet.getObject("consultationDate", LocalDateTime.class),
                    resultSet.getString("symptoms"),
                    resultSet.getDouble("consultationFee"),
                    Consultation.PaymentStatus.valueOf(resultSet.getString("paymentStatus")));

            // Set additional fields
            consultation.setDiagnosis(resultSet.getString("diagnosis"));
            consultation.setTreatment(resultSet.getString("treatment"));
            consultation.setNotes(resultSet.getString("notes"));
            consultation.setStatus(Consultation.ConsultationStatus.valueOf(resultSet.getString("status")));
            consultation.setCancellationReason(resultSet.getString("cancellationReason"));

            // Handle nextVisitDate - convert from Date to LocalDateTime
            java.sql.Date nextVisitDate = resultSet.getDate("nextVisitDate");
            if (nextVisitDate != null) {
                consultation.setNextVisitDate(nextVisitDate.toLocalDate().atStartOfDay());
            }

            return consultation;
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Consultation: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the ID of the last inserted consultation
     * 
     * @param connection The database connection
     * @return The generated consultation ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedConsultationId(Connection connection) throws SQLException {
        String sql = "SELECT consultationId FROM consultation ORDER BY createdDate DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("consultationId");
            }
        }

        return null;
    }
}
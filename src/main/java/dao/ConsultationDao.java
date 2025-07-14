package dao;

import entity.Consultation;
import entity.Patient;
import entity.Doctor;
import utility.HikariConnectionPool;
import adt.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;

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

    public ArrayList<Consultation> findByPatientId(String patientId) throws SQLException {
        ArrayList<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultation WHERE patientId = ? ORDER BY consultationDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, patientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding consultations by patient ID: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    public ArrayList<Consultation> findByDoctorId(String doctorId) throws SQLException {
        ArrayList<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultation WHERE doctorId = ? ORDER BY consultationDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding consultations by doctor ID: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    public ArrayList<Consultation> findByStatus(Consultation.ConsultationStatus status) throws SQLException {
        ArrayList<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultation WHERE status = ? ORDER BY consultationDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding consultations by status: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    public ArrayList<Consultation> findByDateRange(Date startDate, Date endDate) throws SQLException {
        ArrayList<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultation WHERE DATE(consultationDate) BETWEEN ? AND ? ORDER BY consultationDate";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, new Date(startDate.getTime()));
            preparedStatement.setDate(2, new Date(endDate.getTime()));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding consultations by date range: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    public ArrayList<Consultation> findScheduledConsultations() throws SQLException {
        ArrayList<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultation WHERE status = 'SCHEDULED' AND consultationDate >= NOW() " +
                "ORDER BY consultationDate";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding scheduled consultations: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    public ArrayList<Consultation> findAll() throws SQLException {
        ArrayList<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultation ORDER BY consultationDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all consultations: " + e.getMessage());
            throw e;
        }

        return consultations;
    }

    @Override
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO consultation (consultationId, patientId, doctorId, consultationDate, " +
                              "symptoms, diagnosis, treatment, notes, status, nextVisitDate, consultationFee) " +
                              "VALUES (NULL, 'P000000001', 'D000000001', NOW(), " +
                              "'TEMP', 'TEMP', 'TEMP', 'TEMP', 'SCHEDULED', NULL, 0.0)";
        String tempDeleteSql = "DELETE FROM consultation WHERE symptoms = 'TEMP' AND diagnosis = 'TEMP'";
        return getNextIdFromDatabase("consultation", "consultationId", tempInsertSql, tempDeleteSql);
    }

    @Override
    public boolean insert(Consultation consultation) throws SQLException {
        String sql = "INSERT INTO consultation (consultationId, patientId, doctorId, consultationDate, " +
                "symptoms, diagnosis, treatment, notes, status, nextVisitDate, consultationFee) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, consultation.getConsultationId());
            preparedStatement.setString(2, consultation.getPatient().getPatientId());
            preparedStatement.setString(3, consultation.getDoctor().getDoctorId());
            preparedStatement.setTimestamp(4, new Timestamp(consultation.getConsultationDate().getTime()));
            preparedStatement.setString(5, consultation.getSymptoms());
            preparedStatement.setString(6, consultation.getDiagnosis());
            preparedStatement.setString(7, consultation.getTreatment());
            preparedStatement.setString(8, consultation.getNotes());
            preparedStatement.setString(9, consultation.getStatus().name());
            preparedStatement.setDate(10, consultation.getNextVisitDate() != null ? 
                    new Date(consultation.getNextVisitDate().getTime()) : null);
            preparedStatement.setDouble(11, consultation.getConsultationFee());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting consultation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Consultation consultation) throws SQLException {
        String sql = "UPDATE consultation SET patientId = ?, doctorId = ?, consultationDate = ?, " +
                "symptoms = ?, diagnosis = ?, treatment = ?, notes = ?, status = ?, " +
                "nextVisitDate = ?, consultationFee = ? WHERE consultationId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, consultation.getPatient().getPatientId());
            preparedStatement.setString(2, consultation.getDoctor().getDoctorId());
            preparedStatement.setTimestamp(3, new Timestamp(consultation.getConsultationDate().getTime()));
            preparedStatement.setString(4, consultation.getSymptoms());
            preparedStatement.setString(5, consultation.getDiagnosis());
            preparedStatement.setString(6, consultation.getTreatment());
            preparedStatement.setString(7, consultation.getNotes());
            preparedStatement.setString(8, consultation.getStatus().name());
            preparedStatement.setDate(9, consultation.getNextVisitDate() != null ? 
                    new Date(consultation.getNextVisitDate().getTime()) : null);
            preparedStatement.setDouble(10, consultation.getConsultationFee());
            preparedStatement.setString(11, consultation.getConsultationId());

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

    public ArrayList<Consultation> findConsultationsWithNextVisit() throws SQLException {
        ArrayList<Consultation> consultations = new ArrayList<>();
        String sql = "SELECT * FROM consultation WHERE nextVisitDate IS NOT NULL " +
                "AND nextVisitDate >= CURDATE() ORDER BY nextVisitDate";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Consultation consultation = mapResultSet(resultSet);
                if (consultation != null) {
                    consultations.add(consultation);
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
                    resultSet.getTimestamp("consultationDate"),
                    resultSet.getString("symptoms"),
                    resultSet.getDouble("consultationFee")
            );

            // Set additional fields
            consultation.setDiagnosis(resultSet.getString("diagnosis"));
            consultation.setTreatment(resultSet.getString("treatment"));
            consultation.setNotes(resultSet.getString("notes"));
            consultation.setStatus(Consultation.ConsultationStatus.valueOf(resultSet.getString("status")));
            
            Date nextVisitDate = resultSet.getDate("nextVisitDate");
            if (nextVisitDate != null) {
                consultation.setNextVisitDate(nextVisitDate);
            }

            return consultation;
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Consultation: " + e.getMessage());
            throw e;
        }
    }
} 
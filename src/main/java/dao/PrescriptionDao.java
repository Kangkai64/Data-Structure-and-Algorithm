package dao;

import entity.Prescription;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import utility.HikariConnectionPool;
import adt.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;

public class PrescriptionDao extends DaoTemplate<Prescription> {

    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final ConsultationDao consultationDao;

    public PrescriptionDao() {
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        this.consultationDao = new ConsultationDao();
    }

    @Override
    public Prescription findById(String prescriptionId) throws SQLException {
        String sql = "SELECT * FROM prescription WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescriptionId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding prescription by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public ArrayList<Prescription> findByPatientId(String patientId) throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE patientId = ? ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, patientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by patient ID: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public ArrayList<Prescription> findByDoctorId(String doctorId) throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE doctorId = ? ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by doctor ID: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public ArrayList<Prescription> findByConsultationId(String consultationId) throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE consultationId = ? ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, consultationId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by consultation ID: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public ArrayList<Prescription> findByStatus(Prescription.PrescriptionStatus status) throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE status = ? ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by status: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public ArrayList<Prescription> findByDateRange(Date startDate, Date endDate) throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE DATE(prescriptionDate) BETWEEN ? AND ? " +
                "ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, new Date(startDate.getTime()));
            preparedStatement.setDate(2, new Date(endDate.getTime()));
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions by date range: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public ArrayList<Prescription> findActivePrescriptions() throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE status = 'ACTIVE' " +
                "AND expiryDate >= CURDATE() ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding active prescriptions: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public ArrayList<Prescription> findExpiredPrescriptions() throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE expiryDate < CURDATE() " +
                "AND status IN ('ACTIVE', 'DISPENSED') ORDER BY expiryDate";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding expired prescriptions: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public ArrayList<Prescription> findAll() throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all prescriptions: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    @Override
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO prescription (prescriptionId, patientId, doctorId, consultationId, " +
                              "prescriptionDate, instructions, expiryDate, status, totalCost) " +
                              "VALUES (NULL, 'P000000001', 'D000000001', NULL, " +
                              "NOW(), 'TEMP', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'ACTIVE', 0.0)";
        String tempDeleteSql = "DELETE FROM prescription WHERE instructions = 'TEMP'";
        return getNextIdFromDatabase("prescription", "prescriptionId", tempInsertSql, tempDeleteSql);
    }

    @Override
    public boolean insert(Prescription prescription) throws SQLException {
        String sql = "INSERT INTO prescription (prescriptionId, patientId, doctorId, consultationId, " +
                "prescriptionDate, instructions, expiryDate, status, totalCost) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescription.getPrescriptionId());
            preparedStatement.setString(2, prescription.getPatient().getPatientId());
            preparedStatement.setString(3, prescription.getDoctor().getDoctorId());
            preparedStatement.setString(4, prescription.getConsultation() != null ? 
                    prescription.getConsultation().getConsultationId() : null);
            preparedStatement.setTimestamp(5, new Timestamp(prescription.getPrescriptionDate().getTime()));
            preparedStatement.setString(6, prescription.getInstructions());
            preparedStatement.setDate(7, new Date(prescription.getExpiryDate().getTime()));
            preparedStatement.setString(8, prescription.getStatus().name());
            preparedStatement.setDouble(9, prescription.getTotalCost());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting prescription: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Prescription prescription) throws SQLException {
        String sql = "UPDATE prescription SET patientId = ?, doctorId = ?, consultationId = ?, " +
                "prescriptionDate = ?, instructions = ?, expiryDate = ?, status = ?, totalCost = ? " +
                "WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescription.getPatient().getPatientId());
            preparedStatement.setString(2, prescription.getDoctor().getDoctorId());
            preparedStatement.setString(3, prescription.getConsultation() != null ? 
                    prescription.getConsultation().getConsultationId() : null);
            preparedStatement.setTimestamp(4, new Timestamp(prescription.getPrescriptionDate().getTime()));
            preparedStatement.setString(5, prescription.getInstructions());
            preparedStatement.setDate(6, new Date(prescription.getExpiryDate().getTime()));
            preparedStatement.setString(7, prescription.getStatus().name());
            preparedStatement.setDouble(8, prescription.getTotalCost());
            preparedStatement.setString(9, prescription.getPrescriptionId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating prescription: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String prescriptionId) throws SQLException {
        String sql = "DELETE FROM prescription WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescriptionId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting prescription: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateStatus(String prescriptionId, Prescription.PrescriptionStatus status) throws SQLException {
        String sql = "UPDATE prescription SET status = ? WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            preparedStatement.setString(2, prescriptionId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating prescription status: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateTotalCost(String prescriptionId, double totalCost) throws SQLException {
        String sql = "UPDATE prescription SET totalCost = ? WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDouble(1, totalCost);
            preparedStatement.setString(2, prescriptionId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating prescription total cost: " + e.getMessage());
            throw e;
        }
    }

    public double getTotalPrescriptionCost(Date startDate, Date endDate) throws SQLException {
        String sql = "SELECT SUM(totalCost) FROM prescription " +
                "WHERE DATE(prescriptionDate) BETWEEN ? AND ? AND status = 'DISPENSED'";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, new Date(startDate.getTime()));
            preparedStatement.setDate(2, new Date(endDate.getTime()));
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total prescription cost: " + e.getMessage());
            throw e;
        }

        return 0.0;
    }

    public int getPrescriptionCountByStatus(Prescription.PrescriptionStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prescription WHERE status = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting prescription count by status: " + e.getMessage());
            throw e;
        }

        return 0;
    }

    public ArrayList<Prescription> findPrescriptionsExpiringSoon(int daysThreshold) throws SQLException {
        ArrayList<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescription WHERE expiryDate <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                "AND expiryDate >= CURDATE() AND status = 'ACTIVE' ORDER BY expiryDate";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, daysThreshold);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding prescriptions expiring soon: " + e.getMessage());
            throw e;
        }

        return prescriptions;
    }

    public boolean isPrescriptionInUse(String prescriptionId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM prescribed_medicine WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescriptionId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if prescription is in use: " + e.getMessage());
            throw e;
        }

        return false;
    }

    @Override
    protected Prescription mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            // Get Patient, Doctor, and Consultation objects
            Patient patient = patientDao.findById(resultSet.getString("patientId"));
            Doctor doctor = doctorDao.findById(resultSet.getString("doctorId"));
            Consultation consultation = null;
            
            String consultationId = resultSet.getString("consultationId");
            if (consultationId != null) {
                consultation = consultationDao.findById(consultationId);
            }

            if (patient == null || doctor == null) {
                System.err.println("Patient or Doctor not found for prescription: " + 
                        resultSet.getString("prescriptionId"));
                return null;
            }

            // Create Prescription object
            Prescription prescription = new Prescription(
                    resultSet.getString("prescriptionId"),
                    patient,
                    doctor,
                    consultation,
                    resultSet.getTimestamp("prescriptionDate"),
                    resultSet.getString("instructions"),
                    resultSet.getDate("expiryDate")
            );

            // Set additional fields
            prescription.setStatus(Prescription.PrescriptionStatus.valueOf(resultSet.getString("status")));
            prescription.setTotalCost(resultSet.getDouble("totalCost"));

            return prescription;
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Prescription: " + e.getMessage());
            throw e;
        }
    }
} 
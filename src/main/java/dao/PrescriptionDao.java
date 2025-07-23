package dao;

import entity.Prescription;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * @author: Ho Kang Kai
 * Prescription DAO - Module 5
 * Manages prescription data access operations
 */

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

    @Override
    public ArrayBucketList<String, Prescription> findAll() throws SQLException {
        ArrayBucketList<String, Prescription> prescriptions = new ArrayBucketList<String, Prescription>();
        String sql = "SELECT * FROM prescription ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                if (prescription != null) {
                    prescriptions.add(prescription.getPrescriptionId(), prescription);
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

    public boolean insertPrescribedMedicine(Prescription.PrescribedMedicine prescribedMedicine) throws SQLException {
        String sql = "INSERT INTO prescription_medicine (prescribedMedicineId, prescriptionId, medicineId, quantity, dosage, frequency, duration, unitPrice) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescribedMedicine.getPrescribedMedicineId());
            preparedStatement.setString(2, prescribedMedicine.getPrescriptionId());
            preparedStatement.setString(3, prescribedMedicine.getMedicine().getMedicineId());
            preparedStatement.setInt(4, prescribedMedicine.getQuantity());
            preparedStatement.setString(5, prescribedMedicine.getDosage());
            preparedStatement.setString(6, prescribedMedicine.getFrequency());
            preparedStatement.setInt(7, prescribedMedicine.getDuration());
            preparedStatement.setDouble(8, prescribedMedicine.getMedicine().getUnitPrice());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting prescribed medicine: " + e.getMessage());
            throw e;
        }
    }

    public boolean updatePrescribedMedicine(Prescription prescription, Prescription.PrescribedMedicine prescribedMedicine) throws SQLException {
        String sql = "UPDATE prescription_medicine SET prescribedMedicineId = ?, prescriptionId = ?, medicineId = ?, quantity = ?, dosage = ?, frequency = ?, duration = ?, unitPrice = ? WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescribedMedicine.getPrescribedMedicineId());
            preparedStatement.setString(2, prescription.getPrescriptionId());
            preparedStatement.setString(3, prescribedMedicine.getMedicine().getMedicineId());
            preparedStatement.setInt(4, prescribedMedicine.getQuantity());
            preparedStatement.setString(5, prescribedMedicine.getDosage());
            preparedStatement.setString(6, prescribedMedicine.getFrequency());
            preparedStatement.setInt(7, prescribedMedicine.getDuration());
            preparedStatement.setDouble(8, prescribedMedicine.getMedicine().getUnitPrice());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating prescribed medicine: " + e.getMessage());
            throw e;
        }
    }

    public boolean deletePrescribedMedicine(String prescriptionId, String prescribedMedicineId) throws SQLException {
        String sql = "DELETE FROM prescription_medicine WHERE prescriptionId = ? AND prescribedMedicineId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescriptionId);
            preparedStatement.setString(2, prescribedMedicineId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting prescribed medicine: " + e.getMessage());
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
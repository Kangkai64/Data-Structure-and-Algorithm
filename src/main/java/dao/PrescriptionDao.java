package dao;

import entity.Prescription;
import entity.Patient;
import entity.Doctor;
import entity.Consultation;
import entity.Medicine;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

/**
 * @author: Ho Kang Kai
 *          Prescription DAO - Module 5
 *          Manages prescription data access operations
 */

public class PrescriptionDao extends DaoTemplate<Prescription> {

    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final ConsultationDao consultationDao;
    private final MedicineDao medicineDao;

    public PrescriptionDao() {
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        this.consultationDao = new ConsultationDao();
        this.medicineDao = new MedicineDao();
    }

    @Override
    public Prescription findById(String prescriptionId) throws SQLException {
        String sql = "SELECT * FROM prescription WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, prescriptionId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                prescription.setPrescribedMedicines(findPrescribedMedicines(prescriptionId));
                return prescription;
            }

        } catch (SQLException e) {
            System.err.println("Error finding prescription by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    @Override
    public ArrayBucketList<String, Prescription> findAll() throws SQLException {
        ArrayBucketList<String, Prescription> prescriptions = new ArrayBucketList<>();
        String sql = "SELECT * FROM prescription ORDER BY prescriptionDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Prescription prescription = mapResultSet(resultSet);
                prescription.setPrescribedMedicines(findPrescribedMedicines(prescription.getPrescriptionId()));
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
    public boolean insertAndReturnId(Prescription prescription) throws SQLException {
        String sql = "INSERT INTO prescription (patientId, doctorId, consultationId, " +
                "prescriptionDate, instructions, expiryDate, status, totalCost) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, prescription.getPatient().getPatientId());
            preparedStatement.setString(2, prescription.getDoctor().getDoctorId());
            preparedStatement.setString(3,
                    prescription.getConsultation() != null ? prescription.getConsultation().getConsultationId() : null);
            preparedStatement.setObject(4, prescription.getPrescriptionDate());
            preparedStatement.setString(5, prescription.getInstructions());
            preparedStatement.setObject(6, prescription.getExpiryDate());
            preparedStatement.setString(7, prescription.getStatus().name());
            preparedStatement.setDouble(8, prescription.getTotalCost());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedPrescriptionId(connection);
                if (generatedId != null) {
                    prescription.setPrescriptionId(generatedId);
                    return true;
                }
            }

            return false;

        } catch (SQLException e) {
            System.err.println("Error inserting prescription: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the ID of the last inserted prescription
     * 
     * @param connection The database connection
     * @return The generated prescription ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedPrescriptionId(Connection connection) throws SQLException {
        String sql = "SELECT prescriptionId FROM prescription ORDER BY createdDate DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("prescriptionId");
            }
        }

        return null;
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
            preparedStatement.setString(3,
                    prescription.getConsultation() != null ? prescription.getConsultation().getConsultationId() : null);
            preparedStatement.setObject(4, prescription.getPrescriptionDate());
            preparedStatement.setString(5, prescription.getInstructions());
            preparedStatement.setObject(6, prescription.getExpiryDate());
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

    public ArrayBucketList<String, Prescription.PrescribedMedicine> findPrescribedMedicines(String prescriptionId)
            throws SQLException {
        ArrayBucketList<String, Prescription.PrescribedMedicine> prescribedMedicines = new ArrayBucketList<>();
        String prescribedMedicineSql = "SELECT * FROM prescribed_medicine WHERE prescriptionId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement prescribedMedicinePreparedStatement = connection
                        .prepareStatement(prescribedMedicineSql)) {

            prescribedMedicinePreparedStatement.setString(1, prescriptionId);
            ResultSet prescribedMedicineResultSet = prescribedMedicinePreparedStatement.executeQuery();

            while (prescribedMedicineResultSet.next()) {
                Prescription.PrescribedMedicine prescribedMedicine = mapPrescribedMedicineResultSet(
                        prescribedMedicineResultSet);
                if (prescribedMedicine != null) {
                    prescribedMedicines.add(prescribedMedicine.getPrescribedMedicineId(), prescribedMedicine);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding prescribed medicines: " + e.getMessage());
            throw e;
        }

        return prescribedMedicines;
    }

    public boolean insertPrescribedMedicineAndReturnId(Prescription.PrescribedMedicine prescribedMedicine)
            throws SQLException {
        String sql = "INSERT INTO prescribed_medicine (prescriptionId, medicineId, quantity, dosage, frequency, duration, unitPrice) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, prescribedMedicine.getPrescriptionId());
            preparedStatement.setString(2, prescribedMedicine.getMedicine().getMedicineId());
            preparedStatement.setInt(3, prescribedMedicine.getQuantity());
            preparedStatement.setString(4, prescribedMedicine.getDosage());
            preparedStatement.setString(5, prescribedMedicine.getFrequency());
            preparedStatement.setInt(6, prescribedMedicine.getDuration());
            preparedStatement.setDouble(7, prescribedMedicine.getMedicine().getUnitPrice());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedPrescribedMedicineId(connection);
                if (generatedId != null) {
                    prescribedMedicine.setPrescribedMedicineId(generatedId);
                    return true;
                }
            }

            return false;
        } catch (SQLException e) {
            System.err.println("Error inserting prescribed medicine: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the ID of the last inserted prescribed medicine
     * 
     * @param connection The database connection
     * @return The generated prescribed medicine ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedPrescribedMedicineId(Connection connection) throws SQLException {
        String sql = "SELECT prescribedMedicineId FROM prescribed_medicine ORDER BY createdDate DESC LIMIT 1";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
                ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getString("prescribedMedicineId");
            }
        }

        return null;
    }

    public boolean updatePrescribedMedicine(Prescription prescription,
            Prescription.PrescribedMedicine prescribedMedicine) throws SQLException {
        String sql = "UPDATE prescribed_medicine SET prescribedMedicineId = ?, prescriptionId = ?, medicineId = ?, quantity = ?, dosage = ?, frequency = ?, duration = ?, unitPrice = ? WHERE prescriptionId = ?";

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
            preparedStatement.setString(9, prescription.getPrescriptionId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating prescribed medicine: " + e.getMessage());
            throw e;
        }
    }

    public boolean deletePrescribedMedicine(String prescriptionId, String prescribedMedicineId) throws SQLException {
        String sql = "DELETE FROM prescribed_medicine WHERE prescriptionId = ? AND prescribedMedicineId = ?";

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
                    resultSet.getObject("prescriptionDate", LocalDate.class),
                    resultSet.getString("instructions"),
                    resultSet.getObject("expiryDate", LocalDate.class));

            // Set additional fields
            prescription.setStatus(Prescription.PrescriptionStatus.valueOf(resultSet.getString("status")));
            prescription.setTotalCost(resultSet.getDouble("totalCost"));

            return prescription;
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Prescription: " + e.getMessage());
            throw e;
        }
    }

    protected Prescription.PrescribedMedicine mapPrescribedMedicineResultSet(ResultSet resultSet) throws SQLException {
        Medicine medicine = medicineDao.findById(resultSet.getString("medicineId"));
        return new Prescription.PrescribedMedicine(resultSet.getString("prescribedMedicineId"),
                resultSet.getString("prescriptionId"), medicine, resultSet.getInt("quantity"),
                resultSet.getString("dosage"), resultSet.getString("frequency"), resultSet.getInt("duration"),
                resultSet.getDouble("unitPrice"));
    }
}
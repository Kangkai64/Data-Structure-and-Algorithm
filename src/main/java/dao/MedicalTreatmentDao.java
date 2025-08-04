package dao;

import entity.MedicalTreatment;
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

public class MedicalTreatmentDao extends DaoTemplate<MedicalTreatment> {

    private final PatientDao patientDao;
    private final DoctorDao doctorDao;
    private final ConsultationDao consultationDao;

    public MedicalTreatmentDao() {
        this.patientDao = new PatientDao();
        this.doctorDao = new DoctorDao();
        this.consultationDao = new ConsultationDao();
    }

    @Override
    public MedicalTreatment findById(String treatmentId) throws SQLException {
        String sql = "SELECT * FROM medical_treatment WHERE treatmentId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, treatmentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding medical treatment by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    @Override
    public ArrayBucketList<String, MedicalTreatment> findAll() throws SQLException {
        ArrayBucketList<String, MedicalTreatment> treatments = new ArrayBucketList<String, MedicalTreatment>();
        String sql = "SELECT * FROM medical_treatment ORDER BY treatmentDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                MedicalTreatment treatment = mapResultSet(resultSet);
                if (treatment != null) {
                    treatments.add(treatment.getTreatmentId(), treatment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all medical treatments: " + e.getMessage());
            throw e;
        }

        return treatments;
    }

    @Override
    public boolean insertAndReturnId(MedicalTreatment treatment) throws SQLException {
        String sql = "INSERT INTO medical_treatment (patientId, doctorId, consultationId, " +
                "diagnosis, treatmentPlan, prescribedMedications, treatmentNotes, treatmentDate, " +
                "followUpDate, status, treatmentCost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, treatment.getPatient().getPatientId());
            preparedStatement.setString(2, treatment.getDoctor().getDoctorId());
            preparedStatement.setString(3, treatment.getConsultation() != null ? 
                    treatment.getConsultation().getConsultationId() : null);
            preparedStatement.setString(4, treatment.getDiagnosis());
            preparedStatement.setString(5, treatment.getTreatmentPlan());
            preparedStatement.setString(6, treatment.getPrescribedMedications());
            preparedStatement.setString(7, treatment.getTreatmentNotes());
            preparedStatement.setTimestamp(8, new Timestamp(treatment.getTreatmentDate().getTime()));
            preparedStatement.setDate(9, treatment.getFollowUpDate() != null ? 
                    new Date(treatment.getFollowUpDate().getTime()) : null);
            preparedStatement.setString(10, treatment.getStatus().name());
            preparedStatement.setDouble(11, treatment.getTreatmentCost());

            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedTreatmentId(connection);
                if (generatedId != null) {
                    treatment.setTreatmentId(generatedId);
                    return true;
                }
            }
            
            return false;

        } catch (SQLException e) {
            System.err.println("Error inserting medical treatment: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(MedicalTreatment treatment) throws SQLException {
        String sql = "UPDATE medical_treatment SET patientId = ?, doctorId = ?, consultationId = ?, " +
                "diagnosis = ?, treatmentPlan = ?, prescribedMedications = ?, treatmentNotes = ?, " +
                "treatmentDate = ?, followUpDate = ?, status = ?, treatmentCost = ? " +
                "WHERE treatmentId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, treatment.getPatient().getPatientId());
            preparedStatement.setString(2, treatment.getDoctor().getDoctorId());
            preparedStatement.setString(3, treatment.getConsultation() != null ? 
                    treatment.getConsultation().getConsultationId() : null);
            preparedStatement.setString(4, treatment.getDiagnosis());
            preparedStatement.setString(5, treatment.getTreatmentPlan());
            preparedStatement.setString(6, treatment.getPrescribedMedications());
            preparedStatement.setString(7, treatment.getTreatmentNotes());
            preparedStatement.setTimestamp(8, new Timestamp(treatment.getTreatmentDate().getTime()));
            preparedStatement.setDate(9, treatment.getFollowUpDate() != null ? 
                    new Date(treatment.getFollowUpDate().getTime()) : null);
            preparedStatement.setString(10, treatment.getStatus().name());
            preparedStatement.setDouble(11, treatment.getTreatmentCost());
            preparedStatement.setString(12, treatment.getTreatmentId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medical treatment: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String treatmentId) throws SQLException {
        String sql = "DELETE FROM medical_treatment WHERE treatmentId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, treatmentId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting medical treatment: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateStatus(String treatmentId, MedicalTreatment.TreatmentStatus status) throws SQLException {
        String sql = "UPDATE medical_treatment SET status = ? WHERE treatmentId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            preparedStatement.setString(2, treatmentId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medical treatment status: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateFollowUpDate(String treatmentId, Date followUpDate) throws SQLException {
        String sql = "UPDATE medical_treatment SET followUpDate = ? WHERE treatmentId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setDate(1, new Date(followUpDate.getTime()));
            preparedStatement.setString(2, treatmentId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medical treatment follow-up date: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the ID of the last inserted medical treatment
     * @param connection The database connection
     * @return The generated treatment ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedTreatmentId(Connection connection) throws SQLException {
        String sql = "SELECT treatmentId FROM medical_treatment ORDER BY createdDate DESC LIMIT 1";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getString("treatmentId");
            }
        }
        
        return null;
    }

    @Override
    protected MedicalTreatment mapResultSet(ResultSet resultSet) throws SQLException {
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
                System.err.println("Patient or Doctor not found for medical treatment: " + 
                        resultSet.getString("treatmentId"));
                return null;
            }

            // Create MedicalTreatment object
            MedicalTreatment treatment = new MedicalTreatment(
                    resultSet.getString("treatmentId"),
                    patient,
                    doctor,
                    consultation,
                    resultSet.getString("diagnosis"),
                    resultSet.getString("treatmentPlan"),
                    resultSet.getString("prescribedMedications"),
                    resultSet.getString("treatmentNotes"),
                    resultSet.getDate("treatmentDate"),
                    resultSet.getDouble("treatmentCost")
            );

            // Set additional fields
            Date followUpDate = resultSet.getDate("followUpDate");
            if (followUpDate != null) {
                treatment.setFollowUpDate(followUpDate);
            }
            
            treatment.setStatus(MedicalTreatment.TreatmentStatus.valueOf(resultSet.getString("status")));

            return treatment;
        } catch (SQLException e) {
            System.err.println("Error mapping result set to MedicalTreatment: " + e.getMessage());
            throw e;
        }
    }
} 
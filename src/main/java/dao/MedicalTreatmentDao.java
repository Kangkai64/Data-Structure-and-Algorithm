package dao;

import entity.MedicalTreatment;
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
    public ArrayList<MedicalTreatment> findAll() throws SQLException {
        ArrayList<MedicalTreatment> treatments = new ArrayList<>();
        String sql = "SELECT * FROM medical_treatment ORDER BY treatmentDate DESC";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                MedicalTreatment treatment = mapResultSet(resultSet);
                if (treatment != null) {
                    treatments.add(treatment);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all medical treatments: " + e.getMessage());
            throw e;
        }

        return treatments;
    }

    @Override
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO medical_treatment (treatmentId, patientId, doctorId, consultationId, " +
                              "diagnosis, treatmentPlan, prescribedMedications, treatmentNotes, treatmentDate, " +
                              "followUpDate, status, treatmentCost) VALUES (NULL, 'P000000001', 'D000000001', NULL, " +
                              "'TEMP', 'TEMP', 'TEMP', 'TEMP', NOW(), NULL, 'ACTIVE', 0.0)";
        String tempDeleteSql = "DELETE FROM medical_treatment WHERE diagnosis = 'TEMP' AND treatmentPlan = 'TEMP'";
        return getNextIdFromDatabase("medical_treatment", "treatmentId", tempInsertSql, tempDeleteSql);
    }

    @Override
    public boolean insert(MedicalTreatment treatment) throws SQLException {
        String sql = "INSERT INTO medical_treatment (treatmentId, patientId, doctorId, consultationId, " +
                "diagnosis, treatmentPlan, prescribedMedications, treatmentNotes, treatmentDate, " +
                "followUpDate, status, treatmentCost) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, treatment.getTreatmentId());
            preparedStatement.setString(2, treatment.getPatient().getPatientId());
            preparedStatement.setString(3, treatment.getDoctor().getDoctorId());
            preparedStatement.setString(4, treatment.getConsultation() != null ? 
                    treatment.getConsultation().getConsultationId() : null);
            preparedStatement.setString(5, treatment.getDiagnosis());
            preparedStatement.setString(6, treatment.getTreatmentPlan());
            preparedStatement.setString(7, treatment.getPrescribedMedications());
            preparedStatement.setString(8, treatment.getTreatmentNotes());
            preparedStatement.setTimestamp(9, new Timestamp(treatment.getTreatmentDate().getTime()));
            preparedStatement.setDate(10, treatment.getFollowUpDate() != null ? 
                    new Date(treatment.getFollowUpDate().getTime()) : null);
            preparedStatement.setString(11, treatment.getStatus().name());
            preparedStatement.setDouble(12, treatment.getTreatmentCost());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

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
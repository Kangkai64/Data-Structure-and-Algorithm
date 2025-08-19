package dao;

import entity.Patient;
import entity.Address;
import entity.BloodType;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class PatientDao extends DaoTemplate<Patient> {
    private final AddressDao addressDao = new AddressDao();

    @Override
    public Patient findById(String patientId) throws SQLException {
        String sql = "SELECT * FROM patient WHERE patientId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, patientId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding patient by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public ArrayBucketList<String, Patient> findAll() throws SQLException {
        ArrayBucketList<String, Patient> patients = new ArrayBucketList<String, Patient>();
        String sql = "SELECT * FROM patient";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Patient patient = mapResultSet(resultSet);
                if (patient != null) {
                    patients.add(patient.getPatientId(), patient);
                }
            }   
        } catch (SQLException e) {
            System.err.println("Error finding all patients: " + e.getMessage());
            throw e;
        }

        return patients;
    }

    @Override
    public boolean insertAndReturnId(Patient patient) throws SQLException {
        String sql = "INSERT INTO patient (fullName, ICNumber, email, phoneNumber, " +
                "addressId, registrationDate, wardNumber, bloodType, allergies, emergencyContact, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, patient.getFullName());
            preparedStatement.setString(2, patient.getICNumber());
            preparedStatement.setString(3, patient.getEmail());
            preparedStatement.setString(4, patient.getPhoneNumber());
            preparedStatement.setString(5, patient.getAddress().getAddressId());
            preparedStatement.setObject(6, patient.getRegistrationDate());
            preparedStatement.setString(7, patient.getWardNumber());
            preparedStatement.setString(8, patient.getBloodType().name());
            preparedStatement.setString(9, allergiesToString(patient.getAllergies()));
            preparedStatement.setString(10, patient.getEmergencyContact());
            preparedStatement.setBoolean(11, patient.isActive());

            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedPatientId(connection);
                if (generatedId != null) {
                    patient.setPatientId(generatedId);
                    return true;
                }
            }
            
            return false;

        } catch (SQLException e) {
            System.err.println("Error inserting patient: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Patient patient) throws SQLException {
        String sql = "UPDATE patient SET fullName = ?, ICNumber = ?, email = ?, phoneNumber = ?, " +
                "addressId = ?, wardNumber = ?, bloodType = ?, allergies = ?, emergencyContact = ?, isActive = ? " +
                "WHERE patientId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, patient.getFullName());
            preparedStatement.setString(2, patient.getICNumber());
            preparedStatement.setString(3, patient.getEmail());
            preparedStatement.setString(4, patient.getPhoneNumber());
            preparedStatement.setString(5, patient.getAddress().getAddressId());
            preparedStatement.setString(6, patient.getWardNumber());
            preparedStatement.setString(7, patient.getBloodType().name());
            preparedStatement.setString(8, allergiesToString(patient.getAllergies()));
            preparedStatement.setString(9, patient.getEmergencyContact());
            preparedStatement.setBoolean(10, patient.isActive());
            preparedStatement.setString(11, patient.getPatientId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating patient: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String patientId) throws SQLException {
        String sql = "DELETE FROM patient WHERE patientId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, patientId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting patient: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Patient mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            // Create Address object
            String addressId = resultSet.getString("addressId");
            Address address = addressDao.findById(addressId);

            // Parse allergies from string to ArrayList
            String allergies = parseAllergies(resultSet.getString("allergies"));

            // Create Patient object directly (it will call Person constructor via super())
            Patient patient = new Patient(
                    resultSet.getString("fullName"),
                    resultSet.getString("ICNumber"),
                    resultSet.getString("email"),
                    resultSet.getString("phoneNumber"),
                    address,
                    resultSet.getObject("registrationDate", LocalDate.class),
                    resultSet.getString("patientId"),
                    resultSet.getString("wardNumber"),
                    BloodType.valueOf(resultSet.getString("bloodType")),
                    allergies,
                    resultSet.getString("emergencyContact")
            );

            patient.setActive(resultSet.getBoolean("isActive"));
            return patient;

        } catch (SQLException e) {
            System.err.println("Error mapping result set to Patient: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Helper method to convert ArrayList of allergies to comma-separated string
     */
    private String allergiesToString(String allergies) {
        if (allergies == null) {
            return "";
        }
        return allergies.trim();
    }

    /**
     * Helper method to parse comma-separated string to ArrayList of allergies
     */
    private String parseAllergies(String allergiesString) {
        if (allergiesString == null) {
            return "";
        }
        return allergiesString.trim();
    }

    /**
     * Get the ID of the last inserted patient
     * @param connection The database connection
     * @return The generated patient ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedPatientId(Connection connection) throws SQLException {
        String sql = "SELECT patientId FROM patient ORDER BY createdDate DESC LIMIT 1";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getString("patientId");
            }
        }
        
        return null;
    }
}
package dao;

import entity.Patient;
import entity.Address;
import entity.BloodType;
import utility.HikariConnectionPool;
import adt.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;

public class PatientDao extends DaoTemplate<Patient> {

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

    public ArrayList<Patient> findAll() throws SQLException {
        ArrayList<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patient";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Patient patient = mapResultSet(resultSet);
                if (patient != null) {
                    patients.add(patient);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all patients: " + e.getMessage());
            throw e;
        }

        return patients;
    }

    @Override
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO patient (patientId, fullName, ICNumber, email, phoneNumber, " +
                              "addressId, registrationDate, wardNumber, bloodType, allergies, emergencyContact, isActive) " +
                              "VALUES (NULL, 'TEMP', '000000-00-0000', 'temp@temp.com', '000-0000000', " +
                              "'A000000001', CURDATE(), 'TEMP', 'A_POSITIVE', '', '', false)";
        String tempDeleteSql = "DELETE FROM patient WHERE fullName = 'TEMP' AND ICNumber = '000000-00-0000'";
        return getNextIdFromDatabase("patient", "patientId", tempInsertSql, tempDeleteSql);
    }

    @Override
    public boolean insert(Patient patient) throws SQLException {
        String sql = "INSERT INTO patient (patientId, fullName, ICNumber, email, phoneNumber, " +
                "addressId, registrationDate, wardNumber, bloodType, allergies, emergencyContact, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, patient.getPatientId());
            preparedStatement.setString(2, patient.getFullName());
            preparedStatement.setString(3, patient.getICNumber());
            preparedStatement.setString(4, patient.getEmail());
            preparedStatement.setString(5, patient.getPhoneNumber());
            preparedStatement.setString(6, patient.getAddress().getAddressId());
            preparedStatement.setDate(7, new Date(patient.getRegistrationDate().getTime()));
            preparedStatement.setString(8, patient.getWardNumber());
            preparedStatement.setString(9, patient.getBloodType().name());
            preparedStatement.setString(10, allergiesToString(patient.getAllergies()));
            preparedStatement.setString(11, patient.getEmergencyContact());
            preparedStatement.setBoolean(12, patient.isActive());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

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
            AddressDao addressDao = new AddressDao();
            Address address = addressDao.findById(addressId);

            // Parse allergies from string to ArrayList
            ArrayList<String> allergies = parseAllergies(resultSet.getString("allergies"));

            // Create Patient object directly (it will call Person constructor via super())
            Patient patient = new Patient(
                    resultSet.getString("fullName"),
                    resultSet.getString("ICNumber"),
                    resultSet.getString("email"),
                    resultSet.getString("phoneNumber"),
                    address,
                    resultSet.getDate("registrationDate"),
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
    private String allergiesToString(ArrayList<String> allergies) {
        if (allergies == null || allergies.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allergies.getNumberOfEntries(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(allergies.getEntry(i + 1)); // ArrayList is 1-indexed
        }
        return sb.toString();
    }

    /**
     * Helper method to parse comma-separated string to ArrayList of allergies
     */
    private ArrayList<String> parseAllergies(String allergiesString) {
        ArrayList<String> allergies = new ArrayList<>();
        if (allergiesString != null && !allergiesString.trim().isEmpty()) {
            String[] allergyArray = allergiesString.split(",");
            for (String allergy : allergyArray) {
                allergies.add(allergy.trim());
            }
        }
        return allergies;
    }
}
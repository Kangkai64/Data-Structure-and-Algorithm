package dao;

import entity.Doctor;
import entity.Address;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

public class DoctorDao extends DaoTemplate<Doctor> {

    @Override
    public Doctor findById(String doctorId) throws SQLException {
        String sql = "SELECT d.*, a.street, a.city, a.state, a.postalCode, a.country " +
                "FROM doctor d " +
                "LEFT JOIN address a ON d.addressId = a.addressId " +
                "WHERE d.doctorId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding doctor by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    @Override
    public ArrayBucketList<String, Doctor> findAll() throws SQLException {
        ArrayBucketList<String, Doctor> doctors = new ArrayBucketList<String, Doctor>();
        String sql = "SELECT d.*, a.street, a.city, a.state, a.postalCode, a.country " +
                "FROM doctor d " +
                "LEFT JOIN address a ON d.addressId = a.addressId " +
                "ORDER BY d.fullName";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Doctor doctor = mapResultSet(resultSet);
                if (doctor != null) {
                    doctors.add(doctor.getDoctorId(), doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all doctors: " + e.getMessage());
            throw e;
        }

        return doctors;
    }

    @Override
    public boolean insertAndReturnId(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctor (fullName, ICNumber, email, phoneNumber, " +
                "addressId, registrationDate, medicalSpecialty, licenseNumber, expYears, isAvailable) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, doctor.getFullName());
            preparedStatement.setString(2, doctor.getICNumber());
            preparedStatement.setString(3, doctor.getEmail());
            preparedStatement.setString(4, doctor.getPhoneNumber());
            preparedStatement.setString(5, doctor.getAddress() != null ? doctor.getAddress().getAddressId() : null);
            preparedStatement.setObject(6, doctor.getRegistrationDate());
            preparedStatement.setString(7, doctor.getMedicalSpecialty());
            preparedStatement.setString(8, doctor.getLicenseNumber());
            preparedStatement.setInt(9, doctor.getExpYears());
            preparedStatement.setBoolean(10, doctor.isAvailable());

            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedDoctorId(connection);
                if (generatedId != null) {
                    doctor.setDoctorId(generatedId);
                    return true;
                }
            }
            
            return false;

        } catch (SQLException e) {
            System.err.println("Error inserting doctor: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctor SET fullName = ?, ICNumber = ?, email = ?, phoneNumber = ?, " +
                "addressId = ?, medicalSpecialty = ?, licenseNumber = ?, expYears = ?, isAvailable = ? " +
                "WHERE doctorId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctor.getFullName());
            preparedStatement.setString(2, doctor.getICNumber());
            preparedStatement.setString(3, doctor.getEmail());
            preparedStatement.setString(4, doctor.getPhoneNumber());
            preparedStatement.setString(5, doctor.getAddress() != null ? doctor.getAddress().getAddressId() : null);
            preparedStatement.setString(6, doctor.getMedicalSpecialty());
            preparedStatement.setString(7, doctor.getLicenseNumber());
            preparedStatement.setInt(8, doctor.getExpYears());
            preparedStatement.setBoolean(9, doctor.isAvailable());
            preparedStatement.setString(10, doctor.getDoctorId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating doctor: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String doctorId) throws SQLException {
        String sql = "DELETE FROM doctor WHERE doctorId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting doctor: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateAvailability(String doctorId, boolean isAvailable) throws SQLException {
        String sql = "UPDATE doctor SET isAvailable = ? WHERE doctorId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBoolean(1, isAvailable);
            preparedStatement.setString(2, doctorId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating doctor availability: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Doctor mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            // Create Address object if addressId is not null
            Address address = null;
            if (resultSet.getString("addressId") != null) {
                address = new Address(
                        resultSet.getString("street"),
                        resultSet.getString("city"),
                        resultSet.getString("state"),
                        resultSet.getString("postalCode"),
                        resultSet.getString("country")
                );
                address.setAddressId(resultSet.getString("addressId"));
            }

            // Create Doctor object
            Doctor doctor = new Doctor(
                    resultSet.getString("fullName"),
                    resultSet.getString("ICNumber"),
                    resultSet.getString("email"),
                    resultSet.getString("phoneNumber"),
                    address,
                    resultSet.getObject("registrationDate", LocalDate.class),
                    resultSet.getString("doctorId"),
                    resultSet.getString("medicalSpecialty"),
                    resultSet.getString("licenseNumber"),
                    resultSet.getInt("expYears")
            );

            // Set availability
            doctor.setAvailable(resultSet.getBoolean("isAvailable"));

            return doctor;
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Doctor: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the ID of the last inserted doctor
     * @param connection The database connection
     * @return The generated doctor ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedDoctorId(Connection connection) throws SQLException {
        String sql = "SELECT doctorId FROM doctor ORDER BY createdDate DESC LIMIT 1";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getString("doctorId");
            }
        }
        
        return null;
    }
}

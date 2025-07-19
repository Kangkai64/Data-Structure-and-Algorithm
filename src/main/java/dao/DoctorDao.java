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
import java.sql.Date;

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
    public ArrayBucketList<Doctor> findAll() throws SQLException {
        ArrayBucketList<Doctor> doctors = new ArrayBucketList<>();
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
                    doctors.add(doctor.hashCode(), doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all doctors: " + e.getMessage());
            throw e;
        }

        return doctors;
    }

    @Override
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO doctor (doctorId, fullName, ICNumber, email, phoneNumber, " +
                              "addressId, registrationDate, medicalSpecialty, licenseNumber, expYears, isAvailable) " +
                              "VALUES (NULL, 'TEMP', '000000-00-0000', 'temp@temp.com', '000-0000000', " +
                              "'A000000001', CURDATE(), 'TEMP', 'TEMP000', 0, false)";
        String tempDeleteSql = "DELETE FROM doctor WHERE fullName = 'TEMP' AND ICNumber = '000000-00-0000'";
        return getNextIdFromDatabase("doctor", "doctorId", tempInsertSql, tempDeleteSql);
    }

    @Override
    public boolean insert(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctor (doctorId, fullName, ICNumber, email, phoneNumber, " +
                "addressId, registrationDate, medicalSpecialty, licenseNumber, expYears, isAvailable) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctor.getDoctorId());
            preparedStatement.setString(2, doctor.getFullName());
            preparedStatement.setString(3, doctor.getICNumber());
            preparedStatement.setString(4, doctor.getEmail());
            preparedStatement.setString(5, doctor.getPhoneNumber());
            preparedStatement.setString(6, doctor.getAddress() != null ? doctor.getAddress().getAddressId() : null);
            preparedStatement.setDate(7, new Date(doctor.getRegistrationDate().getTime()));
            preparedStatement.setString(8, doctor.getMedicalSpecialty());
            preparedStatement.setString(9, doctor.getLicenseNumber());
            preparedStatement.setInt(10, doctor.getExpYears());
            preparedStatement.setBoolean(11, doctor.isAvailable());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

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
            }

            // Create Doctor object
            Doctor doctor = new Doctor(
                    resultSet.getString("fullName"),
                    resultSet.getString("ICNumber"),
                    resultSet.getString("email"),
                    resultSet.getString("phoneNumber"),
                    address,
                    resultSet.getDate("registrationDate"),
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
}

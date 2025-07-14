package dao;

import entity.Doctor;
import entity.Address;
import utility.HikariConnectionPool;
import adt.ArrayList;

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

    public Doctor findByLicenseNumber(String licenseNumber) throws SQLException {
        String sql = "SELECT d.*, a.street, a.city, a.state, a.postalCode, a.country " +
                "FROM doctor d " +
                "LEFT JOIN address a ON d.addressId = a.addressId " +
                "WHERE d.licenseNumber = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, licenseNumber);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding doctor by license number: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public Doctor findByEmail(String email) throws SQLException {
        String sql = "SELECT d.*, a.street, a.city, a.state, a.postalCode, a.country " +
                "FROM doctor d " +
                "LEFT JOIN address a ON d.addressId = a.addressId " +
                "WHERE d.email = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, email);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding doctor by email: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public ArrayList<Doctor> findBySpecialty(String medicalSpecialty) throws SQLException {
        ArrayList<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT d.*, a.street, a.city, a.state, a.postalCode, a.country " +
                "FROM doctor d " +
                "LEFT JOIN address a ON d.addressId = a.addressId " +
                "WHERE d.medicalSpecialty = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, medicalSpecialty);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Doctor doctor = mapResultSet(resultSet);
                if (doctor != null) {
                    doctors.add(doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding doctors by specialty: " + e.getMessage());
            throw e;
        }

        return doctors;
    }

    public ArrayList<Doctor> findAvailableDoctors() throws SQLException {
        ArrayList<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT d.*, a.street, a.city, a.state, a.postalCode, a.country " +
                "FROM doctor d " +
                "LEFT JOIN address a ON d.addressId = a.addressId " +
                "WHERE d.isAvailable = true " +
                "ORDER BY d.fullName";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Doctor doctor = mapResultSet(resultSet);
                if (doctor != null) {
                    doctors.add(doctor);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding available doctors: " + e.getMessage());
            throw e;
        }

        return doctors;
    }

    public ArrayList<Doctor> findAll() throws SQLException {
        ArrayList<Doctor> doctors = new ArrayList<>();
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
                    doctors.add(doctor);
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

    public ArrayList<String> getAllSpecialties() throws SQLException {
        ArrayList<String> specialties = new ArrayList<>();
        String sql = "SELECT DISTINCT medicalSpecialty FROM doctor ORDER BY medicalSpecialty";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                specialties.add(resultSet.getString("medicalSpecialty"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all specialties: " + e.getMessage());
            throw e;
        }

        return specialties;
    }

    public int getDoctorCountBySpecialty(String medicalSpecialty) throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctor WHERE medicalSpecialty = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, medicalSpecialty);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting doctor count by specialty: " + e.getMessage());
            throw e;
        }

        return 0;
    }

    public boolean isDoctorInUse(String doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM consultation WHERE doctorId = ? UNION ALL " +
                    "SELECT COUNT(*) FROM medical_treatment WHERE doctorId = ? UNION ALL " +
                    "SELECT COUNT(*) FROM prescription WHERE doctorId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            preparedStatement.setString(2, doctorId);
            preparedStatement.setString(3, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            int totalCount = 0;
            while (resultSet.next()) {
                totalCount += resultSet.getInt(1);
            }

            return totalCount > 0;
        } catch (SQLException e) {
            System.err.println("Error checking if doctor is in use: " + e.getMessage());
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

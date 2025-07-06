package dao;

import entity.Doctor;
import utility.HikariConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoctorDao extends DaoTemplate<Doctor> {

    @Override
    public Doctor findById(String doctorId) throws SQLException {
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

    @Override
    public boolean insert(Doctor object) throws SQLException {
        return false;
    }

    @Override
    public boolean update(Doctor object) throws SQLException {
        return false;
    }

    @Override
    public boolean delete(String id) throws SQLException {
        return false;
    }

    @Override
    protected Doctor mapResultSet(ResultSet resultSet) throws SQLException {
        return null;
    }
}

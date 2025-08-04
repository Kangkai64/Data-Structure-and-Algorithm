package dao;

import entity.Schedule;
import entity.DayOfWeek;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

public class ScheduleDao extends DaoTemplate<Schedule> {

    @Override
    public Schedule findById(String scheduleId) throws SQLException {
        String sql = "SELECT * FROM schedule WHERE scheduleId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, scheduleId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding schedule by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    @Override
    public ArrayBucketList<String, Schedule> findAll() throws SQLException {
        ArrayBucketList<String, Schedule> schedules = new ArrayBucketList<String, Schedule>();
        String sql = "SELECT * FROM schedule ORDER BY doctorId, dayOfWeek, fromTime";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Schedule schedule = mapResultSet(resultSet);
                if (schedule != null) {
                    schedules.add(schedule.getScheduleId(), schedule);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all schedules: " + e.getMessage());
            throw e;
        }

        return schedules;
    }

    @Override
    public boolean insertAndReturnId(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedule (doctorId, dayOfWeek, fromTime, toTime, isAvailable) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, schedule.getDoctorId());
            preparedStatement.setString(2, schedule.getDayOfWeek().name());
            preparedStatement.setTime(3, Time.valueOf(schedule.getFromTime()));
            preparedStatement.setTime(4, Time.valueOf(schedule.getToTime()));
            preparedStatement.setBoolean(5, schedule.isAvailable());

            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedScheduleId(connection);
                if (generatedId != null) {
                    schedule.setScheduleId(generatedId);
                    return true;
                }
            }
            
            return false;

        } catch (SQLException e) {
            System.err.println("Error inserting schedule: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Schedule schedule) throws SQLException {
        String sql = "UPDATE schedule SET doctorId = ?, dayOfWeek = ?, fromTime = ?, " +
                "toTime = ?, isAvailable = ? WHERE scheduleId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, schedule.getDoctorId());
            preparedStatement.setString(2, schedule.getDayOfWeek().name());
            preparedStatement.setTime(3, Time.valueOf(schedule.getFromTime()));
            preparedStatement.setTime(4, Time.valueOf(schedule.getToTime()));
            preparedStatement.setBoolean(5, schedule.isAvailable());
            preparedStatement.setString(6, schedule.getScheduleId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating schedule: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String scheduleId) throws SQLException {
        String sql = "DELETE FROM schedule WHERE scheduleId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, scheduleId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting schedule: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateAvailability(String scheduleId, boolean isAvailable) throws SQLException {
        String sql = "UPDATE schedule SET isAvailable = ? WHERE scheduleId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setBoolean(1, isAvailable);
            preparedStatement.setString(2, scheduleId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating schedule availability: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Schedule mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            return new Schedule(
                    resultSet.getString("scheduleId"),
                    resultSet.getString("doctorId"),
                    DayOfWeek.valueOf(resultSet.getString("dayOfWeek")),
                    resultSet.getTime("fromTime").toString(),
                    resultSet.getTime("toTime").toString(),
                    resultSet.getBoolean("isAvailable")
            );
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Schedule: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get the ID of the last inserted schedule
     * @param connection The database connection
     * @return The generated schedule ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedScheduleId(Connection connection) throws SQLException {
        String sql = "SELECT scheduleId FROM schedule ORDER BY createdDate DESC LIMIT 1";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getString("scheduleId");
            }
        }
        
        return null;
    }
} 
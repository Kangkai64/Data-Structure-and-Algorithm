package dao;

import entity.Schedule;
import entity.DayOfWeek;
import utility.HikariConnectionPool;
import adt.ArrayList;

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

    public ArrayList<Schedule> findByDoctorId(String doctorId) throws SQLException {
        ArrayList<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedule WHERE doctorId = ? ORDER BY dayOfWeek, fromTime";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Schedule schedule = mapResultSet(resultSet);
                if (schedule != null) {
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding schedules by doctor ID: " + e.getMessage());
            throw e;
        }

        return schedules;
    }

    public ArrayList<Schedule> findByDayOfWeek(DayOfWeek dayOfWeek) throws SQLException {
        ArrayList<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedule WHERE dayOfWeek = ? AND isAvailable = true ORDER BY fromTime";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, dayOfWeek.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Schedule schedule = mapResultSet(resultSet);
                if (schedule != null) {
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding schedules by day of week: " + e.getMessage());
            throw e;
        }

        return schedules;
    }

    public ArrayList<Schedule> findAvailableSchedules() throws SQLException {
        ArrayList<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedule WHERE isAvailable = true ORDER BY dayOfWeek, fromTime";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Schedule schedule = mapResultSet(resultSet);
                if (schedule != null) {
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding available schedules: " + e.getMessage());
            throw e;
        }

        return schedules;
    }

    public ArrayList<Schedule> findSchedulesByTimeRange(String doctorId, DayOfWeek dayOfWeek, 
                                                      String fromTime, String toTime) throws SQLException {
        ArrayList<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedule WHERE doctorId = ? AND dayOfWeek = ? " +
                "AND ((fromTime BETWEEN ? AND ?) OR (toTime BETWEEN ? AND ?) " +
                "OR (fromTime <= ? AND toTime >= ?))";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            preparedStatement.setString(2, dayOfWeek.name());
            preparedStatement.setString(3, fromTime);
            preparedStatement.setString(4, toTime);
            preparedStatement.setString(5, fromTime);
            preparedStatement.setString(6, toTime);
            preparedStatement.setString(7, fromTime);
            preparedStatement.setString(8, toTime);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Schedule schedule = mapResultSet(resultSet);
                if (schedule != null) {
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding schedules by time range: " + e.getMessage());
            throw e;
        }

        return schedules;
    }

    public ArrayList<Schedule> findAll() throws SQLException {
        ArrayList<Schedule> schedules = new ArrayList<>();
        String sql = "SELECT * FROM schedule ORDER BY doctorId, dayOfWeek, fromTime";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Schedule schedule = mapResultSet(resultSet);
                if (schedule != null) {
                    schedules.add(schedule);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all schedules: " + e.getMessage());
            throw e;
        }

        return schedules;
    }

    @Override
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO schedule (scheduleId, doctorId, dayOfWeek, fromTime, toTime, isAvailable) " +
                              "VALUES (NULL, 'D000000001', 'MONDAY', '09:00:00', '10:00:00', false)";
        String tempDeleteSql = "DELETE FROM schedule WHERE doctorId = 'D000000001' AND dayOfWeek = 'MONDAY' AND fromTime = '09:00:00'";
        return getNextIdFromDatabase("schedule", "scheduleId", tempInsertSql, tempDeleteSql);
    }

    @Override
    public boolean insert(Schedule schedule) throws SQLException {
        String sql = "INSERT INTO schedule (scheduleId, doctorId, dayOfWeek, fromTime, toTime, isAvailable) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, schedule.getScheduleId());
            preparedStatement.setString(2, schedule.getDoctorId());
            preparedStatement.setString(3, schedule.getDayOfWeek().name());
            preparedStatement.setTime(4, Time.valueOf(schedule.getFromTime()));
            preparedStatement.setTime(5, Time.valueOf(schedule.getToTime()));
            preparedStatement.setBoolean(6, schedule.isAvailable());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

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

    public boolean deleteByDoctorId(String doctorId) throws SQLException {
        String sql = "DELETE FROM schedule WHERE doctorId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting schedules by doctor ID: " + e.getMessage());
            throw e;
        }
    }

    public int getScheduleCountByDoctor(String doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schedule WHERE doctorId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting schedule count by doctor: " + e.getMessage());
            throw e;
        }

        return 0;
    }

    public ArrayList<String> getAvailableTimeSlots(String doctorId, DayOfWeek dayOfWeek) throws SQLException {
        ArrayList<String> timeSlots = new ArrayList<>();
        String sql = "SELECT fromTime, toTime FROM schedule " +
                "WHERE doctorId = ? AND dayOfWeek = ? AND isAvailable = true " +
                "ORDER BY fromTime";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            preparedStatement.setString(2, dayOfWeek.name());
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                String fromTime = resultSet.getTime("fromTime").toString();
                String toTime = resultSet.getTime("toTime").toString();
                timeSlots.add(fromTime + " - " + toTime);
            }
        } catch (SQLException e) {
            System.err.println("Error getting available time slots: " + e.getMessage());
            throw e;
        }

        return timeSlots;
    }

    public boolean isTimeSlotAvailable(String doctorId, DayOfWeek dayOfWeek, 
                                     String fromTime, String toTime) throws SQLException {
        String sql = "SELECT COUNT(*) FROM schedule WHERE doctorId = ? AND dayOfWeek = ? " +
                "AND isAvailable = true AND ((fromTime BETWEEN ? AND ?) OR (toTime BETWEEN ? AND ?) " +
                "OR (fromTime <= ? AND toTime >= ?))";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, doctorId);
            preparedStatement.setString(2, dayOfWeek.name());
            preparedStatement.setString(3, fromTime);
            preparedStatement.setString(4, toTime);
            preparedStatement.setString(5, fromTime);
            preparedStatement.setString(6, toTime);
            preparedStatement.setString(7, fromTime);
            preparedStatement.setString(8, toTime);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getInt(1) == 0; // Available if no conflicts found
            }
        } catch (SQLException e) {
            System.err.println("Error checking time slot availability: " + e.getMessage());
            throw e;
        }

        return false;
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
} 
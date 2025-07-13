package dao;

import entity.Address;
import utility.HikariConnectionPool;
import adt.ArrayList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class AddressDao extends DaoTemplate<Address> {

    @Override
    public Address findById(String addressId) throws SQLException {
        String sql = "SELECT * FROM address WHERE addressId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, addressId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding address by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public Address findByStreetAndCity(String street, String city) throws SQLException {
        String sql = "SELECT * FROM address WHERE street = ? AND city = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, street);
            preparedStatement.setString(2, city);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding address by street and city: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public ArrayList<Address> findByCity(String city) throws SQLException {
        ArrayList<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE city = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, city);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Address address = mapResultSet(resultSet);
                if (address != null) {
                    addresses.add(address);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding addresses by city: " + e.getMessage());
            throw e;
        }

        return addresses;
    }

    public ArrayList<Address> findByState(String state) throws SQLException {
        ArrayList<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address WHERE state = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, state);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Address address = mapResultSet(resultSet);
                if (address != null) {
                    addresses.add(address);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding addresses by state: " + e.getMessage());
            throw e;
        }

        return addresses;
    }

    public ArrayList<Address> findAll() throws SQLException {
        ArrayList<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM address ORDER BY city, street";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Address address = mapResultSet(resultSet);
                if (address != null) {
                    addresses.add(address);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all addresses: " + e.getMessage());
            throw e;
        }

        return addresses;
    }

    @Override
    public boolean insert(Address address) throws SQLException {
        String sql = "INSERT INTO address (addressId, street, city, state, postalCode, country) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, address.getAddressId());
            preparedStatement.setString(2, address.getStreet());
            preparedStatement.setString(3, address.getCity());
            preparedStatement.setString(4, address.getState());
            preparedStatement.setString(5, address.getZipCode());
            preparedStatement.setString(6, address.getCountry());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting address: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Address address) throws SQLException {
        String sql = "UPDATE address SET street = ?, city = ?, state = ?, postalCode = ?, country = ? " +
                "WHERE addressId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, address.getStreet());
            preparedStatement.setString(2, address.getCity());
            preparedStatement.setString(3, address.getState());
            preparedStatement.setString(4, address.getZipCode());
            preparedStatement.setString(5, address.getCountry());
            preparedStatement.setString(6, address.getAddressId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating address: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String addressId) throws SQLException {
        String sql = "DELETE FROM address WHERE addressId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, addressId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting address: " + e.getMessage());
            throw e;
        }
    }

    public boolean isAddressInUse(String addressId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patient WHERE addressId = ? UNION ALL " +
                    "SELECT COUNT(*) FROM doctor WHERE addressId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, addressId);
            preparedStatement.setString(2, addressId);
            ResultSet resultSet = preparedStatement.executeQuery();

            int totalCount = 0;
            while (resultSet.next()) {
                totalCount += resultSet.getInt(1);
            }

            return totalCount > 0;
        } catch (SQLException e) {
            System.err.println("Error checking if address is in use: " + e.getMessage());
            throw e;
        }
    }

    public ArrayList<String> getAllCities() throws SQLException {
        ArrayList<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM address ORDER BY city";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                cities.add(resultSet.getString("city"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all cities: " + e.getMessage());
            throw e;
        }

        return cities;
    }

    public ArrayList<String> getAllStates() throws SQLException {
        ArrayList<String> states = new ArrayList<>();
        String sql = "SELECT DISTINCT state FROM address ORDER BY state";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                states.add(resultSet.getString("state"));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all states: " + e.getMessage());
            throw e;
        }

        return states;
    }

    @Override
    protected Address mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            return new Address(
                    resultSet.getString("addressId"),
                    null, // userId is not used in the new schema
                    resultSet.getString("street"),
                    resultSet.getString("city"),
                    resultSet.getString("state"),
                    resultSet.getString("postalCode"),
                    resultSet.getString("country")
            );
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Address: " + e.getMessage());
            throw e;
        }
    }
}

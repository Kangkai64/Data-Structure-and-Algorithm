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

    @Override
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
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO address (addressId, street, city, state, postalCode, country) " +
                              "VALUES (NULL, 'TEMP', 'TEMP', 'TEMP', '00000', 'TEMP')";
        String tempDeleteSql = "DELETE FROM address WHERE street = 'TEMP' AND city = 'TEMP'";
        return getNextIdFromDatabase("address", "addressId", tempInsertSql, tempDeleteSql);
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

    @Override
    protected Address mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            return new Address(
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

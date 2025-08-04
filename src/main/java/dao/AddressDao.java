package dao;

import entity.Address;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

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
    public ArrayBucketList<String, Address> findAll() throws SQLException {
        ArrayBucketList<String, Address> addresses = new ArrayBucketList<String, Address>();
        String sql = "SELECT * FROM address ORDER BY city, street";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Address address = mapResultSet(resultSet);
                if (address != null) {
                    addresses.add(address.getAddressId(), address);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all addresses: " + e.getMessage());
            throw e;
        }

        return addresses;
    }

    @Override
    public boolean insertAndReturnId(Address address) throws SQLException {
        String sql = "INSERT INTO address (street, city, state, postalCode, country) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setString(1, address.getStreet());
            preparedStatement.setString(2, address.getCity());
            preparedStatement.setString(3, address.getState());
            preparedStatement.setString(4, address.getZipCode());
            preparedStatement.setString(5, address.getCountry());

            int affectedRows = preparedStatement.executeUpdate();
            
            if (affectedRows > 0) {
                // Get the generated ID from the database
                String generatedId = getLastInsertedAddressId(connection);
                if (generatedId != null) {
                    address.setAddressId(generatedId);
                    return true;
                }
            }
            
            return false;

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

    /**
     * Get the ID of the last inserted address
     * @param connection The database connection
     * @return The generated address ID
     * @throws SQLException if database error occurs
     */
    private String getLastInsertedAddressId(Connection connection) throws SQLException {
        String sql = "SELECT addressId FROM address ORDER BY createdDate DESC LIMIT 1";
        
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getString("addressId");
            }
        }
        
        return null;
    }

    @Override
    protected Address mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            Address address = new Address(
                    resultSet.getString("street"),
                    resultSet.getString("city"),
                    resultSet.getString("state"),
                    resultSet.getString("postalCode"),
                    resultSet.getString("country")
            );
            address.setAddressId(resultSet.getString("addressId"));
            return address;
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Address: " + e.getMessage());
            throw e;
        }
    }
}

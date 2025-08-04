package control;

import dao.AddressDao;
import entity.Address;
import java.sql.SQLException;

public class AddressManagementControl {

    private static final AddressDao ADDRESS_DAO = new AddressDao();

    /**
     * Retrieves an address by its ID
     * @param addressId The ID of the address to retrieve
     * @return The address if found, null otherwise
     * @throws IllegalArgumentException if addressId is null or empty
     * @throws SQLException if there is a database error
     */
    public Address getAddressById(String addressId) throws SQLException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty");
        }

        try {
            return ADDRESS_DAO.findById(addressId.trim());
        } catch (SQLException e) {
            throw new SQLException("Failed to retrieve address: " + e.getMessage(), e);
        }
    }

    /**
     * Adds a new address
     * @param address The address to add
     * @return true if the address was added successfully, false otherwise
     * @throws IllegalArgumentException if address is null or invalid
     * @throws SQLException if there is a database error
     */
    public boolean addAddress(Address address) throws SQLException {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        try {
            return ADDRESS_DAO.insertAndReturnId(address);
        } catch (SQLException e) {
            throw new SQLException("Failed to add address: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing address
     * @param address The address to update
     * @return true if the address was updated successfully, false otherwise
     * @throws IllegalArgumentException if address is null or invalid
     * @throws SQLException if there is a database error
     */
    public boolean updateAddress(Address address) throws SQLException {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        if (address.getAddressId() == null || address.getAddressId().trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty");
        }

        try {
            return ADDRESS_DAO.update(address);
        } catch (SQLException e) {
            throw new SQLException("Failed to update address: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an address
     * @param addressId The ID of the address to delete
     * @return true if the address was deleted successfully, false otherwise
     * @throws IllegalArgumentException if addressId is null or empty
     * @throws SQLException if there is a database error
     */
    public boolean deleteAddress(String addressId) throws SQLException {
        if (addressId == null || addressId.trim().isEmpty()) {
            throw new IllegalArgumentException("Address ID cannot be null or empty");
        }

        try {
            return ADDRESS_DAO.delete(addressId.trim());
        } catch (SQLException e) {
            throw new SQLException("Failed to delete address: " + e.getMessage(), e);
        }
    }
}

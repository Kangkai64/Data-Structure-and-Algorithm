package dao;

import entity.Medicine;
import utility.HikariConnectionPool;
import adt.ArrayBucketList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author: Ho Kang Kai
 * Medicine DAO - Module 5
 * Manages medicine data access operations
 */

public class MedicineDao extends DaoTemplate<Medicine> {

    @Override
    public Medicine findById(String medicineId) throws SQLException {
        String sql = "SELECT * FROM medicine WHERE medicineId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, medicineId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                return mapResultSet(resultSet);
            }
        } catch (SQLException e) {
            System.err.println("Error finding medicine by ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    @Override
    public ArrayBucketList<String, Medicine> findAll() throws SQLException {
        ArrayBucketList<String, Medicine> medicines = new ArrayBucketList<String, Medicine>();
        String sql = "SELECT * FROM medicine ORDER BY medicineName";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                Medicine medicine = mapResultSet(resultSet);
                if (medicine != null) {
                    medicines.add(medicine.getMedicineId(), medicine);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all medicines: " + e.getMessage());
            throw e;
        }

        return medicines;
    }

    @Override
    public String getNewId() throws SQLException {
        String tempInsertSql = "INSERT INTO medicine (medicineId, medicineName, genericName, manufacturer, " +
                              "description, dosageForm, strength, quantityInStock, minimumStockLevel, unitPrice, " +
                              "expiryDate, storageLocation, requiresPrescription, status) " +
                              "VALUES (NULL, 'TEMP', 'TEMP', 'TEMP', 'TEMP', 'TEMP', 'TEMP', 0, 0, 0.0, " +
                              "DATE_ADD(CURDATE(), INTERVAL 1 YEAR), 'TEMP', false, 'AVAILABLE')";
        String tempDeleteSql = "DELETE FROM medicine WHERE medicineName = 'TEMP' AND genericName = 'TEMP'";
        return getNextIdFromDatabase("medicine", "medicineId", tempInsertSql, tempDeleteSql);
    }

    @Override
    public boolean insert(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicine (medicineId, medicineName, genericName, manufacturer, " +
                "description, dosageForm, strength, quantityInStock, minimumStockLevel, unitPrice, " +
                "expiryDate, storageLocation, requiresPrescription, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, medicine.getMedicineId());
            preparedStatement.setString(2, medicine.getMedicineName());
            preparedStatement.setString(3, medicine.getGenericName());
            preparedStatement.setString(4, medicine.getManufacturer());
            preparedStatement.setString(5, medicine.getDescription());
            preparedStatement.setString(6, medicine.getDosageForm());
            preparedStatement.setString(7, medicine.getStrength());
            preparedStatement.setInt(8, medicine.getQuantityInStock());
            preparedStatement.setInt(9, medicine.getMinimumStockLevel());
            preparedStatement.setDouble(10, medicine.getUnitPrice());
            preparedStatement.setDate(11, new java.sql.Date(medicine.getExpiryDate().getTime()));
            preparedStatement.setString(12, medicine.getStorageLocation());
            preparedStatement.setBoolean(13, medicine.getRequiresPrescription());
            preparedStatement.setString(14, medicine.getStatus().name());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error inserting medicine: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean update(Medicine medicine) throws SQLException {
        String sql = "UPDATE medicine SET medicineName = ?, genericName = ?, manufacturer = ?, " +
                "description = ?, dosageForm = ?, strength = ?, quantityInStock = ?, minimumStockLevel = ?, " +
                "unitPrice = ?, expiryDate = ?, storageLocation = ?, requiresPrescription = ?, status = ? " +
                "WHERE medicineId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, medicine.getMedicineName());
            preparedStatement.setString(2, medicine.getGenericName());
            preparedStatement.setString(3, medicine.getManufacturer());
            preparedStatement.setString(4, medicine.getDescription());
            preparedStatement.setString(5, medicine.getDosageForm());
            preparedStatement.setString(6, medicine.getStrength());
            preparedStatement.setInt(7, medicine.getQuantityInStock());
            preparedStatement.setInt(8, medicine.getMinimumStockLevel());
            preparedStatement.setDouble(9, medicine.getUnitPrice());
            preparedStatement.setDate(10, new java.sql.Date(medicine.getExpiryDate().getTime()));
            preparedStatement.setString(11, medicine.getStorageLocation());
            preparedStatement.setBoolean(12, medicine.getRequiresPrescription());
            preparedStatement.setString(13, medicine.getStatus().name());
            preparedStatement.setString(14, medicine.getMedicineId());

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medicine: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean delete(String medicineId) throws SQLException {
        String sql = "DELETE FROM medicine WHERE medicineId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, medicineId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting medicine: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateStock(String medicineId, int newQuantity) throws SQLException {
        String sql = "UPDATE medicine SET quantityInStock = ? WHERE medicineId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, newQuantity);
            preparedStatement.setString(2, medicineId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medicine stock: " + e.getMessage());
            throw e;
        }
    }

    public boolean updateStatus(String medicineId, Medicine.MedicineStatus status) throws SQLException {
        String sql = "UPDATE medicine SET status = ? WHERE medicineId = ?";

        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, status.name());
            preparedStatement.setString(2, medicineId);

            int affectedRows = preparedStatement.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medicine status: " + e.getMessage());
            throw e;
        }
    }

    public boolean updatePrice(String medicineId, double newPrice) throws SQLException {
        String sql = "UPDATE medicine SET unitPrice = ? WHERE medicineId = ?";
        try (Connection connection = HikariConnectionPool.getInstance().getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setDouble(1, newPrice);
            preparedStatement.setString(2, medicineId);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating medicine price: " + e.getMessage());
            throw e;
        }
    }

    @Override
    protected Medicine mapResultSet(ResultSet resultSet) throws SQLException {
        try {
            return new Medicine(
                    resultSet.getString("medicineId"),
                    resultSet.getString("medicineName"),
                    resultSet.getString("genericName"),
                    resultSet.getString("manufacturer"),
                    resultSet.getString("description"),
                    resultSet.getString("dosageForm"),
                    resultSet.getString("strength"),
                    resultSet.getInt("quantityInStock"),
                    resultSet.getInt("minimumStockLevel"),
                    resultSet.getDouble("unitPrice"),
                    resultSet.getDate("expiryDate"),
                    resultSet.getString("storageLocation"),
                    resultSet.getBoolean("requiresPrescription")
            );
        } catch (SQLException e) {
            System.err.println("Error mapping result set to Medicine: " + e.getMessage());
            throw e;
        }
    }
} 
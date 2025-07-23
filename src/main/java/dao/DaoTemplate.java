package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import adt.ArrayBucketList;
import utility.HikariConnectionPool;

/**
 * @author: Ho Kang Kai
 * DaoTemplate - Module 1 - 5
 * Generic DAO template for database operations
 */

public abstract class DaoTemplate<T> {
    public abstract T findById(String Id) throws SQLException;
    public abstract ArrayBucketList<String, T> findAll() throws SQLException;
    public abstract String getNewId() throws SQLException;
    public abstract boolean insert(T object) throws SQLException;
    public abstract boolean update(T object) throws SQLException;
    public abstract boolean delete(String id) throws SQLException;
    protected abstract T mapResultSet(ResultSet resultSet) throws SQLException;
    
    /**
     * Generic method to get the next ID for any table using database triggers
     * This method inserts a temporary record to trigger the ID generation,
     * retrieves the generated ID, then deletes the temporary record
     * 
     * @param tableName The name of the table
     * @param idColumnName The name of the ID column
     * @param tempInsertSql SQL statement to insert a temporary record (should set ID to NULL)
     * @param tempDeleteSql SQL statement to delete the temporary record
     * @return The generated ID
     * @throws SQLException if database operation fails
     */
    protected String getNextIdFromDatabase(String tableName, String idColumnName, 
                                         String tempInsertSql, String tempDeleteSql) throws SQLException {
        try (Connection connection = HikariConnectionPool.getInstance().getConnection()) {
            // Start transaction
            connection.setAutoCommit(false);
            
            try {
                // Insert temporary record to trigger ID generation
                try (PreparedStatement insertStatement = connection.prepareStatement(tempInsertSql)) {
                    insertStatement.executeUpdate();
                }
                
                // Get the generated ID
                String selectSql = "SELECT " + idColumnName + " FROM " + tableName + 
                                 " ORDER BY " + idColumnName + " DESC LIMIT 1";
                String generatedId = null;
                
                try (Statement selectStatement = connection.createStatement();
                     ResultSet resultSet = selectStatement.executeQuery(selectSql)) {
                    if (resultSet.next()) {
                        generatedId = resultSet.getString(idColumnName);
                    }
                }
                
                // Delete the temporary record
                try (PreparedStatement deleteStatement = connection.prepareStatement(tempDeleteSql)) {
                    deleteStatement.executeUpdate();
                }
                
                // Commit transaction
                connection.commit();
                
                return generatedId;
                
            } catch (SQLException e) {
                // Rollback transaction on error
                connection.rollback();
                throw e;
            } finally {
                // Reset auto-commit
                connection.setAutoCommit(true);
            }
        }
    }
}

package dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import adt.ArrayBucketList;

/**
 * @author: Ho Kang Kai
 *          DaoTemplate - Module 1 - 5
 *          Generic DAO template for database operations
 */

public abstract class DaoTemplate<T> {
    public abstract T findById(String Id) throws SQLException;

    public abstract ArrayBucketList<String, T> findAll() throws SQLException;

    public abstract boolean insertAndReturnId(T object) throws SQLException;

    public abstract boolean update(T object) throws SQLException;

    public abstract boolean delete(String id) throws SQLException;

    protected abstract T mapResultSet(ResultSet resultSet) throws SQLException;
}

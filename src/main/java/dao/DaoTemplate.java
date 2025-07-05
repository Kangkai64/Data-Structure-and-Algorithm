package dao;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DaoTemplate<T> {
    public abstract T findById(String Id) throws SQLException;
    public abstract boolean insert(T object) throws SQLException;
    public abstract boolean update(T object) throws SQLException;
    public abstract boolean delete(String id) throws SQLException;
    protected abstract T mapResultSet(ResultSet resultSet) throws SQLException;
}

package com.student.ohmyibatis.core.transaction;

import com.student.ohmyibatis.core.datasource.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Student
 */
public interface TransactionManager
{
    void commit() throws SQLException;

    void rollback() throws SQLException;

    void close() throws Exception;

    void setConnection(Connection connection);

    void setDataSource(DataSource dataSource);

    Connection getConnection() throws SQLException;

    DataSource getDataSource();
}

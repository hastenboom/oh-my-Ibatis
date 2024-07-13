package com.student.ohmyibatis.core.datasource;

import java.sql.Connection;

/**
 * @author Student
 */
public interface DataSource
{
    Connection getConnection() throws Exception;

    void releaseConnection(Connection connection) throws Exception;

    void close() throws Exception;
}

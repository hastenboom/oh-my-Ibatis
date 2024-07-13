package com.student.ohmyibatis.core.datasource;

import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Student
 */
@RequiredArgsConstructor
public class UnpooledDataSource implements DataSource
{
    final private DataSourceProperty dataSourceProperty;

    @Override
    public Connection getConnection() throws SQLException
    {
        try
        {
            Class.forName(dataSourceProperty.getDriverClassName());
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        return DriverManager.getConnection(
                dataSourceProperty.getUrl(),
                dataSourceProperty.getUsername(),
                dataSourceProperty.getPassword()
        );

    }

    @Override
    public void releaseConnection(Connection connection) throws Exception
    {

    }

    @Override
    public void close() throws Exception
    {

    }


}

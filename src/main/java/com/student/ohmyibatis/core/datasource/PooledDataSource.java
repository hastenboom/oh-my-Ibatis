package com.student.ohmyibatis.core.datasource;

import lombok.Data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: that's a fairly simple PooledDataSource,
 *  more considerations like the thread safety should take into account;
 *
 * @author Student
 */
@Data
public class PooledDataSource implements DataSource
{
    private final DataSourceProperty dataSourceProperty;
    private final List<Connection> connectionPool = new ArrayList<>();


    public PooledDataSource(DataSourceProperty dataSourceProperty)
    {
        this.dataSourceProperty = dataSourceProperty;
        try
        {
            Class.forName(dataSourceProperty.getDriverClassName());
            for (int i = 0; i < dataSourceProperty.getMaxSize(); i++)
            {
                String url = dataSourceProperty.getUrl();
                String username = dataSourceProperty.getUsername();
                String password = dataSourceProperty.getPassword();
                try
                {
                    connectionPool.add(DriverManager.getConnection(url, username, password));
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    //FIXME: Thread-unsafe
    @Override
    public Connection getConnection() throws SQLException
    {
        if (connectionPool.isEmpty())
        {
            throw new SQLException("Connection pool is empty");
        }
        return connectionPool.remove(0);
    }

    //FIXME: Thread-unsafe
    @Override
    public void releaseConnection(Connection connection) throws Exception
    {
        connectionPool.add(connection);
    }

    @Override
    public void close() throws Exception
    {
        for (Connection connection : connectionPool)
        {
            connection.close();
        }
        connectionPool.clear();
    }


}

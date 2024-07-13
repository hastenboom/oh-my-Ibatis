package com.student.ohmyibatis.core.transaction;


import com.student.ohmyibatis.core.datasource.DataSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Student
 */
@Slf4j
@AllArgsConstructor
@Data
public class JdbcTransaction implements TransactionManager
{
    private Connection connection;
    private DataSource dataSource;
//    protected boolean autoCommmit;

    @Override
    public void commit() throws SQLException
    {
/*        if (connection != null && !connection.getAutoCommit())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Committing JDBC Connection [" + connection + "]");
            }
        }*/
        connection.commit();
    }

    @Override
    public void rollback() throws SQLException
    {
        /*if (connection != null && !connection.getAutoCommit())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Rolling back JDBC Connection [" + connection + "]");
            }
        }*/
        connection.rollback();
    }

    @Override
    public void close() throws Exception
    {
        /*if (connection != null)
        {
            resetAutoCommit();
            if (log.isDebugEnabled())
            {
                log.debug("Closing JDBC Connection [" + connection + "]");
            }
        }*/
        dataSource.releaseConnection(connection);

    }

    protected void resetAutoCommit()
    {
        try
        {
            if (!connection.getAutoCommit())
            {
                // MyBatis does not call commit/rollback on a connection if just selects were performed.
                // Some databases start transactions with select statements
                // and they mandate a commit/rollback before closing the connection.
                // A workaround is setting the autocommit to true before closing the connection.
                // Sybase throws an exception here.
                if (log.isDebugEnabled())
                {
                    log.debug("Resetting autocommit to true on JDBC Connection [" + connection + "]");
                }
                connection.setAutoCommit(true);
            }
        }
        catch (SQLException e)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Error resetting autocommit to true "
                        + "before closing the connection.  Cause: " + e);
            }
        }
    }
}

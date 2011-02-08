package com.ning.metrics.goodwill.dao;


import com.ning.metrics.goodwill.store.MySQLStore;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class DAOUtil
{
    private static Logger log = Logger.getLogger(MySQLStore.class);

    /**
     * Quietly close the Connection
     *
     * @param connection The Connection to be closed quietly
     */
    public static void close(Connection connection)
    {
        if (connection != null) {
            try {
                connection.close();
            }
            catch (SQLException e) {
                log.warn("Closing Connection failed: " + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Quietly close the Statement
     *
     * @param statement The Statement to be closed quietly
     */
    public static void close(Statement statement)
    {
        if (statement != null) {
            try {
                statement.close();
            }
            catch (SQLException e) {
                log.warn("Closing Statement failed: " + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Quietly close the ResultSet.
     *
     * @param resultSet The ResultSet to be closed quietly
     */
    public static void close(ResultSet resultSet)
    {
        if (resultSet != null) {
            try {
                resultSet.close();
            }
            catch (SQLException e) {
                log.warn("Closing ResultSet failed: " + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Quietly close the Connection and Statement.
     *
     * @param connection The Connection to be closed quietly
     * @param statement  The Statement to be closed quietly
     */
    public static void close(Connection connection, Statement statement)
    {
        close(statement);
        close(connection);
    }

    /**
     * Quietly close the Connection, Statement and ResultSet
     *
     * @param connection The Connection to be closed quietly
     * @param statement  The Statement to be closed quietly
     * @param resultSet  The ResultSet to be closed quietly
     */
    public static void close(Connection connection, Statement statement, ResultSet resultSet)
    {
        close(resultSet);
        close(statement);
        close(connection);
    }
}

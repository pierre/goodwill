package com.ning.metrics.goodwill.sink;

import com.google.inject.Inject;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.store.ThriftField;
import com.ning.metrics.goodwill.store.ThriftType;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.netezza.datasource.NzDatasource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class NetezzaSink implements GoodwillSink
{
    private final Logger log = Logger.getLogger(NetezzaSink.class);

    private Connection connection;
    private final String extraSQL;
    private final String tableNameFormat;

    @Inject
    public NetezzaSink(
        GoodwillConfig config
    ) throws SQLException, IOException, ClassNotFoundException
    {
        this(config.getSinkDBHost(), config.getSinkDBPort(), config.getSinkDBName(), config.getSinkDBUsername(), config.getSinkDBPassword(),
            config.getSinkExtraSQL(), config.getSinkDBTableNameFormat());
    }

    public NetezzaSink(
        String DBHost,
        int DBPort,
        String DBName,
        String DBUsername,
        String DBPassword,
        String extraSQL,
        String tableNameFormat
    ) throws SQLException, IOException, ClassNotFoundException
    {
        connectToNetezza(DBHost, DBPort, DBName, DBUsername, DBPassword);
        this.extraSQL = extraSQL;
        this.tableNameFormat = tableNameFormat;
    }

    /**
     * Add a new type to the sink
     * <p/>
     * For Netezza, this means creating a table where the data can be dumped. The CREATE TABLE statement
     * is constructed from the SQL information documented in the ThriftFields.
     *
     * @param thriftType ThriftType to add
     */
    @Override
    public boolean addType(ThriftType thriftType)
    {
        String createTableStatement = getCreateTableStatement(thriftType);

        try {
            Statement statement = connection.createStatement();
            statement.addBatch(createTableStatement);

            PreparedStatement extraStatement = null;
            if (extraSQL != null) {
                extraStatement = connection.prepareStatement(extraSQL);
                int i = 0;
                while (i < extraStatement.getParameterMetaData().getParameterCount()) {
                    extraStatement.setString(i, thriftType.getName());
                }
                extraStatement.addBatch();
            }

            log.info(String.format("Netezza sink: %s", statement.executeBatch().toString()));
            if (extraStatement != null) {
                log.info(String.format("Netezza sink extra SQL: %s", extraStatement.executeBatch().toString()));
            }

            connection.commit();
            return true;
        }
        catch (SQLException e) {
            log.warn(e);
            return false;
        }
    }

    private String getCreateTableStatement(ThriftType thriftType)
    {
        String tableName = getTableName(thriftType);
        String statement = String.format("CREATE TABLE %s (", tableName);

        for (ThriftField field : thriftType.getThriftItems()) {
            statement += String.format("%s %s,", sanitizeThriftName(field.getName()), field.getFullSQLType());
        }
        statement = StringUtils.chop(statement); // remove last comma
        statement += ") DISTRIBUTE ON RANDOM;";

        return statement;
    }

    private String getTableName(ThriftType thriftType)
    {
        return String.format(tableNameFormat, sanitizeThriftName(thriftType.getName()));
    }

    private String sanitizeThriftName(String name)
    {
        return StringUtils.lowerCase(StringUtils.deleteWhitespace(name));
    }

    /**
     * Update a type to the sink
     * <p/>
     * Updating a table in Netezza can be quite tricky. Don't do it.
     *
     * @param thriftType ThriftType to update
     * @return true is success, false otherwise
     */
    @Override
    public boolean updateType(ThriftType thriftType)
    {
        return false;
    }

    /**
     * Give information on how to add a Type in the sink
     *
     * @param thriftType ThriftType to add
     * @return info how to create a Type in the sink
     */
    @Override
    public String addTypeInfo(ThriftType thriftType)
    {
        String info = String.format("%s\n", getCreateTableStatement(thriftType));

        if (extraSQL != null) {
            info += StringUtils.replace(extraSQL, "?", getTableName(thriftType));
        }

        return info;
    }


    public void close() throws SQLException
    {
        connection.close();
    }


    private void connectToNetezza(String host, int port, String db, String username, String password) throws SQLException, ClassNotFoundException
    {
        NzDatasource datasource = new NzDatasource();
        datasource.setHost(host);
        datasource.setPort(port);
        datasource.setDatabase(db);
        datasource.setUser(username);
        datasource.setPassword(password);

        connection = datasource.getConnection();
        connection.setAutoCommit(false);
    }
}

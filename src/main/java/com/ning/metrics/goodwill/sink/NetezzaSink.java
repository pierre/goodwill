package com.ning.metrics.goodwill.sink;

import com.google.inject.Inject;
import com.ning.metrics.goodwill.access.GoodwillSchema;
import com.ning.metrics.goodwill.access.GoodwillSchemaField;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.netezza.datasource.NzDatasource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class NetezzaSink implements GoodwillSink
{
    private final Logger log = Logger.getLogger(NetezzaSink.class);

    private final GoodwillConfig config;

    @Inject
    public NetezzaSink(
        GoodwillConfig config
    )
    {
        this.config = config;
    }

    /**
     * Add a new type to the sink
     * <p/>
     * For Netezza, this means creating a table where the data can be dumped. The CREATE TABLE statement
     * is constructed from the SQL information documented in the ThriftFields.
     *
     * @param schema GoodwillSchema to add
     */
    @Override
    public boolean addType(GoodwillSchema schema)
        throws SQLException, IOException, ClassNotFoundException
    {
        boolean success = false;

        try {
            Connection connection = connectToNetezza(config.getSinkDBFirstHost(), config.getSinkDBFirstPort(), config.getSinkDBFirstSchema(),
                config.getSinkDBFirstUsername(), config.getSinkDBFirstPassword());

            String createTableStatement = getCreateTableStatement(schema);
            Statement statement = connection.createStatement();
            statement.addBatch(createTableStatement);

            statement.executeBatch();
            log.info(String.format("Added Thrift to Netezza: %s", schema.getName()));
            connection.commit();

            connection.close();
            success = true;

            if (config.getSinkFirstExtraSQL() != null) {
                success = executeExtraSql(config.getSinkDBFirstHost(), config.getSinkDBFirstPort(), config.getSinkDBFirstSchema(),
                    config.getSinkDBFirstUsername(), config.getSinkDBFirstPassword(), config.getSinkFirstExtraSQL(), schema);

                if (success && config.getSinkSecondExtraSQL() != null) {
                    success = executeExtraSql(config.getSinkDBSecondHost(), config.getSinkDBSecondPort(), config.getSinkDBSecondSchema(),
                        config.getSinkDBSecondUsername(), config.getSinkDBSecondPassword(), config.getSinkSecondExtraSQL(), schema);
                }
            }

            return success;
        }
        catch (SQLException e) {
            log.warn(String.format("Unable to add Type to Netezza: %s", e));
            return success;
        }
    }

    private boolean executeExtraSql(String host, int port, String database, String username,
                                    String password, String statement, GoodwillSchema schema)
    {
        try {
            Connection connection = connectToNetezza(host, port, database, username, password);

            String safeSQL = getUnescapedStatement(statement, schema);

            log.info(String.format("Running extra SQL in Netezza: %s", safeSQL));
            Statement extraStatement = connection.createStatement();
            extraStatement.execute(safeSQL);
            connection.commit();

            connection.close();
            return true;
        }
        catch (SQLException e) {
            log.warn(String.format("Unable to run extra SQL in Netezza: %s", e));
            return false;
        }
        catch (ClassNotFoundException e) {
            log.warn(String.format("Unable to run extra SQL in Netezza: %s", e));
            return false;
        }
    }

    private String getUnescapedStatement(String statement, GoodwillSchema schema)
    {
        // TODO: hack. We do a manual replacement first because escaping can do strange things. More thoughts needed here.
        // TODO: hack. Maven escapes strangely parameters on the command line, replace manually \* with *.
        String safeSQL = StringUtils.replace(statement, "\\*", "*");
        safeSQL = StringUtils.replace(safeSQL, "?", getTableName(schema));
        return safeSQL;
    }

    private String getCreateTableStatement(GoodwillSchema schema)
    {
        String tableName = getTableName(schema);
        String statement = String.format("CREATE TABLE %s (", tableName);

        for (GoodwillSchemaField field : schema.getSchema()) {
            statement += String.format("%s %s,", sanitizeThriftName(field.getName()), field.getFullSQLType());
        }
        statement = StringUtils.chop(statement); // remove last comma
        statement += ") DISTRIBUTE ON RANDOM;";

        return statement;
    }

    private String getTableName(GoodwillSchema schema)
    {
        return String.format(config.getSinkDBTableNameFormat(), sanitizeThriftName(schema.getName()));
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
     * @param schema ThriftType to update
     * @return true is success, false otherwise
     */
    @Override
    public boolean updateType(GoodwillSchema schema)
    {
        return false;
    }

    /**
     * Give information on how to add a Type in the sink
     *
     * @param schema ThriftType to add
     * @return info how to create a Type in the sink
     */
    @Override
    public String addTypeInfo(GoodwillSchema schema)
    {
        String info = String.format("%s\n", getCreateTableStatement(schema));

        if (config.getSinkFirstExtraSQL() != null) {
            info += getUnescapedStatement(config.getSinkFirstExtraSQL(), schema);

            if (config.getSinkSecondExtraSQL() != null) {
                info += getUnescapedStatement(config.getSinkSecondExtraSQL(), schema);
            }
        }

        return info;
    }

    private Connection connectToNetezza(String host, int port, String db, String username, String password) throws SQLException, ClassNotFoundException
    {
        NzDatasource datasource = new NzDatasource();
        datasource.setHost(host);
        datasource.setPort(port);
        datasource.setDatabase(db);
        datasource.setUser(username);
        datasource.setPassword(password);

        Connection connection = datasource.getConnection();
        connection.setAutoCommit(false);

        return connection;
    }
}

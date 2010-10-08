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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class NetezzaSink implements GoodwillSink
{
    private final Logger log = Logger.getLogger(NetezzaSink.class);

    private Connection connection;
    private final String extraSQL;
    private final String tableNameFormat;
    private final String DBHost;
    private final int DBPort;
    private final String DBName;
    private final String DBUsername;
    private final String DBPassword;

    @Inject
    public NetezzaSink(
        GoodwillConfig config
    )
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
    )
    {
        this.DBHost = DBHost;
        this.DBPort = DBPort;
        this.DBName = DBName;
        this.DBUsername = DBUsername;
        this.DBPassword = DBPassword;

        // TODO: hack. Maven escapes strangely parameters on the command line, replace manually \* with *.
        this.extraSQL = StringUtils.replace(extraSQL, "\\*", "*");
        this.tableNameFormat = tableNameFormat;
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
        connectToNetezza(DBHost, DBPort, DBName, DBUsername, DBPassword);

        String createTableStatement = getCreateTableStatement(schema);

        try {
            Statement statement = connection.createStatement();
            statement.addBatch(createTableStatement);

            PreparedStatement extraStatement = null;
            if (extraSQL != null) {
                // TODO: hack. We do a manual replacement first because escaping can do strange things. More thoughts needed here.
                extraStatement = connection.prepareStatement(StringUtils.replace(extraSQL, "?", getTableName(schema)));
//                int i = 1;
//                while (i <= extraStatement.getParameterMetaData().getParameterCount()) {
//                    extraStatement.setString(i, schema.getName());
//                    i++;
//                }
                extraStatement.addBatch();
            }

            // We need to commit in two stages unfortunately, because the free-form SQL snippet may refer to the table in the first
            // statement (e.g. to create a view).
            log.info(String.format("Adding Thrift to Netezza: %s", statement.executeBatch().toString()));
            connection.commit();

            if (extraStatement != null) {
                log.info(String.format("Running extra SQL in Netezza: %s", extraStatement.executeBatch().toString()));
                connection.commit();
            }

            return true;
        }
        catch (SQLException e) {
            log.warn(String.format("Unable to add Type to Netezza: %s", e));
            return false;
        }
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
        return String.format(tableNameFormat, sanitizeThriftName(schema.getName()));
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

        if (extraSQL != null) {
            info += StringUtils.replace(extraSQL, "?", getTableName(schema));
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

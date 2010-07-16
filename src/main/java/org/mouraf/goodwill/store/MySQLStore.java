package org.mouraf.goodwill.store;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.mouraf.goodwill.binder.config.GoodwillConfig;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MySQLStore implements GoodwillStore
{
    private static Logger log = Logger.getLogger(MySQLStore.class);

    private final String TABLE_STRING_DESCRIPTOR =
        "event_type = ?, " +
            "field_id = ?, " +
            "field_type = ?, " +
            "field_name = ?, " +
            "sql_type = ?, " +
            "sql_length = ?, " +
            "description = ?";

    private Connection connection;
    private final String tableName;

    private Map<String, ThriftType> thriftTypes;

    @Inject
    public MySQLStore(
        GoodwillConfig config
    ) throws SQLException, IOException, ClassNotFoundException
    {
        this(config.getDBHost(), config.getDBName(), config.getDBUsername(), config.getDBThriftTableName());
    }

    public MySQLStore(
        String DBHost,
        String DBName,
        String DBUsername,
        String DBTableName
    ) throws SQLException, IOException, ClassNotFoundException
    {
        tableName = DBTableName;
        connectToMySQL(DBHost, DBName, DBUsername);
        buildThrifTtypeList();
    }

    private void buildThrifTtypeList() throws IOException
    {
        HashMap<String, ThriftType> thriftTypes = new HashMap<String, ThriftType>();
        ThriftType currentThriftType = null;
        String currentThriftTypeName = null;
        try {
            Statement select = connection.createStatement();
            ResultSet result = select.executeQuery(String.format("SELECT event_type, field_name, field_type, field_id, description, sql_type, sql_length FROM %s", tableName));

            while (result.next()) {
                String thriftType = result.getString(1);

                // Don't conver sqlLength from NULL to 0
                Integer sqlLength = result.getInt(7);
                if (result.wasNull()) {
                    sqlLength = null;
                }

                ThriftField thriftField = new ThriftField(result.getString(2), result.getString(3), result.getInt(4), result.getString(5), result.getString(6), sqlLength);

                if (currentThriftTypeName == null || !thriftType.equals(currentThriftTypeName)) {
                    currentThriftTypeName = thriftType;

                    // Do we have records for this Type already?
                    if (thriftTypes != null && thriftTypes.get(currentThriftTypeName) != null) {
                        currentThriftType = thriftTypes.get(currentThriftTypeName);
                    }
                    else {
                        currentThriftType = new ThriftType(currentThriftTypeName);
                        thriftTypes.put(currentThriftTypeName, currentThriftType);
                        log.debug(String.format("Found new ThriftType: %s", currentThriftTypeName));
                    }
                }

                currentThriftType.addThriftField(thriftField);
                log.debug(String.format("Added ThriftField to %s: %s", currentThriftTypeName, thriftField.toString()));
            }

        }
        catch (SQLException e) {
            throw new IOException(e);
        }

        this.thriftTypes = thriftTypes;
    }

    public void close() throws SQLException
    {
        connection.close();
    }

    @Override
    public Collection<ThriftType> getTypes() throws IOException
    {
        buildThrifTtypeList();
        return thriftTypes.values();
    }

    /**
     * Given a Thrift name, find it in the store
     *
     * @param typeName name of the Thrift to search
     * @return the ThriftType if found, null otherwise
     */
    @Override
    public ThriftType findByName(String typeName)
    {
        try {
            for (ThriftType thriftType : getTypes()) {
                if (thriftType.getName().equals(typeName)) {
                    return thriftType;
                }
            }
        }
        catch (IOException e) {
            log.warn("Unable to fetch Thrift types", e);
        }

        return null;
    }

    /**
     * Serialize all Thrifts in the store
     *
     * @return JSONArray representation
     * @throws org.json.JSONException
     */
    @Override
    public JSONArray toJSON() throws JSONException
    {
        JSONArray array = new JSONArray();
        try {
            for (ThriftType type : getTypes()) {
                array.put(type.toJSON());
            }
        }
        catch (IOException e) {
            log.warn("Unable to fetch Thrift types", e);
        }

        return array;
    }

    /**
     * Add a new type to the store
     *
     * @param thriftType ThriftType to add
     */
    @Override
    public void addType(ThriftType thriftType)
    {
        try {
            PreparedStatement sqlInsert = connection.prepareStatement(String.format("insert into %s set %s", tableName, TABLE_STRING_DESCRIPTOR));

            for (ThriftField field : thriftType.getThriftItems()) {
                addSQLStatementToBatch(sqlInsert, thriftType, field);
            }

            sqlInsert.executeBatch();
            connection.commit();
        }
        catch (SQLException e) {
            log.error(String.format("Unable to add type [%s]: %s", thriftType, e));
            try {
                connection.rollback();
            }
            catch (SQLException e1) {
                log.error("Got a SQLException during a rollback!");
            }
        }
    }

    private void addSQLStatementToBatch(PreparedStatement statement, ThriftType thriftType, ThriftField field)
        throws SQLException
    {
        statement.setString(1, thriftType.getName());
        statement.setInt(2, field.getPosition());
        statement.setString(3, field.getType());
        statement.setString(4, field.getName());
        if (field.getSqlType() == null) {
            statement.setNull(5, Types.VARCHAR);
        }
        else {
            statement.setString(5, field.getSqlType());
        }
        if (field.getSqlLength() == null) {
            statement.setNull(6, Types.INTEGER);
        }
        else {
            statement.setInt(6, field.getSqlLength());
        }
        if (field.getDescription() == null) {
            statement.setNull(7, Types.VARCHAR);
        }
        else {
            statement.setString(7, field.getDescription());
        }
        statement.addBatch();
    }

    /**
     * Update a type to the store
     *
     * @param thriftType ThriftType to update
     */
    @Override
    public boolean updateType(ThriftType thriftType)
    {
        try {
            Statement select = connection.createStatement();
            PreparedStatement inserts = connection.prepareStatement(String.format("INSERT INTO %s SET %s", tableName, TABLE_STRING_DESCRIPTOR));
            PreparedStatement updates = connection.prepareStatement(String.format("UPDATE %s SET %s WHERE id = ?", tableName, TABLE_STRING_DESCRIPTOR));

            // Update all fields
            for (ThriftField field : thriftType.getThriftItems()) {
                // There needs to be a UNIQUE constraint on (event_type, field_id)
                ResultSet result = select.executeQuery(String.format("SELECT id FROM %s WHERE event_type = '%s' AND field_id = %d LIMIT 1", tableName, thriftType.getName(), field.getPosition()));
                boolean seen = false;

                while (result.next()) {
                    if (seen) {
                        throw new SQLException(String.format("Duplicated Thiftfield [%s]! add a UNIQUE constraint on (event_type, field_id)", field));
                    }
                    else {
                        seen = true;
                    }

                    int key = result.getInt(1);

                    updates.setInt(8, key);
                    addSQLStatementToBatch(updates, thriftType, field);
                }

                if (!seen) {
                    addSQLStatementToBatch(inserts, thriftType, field);
                }
            }

            log.info(String.format("ThriftType updates: %s", updates.executeBatch().toString()));
            log.info(String.format("ThriftType inserts: %s", inserts.executeBatch().toString()));
            connection.commit();
        }
        catch (SQLException e) {
            log.error(String.format("Unable to modify type [%s]: %s", thriftType, e));
            return false;
        }

        return true;
    }

    private void connectToMySQL(String host, String db, String username) throws SQLException, ClassNotFoundException
    {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(String.format("jdbc:mysql://%s/%s?user=%s", host, db, username));
        connection.setAutoCommit(false);
    }
}

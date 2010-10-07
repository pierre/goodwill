/*
 * Copyright 2010 Ning, Inc.
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.ning.metrics.goodwill.store;

import com.google.inject.Inject;
import com.ning.metrics.goodwill.access.ThriftField;
import com.ning.metrics.goodwill.access.ThriftType;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MySQLStore extends GoodwillStore
{
    private static Logger log = Logger.getLogger(MySQLStore.class);

    private final String TABLE_STRING_DESCRIPTOR =
        "event_type = ?, " +
            "field_id = ?, " +
            "field_type = ?, " +
            "field_name = ?, " +
            "sql_type = ?, " +
            "sql_length = ?, " +
            "sql_precision = ?, " +
            "sql_scale = ?, " +
            "description = ?";

    private Connection connection;
    private final String tableName;

    private Map<String, ThriftType> thriftTypes;

    @Inject
    public MySQLStore(
        GoodwillConfig config
    ) throws SQLException, IOException, ClassNotFoundException
    {
        this(config.getStoreDBHost(), config.getStoreDBPort(), config.getStoreDBName(), config.getStoreDBUsername(), config.getStoreDBPassword(), config.getStoreDBThriftTableName());
    }

    public MySQLStore(
        String DBHost,
        int DBPort,
        String DBName,
        String DBUsername,
        String DBPassword,
        String DBTableName
    ) throws SQLException, IOException, ClassNotFoundException
    {
        tableName = DBTableName;
        connectToMySQL(DBHost, DBPort, DBName, DBUsername, DBPassword);
        buildThrifTtypeList();
    }

    @Override
    public Collection<ThriftType> getTypes() throws IOException
    {
        buildThrifTtypeList();

        final ArrayList<ThriftType> thriftTypesList = new ArrayList(thriftTypes.values());
        Collections.sort(thriftTypesList, new Comparator<ThriftType>()
        {
            @Override
            public int compare(ThriftType o, ThriftType o1)
            {
                return o.getName().compareTo(o1.getName());
            }
        });

        return thriftTypesList;
    }

    /**
     * Add a new type to the store
     *
     * @param thriftType ThriftType to add
     */
    @Override
    public void addType(ThriftType thriftType)
    {
        // Creating a new Schema is really the same as updating/extending one, since the data model we use define schemas
        // as a list of fields.
        updateType(thriftType);
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
            for (ThriftField field : thriftType.getSchema()) {
                // There needs to be a UNIQUE constraint on (event_type, field_id)
                ResultSet result = select.executeQuery(String.format("SELECT id FROM %s WHERE event_type = '%s' AND field_id = %d LIMIT 1", tableName, thriftType.getName(), field.getId()));
                boolean seen = false;

                while (result.next()) {
                    if (seen) {
                        throw new SQLException(String.format("Duplicated Thiftfield [%s]! add a UNIQUE constraint on (event_type, field_id)", field));
                    }
                    else {
                        seen = true;
                    }

                    int key = result.getInt(1);

                    // Needs to be changed if TABLE_STRING_DESCRIPTOR changes!
                    updates.setInt(10, key);
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

    private void buildThrifTtypeList() throws IOException
    {
        HashMap<String, ThriftType> thriftTypes = new HashMap<String, ThriftType>();
        ThriftType currentThriftType = null;
        String currentThriftTypeName = null;
        try {
            Statement select = connection.createStatement();
            ResultSet result = select.executeQuery(String.format("SELECT event_type, field_name, field_type, field_id, description, sql_type, sql_length, sql_scale, sql_precision FROM %s ORDER BY field_id ASC", tableName));

            while (result.next()) {
                String thriftType = result.getString(1);

                // Don't convert int from NULL to 0
                Integer sqlLength = result.getInt(7);
                if (result.wasNull()) {
                    sqlLength = null;
                }
                Integer sqlScale = result.getInt(8);
                if (result.wasNull()) {
                    sqlScale = null;
                }
                Integer sqlPrecision = result.getInt(9);
                if (result.wasNull()) {
                    sqlPrecision = null;
                }

                ThriftField thriftField;
                try {
                    thriftField = new ThriftField(result.getString(2), result.getString(3), result.getShort(4), result.getString(5), result.getString(6), sqlLength, sqlScale, sqlPrecision);
                }
                catch (IllegalArgumentException e) {
                    log.warn(e);
                    continue;
                }

                if (currentThriftTypeName == null || !thriftType.equals(currentThriftTypeName)) {
                    currentThriftTypeName = thriftType;

                    // Do we have records for this Type already?
                    if (thriftTypes != null && thriftTypes.get(currentThriftTypeName) != null) {
                        currentThriftType = thriftTypes.get(currentThriftTypeName);
                    }
                    else {
                        currentThriftType = new ThriftType(currentThriftTypeName, new ArrayList<ThriftField>());
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

    private void addSQLStatementToBatch(PreparedStatement statement, ThriftType thriftType, ThriftField field)
        throws SQLException
    {
        statement.setString(1, thriftType.getName());
        statement.setInt(2, field.getId());
        statement.setString(3, field.getType().name());
        statement.setString(4, field.getName());
        if (field.getSql().getType() == null) {
            statement.setNull(5, Types.VARCHAR);
        }
        else {
            statement.setString(5, field.getSql().getType());
        }
        if (field.getSql().getLength() == null) {
            statement.setNull(6, Types.INTEGER);
        }
        else {
            statement.setInt(6, field.getSql().getLength());
        }
        if (field.getSql().getPrecision() == null) {
            statement.setNull(7, Types.INTEGER);
        }
        else {
            statement.setInt(7, field.getSql().getPrecision());
        }
        if (field.getSql().getScale() == null) {
            statement.setNull(8, Types.INTEGER);
        }
        else {
            statement.setInt(8, field.getSql().getScale());
        }
        if (field.getDescription() == null) {
            statement.setNull(9, Types.VARCHAR);
        }
        else {
            statement.setString(9, field.getDescription());
        }
        statement.addBatch();
    }

    private void connectToMySQL(String host, int port, String db, String username, String password) throws SQLException, ClassNotFoundException
    {
        Class.forName("com.mysql.jdbc.Driver");
        connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%d/%s", host, port, db), username, password);
        connection.setAutoCommit(false);
    }

    public void close() throws SQLException
    {
        connection.close();
    }
}

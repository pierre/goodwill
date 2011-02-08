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
import com.ning.metrics.goodwill.access.GoodwillSchema;
import com.ning.metrics.goodwill.access.GoodwillSchemaField;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.dao.DAOAccess;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
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

import static com.ning.metrics.goodwill.dao.DAOUtil.close;

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

    private final String tableName;
    private final DAOAccess access;

    @Inject
    public MySQLStore(
        GoodwillConfig config,
        DAOAccess access
    ) throws IOException
    {
        this(config.getStoreDBThriftTableName(), access);
    }

    public MySQLStore(
        String DBTableName,
        DAOAccess access
    ) throws IOException
    {
        tableName = DBTableName;
        this.access = access;

        buildGoodwillSchemaList();
    }

    @Override
    public Collection<GoodwillSchema> getTypes() throws IOException
    {
        buildGoodwillSchemaList();

        final ArrayList<GoodwillSchema> thriftTypesList = new ArrayList(goodwillSchemata.values());
        Collections.sort(thriftTypesList, new Comparator<GoodwillSchema>()
        {
            @Override
            public int compare(GoodwillSchema o, GoodwillSchema o1)
            {
                return o.getName().compareTo(o1.getName());
            }
        });

        if (sink != null) {
            for (int i = 0; i < thriftTypesList.size(); i++) {
                GoodwillSchema schema = thriftTypesList.get(i);
                schema.setSinkAddInfo(sink.addTypeInfo(schema));
                thriftTypesList.set(i, schema);

            }
        }

        return thriftTypesList;
    }

    /**
     * Add a new type to the store
     *
     * @param schema GoodwillSchema to add
     */
    @Override
    public void addType(GoodwillSchema schema)
    {
        // Creating a new Schema is really the same as updating/extending one, since the data model we use define schemas
        // as a list of fields.
        updateType(schema);
    }

    /**
     * Update a type to the store
     *
     * @param schema GoodwillSchema to update
     */
    @Override
    public boolean updateType(GoodwillSchema schema)
    {
        Connection connection = null;
        Statement select = null;
        PreparedStatement inserts = null;
        PreparedStatement updates = null;
        ResultSet result = null;

        try {
            connection = getConnection();
            select = connection.createStatement();
            inserts = connection.prepareStatement(sqlInsertField());
            updates = connection.prepareStatement(sqlUpdateField());

            // Update all fields
            for (GoodwillSchemaField field : schema.getSchema()) {
                // There needs to be a UNIQUE constraint on (event_type, field_id)
                result = select.executeQuery(sqlSelectFieldId(schema, field));
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
                    addSQLStatementToBatch(updates, schema, field);
                }

                if (!seen) {
                    addSQLStatementToBatch(inserts, schema, field);
                }
            }

            log.info(String.format("ThriftType updates: %s", updates.executeBatch().toString()));
            log.info(String.format("ThriftType inserts: %s", inserts.executeBatch().toString()));
            connection.commit();
        }
        catch (SQLException e) {
            log.error(String.format("Unable to modify type [%s]: %s", schema, e));
            return false;
        }
        finally {
            close(inserts);
            close(updates);
            close(connection, select, result);
        }

        return true;
    }

    /**
     * Delete a type
     *
     * @param schema GoodwillSchema to delete
     * @return true is success, false otherwise
     */
    @Override
    public boolean deleteType(GoodwillSchema schema)
    {
        Connection connection = null;
        PreparedStatement delete = null;

        try {
            connection = getConnection();
            delete = connection.prepareStatement(sqlDeleteSchema());
            delete.setString(1, schema.getName());
            delete.addBatch();

            int[] results = delete.executeBatch();
            if (results.length == 0) {
                throw new SQLException(String.format("[%s] no DELETE statement submitted", delete.toString()));
            }

            int resultCode = results[0];
            if (resultCode == PreparedStatement.EXECUTE_FAILED) {
                throw new SQLException(String.format("[%s] PreparedStatement.EXECUTE_FAILED", delete.toString()));
            }

            log.info(String.format("ThriftType deletes: [%s] %d", delete.toString(), resultCode));
            connection.commit();

            return true;
        }
        catch (SQLException e) {
            log.error(String.format("Unable to delete type [%s]: %s", schema.getName(), e));
            return false;
        }
        finally {
            close(connection, delete);
        }
    }


    private void buildGoodwillSchemaList() throws IOException
    {
        HashMap<String, GoodwillSchema> schemata = new HashMap<String, GoodwillSchema>();
        GoodwillSchema currentThriftType = null;
        String currentThriftTypeName = null;

        Connection connection = null;
        Statement select = null;
        ResultSet result = null;
        try {
            connection = getConnection();
            select = connection.createStatement();
            result = select.executeQuery(sqlSelectSchema());

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

                GoodwillSchemaField thriftField;
                try {
                    thriftField = new GoodwillSchemaField(result.getString(2), result.getString(3), result.getShort(4), result.getString(5), result.getString(6), sqlLength, sqlScale, sqlPrecision);
                }
                catch (IllegalArgumentException e) {
                    log.warn(e);
                    continue;
                }

                if (currentThriftTypeName == null || !thriftType.equals(currentThriftTypeName)) {
                    currentThriftTypeName = thriftType;

                    // Do we have records for this Type already?
                    if (schemata != null && schemata.get(currentThriftTypeName) != null) {
                        currentThriftType = schemata.get(currentThriftTypeName);
                    }
                    else {
                        currentThriftType = new GoodwillSchema(currentThriftTypeName, new ArrayList<GoodwillSchemaField>());
                        schemata.put(currentThriftTypeName, currentThriftType);
                        log.debug(String.format("Found new ThriftType: %s", currentThriftTypeName));
                    }
                }

                currentThriftType.addThriftField(thriftField);
                log.debug(String.format("Added ThriftField to %s: %s", currentThriftTypeName, thriftField.toString()));
            }

        }
        catch (SQLException e) {
            log.warn(String.format("Unable to retrieve schemata: %s", e.getLocalizedMessage()));
        }
        finally {
            close(connection, select, result);
        }

        this.goodwillSchemata = schemata;
    }

    private void addSQLStatementToBatch(PreparedStatement statement, GoodwillSchema schema, GoodwillSchemaField field)
        throws SQLException
    {
        statement.setString(1, schema.getName());
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

    /**
     * Get the select statement to find the row id for a field
     *
     * @param schema Schema the field belongs to
     * @param field  The field to look up
     * @return The select SQL statement
     */
    private String sqlSelectFieldId(GoodwillSchema schema, GoodwillSchemaField field)
    {
        return String.format("SELECT id FROM %s WHERE event_type = '%s' AND field_id = %d LIMIT 1", tableName, schema.getName(), field.getId());
    }

    /**
     * Get the select statement to retrieve a full schema
     *
     * @return The select SQL statement
     */
    private String sqlSelectSchema()
    {
        return String.format("SELECT event_type, field_name, field_type, field_id, description, sql_type, sql_length, sql_scale, sql_precision FROM %s ORDER BY field_id ASC", tableName);
    }

    /**
     * Get the update statement for a specific field
     *
     * @return The update SQL statement
     */
    private String sqlUpdateField()
    {
        return String.format("UPDATE %s SET %s WHERE id = ?", tableName, TABLE_STRING_DESCRIPTOR);
    }

    /**
     * Get the insert statement to add a field
     *
     * @return The insert SQL statement
     */
    private String sqlInsertField()
    {
        return String.format("INSERT INTO %s SET %s", tableName, TABLE_STRING_DESCRIPTOR);
    }

    /**
     * Get the delete statement to remove a schema (all fields for the schema)
     *
     * @return The delete SQL statement
     */
    private String sqlDeleteSchema()
    {
        return String.format("DELETE FROM %s WHERE event_type = ?", tableName);
    }

    private Connection getConnection() throws SQLException
    {
        return access.getDataSource().getConnection();
    }
}

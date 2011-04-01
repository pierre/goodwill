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

package com.ning.metrics.goodwill.binder.config;

import org.skife.config.Config;

public class GoodwillConfig
{
    @Config(value = "goodwill.store.type")
    public String getStoreType()
    {
        return "mysql";
    }

    @Config(value = "goodwill.store.csv.file.path")
    public String getCSVFilePath()
    {
        return "";
    }

    @Config(value = "goodwill.store.db.host")
    public String getStoreDBHost()
    {
        return "localhost";
    }

    @Config(value = "goodwill.store.db.port")
    public int getStoreDBPort()
    {
        return 3306;
    }

    @Config(value = "goodwill.store.db.name")
    public String getStoreDBName()
    {
        return "goodwill";
    }

    @Config(value = "goodwill.store.db.user")
    public String getStoreDBUsername()
    {
        return "root";
    }

    @Config(value = "goodwill.store.db.password")
    public String getStoreDBPassword()
    {
        return "";
    }

    @Config(value = "goodwill.store.db.thrift_table.name")
    public String getStoreDBThriftTableName()
    {
        return "thrift_types";
    }

    @Config(value = "goodwill.sink.type")
    public String getSinkType()
    {
        return null;
    }

    @Config(value = "goodwill.sink.db.table_name_format")
    public String getSinkDBTableNameFormat()
    {
        return "xe_%s";
    }

    @Config(value = "goodwill.sink.db.first.host")
    public String getSinkDBFirstHost()
    {
        return "localhost";
    }

    @Config(value = "goodwill.sink.db.first.port")
    public int getSinkDBFirstPort()
    {
        return 3306;
    }

    /**
     * First schema to run statements in (where the table is created)
     *
     * @return schema where to create the table
     */
    @Config(value = "goodwill.sink.db.first.schema")
    public String getSinkDBFirstSchema()
    {
        return "goodwill_sink";
    }

    @Config(value = "goodwill.sink.db.first.user")
    public String getSinkDBFirstUsername()
    {
        return "root";
    }

    @Config(value = "goodwill.sink.db.first.password")
    public String getSinkDBFirstPassword()
    {
        return "";
    }

    /**
     * To create a stage table for instance:
     * <p/>
     * CREATE TABLE STAGE_? AS SELECT * FROM ? LIMIT 0;
     *
     * @return extra SQL to run in the schema where the table is created
     */
    @Config(value = "goodwill.sink.db.first.extra_sql")
    public String getSinkFirstExtraSQL()
    {
        return null;
    }

    @Config(value = "goodwill.sink.db.first.host")
    public String getSinkDBSecondHost()
    {
        return getSinkDBFirstHost();
    }

    @Config(value = "goodwill.sink.db.first.port")
    public int getSinkDBSecondPort()
    {
        return getSinkDBFirstPort();
    }

    /**
     * Optionally, Goodwill can run statements in another schema (database). This can be useful when cross database access
     * is not supported.
     *
     * @return Second schema to run statements in
     */
    @Config(value = "goodwill.sink.db.second.schema")
    public String getSinkDBSecondSchema()
    {
        return getSinkDBFirstSchema();
    }

    @Config(value = "goodwill.sink.db.second.user")
    public String getSinkDBSecondUsername()
    {
        return getSinkDBFirstUsername();
    }

    @Config(value = "goodwill.sink.db.second.password")
    public String getSinkDBSecondPassword()
    {
        return getSinkDBFirstPassword();
    }

    @Config(value = "goodwill.sink.db.second.extra_sql")
    public String getSinkSecondExtraSQL()
    {
        return null;
    }

    @Config(value = "goodwill.action.url")
    public String getActionCoreURL()
    {
        return null;
    }

    // Whether the DELETE API is allowed.
    // In general, you don't want to enable it in production as you won't be able to read data in HDFS associated
    // to deleted events via the goodwill-access library

    @Config(value = "goodwill.api.delete")
    public boolean allowDeleteEvent()
    {
        return false;
    }
}

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
import org.skife.config.Default;
import org.skife.config.DefaultNull;

public interface GoodwillConfig
{
    @Config(value = "goodwill.store.type")
    @Default("mysql")
    public String getStoreType();

    @Config(value = "goodwill.store.csv.file.path")
    @DefaultNull
    public String getCSVFilePath();

    @Config(value = "goodwill.store.db.host")
    @Default("localhost")
    public String getStoreDBHost();

    @Config(value = "goodwill.store.db.port")
    @Default("3306")
    public int getStoreDBPort();

    @Config(value = "goodwill.store.db.name")
    @Default("goodwill")
    public String getStoreDBName();

    @Config(value = "goodwill.store.db.user")
    @Default("root")
    public String getStoreDBUsername();

    @Config(value = "goodwill.store.db.password")
    @DefaultNull
    public String getStoreDBPassword();

    @Config(value = "goodwill.store.db.thrift_table.name")
    @Default("thrift_types")
    public String getStoreDBThriftTableName();

    @Config(value = "goodwill.sink.type")
    @DefaultNull
    public String getSinkType();

    @Config(value = "goodwill.sink.db.table_name_format")
    @Default("xe_%s")
    public String getSinkDBTableNameFormat();

    @Config(value = "goodwill.sink.db.first.host")
    @Default("localhost")
    public String getSinkDBFirstHost();

    @Config(value = "goodwill.sink.db.first.port")
    @Default("3306")
    public int getSinkDBFirstPort();

    /**
     * First schema to run statements in (where the table is created)
     *
     * @return schema where to create the table
     */
    @Config(value = "goodwill.sink.db.first.schema")
    @Default("goodwill_sink")
    public String getSinkDBFirstSchema();

    @Config(value = "goodwill.sink.db.first.user")
    @Default("root")
    public String getSinkDBFirstUsername();

    @Config(value = "goodwill.sink.db.first.password")
    @DefaultNull
    public String getSinkDBFirstPassword();

    /**
     * To create a stage table for instance:
     * <p/>
     * CREATE TABLE STAGE_? AS SELECT * FROM ? LIMIT 0;
     *
     * @return extra SQL to run in the schema where the table is created
     */
    @Config(value = "goodwill.sink.db.first.extra_sql")
    @DefaultNull
    public String getSinkFirstExtraSQL();

    @Config(value = "goodwill.sink.db.second.host")
    @Default("localhost")
    public String getSinkDBSecondHost();

    @Config(value = "goodwill.sink.db.second.port")
    @Default("3306")
    public int getSinkDBSecondPort();

    /**
     * Optionally, Goodwill can run statements in another schema (database). This can be useful when cross database access
     * is not supported.
     *
     * @return Second schema to run statements in
     */
    @Config(value = "goodwill.sink.db.second.schema")
    @Default("goodwill_sink")
    public String getSinkDBSecondSchema();

    @Config(value = "goodwill.sink.db.second.user")
    @Default("root")
    public String getSinkDBSecondUsername();

    @Config(value = "goodwill.sink.db.second.password")
    @DefaultNull
    public String getSinkDBSecondPassword();

    @Config(value = "goodwill.sink.db.second.extra_sql")
    @DefaultNull
    public String getSinkSecondExtraSQL();

    @Config(value = "goodwill.action.url")
    @DefaultNull
    public String getActionCoreURL();

    // Whether the DELETE API is allowed.
    // In general, you don't want to enable it in production as you won't be able to read data in HDFS associated
    // to deleted events via the goodwill-access library

    @Config(value = "goodwill.api.delete")
    @Default("false")
    public boolean allowDeleteEvent();
}

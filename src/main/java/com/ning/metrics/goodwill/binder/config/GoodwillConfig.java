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

    @Config(value = "goodwill.sink.db.host")
    public String getSinkDBHost()
    {
        return "localhost";
    }

    @Config(value = "goodwill.sink.db.port")
    public int getSinkDBPort()
    {
        return 3306;
    }

    @Config(value = "goodwill.sink.db.name")
    public String getSinkDBName()
    {
        return "goodwill_sink";
    }

    @Config(value = "goodwill.sink.db.user")
    public String getSinkDBUsername()
    {
        return "root";
    }

    @Config(value = "goodwill.sink.db.password")
    public String getSinkDBPassword()
    {
        return "";
    }

    @Config(value = "goodwill.sink.db.table_name_format")
    public String getSinkDBTableNameFormat()
    {
        return "xe_%s";
    }

    @Config(value = "goodwill.sink.db.extra_sql")
    public String getSinkExtraSQL()
    {
        return String.format("GRANT ALL ON %s.? TO 'someuser'@'somehost';\nCREATE OR REPLACE VIEW v_count_? as SELECT COUNT(*) FROM ?;\n", getSinkDBName());
    }

    @Config(value = "goodwill.action.url")
    public String getActionCoreURL()
    {
        return null;
    }
}

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
    @Config(value = "goodwill.server.ip")
    public String getLocalIp()
    {
        return "127.0.0.1";
    }

    @Config(value = "goodwill.server.port")
    public int getLocalPort()
    {
        return 8080;
    }

    @Config(value = "goodwill.server.ssl.enabled")
    public boolean isSSLEnabled()
    {
        return false;
    }

    @Config(value = "goodwill.server.ssl.port")
    public int getLocalSSLPort()
    {
        return 443;
    }

    @Config(value = "goodwill.jetty.ssl.keystore")
    public String getSSLkeystoreLocation()
    {
        return "";
    }

    @Config(value = "goodwill.jetty.ssl.keystore.password")
    public String getSSLkeystorePassword()
    {
        return "";
    }

    @Config(value = "goodwill.store.csv.file.path")
    public String getCSVFilePath()
    {
        return "";
    }

    @Config(value = "goodwill.log.conf.file.path")
    public String getLogConfFilePath()
    {
        return null;
    }

    @Config(value = "goodwill.store.db.host")
    public String getDBHost()
    {
        return "localhost";
    }

    @Config(value = "goodwill.store.db.port")
    public int getDBPort()
    {
        return 3306;
    }

    @Config(value = "goodwill.store.db.name")
    public String getDBName()
    {
        return "goodwill";
    }

    @Config(value = "goodwill.store.db.user")
    public String getDBUsername()
    {
        return "root";
    }

    @Config(value = "goodwill.store.db.password")
    public String getDBPassword()
    {
        return "";
    }

    @Config(value = "goodwill.store.db.thrift_table.name")
    public String getDBThriftTableName()
    {
        return "thrift_types";
    }
}

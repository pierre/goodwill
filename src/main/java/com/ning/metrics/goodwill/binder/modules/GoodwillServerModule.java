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

package com.ning.metrics.goodwill.binder.modules;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Providers;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.dao.DAOBoneCPAccess;
import com.ning.metrics.goodwill.dao.DAOAccess;
import com.ning.metrics.goodwill.sink.GoodwillSink;
import com.ning.metrics.goodwill.sink.NetezzaSink;
import com.ning.metrics.goodwill.store.CSVFileStore;
import com.ning.metrics.goodwill.store.GoodwillStore;
import com.ning.metrics.goodwill.store.MySQLStore;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.apache.log4j.Logger;
import org.skife.config.ConfigurationObjectFactory;

public class GoodwillServerModule extends ServletModule
{
    private static final Logger log = Logger.getLogger(GoodwillServerModule.class);

    @Override
    protected void configureServlets()
    {
        install(new Module()
        {
            @Override
            public void configure(final Binder binder)
            {
                final GoodwillConfig config = new ConfigurationObjectFactory(System.getProperties()).build(GoodwillConfig.class);
                binder.bind(GoodwillConfig.class).toInstance(config);

                final String storeType = config.getStoreType();
                if (storeType.equals("mysql")) {
                    // Fail early if we can't connect to MySQL
                    binder.bind(DAOAccess.class).to(DAOBoneCPAccess.class).asEagerSingleton();
                    binder.bind(GoodwillStore.class).to(MySQLStore.class).asEagerSingleton();
                    log.info("Enabled MySQL store");
                }
                else if (storeType.equals("csv")) {
                    binder.bind(GoodwillStore.class).to(CSVFileStore.class);
                    log.info("Enabled CSV store");
                }
                else {
                    throw new IllegalStateException("Unknown store type " + storeType);
                }

                final String sinkType = config.getSinkType();
                if (sinkType == null) {
                    binder.bind(GoodwillSink.class).toProvider(Providers.<GoodwillSink>of(null));
                }
                else if (sinkType.equals("netezza")) {
                    binder.bind(GoodwillSink.class).to(NetezzaSink.class);
                    log.info("Enabled Netezza sink");
                }
                else {
                    throw new IllegalStateException("Unknown sink type " + sinkType);
                }
            }
        });

        // TODO: add these filters
        // ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, GZIPContentEncodingFilter.class.getName(),
        // ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, GZIPContentEncodingFilter.class.getName()
        filter("/*").through(GuiceContainer.class, ImmutableMap.of(
            PackagesResourceConfig.PROPERTY_PACKAGES, "com.ning.metrics.goodwill.endpoint"
        ));
    }
}

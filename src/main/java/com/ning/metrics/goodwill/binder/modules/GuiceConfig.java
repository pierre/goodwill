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

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Providers;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.sink.GoodwillSink;
import com.ning.metrics.goodwill.sink.NetezzaSink;
import com.ning.metrics.goodwill.store.CSVFileStore;
import com.ning.metrics.goodwill.store.GoodwillStore;
import com.ning.metrics.goodwill.store.MySQLStore;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.apache.log4j.Logger;
import org.skife.config.ConfigurationObjectFactory;

import java.util.HashMap;
import java.util.Map;

public class GuiceConfig extends GuiceServletContextListener
{
    private static final Logger log = Logger.getLogger(GuiceConfig.class);

    @Override
    protected Injector getInjector()
    {
        /* Scan for Jersey endpoints */
        final Map<String, String> params = new HashMap<String, String>();
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "com.ning.metrics.goodwill.endpoint");

        return Guice.createInjector(
            Stage.PRODUCTION,
            new Module()
            {
                @Override
                public void configure(Binder binder)
                {
                    GoodwillConfig config = new ConfigurationObjectFactory(System.getProperties()).build(GoodwillConfig.class);
                    binder.bind(GoodwillConfig.class).toInstance(config);


                    final String storeType = config.getStoreType();
                    if (storeType.equals("mysql")) {
                        binder.bind(GoodwillStore.class).to(MySQLStore.class);
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
            },
            new ServletModule()
            {
                @Override
                protected void configureServlets()
                {
                    //serve("/*").with(GuiceContainer.class, params);
                    filter("/*").through(GuiceContainer.class, params);
                }
            }
        );
    }
}

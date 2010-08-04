package org.mouraf.goodwill.binder.modules;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.mouraf.goodwill.binder.config.GoodwillConfig;
import org.mouraf.goodwill.store.CSVFileStore;
import org.mouraf.goodwill.store.GoodwillStore;
import org.mouraf.goodwill.store.MySQLStore;
import org.skife.config.ConfigurationObjectFactory;

import java.util.HashMap;
import java.util.Map;

public class GuiceConfig extends GuiceServletContextListener
{
    @Override
    protected Injector getInjector()
    {
        /* Scan for Jersey endpoints */
        final Map<String, String> params = new HashMap<String, String>();
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "org.mouraf.goodwill.endpoint");

        return Guice.createInjector(
            Stage.PRODUCTION,
            new Module()
            {
                @Override
                public void configure(Binder binder)
                {
                    GoodwillConfig config = new ConfigurationObjectFactory(System.getProperties()).build(GoodwillConfig.class);
                    binder.bind(GoodwillConfig.class).toInstance(config);

                    binder.bind(GoodwillStore.class).to(MySQLStore.class);
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

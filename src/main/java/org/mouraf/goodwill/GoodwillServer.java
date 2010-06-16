package org.mouraf.goodwill;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.apache.log4j.Logger;
import org.mouraf.goodwill.endpoint.HttpServer;

import java.util.HashMap;
import java.util.Map;

public class GoodwillServer
{
    private final static Logger log = Logger.getLogger(GoodwillServer.class);
    private static Injector injector;

    public static void main(String... args) throws Exception
    {
        final long startTime = System.currentTimeMillis();

        /* Scan for Jersey endpoints */
        final Map<String, String> params = new HashMap<String, String>();
        params.put(PackagesResourceConfig.PROPERTY_PACKAGES, "org.mouraf.goodwill.endpoint");

        injector = Guice.createInjector(
            new ServletModule()
            {
                @Override
                protected void configureServlets()
                {
                    // Note! It's "*", NOT "/*"
                    serve("*").with(GuiceContainer.class, params);
                }
            }
        );

        /* Start the Jetty endpoint */
        injector.getInstance(HttpServer.class);

        final long secondsToStart = (System.currentTimeMillis() - startTime) / 1000;
        log.info(String.format("Goodwill initialized in %d:%02d", secondsToStart / 60, secondsToStart % 60));
    }

    /**
     * Hack to share the injector with the Jersey GuiceFilter
     *
     * @see org.mouraf.goodwill.binder.modules.JettyListener:getInjector
     */
    public static Injector getInjector()
    {
        return injector;
    }
}
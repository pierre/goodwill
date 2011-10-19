package com.ning.metrics.goodwill.binder.listeners;

import com.ning.jetty.base.modules.ServerModuleBuilder;
import com.ning.jetty.core.listeners.SetupServer;
import com.ning.jetty.utils.healthchecks.DBIHealthCheck;
import com.ning.jetty.utils.log4j.Log4JMBean;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.binder.modules.GoodwillServicesModule;

import javax.servlet.ServletContextEvent;

public class GoodwillGuiceListener extends SetupServer
{
    @Override
    public void contextInitialized(ServletContextEvent event)
    {
        final ServerModuleBuilder builder = new ServerModuleBuilder()
            .addConfig(GoodwillConfig.class)
            .addJMXExport(Log4JMBean.class)
            .setAreciboProfile(System.getProperty("action.arecibo.profile", "ning.jmx:name=MonitoringProfile"))
            .addModule(new GoodwillServicesModule())
            .addResource("com.ning.metrics.goodwill.endpoint");

        guiceModule = builder.build();

        super.contextInitialized(event);
    }
}

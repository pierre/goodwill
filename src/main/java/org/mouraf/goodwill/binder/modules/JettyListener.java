package org.mouraf.goodwill.binder.modules;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import org.mouraf.goodwill.GoodwillServer;

public class JettyListener extends GuiceServletContextListener
{
    @Override
    protected Injector getInjector()
    {
        return GoodwillServer.getInjector();
    }
}

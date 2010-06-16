package org.mouraf.goodwill.binder.config;

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
}

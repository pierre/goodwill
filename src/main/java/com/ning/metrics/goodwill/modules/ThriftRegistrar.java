package com.ning.metrics.goodwill.modules;

import java.io.ByteArrayOutputStream;

public class ThriftRegistrar
{
    private final ByteArrayOutputStream storeInJSON;
    private String actionCoreURL;

    public ThriftRegistrar(ByteArrayOutputStream storeInJSON)
    {
        this.storeInJSON = storeInJSON;
    }

    public ByteArrayOutputStream getStoreInJSON()
    {
        return storeInJSON;
    }

    /**
     * Setter for the actionCoreURL field.
     * The action core is an open-source HDFS front-end, similar to the one provided by the Namenode,
     * but on steroids.
     *
     * @param actionCoreURL URL to the action core instance
     * @link http://github.com/pierre/action-core
     */
    public void setActionCoreURL(String actionCoreURL)
    {
        this.actionCoreURL = actionCoreURL;
    }

    public String getActionCoreURL()
    {
        return actionCoreURL;
    }
}

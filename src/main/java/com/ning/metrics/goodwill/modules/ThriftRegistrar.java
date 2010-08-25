package com.ning.metrics.goodwill.modules;

import org.json.JSONArray;

public class ThriftRegistrar
{
    private final JSONArray storeInJSON;
    private String actionCoreURL;

    public ThriftRegistrar(JSONArray storeInJSON)
    {
        this.storeInJSON = storeInJSON;
    }

    public JSONArray getStoreInJSON()
    {
        return storeInJSON;
    }

    /**
     * Setter for the actionCoreURL field.
     * The action core is an open-source HDFS front-end, similar to the one provided by the Namenode,
     * but on steroids.
     * @link http://github.com/pierre/action-core
     *
     * @param actionCoreURL URL to the action core instance
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

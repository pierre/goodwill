package org.mouraf.goodwill.store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ThriftType
{
    private String name;
    private final HashMap<Integer, ThriftItem> thriftItems = new HashMap<Integer, ThriftItem>();

    public ThriftType(
        String name
    )
    {
        this.name = name;
    }

    public void addThriftItem(Integer position, ThriftItem item)
    {
        thriftItems.put(position, item);
    }

    public String getName()
    {
        return name;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONArray array = new JSONArray();
        for (ThriftItem item : thriftItems.values()) {
            array.put(item.toJSON());
        }

        return new JSONObject()
            .put("name", name)
            .put("schema", array);
    }
}

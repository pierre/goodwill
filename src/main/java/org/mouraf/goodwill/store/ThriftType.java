package org.mouraf.goodwill.store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ThriftType
{
    private String name;
    private final HashMap<Integer, ThriftField> thriftItems = new HashMap<Integer, ThriftField>();
    public static final String EVENT_TYPE_NAME = "name";
    public static final String EVENT_TYPE_SCHEMA = "schema";

    public ThriftType(
        String name
    )
    {
        this.name = name;
    }

    public ThriftType(
        JSONObject eventJson
    ) throws JSONException
    {
        this.name = (String) eventJson.get(ThriftType.EVENT_TYPE_NAME);

        JSONArray array = (JSONArray) eventJson.get(ThriftType.EVENT_TYPE_SCHEMA);
        int i = 0;
        for (i = 0; i < array.length(); i++) {
            JSONObject thriftItemObject = (JSONObject) array.get(i);

            ThriftField thriftField = new ThriftField(thriftItemObject);
            addThriftItem(thriftField.getPosition(), thriftField);
        }
    }

    public void addThriftItem(Integer position, ThriftField thriftField)
    {
        thriftItems.put(position, thriftField);
    }

    public String getName()
    {
        return name;
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONArray array = new JSONArray();
        for (ThriftField thriftField : thriftItems.values()) {
            array.put(thriftField.toJSON());
        }

        return new JSONObject()
            .put(EVENT_TYPE_NAME, name)
            .put(EVENT_TYPE_SCHEMA, array);
    }

    @Override
    public String toString()
    {
        return "ThriftType{" +
            "name='" + name + '\'' +
            ", thriftItems=" + thriftItems +
            '}';
    }
}

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

package org.mouraf.goodwill.store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;

public class ThriftType
{
    private String name;

    private final HashMap<Integer, ThriftField> thriftItems = new HashMap<Integer, ThriftField>();

    public static final String JSON_THRIFT_TYPE_NAME = "name";
    public static final String JSON_THRIFT_TYPE_SCHEMA = "schema";

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
        this(eventJson.getString(ThriftType.JSON_THRIFT_TYPE_NAME));

        JSONArray array = eventJson.getJSONArray(ThriftType.JSON_THRIFT_TYPE_SCHEMA);
        for (int i = 0; i < array.length(); i++) {
            JSONObject thriftItemObject = array.getJSONObject(i);
            ThriftField thriftField = new ThriftField(thriftItemObject);
            addThriftField(thriftField);
        }
    }

    /**
     * Add a field in the Thrift. The code does not enforce sanity w.r.t. field positions.
     *
     * @param thriftField field to add
     */
    public void addThriftField(ThriftField thriftField)
    {
        thriftItems.put(thriftField.getPosition(), thriftField);
    }

    public String getName()
    {
        return name;
    }

    /**
     * Given a position, return the field at that position.
     *
     * @param i position in the Thrift (start with 1)
     * @return the ThriftField object
     */
    public ThriftField getFieldByPosition(int i)
    {
        return thriftItems.get(i);
    }

    public Collection<ThriftField> getThriftItems()
    {
        return thriftItems.values();
    }

    @Override
    public String toString()
    {
        try {
            return toJSON().toString();
        }
        catch (JSONException e) {
            return "ThriftType{" +
                JSON_THRIFT_TYPE_NAME + "='" + name + '\'' +
                ", thriftItems=" + thriftItems +
                '}';
        }
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONArray array = new JSONArray();
        for (ThriftField thriftField : thriftItems.values()) {
            array.put(thriftField.toJSON());
        }

        return new JSONObject()
            .put(JSON_THRIFT_TYPE_NAME, name)
            .put(JSON_THRIFT_TYPE_SCHEMA, array);
    }
}

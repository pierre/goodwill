package org.mouraf.goodwill.store;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.Collection;

public interface GoodwillStore
{
    public Collection<ThriftType> getTypes() throws IOException;

    /**
     * Given a Thrift name, find it in the store
     *
     * @param typeName name of the Thrift to search
     * @return the ThriftType if found, null otherwise
     */
    public ThriftType findByName(String typeName);

    /**
     * Serialize all Thrifts in the store
     *
     * @return JSONArray representation
     * @throws org.json.JSONException
     */
    public JSONArray toJSON() throws JSONException;

    /**
     * Add a new type to the store
     *
     * @param thriftType ThriftType to add
     */
    public void addType(ThriftType thriftType);

    /**
     * Update a type to the store
     *
     * @param thriftType ThriftType to update
     * @return true is success, false otherwise
     */
    public boolean updateType(ThriftType thriftType);
}

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

package com.ning.metrics.goodwill.store;

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

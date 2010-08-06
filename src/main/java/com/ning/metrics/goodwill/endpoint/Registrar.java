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

package com.ning.metrics.goodwill.endpoint;

import com.google.inject.Inject;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.view.Viewable;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.ning.metrics.goodwill.store.GoodwillStore;
import com.ning.metrics.goodwill.store.ThriftType;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("registrar")
public class Registrar
{
    private GoodwillStore store;
    private Logger log = Logger.getLogger(Registrar.class);

    @Inject
    public Registrar(
        GoodwillStore store
    )
    {
        this.store = store;
    }

    /*
     * UI
     */

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAll() throws JSONException
    {
        return new Viewable("/registrar/type.jsp", store.toJSON());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{type}/")
    public Viewable getType(@PathParam("type") String typeName) throws JSONException
    {
        ThriftType typeFound = store.findByName(typeName);

        log.debug(String.format("Found type: %s", typeFound));
        if (typeFound != null) {
            // Return a JSON array (JS code expects it)
            JSONArray array = new JSONArray();
            array.put(typeFound.toJSON());
            return new Viewable("/registrar/type.jsp", array);
        }

        throw new NotFoundException("Type, " + typeName + ", is not found");
    }

    /*
     * REST API
     */

    @GET
    @Produces("application/json")
    public Response getAllJson() throws IOException, JSONException
    {
        return Response.ok(store.toJSON().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{type}/")
    public Response getTypeJson(@PathParam("type") String typeName) throws IOException, JSONException
    {
        if (typeName == null) {
            return Response.ok(store.toJSON().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        else {
            ThriftType typeFound = store.findByName(typeName);
            if (typeFound != null) {
                return Response.ok(typeFound.toJSON().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Consumes("application/json")
    public Response post(
        String jsonThriftTypeString
    )
    {
        try {
            JSONObject eventJSON = new JSONObject(jsonThriftTypeString);
            ThriftType thriftType = new ThriftType(eventJSON);
            store.addType(thriftType);
            log.info(String.format("Created new ThriftType <%s> from JSON <%s>", thriftType.toString(), jsonThriftTypeString));
        }
        catch (JSONException e) {
            log.warn(String.format("Malformatted JSON: %s (%s)", jsonThriftTypeString, e));
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Consumes("application/json")
    public Response put(
        String jsonThriftTypeString
    )
    {
        try {
            JSONObject eventJSON = new JSONObject(jsonThriftTypeString);
            ThriftType thriftType = new ThriftType(eventJSON);
            store.updateType(thriftType);
            log.info(String.format("Updated ThriftType <%s> from JSON <%s>", thriftType.toString(), jsonThriftTypeString));
        }
        catch (JSONException e) {
            log.warn(String.format("Malformatted JSON: %s", jsonThriftTypeString));
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.status(Response.Status.ACCEPTED).build();
    }
}
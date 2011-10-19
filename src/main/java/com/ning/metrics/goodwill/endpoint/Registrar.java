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
import com.ning.metrics.goodwill.access.GoodwillSchema;
import com.ning.metrics.goodwill.binder.config.GoodwillConfig;
import com.ning.metrics.goodwill.modules.ThriftRegistrar;
import com.ning.metrics.goodwill.sink.GoodwillSink;
import com.ning.metrics.goodwill.store.GoodwillStore;
import com.sun.jersey.api.view.Viewable;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
    private Logger log = Logger.getLogger(Registrar.class);

    private GoodwillStore store;
    private final GoodwillSink sink;
    private final GoodwillConfig config;

    @Inject
    public Registrar(
        GoodwillConfig config,
        GoodwillStore store,
        @Nullable GoodwillSink sink
    )
    {
        this.config = config;
        this.store = store;
        this.sink = sink;

        if (sink != null) {
            this.store.setSink(sink);
        }
    }

    /*
     * UI
     */

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable getAll() throws IOException
    {
        ThriftRegistrar registrar = new ThriftRegistrar(store.toJSON());
        registrar.setActionCoreURL(config.getActionCoreURL());

        return new Viewable("/registrar/type.jsp", registrar);
    }

    /*
     * REST API
     */

    @GET
    @Produces("application/json")
    public Response getAllJson() throws IOException
    {
        return Response.ok(store.toJSON().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @GET
    @Produces("application/json")
    @Path("/{type}/")
    public Response getTypeJson(@PathParam("type") String typeName) throws IOException
    {
        if (typeName == null) {
            return Response.ok(store.toJSON().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        else {
            GoodwillSchema typeFound = store.findByName(typeName);
            if (typeFound != null) {
                return Response.ok(typeFound.toJSON().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/{type}/")
    public Response deleteType(@PathParam("type") String typeName) throws IOException
    {
        if (!config.allowDeleteEvent()) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        if (typeName == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        else {
            GoodwillSchema typeFound = store.findByName(typeName);
            if (typeFound != null) {
                if (store.deleteType(typeFound)) {
                    return Response.noContent().build();
                }
                else {
                    return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
                }
            }
            else {
                // Don't! The condition is not necessarily permanent!
                //return Response.status(Response.Status.GONE)
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
    }

    @POST
    @Consumes("application/json")
    public Response post(
        String jsonThriftTypeString
    )
    {
        try {
            GoodwillSchema schema = GoodwillSchema.decode(jsonThriftTypeString);
            store.addType(schema);
            log.info(String.format("Created new ThriftType <%s> from JSON <%s>", schema.toString(), jsonThriftTypeString));
        }
        catch (IOException e) {
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
            GoodwillSchema thriftType = GoodwillSchema.decode(jsonThriftTypeString);
            store.updateType(thriftType);
            log.info(String.format("Updated ThriftType <%s> from JSON <%s>", thriftType.toString(), jsonThriftTypeString));
        }
        catch (IOException e) {
            log.warn(String.format("Malformatted JSON: %s", jsonThriftTypeString));
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        return Response.status(Response.Status.ACCEPTED).build();
    }
}

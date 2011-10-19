package com.ning.metrics.goodwill.endpoint;

import com.google.inject.Inject;
import com.ning.metrics.goodwill.access.GoodwillSchema;
import com.ning.metrics.goodwill.sink.GoodwillSink;
import com.ning.metrics.goodwill.store.GoodwillStore;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("sink")
public class Sink
{
    private Logger log = Logger.getLogger(Sink.class);

    private GoodwillStore store;
    private final GoodwillSink sink;

    @Inject
    public Sink(
        GoodwillStore store,
        @Nullable GoodwillSink sink
    )
    {
        this.store = store;
        this.sink = sink;
    }

    @POST
    @Path("/{type}/")
    @Produces(MediaType.TEXT_PLAIN)
    public Response addTypeToSink(@PathParam("type") String typeName) throws Exception
    {
        GoodwillSchema typeFound = store.findByName(typeName);

        log.debug(String.format("Found type: %s", typeFound));
        if (typeFound != null) {
            if (sink.addType(typeFound)) {
                return Response.status(Response.Status.CREATED).build();
            }
            else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

        return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
    }
}
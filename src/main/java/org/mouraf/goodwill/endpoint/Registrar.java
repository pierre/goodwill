package org.mouraf.goodwill.endpoint;

import com.google.inject.Inject;
import org.json.JSONArray;
import org.json.JSONException;
import org.mouraf.goodwill.store.GoodwillStore;
import org.mouraf.goodwill.store.ThriftType;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

@Path("/registrar")
public class Registrar
{
    private GoodwillStore store;

    @Inject
    public Registrar(
        GoodwillStore store
    )
    {
        this.store = store;
    }

    @GET
    public Response get(
        @QueryParam("type") String typeName
    ) throws IOException, JSONException
    {
        if (typeName == null) {
            JSONArray array = new JSONArray();
            for (ThriftType type : store.getTypes()) {
                array.put(type.toJSON());
            }
            return Response.ok(array.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        }
        else {
            for (ThriftType thriftType : store.getTypes()) {
                if (thriftType.getName().equals(typeName)) {
                    return Response.ok(thriftType.toJSON().toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
                }
            }
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}


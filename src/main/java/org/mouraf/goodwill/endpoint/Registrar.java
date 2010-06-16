package org.mouraf.goodwill.endpoint;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/registrar")
public class Registrar
{
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String get(
        @QueryParam("type") String typeName
    )
    {
        return typeName;
    }
}


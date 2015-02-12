package com.github.cambridgeAlphaTeam;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import com.github.cambridgeAlphaTeam.OscSender;
import com.github.cambridgeAlphaTeam.OscException;

@Path("/osc")
public class OscInterface {

    private static OscSender sender;

    public OscInterface() throws OscException {
        sender = new OscSender();
    }

    @GET
    @Path("/hw")
    public Response get() {
        return Response.ok().entity("Hello, world").build();
    }

    @POST
    @Path("/run")
    @Consumes("text/plain")
    public Response run(String codeToRun) {
        try {
            sender.sendCode(codeToRun);
        } catch (OscException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("/stop")
    public Response stop() {
        try {
            sender.stopAll();
        } catch (OscException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
        return Response.ok().build();
    }

}

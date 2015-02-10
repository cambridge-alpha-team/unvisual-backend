package com.github.cambridgeAlphaTeam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/test")
public class Test
{ @GET
  @Path("/get")
  public Response get()
  { return Response.ok().entity("Hello, world").build();
  }
}

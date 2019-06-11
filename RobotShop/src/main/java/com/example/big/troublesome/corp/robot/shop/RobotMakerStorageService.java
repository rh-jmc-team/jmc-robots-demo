package com.example.big.troublesome.corp.robot.shop;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/storage")
@RegisterRestClient
public interface RobotMakerStorageService {

    @GET
    @Path("/")
    Response isAvailable();

    @POST
    @Path("/buy")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    Response buy(String message);

}

package com.example.big.troublesome.corp.robot.controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/production")
@RegisterRestClient
public interface RobotMakerControlService {

    @GET
    @Path("/")
    Response isAvailable();

    @GET
    @Path("/factoryId")
    @Produces(MediaType.TEXT_PLAIN)
    String getFactoryId();
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/threshold")
    String getProductionThreshold();
    
    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/threshold")
    Response updateProductionThreshold(String message);

}

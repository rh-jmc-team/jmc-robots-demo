package com.example.big.troublesome.corp.robot.maker2k;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.big.troublesome.corp.robot.commons.Message;
import com.example.big.troublesome.corp.robot.commons.Protocol;

@Path("/production")
public class ControlResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ControlResource.class);
    
    @Inject
    RobotService service;
    
    @GET
    @Path("/")
    public Response isAvailable() {
        return Response.ok().build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/factoryId")
    public String getFactoryId() {
        Message reply = new Message();
        reply.protocol = Protocol.FACTORY_ID;
        try {
            reply.payload = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOGGER.warn("Couldn't determine IP address, using fallback", e);
            reply.payload = "robotmaker";
        }
        return reply.toString();
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/threshold")
    public String getProductionThreshold() {
        Message reply = new Message();
        reply.protocol = Protocol.PRODUCTION_THRESHOLD;
        reply.payload = String.valueOf(service.getProductionThreshold());
        return reply.toString();
    }

    @PUT
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/threshold")
    public Response updateProductionThreshold(String message) {
        Message reply = new Message();
        reply.protocol = Protocol.PRODUCTION_THRESHOLD_UPDATE;
        try {
            Message request = Message.create(message);
            if (!Protocol.PRODUCTION_THRESHOLD_UPDATE.equals(request.protocol)) {
                return Response.status(Status.BAD_REQUEST).entity(reply.toString()).build();
            }
            long parsed = Long.parseLong(request.payload);
            LOGGER.info("setting delay to new value: " + parsed);
            service.setProductionThreshold(parsed);
            return Response.ok().entity(reply.toString()).build();
        } catch (IllegalArgumentException e) {
            LOGGER.error("could not update production threshold", e);
            return Response.status(Status.BAD_REQUEST).entity(reply.toString()).build();
        }
    }
    
}

package com.example.big.troublesome.corp.robot.maker2k;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.big.troublesome.corp.robot.commons.Message;
import com.example.big.troublesome.corp.robot.commons.Protocol;

@Path("/storage")
public class StorageResource {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StorageResource.class);
    
    @Inject
    RobotService service;

    @GET
    @Path("/")
    public Response isAvailable() {
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/buy")
    public Response buy(String message) {
        Message reply = Message.EMPTY;
        try {
            Message request = Message.create(message);
            if (Protocol.BUY.equals(request.protocol)) {
                int available = service.numRobots();
                int requested = Integer.valueOf(request.payload).intValue();
                reply = new Message();
                reply.protocol = Protocol.SOLD;
                reply.payload = "" + requested;

                if (available < requested) {
                    reply.protocol = Protocol.SOLD_DELAY;
                }

                LogWhatsUpp.logValues(available, requested);

                while (requested-- > 0) {
                    service.takeRobot();
                }
            }
            return Response.ok().entity(reply.toString()).build();
        } catch (IllegalArgumentException e) {
            LOGGER.error("could not process buy request", e);
            return Response.status(Status.BAD_REQUEST).entity(reply.toString()).build();
        }
    }
}

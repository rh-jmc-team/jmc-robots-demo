package com.example.big.troublesome.corp.robot.maker2k;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.jfr.Event;
import jdk.jfr.Label;

public class LogWhatsUpp {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogWhatsUpp.class);

    @Label("RobotEvent_Storage_Request")
    static final class RobotEventStorageRequested extends Event {
        @Label("Requested")
        long requested;
        
        @Label("Available")
        long available;
    }
    
    public static void logValues(long available, long requested) {
        if (available < requested) {
            LOGGER.info("can't fullfill request, will delay, ask: " + requested + " available: " + available);
        } else {
            LOGGER.info("current ask: " + requested + " available: " + available);
        }
        
        RobotEventStorageRequested jfrEvent = new RobotEventStorageRequested();
        jfrEvent.available = available;
        jfrEvent.requested = requested;
        jfrEvent.commit();
    }
}

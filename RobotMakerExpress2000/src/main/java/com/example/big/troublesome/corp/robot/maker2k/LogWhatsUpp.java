package com.example.big.troublesome.corp.robot.maker2k;

import jdk.jfr.Event;
import jdk.jfr.Label;

public class LogWhatsUpp {
    
    @Label("RobotEvent_Storage_Request")
    static final class RobotEventStorageRequested extends Event {
        @Label("Requested")
        long requested;
        
        @Label("Available")
        long available;
    }
    
    public static void logValues(long available, long requested) {
        if (available < requested) {
            System.err.println("can't fullfill request, will delay, ask: " + requested + " available: " + available);
        } else {
            System.err.println("current ask: " + requested + " available: " + available);
        }
        
        RobotEventStorageRequested jfrEvent = new RobotEventStorageRequested();
        jfrEvent.available = available;
        jfrEvent.requested = requested;
        jfrEvent.commit();
    }
}

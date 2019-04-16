package com.example.big.troublesome.corp.robot.maker2k;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jdk.jfr.Event;
import jdk.jfr.Label;

public class RobotStorage {
    
    @Label("RobotEvent_Storage_IN")
    static final class RobotEventStorageIn extends Event {
        @Label("Quantity Left")
        long quantity;
    }
    
    @Label("RobotEvent_Storage_OUT")
    static final class RobotEventStorageOut extends Event {
        @Label("Quantity Left")
        long quantity;
    }
    
    private static BlockingQueue<Robot> robots = new LinkedBlockingQueue<>();

    public void add(Robot robot) {
        
        robots.add(robot);
        
        RobotEventStorageIn in = new RobotEventStorageIn();
        in.quantity = robots.size();
        in.commit();
    }

    public Robot take() {

        try {
            return robots.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            RobotEventStorageOut out = new RobotEventStorageOut();
            out.quantity = robots.size();
            out.commit();
        }
        
        return null;
    }

    public int size() {
        return robots.size();
    }
}

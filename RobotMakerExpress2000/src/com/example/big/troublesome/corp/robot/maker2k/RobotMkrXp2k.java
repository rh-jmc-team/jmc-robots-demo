package com.example.big.troublesome.corp.robot.maker2k;

import java.util.concurrent.atomic.AtomicLong;

import com.example.big.troublesome.corp.robot.commons.Handler;
import com.example.big.troublesome.corp.robot.commons.Message;
import com.example.big.troublesome.corp.robot.commons.Protocol;
import com.example.big.troublesome.corp.robot.commons.ServerSocketHandler;
import com.example.big.troublesome.corp.robot.commons.ServicePorts;

public class RobotMkrXp2k {

    private static RobotStorage robots = new RobotStorage();
    private static AtomicLong productionThreshold = new AtomicLong(100);

    public static void main(String[] args) throws Exception {
        System.err.println("Starting making robots!");
        
        Thread productionServer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Robot robot = new Robot();
                    robots.add(robot);
                    try {
                        Thread.sleep(productionThreshold.longValue());
                        
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        productionServer.setName("productionServer");
        productionServer.setDaemon(false);
        productionServer.start();
        
        Thread controlServer = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHandler.connect(ServicePorts.PRODUCTION_LINE,
                                            new Handler()
                {
                    @Override
                    public Message handle(Message message) {
                        
                        Message reply = Message.EMPTY;
                        
                        if (Protocol.FACTORY_ID.equals(message.protocol)) {
                            ProcessHandle me = ProcessHandle.current();
                            
                            reply = new Message();
                            reply.protocol = Protocol.FACTORY_ID;
                            reply.payload = "" + me.pid();
                        } else if (Protocol.PRODUCTION_THRESHOLD.equals(message.protocol)) {
                            reply = new Message();
                            reply.protocol = Protocol.PRODUCTION_THRESHOLD;
                            reply.payload = "" + productionThreshold.get();
                            
                        } else if (Protocol.PRODUCTION_THRESHOLD_UPDATE.equals(message.protocol)) {
                            long requested = Long.valueOf(message.payload).longValue();
                            System.err.println("setting delay to new value: " + requested);
                            productionThreshold.getAndSet(requested);
                            
                            reply = new Message();
                            reply.protocol = Protocol.PRODUCTION_THRESHOLD_UPDATE;
                        }
                        
                        return reply;
                    }
                });
            }
        });
        controlServer.setName("controlServer");
        controlServer.setDaemon(true);
        controlServer.start();
        
        Thread storageServer = new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocketHandler.connect(ServicePorts.STORAGE,
                                            new Handler()
                {
                    @Override
                    public Message handle(Message message) {
                        Message reply = Message.EMPTY;
                        if (Protocol.BUY.equals(message.protocol)) {
                            int available = robots.size();
                            int requested = Integer.valueOf(message.payload).intValue();
                            reply = new Message();
                            reply.protocol = Protocol.SOLD;
                            reply.payload = "" + requested;
                            
                            if (available < requested) {
                                reply.protocol = Protocol.SOLD_DELAY;
                            }
                            
                            LogWhatsUpp.logValues(available, requested);
                            
                            while (requested-- > 0) {
                                robots.take();
                            }
                        }
                        return reply;
                    }
                });
            }
        });
        storageServer.setName("storageServer");
        storageServer.setDaemon(true);
        storageServer.start();
    }
}

package com.example.big.troublesome.corp.robot.shop;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.example.big.troublesome.corp.robot.commons.ClientHandler;
import com.example.big.troublesome.corp.robot.commons.ClientSocketHandler;
import com.example.big.troublesome.corp.robot.commons.Message;
import com.example.big.troublesome.corp.robot.commons.Protocol;
import com.example.big.troublesome.corp.robot.commons.ServicePorts;

public class RobotShop {
    
    private static Random random = new Random(0);
    private static AtomicInteger orders = new AtomicInteger();
    
    public static void main(String[] args) {
        System.err.println("Starting buying robots!");
        
        Thread shop = new Thread(new Runnable() {            
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                        if (random.nextBoolean()) {
                            orders.incrementAndGet();
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        shop.setName("shop");
        shop.start();
        
        Thread factory = new Thread(new Runnable() {            
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(500);
                        int backlog = orders.getAndSet(0);
                        if (backlog > 0) {
                            Message message = new Message();
                            message.protocol = Protocol.BUY;
                            message.payload = "" + backlog;
                            
                            ClientSocketHandler.queue(message);
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        factory.start();
        
        Thread controlServer = new Thread(new Runnable() {
            @Override
            public void run() {
                ClientSocketHandler.connect("robotmaker", ServicePorts.STORAGE, new ClientHandler() {
                    @Override
                    public void handle(Message message) {
                        System.err.println("acknowledge: " + message);
                    }
                });
            }
        });
        controlServer.setName("controlServer");
        controlServer.setDaemon(true);
        controlServer.start();
    }  
}

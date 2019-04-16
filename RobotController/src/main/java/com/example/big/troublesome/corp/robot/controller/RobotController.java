package com.example.big.troublesome.corp.robot.controller;

import java.io.File;
import java.util.concurrent.CountDownLatch;

import com.example.big.troublesome.corp.robot.commons.ClientHandler;
import com.example.big.troublesome.corp.robot.commons.ClientSocketHandler;
import com.example.big.troublesome.corp.robot.commons.Message;
import com.example.big.troublesome.corp.robot.commons.Protocol;
import com.example.big.troublesome.corp.robot.commons.ServicePorts;

public class RobotController {

    private static final JFRManager jfr = new JFRManager();
    private static final MissionControl jmc = new MissionControl();

    private static volatile long currentThreshold = 100;

    private static final long DELTA = 100;

    private static final CountDownLatch latch = new CountDownLatch(2);
    public static void main(String[] args) {
        System.err.println("Start controlling robots!");
        
        Thread controller = new Thread(new Runnable() {
            @Override
            public void run() {
                ClientSocketHandler.connect(ServicePorts.PRODUCTION_LINE, new ClientHandler() {
                    @Override
                    public void handle(Message message) {
                        if (Protocol.FACTORY_ID.equals(message.protocol)) {
                            long pid = Long.valueOf(message.payload);
                            jfr.setPid(pid);
                            latch.countDown();
                            
                        } else if (Protocol.PRODUCTION_THRESHOLD.equals(message.protocol)) {
                            currentThreshold = Long.valueOf(message.payload);
                            latch.countDown();
                        }
                    }
                });
            }
        });
        controller.setName("controller");
        controller.start();
        
        Thread starter = new Thread(new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                message.protocol = Protocol.FACTORY_ID;
                ClientSocketHandler.queue(message);
                
                message = new Message();
                message.protocol = Protocol.PRODUCTION_THRESHOLD;
                ClientSocketHandler.queue(message);
            }
        });
        starter.setName("starter");
        starter.setDaemon(true);
        starter.start();
        
        Thread jfrChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                
                try {
                    latch.await();

                    jfr.connect();
                    
                    while(true) {
                        Message message = new Message();
                        message.protocol = Protocol.PRODUCTION_THRESHOLD;
                        ClientSocketHandler.queue(message);

                        // the interesting bits
                        jfr.startRercording();
                        Thread.sleep(5_000);
                        File recording = jfr.stopRecording();
                        
                        double ratio = jmc.analyse(recording);
                        System.err.println("ratio: " + ratio);
                       
                        long delta = DELTA;
                        if (ratio <= 2) {
                            delta = -delta;
                        } else if (delta > 2 && delta <= 10) {
                            delta = 0;
                        }
                        
                        long _currentThreshold = currentThreshold + delta;
                        if (_currentThreshold < DELTA) {
                            _currentThreshold = DELTA;
                        }
                        
                        if (_currentThreshold < 15_000) {
                            System.err.println("setting threshold to: " + _currentThreshold);
                            message = new Message();
                            message.protocol = Protocol.PRODUCTION_THRESHOLD_UPDATE;
                            message.payload = "" + _currentThreshold;
                            ClientSocketHandler.queue(message);   
                        }
                    }
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        jfrChecker.setName("jfrChecker");
        jfrChecker.setDaemon(true);
        jfrChecker.start();
    }
}

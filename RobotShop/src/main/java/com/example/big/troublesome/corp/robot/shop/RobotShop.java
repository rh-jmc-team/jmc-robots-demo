package com.example.big.troublesome.corp.robot.shop;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Response.StatusType;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.big.troublesome.corp.robot.commons.Message;
import com.example.big.troublesome.corp.robot.commons.Protocol;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RobotShop {

    private static final Logger LOGGER = LoggerFactory.getLogger(RobotShop.class);

    private final Random random;
    private final AtomicInteger orders;
    private final Thread shopThread;
    private final Thread factoryThread;
    private final Thread controlThread;
    private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();

    @Inject
    @RestClient
    RobotMakerStorageService service;

    public RobotShop() {
        random = new Random(0);
        orders = new AtomicInteger();

        shopThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        // Wait for service to become available
                        waitForService();

                        Thread.sleep(500);
                        if (random.nextBoolean()) {
                            orders.incrementAndGet();
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        shopThread.setName("shop");

        factoryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        Thread.sleep(500);
                        int backlog = orders.getAndSet(0);
                        if (backlog > 0) {
                            Message message = new Message();
                            message.protocol = Protocol.BUY;
                            message.payload = "" + backlog;
                            
                            messages.add(message);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        factoryThread.setName("factory");

        controlThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Message message = messages.take();
                        Response response = service.buy(message.toString());
                        StatusType type = response.getStatusInfo();
                        if (Status.OK.getStatusCode() != type.getStatusCode()) {
                            LOGGER.error("bad response from server: HTTP " + type.getStatusCode()
                            + " - " + type.getReasonPhrase());
                        }
                        LOGGER.info("acknowledge: " + message);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        controlThread.setName("controlServer");
        controlThread.setDaemon(true);
    }

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Starting buying robots!");
        shopThread.start();
        factoryThread.start();
        controlThread.start();
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("Stopping buying robots!");
        stopThread(controlThread);
        stopThread(factoryThread);
        stopThread(shopThread);
    }

    private void stopThread(Thread t) {
        t.interrupt();
        try {
            t.join();
        } catch (InterruptedException e) {
            LOGGER.warn("interrupted while waiting for " + t.getName() + " to stop");
        }
    }

    private void waitForService() throws InterruptedException {
        boolean started = false;
        while (!started) {
            try {
                started = (service.isAvailable().getStatus() == Status.OK.getStatusCode());
            } catch (Exception e) {
                LOGGER.info("Tried connecting to RobotMaker: " + e.getMessage());
            }
            if (!started) {
                LOGGER.info("Waiting for RobotMaker service to become available");
                Thread.sleep(500);
            }
        }
    }

}

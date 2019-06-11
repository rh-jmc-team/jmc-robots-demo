package com.example.big.troublesome.corp.robot.maker2k;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class RobotMkrXp2k {

    private static final Logger LOGGER = LoggerFactory.getLogger(RobotMkrXp2k.class);

    private final Thread productionServer;

    @Inject
    RobotService service;

    RobotMkrXp2k() {
        productionServer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    Robot robot = new Robot();
                    service.addRobot(robot);
                    try {
                        service.waitProductionThreshold();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
        productionServer.setName("productionServer");
        productionServer.setDaemon(false);
    }

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Starting making robots!");
        productionServer.start();
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("Stopping making robots!");
        productionServer.interrupt();
        try {
            productionServer.join();
        } catch (InterruptedException e) {
            LOGGER.warn("interrupted while waiting for production to stop");
        }
    }

}

package com.example.big.troublesome.corp.robot.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
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
public class RobotController {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RobotController.class);

    private static final JFRManager jfr = new JFRManager();
    private static final MissionControl jmc = new MissionControl();

    private static volatile long currentThreshold = 100;

    private static final long DELTA = 100;

    @Inject
    @RestClient
    RobotMakerControlService service;
    
    private final Thread jfrChecker;

    public RobotController() {
        jfrChecker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Wait for service to become available
                    waitForService();

                    // Get remote JMX URL and connect
                    String response = service.getFactoryId();
                    String factoryId = safeGetPayload(response, Protocol.FACTORY_ID);
                    jfr.setHost(factoryId);
                    jfr.connect();
                    
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            String thresholdResp = service.getProductionThreshold();
                            String thresholdStr = safeGetPayload(thresholdResp, Protocol.PRODUCTION_THRESHOLD);
                            currentThreshold = Long.valueOf(thresholdStr);

                            analyzeRecording();
                        } catch (IOException e) {
                            LOGGER.error("Error reading recording", e);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    LOGGER.error("Failed to communicate with factory", e);
                }
            }
        });
        jfrChecker.setName("jfrChecker");
        jfrChecker.setDaemon(true);
    }
    
    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("Starting controlling robots!");
        jfrChecker.start();
    }

    private String safeGetPayload(String response, Protocol expectedProtocol) throws IOException {
        Message message = Message.create(response);
        if (!expectedProtocol.equals(message.protocol)) {
            throw new IOException("Expected " + expectedProtocol + " response from server, got: " + message.protocol);
        }
        return message.payload;
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOGGER.info("Stopping controlling robots!");
        jfrChecker.interrupt();
        try {
            jfrChecker.join();
        } catch (InterruptedException e) {
            LOGGER.warn("interrupted while waiting for " + jfrChecker.getName() + " to stop");
        }
    }

    private void analyzeRecording() throws InterruptedException, IOException {
        // the interesting bits
        jfr.startRercording();
        Thread.sleep(5_000);
        try (InputStream stream = jfr.stopRecording()) {
            double ratio = jmc.analyse(stream);
            LOGGER.info("ratio: " + ratio);

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
                LOGGER.info("setting threshold to: " + _currentThreshold);
                Message message = new Message();
                message.protocol = Protocol.PRODUCTION_THRESHOLD_UPDATE;
                message.payload = "" + _currentThreshold;
                Response response = service.updateProductionThreshold(message.toString());
                StatusType type = response.getStatusInfo();
                if (Status.OK.getStatusCode() != type.getStatusCode()) {
                    LOGGER.error("bad response from server: HTTP " + type.getStatusCode()
                    + " - " + type.getReasonPhrase());
                }
            }
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

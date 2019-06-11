package com.example.big.troublesome.corp.robot.controller;

import static org.openjdk.jmc.common.unit.UnitLookup.NUMBER;

import java.io.InputStream;

import org.openjdk.jmc.common.item.Aggregators;
import org.openjdk.jmc.common.item.Attribute;
import org.openjdk.jmc.common.item.IAttribute;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.item.ItemFilters;
import org.openjdk.jmc.common.unit.IQuantity;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MissionControl {
    private static final Logger LOGGER = LoggerFactory.getLogger(MissionControl.class);
    private static final String EVENT_NAME = "com.example.big.troublesome.corp.robot.maker2k.LogWhatsUpp$RobotEventStorageRequested";
    
    public static final IAttribute<IQuantity> REQUESTED =
            Attribute.attr("requested", EVENT_NAME, NUMBER);
    public static final IAttribute<IQuantity> AVAILABLE =
            Attribute.attr("available", EVENT_NAME, NUMBER);

    private double getAverage(IAttribute<IQuantity> attribute, IItemCollection events) {
        IQuantity aggregate = events.getAggregate(Aggregators.avg(attribute));
        return aggregate.doubleValue();
    }
    
    public double analyse(InputStream recording) {
        if (recording == null) {
            LOGGER.warn("Recording to analyze was null");
            return 0;
        }
        
        try {
            IItemCollection events = JfrLoaderToolkit.loadEvents(recording).apply(ItemFilters.type(EVENT_NAME));
            if (!events.hasItems()) {
                LOGGER.warn("No matching events in recording");
                return 0;
            }
            
            double requested = getAverage(REQUESTED, events);
            double available = getAverage(AVAILABLE, events);
            
            if (requested <= 0) {
                requested = 1;
            }
            
            LOGGER.info("requested: " + requested + ", available: " + available);
            return (available/requested);
            
        } catch (Exception e) {
            LOGGER.error("Failed to analyze recording", e);
        }
        
        return 0;
    }
}

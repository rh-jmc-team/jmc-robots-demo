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

public class MissionControl {
    private static final String EVENT_NAME = "com.example.big.troublesome.corp.robot.maker2k.LogWhatsUpp$RobotEventStorageRequested";
    
    public static final IAttribute<IQuantity> REQUESTED =
            Attribute.attr("requested", EVENT_NAME, NUMBER);
    public static final IAttribute<IQuantity> AVAILABLE =
            Attribute.attr("available", EVENT_NAME, NUMBER);

    private double getAverage(IAttribute<IQuantity> attribute, IItemCollection events) {
        IQuantity aggregate = events.apply(ItemFilters.type(EVENT_NAME)).getAggregate(Aggregators.avg(attribute));
        return aggregate.doubleValue();
    }
    
    public double analyse(InputStream recording) {
        if (recording == null) {
            System.err.println("Recording to analyze was null");
            return 0;
        }
        
        try {
            IItemCollection events = JfrLoaderToolkit.loadEvents(recording);
            if (!events.hasItems()) {
                System.err.println("No events in recording");
                return 0;
            }
            
            double requested = getAverage(REQUESTED, events);
            double available = getAverage(AVAILABLE, events);
            
            if (requested <= 0) {
                requested = 1;
            }
            
            System.err.println("requested: " + requested + ", available: " + available);
            return (available/requested);
            
        } catch (Exception ignore) {}
        
        return 0;
    }
}

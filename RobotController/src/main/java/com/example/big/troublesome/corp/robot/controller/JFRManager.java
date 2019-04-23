package com.example.big.troublesome.corp.robot.controller;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import jdk.management.jfr.FlightRecorderMXBean;

public class JFRManager {

    private long pid;
    private FlightRecorderMXBean jfr;
    private long id;
    
    public void setPid(long pid) {
        this.pid = pid;
    }
    
    public void connect() {
        try {
            System.err.println(pid);

            JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://robotmaker:9091/jmxrmi");
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
            MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();

            ObjectName jfrBeanName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
            jfr = JMX.newMXBeanProxy(mBeanServer, jfrBeanName, FlightRecorderMXBean.class);
            
        } catch (Exception e) {
            System.err.println("Could not initialize JFR");
            e.printStackTrace();
        }
    }
    
    public void startRercording() {
        if (jfr == null) {
            return;
        }
        id = jfr.newRecording();
        jfr.startRecording(id);
    }

    public InputStream stopRecording() {
        if (jfr == null) {
            return null;
        }
        
        try {
            jfr.stopRecording(id);
            
            long streamId = jfr.openStream(id, null);
            byte[] chunks = new byte[0];
            while (true) {
                byte[] chunk = jfr.readStream(streamId);
                if (chunk == null) {
                    break;
                }
                chunks = concat(chunks, chunk);
            }
            jfr.closeRecording(id);
            return new ByteArrayInputStream(chunks);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static byte[] concat(byte[] a, byte[] b) {
        byte[] out = new byte[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

}

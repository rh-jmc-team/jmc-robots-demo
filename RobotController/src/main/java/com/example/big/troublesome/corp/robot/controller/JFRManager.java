package com.example.big.troublesome.corp.robot.controller;

import java.io.IOException;
import java.io.InputStream;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.management.jfr.FlightRecorderMXBean;

public class JFRManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(JFRManager.class);

    private String host;
    private FlightRecorderMXBean jfr;
    private long id;
    
    public void setHost(String host) {
        this.host = host;
    }
    
    public void connect() {
        try {
            LOGGER.info(host);

            JMXServiceURL serviceURL = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":9091/jmxrmi");
            JMXConnector connector = JMXConnectorFactory.connect(serviceURL);
            MBeanServerConnection mBeanServer = connector.getMBeanServerConnection();

            ObjectName jfrBeanName = new ObjectName("jdk.management.jfr:type=FlightRecorder");
            jfr = JMX.newMXBeanProxy(mBeanServer, jfrBeanName, FlightRecorderMXBean.class);
            
        } catch (Exception e) {
            LOGGER.error("Could not initialize JFR", e);
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
        
        jfr.stopRecording(id);
        try {
            return new FlightRecordingInputStream(id, jfr.openStream(id, null), jfr);
        } catch (IOException e) {
            LOGGER.error("Failed to open recording", e);
            return null;
        }
    }

    static class FlightRecordingInputStream extends InputStream {

        private static final int EOF = -1;

        private final long recordingId;
        private final long streamId;
        private final FlightRecorderMXBean jfr;
        private byte[] buf = null;
        private int pos = EOF;

        FlightRecordingInputStream(long recordingId, long streamId, FlightRecorderMXBean jfr) {
            this.recordingId = recordingId;
            this.streamId = streamId;
            this.jfr = jfr;
        }

        private void readChunk() throws IOException {
            if (pos != EOF && pos < buf.length) {
                return;
            }
            buf = jfr.readStream(streamId);
            pos = 0;
        }

        @Override
        public int available() throws IOException {
            if (buf == null) {
                return 0;
            }
            return Math.max(0, buf.length - pos);
        }

        @Override
        public int read() throws IOException {
            readChunk();
            if (buf == null) {
                return EOF;
            }
            return buf[pos++];
        }

        @Override
        public int read(byte[] b) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (b.length == 0) {
                return 0;
            }
            readChunk();
            int len = Math.min(b.length, buf.length - pos);
            if (len == 0) {
                return 0;
            }
            if (buf == null) {
                return EOF;
            }
            System.arraycopy(buf, 0, b, 0, len);
            pos += len;
            return len;
        }

        @Override
        public int read(byte[] b, int off, int reqLen) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            }
            if (b.length == 0) {
                return 0;
            }
            readChunk();
            if (buf == null) {
                return EOF;
            }
            int len = Math.min(Math.min(b.length, reqLen), buf.length - pos);
            if (len == 0) {
                return 0;
            }
            System.arraycopy(buf, pos, b, off, len);
            pos += len;
            return len;
        }

        @Override
        public byte[] readAllBytes() throws IOException {
            byte[] out = new byte[0];
            pos = 0;
            while (true) {
                byte[] chunk = jfr.readStream(streamId);
                if (chunk == null) {
                    break;
                }
                if (out.length == 0) {
                    out = chunk;
                } else {
                    out = concat(out, chunk);
                }
            }
            return out;
        }

        @Override
        public void close() throws IOException {
            jfr.closeStream(streamId);
            jfr.closeRecording(recordingId);
        }

        @Override
        public boolean markSupported() {
            return false;
        }

        private static byte[] concat(byte[] a, byte[] b) {
            byte[] out = new byte[a.length + b.length];
            System.arraycopy(a, 0, out, 0, a.length);
            System.arraycopy(b, 0, out, a.length, b.length);
            return out;
        }
    }

}

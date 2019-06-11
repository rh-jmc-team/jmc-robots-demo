package com.example.big.troublesome.corp.robot.commons;

public class Message {
    public Protocol protocol;
    public String payload;
    
    public static final Message EMPTY = new Message() {
        {
            protocol = Protocol.NO_OP;
            payload = "-";
        }
    };
    
    public static Message create(String message) {
        if (message == null) {
            return EMPTY;
        }
        
        Message theMessage  = new Message();

        try {
            String[] parts = message.split(":");
            theMessage.protocol = Protocol.valueOf(parts[0]);
            theMessage.payload = parts[1];
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not parse message", e);
        }
        
        return theMessage;
    }
    
    @Override
    public String toString() {
        String _payload = payload;
        if (_payload == null) {
            _payload = "-";
        }
        return protocol + ":" + _payload;
    }
}

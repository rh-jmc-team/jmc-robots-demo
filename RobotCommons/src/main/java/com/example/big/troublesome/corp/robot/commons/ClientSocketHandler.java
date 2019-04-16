package com.example.big.troublesome.corp.robot.commons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientSocketHandler {
    
    private static BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    
    public static void queue(Message message) {
        messages.add(message);
    }

    public static void connect(int port, ClientHandler handler) {
        connect("localhost", port, handler);
    }

    public static void connect(String host, int port, ClientHandler handler) {
        try (Socket socket = new Socket(host, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));)
        {
            System.err.println("Client: socket opened");
            do {
                Message message = messages.take();
                if (message != Message.EMPTY) {
                    out.println(message);
                    String reply = in.readLine();
                    handler.handle(Message.create(reply));
                }
            }
            while (true);
                
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

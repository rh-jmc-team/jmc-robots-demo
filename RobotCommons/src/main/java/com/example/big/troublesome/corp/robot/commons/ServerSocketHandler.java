package com.example.big.troublesome.corp.robot.commons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketHandler {

    public static void connect(int port, Handler handler) {
        try (ServerSocket serverSocket = new ServerSocket(port);
             Socket clientSocket = serverSocket.accept();
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));)
        {
            System.err.println("Server: accepting input");
            String message = null;
            while ((message = in.readLine()) != null) {
                if (message.equals("EOF"))
                    break;

                Message reply = handler.handle(Message.create(message));
                if (!Protocol.NO_OP.equals(reply.protocol)) {
                    out.println(reply);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

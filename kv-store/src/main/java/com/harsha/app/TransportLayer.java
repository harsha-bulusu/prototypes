package com.harsha.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class TransportLayer {
    public void startServer() throws IOException {
        int port = 8080;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started running on port: " + port);
            while(true) {
                Socket socket = serverSocket.accept();
                new QueryParser().parseCommand(socket);
            }
        }
    }
}

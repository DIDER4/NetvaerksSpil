package TCP;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TCPServer {
    private static List<DataOutputStream> clients = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(6789);
        System.out.println("Server started on port 6789");
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            // Log connection on server console
            System.out.println("Client connected: " + connectionSocket.getRemoteSocketAddress());

            clients.add(outToClient);
            System.out.println("Connected clients: " + clients.size());
            new Thread(() -> handleClient(connectionSocket, outToClient)).start();
        }
    }

    private static void handleClient(Socket socket, DataOutputStream outToClient) {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String clientMessage;
            while ((clientMessage = inFromClient.readLine()) != null) {
                // Echo back to the sender
                try {
                    outToClient.writeBytes(clientMessage + "\n");
                } catch (IOException e) {
                    // ignore write error to sender
                }
                // Broadcast to all other clients
                broadcast(clientMessage, outToClient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clients.remove(outToClient);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("Client disconnected: " + outToClient);
            System.out.println("Connected clients: " + clients.size());
        }
    }

    private static void broadcast(String message, DataOutputStream exclude) {
        synchronized (clients) {
            for (DataOutputStream client : clients) {
                if (client == exclude) continue;
                try {
                    client.writeBytes(message + "\n");
                } catch (IOException e) {
                    // ignore write errors for individual clients
                }
            }
        }
    }
}
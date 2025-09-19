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
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
//            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());
//            String clientMessage;
//            while ((clientMessage = inFromClient.readLine()) != null) {
//                System.out.println("Received: " + clientMessage);
//                outToClient.writeBytes(clientMessage + "\n"); // Opdaterer spilleren igen
//            }
            clients.add(outToClient);
            new Thread(() -> handleClient(connectionSocket, outToClient)).start();
        }
    }

    private static void handleClient(Socket socket, DataOutputStream outToClient) {
        try {
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String clientMessage;
            while ((clientMessage = inFromClient.readLine()) != null) {
                broadcast(clientMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clients.remove(outToClient);
        }
    }

    private static void broadcast(String message) {
        synchronized (clients) {
            for (DataOutputStream client : clients) {
                try {
                    client.writeBytes(message + "\n");
                } catch (IOException e) {

                }
            }
        }
    }
}
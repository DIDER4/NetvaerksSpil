package TCP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class RecievingThread extends Thread {
    Socket connSocket;
    String message;

    public RecievingThread(Socket connSocket) {
        this.connSocket = connSocket;
    }


    public void run() {
        try {
            BufferedReader inFromAfsender = new BufferedReader(new InputStreamReader(connSocket.getInputStream()));
            System.out.println("Forbundet til server.");

            while (true) {
                message = inFromAfsender.readLine();
                System.out.println("From sender: " + message);
            }

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}


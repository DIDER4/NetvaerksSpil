package TCP;

import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    public static void main(String[] args) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(6789);
        while (true) {
            Socket connectionSocket = welcomeSocket.accept();
            (new TCPSendThread(connectionSocket)).start();
            (new RecievingThread(connectionSocket)).start();


        }
    }

}
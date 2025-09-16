package TCP;

import java.net.Socket;

public class TCPClient {

	public static void main(String argv[]) throws Exception {

		Socket clientSocket = new Socket("10.10.139.138", 6789);

//		(new TCPSendThread(clientSocket)).start();
//		(new RecievingThread(clientSocket)).start();
	}
}
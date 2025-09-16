

import javafx.application.Application;

import java.net.Socket;

public class App {

	public static void main(String[] args) throws Exception {

		Socket clientSocket = new Socket("10.10.139.138", 6789);

		(new TCPSendThread(clientSocket)).start();
		(new RecievingThread(clientSocket)).start();
		
		Application.launch(GUI.class);

	}
}

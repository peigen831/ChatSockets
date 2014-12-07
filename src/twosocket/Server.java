package twosocket;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private ServerSocket server;
	private Monitor monitor;
	
	private void startServer() {
		try {
			server = new ServerSocket(80, 1000, InetAddress.getByName("localhost"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		monitor = new Monitor();
		
		while(true) {
			try {
				//System.out.println("SERVER: Wait connected");
				Socket clientSocket = server.accept();
				//System.out.println("SERVER: Someone connected");
				Subserver subserver = new Subserver(clientSocket, monitor);
				subserver.start();
				
			} catch(Exception e) {
				System.out.println("Error occure wait for connection and setup streams.");
			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.startServer();
	}
	
}

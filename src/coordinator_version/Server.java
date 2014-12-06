package coordinator_version;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{
	
	private ServerSocket server;
	private Monitor monitor;
	private int subserverType;
	
	public static int COORDINATOR_TO_CLIENT=0;
	public static int COORDINATOR_TO_SERVER=1;
	public static int SERVER_TO_CLIENT=2;
	//public static int CLIENT_TO_SERVER=3; //unused; client has no subservers
	
	public Server(int subserverType)
	{
		this.subserverType=subserverType;
	}
	
	public void run() {
		try {
			server = new ServerSocket(80, 1000, InetAddress.getByName("localhost"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		monitor = new Monitor();
		
		while(true) {
			try {
				System.out.println("SERVER: Wait connected");
				Socket clientSocket = server.accept();
				System.out.println("SERVER: Someone connected");
				//TODO instantiate a different type of subserver depending on the passed subserver type
				StandardSubserver subserver = new StandardSubserver(clientSocket, monitor);
				subserver.start();
				
			} catch(Exception e) {
				System.out.println("Error occure wait for connection and setup streams.");
			}
		}
	}

	public static void main(String[] args) {
		Server server = new Server(2);
		server.start();
	}
	
}

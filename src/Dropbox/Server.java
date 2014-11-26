package Dropbox;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private ServerSocket server;
	
	
	//mutex for file retrieving
	
	private void startServer(){
		try 
		{
			server = new ServerSocket(80, 1000, InetAddress.getByName("localhost"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while(true)
		{
			try
			{
				System.out.println("Wait connected");
				Socket clientSocket = server.accept();
				System.out.println("Someone connected");
				Subserver subserver = new Subserver(clientSocket);
				subserver.start();
				
			}catch(Exception e){
				System.out.println("Error occure wait for connection and setup streams.");
			}
		}
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Server server = new Server();
		
		server.startServer();
	}

}

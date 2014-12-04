package twosocket;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Coordinator extends Thread {
	private int maxServers=3;
	private int numServers=0;
	ServerSocket forwardServer;
	private Monitor monitor;
	
	public void run(){
		//waitForServers();
		startForwardServer();//open forward server to client connections
		
	}

	

	private void waitForServers() {
		do{
			
			
		}while(numServers<maxServers);
		// TODO Auto-generated method stub
		
	}
	private void distributeFiles()
	{
		
	}
	private void receiveServer()
	{
		numServers++;
	}
	private void startForwardServer(){//serversocket for clients to connect to
		try 
		{
			forwardServer = new ServerSocket(80, 1000, InetAddress.getByName("localhost"));//change this to avoid conflicts?
		} catch (Exception e) {
			e.printStackTrace();
		}
		monitor = new Monitor();
		while(true)
		{
			try
			{
				System.out.println("Wait connected");
				Socket clientSocket = forwardServer.accept();
				System.out.println("Client connected");
				Subserver subserver = new Subserver(clientSocket, monitor);
				subserver.start();
				
			}catch(Exception e){
				System.out.println("Error occure wait for connection and setup streams.");
			}
		}

}
}

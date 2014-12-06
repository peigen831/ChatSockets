package coordinator_version.coordinator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import coordinator_version.Server;

public class Coordinator extends Thread {
	private int maxServers=3;
	private int numServers=0;
	
	
	Server frontServer;
	Server backServer;
	private coordinator_version.Monitor monitor;//should we pass this monitor instead of having separate monitors for front and back servers?
	
	public void run(){
		startBackServer();
		startFrontServer();//open forward server to client connections
		
	}

	private void startBackServer(){
		backServer=new Server(1);
		backServer.start();
	}
	private void startFrontServer(){
		frontServer=new Server(0);
		frontServer.start();
	}

	private void distributeFiles()
	{
		
	}
	
	
}

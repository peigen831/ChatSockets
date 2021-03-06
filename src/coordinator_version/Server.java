package coordinator_version;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import coordinator_version.coordinator.BackSubserver;
import coordinator_version.coordinator.CoordinatorMonitor;
import coordinator_version.coordinator.FrontSubserver;

public class Server extends Thread{
	
	private ServerSocket server;
	private Monitor monitor;
	private int subserverType;
	
	public static final int COORDINATOR_TO_CLIENT=0;
	public static final int COORDINATOR_TO_SERVER=1;
	public static final int SERVER_TO_CLIENT=2;
	
	private String address;
	private int port=80;
	private CoordinatorMonitor coordinatorMonitor;
	
	//public static int CLIENT_TO_SERVER=3; //unused; client has no subservers
	
	public Server(int port,int subserverType, CoordinatorMonitor coordinatorMonitor)//a kludge so we can pass the CoordinatorMonitor to the BackSubserver and FrontSubserver
	{
		this.subserverType=subserverType;
		this.coordinatorMonitor=coordinatorMonitor;
		this.port=port;
	}
	public Server(int port,int subserverType)
	{
		this.subserverType=subserverType;
		//this.address=address;
		this.port=port;
	}
	
	public void run() {
		try {
			address=InetAddress.getByName("localhost").toString().split("/")[1];
			server = new ServerSocket(port, 1000, InetAddress.getByName("localhost"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		monitor = new Monitor();
		if(subserverType==SERVER_TO_CLIENT)
		{
			ServerToCoordinatorClient s2cClient=new ServerToCoordinatorClient(address+"-"+port);
			s2cClient.start();
		}
		while(true) {
			try {
				System.out.println("SERVER: Wait connected");
				Socket clientSocket = server.accept();
				System.out.println("SERVER: Someone connected");
				//TODO instantiate a different type of subserver depending on the passed subserver type
				Subserver subserver;
				switch(subserverType)
				{
				case COORDINATOR_TO_CLIENT:
					subserver=new FrontSubserver(clientSocket, monitor); break;
				case COORDINATOR_TO_SERVER:
					subserver=new BackSubserver(clientSocket, monitor, coordinatorMonitor); break;
				case SERVER_TO_CLIENT:
				default:
					subserver = new StandardSubserver(address + "-" + port, clientSocket, monitor); break;
				}
				subserver.start();
				
				
			} catch(Exception e) {
				System.out.println("Error occure wait for connection and setup streams.");
			}
		}
	}

	public static void main(String[] args) {
		new Server(50,Server.SERVER_TO_CLIENT).start();
		new Server(60,Server.SERVER_TO_CLIENT).start();
		new Server(75,Server.SERVER_TO_CLIENT).start();
	}
	
}

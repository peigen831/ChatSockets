package src.coordinator_version.coordinator;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.TimerTask;


public class Pinger extends TimerTask {
	
	// TODO change monitor type
	private CoordinatorMonitor monitor;
	private String hostName;
	private int portNumber;
	private boolean connectionSuccess;
	
	private Socket socket;
    private PrintWriter outputToServer;
	
	public Pinger(CoordinatorMonitor monitor, String hostName, int portNumber) {
		this.monitor = monitor;
		this.hostName = hostName;
		this.portNumber = portNumber;
		connectionSuccess = false;
	}
	
	@Override
	public void run() {
		connectToServer();
		setupStreams();
		if(connectionSuccess)
		{
		    outputToServer.println("PING");
		    closeConnection();
		}
		
		if (connectionSuccess) {
			// Ask monitor to set server as available
			monitor.addAvailableServer(hostName, portNumber);
		}
		else {
			// Ask monitor to set server as unavailable
			monitor.removeAvailableServer(hostName, portNumber);
		}
	}
	
	private void connectToServer() {
		try {
			socket = new Socket(hostName, portNumber);
			connectionSuccess = true;
			System.out.println("HOST " + hostName + ":" + portNumber + " FOUND");
		} catch(Exception e) {
			connectionSuccess = false;
			System.out.println("ERROR: HOST " + hostName + ":" + portNumber + " DOES NOT EXIST");
			// e.printStackTrace();
		}
	}
	
	private void setupStreams() {
		try {
			outputToServer = new PrintWriter(socket.getOutputStream(), true);
			outputToServer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnection() {
		try {
			outputToServer.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

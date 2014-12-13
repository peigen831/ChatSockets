package coordinator_version.coordinator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import coordinator_version.Server;

public class Coordinator extends Thread {
	private int maxServers=3;
	private int numServers=0;
	public static final String MASTER_LIST="src/coordinator_version/Coordinator_Folder/masterlist.properties";
	public static final String SERVER_FOLDER = "src/coordinator_version/Coordinator_Folder/ServerProperties/";
	
	Server frontServer;
	Server backServer;
	private coordinator_version.Monitor monitor;//should we pass this monitor instead of having separate monitors for front and back servers?
	static CoordinatorMonitor coordinatorMonitor;
	
	public void run(){
		coordinatorMonitor = new CoordinatorMonitor();
		startHeartbeatChecks(5); //starts pinging servers for heartbeats every 5 seconds
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
	
	private void startHeartBeatChecks() {
		startHeartbeatChecks(5);
	}
	
	private void startHeartbeatChecks(int seconds) {
		List<String> existingServers = new ArrayList<>();
		
		File fileDirector = new File("ServerProperties/");
		for (File file : fileDirector.listFiles()) {
			Properties properties = new Properties();
			try {
				FileInputStream fileInput = new FileInputStream(file);
				properties.load(fileInput);
				fileInput.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			@SuppressWarnings("rawtypes")
			Enumeration enuKeys = properties.keys();
			String hostName = null;
			int portNumber = 0;
			while (enuKeys.hasMoreElements()) {
				String key = (String) enuKeys.nextElement();
				String value = properties.getProperty(key);
				if (key == "ADDRESS") {
					hostName = value;
				}
				if (key == "PORT") {
					portNumber = Integer.parseInt(value);
					break;
				}
			}
			existingServers.add(hostName + ":" + portNumber);
		}
		
		for (String serverAddress : existingServers) {
			String[] arrServerAddress = serverAddress.split(":");
			String hostName = arrServerAddress[0];
			int portNumber = Integer.parseInt(arrServerAddress[1]);
			Timer timer = new Timer();
			timer.schedule(new Pinger(coordinatorMonitor, hostName, portNumber), seconds * 1000);
		}
	}
}

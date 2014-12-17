package coordinator_version.coordinator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;

import coordinator_version.Server;

public class Coordinator extends Thread {
	static final int maxServers=3;
	private int numServers=0;
	public static final String MASTER_LIST="src/coordinator_version/Coordinator_Folder/masterlist.properties";
	public static final String SERVER_FOLDER = "src/coordinator_version/Coordinator_Folder/ServerProperties/";
	
	Server frontServer;
	Server backServer;
	private coordinator_version.Monitor monitor;//should we pass this monitor instead of having separate monitors for front and back servers?
	static CoordinatorMonitor coordinatorMonitor;
	static PropertiesMonitor propertiesMonitor;
	
	public void run(){
		coordinatorMonitor = new CoordinatorMonitor();
		propertiesMonitor = new PropertiesMonitor();
		startHeartbeatChecks(5); //starts pinging servers for heartbeats every 5 seconds
		startBackServer();
		startFrontServer();//open forward server to client connections
		
	}

	private void startBackServer(){
		backServer=new Server(100,Server.COORDINATOR_TO_SERVER);
		backServer.start();
	}
	private void startFrontServer(){
		frontServer=new Server(101,Server.COORDINATOR_TO_CLIENT);
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
		
		File fileDirectory = new File(Coordinator.SERVER_FOLDER);
		if(fileDirectory.exists())
		{
			for (File file : fileDirectory.listFiles()) {
				Properties properties = new Properties();
				try {
					FileInputStream fileInput = new FileInputStream(file);
					properties.load(fileInput);
					fileInput.close();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				Set<Entry<Object, Object>> propertiesSet = properties.entrySet();
				
				String hostName = null;
				int portNumber = 0;
				
				for (Entry<Object, Object> entry : propertiesSet) {
					if (entry.getKey().equals("ADDRESS")) {
						hostName = entry.getValue().toString();
					}
					else if (entry.getKey().equals("PORT")) {
						portNumber = Integer.parseInt(entry.getValue().toString());
					}
				}
				
				System.out.print(file.getName() + ":: ");
				System.out.println(hostName + ":" + portNumber);
				existingServers.add(hostName + ":" + portNumber);
			}
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

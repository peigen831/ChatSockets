package coordinator_version.coordinator;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import coordinator_version.Monitor;
import coordinator_version.Subserver;

/**
 * Serves a client connecting to the coordinator
 * @author Andrew
 *
 */
public class FrontSubserver extends Subserver{

	private String clientPropPath = "src/coordinator_version/Coordinator_Folder/ClientProperties/";
	private Socket socket;
	public final String ADDED = "ADDED";
	public final String DELETED = "DELETED";
	
	
	public FrontSubserver(Socket socket, Monitor monitor){
		super(socket, monitor);
	}
	
	@Override
	protected void parseAndRunCommand(String command) {
		System.out.println("Command: " + command);
		switch (command) {
			case "INDEX": getIndex(); break;
			/*case "GET": giveFile(); break;
			case "GET_SIZE": getFileSize(); break;
			case "GIVE": getFile(); break;
			case "INSYNC": monitor.checkAndSetLastSync(System.currentTimeMillis()); break;*/
			default: break;
		}
	}

	private void getIndex() {
		
		//TODO for each file to be sent to the client, include the IP + port  of the relevant server
		String index = null;
		List<String> listIndexToGet = new ArrayList<>();
		List<String> listIndexToGive = new ArrayList<>();
		List<String> listToDestroyServer = new ArrayList<>();
		List<String> listToDestroyClient = new ArrayList<>();
		
		Map<String, Long> mapIndexFromClient = new HashMap<>();
		
		//for property file
		HashMap<String, String> masterlistAction = new HashMap<>();
		HashMap<String, String> clientPropertyAction = new HashMap<>();
		
		//Get client's file index
		do {
			try {
				index = inputFromClient.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (index != null) {
				if (index.equals("INDEX_DONE")) {
					break;
				}
				String[] dataFromClient = index.split(":");
				
				//indicate client's identity
				if(dataFromClient[0].equals("NAME"))
					clientPropPath += dataFromClient[1] + ".properties";
				
				//indicate files' identity
				else
					mapIndexFromClient.put(dataFromClient[0], Long.parseLong(dataFromClient[1]));
			}
		} while (index != null);
		

		//load server property
		Properties serverProp = Coordinator.coordinatorMonitor.loadProperties(Coordinator.MASTER_LIST);
		
		//load client's property
		Properties clientProp = Coordinator.coordinatorMonitor.loadProperties(clientPropPath);
		
		// Get master list index and compare to client's file index
		
		for(String filename: serverProp.stringPropertyNames())
		{
			String[] value = serverProp.getProperty(filename).split("\\|");
        	Long serverLastmodify = Long.valueOf(value[0]);
			String status = value[1];
			String[] listServer = new String[value.length-2];
			
			for(int i = 0; i < value.length-2; i++)
				listServer[i] = value[i+2];
			

			Long time = System.currentTimeMillis();
			HashMap<String, Integer> serverportMap;
			// if the file exists on both client and server
			if (mapIndexFromClient.containsKey(filename)) {
				long clientDate = mapIndexFromClient.get(filename);
				
				// add to client, if the file on server is newer
				if (clientDate < serverLastmodify) { 
					
					serverportMap = Coordinator.coordinatorMonitor.getServerportMap(listServer);
					String masterAction = generateMasterlistAction(time, ADDED, serverportMap);
					String serverports = stringServerport(serverportMap);

					listIndexToGive.add(filename + "|" + serverports);
					masterlistAction.put(filename, masterAction);
					clientPropertyAction.put(filename, Long.toString(time));
				}
				
				// add to client, if the file on client is newer
				else if (clientDate > serverLastmodify){
					
					serverportMap = Coordinator.coordinatorMonitor.getServerToGetMap(listServer);
					String masterAction = generateMasterlistAction(time, ADDED, serverportMap);
					String serverports = stringServerport(serverportMap);
					
					listIndexToGet.add(filename + "|" + serverports);//ok
					masterlistAction.put(filename, masterAction);//ok
					clientPropertyAction.put(filename, Long.toString((time)));
				}
				
				// if the file on client is not updated, do nothing
				
				mapIndexFromClient.remove(filename);
			}
			
			// if the file only exists on the server
			else {
				// delete on server
				if(clientProp.containsKey(filename)){
					serverportMap = Coordinator.coordinatorMonitor.getServerportMap(listServer);
					String masterAction = generateMasterlistAction(time, DELETED, serverportMap);
					String serverports = stringServerport(serverportMap);
					
					
					listToDestroyServer.add(filename + "|" + serverports);
					masterlistAction.put(filename, masterAction);
				}
				
				// add to client
				else if (!clientProp.containsKey(filename)) {
					serverportMap = Coordinator.coordinatorMonitor.getServerportMap(listServer);
					String masterAction = generateMasterlistAction(time, ADDED, serverportMap);
					String serverports = stringServerport(serverportMap);

					listIndexToGive.add(filename + "|" + serverports);
					masterlistAction.put(filename, masterAction);
					clientPropertyAction.put(filename, Long.toString(time));
				}	
			}
		}
		
		
		// check for files that exist only on the client
		//TODO use masterlist instead of server properties
		for (Entry<String, Long> entry : mapIndexFromClient.entrySet()) {
			String filename = entry.getKey();
			long clientDate = entry.getValue();
			System.out.println("File info: " + filename + " " + clientDate);
			
			if(serverProp.containsKey(filename)){
				String[] arr = serverProp.get(filename).toString().split(":"); 
				long serverDate = Long.parseLong(arr[0]);
				String action = arr[1];
				
				// delete on client, if the file previously exist on the server
				if(action.equals("DELETED") && serverDate >= clientDate){
					System.out.println("Destroy on client " + filename );
					listToDestroyClient.add(filename);
					masterlistAction.put(filename, System.currentTimeMillis() + ":DELETED");
				}
				
				// add to server, when clientDate is greater than server's file deleted date
				else{
					listIndexToGet.add(filename);
					masterlistAction.put(filename, System.currentTimeMillis() + ":ADDED");
					clientPropertyAction.put(filename, Long.toString(System.currentTimeMillis()));
				}
			}
			//add to server, when server never encounter this file
			else{
				listIndexToGet.add(filename);
				clientPropertyAction.put(filename, Long.toString(System.currentTimeMillis()));
				masterlistAction.put(filename, System.currentTimeMillis() + ":ADDED");
			}
		}
		
		StringBuilder sb = new StringBuilder();
		
		// send instructions to client
		for (String filename : listIndexToGet) {
			// give client list of requested files "to give" to server
			sb.append("TO_GIVE|" + filename + "\n");
		}
		for (String filename : listIndexToGive) {
			// give client list of files "to get" from server
			sb.append("TO_GET|" + filename + "\n");
		}
		for (String filename : listToDestroyClient) {
			// give client list of files "to destroy" on client
			sb.append("TO_DESTROY|" + filename + "\n");
		}
		for (String filename : listToDestroyServer) {
			sb.append("TO_DESTROY_SERVER|" + filename + "\n");
		}
		
		try {
			sb.append("INDEX_DONE");
			outputToClient.println(sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		long syncTime = System.currentTimeMillis();
		
		masterlistAction.put("LAST_SYNC", Long.toString(syncTime));
		clientPropertyAction.put("LAST_SYNC", Long.toString(syncTime));
		
		//monitor.updateServerProperties(serverPropertiesPath, masterlistAction);
		//monitor.updateClientProperties(clientPropertiesPath, clientPropertyAction);
		
	}
	
	private String generateMasterlistAction(Long time, String status, HashMap<String, Integer> serverportMap){
		StringBuilder action = new StringBuilder();
		
		action.append(time + "|" + status);
		
		for(Entry<String, Integer> entry: serverportMap.entrySet()){
			action.append("|" + entry.getKey());
		}
		
		return action.toString();
	}
	
	private String stringServerport(HashMap<String, Integer> serverportMap){
		StringBuilder action = new StringBuilder();
		int cnt = 0;
		
		for(Entry<String, Integer> entry: serverportMap.entrySet()){
			action.append(entry.getKey() + ":" + entry.getValue());
			
			if(cnt < serverportMap.size()-1)
				action.append("|");
			
			cnt++;
		}
		
		return action.toString();
	}
	
	

}

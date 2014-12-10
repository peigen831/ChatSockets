package coordinator_version.coordinator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import coordinator_version.Monitor;
import coordinator_version.Subserver;

/**
 * Serves a client connecting to the coordinator
 * @author Andrew
 *
 */
public class FrontSubserver extends Subserver{

	private String serverPropertiesPath = "src/coordinator_version/masterlist.properties";
	private String clientPropertiesPath;
	
	public FrontSubserver(Socket socket, Monitor monitor) {
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
		HashMap<String, String> serverPropertyAction = new HashMap<>();
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
					clientPropertiesPath = "src/coordinator_version/" + dataFromClient[1] + ".properties";
				
				//indicate files' identity
				else
					mapIndexFromClient.put(dataFromClient[0], Long.parseLong(dataFromClient[1]));
			}
		} while (index != null);
		

		//load server property
		Properties serverProp = monitor.loadProperties(serverPropertiesPath);
		
		//load client's property
		Properties clientProp = monitor.loadProperties(clientPropertiesPath);
		
		// Get coordinator's file index and compare to client's file index
		File masterList = new File(Coordinator.FILE_MASTER_LIST);
		BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader(masterList));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line;
		try {
			while ((line = br.readLine()) != null) {
			   String[] results=line.split(":");
			   
			   
			   String filename = results[0];
				long serverLastmodify = Long.parseLong(results[1]);
				// if the file exists on both client and server
				if (mapIndexFromClient.containsKey(filename)) {
					long clientDate = mapIndexFromClient.get(filename);
					
					// if the file on server is newer
					if (clientDate < serverLastmodify) {
						listIndexToGive.add(filename);
						clientPropertyAction.put(filename, Long.toString(System.currentTimeMillis()));
						serverPropertyAction.put(filename, System.currentTimeMillis()+ ":ADDED");
					}
					
					// if the file on client is newer
					else if (clientDate > serverLastmodify){
						listIndexToGet.add(filename);
						serverPropertyAction.put(filename, System.currentTimeMillis() + ":ADDED");
						clientPropertyAction.put(filename, Long.toString(System.currentTimeMillis()));
					}
					
					// if the file on client is not updated, do nothing
					
					mapIndexFromClient.remove(filename);
				}
				
				// if the file only exists on the server
				else {
					// delete on server, 
					if(clientProp.containsKey(filename)){
						listToDestroyServer.add(filename);
						serverPropertyAction.put(filename, System.currentTimeMillis() + ":DELETED");
						System.out.println(filename + " clientProp.containskey: DELETE on server");
					}
					
					// add to client, 
					else if (!clientProp.containsKey(filename)) {
						listIndexToGive.add(filename);
						clientPropertyAction.put(filename, Long.toString(System.currentTimeMillis()));
						serverPropertyAction.put(filename, System.currentTimeMillis()+ ":ADDED");
					}	
				}
				
			   
			}
			br.close();
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		

		
		//mapIndexFromClient.remove("LAST_SYNC");
		
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
					serverPropertyAction.put(filename, System.currentTimeMillis() + ":DELETED");
				}
				
				// add to server, when clientDate is greater than server's file deleted date
				else{
					listIndexToGet.add(filename);
					serverPropertyAction.put(filename, System.currentTimeMillis() + ":ADDED");
					clientPropertyAction.put(filename, Long.toString(System.currentTimeMillis()));
				}
			}
			//add to server, when server never encounter this file
			else{
				listIndexToGet.add(filename);
				clientPropertyAction.put(filename, Long.toString(System.currentTimeMillis()));
				serverPropertyAction.put(filename, System.currentTimeMillis() + ":ADDED");
			}
		}
		
		StringBuilder sb = new StringBuilder();
		
		// send instructions to client
		for (String filename : listIndexToGet) {
			// give client list of requested files "to give" to server
			sb.append("TO_GIVE:" + filename + "\n");
		}
		for (String filename : listIndexToGive) {
			// give client list of files "to get" from server
			sb.append("TO_GET:" + filename + "\n");
		}
		for (String filename : listToDestroyClient) {
			// give client list of files "to destroy" on client
			sb.append("TO_DESTROY:" + filename + "\n");
		}
		for (String filename : listToDestroyServer) {
			sb.append("TO_DESTROY_SERVER:" + filename + "\n");
		}
		
		try {
			sb.append("INDEX_DONE");
			outputToClient.println(sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		
		long syncTime = System.currentTimeMillis();
		serverPropertyAction.put("LAST_SYNC", Long.toString(syncTime));
		clientPropertyAction.put("LAST_SYNC", Long.toString(syncTime));
		
		monitor.updateServerProperties(serverPropertiesPath, serverPropertyAction);
		monitor.updateClientProperties(clientPropertiesPath, clientPropertyAction);
		
	}
	
	

}

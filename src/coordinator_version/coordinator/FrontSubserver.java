package coordinator_version.coordinator;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
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

	private String clientPropPath = Coordinator.CLIENT_FOLDER;
	public final String ADDED = "ADDED";
	public final String DELETED = "DELETED";
	public final String UPDATED = "UPDATED";
	
	
	public FrontSubserver(Socket socket, Monitor monitor){
		super(socket, monitor);
	}
	
	@Override
	protected void parseAndRunCommand(String command) {
		System.out.println("FRONTSUBSERVER | Command: " + command);
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
		
		String index = null;
		String clientName = null;
        Long time = System.currentTimeMillis();
        
        // Map<File Name, Action> (Action = ADDED, DELETED, UPDATED)
        Map<String, String> mapClient = new HashMap<>();
        Map<String, String> mapServer = new HashMap<>();
        
        Set<String> listIndexToGet = new HashSet<String>();
        Set<String> listIndexToGive = new HashSet<String>();
        Set<String> listOfConflicts = new HashSet<String>();
        Set<String> listToDestroyServer = new HashSet<String>();
        Set<String> listToDestroyClient = new HashSet<String>();
        
        // Map<File Name, Last Modified>
        Map<String, Long> mapNewClientIndex = new HashMap<>();
        
        //for property file
        //HashMap<String, String> masterlistAction = new HashMap<>();
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
                if(dataFromClient[0].equals("NAME")) {
                	clientName = dataFromClient[1];
                    clientPropPath += clientName + ".properties";
                }
                
                //indicate files' identity
                else
                    mapNewClientIndex.put(dataFromClient[0], Long.parseLong(dataFromClient[1]));
            }
        } while (index != null);
        

        //load server property
        Properties masterlistProp = Coordinator.propertiesMonitor.loadProperties(Coordinator.MASTER_LIST);
        
        //load client's property
        Properties clientProp = Coordinator.propertiesMonitor.loadProperties(clientPropPath);
        
        
        /** Get client's old file index and compare to client's new file index **/
        Map<String, Long> mapOldClientIndex = new HashMap<>();
        
        // Get client's old file index, check if each file is found in new file index
        for (String filename : clientProp.stringPropertyNames()) {
            Long oldLastModified = Long.valueOf(clientProp.getProperty(filename));
            Long newLastModified = mapNewClientIndex.get(filename);
            
            mapOldClientIndex.put(filename, oldLastModified);
            
            if (!filename.equals("LAST_SYNC")) {
            	// if file is in new file index, check if modified
                if (mapNewClientIndex.containsKey(filename)) {
                    if (newLastModified > oldLastModified) {
                        mapClient.put(filename, UPDATED);
                        listIndexToGet.add(filename);
                    }
                    else {
                    	clientPropertyAction.put(filename, Long.toString((time)));
                    }
                }
                // if file is not in new file index, set action as DELETED
                else {
                    mapClient.put(filename, DELETED);
                    listToDestroyServer.add(filename);
                }
            }
        }
        
        // Check if each file from new file index is not found in old file index
        for (String filename : mapNewClientIndex.keySet()) {
            // if file is not in old file index, set action as ADDED
            if (!mapOldClientIndex.containsKey(filename)) {
                mapClient.put(filename, ADDED);
                listIndexToGet.add(filename);
            }
        }
        
        
        /** Get master list index and compare to client's old file index **/
        Map<String, String> mapServerIndex = new HashMap<>();
        
        // Get master list index and compare to client's old file index
        for (String filename : masterlistProp.stringPropertyNames()) {
            String value = masterlistProp.getProperty(filename);
            mapServerIndex.put(filename, value);
            System.out.println(filename + ": " + value);
            String[] propertyValues = value.split("\\|");
            Long lastModified = Long.valueOf(propertyValues[0]);
            String status = propertyValues[1];
            
            // if file is deleted, delete in client
            if (status == DELETED) {
            	mapServer.put(filename, DELETED);
            	listToDestroyClient.add(filename);
            }
            
            // if file is in the client's old file index, check if modified
            else if (mapOldClientIndex.containsKey(filename)) {
                if (lastModified > mapOldClientIndex.get(filename)) {
                    mapServer.put(filename, UPDATED);
                    listIndexToGive.add(filename);
                }
            }
            // if file is not in the client's old file index, set action as ADDED
            else {
                mapServer.put(filename, ADDED);
                listIndexToGive.add(filename);
            }
        }
        
        // Check if each file from the client's old file index is not found in the master list
        for (String filename : mapOldClientIndex.keySet()) {
            if (!mapServerIndex.containsKey(filename)) {
                mapServer.put(filename, DELETED);
                listToDestroyClient.add(filename);
            }
        }
        
        
        /** Compare resulting lists for conflicts **/
        /** For now, conflicting copies will be overwritten by the server's copy,
         *  in the future, conflicting copies may create a new file **/
        
        for (String filename : mapClient.keySet()) {
            if (mapServer.containsKey(filename)) {
                String serverStatus = mapServer.get(filename);
                String clientStatus = mapClient.get(filename);
                
                // if file is UPDATED or ADDED on server
                if (serverStatus == UPDATED || serverStatus == ADDED) {
                    // if file is UPDATED or ADDED on client, conflict
                    if (clientStatus == UPDATED || clientStatus == ADDED) {
                        listOfConflicts.add(filename);
                        listIndexToGive.add(filename);
                        listIndexToGet.remove(filename);
                    }
                    // if file is DELETED on client, overwrite from server
                    else if (clientStatus == DELETED) {
                        listIndexToGive.add(filename);
                        listToDestroyServer.remove(filename);
                    }
                }
                
                // if file is DELETED on server
                else if (serverStatus == DELETED) {
                    // if file is UPDATED or ADDED on client, overwrite from client
                    if (clientStatus == UPDATED || clientStatus == ADDED) {
                        listIndexToGet.add(filename);
                        listToDestroyClient.remove(filename);
                    }
                }
            }
        }
        
        
        /** Get servers for each file **/
        Set<String> tempList; 
        
        tempList = new HashSet<>(listIndexToGet);
        listIndexToGet.clear();
        for (String filename : tempList) {
            String value = masterlistProp.getProperty(filename);
            String[] serversWithFile = new String[0];
            if (value != null) {
	            String[] propertyValues = masterlistProp.getProperty(filename).split("\\|");
	            serversWithFile = value.replace(propertyValues[0] + "|" + propertyValues[1] + "|",
	                    "").split("\\|");
            }
            HashMap<String, String> serverAddressportMap = Coordinator.propertiesMonitor.
            											   getServerToGetMap(serversWithFile);
            String servers = stringAddressport(serverAddressportMap);
            
            listIndexToGet.add(filename + "|" + servers);
            clientPropertyAction.put(filename, Long.toString((time)));
        }
        
        tempList = new HashSet<>(listIndexToGive);
        listIndexToGive.clear();
        for (String filename : tempList) {
            String value = masterlistProp.getProperty(filename);
            String[] serversWithFile = new String[0];
            if (value != null) {
	            String[] propertyValues = masterlistProp.getProperty(filename).split("\\|");
	            serversWithFile = value.replace(propertyValues[0] + "|" + propertyValues[1] + "|",
	                    "").split("\\|");
            }
            HashMap<String, String> serverAddressportMap = Coordinator.propertiesMonitor.
            											   getServerAddressportMap(serversWithFile);
            String servers = stringAddressport(serverAddressportMap);
            
            listIndexToGive.add(filename + "|" + servers);
            clientPropertyAction.put(filename, Long.toString((time)));
            
            if (listOfConflicts.contains(filename)) {
            	String newfilename = filename + "(" + clientName + "'s_conflicted_copy)";
            	listOfConflicts.remove(filename);
            	listOfConflicts.add(filename + "|" + servers);
                clientPropertyAction.put(newfilename, Long.toString((time)));
            }
        }
        
        tempList = new HashSet<>(listToDestroyServer);
        listToDestroyServer.clear();
        for (String filename : tempList) {
            String value = masterlistProp.getProperty(filename);
            String[] serversWithFile = new String[0];
            if (value != null) {
	            String[] propertyValues = masterlistProp.getProperty(filename).split("\\|");
	            serversWithFile = value.replace(propertyValues[0] + "|" + propertyValues[1] + "|",
	                    "").split("\\|");
            }
            HashMap<String, String> serverAddressportMap = Coordinator.propertiesMonitor.
            											   getServerAddressportMap(serversWithFile);
            String servers = stringAddressport(serverAddressportMap);
            
            listToDestroyServer.add(filename + "|" + servers);
        }
		
        
        /** Send data to client **/
        
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
		for (String filename : listOfConflicts) {
			// give client list of files "to get" from server
			sb.append("CONFLICT|" + filename + "\n");
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
		
		//masterlistAction.put("LAST_SYNC", Long.toString(syncTime));
		clientPropertyAction.put("LAST_SYNC", Long.toString(syncTime));
		
		//Coordinator.propertiesMonitor.updateMasterlistProperties(Coordinator.MASTER_LIST, masterlistAction);
		Coordinator.propertiesMonitor.updateClientProperties(clientPropPath, clientPropertyAction);
		
	}
	
	private String stringAddressport(HashMap<String, String> serverportMap){
		StringBuilder action = new StringBuilder();
		int cnt = 0;
		
		for(Entry<String, String> entry: serverportMap.entrySet()){
			action.append(entry.getValue());
			
			if(cnt < serverportMap.size()-1)
				action.append("|");
			
			cnt++;
		}
		
		return action.toString();
	}
}

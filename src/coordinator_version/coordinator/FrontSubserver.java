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
import java.util.Map.Entry;

import coordinator_version.Monitor;
import coordinator_version.Subserver;

/**
 * Serves a client connecting to the coordinator
 * @author Andrew
 *
 */
public class FrontSubserver extends Subserver{

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
		
		// Get client's file index
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
				String[] file = index.split(":");
				mapIndexFromClient.put(file[0], Long.parseLong(file[1]));
			}
		} while (index != null);
		
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
				long filedate = Long.parseLong(results[1]);
				
				// if the file exists on both client and server
				if (mapIndexFromClient.containsKey(filename)) {
					
					// if the file on server is newer
					if (mapIndexFromClient.get(filename) < filedate) {
						listIndexToGive.add(filename);
					}
					// if the file on client is newer
					else if (mapIndexFromClient.get(filename) > filedate){
						listIndexToGet.add(filename);
					}
					
					// if the file on client is not updated, do nothing
					
					mapIndexFromClient.remove(filename);
				}
				
				// if the file only exists on the server
				else {
					if (filedate > mapIndexFromClient.get("LAST_SYNC")) {
						listIndexToGive.add(filename);
					}
					else {
						listToDestroyServer.add(filename);
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
		
		
		
		
		
		mapIndexFromClient.remove("LAST_SYNC");
		// check for files that exist only on the client
		for (Entry<String, Long> entry : mapIndexFromClient.entrySet()) {
			if (entry.getValue() > monitor.getLastSync()) {
				listIndexToGet.add(entry.getKey());
			}
			else {
				listToDestroyClient.add(entry.getKey());
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
			// give client list of files "to destroy" on server
			sb.append("TO_DESTROY_SERVER:" + filename + "\n");
		}
		
		try {
			sb.append("INDEX_DONE");
			outputToClient.println(sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	

}

package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Subserver of a file server
 * @author Andrew
 *
 */
public class StandardSubserver extends Subserver {
	
	private String serverPropertiesPath = "src/coordinator_version/server.properties";
	private String clientPropertiesPath;
	
	public StandardSubserver(Socket socket, Monitor monitor) {
		super(socket, monitor);
	}
	
	
	@Override
	protected void parseAndRunCommand(String command) {
		//System.out.println("Command: " + command);
		switch (command) {
			case "INDEX": getIndex(); break;
			case "GET": giveFile(); break;
			case "GET_SIZE": getFileSize(); break;
			case "GIVE": getFile(); break;
			//TODO remove
			//case "INSYNC": monitor.checkAndSetLastSync(serverPropertiesPath,  System.currentTimeMillis()); break;
			default: break;
		}
	}
	
	private void getIndex() {
		//TODO change this to receive the complete list of files to give and receive from client, instead of calculating it here
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
					clientPropertiesPath = "src/twosocket/" + dataFromClient[1] + ".properties";
				
				//indicate files' identity
				else
					mapIndexFromClient.put(dataFromClient[0], Long.parseLong(dataFromClient[1]));
			}
		} while (index != null);
		

		//load server property
		Properties serverProp = monitor.loadProperties(serverPropertiesPath);
		
		//load client's property
		Properties clientProp = monitor.loadProperties(clientPropertiesPath);
		
		// Get server's file index and compare to client's file index
		File folder = new File("Server_Folder/");
		File[] fileList = folder.listFiles();
		
		for(File file : fileList) {
			String filename = file.getName();
			long serverLastmodify = file.lastModified();
			
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
		
		//mapIndexFromClient.remove("LAST_SYNC");
		
		// check for files that exist only on the client
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
			String filePath = "Server_Folder/" + filename;
			monitor.deleteFile(filePath);
			/*
			monitor.updateFile(filename);
			File file = new File("Server_Folder/" + filename);
			file.delete();
			monitor.doneUpdatingFile(filename);
			*/
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
	
	private void getFile() {
		String filedata = null;
		
		try {
			filedata = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] arrStrFile = filedata.split(":");
		
		monitor.updateFile(arrStrFile[0]);
		try {
			InputStream in = socket.getInputStream();
			FileOutputStream fos = new FileOutputStream("Server_Folder/_" + arrStrFile[0]);
	        int x = 0;
	        while(true){
	            x = in.read();
	            if(x == -1)
	            	break;
	            fos.write(x);
	        }
	        fos.flush();
	        fos.close();
	        in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File file = new File("Server_Folder/_" + arrStrFile[0]);
		if (Long.parseLong(arrStrFile[1]) == file.length()) {
			if (file.exists())
				file.renameTo(new File("Server_Folder/" + arrStrFile[0]));
				file.delete();
		}
		
		monitor.doneUpdatingFile(arrStrFile[0]);
	}
	
	private void getFileSize() {
		String filename = null;
		
		try {
			filename = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File file = new File("Server_Folder/_" + filename);
		if (!file.exists()) {
			file = new File("Server_Folder/" + filename);
		}
		outputToClient.println(file.length());
	}
	
	private void giveFile() {
		String filedata = null;
		try {
			filedata = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File("Server_Folder/" + filedata);
		
		if(!file.exists())
			outputToClient.println("NOT_FOUND");
		
		else{
			outputToClient.println(file.getName() + ":" + file.length());
			try {
				OutputStream out = socket.getOutputStream();
				FileInputStream fis = new FileInputStream(file);
		        int x = 0;
		        while(true) {
		            x = fis.read();
		            if(x == -1)
		            	break;
		            out.write(x);
		        }
		        out.flush();
		        fis.close();
		        out.close();
			}
			catch (Exception e) {
		    	e.printStackTrace();
		    }
		}
	}
	
	protected void closeEverything(){
		try {
			outputToClient.close();
			inputFromClient.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

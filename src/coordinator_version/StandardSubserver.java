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

public class StandardSubserver extends Subserver {
	
	private Monitor monitor;
	
	private Socket socket;
	private PrintWriter outputToClient;
	private BufferedReader inputFromClient;
	
	public StandardSubserver(Socket socket, Monitor monitor) {
		super(socket, monitor);
	}
	
	@Override
	public void run() {
		try {
			outputToClient = new PrintWriter(socket.getOutputStream(), true);
			outputToClient.flush();
			
			inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		String command = null;
		try {
			command = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parseAndRunCommand(command);
		
		try {
			outputToClient.close();
			inputFromClient.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void parseAndRunCommand(String command) {
		System.out.println("Command: " + command);
		switch (command) {
			case "INDEX": getIndex(); break;
			case "GET": giveFile(); break;
			case "GET_SIZE": getFileSize(); break;
			case "GIVE": getFile(); break;
			case "INSYNC": monitor.checkAndSetLastSync(System.currentTimeMillis()); break;
			default: break;
		}
	}
	
	private void getIndex() {
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
		
		// Get server's file index and compare to client's file index
		File folder = new File("Server_Folder/");
		File[] fileList = folder.listFiles();
		
		for(File file : fileList) {
			String filename = file.getName();
			long filedate = file.lastModified();
			
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
			monitor.updateFile(filename);
			File file = new File("Server_Folder/" + filename);
			file.delete();
			monitor.doneUpdatingFile(filename);
		}
		
		try {
			sb.append("INDEX_DONE");
			outputToClient.println(sb.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
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

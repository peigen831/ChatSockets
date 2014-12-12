package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * The socket created by a server in order to send a file(s) to another server.
 * 
 * Sends its name, and a list of files on the server, to the coordinator.
 * 
 * 
 * 
 * @author Andrew
 *
 */
public class ServerToServerClient extends Thread {
	private String hostName = "localhost";
	private int portNumber = 80;
	private String folderName;
	//private String clientProperties;
	//private long lastSync;
	private String serverName;
	
	private Socket socket;
	private PrintWriter outputToServer;
	private BufferedReader inputFromServer;

	
	/**files to send to backup server
	 * 
	 */
	private List<String> listToGive;
	/**
	 * files to delete on backup server
	 */
	private List<String> listToDeleteServer;
	
	
	public ServerToServerClient(String serverName) {
		this.serverName = serverName;
		folderName = serverName + "_Folder/";
		
	}
	
	@Override
	public void run() {
		connectToCoordinator();
		
		setupStreams();

		//note: coordinator automatically stores server's IP address, port number, and last sync time as based on coordinator's current system time.
		//sendFileDetails();
		
		sendFiles();
		
		//getResponse();
		
		closeConnection();
	
	}
	
	private void sendFiles(){
List<Thread> threads = new ArrayList<>();
		
		
		
		if (!listToGive.isEmpty()) {
			ClientSender cs = new ClientSender(hostName, portNumber);
			cs.setFileList(listToGive);
			cs.setFolderName(folderName);
			threads.add(cs);
			cs.run();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void connectToCoordinator() {
		try {
			socket = new Socket(hostName, portNumber);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setupStreams() {
		try {
			outputToServer = new PrintWriter(socket.getOutputStream(), true);
			outputToServer.flush();
			
			inputFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void sendFileDetails() {
		File folder = new File(folderName);
		File[] fileList = folder.listFiles();
		StringBuilder sb = new StringBuilder();
		
		sb.append("INDEX\n");
		
		sb.append("NAME:" + serverName + "\n");
		
		for(int i = 0; i < fileList.length; i++) {
			sb.append(fileList[i].getName() + ":" + fileList[i].lastModified() + "\n");
		}
		
		try {
			sb.append("INDEX_DONE");
			outputToServer.println(sb.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeConnection() {
		try {
			inputFromServer.close();
			outputToServer.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

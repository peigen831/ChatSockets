package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coordinator_version.coordinator.MasterlistEntry;


/**
 * The socket created by a server in order to send a SINGLE file to another (SINGLE) server.
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
	
	//Map<String, Integer>backupServers=new HashMap<String, Integer>();
	
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
	private String fileData;
	private int status;
	private String [] serverList;
	
	
	
	public ServerToServerClient(String serverName, String fileData, String[] serverList,int status) {
		this.serverName = serverName;
		this.fileData=fileData;
		this.status=status;
		folderName = serverName + "_Folder/";
		
	}
	/**
	 * To be called by the StandardSubserver when a file has been received, before running the ServerToServerClient
	 * @param address
	 * @param port
	 */
	/*public void addBackupServer(String address, int port)
	{
		backupServers.put(address,port);
	}*/
	
	@Override
	public void run() {
		connectToServer();
		
		setupStreams();
		
		sendFiles();
		
		//getResponse();
		
		closeConnection();
		
		spawnServerToCoordinatorClient();
	
	}
	
	private void spawnServerToCoordinatorClient() {
		ServerToCoordinatorClient serverToCoordinatorClient =new ServerToCoordinatorClient(serverName);
		serverToCoordinatorClient.setFilename(fileData, status);
		serverToCoordinatorClient.start();
		
	}
	private void sendFiles(){
		
		
		List<Thread> threads = new ArrayList<>();
		
		for(String server: serverList)
		{
			String [] serverInfo=server.split(":");
			hostName=serverInfo[0];
			portNumber=Integer.parseInt(serverInfo[1]);
			if (status!=MasterlistEntry.STATUS_DELETED) {
				S2SClientSender cs = new S2SClientSender();
				
					cs.setFileData(fileData);//we only put the file in a list to avoid modifying ClientSender
			
				cs.setFolderName(folderName);
				threads.add(cs);
				cs.run();
			}
			else
			{
				//TODO notify backup server to delete the file
			}
		}
			for (Thread thread : threads) {
				try {
					thread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			//TODO get confirmation of successful sync; this may require modifying ClientSender
		//}
	}
	
	private void connectToServer() {
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

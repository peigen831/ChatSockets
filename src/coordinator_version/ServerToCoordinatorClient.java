package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import coordinator_version.coordinator.MasterlistEntry;


/**
 * The socket created by a server in order to communicate with the coordinator.
 * 
 * Sends its name, and a list of files on the server, to the coordinator.
 * @author Andrew
 *
 */
public class ServerToCoordinatorClient extends Thread {
	private String hostName = "localhost";
	private int portNumber = 100;
	private String folderName;
	//private String clientProperties;
	//private long lastSync;
	private String serverName;
	
	private Socket socket;
	private PrintWriter outputToServer;
	private BufferedReader inputFromServer;
	/**
	 * null if client is to send a complete list, otherwise will only send metadata of specified file
	 */
	private String fileData=null;
	private int status;
	
	
	public ServerToCoordinatorClient(String serverName) {
		this.serverName = serverName;
		folderName = serverName + "_Folder/";
		
	}
	public void setFileData(String fileData){
		this.fileData=fileData;
		//this.status=status;
	}
	
	@Override
	public void run() {
		connectToCoordinator();
		
		setupStreams();

		//note: coordinator automatically stores server's IP address, port number, and last sync time as based on coordinator's current system time.
		sendFileDetails();
		
		
		
		//getResponse();
		
		closeConnection();
	
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
		if(fileData==null)//first-time connection to coordinator
		{
			File folder = new File(folderName);
			File[] fileList = folder.listFiles();
			StringBuilder sb = new StringBuilder();
			
			sb.append("INDEX\n");
			
			sb.append("NAME:" + serverName + "\n");
			if(fileList!=null)
			{
				for(int i = 0; i < fileList.length; i++) {
					sb.append(fileList[i].getName() + "|" + fileList[i].lastModified() + "\n");
				}
			}
			
			try {
				sb.append("INDEX_DONE");
				outputToServer.println(sb.toString());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		else{
		
	
				String [] data=fileData.split("|");
	
				StringBuilder sb = new StringBuilder();
				sb.append("FILE_CHANGE\n");
				sb.append("NAME:" + serverName + "\n");
				sb.append(data[0]+"|"+data[1]+"|"+data[2]+"\n");
				
				try {
					sb.append("FILE_CHANGE_DONE");
					outputToServer.println(sb.toString());
				} catch(Exception e) {
					e.printStackTrace();
				}
			
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

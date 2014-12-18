package coordinator_version;

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
	/*private String hostName = "localhost";
	private int portNumber = 80;*/
	private String folderName;
	//private String clientProperties;
	//private long lastSync;
	private String serverName;
	
	//Map<String, Integer>backupServers=new HashMap<String, Integer>();
	
	private String fileData;
	private int status;
	private String [] serverList;
	
	
	
	public ServerToServerClient(String serverName, String fileData, String[] serverList,int status) {
		System.out.println("S2SClient | server name:"+serverName);
		this.serverName = serverName;
		this.fileData=fileData;
		this.status=status;
		folderName = serverName + "_Folder/";
		this.serverList = serverList;
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
		sendFiles();
		spawnServerToCoordinatorClient();
	}
	
	private void spawnServerToCoordinatorClient() {
		ServerToCoordinatorClient serverToCoordinatorClient =new ServerToCoordinatorClient(serverName);
		serverToCoordinatorClient.setFileData(fileData);
		serverToCoordinatorClient.start();
		
	}
	
	private void sendFiles() {
		if(serverList!=null) {
			for(String server: serverList) {
				/*String [] serverInfo=server.split("-");
				hostName=serverInfo[0];
				portNumber=Integer.parseInt(serverInfo[1]);*/
				S2SClientSender cs;
				if (status!=MasterlistEntry.STATUS_DELETED) 
					cs = new S2SClientSender(false);
				else
					cs = new S2SClientSender(true);
				
				System.out.println("ServerToServerClient: " + fileData);
				cs.setFileData(fileData);//we only put the file in a list to avoid modifying ClientSender
				cs.setFolderName(folderName);
				cs.setServer(server);
				cs.run();
			}
		}
			//TODO get confirmation of successful sync; this may require modifying ClientSender
		//}
	}
	
}

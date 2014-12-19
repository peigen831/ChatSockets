package coordinator_version;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientDeleter extends Thread {
	
	private List<String> fileList;
	private String name;
	
    public ClientDeleter(String name) {
    	this.name = name;
    }
    
	@Override
	public void run() {
		while (!fileList.isEmpty()) {
			List<String> tempFileList = new ArrayList<>(fileList);
			for (String fileListItem : tempFileList) {
				String[] fileArray = fileListItem.split("\\|");
				String fileName = fileArray[0];
				String servers = fileListItem.replace(fileName + "|", "");
				Deleter deleter = new Deleter();
				deleter.setFileName(fileName);
				System.out.println(name + ":: " + fileListItem);
				System.out.println(name + ":: " + "File Name: " + fileName);
				//for (int i = 1; i < fileArray.length; i++) {
					String[] serverIp = fileArray[1].split(":");
					String hostName = serverIp[0];
					int portNumber = Integer.parseInt(serverIp[1]);
					servers = servers.replace(fileArray[1] + "|", "");
					deleter.setServerAddress(hostName, portNumber);
					deleter.setBackupServers(servers);
					deleter.run();
					if (!deleter.isConnectionSuccessful()) {
						break;
					}
				//}
				System.out.println(name + ":: " + "Deleter Servers: " + servers);
				if (deleter.isFileDeleted() || servers.equals("")) {
					fileList.remove(fileListItem);
				}
			}
		}
	}
	
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
	
	class Deleter {
		
		private String hostName;
		private int portNumber;
		
		private String filename;
		private String servers;
		private boolean isFileDeleted;
		private boolean connectionSuccess;
		
		private Socket socket;
	    private PrintWriter outputToServer;
	    private BufferedReader inputFromServer;
		
		public Deleter() {
	    	connectionSuccess = false;
	    	isFileDeleted = false;
	    }
	    
		public void run() {
			connectToServer();
			setupStreams();
			tellToDeleteFile();
			closeConnection();
		}
		
		private void connectToServer() {
			try {
				System.out.println(name + ":: DELETER:" + "Connecting to " + hostName + ":" + portNumber);
				socket = new Socket(hostName, portNumber);
				connectionSuccess = true;
			}catch(Exception e) {
				connectionSuccess = false;
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
		
		private void tellToDeleteFile() {
			System.out.println(name + ":: " + "DELETE: " + filename);
			outputToServer.println("DELETE\n" + filename + "|" + servers);
			try {
				String reply = inputFromServer.readLine();
				if (reply.equals("DELETED")) {
					isFileDeleted = true;
				}
			}
			catch (Exception e) {
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
		
		public void setServerAddress(String hostName, int portNumber) {
	    	this.hostName = hostName;
	    	this.portNumber = portNumber;
		}
		
		public void setFileName(String filename) {
			this.filename = filename;
		}
		
		public void setBackupServers(String servers) {
			this.servers = servers.replace(":", "-");
		}
		
		public boolean isFileDeleted() {
			return isFileDeleted;
		}
		
		public boolean isConnectionSuccessful() {
			return connectionSuccess;
		}
	}
	
}

package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientDeleter extends Thread {
	
	private List<String> fileList;
	private String folderName;
	
    public ClientDeleter() {}
    
	@Override
	public void run() {
		while (!fileList.isEmpty()) {
			List<String> tempFileList = new ArrayList<>(fileList);
			for (String fileListItem : tempFileList) {
				String[] fileArray = fileListItem.split("|");
				String fileName = fileArray[0];
				String servers = fileListItem.replace(fileName + "|", "");
				Deleter deleter = new Deleter();
				deleter.setFilepath(folderName + fileName);
				for (int i = 1; i < fileArray.length; i++) {
					String[] serverIp = fileArray[i].split(":");
					String hostName = serverIp[0];
					int portNumber = Integer.parseInt(serverIp[1]);
					servers = servers.replace(fileArray[i] + "|", "");
					deleter.setServerAddress(hostName, portNumber);
					deleter.setBackupServers(servers);
					deleter.start();
					
					try {
						deleter.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!deleter.isConnectionSuccessful()) {
						break;
					}
				}
				if (deleter.isFileDeleted()) {
					fileList.remove(fileName);
				}
			}
		}
	}
	
	public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}
	
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	class Deleter extends Thread {
		
		private String hostName;
		private int portNumber;
		
		private File file;
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
	    
		@Override
		public void run() {
			connectToServer();
			setupStreams();
			tellToDeleteFile();
			closeConnection();
		}
		
		private void connectToServer() {
			try {
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
			outputToServer.println("DELETE\n" + file.getName() + "|" + file.length() + "|" + servers);
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
		
		public void setFilepath(String filepath) {
			file = new File(filepath);
		}
		
		public void setBackupServers(String servers) {
			this.servers = servers;
		}
		
		public boolean isFileDeleted() {
			return isFileDeleted;
		}
		
		public boolean isConnectionSuccessful() {
			return connectionSuccess;
		}
	}
	
}

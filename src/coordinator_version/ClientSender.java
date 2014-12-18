package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientSender extends Thread {
	
	private List<String> fileList;
	private String folderName;
	
    public ClientSender() {}
    
	@Override
	public void run() {
		while (!fileList.isEmpty()) {
			List<String> tempFileList = new ArrayList<>(fileList);
			for (String fileListItem : tempFileList) {
				String[] fileArray = fileListItem.split("\\|");
				String fileName = fileArray[0];
				String servers = fileListItem.replace(fileName + "|", "");
				Sender sender = new Sender();
				sender.setFilepath(folderName + fileName);
				System.out.println("SENDER: " + fileListItem);
				System.out.println("SENDER: File Name: " + fileName);
				for (int i = 1; i < fileArray.length; i++) {
					String[] serverIp = fileArray[i].split(":");
					String hostName = serverIp[0];
					int portNumber = Integer.parseInt(serverIp[1]);
					servers = servers.replace(fileArray[i] + "|", "");
					sender.setServerAddress(hostName, portNumber);
					sender.setBackupServers(servers);
					sender.run();
					if (sender.isConnectionSuccessful()) {
						break;
					}
				}
				System.out.println("SENDER: Servers: " + servers);
				if (sender.isReceivedFileCorrect() || servers.isEmpty()) {
					fileList.remove(fileListItem);
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
	
	class Sender {
		
		private String hostName;
		private int portNumber;
		
		private File file;
		private long receivedFileSize;
		private String servers;
		private boolean connectionSuccess;
		
		private Socket socket;
	    private PrintWriter outputToServer;
	    private BufferedReader inputFromServer;
		
		public Sender() {
	    	connectionSuccess = false;
	    }
	    
		public void run() {
			connectToServer();
			setupStreams();
			sendFile();
			closeConnection();
			
			connectToServer();
			setupStreams();
			requestFileSize();
			closeConnection();
		}
		
		private void connectToServer() {
			try {
				System.out.println("SENDER: Connecting to " + hostName + ":" + portNumber);
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
		
		private void sendFile() {
			System.out.println("SENDER: GIVE: " + file.getName() + "|" + file.length());
			outputToServer.println("GIVE\n" + file.getName() + "|" + file.length() + "|" + servers);
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
		
		private void requestFileSize() {
			outputToServer.println("GET_SIZE\n" + file.getName());
			try {
				String input = inputFromServer.readLine();
				receivedFileSize = Long.parseLong(input);
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
			this.servers = servers.replace(":", "-");
		}
		
		public boolean isReceivedFileCorrect() {
			System.out.println("RECEIVER: " + file.length());
			if (file.length() == receivedFileSize)
				return true;
			return false;
		}
		
		public boolean isConnectionSuccessful() {
			return connectionSuccess;
		}
	}
	
}

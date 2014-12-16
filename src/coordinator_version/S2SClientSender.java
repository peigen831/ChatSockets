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

public class S2SClientSender extends Thread {
	
	private String fileData;
	private String folderName;
	private boolean toDelete=false;
	
    public S2SClientSender() {}
    public S2SClientSender(boolean toDelete) {
    	this.toDelete=toDelete;
    }
	@Override
	public void run() {
		
				String[] fileArray = getFileData().split("|");
				String fileName = fileArray[0];
				//String servers = fileListItem.replace(fileName + "|", "");
				Sender sender = new Sender(toDelete);
				sender.setFilepath(folderName + fileName);
				for (int i = 1; i < fileArray.length; i++) {
					String[] serverIp = fileArray[i].split(":");
					String hostName = serverIp[0];
					int portNumber = Integer.parseInt(serverIp[1]);
					//servers = servers.replace(fileArray[i] + "|", "");
					sender.setServerAddress(hostName, portNumber);
					//sender.setBackupServers(servers);
					sender.start();
					
					try {
						sender.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (!sender.isConnectionSuccessful()) {
						break;
					}
				}
				if (sender.isReceivedFileCorrect()) {
					//fileList.remove(fileName);
				}
			
		
	}
	
	/*public void setFileList(List<String> fileList) {
		this.fileList = fileList;
	}*/
	
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}
	
	public String getFileData() {
		return fileData;
	}

	public void setFileData(String fileData) {
		this.fileData = fileData;
	}

	class Sender extends Thread {
		
		private String hostName;
		private int portNumber;
		
		private File file;
		private long receivedFileSize;
		private boolean connectionSuccess;
		
		private Socket socket;
	    private PrintWriter outputToServer;
	    private BufferedReader inputFromServer;
	    private boolean toDelete=false;
		
		public Sender() {
	    	connectionSuccess = false;
	    }
		public Sender(boolean toDelete) {
	    	connectionSuccess = false;
	    	this.toDelete=toDelete;
	    }
	    
		@Override
		public void run() {
			connectToServer();
			setupStreams();
			if(!toDelete)
				sendFile();
			else notifyDeleteFile();
			closeConnection();
			
			connectToServer();
			setupStreams();
			requestFileSize();
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
		
		private void sendFile() {
			System.out.println("GIVE_BACKUP " + file.getName() + "|" + file.length());
			outputToServer.println("GIVE_BACKUP\n" + file.getName() + "|" + file.length());
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
		
		private void notifyDeleteFile()
		{
			outputToServer.println("DELETE_BACKUP\n"+fileData);
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
		
		
		
		public boolean isReceivedFileCorrect() {
			if (file.length() == receivedFileSize)
				return true;
			return false;
		}
		
		public boolean isConnectionSuccessful() {
			return connectionSuccess;
		}
	}
	
}

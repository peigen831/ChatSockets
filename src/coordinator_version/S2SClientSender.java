package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class S2SClientSender extends Thread {
	
	private String fileData;
	private String folderName;
	private boolean toDelete=false;
	private String server;
	
    public S2SClientSender() {}
    public S2SClientSender(boolean toDelete) {
    	this.toDelete=toDelete;
    }
    
    public void setServer(String server) {
    	this.server = server;
    }
    
	@Override
	public void run() {
		boolean updateSuccessful=false;
		while(!updateSuccessful){
				String[] fileArray = getFileData().split("\\|");
				String fileName = fileArray[0];
				//String servers = fileListItem.replace(fileName + "|", "");
				Sender sender = new Sender(toDelete);
				sender.setFilepath(folderName + fileName);
				System.out.println("S2S filedata: " + fileData);
				System.out.println("S2S filename: " + fileName);
				String[] serverIp = server.split("-");
				String hostName = serverIp[0];
				int portNumber = Integer.parseInt(serverIp[1]);
				//servers = servers.replace(fileArray[i] + "|", "");
				sender.setServerAddress(hostName, portNumber);
				//sender.setBackupServers(servers);
				sender.run();
				if (!toDelete&&sender.isReceivedFileCorrect()||toDelete&&sender.isFileDeleted()) {
					updateSuccessful=true;
				}
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

	class Sender {
		
		private String hostName;
		private int portNumber;
		
		private File file;
		private long receivedFileSize;
		private boolean connectionSuccess;
		
		private Socket socket;
	    private PrintWriter outputToServer;
	    private BufferedReader inputFromServer;
	    private boolean toDelete=false;
	    private boolean isFileDeleted=false;
		
		public Sender() {
	    	connectionSuccess = false;
	    }
		public Sender(boolean toDelete) {
	    	connectionSuccess = false;
	    	this.toDelete=toDelete;
	    }
	    
		public void run() {
			connectToServer();
			setupStreams();
			if(!toDelete)
				sendFile();
			else notifyDeleteFile();
			closeConnection();
			
			if(!toDelete)
			{
				connectToServer();
				setupStreams();
				requestFileSize();
				closeConnection();
			}
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
				System.out.println(server+":: S2SClientSender: NOTIFY DELETE BACKUP "+ file.getName() + "|" + file.length());
				outputToServer.println("DELETE_BACKUP\n" + file.getName() + "|" + file.length() );
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
		
		public boolean isFileDeleted()
		{
			return isFileDeleted;
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

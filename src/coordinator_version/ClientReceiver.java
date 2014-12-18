package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientReceiver extends Thread {
	
	private List<String> fileList;
	private String folderName;
	
    public ClientReceiver() {}
	
	@Override
	public void run() {
		while (!fileList.isEmpty()) {
			List<String> tempFileList = new ArrayList<>(fileList);
			for (String fileListItem : tempFileList) {
				String[] fileArray = fileListItem.split("\\|");
				String fileName = fileArray[0];
				Receiver receiver = new Receiver();
				receiver.setFilepath(folderName, fileName);
				System.out.println("RECEIVER: " + fileListItem);
				System.out.println("RECEIVER: File Name: " + fileName);
				for (int i = 1; i < fileArray.length; i++) {
					System.out.println(fileArray[i]);
					String[] serverIp = fileArray[i].split(":");
					String hostName = serverIp[0];
					int portNumber = Integer.parseInt(serverIp[1]);
					receiver.setServerAddress(hostName, portNumber);
					receiver.run();
				}
				if (receiver.isReceivedFileCorrect() || fileArray.length == 1) {
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
	
	class Receiver {
		
		private String hostName;
		private int portNumber;
		
		private String folderName;
		private String filename;
		private long fileSizeToReceive;
		private boolean connectionSuccess;
		
		private Socket socket;
	    private PrintWriter outputToServer;
	    private BufferedReader inputFromServer;
		
		public Receiver() {
			connectionSuccess = false;
	    }
		
		public void run() {
			connectToServer();
			setupStreams();
			requestFile();
			closeConnection();
		}
		
		private void connectToServer() {
			try {
				System.out.println("RECEIVER: Connecting to " + hostName + ":" + portNumber);
				socket = new Socket(hostName, portNumber);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private void requestFile() {
			System.out.println("RECEIVER: GET: " + filename);
			outputToServer.println("GET\n" + filename);
			
			String filedata = null;
			
			try {
				filedata = inputFromServer.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println(filedata);
			if(!filedata.equals("NOT_FOUND"))
				receiveFile(filedata);
		}
		
		private void receiveFile(String filedata){
			String[] arrStrFile = filedata.split("\\|");
			System.out.println("RECEIVER: Receive " + filedata);
			fileSizeToReceive = Long.parseLong(arrStrFile[1]);
			
			try {
				InputStream in = socket.getInputStream();
				FileOutputStream fos = new FileOutputStream(folderName + "_" + filename);
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
			File file = new File(folderName + "_" + filename);
			if (file.exists()) {
				if (file.length() == fileSizeToReceive) {
					File newFile = new File(folderName + filename);
					if (newFile.exists()) {
						newFile.delete();
					}
					file.renameTo(newFile);
				}
				file.delete();
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
		
		public void setServerAddress(String hostName, int portNumber) {
	    	this.hostName = hostName;
	    	this.portNumber = portNumber;
		}
		
		public void setFilepath(String folderName, String filename) {
			this.folderName = folderName;
			this.filename = filename;
		}
		
		public boolean isReceivedFileCorrect() {
			File file = new File(folderName + filename);
			if (file.length() == fileSizeToReceive)
				return true;
			return false;
		}
		
		public boolean isConnectionSuccessful() {
			return connectionSuccess;
		}
	}
	
}

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
	
	private String hostName;
	private int portNumber;
	
	private List<String> fileList;
	private String folderName;
	
    public ClientReceiver(String hostName, int portNumber) {
    	this.hostName = hostName;
    	this.portNumber = portNumber;
    }
	
	@Override
	public void run() {
		while (!fileList.isEmpty()) {
			List<String> tempFileList = new ArrayList<>(fileList);
			for (String filename : tempFileList) {
				Receiver receiver = new Receiver(hostName, portNumber);
				receiver.setFilepath(folderName, filename);
				receiver.start();
				try {
					receiver.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (receiver.isReceivedFileCorrect()) {
					fileList.remove(filename);
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
	
	class Receiver extends Thread {
		
		private String hostName;
		private int portNumber;
		
		private String folderName;
		private String filename;
		private long fileSizeToReceive;
		
		private Socket socket;
	    private PrintWriter outputToServer;
	    private BufferedReader inputFromServer;
		
		public Receiver(String hostName, int portNumber) {
	    	this.hostName = hostName;
	    	this.portNumber = portNumber;
	    }
		
		@Override
		public void run() {
			connectToServer();
			setupStreams();
			requestFile();
			closeConnection();
		}
		
		private void connectToServer() {
			try {
				socket = new Socket(hostName, portNumber);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		private void requestFile() {
			outputToServer.println("GET\n" + filename);
			
			String filedata = null;
			
			try {
				filedata = inputFromServer.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			String[] arrStrFile = filedata.split(":");
			System.out.println("Receive " + filedata);
			fileSizeToReceive = Long.parseLong(arrStrFile[1]);
			
			try {
				InputStream in = socket.getInputStream();
				FileOutputStream fos = new FileOutputStream(folderName + "_" + arrStrFile[0]);
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
			System.out.println("Received file size " + file.length());
			if (isReceivedFileCorrect()) {
				if (file.exists())
					file.renameTo(new File(folderName + arrStrFile[0]));
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
		
		public void setFilepath(String folderName, String filename) {
			this.folderName = folderName;
			this.filename = filename;
		}
		
		public boolean isReceivedFileCorrect() {
			File file = new File(folderName + "_" + filename);
			if (!file.exists()) {
				file = new File(folderName + filename);
			}
			if (file.length() == fileSizeToReceive)
				return true;
			return false;
		}
		
	}
	
}

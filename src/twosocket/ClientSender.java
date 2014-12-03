package twosocket;

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
	
	private String hostName;
	private int portNumber;
	
	private List<String> fileList;
	private String folderName;
	
    public ClientSender(String hostName, int portNumber) {
    	this.hostName = hostName;
    	this.portNumber = portNumber;
    }
    
	@Override
	public void run() {
		while (!fileList.isEmpty()) {
			List<String> tempFileList = new ArrayList<>(fileList);
			for (String filename : tempFileList) {
				Sender sender = new Sender(hostName, portNumber);
				sender.setFilepath(folderName + filename);
				sender.start();
				try {
					sender.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (sender.isReceivedFileCorrect()) {
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
	
	class Sender extends Thread {
		
		private String hostName;
		private int portNumber;
		
		private File file;
		private long receivedFileSize;
		
		private Socket socket;
	    private PrintWriter outputToServer;
	    private BufferedReader inputFromServer;
		
		public Sender(String hostName, int portNumber) {
	    	this.hostName = hostName;
	    	this.portNumber = portNumber;
	    }
	    
		@Override
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
		
		private void sendFile() {
			System.out.println("GIVE " + file.getName() + ":" + file.length());
			outputToServer.println("GIVE\n" + file.getName() + ":" + file.length());
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
		
		public void setFilepath(String filepath) {
			file = new File(filepath);
		}
		
		public boolean isReceivedFileCorrect() {
			if (file.length() == receivedFileSize)
				return true;
			return false;
		}
	}
	
}

package twosocket;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client extends Thread {
	
	private String hostName = "localhost";
	private int portNumber = 80;
	private String folderName;
	//private String clientProperties;
	//private long lastSync;
	private String clientName;
	
	private Socket socket;
	private PrintWriter outputToServer;
	private BufferedReader inputFromServer;
	
	private List<String> listToGet;
	private List<String> listToGive;
	
	public Client(String clientName) {
		this.clientName = clientName;
		folderName = clientName + "_Folder/";
		
	}
	
	@Override
	public void run() {
		connectToServer();
		
		setupStreams();
		
		sendFileDetails();
		
		getResponse();
		
		closeConnection();
		
		List<Thread> threads = new ArrayList<>();
		
		if (!listToGet.isEmpty()) {
			ClientReceiver cr = new ClientReceiver(hostName, portNumber);
			cr.setFileList(listToGet);
			cr.setFolderName(folderName);
			threads.add(cr);
			cr.run();
		}
		
		if (!listToGive.isEmpty()) {
			ClientSender cs = new ClientSender(hostName, portNumber);
			cs.setFileList(listToGive);
			cs.setFolderName(folderName);
			threads.add(cs);
			cs.run();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		//setLastSync();
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
	
	private void sendFileDetails() {
		File folder = new File(folderName);
		File[] fileList = folder.listFiles();
		StringBuilder sb = new StringBuilder();
		
		sb.append("INDEX\n");
		
		sb.append("NAME:" + clientName + "\n");
		
		for(int i = 0; i < fileList.length; i++) {
			sb.append(fileList[i].getName() + ":" + fileList[i].lastModified() + "\n");
		}
		
		try {
			sb.append("INDEX_DONE");
			outputToServer.println(sb.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void getResponse() {
		String index = null;
		boolean disconnect = false;
		listToGet = new ArrayList<>();
		listToGive = new ArrayList<>();
		do {
			try {
				index = inputFromServer.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (index != null) {
				String[] arr = index.split(":");
				switch (arr[0]) {
					case "TO_GET": listToGet.add(arr[1]); break;
					case "TO_GIVE": listToGive.add(arr[1]); break;
					case "TO_DESTROY": File file = new File(folderName + arr[1]); file.delete(); break;
					case "INDEX_DONE": disconnect = true; break;
				}
			}
		} while (index != null || !disconnect);
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
	
	public static void main(String[] args) {
		(new Client("Client1")).start();
		try{
			Thread.sleep(1000);
		}catch(Exception e){
			e.printStackTrace();
		}
		(new Client("Client2")).start();
	}
}

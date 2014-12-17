package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/*
 * Message from Coordinator to Client:
 * 		TO_GET|filename|server1IP:port|server2IP:port|...
 * 		TO_GIVE|filename|server1IP:port|server2IP:port|...
 * 
 * Message from Client to Server|
 * 		filename|server2IP:port|server3IP:port|...
 * 
 */
public class Client extends Thread {
	
	private String hostName = "localhost";
	private int portNumber = 101;
	private String folderName;
	//private String clientProperties;
	//private long lastSync;
	private String clientName;
	
	private Socket socket;
	private PrintWriter outputToServer;
	private BufferedReader inputFromServer;
	
	private List<String> listToGet;
	private List<String> listToGive;
	private List<String> listToDeleteServer;
	
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
			ClientReceiver cr = new ClientReceiver();
			cr.setFileList(listToGet);
			cr.setFolderName(folderName);
			threads.add(cr);
			cr.start();
		}
		
		if (!listToGive.isEmpty()) {
			ClientSender cs = new ClientSender();
			cs.setFileList(listToGive);
			cs.setFolderName(folderName);
			threads.add(cs);
			cs.start();
		}
		
		/*if (!listToDeleteServer.isEmpty()) {
			ClientDeleter cd = new ClientDeleter();
			cd.setFileList(listToDeleteServer);
			cd.setFolderName(folderName);
			threads.add(cd);
			cd.start();
		}*/
		
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
		listToDeleteServer = new ArrayList<>();
		do {
			try {
				index = inputFromServer.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (index != null) {
				System.out.println(index);
				String[] arr = index.split("\\|");
				String toSave = index.replace(arr[0] + "|", "");
				System.out.println(toSave);
				switch (arr[0]) {
					case "TO_GET": listToGet.add(toSave); break;
					case "TO_GIVE": listToGive.add(toSave); break;
					case "TO_DESTROY": File file = new File(folderName + arr[1]); file.delete(); break;
					case "TO_DESTROY_SERVER": listToDeleteServer.add(toSave);break;
					case "INDEX_DONE": disconnect = true; break;
				}
			}
		} while (index != null || !disconnect);
		System.out.println("DISCONNECTED");
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

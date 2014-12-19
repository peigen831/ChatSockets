package coordinator_version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;

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
			ClientReceiver cr = new ClientReceiver(clientName);
			cr.setFileList(listToGet);
			cr.setFolderName(folderName);
			threads.add(cr);
			cr.start();
		}
		
		if (!listToGive.isEmpty()) {
			ClientSender cs = new ClientSender(clientName);
			cs.setFileList(listToGive);
			cs.setFolderName(folderName);
			threads.add(cs);
			cs.start();
		}
		
		if (!listToDeleteServer.isEmpty()) {
			ClientDeleter cd = new ClientDeleter(clientName);
			cd.setFileList(listToDeleteServer);
			threads.add(cd);
			cd.start();
		}
		
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		checkConflictingFiles();
		
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
				System.out.println(clientName+":: received "+index);
				String[] arr = index.split("\\|");
				String toSave = index.replace(arr[0] + "|", "");
				System.out.println(toSave);
				File file;
				switch (arr[0]) {
					case "CONFLICT":
						String filename = toSave.split("\\|")[0];
						file = new File(folderName + filename);
						if (file.exists()) {
							String cfilename = folderName + filename + "(" + clientName + "'s_conflicted_copy)";
							File newfile = new File(cfilename);
							int counter = 1;
							while (newfile.exists()) {
								String cfilename2 = cfilename + "(" + counter + ")";
								newfile = new File(cfilename2);
							}
							file.renameTo(newfile);
							file.delete();
							
							toSave = toSave.replace(filename, newfile.getName());
						}
					case "TO_GIVE":
						listToGive.add(toSave);
						break;
					case "TO_GET":
						listToGet.add(toSave);
						break;
					case "TO_DESTROY":
						file = new File(folderName + arr[1]);
						file.delete();
						break;
					case "TO_DESTROY_SERVER":
						listToDeleteServer.add(toSave);
						break;
					case "INDEX_DONE":
						disconnect = true;
						break;
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
	
	private void checkConflictingFiles() {
		String conflictText = "(" + clientName + "'s_conflicted_copy)";
		File fileDirectory = new File(folderName);
		if (fileDirectory.exists() && fileDirectory.isDirectory()) {
			List<String> files = Arrays.asList(fileDirectory.list());
			for (String filename : files) {
				if (filename.contains(conflictText) && files.contains(filename.replace(conflictText, ""))) {
					
					// Compare file with conflicted file
					File cfile = new File(folderName + filename);
					File file = new File(folderName + filename.replace(conflictText, ""));
					
					if (cfile != null && file != null && cfile.exists() && file.exists()) {
						try {
							if (FileUtils.contentEquals(file, cfile)) {
								cfile.delete();
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		(new Client("Client1")).start();
		(new Client("Client2")).start();
	}
}

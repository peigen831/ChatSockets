package Dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;

public class Subserver extends Thread{
	
	final static int clientNewVersion = 1;
	final static int serverNewVersion = 2;
	final static int newFile = 3;
	final static int sameVersion = 4;
	
	Socket socket;
	PrintWriter output;
	BufferedReader input;
	long lastConnection = 0;
	HashMap<String, Long> filedateMap = new HashMap<>();
	HashMap<String, Long> serverFiledateMap = new HashMap<>();
	
	public Subserver(Socket clientSocket){
		this.socket = clientSocket;
	}
	
	public void run(){
		try {
			setupStreams();
			getClientFilelist();
			getServerFilelist();
			removeSameFilesFromLists();
			
			// Print remaining unique or latest files

			for(Entry<String, Long> entry : filedateMap.entrySet()) {
				System.out.println("Client unique/latest file: " + entry.getKey());
			}
			for(Entry<String, Long> entry : serverFiledateMap.entrySet()) {
				System.out.println("Server unique/latest file: " + entry.getKey());
			}
			
			requestFilesFromClient();
			//if something new on client
			//	then send request to client
			//	then get the respond from
			
			sendFilesToClient();
			//if something new on server
			//	then send the new files
			
			sendDisconnectMessage();
			//if everything is the same
			//	then send a closing respond to client
			
			closeEverything();
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}

	public void setupStreams(){
		try {
			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void getClientFilelist(){
		try {
			String str;
			String[] split;
			
			do {
				str = input.readLine();
				System.out.println(str);
				split = str.split(":");
				filedateMap.put(split[1], Long.parseLong(split[0]));
				System.out.println("Server notes that " + split[1]  + " was last modified on " + split[0]);
				
			}while(input.ready() && str != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void getServerFilelist() {
		File folder = new File("src/Dropbox/Server/");
		File[] fileList = folder.listFiles();
		
		for(File file : fileList) {
			serverFiledateMap.put(file.getName(), file.lastModified());
			System.out.println("Server's own file: " + file.getName() + " was last modified " + file.lastModified());
		}
	}
	
	private void removeSameFilesFromLists() {
		for(Entry<String, Long> entry : filedateMap.entrySet()) {
			String key = entry.getKey();
			long clientValue = entry.getValue().longValue();
			if (serverFiledateMap.containsKey(key)) {
				long serverValue = serverFiledateMap.get(key);
				if (serverValue == clientValue) {
					serverFiledateMap.remove(key);
					filedateMap.remove(key);
				}
				else if (serverValue > clientValue) {
					filedateMap.remove(key);
				}
				else {
					serverFiledateMap.remove(key);
				}
			}
		}
	}

	private void requestFilesFromClient() {
		// TODO Auto-generated method stub
		
	}

	private void sendFilesToClient() {
		// TODO Auto-generated method stub
		
	}

	private void sendDisconnectMessage() {
		// TODO Auto-generated method stub
		
	}
	
	private void closeEverything() {
		try {
			output.close();
			input.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}

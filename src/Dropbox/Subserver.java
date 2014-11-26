package Dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Subserver extends Thread{
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int SAME_VERSION = 3;
	final static int END_STATUS_CHECK = 4;
	
	Socket socket;
	PrintWriter output;
	BufferedReader input;
	
	ArrayList<String> serverUpdateList = new ArrayList<String>();
	ArrayList<String> clientUpdateList = new ArrayList<String>();

	long lastConnection = 0;
	HashMap<String, Long> filedateMap = new HashMap<>();

	
	public Subserver(Socket clientSocket){
		this.socket = clientSocket;
	}
	
	public void run(){
		try {
			setupStreams();
			getClientFilelist();

			updateFileStatus();
			

			//getServerFilelist();
			
			
			// Print remaining unique or latest files

			for(Entry<String, Long> entry : filedateMap.entrySet()) {
				System.out.println("Client unique/latest file: " + entry.getKey());
			}
			

			//sendFilesToClient();
			
			//requestFilesFromClient();
			
			closeEverything();
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}

	
	
	
	public void updateFileStatus(){
		for(Map.Entry<String, Long> entry: filedateMap.entrySet()){
			int type = Server.monitor.checkFile(entry.getKey(), entry.getValue());
			
			if(type == NEW_SERVER_VERSION)
				clientUpdateList.add(entry.getKey());
			
			else if(type == NEW_CLIENT_VERSION)
			{
				serverUpdateList.add(entry.getKey());
			}
		}
	}
	
	
	public void printMap(HashMap<String, String> map){
		for(Map.Entry<String, String> entry : map.entrySet()){
		    System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue());
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

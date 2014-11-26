package Dropbox;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Subserver extends Thread{
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int SAME_VERSION = 3;
	final static int END_STATUS_CHECK = 4;
	
	Socket socket;
	PrintWriter output;
	BufferedReader input;
	HashMap<String, String> filedateMap = new HashMap<String, String>();
	ArrayList<String> serverUpdateList = new ArrayList<String>();
	ArrayList<String> clientUpdateList = new ArrayList<String>();
	
	public Subserver(Socket clientSocket){
		this.socket = clientSocket;
	}
	
	
	public void run(){
		try
		{
			setupStreams();
			getClientFilelist();
			updateFileStatus();
			//updateFiles();
			//if something new on client
			//	then send request to client
			//	then get the respond from
			//if something new on server
			//	then send the new files
			//if everything is the same
			//	then send a closing respond to client
			
			closeEverything();
		}catch(Exception e ){
			e.printStackTrace();
		}
	}
	
	
	
	public void updateFileStatus(){
		for(Map.Entry<String, String> entry: filedateMap.entrySet()){
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
	
	
	private void setupStreams(){
		try
		{
			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void getClientFilelist(){
		try 
		{
			String str;
			String[] split;
			
			do {
				
				str = input.readLine();
				System.out.println(str);
				split = str.split(" ");
				filedateMap.put(split[0], split[1]);
				System.out.println("Server side: " + split[0]  + " " + split[1]);
				
			}while(input.ready() && str != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeEverything()
	{
		try 
		{
			output.close();
			input.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}

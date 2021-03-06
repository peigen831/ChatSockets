package Dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Subserver extends Thread{
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int END_SYNCHRONIZE = 3;
	final static String END_SERVER_REQUEST = "end server request";
	
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
			
			getFiledataFromClient();

			verifyToUpdateFileStatus();
			
			//updateClientFile();
			
			//updateServerFile();
			
			closeEverything();
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
	
	public void updateServerFile(){
		String type = NEW_CLIENT_VERSION + "\n";
		
		
		for(int i = 0; i < serverUpdateList.size(); i++)
		{
			output.print(type);
			output.flush();
			System.out.println("Subserver: sent request type");
			output.print(serverUpdateList.get(i) + "\n");
			output.flush();
			System.out.println("Subserver: sent requested filename");
			try {
				Server.monitor.updateServerFile(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		//end of requesting files
		//output.print(END_SERVER_REQUEST);
	}
	
	public void sendFileRequestToClient (){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < serverUpdateList.size(); i++)
		{
			sb.append(serverUpdateList.get(i) + "\n");
		}
		
		sb.append(END_SERVER_REQUEST +"\n");;
		
		try{
			output.print(sb.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	public void updateClientFile(){
		int type = NEW_SERVER_VERSION;
		
		for(int i = 0; i < clientUpdateList.size(); i++)
		{
			output.println(type);
			try {
				Server.monitor.sendFile(socket.getOutputStream(), clientUpdateList.get(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void verifyToUpdateFileStatus(){
		//lacking: when server has a file that client doesn't have
		
		clientUpdateList = Server.monitor.getClientToUpdateList(filedateMap);
		System.out.println("Client to update list: ");
		printList(clientUpdateList);
		
		serverUpdateList = Server.monitor.getServerToUpdateList(filedateMap);
		System.out.println("Server to update list: ");
		printList(serverUpdateList);
		
	}
	
	public void printList(ArrayList<String> list){
		for(int i =0 ; i<list.size(); i++)
			System.out.println(list.get(i));
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
	
	public void getFiledataFromClient(){
		try {

			String str;
			String[] split;
			
			do {
				str = input.readLine();
				if(!str.equals(""))
				{
					split = str.split(" ");
					filedateMap.put(split[0], Long.parseLong(split[1]));
				}
				
			}while(input.ready() && str != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void closeEverything() {
		try {
			output.println(END_SYNCHRONIZE);
			output.close();
			input.close();
			socket.close();
			System.out.println("Subserver: close everything");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

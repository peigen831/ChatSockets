package Dropbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

public class Subserver extends Thread{
	
	final static int clientNewVersion = 1;
	final static int serverNewVersion = 2;
	final static int sameVersion = 3;
	Socket socket;
	PrintWriter output;
	BufferedReader input;
	HashMap<String, String> filedateMap = new HashMap<String, String>();
	
	public Subserver(Socket clientSocket){
		this.socket = clientSocket;
	}
	
	
	public void run(){
		try
		{
			setupStreams();
			getClientFilelist();
			
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
	
	public void setupStreams(){
		try
		{
			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void getClientFilelist(){
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

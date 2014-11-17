package WebServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class WebServer {
	
	static final String ONE_MB = "one.html";

	private ServerSocket server;
	
	public static String oneMB = "";
	
	public WebServer(){
		
		loadFile();
		
		startServer();
		
	}
	
	private void loadFile(){
		BufferedReader br = null;
		
		try {
			FileReader reader = new FileReader("src/WebServer/one.html");
			br = new BufferedReader(reader);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
	    try 
	    {
	    	StringBuilder sb = new StringBuilder();
	    	String temp;
	        while ((temp = br.readLine()) != null)
	        {	
	        	sb.append(temp);
	            sb.append(System.lineSeparator());
	        }
	        
	        oneMB = sb.toString();
	        
			br.close();
			
	    }catch(Exception e){
	    	System.out.println("IO Error");
	    }
	}
	
	private void startServer(){
		try 
		{
			server = new ServerSocket(80, 1000, InetAddress.getByName("localhost"));
		} catch (UnknownHostException e1) {
			System.out.println("Unknown host");
			e1.printStackTrace();
			
		} catch (IOException e1) {
			System.out.println("IO Exception");
		}
		
		while(true)
		{
			try
			{
				System.out.println("Wait connected");
				Socket clientSocket = server.accept();
				System.out.println("Someone connected");
				Subserver subserver = new Subserver(clientSocket);
				subserver.start();
				
			}catch(Exception e){
				System.out.println("Error occure wait for connection and setup streams.");
			}
		}
	}
	
	public static String getFile(String fileName){

		if(fileName.equals(ONE_MB)){
			return oneMB;
		}
		
		else return "";
	}
	
	
	public static void main(String args[])
	{
		WebServer server = new WebServer();
	}
}

package WebServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class WebServer {

	private ServerSocket server;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	
	public WebServer(){


		try {
			server = new ServerSocket(8080, 10, InetAddress.getByName("localhost"));
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
				waitForConnection();
				setupStreams();
				getFromBrowser();
				respondToBrowser();
				closeEverything();
				
			}catch(Exception e){
				System.out.println("Error occure wait for connection and setup streams.");
			}
			
			System.out.println("Disconnected");
		}
	}
	
	private void waitForConnection(){
		try
		{	
			System.out.println("Start waiting for connection");
			socket = server.accept();
			System.out.println("Someone connected");
			
		}catch(Exception e){
			System.out.println("Setup server socket failed.");
		}	
	}
	
	//setup stream to send and receive data
	private void setupStreams() throws IOException{
		
		try
		{
			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(Exception e){
			System.out.println("Error setup streams");
		}
		//should add input streams for getting request(get) from browser 
		//input = new ObjectInputStream(socket.getInputStream());
		

	}
			
	private void getFromBrowser(){
		
		try 
		{
			String str = "";
			
			while(!(str = input.readLine()).equals(null))
				System.out.println("String from browser: " + str);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void respondToBrowser(){
		//output.print("GET/HTTP/1.1");
		output.println(
				"<html>"
				+ "HELLO"
				+ "</html>");
		output.println("");
		
		System.out.println("Message send");
		
	}
	
	private void closeEverything()
	{
		try {
			output.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("Close failed");
		}
	}
	
	public static void main(String args[])
	{
		WebServer server = new WebServer();
		
		
	}
}

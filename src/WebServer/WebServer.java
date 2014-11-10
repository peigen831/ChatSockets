package WebServer;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class WebServer {

	private ServerSocket server;
	private Socket socket;
	private PrintWriter output;
	//private ObjectOutputStream output;
	
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
				writeSomething();
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
		
		//output = new ObjectOutputStream(socket.getOutputStream());
		output = new PrintWriter(socket.getOutputStream(), true);
		output.flush();
		
		//should add input streams for getting request(get) from browser 
		//input = new ObjectInputStream(socket.getInputStream());
		

	}
			
	private void writeSomething(){
		//output.print("GET/HTTP/1.1");
		output.println(
				"<html>"
				+ "HELLO"
				+ "</html>");
		output.println("");
		
		System.out.println("message send");
		
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

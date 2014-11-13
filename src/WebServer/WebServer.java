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

	private ServerSocket server;
	
	public WebServer(){
		try 
		{
			server = new ServerSocket(80, 10, InetAddress.getByName("localhost"));
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
				Socket clientSocket = server.accept();
				
				Subserver subserver = new Subserver(clientSocket);
				subserver.start();
				//waitForConnection();
				
			}catch(Exception e){
				System.out.println("Error occure wait for connection and setup streams.");
			}
			
			System.out.println("Disconnected");
		}
	}
	/*
	private void waitForConnection(){
		try
		{	
			socket = server.accept();
			
		}catch(Exception e){
			System.out.println("Setup server socket failed.");
		}	
	}
	*/
	
	
	
	public static void main(String args[])
	{
		WebServer server = new WebServer();
	}
}

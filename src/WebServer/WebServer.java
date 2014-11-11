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
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private String getRequest;
	
	public WebServer(){
		try 
		{
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
		

	}
			
	private void getFromBrowser(){
		
		try 
		{
			getRequest = input.readLine();
			
			System.out.println(getRequest);
			
			String str;
			
			while(!(str = input.readLine()).equals(""))
			{
				System.out.println(str);
			}
			
			System.out.println("Done receiving from browser");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public String parseGetRequest(){
		String result;
		
		result = getRequest.split(" ")[1];
		
		result = result.substring(1, result.length());
		
		return result;
		
	}
	
	public String getFileContent(String filePath) throws IOException
	{
		FileReader reader = new FileReader("src/WebServer/" + filePath);
		BufferedReader br = new BufferedReader(reader);
		String result = "";
		
	    try 
	    {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) 
	        {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        
	        result = sb.toString();
	        
	    }catch(Exception e){
	    	System.out.println("IO Error");
	    	
	    }finally {
			br.close();
	    }
	    
	    return result;
	}
	
	private void respondToBrowser() throws IOException{
		
		String fileName = parseGetRequest();
		
		String fileContent = getFileContent(fileName);
		
		try
		{
			output.println(fileContent);
			
			System.out.println("Message send");
			
		}catch(Exception e){
			System.out.println("respond to browser failed");
		}
	}
	
	private void closeEverything()
	{
		try 
		{
			output.close();
			input.close();
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

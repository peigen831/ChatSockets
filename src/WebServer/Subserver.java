package WebServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Subserver extends Thread{
	
	Socket socket;
	PrintWriter output;
	BufferedReader input;
	String getRequest;
	
	public Subserver(Socket clientSocket){
		this.socket = clientSocket;
	}
	
	public void run(){
		try
		{
			setupStreams();
			getFromBrowser();
			respondToBrowser();
			closeEverything();
			//System.out.println("END OF SUBSERVER");
		}catch(Exception e ){
			System.out.println("Subserver mess up");
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
				String str;
				do {
					str = input.readLine();
					//Syste
				}while(input.ready() && str != null);
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
			//String fileContent = getFileContent(fileName);
			
			try
			{
				if(fileName.equals("one.html"))
					output.println(WebServer.oneMB);
				
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
				System.out.println("Disconnected");
			} catch (IOException e) {
				System.out.println("Close failed");
			}
		}
	
	
}

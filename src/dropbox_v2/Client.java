package dropbox_v2;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class Client extends Thread{
	static final String UPDATE_CLIENT = "UPDATE CLIENT";
	static final String UPDATE_SERVER = "UPDATE SERVER";
	static final String END_OF_FILE = "END OF FILE";
	static final String END_OF_UPDATE_LIST = "END OF UPDATE LIST";
	static final String CLOSE_CONNECTION = "CLOSE CONNECTION";
	
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private String folderName;
	
	public Client(String folderName){
		this.folderName = folderName;
	}
	
	public void run(){
		connectToServer();
		
		setupStreams();
		
		sendFiledataToServer();
		
		synchronizeFile();
		
		closeEverything();
	}
	
	
	public void synchronizeFile(){
		String request;
		
		do{
			request = getRequestType();
			System.out.println("Client: Respond Type - " + request);
			
			if(request.equals(UPDATE_SERVER))
			{
				//String filename = receiveToUpdateFilename();
				
				//sendFileToServer(filename);
				
			}
			
			else if(request.equals(UPDATE_CLIENT))
			{
				//String[] metadata = getFileMetadata();
				
				receiveFile();
			}
			
		}while(!request.equals(CLOSE_CONNECTION));
	}
	
	public void receiveFile(){
		try
		{
			int nFile = Integer.parseInt(input.readLine());
			
			while(nFile > 0)
			{
				//create file
				//get filename
				//get content until END OF FILE
				//nFile--
			}
					
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	public 	String getRequestType(){
		String requestType = null;
		try 
		{
			 requestType = input.readLine();
				
		}catch(Exception e) {
			e.printStackTrace();
		}
		return requestType;
	}
	
	private void sendFiledataToServer(){
		File folder = new File("src/Dropbox/" + folderName);
		File[] fileList = folder.listFiles();
		StringBuilder sb = new StringBuilder();
		
		for(int i = 0; i < fileList.length; i++)
		{
			sb.append(fileList[i].getName() + " " + fileList[i].lastModified());
			if(i < fileList.length-1)
				sb.append("\n");
		}
		
		try
		{
			output.println(sb.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void closeEverything() {
		try {
			output.close();
			input.close();
			socket.close();
			System.out.println("Client: Terminated");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void setupStreams() {
		try {
			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void connectToServer(){
		try
		{
			socket = new Socket("localhost", 80);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Client client1 = new Client("Client1File");
		
		client1.start();
	}
}

package Dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int SAME_VERSION = 3;
	final static int END_STATUS_CHECK = 4;
	
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private String folderName;
	
	public Client(String folderName){
		this.folderName = folderName;
	}
	
	private void connectToServer(){
		try
		{
			socket = new Socket("localhost", 80);
			
		}catch(Exception e){
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
	
	private void sendFiledate(){
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
	
	public void run(){
		connectToServer();
		
		setupStreams();
		
		sendFiledate();
		
		//getRespond();
	}

	public static void main(String[] args) {
		Client client1 = new Client("Client1File");
		
		client1.start();
	}
}

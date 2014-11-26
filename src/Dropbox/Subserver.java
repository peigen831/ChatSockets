package Dropbox;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import WebServer.WebServer;

public class Subserver extends Thread{
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int END_SYNCHRONIZE = 3;
	
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
			getClientFilelist();

			updateFileStatus();
			
			updateClient();
			//getServerFilelist();
			
			//sendFilesToClient();
			
			//requestFilesFromClient();
			
			closeEverything();
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}

	
	public void updateClient(){
		int type = NEW_SERVER_VERSION;
		
		output.println(type);
		for(int i = 0; i < clientUpdateList.size(); i++)
		{
			try {
				Server.monitor.sendFile(socket.getOutputStream(), clientUpdateList.get(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateFileStatus(){
		for(Map.Entry<String, Long> entry: filedateMap.entrySet()){
			int type = Server.monitor.checkFile(entry.getKey(), entry.getValue());
			
			if(type == NEW_SERVER_VERSION)
				clientUpdateList.add(entry.getKey());
			
			else if(type == NEW_CLIENT_VERSION)
			{
				serverUpdateList.add(entry.getKey());
			}
		}
		System.out.println("Server: finish update status");
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
	
	public void getClientFilelist(){
		try {

			String str;
			String[] split;
			
			do {
				str = input.readLine();
				System.out.println("Server: Receive - " + str);
				split = str.split(" ");
				filedateMap.put(split[0], Long.parseLong(split[1]));
				
			}while(input.ready() && str != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void sendFileMetadata(File file)
	{
		String filename=file.getName();
		long filesize=file.length();
		 try {
		        OutputStream os = socket.getOutputStream();
		        DataOutputStream dos = new DataOutputStream(os);
		        dos.writeUTF(filename);  
		        dos.writeLong(filesize);
		 }catch(IOException e)
		 {
			 e.printStackTrace();
		 }
		 System.out.println("Server: META DATA SENT - " + filename);
		
	}
	
	public void sendFile(File file)
	{
		byte[] fileByteArray = new byte[(int) file.length()];
	      BufferedInputStream bis=null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			System.out.println("File not found");
			
		}
	      try {
			bis.read(fileByteArray, 0, fileByteArray.length);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	      try {
	    	  OutputStream os = socket.getOutputStream();
			os.write(fileByteArray, 0, fileByteArray.length);
			os.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      System.out.println("SERVER: FILE SENT");
	}
	
	public String[] getFileMetadata()
	{
		String filename=null;
		long filesize=0;
		try{
	        InputStream in=socket.getInputStream();
	        DataInputStream dataStream = new DataInputStream(in);
	        filename = dataStream.readUTF();
	        filesize=dataStream.readLong();
	        System.out.println(filename);
		}catch(IOException e)
		 {
			 e.printStackTrace();
		 }
		String [] metadata=new String[]{filename,Long.toString(filesize)};
		return metadata;
		
	}
	
	//TODO make sure the server sends file name and size to client and vice versa
	public void receiveFile(String filename, long filesize)
	{
		byte[] mybytearray = new byte[(int) filesize];
	    InputStream is=null;
		try {
			is = socket.getInputStream();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	    FileOutputStream fos=null;
		try {
			fos = new FileOutputStream(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    int bytesRead;
	     int current=0;
		try {
			do {
			bytesRead =  is.read(mybytearray, current, (mybytearray.length-current));
	         if(bytesRead >= 0) current += bytesRead;
		 } while(bytesRead > -1);
			 bos.write(mybytearray, 0, bytesRead);
			 bos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  
	}
	
	private void closeEverything() {
		try {
			output.close();
			input.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

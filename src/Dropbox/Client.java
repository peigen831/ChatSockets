package Dropbox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client extends Thread{
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int END_SYNCHRONIZE = 3;
	
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private String folderName;
	
	public Client(String folderName){
		this.folderName = folderName;
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
		
		int respond;
		
		do{
			respond = getRespond();
			System.out.println("Client: Respond Type - " + respond);
			
			if(respond == NEW_CLIENT_VERSION)
			{
				
				//get file first
				//sendFileMetadata(file);
				//sendFile(file);
				
			}
			
			else if(respond == NEW_SERVER_VERSION)
			{
				String[] metadata = getFileMetadata();
				
				receiveFile(metadata[0], Long.parseLong(metadata[1]));
			}
			
		}while(respond != END_SYNCHRONIZE);
		
		closeEverything();
	}
	
	public int getRespond(){
		int result = 0;
		
		try 
		{
			result = Integer.parseInt(input.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	public String[] getFileMetadata()
	{
		String filename=null;
		long filesize=0;
		try{
	        InputStream in=socket.getInputStream();
	        DataInputStream dataStream = new DataInputStream(in);
	        filename = dataStream.readUTF();
	        filesize = dataStream.readLong();
	        System.out.println("Client: Filename" + filename + " Size:" + filesize);
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
				e1.printStackTrace();
			}
			
		    FileOutputStream fos=null;
			try {
				fos = new FileOutputStream(filename);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    int bytesRead;
		    int current=0;
		     
			try {
				do {
					bytesRead =  is.read(mybytearray, current, (mybytearray.length-current));
			        if(bytesRead >= 0) 
			        	 current += bytesRead;
				} while(bytesRead > -1);
				bos.write(mybytearray, 0, bytesRead);
				bos.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	
	public void sendFile(File file)
	{
		byte[] fileByteArray = new byte[(int) file.length()];
	      BufferedInputStream bis=null;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e1) {
			System.out.println("Client: File not found");
			
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

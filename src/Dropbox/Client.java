package Dropbox;

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
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int END_SYNCHRONIZE = 3;
	final static String END_SERVER_REQUEST = "end server request";
	
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
		
		//synchronizeFile();
		
		closeEverything();
	}
	
	public void synchronizeFile(){
		int request;
		do{
			request = getRequestType();
			System.out.println("Client: Respond Type - " + request);
			
			if(request == NEW_CLIENT_VERSION)
			{
				String filename = receiveToUpdateFilename();
				
				sendFileToServer(filename);
				
				//ArrayList<String> toSendlist = getUpdatelistFromServer();

				//sendAllFiles();
				//get file first
				//sendFileMetadata(file);
				//sendFile(file);
			}
			
			else if(request == NEW_SERVER_VERSION)
			{
				String[] metadata = getFileMetadata();
				
				receiveFile(metadata[0], Long.parseLong(metadata[1]));
			}
			
			else break;
			
		}while(request != END_SYNCHRONIZE);
	}
	
	public String receiveToUpdateFilename(){
		String filename = "";
		try
		{
			filename = input.readLine();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return filename;
	}

	public void sendFileToServer(String filename)
	{
		//send metadata
		File file = new File("src/Dropbox/"+ folderName + "/" + filename);
		
		long filesize = file.length();
		OutputStream os = null;
		
		try {
			os = socket.getOutputStream();
			DataOutputStream dos = new DataOutputStream(os);
		    dos.writeUTF(filename);  
		    dos.writeLong(filesize);
		 }catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 System.out.println("Client: SENT METADATA of " + filename );
		 
		 //send file 
		 byte[] mybytearray = new byte[(int) file.length()];
		 try {
		    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		    bis.read(mybytearray, 0, mybytearray.length);
		    os.write(mybytearray, 0, mybytearray.length);
		    os.flush();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		    
	    System.out.println("Client: Done sending file " + filename);
	}
	
	public ArrayList<String> getUpdatelistFromServer(){
		ArrayList<String> resultList = new ArrayList<String>();
		
		String str;
		try
		{
			do {
				str = input.readLine();
				if(!str.equals(END_SERVER_REQUEST))
					resultList.add(str);
			}while(input.ready() && !str.equals(END_SERVER_REQUEST));
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return resultList;
	}
	
	
	public int getRequestType(){
		int result = 0;
		try 
		{
			String str = input.readLine();
			result = Integer.parseInt(str);
				
		}catch(Exception e) {
			e.printStackTrace();
		}
		return result;
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
	
	public String[] getFileMetadata()
	{
		String filename=null;
		long filesize=0;
		try{
	        InputStream in=socket.getInputStream();
	        DataInputStream dataStream = new DataInputStream(in);
	        filename = dataStream.readUTF();
	        filesize = dataStream.readLong();
	        System.out.println("Client: Filename: " + filename + "; Size:" + filesize);
		}catch(IOException e)
		 {
			 e.printStackTrace();
		 }
		String [] metadata=new String[]{filename,Long.toString(filesize)};
		return metadata;
	}
	
	
	public void receiveFile(String filename, long filesize)
	{
		try {
			byte[] mybytearray = new byte[(int)filesize];
		    InputStream is = socket.getInputStream();
		    FileOutputStream fos = new FileOutputStream("src/Dropbox/" + folderName + "/" + filename);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    int bytesRead = is.read(mybytearray, 0, mybytearray.length);
		    bos.write(mybytearray, 0, bytesRead);
		    bos.flush();
		}
		catch (Exception e) {
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

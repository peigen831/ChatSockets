package Dropbox;


import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class monitor{
	final Lock lock = new ReentrantLock();
	Condition server = lock.newCondition();
	//Condition sendFile = lock.newCondition();
	boolean using = false;
	
	public int checkFile(String filename, Long lastModify){
		lock.lock();
		
		int result;
		File file = new File("src/Dropbox/Server/" + filename);
		
		Long clientFileDate = lastModify;
		Long serverFileDate = file.lastModified();
		
		if(Long.compare(serverFileDate, clientFileDate) < 0 )
			result = Subserver.NEW_CLIENT_VERSION;
		
		else
			result = Subserver.NEW_SERVER_VERSION;
		
		lock.unlock();
		
		return result;
	}
	
	public void sendFile(OutputStream os, String filename)
	{
		//send metadata
		File file = new File("src/Dropbox/Server/" + filename);
		
		long filesize = file.length();
		 try {
		        DataOutputStream dos = new DataOutputStream(os);
		        dos.writeUTF(filename);  
		        dos.writeLong(filesize);
		 }catch(IOException e)
		 {
			 e.printStackTrace();
		 }
		 System.out.println("Server: SENT METADATA of" + filename );
		 
		 //send file 
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
			e.printStackTrace();
		}
	     
	    try {
			os.write(fileByteArray, 0, fileByteArray.length);
			os.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    System.out.println("Server: Done sending file");
	}
	
	public void updateFile(){
		
	}
}

public class Server {
	

	
	private ServerSocket server;
	
	static monitor monitor = new monitor();
	
	private void startServer(){
		try 
		{
			server = new ServerSocket(80, 1000, InetAddress.getByName("localhost"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		while(true)
		{
			try
			{
				System.out.println("Wait connected");
				Socket clientSocket = server.accept();
				System.out.println("Someone connected");
				Subserver subserver = new Subserver(clientSocket);
				subserver.start();
				
			}catch(Exception e){
				System.out.println("Error occure wait for connection and setup streams.");
			}
		}
	}

	public static void main(String[] args) {
		
		Server server = new Server();
		
		server.startServer();
	}

}

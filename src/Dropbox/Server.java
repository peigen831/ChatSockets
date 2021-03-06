package Dropbox;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


class monitor{
	final Lock lock = new ReentrantLock();
	Condition server = lock.newCondition();
	//Condition sendFile = lock.newCondition();
	InputStream in;
	OutputStream out;
	boolean using = false;
	

	public void updateServerFile(InputStream inputStream){
		lock.lock();
		
		in = inputStream;
		
		String[] metadata = getFileMetadata();
		receiveFile(metadata[0], Long.parseLong(metadata[1]));
		
		lock.unlock();
	}
	
	public String[] getFileMetadata()
	{
		String filename=null;
		long filesize=0;
		try{
	        DataInputStream dataStream = new DataInputStream(in);
	        filename = dataStream.readUTF();
	        filesize = dataStream.readLong();
	        System.out.println("Server: receive metadata Filename: " + filename + "; Size:" + filesize);
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
		    FileOutputStream fos = new FileOutputStream("src/Dropbox/Server/" + filename);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    int bytesRead = in.read(mybytearray, 0, mybytearray.length);
		    bos.write(mybytearray, 0, bytesRead);
		    System.out.println("Server: received - " + filename);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getClientToUpdateList(HashMap<String, Long> filedataMap){
		lock.lock();
		
		ArrayList<String> clientToUpdateList = new ArrayList<String>();
		File folder = new File("src/Dropbox/server");
		File[] fileList = folder.listFiles();
		
		for(int i = 0; i < fileList.length; i++){
			
			String filename = fileList[i].getName();
			Long serverFiledate = fileList[i].lastModified();
			
			if(filedataMap.containsKey(fileList[i].getName()))
			{
				Long clientFiledate = filedataMap.get(filename);
				if(Long.compare(serverFiledate, clientFiledate) > 0 )
					clientToUpdateList.add(filename);
			}
			else
				clientToUpdateList.add(fileList[i].getName());
		}
		lock.unlock();
		
		return clientToUpdateList;
	}
	
	public ArrayList<String> getServerToUpdateList(HashMap<String, Long> filedataMap){
		lock.lock();
		ArrayList<String> serverToUpdateList = new ArrayList<String>();
		
		for(Map.Entry<String, Long> entry: filedataMap.entrySet()){
			String filename = entry.getKey();
			Long clientFileDate = entry.getValue();
			
			File file = new File("src/Dropbox/Server/" + filename);
			if (file.exists()) {
				Long serverFileDate = file.lastModified();
				if(Long.compare(serverFileDate, clientFileDate) < 0 )
					serverToUpdateList.add(filename);
			}
			else
				serverToUpdateList.add(filename);
			
		}
		lock.unlock();
		return serverToUpdateList;
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
		    dos.flush();
		 }catch(IOException e)
		 {
			 e.printStackTrace();
		 }
		 System.out.println("Server: SENT METADATA of" + filename );
		 
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
		    
	    System.out.println("Server: Done sending file");
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

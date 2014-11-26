package Dropbox;

import java.io.File;
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
	
	public int checkFile(String filename, String lastModify){
		lock.lock();
		
		int result;
		File file = new File("src/Dropbox/Server/" + filename );
		
		Long clientFileDate = Long.parseLong(lastModify);
		Long serverFileDate = file.lastModified();
		
		if(Long.compare(serverFileDate, clientFileDate) < 0 )
			result = Subserver.NEW_CLIENT_VERSION;
		
		else if(Long.compare(serverFileDate, clientFileDate) > 0)
			result = Subserver.NEW_SERVER_VERSION;
		
		else result = Subserver.SAME_VERSION;
		
		lock.unlock();
		
		return result;
	}
	
	public void sendFile(){
		
	}
	
	public void updateFile(){
		
	}
}

public class Server {
	

	
	private ServerSocket server;
	
	static monitor monitor = new monitor();
	
	static HashMap<String, String> filedateMap = new HashMap<String, String>();
	
	private void loadFilelist(){
		File folder = new File("src/Dropbox/Server");
		File[] fileList = folder.listFiles();
		
		for(int i = 0; i < fileList.length; i++)
		{
			filedateMap.put(fileList[i].getName(), String.valueOf(fileList[i].lastModified()));
		}
	}
	
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
		// TODO Auto-generated method stub
		Server server = new Server();
		
		
		server.loadFilelist();
		server.startServer();
	}

}

package dropbox_v2;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
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
		
		receiveFile();
		
		lock.unlock();
	}
	
	
	//TODO to be revised
	public void receiveFile()
	{
		
	}
	
	//TODO  to be test
	public void sendFile(PrintWriter output, ArrayList<String> updateList)
	{
		for(int i = 0; i < updateList.size(); i++)
		{
			output.println(updateList.get(i));
			try
			{
				FileReader fr = new FileReader("src/Dropbox/Server/" + updateList.get(i));
				BufferedReader reader = new BufferedReader(fr);
				String str;
				
				while((str = reader.readLine()) != null){
					output.println(str);
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
			
			output.println(Subserver.END_OF_FILE);
			System.out.println("Subserver: Sent " + updateList.get(i));
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

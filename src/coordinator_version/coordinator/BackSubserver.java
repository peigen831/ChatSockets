package coordinator_version.coordinator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import coordinator_version.Monitor;
import coordinator_version.Subserver;

public class BackSubserver extends Subserver{

	private String serverProperties; 
	
	public BackSubserver(Socket socket, Monitor monitor) {
		super(socket, monitor);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void parseAndRunCommand(String command) {
		System.out.println("Command: " + command);
		switch(command){
			
		}
	}
	
	private void receiveServer(String serverName )
	{
		//TODO if we use pings instead of constant connections, check if server already has a file before overwriting the existing file? Or is this unnecessary?
		
		serverProperties="src/coordinator_version/coordinator/" + serverName + ".properties";
		
		long lastHeartbeat = System.currentTimeMillis();
		try {
			Properties properties = new Properties();
			properties.setProperty("LAST_HEARTBEAT", Long.toString(lastHeartbeat));
			properties.setProperty("ADDRESS",socket.getRemoteSocketAddress().toString());
			properties.setProperty("PORT",Integer.toString(socket.getPort()));
			
			File file = new File(serverProperties);
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, null);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * get list of files on server. this method will be triggered when the server sends a certain message
	 */
	private void receiveFileList()
	{
		
	}
}

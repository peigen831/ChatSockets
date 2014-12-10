package coordinator_version.coordinator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import coordinator_version.Monitor;
import coordinator_version.Subserver;

/**
 * Serves a server connecting to the coordinator
 * @author Andrew
 *
 */
public class BackSubserver extends Subserver{

	/**
	 * Properties of the server that this BackSubserver is connected to
	 */
	private String serverProperties; 
	
	
	public BackSubserver(Socket socket, Monitor monitor) {
		super(socket, monitor);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void parseAndRunCommand(String command) {
		System.out.println("Command: " + command);
		switch (command) {
			case "INDEX": receiveFileList(); break;
			/*case "GET": giveFile(); break;
			case "GET_SIZE": getFileSize(); break;
			case "GIVE": getFile(); break;
			case "INSYNC": monitor.checkAndSetLastSync(System.currentTimeMillis()); break;*/
			default: break;
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
		
			
			//TODO for each file to be sent to the client, include the IP + port  of the relevant server
			String index = null;
			
			Map<String, Long> mapIndexFromClient = new HashMap<>();
			
			// Get server's file index
			do {
				try {
					index = inputFromClient.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (index != null) {
					if (index.equals("INDEX_DONE")) {
						break;
					}
					String[] file = index.split(":");
					mapIndexFromClient.put(file[0], Long.parseLong(file[1]));
				}
			} while (index != null);
			
			//write master list to file
			//TODO test if this writes properly; this version assumes that the server's list of files is already the master list
			File f=new File(Coordinator.FILE_MASTER_LIST);
			PrintWriter printWriter=null;
			try {
				printWriter = new PrintWriter(f);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	       
			for(Map.Entry e: mapIndexFromClient.entrySet())
			{
				printWriter.println(e.getKey()+":"+e.getValue());
			}
			printWriter.close();
			
		}	
	
}

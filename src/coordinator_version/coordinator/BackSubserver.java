package coordinator_version.coordinator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
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
	private String remoteServerName;
	private List<MasterlistEntry> masterList=new ArrayList<MasterlistEntry>();
	
	public BackSubserver(Socket socket, Monitor monitor) {
		super(socket, monitor);
	}
	
	@Override
	public void run()
	{
		loadMasterlist();
		
		setupStream();
		
		String command = getCommand();
		System.out.println("BACKSUBSERVER | "+command);
		
		
		parseAndRunCommand(command);
		receiveServer();
		
		closeEverything();
	}
	
	@Override
	protected void parseAndRunCommand(String command) {
		System.out.println("BACKSUBSERVER | Command: " + command);
		switch (command) {
			case "INDEX": receiveFileList(); break;
			case "FILE_CHANGE":receiveFile();break;
			/*case "GET": giveFile(); break;
			case "GET_SIZE": getFileSize(); break;
			case "GIVE": getFile(); break;
			case "INSYNC": monitor.checkAndSetLastSync(System.currentTimeMillis()); break;*/
			default: break;
		}
	}
	

	private void loadMasterlist()
	{

		File masterListFile = new File(Coordinator.MASTER_LIST);
		
		BufferedReader br=null;
		try {
			br = new BufferedReader(new FileReader(masterListFile));
		} catch (FileNotFoundException e1) {

			e1.printStackTrace();
		}
		
		String line;
		try {
			while ((line = br.readLine()) != null) {
				System.out.println("BACKSUBSERVER | Masterlist load line: "+line);
				if(!line.contains("#"))
				{
					String [] entry=line.split("=");
					String tempSplit[]=entry[1].split("\\|");
					String [] results=new String[tempSplit.length+1];
					results[0]=entry[0];
					for(int i=0;i<tempSplit.length;i++)
					{
						results[i+1]=tempSplit[i];
					}
				   MasterlistEntry newFile=toMasterlistEntry(results);
				   masterList.add(newFile);
				}
			   
			}
			br.close();
		} catch (NumberFormatException e1) {

			e1.printStackTrace();
		} catch (IOException e1) {

			e1.printStackTrace();
		}
	}
	private MasterlistEntry toMasterlistEntry(String [] rawInput)
	{
		MasterlistEntry entry=new MasterlistEntry(rawInput[0],Long.parseLong(rawInput[1]));
		   
		   if(rawInput.length>2)
		   {
				switch(rawInput[2])
			   {
			   case "DELETED":
				   entry.setStatus(MasterlistEntry.STATUS_DELETED);
				   break;
			   case "ADDED":
				   entry.setStatus(MasterlistEntry.STATUS_ADDED);
				   break;
			   case "UPDATED":
				   entry.setStatus(MasterlistEntry.STATUS_UPDATED);
				   break;
			   }
			   for(int i=3;i<rawInput.length;i++)
			   {
				   entry.addServer(rawInput[i]);
			   }
		   }
		   else entry.addServer(remoteServerName);
		  return entry;
	}
	private void updateMasterlist(MasterlistEntry entry)
	{
		
		

		String filename=entry.getFilename();
		boolean inList=false;
		MasterlistEntry oldEntry=null;
		for(MasterlistEntry e:masterList)
		{
			if(e.getFilename().equals(filename))
			{
				oldEntry=e;
				inList=true;
				break;
			}
		}
		if(!inList)
		{
			masterList.add(entry); //current masterlist in memory
		}
		else if (oldEntry!=null){
				oldEntry.setLastUpdate(entry.getLastUpdate());
				oldEntry.setStatus(entry.getStatus());
				oldEntry.addServer(entry.getServerList().get(0));	
		}
		updateMasterlistFile(); //masterlist on file, so that FrontSubserver can use it. Might need to be in a monitor so that FrontSubserver doesn't read while the file is being written
		
		
		//loadMasterlist();
	}
	
	private void updateMasterlistFile()
	{
		//for now, overwrite everything
				File f=new File(Coordinator.MASTER_LIST);
				PrintWriter printWriter=null;
				try {
					printWriter = new PrintWriter(f);
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				
				for(MasterlistEntry entry: masterList)
				{
					StringBuilder sb = new StringBuilder();
					
					String statusString;
					switch (entry.getStatus())
					{
						case MasterlistEntry.STATUS_DELETED:
							statusString="DELETED"; break;
						case MasterlistEntry.STATUS_ADDED:
							statusString="ADDED";break;
						case MasterlistEntry.STATUS_UPDATED:
						default:
							statusString="UPDATED"; break;
								
					}
					
					sb.append(entry.getFilename()+"="+entry.getLastUpdate()+"|"+statusString);
					for(String server: entry.getServerList())
						if(server!=null)
							sb.append("|"+server);
					printWriter.println(sb.toString());
				}
				printWriter.close();
	}
	
	private void receiveServer()
	{
		System.out.println("BACKSUBSERVER | server connected: "+remoteServerName);
		//String address=socket.getRemoteSocketAddress().toString().split(":")[0];
		
		serverProperties=Coordinator.SERVER_FOLDER + remoteServerName + ".properties";
		
		long lastHeartbeat = System.currentTimeMillis();
		try {
			Properties properties = new Properties();
			properties.setProperty("LAST_SYNC", Long.toString(lastHeartbeat));
			String [] serverAddressData = remoteServerName.split("-");
			
			properties.setProperty("ADDRESS",serverAddressData[0]);
			properties.setProperty("PORT",serverAddressData[1]);
			
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
	
	private void receiveFile() {
		
		String index = null;
		Map<String, String> mapIndexFromClient = new HashMap<>();
		try {
			String serverName=inputFromClient.readLine().split(":")[1];
			//System.out.println(serverName);
			remoteServerName=serverName;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			index = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (index != null&&!index.equals("FILE_CHANGE_DONE")) {
			System.out.println("BACKSUBSERVER | Received file change from server: "+index);
			String[] file = index.split("\\|");
			MasterlistEntry entry=toMasterlistEntry(file);
			
			//String server=socket.getRemoteSocketAddress().toString()+":"+socket.getLocalPort();
			entry.addServer(serverName);
			updateMasterlist(entry);

		}
		
	}
	
	
	/*
	 * get list of files on server. this method will be triggered when the server sends a certain message
	 */
	private void receiveFileList()
	{
		
			
			String index = null;
			
			Map<String, Long> mapIndexFromClient = new HashMap<>();
			try {
				String serverName=inputFromClient.readLine().split(":")[1];
				System.out.println(serverName);
				remoteServerName=serverName;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
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
					System.out.println("BACKSUBSERVER | Index received: "+index);
					String[] file = index.split("\\|");
					mapIndexFromClient.put(file[0], Long.parseLong(file[1]));
				}
			} while (index != null);
			
			
			for(Map.Entry<String, Long> e: mapIndexFromClient.entrySet())
			{
				//String server=socket.getRemoteSocketAddress().toString()+":"+socket.getLocalPort();
				MasterlistEntry entry=new MasterlistEntry(e.getKey(),e.getValue());
				entry.addServer(remoteServerName);
				updateMasterlist(entry);
			}
			
			
			
		}	
	
	
	
}

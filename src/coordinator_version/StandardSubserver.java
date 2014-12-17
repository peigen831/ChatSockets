package coordinator_version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import coordinator_version.coordinator.MasterlistEntry;

/**
 * Subserver of a file server
 * @author Andrew
 *
 */
public class StandardSubserver extends Subserver {
	
	private String folderName = "Server_Folder/";
	
	public StandardSubserver(String serverName, Socket socket, Monitor monitor) {
		super(socket, monitor);
		setServerName(serverName);
	}
	
	public void setServerName(String serverName) {
		folderName = serverName + "_Folder/";
	}
	
	@Override
	protected void parseAndRunCommand(String command) {
		//System.out.println("Command: " + command);
		switch (command) {
			case "GET": giveFile(); break;
			case "GET_SIZE": getFileSize(); break;
			case "GIVE": getFile(true); break;
			case "GIVE_BACKUP": getFile(false); break;
			case "DELETE": deleteFile(true); break;
			case "DELETE_BACKUP":deleteFile(false);break;
			//TODO remove
			//case "INSYNC": monitor.checkAndSetLastSync(serverPropertiesPath,  System.currentTimeMillis()); break;
			default: break;
		}
	}
	
	private void getFile(boolean fromClient) {
		String filedata = null;
		
		try {
			filedata = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] arrStrFile = filedata.split("\\|");
		
		String filename=arrStrFile[0];
		monitor.updateFile(filename);
		try {
			InputStream in = socket.getInputStream();
			FileOutputStream fos = new FileOutputStream(folderName + "_" + arrStrFile[0]);
	        int x = 0;
	        while(true){
	            x = in.read();
	            if(x == -1)
	            	break;
	            fos.write(x);
	        }
	        fos.flush();
	        fos.close();
	        in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		File file = new File(folderName + "_" + arrStrFile[0]);
		if (Long.parseLong(arrStrFile[1]) == file.length()) {
			if (file.exists())
				file.renameTo(new File(folderName + arrStrFile[0]));
				file.delete();
		}
		
		monitor.doneUpdatingFile(filename);
		if(fromClient)
		{
			//send file to backup servers
			String servers = filedata.replace(filename + "|" + arrStrFile[1] + "|", "");
			String fileDataForServers=filedata.replace(servers,"");
			//  send to backup servers	//QUESTION: does this send the name of this server as well?
			// provided: String servers = "server2IP:port|server3IP:port|..."
			String[] serverList=servers.split("\\|");
			
				
				ServerToServerClient s2sClient=new ServerToServerClient(serverName,fileDataForServers, serverList,MasterlistEntry.STATUS_UPDATED);
				s2sClient.start();
				//TODO: indicate if file has been added instead of updated (if necessary)
			
		}
		else{
			//TODO notify first server that backup file has been saved
		}
	}
	
	private void getFileSize() {
		String filename = null;
		
		try {
			filename = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File file = new File(folderName + "_" + filename);
		if (!file.exists()) {
			file = new File(folderName + filename);
		}
		outputToClient.println(file.length());
	}
	
	private void giveFile() {
		String filedata = null;
		try {
			filedata = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File(folderName + filedata);
		
		if(!file.exists())
			outputToClient.println("NOT_FOUND");
		
		else{
			outputToClient.println(file.getName() + "|" + file.length());
			try {
				OutputStream out = socket.getOutputStream();
				FileInputStream fis = new FileInputStream(file);
		        int x = 0;
		        while(true) {
		            x = fis.read();
		            if(x == -1)
		            	break;
		            out.write(x);
		        }
		        out.flush();
		        fis.close();
		        out.close();
			}
			catch (Exception e) {
		    	e.printStackTrace();
		    }
		}
	}
	
	private void deleteFile(boolean fromClient) {
		String filedata = null;
		
		try {
			filedata = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] arrStrFile = filedata.split("\\|");
		
		String filename=arrStrFile[0];
		
		monitor.updateFile(arrStrFile[0]);
		
		File file = new File(folderName + filename);
		if (file.exists()) {
			file.delete();
		}
		
		outputToClient.println("DELETED");
		
		if(fromClient)
		{
			String servers = filedata.replace(filename + "|", "");
			String fileDataForServers=filedata.replace(servers,"");
			//  send to backup servers
			//QUESTION: does this send the name of this server as well?
			// provided: String servers = "server2IP:port|server3IP:port|..."
			String[] serverList=servers.split("\\|");
			
			ServerToServerClient s2sClient=new ServerToServerClient(serverName,fileDataForServers, serverList,MasterlistEntry.STATUS_DELETED);
			s2sClient.start();
		}
		
		monitor.doneUpdatingFile(arrStrFile[1]);
	}
	
	protected void closeEverything(){
		try {
			outputToClient.close();
			inputFromClient.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}

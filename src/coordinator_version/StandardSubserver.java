package coordinator_version;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Subserver of a file server
 * @author Andrew
 *
 */
public class StandardSubserver extends Subserver {
	
	private String folderName = "Server_Folder/";
	
	public StandardSubserver(Socket socket, Monitor monitor) {
		super(socket, monitor);
	}
	
	@Override
	protected void parseAndRunCommand(String command) {
		//System.out.println("Command: " + command);
		switch (command) {
			case "GET": giveFile(); break;
			case "GET_SIZE": getFileSize(); break;
			case "GIVE": getFile(); break;
			case "DELETE": deleteFile(); break;
			//TODO remove
			//case "INSYNC": monitor.checkAndSetLastSync(serverPropertiesPath,  System.currentTimeMillis()); break;
			default: break;
		}
	}
	
	private void getFile() {
		String filedata = null;
		
		try {
			filedata = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] arrStrFile = filedata.split("|");
		
		monitor.updateFile(arrStrFile[0]);
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
		
		monitor.doneUpdatingFile(arrStrFile[0]);
		
		String servers = filedata.replace(arrStrFile[0] + "|" + arrStrFile[1] + "|", "");
		// TODO send to backup servers
		// provided: String servers = "server2IP:port|server3IP:port|..."
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
	
	private void deleteFile() {
		String filename = null;
		
		try {
			filename = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String[] arrStrFile = filename.split("|");
		
		monitor.updateFile(arrStrFile[0]);
		
		File file = new File(folderName + filename);
		if (file.exists()) {
			file.delete();
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

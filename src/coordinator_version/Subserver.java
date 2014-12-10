package coordinator_version;

import java.io.BufferedReader;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

public class Subserver extends Thread {
	
	protected Monitor monitor;
	protected Socket socket;
	protected PrintWriter outputToClient;
	protected BufferedReader inputFromClient;
	
	
	public Subserver(Socket socket, Monitor monitor) {
		this.socket = socket;
		this.monitor = monitor;
	}
	
	@Override
	public void run() {
		
		setupStream();
		
		String command = getCommand();
		
		parseAndRunCommand(command);
		
		closeEverything();
		
	}
	
	protected void setupStream(){
		try {
			outputToClient = new PrintWriter(socket.getOutputStream(), true);
			outputToClient.flush();
			
			inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	protected String getCommand(){
		String command = null;
		try {
			command = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return command;
	}
	
	protected void parseAndRunCommand(String command) {
		//System.out.println("Command: " + command);
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

package coordinator_version;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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
		try {
			outputToClient = new PrintWriter(socket.getOutputStream(), true);
			outputToClient.flush();
			
			inputFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		String command = null;
		try {
			command = inputFromClient.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		parseAndRunCommand(command);
		
		try {
			outputToClient.close();
			inputFromClient.close();
			socket.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void parseAndRunCommand(String command) {
		System.out.println("Command: " + command);
		
	}
	
	
}


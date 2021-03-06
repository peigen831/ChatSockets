package server;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;


public class Server extends JFrame{
	
	private JTextField userText;
	private JTextArea chatWindow;
	
	private ServerSocket server;
	private Socket connection;
	private int nClient = 0;

	public static String oneMB = "";
	
	public Server(){
		
		super("Chat Server");
		
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
					//sendMessage(event.getActionCommand());
					userText.setText("");
				}
			}
		);
		add(userText,BorderLayout.SOUTH);
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow));
		setSize(300,150);
		setVisible(true);
	}

	//set up and run the server
	public void startRunning(){
		
		try{
			server = new ServerSocket(6789,100);
			
			while(true)
			{	
				try
				{
					waitForConnection();
					//setupStreams();
					//whileChatting();
					
				}catch(EOFException e){
					showMessage("\n Server ended the connection!\n");
				}/*
				finally{
					closeSocket();
				}*/
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//wait for connection, display connection information
	private void waitForConnection() throws IOException{
		
		showMessage("Wait for someone to connect...\n");
		
		connection = server.accept();
		
		nClient ++;
		showMessage("Now connected to " + connection.getInetAddress().getHostName() + "\n");
		
		new ChatThread(connection).start();
	}
	
	
	
	//updates chatWindow
	private void showMessage(final String message){
		
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(message);
				}
			}
		);
	}
	
	//let the user type in the chatbox
	private void ableToType(final boolean enable){
		
		SwingUtilities.invokeLater(
				new Runnable(){
					public void run(){
						userText.setEditable(enable);
					}
				}
		);
	}
	
	class ChatThread extends Thread{
		Socket socket;
		ObjectOutputStream output;
		ObjectInputStream input;
		
		public ChatThread(Socket s){
			socket = s;
		}
		
		public void run(){
			try {
				
				setupStreams();
				whileChatting();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//setup stream to send and receive data
		private void setupStreams() throws IOException{
			
			output = new ObjectOutputStream(socket.getOutputStream());
			output.flush();
			
			input = new ObjectInputStream(socket.getInputStream());
			
			showMessage("\n Streams are now setup! \n");
		}
		
		//while chatting
		private void whileChatting()throws IOException{
			
			String message = "You are now connected!";
			//sendMessage(message);
			//ableToType(true);
			
			do
			{
				//have a conversation
				try{
					message = (String)input.readObject();
					showMessage(message + "\n");
				}catch(ClassNotFoundException e){
					showMessage("IDK what the user send!\n");
				}
			}while(!message.equals("CLIENT - END"));
			
			closeSocket();
		}
		
		//close streams and sockets after done chatting
		private void closeSocket(){
			
			showMessage("\n Clossing connections...\n");
			ableToType(false);
			
			try{
				output.close();
				input.close();
				socket.close();
				nClient--;
			}catch(IOException e){
				e.printStackTrace();
			}
		}
		
		//send a message to client
		private void sendMessage(String message){
			
			try{
				output.writeObject("SERVER - " + message);
				output.flush();
				
				showMessage("\nSERVER - " + message + "\n");
				
			}catch(IOException e){
				chatWindow.append("\n ERROR, CAN'T SEND MESSAGE");
			}
		}
		
		
		
	}
}

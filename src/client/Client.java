package client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame{
	
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	
	public Client(String host){
		
		super("Chat Client");
		serverIP = host;
		userText = new JTextField();
		userText.setEditable(false);
		userText.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
					sendData(event.getActionCommand());
					userText.setText("");
				}
			}
		);
		
		add(userText, BorderLayout.SOUTH);
		chatWindow = new JTextArea();
		chatWindow.setEditable(false);
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);
		setSize(300,150);
		setVisible(true);
	}
	
	//connect to server
	public void startRunning(){
		try
		{
			connectToServer();
			setupStreams();
			whileChatting();
			
		}catch(EOFException eof){
			showMessage("\n Client Terminate Connection \n");
		}catch(IOException io){
			io.printStackTrace();
		}finally{
			closeSocket();
		}
	}
	
	private void connectToServer() throws IOException{
		
		showMessage("Attemping Connection...\n");
		
		connection = new Socket(InetAddress.getByName(serverIP), 6789);
		
		showMessage("Connected to: " + connection.getInetAddress().getHostName()+ "\n");
	}
	
	//set up streams to send and receive messages
	private void setupStreams() throws IOException{
		
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		
		input = new ObjectInputStream(connection.getInputStream());
		
		showMessage("\n Streams are good to go! \n");
	}
	
	//while chatting with server
	private void whileChatting()throws IOException{
		
		ableToType(true);
		do{
			try{
				message = (String)input.readObject();
				showMessage(message + "\n ");
			}catch(ClassNotFoundException e){
				showMessage("IDK the object type\n");
			}
			
		}while(!message.equals("SERVER - END"));
	}
	
	//close sockets
	private void closeSocket(){
		
		showMessage("Closing the sockets...\n");
		ableToType(false);
		
		try{
			output.close();
			input.close();
			connection.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//send message to server
	private void sendData(String message){
		
		try{
			output.writeObject("Client - " + message);
			output.flush();
			showMessage("Client - " + message + "\n");
		}catch(IOException e){
			chatWindow.append("Some thing wrong while sending message...\n");
		}
	}
	
	private void showMessage(final String message){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					chatWindow.append(message);
				}
			}
		);
	}
	
	private void ableToType(final boolean enable){
		SwingUtilities.invokeLater(
			new Runnable(){
				public void run(){
					userText.setEditable(enable);
				}
			}
		);
	}
}

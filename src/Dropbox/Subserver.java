package Dropbox;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Subserver extends Thread{
	
	final static int NEW_CLIENT_VERSION = 1;
	final static int NEW_SERVER_VERSION = 2;
	final static int END_SYNCHRONIZE = 3;
	final static String END_SERVER_REQUEST = "end server request";
	
	final static int NAME_BYTE_SIZE=128;
	final static int FILESIZE_BYTE_SIZE=8;
	final static int FILE_BYTE_SIZE=40000;
	
	Socket socket;
	PrintWriter output;
	BufferedReader input;
	
	private String folderName="Server";
	
	ArrayList<String> serverUpdateList = new ArrayList<String>();
	ArrayList<String> clientUpdateList = new ArrayList<String>();

	long lastConnection = 0;
	HashMap<String, Long> filedateMap = new HashMap<>();

	
	public Subserver(Socket clientSocket){
		this.socket = clientSocket;
	}
	
	public void run(){
		try {
			setupStreams();
			
			//getFiledataFromClient();

			//verifyToUpdateFileStatus();
			
			receiveFile();
			
			//updateClientFile();
			
			//updateServerFile();
			
			closeEverything();
		}
		catch(Exception e ){
			e.printStackTrace();
		}
	}
	
	public void updateServerFile(){
		String type = NEW_CLIENT_VERSION + "\n";
		
		
		for(int i = 0; i < serverUpdateList.size(); i++)
		{
			output.print(type);
			output.flush();
			System.out.println("Subserver: sent request type");
			output.print(serverUpdateList.get(i) + "\n");
			output.flush();
			System.out.println("Subserver: sent requested filename");
			try {
				Server.monitor.updateServerFile(socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		//end of requesting files
		//output.print(END_SERVER_REQUEST);
	}
	
	public void sendFileRequestToClient (){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < serverUpdateList.size(); i++)
		{
			sb.append(serverUpdateList.get(i) + "\n");
		}
		
		sb.append(END_SERVER_REQUEST +"\n");;
		
		try{
			output.print(sb.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	
	public void updateClientFile(){
		int type = NEW_SERVER_VERSION;
		
		for(int i = 0; i < clientUpdateList.size(); i++)
		{
			output.println(type);
			try {
				Server.monitor.sendFile(socket.getOutputStream(), clientUpdateList.get(i));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	public void verifyToUpdateFileStatus(){
		//lacking: when server has a file that client doesn't have
		
		clientUpdateList = Server.monitor.getClientToUpdateList(filedateMap);
		System.out.println("Client to update list: ");
		printList(clientUpdateList);
		
		serverUpdateList = Server.monitor.getServerToUpdateList(filedateMap);
		System.out.println("Server to update list: ");
		printList(serverUpdateList);
		
	}
	
	public void printList(ArrayList<String> list){
		for(int i =0 ; i<list.size(); i++)
			System.out.println(list.get(i));
	}
	
	public void printMap(HashMap<String, String> map){
		for(Map.Entry<String, String> entry : map.entrySet()){
		    System.out.printf("Key : %s and Value: %s %n", entry.getKey(), entry.getValue());
		}
	}
	
	public void setupStreams(){
		try {

			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void getFiledataFromClient(){
		try {

			String str;
			String[] split;
			
			do {
				str = input.readLine();
				split = str.split(" ");
				filedateMap.put(split[0], Long.parseLong(split[1]));
				
			}while(input.ready() && str != null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendFileToClient(String filename)
	{
		//send metadata
		//TODO send metadata as bytestream
		File file = new File("src/Dropbox/"+ folderName + "/" + filename);
		
		long filesize = file.length();
		OutputStream os = null;
		byte [] nameBytes=null;
		byte[] sizeBytes=null;
		try {
			os = socket.getOutputStream();
			/*DataOutputStream dos = new DataOutputStream(os);
		    dos.writeUTF(filename);  
		    dos.writeLong(filesize);*/
			
			ByteBuffer nameByteBuffer= ByteBuffer.allocate(NAME_BYTE_SIZE);
			nameByteBuffer.put(filename.getBytes(Charset.forName("UTF-8")));
			nameBytes=nameByteBuffer.array();
			sizeBytes=ByteBuffer.allocate(FILESIZE_BYTE_SIZE).putLong(filesize).array();
			
		 }catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 System.out.println("Client: SENT METADATA of " + filename );
		 
		 //send file, TODO allow variable size
		// byte[] fileBytes = new byte[(int) file.length()];//since file length is variable, the sender should wait between file sends.
		ByteBuffer fileByteBuffer=ByteBuffer.allocate(FILE_BYTE_SIZE);
		 byte[]fileBytes=new byte[FILE_BYTE_SIZE];
		 try {
		    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
		    bis.read(fileBytes, 0, fileBytes.length);
		    ByteBuffer buffer = ByteBuffer.allocate(nameBytes.length + sizeBytes.length+fileBytes.length);
		    buffer.put(nameBytes); 
		    buffer.put(sizeBytes);
		    buffer.put(fileBytes);
		    byte[] byteArray=buffer.array();
		    
		   //os.write(mybytearray, 0, mybytearray.length);
		    os.write(byteArray, 0, byteArray.length);
		    os.flush();
		 } catch (Exception e) {
			 e.printStackTrace();
		 }
		    
	    System.out.println("Client: Done sending file " + filename);
	}
	
	public void receiveFile()
	{
		try {
			byte[] mybytearray = new byte[NAME_BYTE_SIZE+FILESIZE_BYTE_SIZE+FILE_BYTE_SIZE];
		    InputStream is = socket.getInputStream();
		    is.read(mybytearray, 0, mybytearray.length);
		    String filename;
		    long filesize;
		    
		    byte []nameBytes=new byte[NAME_BYTE_SIZE];
		    System.arraycopy(mybytearray, 0, nameBytes, 0, NAME_BYTE_SIZE);
		    filename=new String(nameBytes, "UTF-8").trim();
		    
		    
		    byte []sizeBytes=new byte[FILESIZE_BYTE_SIZE];
		    System.arraycopy(mybytearray, NAME_BYTE_SIZE, sizeBytes, 0, FILESIZE_BYTE_SIZE);
		    filesize = ByteBuffer.wrap(sizeBytes).getLong();
		   
		    System.out.println("Received File Size: "+filesize);
		    System.out.println("Received Filename: "+filename);
		    String filepath="src/Dropbox/"+folderName + "/" + filename;
		    System.out.println("Writing to "+filepath);
		    FileOutputStream fos = new FileOutputStream(filepath);
		    BufferedOutputStream bos = new BufferedOutputStream(fos);
		    bos.write(mybytearray, NAME_BYTE_SIZE+FILESIZE_BYTE_SIZE, (int)filesize);//write from immediately after metadata up to filesize, assuming that filesize is actual size of file in bytes
		    bos.flush();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void closeEverything() {
		try {
			output.println(END_SYNCHRONIZE);
			output.close();
			input.close();
			socket.close();
			System.out.println("Subserver: close everything");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

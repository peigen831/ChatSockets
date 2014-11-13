package WebBrowser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Browser extends JFrame implements Runnable {
	private JTextField addressBar;
	private JEditorPane display;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	private int i;
	
	//constructor
	public Browser(int i){
		super("CSC-Browser");
		this.i = i;
		/*addressBar = new JTextField("Enter the URL:");
		addressBar.addActionListener(
			new ActionListener(){
				public void actionPerformed(ActionEvent event){
					loadPage(event.getActionCommand());
				}
			}
		);
		
		add(addressBar, BorderLayout.NORTH); 
		
		display = new JEditorPane();
		display.setEditable(false);
		display.addHyperlinkListener(
			new HyperlinkListener(){
				public void hyperlinkUpdate(HyperlinkEvent event){
					if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
					{
						loadPage(event.getURL().toString());
					}
				}
			}
		);
		add(new JScrollPane(display), BorderLayout.CENTER);
		setSize(500,300);
		setVisible(false);*/
	}
	
	private void loadPage(String URL){
		try
		{
			//this part should change to sockets.
			long start = System.currentTimeMillis();
			connectToServer(URL);
			
			setupStreams();
			
			sendRequest(URL);
			
			String content = getRespond();
			
			long end = System.currentTimeMillis();
			
			System.out.println(i + ": " + (end-start));
			
			setPage(content);
			
		}catch(Exception e){
			//System.out.println("Crap");
			
		}finally{
			closeEverything();
		}
	}
	
	private void connectToServer(String URL){
		try
		{
			
			socket = new Socket(InetAddress.getByName(parseHost(URL)), 80);
			
		}catch(Exception e){
			System.out.println("Error: connect to server");
		}
			
	}
	
	private void setupStreams(){
		try
		{
			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}catch(Exception e){
			System.out.println("Error: setup streams");
		}
	}
	
	public String parseDirectory(String given)
	{
		String[] split = given.split("/");
		return split[split.length-1];
	}
	
	public String parseHost(String given){
		return given.split("/")[0].split(":")[0];
	}
	
	private void sendRequest(String URL){
		output.println(
				"GET /" + parseDirectory(URL) + " HTTP/1.1\n"
				+ "Host: " + parseHost(URL) + "\n"
				+ "Connection: keep-alive\n"
				+ "Accept: * " + "//" + "*\n"
				+ "User-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.102 Safari/537.36\n"
				+ "Accept-Encoding: gzip,deflate,sdch\n"
				+ "Accept-Language: zh-CN,zh;q=0.8\n"
				);
		
	}
	
	private String getRespond()
	{
		
		String content = "";
		String str;
		
		try{
			do {
				str = input.readLine();
				content += str + "\n"; 
			}
			while(input.ready() && str != null);
			
			
		}catch(Exception e){
			System.out.println("Failed to get Respond");
		}
		
		return content;
	}
	
	private void closeEverything(){
		try 
		{
			input.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setPage(String content){
		display.setText(content);
		
		addressBar.setText("");
	}

	@Override
	public void run() {
		loadPage("localhost:80/one.html");
	}
}

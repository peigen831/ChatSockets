package WebBrowser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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

public class Browser extends JFrame{
	private JTextField addressBar;
	private JEditorPane display;
	private Socket socket;
	private PrintWriter output;
	private BufferedReader input;
	
	//constructor
	public Browser() {
		super("CSC-Browser");
		
		addressBar = new JTextField("Enter the URL:");
		addressBar.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent arg0) {}
			
			public void focusGained(FocusEvent arg0) {
				addressBar.selectAll();
			}
		});
		addressBar.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					String str = addressBar.getText();
					loadPage(event.getActionCommand());
					addressBar.setText(str);
					requestFocusInWindow();
				}
			}
		);
		
		add(addressBar, BorderLayout.NORTH); 
		
		display = new JEditorPane();
		display.setEditable(false);
		display.addHyperlinkListener(
			new HyperlinkListener() {
				public void hyperlinkUpdate(HyperlinkEvent event) {
					if(event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						loadPage(event.getURL().toString());
					}
				}
			}
		);
		add(new JScrollPane(display), BorderLayout.CENTER);
		setSize(500,300);
		setVisible(true);
	}
	
	private void loadPage(String URL) {
		try {
			//this part should change to sockets.
			connectToServer(URL);
			
			setupStreams();
			
			sendRequest(URL);
			
			String content = getRespond();
					
			setPage(content);
			
		} catch(Exception e) {
			System.out.println("Crap");
			
		} finally {
			closeEverything();
		}
	}
	
	private void connectToServer(String URL){
		try
		{
			System.out.println("Attemping Connection...\n");
			
			socket = new Socket(InetAddress.getByName(parseHost(URL)), 80);
			
			System.out.println("Connected to: " + socket.getInetAddress()+ "\n");
			
		}catch(Exception e){
			System.out.println("Error: connect to server");
		}
	
	}
	
	private void setupStreams() {
		try {
			output = new PrintWriter(socket.getOutputStream(), true);
			output.flush();
			
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} catch (Exception e) {
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
	
	private void sendRequest(String URL) {
		output.println(
				"GET /" + parseDirectory(URL) + " HTTP/1.1\n"
				+ "Host: " + parseHost(URL) + "\n"
				+ "Connection: keep-alive\n"
				+ "Accept: * " + "//" + "*\n"
				+ "User-Agent: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.102 Safari/537.36\n"
				+ "Accept-Encoding: gzip,deflate,sdch\n"
				+ "Accept-Language: zh-CN,zh;q=0.8\n"
		);
		
		System.out.println("Request sent");
	}
	
	private String getRespond() {
		
		String content = "";
		String str;
		
		try {
			do {
				str = input.readLine();
				System.out.println("Respond: "+ str);
				content += str + "\n"; 
			}
			while(input.ready() && str != null);
			System.out.println("Done receiving");
			
		} catch(Exception e) {
			System.out.println("Failed to get Respond");
		}
		
		return content;
	}
	
	private void closeEverything() {
		try {
			input.close();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void setPage(String content) {
		display.setText(content);
		
		addressBar.setText("");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Browser browser = new Browser();
		browser.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}

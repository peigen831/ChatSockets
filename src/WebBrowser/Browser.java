package WebBrowser;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Browser extends JFrame{
	private JTextField addressBar;
	private JEditorPane display;
	
	//constructor
	public Browser(){
		super("Gen-Browser");
		
		addressBar = new JTextField("Enter the URL:");
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
		setVisible(true);
	}
	
	private void loadPage(String URL){
		try
		{
			//this part should change to sockets.
			display.setPage(URL);
			addressBar.setText(URL);
		}catch(Exception e){
			System.out.println("Crap");
		}
	}
}

package WebBrowser;

import javax.swing.JFrame;

public class Driver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 1; i++) {
			try
			{
				Thread t = (new Thread(new Browser(i)));
				t.start();
			}catch(Exception e){
			}
		}
		
		
	}

}

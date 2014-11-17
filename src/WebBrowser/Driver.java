package WebBrowser;

import javax.swing.JFrame;

public class Driver {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		for (int i = 0; i < 6400; i++) {
			Thread t = (new Thread(new Browser(i)));
			t.start();
		}
		
		
	}

}

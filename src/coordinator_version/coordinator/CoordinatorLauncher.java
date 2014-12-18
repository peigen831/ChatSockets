package coordinator_version.coordinator;

import coordinator_version.Client;
import coordinator_version.Server;

public class CoordinatorLauncher {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Coordinator().start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Server(50,Server.SERVER_TO_CLIENT).start();
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Server(60,Server.SERVER_TO_CLIENT).start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		(new Client("Client1")).start();
		try{
			Thread.sleep(1000);
		}catch(Exception e){
			e.printStackTrace();
		}
		(new Client("Client2")).start();
	}

}

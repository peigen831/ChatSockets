package twosocket;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.locks.Condition;

public class Monitor {
	private long lastSync;
	private List<String> updatingFiles;
	private Condition condition;
	
	public Monitor() {
		ResourceBundle rb = ResourceBundle.getBundle("twosocket.server");
		lastSync = Long.parseLong(rb.getString("LAST_SYNC"));
		updatingFiles = new ArrayList<>();
	}
	
	public synchronized long getLastSync() {
		return lastSync;
	}
	
	public synchronized void checkAndSetLastSync(long lastSync) {
		if (lastSync > this.lastSync) {
			this.lastSync = lastSync;
		}
		
		try {
			Properties properties = new Properties();
			properties.setProperty("LAST_SYNC", Long.toString(lastSync));

			File file = new File("src/twosocket/server.properties");
			FileOutputStream fileOut = new FileOutputStream(file);
			properties.store(fileOut, null);
			fileOut.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized boolean updateFile(String file) {
		while (updatingFiles.contains(file)) {
			try {
				condition.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		updatingFiles.add(file);
		return true;
	}
	
	public synchronized void doneUpdatingFile(String file) {
		updatingFiles.remove(file);
	}
}
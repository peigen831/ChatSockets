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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Monitor {
	private long lastSync;
	private List<String> updatingFiles;
	final Lock lock = new ReentrantLock();
	Condition mutex = lock.newCondition();
	
	public Monitor() {
		ResourceBundle rb = ResourceBundle.getBundle("twosocket.server");
		lastSync = Long.parseLong(rb.getString("LAST_SYNC"));
		updatingFiles = new ArrayList<>();
	}
	
	public synchronized long getLastSync() {
		return lastSync;
	}
	
	public void checkAndSetLastSync(long lastSync) {
		lock.lock();
		
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
		
		lock.unlock();
	}
	
	public boolean updateFile(String file) {
		lock.lock();
		
		while (updatingFiles.contains(file)) {
			try {
				mutex.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		updatingFiles.add(file);
		lock.unlock();
		
		return true;
	}
	
	public void doneUpdatingFile(String file) {
		lock.lock();
		updatingFiles.remove(file);
		mutex.signalAll();
		
		lock.unlock();
	}
}
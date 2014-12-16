package coordinator_version.coordinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Map.Entry;

public class CoordinatorMonitor {
	
	private List<String> availableServers;
	/*final Lock lock = new ReentrantLock();
	Condition mutex = lock.newCondition();*/
	
	public CoordinatorMonitor() {
		availableServers = new ArrayList<>();
	}
	
	public synchronized void addAvailableServer(String hostName, int portNumber) {
		availableServers.add(hostName + ":" + portNumber);
	}
	
	public synchronized void removeAvailableServer(String hostName, int portNumber) {
		availableServers.remove(hostName + ":" + portNumber);
	}
	
	public synchronized List<String> getAvailableServers() {
		return availableServers;
	}
}

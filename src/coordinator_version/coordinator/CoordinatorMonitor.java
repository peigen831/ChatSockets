package coordinator_version.coordinator;

import java.util.ArrayList;
import java.util.List;

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

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
	
	public synchronized Properties loadProperties(String path){
        Properties properties = new Properties();
        File file = new File(path);
        try{
        	if(file.exists())
        	{
	        	FileInputStream in = new FileInputStream(file);
	            properties.load(in);
	            in.close();
            }
        	else properties.setProperty("LAST_SYNC", "0");

        	FileOutputStream fileOut = new FileOutputStream(file);
            properties.store(fileOut, null);
            fileOut.close();
        }catch(Exception e){
        	e.printStackTrace();
        }
		return properties;
	}
	
	public synchronized HashMap<String, Integer> getServerToGetMap(String[] hasFileServer){
		//TODO get servers alive and random from them
		//if(hasFileServer.length < nThreshold && not all hasFileServers are alive)
		//	then getAvailable servers, random up to nThreshold
		//else just update the list from the hasFileServer
		
		//for now just give to server who already has the file
		HashMap<String, Integer> serverPortMap = new HashMap<String, Integer>();
		for(int i = 0; i < hasFileServer.length; i++){
			serverPortMap.put(hasFileServer[i], getPort(hasFileServer[i]));
		}
		return serverPortMap;
	}
	
	
	public synchronized HashMap<String, Integer> getServerportMap(String[] hasFileServer){
		HashMap<String, Integer> serverPortMap = new HashMap<String, Integer>();
		for(int i = 0; i < hasFileServer.length; i++){
			serverPortMap.put(hasFileServer[i], getPort(hasFileServer[i]));
		}
		return serverPortMap;
	}
	
	public int getPort(String serverName){
		int port = 9999;
		try
		{
			FileInputStream in = new FileInputStream(Coordinator.SERVER_FOLDER + serverName + ".properties");
	        Properties properties = new Properties();
	        properties.load(in);
	        in.close();
	        port = Integer.parseInt(properties.getProperty("PORT"));
		}catch(Exception e){
			e.printStackTrace();
		}
		return port;
	}
	
	public synchronized void updateMasterlistProperties(String path, HashMap<String, String> fileDateAction){
		try
		{
			FileInputStream in = new FileInputStream(path);
            Properties properties = new Properties();
            properties.load(in);
            in.close();
            
            for(Entry<String, String> entry: fileDateAction.entrySet()){
            	properties.setProperty(entry.getKey(), entry.getValue());
            }
			
			File file = new File(path);
	        FileOutputStream fileOut = new FileOutputStream(file);
	        properties.store(fileOut, null);
	        fileOut.close();
	        
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public synchronized void updateClientProperties(String path, HashMap<String, String> fileDateAction){
		try
		{
            Properties properties = new Properties();
            
            for(Entry<String, String> entry: fileDateAction.entrySet()){
            	properties.setProperty(entry.getKey(), entry.getValue());
            }
			
			File file = new File(path);
	        FileOutputStream fileOut = new FileOutputStream(file);
	        properties.store(fileOut, null);
	        fileOut.close();
	        
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
}

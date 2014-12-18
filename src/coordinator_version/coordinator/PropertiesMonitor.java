package coordinator_version.coordinator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

public class PropertiesMonitor {
	HashMap<String, String> serverAddressProperties;
	
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
        	else if(path.contains(Coordinator.CLIENT_FOLDER))
        		properties.setProperty("LAST_SYNC", "0");

        	FileOutputStream fileOut = new FileOutputStream(file);
            properties.store(fileOut, null);
            fileOut.close();
        }catch(Exception e){
        	e.printStackTrace();
        }
		return properties;
	}
	
	public synchronized HashMap<String, String> getServerToGetMap(String[] hasFileServer){
		
		List<String> activeAddressport = Coordinator.coordinatorMonitor.getAvailableServers();
		int nChoosen = 0;
		int nLimit = 2;
		
		//System.out.println("List active servers");
		//for(String a : activeAddressport)
		//	System.out.println(a);
		
		HashMap<String, String> serverAddressportMap = new HashMap<String, String>();
		
		if (hasFileServer.length == 0) {
			for (String activeAddress : activeAddressport) {
				serverAddressportMap.put(activeAddress.replace(":", "-"), activeAddress);
			}
			return serverAddressportMap;
		}
		
		for(int i = 0; i < hasFileServer.length; i++){
			if(activeAddressport.contains(getAddressPort(hasFileServer[i])))
			{
				serverAddressportMap.put(hasFileServer[i], getAddressPort(hasFileServer[i]));
				activeAddressport.remove(getAddressPort(hasFileServer[i]));
				nChoosen ++;
			}
		}
		
		serverAddressProperties = loadServerProperties();
		
		while(activeAddressport.size() > 0 && nChoosen < nLimit){
			for(int i = 0; i < activeAddressport.size(); i++)
			{
				if(nChoosen >= nLimit)
					break;
				serverAddressportMap.put(serverAddressProperties.get(activeAddressport.get(i)), activeAddressport.get(i));
				nChoosen++;
			}
		}
		System.out.println("Servers to give ");
		for(Entry<String, String> entry: serverAddressportMap.entrySet()){
			System.out.println(entry.getKey() +":"+entry.getValue());
		}
		return serverAddressportMap;
	}
	
	public synchronized HashMap<String, String> loadServerProperties(){
		HashMap<String, String> result = new HashMap<String, String>();
		File folder = new File(Coordinator.SERVER_FOLDER);
		File[] fileList = folder.listFiles();
		
		for(int i = 0; i < fileList.length; i++) {
			String filename = removeExtension(fileList[i].getName());
			result.put(getAddressPort(filename), filename);
		}
		
		return result;
	}
	
	public String removeExtension (String str) {

        if (str == null) return null;

        int pos = str.lastIndexOf(".");

        if (pos == -1) return str;

        return str.substring(0, pos);
    }
	
	public synchronized HashMap<String, String> getServerAddressportMap(String[] hasFileServer){
		HashMap<String, String> serverPortMap = new HashMap<String, String>();
		
		for(int i = 0; i < hasFileServer.length; i++){
			serverPortMap.put(hasFileServer[i], getAddressPort(hasFileServer[i]));
		}
		
		return serverPortMap;
	}
	
	public String getAddressPort(String serverName){
		StringBuilder sb = new StringBuilder();
		try
		{
			FileInputStream in = new FileInputStream(Coordinator.SERVER_FOLDER + serverName + ".properties");
	        Properties properties = new Properties();
	        properties.load(in);
	        in.close();
	        String address = properties.getProperty("ADDRESS");
	        int port = Integer.parseInt(properties.getProperty("PORT"));
	        sb.append(address + ":" + port);
		}catch(Exception e){
			e.printStackTrace();
		}
		return sb.toString();
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

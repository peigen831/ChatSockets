package twosocket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
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
		/*
		ResourceBundle rb = ResourceBundle.getBundle("twosocket.server");
		lastSync = Long.parseLong(rb.getString("LAST_SYNC"));
		*/
		updatingFiles = new ArrayList<>();
	}
	
	public synchronized long getLastSync() {
		return lastSync;
	}
	
	public Properties loadProperties(String path){
		lock.lock();
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
		lock.unlock();
		return properties;
	}
	
	public void updateClientProperties(String path, HashMap<String, String> fileDateAction){
		lock.lock();
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
         
        lock.unlock();
	}
	
	public void updateServerProperties(String path, HashMap<String, String> fileDateAction){
		lock.lock();
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
         
        lock.unlock();
	}
	
	public void checkAndSetLastSync(String path, long lastSync) {
		lock.lock();
		
		if (lastSync > this.lastSync) {
			this.lastSync = lastSync;
		}
		
		try {
			FileInputStream in = new FileInputStream(path);
            Properties properties = new Properties();
            properties.load(in);
            in.close();
            
			properties.setProperty("LAST_SYNC", Long.toString(lastSync));

			File file = new File(path);
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
	
	public void deleteFile(String filePath){
		lock.lock();
		File file = new File(filePath);
		file.delete();
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
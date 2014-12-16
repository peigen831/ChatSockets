package coordinator_version.coordinator;

import java.util.HashMap;
import java.util.Map.Entry;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		PropertiesMonitor mon = new PropertiesMonitor();
		
		HashMap<String, String> map = mon.loadServerProperties();
		
		
		for(Entry<String, String> entry: map.entrySet()){
			System.out.println(entry.getKey() + ":" + entry.getValue());	
		}	
	}

}

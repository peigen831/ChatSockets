package coordinator_version.coordinator;

import java.util.ArrayList;
import java.util.List;

public class MasterlistEntry {
	private String filename;
	private long lastUpdate;
	private int status;
	private List<String> servers=new ArrayList<String>();
	
	public static final int STATUS_DELETED=0;
	public static final int STATUS_ADDED=1;
	public static final int STATUS_UPDATED=2;
	
	public MasterlistEntry(String filename, long lastUpdate)
	{
		this.filename=filename;
		this.lastUpdate=lastUpdate;
		this.status=STATUS_ADDED;
	}
	
	public long getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public void addServer(String server)
	{
		boolean alreadyExists=false;
		for(String s: servers)
		{
			if(s.equals(server))
			{
				alreadyExists=true;
				break;
			}
		}
		if(!alreadyExists)
			servers.add(server);
	}
	public void removeServer(String server)
	{
		for(String s: servers)
		{
			if(s.equals(server))
			{
				servers.remove(s);
			}
		}
	}
	public List<String> getServerList()
	{
		return servers;
	}
}

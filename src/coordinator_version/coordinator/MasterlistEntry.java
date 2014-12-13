package coordinator_version.coordinator;

import java.util.List;

public class MasterlistEntry {
	private String filename;
	private long lastUpdate;
	private int status;
	private List<String> servers;
	
	public static final int STATUS_DELETED=0;
	public static final int STATUS_ADDED=1;
	public static final int STATUS_UPDATED=2;
}

public class SystemInfoRecord extends InfoRecord{
	public SystemInfoRecord()
	{
		super();
	}
	public SystemInfoRecord(int indentLength, char block, String topTitle, int titleMaxLength)
	{
		super(indentLength, block, topTitle, titleMaxLength);
	}
	public void setSLES(String version)
	{
		setRecord("OS Version",version);
	}
	public void setDBS(String version)
	{
		setRecord("DBS Version",version);
	}
	public void setPDE(String version)
	{
		setRecord("PDE Version",version);
	}
	public void setViewPoint(String version)
	{
		setRecord("Viewpoint Version",version);
	}
	public void setHostName(String hostName)
	{
		setRecord("Hostname",hostName);
	}
}

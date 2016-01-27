public class CommandInfoRecord extends InfoRecord {
	public CommandInfoRecord()
	{
		super(5,'+'," Command Information ",20);
	}
	public CommandInfoRecord(int indentLength, char block, String topTitle, int titleMaxLength)
	{
		super(indentLength, block, topTitle, titleMaxLength);
	}
	public CommandInfoRecord(String topTitle)
	{
		super(5,'+', topTitle, 20);
	}
	public void setCommand(String commandText)
	{
		setRecord("Command",commandText);
	}
	public void setDescription(String descriptionText)
	{
		setRecord("Description",descriptionText);
	}
}

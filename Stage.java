
public class Stage extends InfoRecord {
	public Stage()
	{
		super(10,'=',"",50);
	}
	public Stage(int indentLength, char block, String topTitle, int titleMaxLength)
	{
		super(indentLength, block, topTitle, titleMaxLength);
	}
	public Stage(String text, int width)
	{
		this(10,'=',"",width);
		setRecord(text, "");
	}
	public Stage(String text)
	{
		this();
		setRecord(text, "");
	}
	
	public void setText(String text)
	{
		setRecord(text, "");
	}

}

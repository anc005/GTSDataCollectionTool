import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class InfoRecord {
	private StringBuffer stringValueBuffer = new StringBuffer("");
	private String topTitle;
	private  int titleMaxLength;
	private  int indentLength;
	private  char block;
	private int padding = 0; //4+value.length()+(2*titleMaxLength)-title.length()-topTitle.length
	private StringBuffer appendRightBlocks(StringBuffer input)
	{
		Pattern p = null;
		Matcher currentMatcher;
		StringBuffer tempBuffer = null;
		p = Pattern.compile("\n[ ]*(["+block+"].*)");
		currentMatcher = p.matcher(input);
		tempBuffer = new StringBuffer();
		while(currentMatcher.find())
		{
			currentMatcher.appendReplacement(tempBuffer,currentMatcher.group(0)+padding((2*padding+topTitle.length())-(currentMatcher.group(1).length())-1,' ')+block);
		}
		currentMatcher.appendTail(tempBuffer);
		return tempBuffer;
	}
	protected void setRecord(String title, String value)
	{
		try
		{
			String[] tokens = value.replaceAll("\t", " ").split("[\n\r]");
			for (int i=0 ; i< tokens.length ; i++)
			{
				stringValueBuffer.append("\n");
				stringValueBuffer.append(padding(indentLength,' '));
				stringValueBuffer.append(block);
				stringValueBuffer.append(padding(titleMaxLength-title.length(),' '));
				stringValueBuffer.append(i==0?title:padding(title.length(),' '));
				stringValueBuffer.append(i==0&&!value.equals("")?" : ":"   ");
				stringValueBuffer.append(Matcher.quoteReplacement(tokens[i]));
				calcPadding(title, tokens[i]);
			}
		}
		catch(Exception e)
		{
			AppLogger.getLogger().log(Level.WARNING, "An Unexpected Exception Occured", e);
		}
	}
	private void calcPadding(String title, String value)
	{
		int newPadding = (4+value.length()+(2*titleMaxLength)-title.length()-topTitle.length())/2;
		padding = padding<newPadding?newPadding:padding;
	}
	private String padding(int r, char x)
	{
		StringBuffer b = new StringBuffer();
		for(int i=0 ; i<r ; i++)
		{
			b.append(x);
		}
		return b.toString();
	}
	private String header()
	{
		return 	"\n"+padding(indentLength,' ')+
				padding(padding, block)+
				topTitle+
				padding(padding, block);
	}
	private String footer()
	{
		return "\n"+padding(indentLength,' ')+
				padding((2*padding)+topTitle.length(), block)+
				"\n\n";
	}
	protected InfoRecord()
	{
		indentLength = 20;
		block = '+';
		topTitle = ": System Data :";
		titleMaxLength = 25;
	}
	protected InfoRecord(int indentLength, char block, String topTitle, int titleMaxLength)
	{
		this.indentLength = indentLength;
		this.block = block;
		this.topTitle = topTitle;
		this.titleMaxLength = titleMaxLength;
	}
	public String stringValue()
	{
		try
		{
			return header()+appendRightBlocks(stringValueBuffer)+footer();
		}
		catch(Exception e)
		{
			AppLogger.getLogger().log(Level.WARNING, "An Unexpected Exception Occured", e);
		}
		return "";
	}
}

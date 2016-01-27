import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;
	
public class DirectoryZip
{
	private static final int BUFFER = 2048;
	
	private String directoryPath;
	private String zipFileName;
   
	public DirectoryZip(String directoryPath, String zipFileName)
	{
		this.directoryPath=directoryPath;
		this.zipFileName=zipFileName;
	}
	
	/*public static void main(String args[])
	{
		new DirectoryZip("C:\\Users\\MH255003\\Desktop\\Mohamed\\All\\Java\\GTSTool\\GTSTool_Annie_Final\\GTSTool\\src\\Test\\GTSTOOL-DBS-2015-11-22-20-48-25-972-EST",
		"GTSTOOL-DBS-2015-11-22-20-48-25-972-EST.zip").archive();
	}*/
	public boolean archive()
	{
		Logger logger = AppLogger.getLogger();
		try
		{
			BufferedInputStream origin = null;
			File directory = new File(directoryPath);
			FileOutputStream zfos = new FileOutputStream(directoryPath+"\\"+zipFileName);
			ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(zfos));
			byte data[] = new byte[BUFFER];
			File files[] = directory.listFiles();

			for (int i=0; i<files.length; i++)
			{
				if(files[i].getName().equals(zipFileName) || files[i].getName().trim().toLowerCase().endsWith(".log.lck"))
				{
					continue;
				}
				System.out.println("Deflating: "+files[i].getName());
				logger.info("Deflating: "+files[i].getName());
				FileInputStream fis = new FileInputStream(files[i]);
				origin = new BufferedInputStream(fis, BUFFER);
				ZipEntry entry = new ZipEntry(files[i].getName());
				zos.putNextEntry(entry);
				int count;
				while((count = origin.read(data, 0, BUFFER)) != -1)
				{
					zos.write(data, 0, count);
				}
				origin.close();
			}
			zos.close();
			return true;
		}
		catch(IOException ioe)
		{
			logger.log(Level.SEVERE, "An Unexpected Exception Occured", ioe);
			return false;
		}
	}
}
/*
 * SSHTunnel.java
 *
 * A collection of static functions that utilize the open jsch library to
 * setup ssh connections, port forward, execute commands through java
 *
 * developed and maintained by: Raed Rizk Soliman
 * Last Edit: 04/07/2015
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHTunnel {

	static final int lport = 3338;
	static final int rport = 1025;
	static Session session = null;
	static Session[] sessions = null;
	static String[][] OsDetails = null;

	static Channel abortableChannel;
	
	public static boolean createConnection(String[] hosts, String[] usernames,
			String[] passwords) {

		try {
			System.out.println("Creating JSCH connection");
			
			JSch jsch = new JSch();

			sessions = new Session[hosts.length];

			String host = hosts[0];
			String user = usernames[0];
			int assinged_port = 0;

			sessions[0] = session = jsch.getSession(user, host, 22);
			sessions[0].setConfig("StrictHostKeyChecking", "no");
			session.setPassword(passwords[0]);
			session.connect();
			System.out.println("The session has been established to " + user
					+ "@" + host);
			

			// If only one hop, forward the port
			if (hosts.length == 1) {
				assinged_port = session.setPortForwardingL(lport, "127.0.0.1",
						rport);
				System.out.println("portforwarding: " + "localhost:"
						+ assinged_port + " -> " + host + ":" + rport);
				
			}

			for (int i = 1; i < hosts.length; i++) {
				host = hosts[i];
				user = usernames[i];

				sessions[i] = session = jsch.getSession(user, host, 22);
				sessions[i].setConfig("StrictHostKeyChecking", "no");
				session.setPassword(passwords[i]);
				session.setHostKeyAlias(host);

				session.connect();
				System.out.println("The session has been established to "
						+ user + "@" + host);

				if (i == (hosts.length - 1)) {
					assinged_port = session.setPortForwardingL(lport,
							"127.0.0.1", rport);
					System.out.println("portforwarding: " + "localhost:"
							+ assinged_port + " -> " + host + ":" + rport);
				}

			}
			return true;
		} catch (Exception e) {
			
			e.printStackTrace();
			return false;
		}

	}

	public static void disconnect() {
		if ((sessions != null) && session.isConnected()) {
			System.out.println("Closing SSH Connection");
			session.disconnect();
		}
	}

/*	public static void executeCommandToFile(String command, String fileName, String filePath) {
		PrintStream outputStream = null;
		try {
			outputStream = new PrintStream(filePath + "\\" + fileName, "UTF-8");
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();

			channel.connect();
			
			byte[] tmp = new byte[1024*32];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024*32);

					if (i < 0) {
						break;
					}
					outputStream.write(tmp,0,i);
				}
				if (channel.isClosed()) {
					if (in.available() > 0) {
						continue;
					}
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
			channel.disconnect();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		finally
		{
			if(outputStream!=null)
				outputStream.close();
		}
	}*/
	
	public static boolean abortCommand()
	{
		if(abortableChannel!=null && abortableChannel.isConnected())
		{
			abortableChannel.disconnect();
			return true;
		}
		return false;
	}
	public static boolean commandAbortable()
	{
		return (abortableChannel!=null && abortableChannel.isConnected());
	}
	
	public static void executeCommandToFile(String command, String fileName, String filePath)
	{
		executeCommandToFile(command, fileName, filePath, "");
	}
	
	public static void executeCommandToFile(String command, String fileName, String filePath, String header) {
		PrintStream outputStream = null;
		Logger logger = AppLogger.getLogger();
		logger.info(command);
		try {
			if(abortableChannel!=null && abortableChannel.isConnected())
			{
				throw new IllegalStateException("Another command is already executing.");
			}
			outputStream = new PrintStream(filePath + "\\" + fileName, "UTF-8");
			abortableChannel = session.openChannel("exec");
			((ChannelExec) abortableChannel).setCommand(command);

			((ChannelExec) abortableChannel).setErrStream(System.err);

			InputStream in = abortableChannel.getInputStream();

			abortableChannel.connect();
			
			byte[] tmp = new byte[1024*32];
			byte[] headerBytes = header.getBytes("UTF-8");
			byte[] largeArray = new byte[(1024*1024*2)+(1024*32)+headerBytes.length];
			int largeArrayIndex=0;
			if(header.length()>0)
			{
				System.arraycopy(header.getBytes("UTF-8"),0,largeArray,0,headerBytes.length);
				largeArrayIndex+=headerBytes.length;
			}
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024*32);

					if (i < 0) {
						break;
					}
					System.arraycopy(tmp,0,largeArray,largeArrayIndex,i);
					largeArrayIndex+=i;
					if(largeArrayIndex >= 1024*1024*2)
					{
						outputStream.write(largeArray,0,largeArrayIndex);
						largeArrayIndex=0;
					}
				}
				if (abortableChannel.isClosed()) {
					if (in.available() > 0) {
						continue;
					}
					if(largeArrayIndex > 0)
					{
						outputStream.write(largeArray,0,largeArrayIndex);
						largeArrayIndex=0;
					}
					int exitStatus = abortableChannel.getExitStatus();
					/*System.out.println("exit-status: "
							+ exitStatus);*/
					if(exitStatus == 0)
						logger.info("Command Completed With Exit Status: "+exitStatus);
					else
						logger.warning("Command Completed With Exit Status: "+exitStatus);
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
			abortableChannel.disconnect();
		} catch (Exception e) {
			logger.log(Level.SEVERE, "An Unexpected Exception Occured", e);
			e.printStackTrace();
		}
		finally
		{
			if(outputStream!=null)
				outputStream.close();
		}
	}

	
	
	public static String executeCommand(String command) {
		String output = "";
		Logger logger = AppLogger.getLogger();
		logger.info(command);
		try {
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(command);

			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();

			channel.connect();
			
			byte[] tmp = new byte[102400];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 102400);

					if (i < 0) {
						break;
					}
					output += (new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0) {
						continue;
					}
					int exitStatus = channel.getExitStatus();
					/*System.out.println("exit-status: "
							+ exitStatus);*/
					if(exitStatus == 0)
						logger.info("Command Completed With Exit Status: "+exitStatus);
					else
						logger.warning("Command Completed With Exit Status: "+exitStatus);
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					ee.printStackTrace();
				}
			}
			channel.disconnect();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		System.out.println(output);
		return output;
	}
	
	public static String useSudo(String command, String SudoPass) {
		String output = "";
		try {
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("sudo -S -p '' "+command);

			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();
			OutputStream out=channel.getOutputStream();
			
			channel.connect();
			
			
			//if(!SudoPass.equals(null) || !SudoPass.equals("")){
			out.write((SudoPass+"\n").getBytes());
		    out.flush();
			//}
			
			
			byte[] tmp = new byte[102400];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 102400);

					if (i < 0) {
						break;
					}
					output += (new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0) {
						continue;
					}
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		System.out.println(output);
		return output;
	}
	
	public static String useSu(String command, String SudoPass) {
		String output = "";
		try {
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand("su -c \""+command+"\"");

			((ChannelExec) channel).setErrStream(System.err);

			InputStream in = channel.getInputStream();
			OutputStream out=channel.getOutputStream();
			
			channel.connect();
			
			
			//if(!SudoPass.equals(null) || !SudoPass.equals("")){
			out.write((SudoPass+"\n").getBytes());
		    out.flush();
			//}
			
			
			byte[] tmp = new byte[102400];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 102400);

					if (i < 0) {
						break;
					}
					output += (new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0) {
						continue;
					}
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
				}
			}
			channel.disconnect();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		System.out.println(output);
		return output;
	}
	
	public static boolean writeStringToFile(String output, String fileName,
			String filePath) {
		PrintWriter writer = null;
		try {

			writer = new PrintWriter(filePath + "\\" + fileName, "UTF-8");
			writer.print(output);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			// close file
			writer.close();
			
		}

	}

	public static boolean getOsDetails(String path) {
		Date date = new Date();
		String output = "";
		try {
			
			String OsVersion = SSHTunnel
					.executeCommand("cat /etc/issue | grep SUSE");
			OsVersion = OsVersion.substring(OsVersion.indexOf('S'),
					OsVersion.length());
			OsVersion = OsVersion.substring(0, OsVersion.indexOf('-') - 1);
			
			// Memory per node
			
			String MemTotal = SSHTunnel
					.executeCommand("cat /proc/meminfo | grep MemTotal");
			MemTotal = (MemTotal.substring(MemTotal.indexOf(':') + 1,
					MemTotal.length())).trim();
			
			String TDInfo = SSHTunnel.executeCommand("tdinfo");



			output += "######################################################################################"
					+ "\n";
			output += "###                                                                                 ##"
					+ "\n";
			output += "###                                PUA Node Details                                 ##"
					+ "\n";
			output += "###                                                                                 ##"
					+ "\n";
			output += "######################################################################################"
					+ "\n";

			output += "Collected on: " + date.toString() + "\n";
			output += "" + "\n";
			output += "OS Version: \t\t" + OsVersion + "\n";
			output += "Total Memory per node: \t" + MemTotal + "\n";
			output += TDInfo + "\n";
			
			writeStringToFile(output, "OSDetails.out", path);
			

			// geting the vconfig file
			PrintWriter vconfigwriter = new PrintWriter(path + "\\"
					+ "vconfig.out", "UTF-8");
			
			vconfigwriter
					.print(SSHTunnel
							.executeCommand("cat /etc/opt/teradata/tdconfig/vconfig.txt"));
			

			vconfigwriter.close();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}

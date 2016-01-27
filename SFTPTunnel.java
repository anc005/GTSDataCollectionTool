/*
 * SSHTunnel.java
 *
 * A collection of static functions that utilize the open jsch library to
 * setup ssh connections, port forward, execute commands through java
 *
 * developed and maintained by: Raed Rizk Soliman
 * Last Edit: 04/07/2015
 */

import javax.swing.ProgressMonitor;
import javax.swing.UIManager;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpProgressMonitor;

public class SFTPTunnel {

	static final int lport = 3338;
	static final int rport = 1025;
	static Session session = null;
	static Session[] sessions = null;
	static String[][] OsDetails = null;

	public static boolean createConnection(String[] hosts, String[] usernames,
			String[] passwords) {

		try {
			System.out.println("Creating JSCH connection");
			JSch jsch = new JSch();

			sessions = new Session[hosts.length];

			String host = hosts[0];
			String user = usernames[0];


			sessions[0] = session = jsch.getSession(user, host, 22);
			sessions[0].setConfig("StrictHostKeyChecking", "no");
			session.setPassword(passwords[0]);
			session.connect();
			System.out.println("The session has been established to " + user
					+ "@" + host);



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

	public static void getFile(String fileName, String location) {
		
		try {
			Channel channel=session.openChannel("sftp");
		      channel.connect();
		      ChannelSftp c=(ChannelSftp)channel;
		      
		      SftpProgressMonitor monitor=new MyProgressMonitor();
			    
		      int mode=ChannelSftp.OVERWRITE;
		      if (location == null)
		    	  location = ".";
		      
		      c.get(fileName, location, monitor, mode);
		      
		      c.exit();
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		
	}
	  public static class MyProgressMonitor implements SftpProgressMonitor{
		    ProgressMonitor monitor;
		    long count=0;
		    long max=0;
		    public void init(int op, String src, String dest, long max){
		      this.max=max;
		      try
		      {
		          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		      }
		      catch (Exception e)
		      {
		          // ...
		      }
		      monitor=new ProgressMonitor(null, 
		                                  "Downloading:"+src,"retrieving through SFTP",  0, (int)max);
		      count=0;
		      percent=-1;
		      monitor.setProgress((int)this.count);
		      monitor.setMillisToDecideToPopup(1000);
		    }
		    private long percent=-1;
		    public boolean count(long count){
		      this.count+=count;
		 
		      if(percent>=this.count*100/max){ return true; }
		      percent=this.count*100/max;
		 
		      monitor.setNote("Completed "+this.count+"("+percent+"%) out of "+max+".");     
		      monitor.setProgress((int)this.count);
		 
		      return !(monitor.isCanceled());
		    }
		    public void end(){
		      monitor.close();
		    }
		  }
		 
}

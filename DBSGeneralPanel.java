import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

import javax.swing.*;




public class DBSGeneralPanel extends JPanel {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel lblConnectionDetails;
	String outputLocation=".";
	JTextArea DBSStatusLabel;
	JTextArea VProcManagerLabel;
	private JScrollPane scrollPane_2;
	private JPanel panel;
	private JPanel dbsSubPanel;
	private CardLayout dbsSubPanelLayout = new CardLayout();
	private JPanel dbsStatusPanel;
	public boolean done = false;
	private JLabel lblDbsGeneral;
	private JLabel lblDbsVersion;
	private JLabel lblPdeVersion;
	private JLabel DBSVersionLabel;
	private JLabel PDEVersionLabel;
	private JLabel lblOsVersion;
	private JLabel OSVersionLabel;
	private JLabel lblCtlSettings;
	private JLabel lblStartDbs;
	private JLabel lblEnableLogons;
	private JLabel lblSaveDumps;
	private JLabel lblMaxDumps;
	private JLabel lblSnapshotCrash;
	private JLabel lblBreakStop;
	private JLabel ctl0label;
	private JLabel ctl4label;
	private JLabel ctl2label;
	private JLabel ctl5label;
	private JLabel ctl1label;
	private JLabel ctl6label;
	private JButton btnNewButton;
	private JButton btnBackButton;
	private JButton btnFwdButton;
	private JButton btnFileButton;
	private JButton btnAbortButton;
	private JLabel lblConnectedTo;
	private JLabel hostNameLabel;
	
	private JPanel dbsDataCollectionPanel = new JPanel();
	private JPanel dbsDataCollectionTopPanel = new JPanel();
	private JPanel dbsDataCollectionBottomPanel = new JPanel();
	
	private JCheckBox allCheckBox = new JCheckBox("All");
	private JCheckBox generalCheckBox = new JCheckBox("General");
	private JCheckBox restartCheckBox = new JCheckBox("Restart");
	private JCheckBox hangCheckBox = new JCheckBox("Hang");
	private JCheckBox miscCheckBox = new JCheckBox("Misc");
	private JCheckBox sysPerfCheckBox = new JCheckBox("System Performance");
	
	
	private JPanel dbsDataCollectionBottomFirstPanel = new JPanel();
	private JPanel dbsDataCollectionBottomSecondPanel = new JPanel();
	private JPanel dbsDataCollectionBottomThirdPanel = new JPanel();
	
	
	private JPanel generalPanel = new JPanel();
	private JPanel restartPanel = new JPanel();
	private JPanel hangPanel = new JPanel();
	private JPanel sysPerfPanel = new JPanel();
	private JPanel miscPanel = new JPanel();

	
	private JPanel submitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
	private JButton proceedButton = new JButton("Collect DBS Data");
	private JButton collectButton = new JButton("Collect DBS Data");
	
	
	private JCheckBox[] generalCheckBoxes;
	private JCheckBox[] restartExtCheckBoxes;
	private JCheckBox[] restartCheckBoxes;
	private JCheckBox[] hangExtCheckBoxes;
	private JCheckBox[] hangCheckBoxes;
	private JCheckBox[] sysPerfExtCheckBoxes;
	private JCheckBox[] sysPerfCheckBoxes;
	private JCheckBox[] miscCheckBoxes;
	private JCheckBox[] allCheckBoxes;
	
	private JCheckBox[] hangRestrictedCheckBoxes;
	private Hashtable<JCheckBox, Command> commandTable = new Hashtable<JCheckBox, Command>();
	private boolean allPanelsVisible = true;
	private CollectionWorker collectionWorker;
	private Logger logger = null; 
	private ProgressDialog progressDialog;
	private JButton btnConsoleButton;
	private JTextArea progressArea;
	private SystemInfoRecord systemInfo = new SystemInfoRecord(21,'+'," System Information ",15);

	
	private long getAvailableServerSpace()
	{
		
		/*Can be used before running commands which can possibly return huge volume of result data.
		The intent is to check if there is plenty of available space under the file system on which serverSideDBSPath is mounted
		See lokdisp command for example.*/
		
		long size;
		String commandText = "df /home/tdc "
				+ "| awk '{ print $4 }' "
				+ "| grep -qi available " //Verifies "Available" is the fourth column
				+ "&& df /home/tdc "
				+ "| awk '$4 ~ /[[:digit:]]+/ { print $4 }'"; //Prints fourth column.
		logger.info("Calculating available server space under /home/tdc:"/*+commandText*/);
		String stringSize = SSHTunnel.executeCommand(commandText); 
		try
		{
			size = Long.parseLong(stringSize.trim());
		}
		catch(Exception e)
		{
			return -1l;
		}
		return size;
	}
	private String getLeftOverProcesses(String serverSideDBSPath)
	{
		/*This method is called from CommandWorker during the finalize phase (after all commands have been completed or collection has been aborted),
		 * to check the PIDs that are kept track of in PIDs.out (See keepTrackOfPIDs()), and compare them against active processes,
		 *  to identify any possible left over processes.*/
		
		String fileName = "PIDs.out";
		String output = null;
		for(int i=0;i<4;i++) /*Will attempt to check four times with 3 seconds between each two attempts.*/
		{
			/*To print full process details of current PIDs that match the PIDs which are kept track of.*/
			String commandText = "cd "+serverSideDBSPath
					+" >/dev/null 2>&1 && ls "
					+fileName
					+" >/dev/null 2>&1 && ps -fp $(ps -ef "   
					+ "| head -1 "
					+ "| awk '{print $2}' "
					+ "| grep -qi pid "
					+ "&& ps -ef "
					+ "| awk '$2 ~ /[[:digit:]]+/ { print $2 }' "
					+ "| grep -xf "
					+fileName
					+") 2> /dev/null || true"; /*short circuit ORing with true to suppress any non-zero exit status*/
			logger.info("Checking for suspect left over processes:"/*+commandText*/);
			output = SSHTunnel.executeCommand(commandText);
			if(output!=null && !output.trim().equals(""))
			{
				try
				{
					Thread.sleep(3000);
				}
				catch(Exception e)
				{}
			}
			else
				break;
		}
		return output!=null?output.trim():"";
	}
	
	private String keepTrackOfPIDs(String command, String serverSideDBSPath)
	{
		/*To be utilized by commands as needed, so the process ID associated with the command can be kept track of in PIDs.out.
		 * getLeftOverProcesses() will be called later to check against running PIDs for identifying possible left over processes.*/
		
		String fileName = "PIDs.out";
		String output = "mkdir -p "
				+ serverSideDBSPath
				+ " ; "
				+ command
				+ " & echo $! >> " //echo $! is run in parallel with the command to find its PID, and direct it to PIDs.out
				+ serverSideDBSPath
				+ "/"
				+ fileName
				+ " & wait"; //wait is run to capture the command output.
		return output;
	}
	private String verifyCNSScreenCount(String command)
	{
		/*If cnstool is run while all the interactive windows are busy, it will go in a loop and will not respond.
		 * The intent of this method is to modify the command so it will check the count of active partitions.
		 * If 6 then error out, and not run cnstool.*/
		
		String output = "if cnscim -s | egrep 'Screen[ ]+.[ ]+--[ ]+' | wc -l | grep -qE '[12345]'; then "
				+command
				+" ; else echo 'All Interactive Partitions are busy!!' && false; fi;";
		return output;
	}

	/**
	 * Create the panel.
	 */
	public DBSGeneralPanel() {

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/* To add new command:       
		(1) Instantiate the command's JCheckBox																		
		(2) Add the instantiated JCheckBox to the proper array.
		...ExtCheckBoxes arrays represent the checkboxes that should be auto-checked by a specific area checkbox, despite being under different area panels. 																
		e.g, "ctl" is under "General" area, but should still be auto-checked by "System Perf" checkbox.			
		To accomplish this, add ctlCheckBox to sysPerfExtCheckBoxes, as well as generalCheckBoxes.				
		(3) Instantiate corresponding anonymous class object extending Command abstract class, and override run() method to implement the server side logic.
		Do not use SwingWorker or Threads to implement the server side logic. run() is already called from within SwingWorker execution.
		If confirmation is needed for the command's checkbox to be enabled, use the overloaded constructor Command(JCheckBox, String).
		e.g, 
				Command pumaM = new Command(pumaMCheckBox,"Please verify no customer restrictions exist on running this command...\nAre you sure?")
				{....
		If the checkbox just needs to be always unchecked by default (without confirmation when enabling it), use the overloaded constructor Command(JCheckBox, boolean).
		e.g,
				Command pumaM = new Command(pumaMCheckBox,false)
				{....
				
		If data needs to be staged on the server (e.g, gathering messages logs from other nodes), serverSideDBSPath is pre-initialized with a timestamped string. 
		Use mkdir -p to create serverSideDBSPath, or a subdirectory under it.
		-p is important to assure the server [sub]directory is created regardless of its parents' pre-existance and regardless of the sequence in which the commands are executed.
		See messages command for example.
		
		Various convenience methods can be used:
		 	. getAvailableServerSpace(). See lokdisp for example
		 	. keepTrackOfPIDs(). See showlocks for example
		 	. verifyCNSScreenCount(). See showlocks for example
	 */
	  
		//(1):	
		//JCheckBox testCheckBox = new JCheckBox("test");
		
		JCheckBox messagesCheckBox = new JCheckBox("Messages");
		JCheckBox vConfigCheckBox = new JCheckBox("VConfig");
		JCheckBox dbsControlCheckBox = new JCheckBox("DBSControl");
		JCheckBox vprocManagerCheckBox = new JCheckBox("vprocmngr");
		JCheckBox ctlCheckBox = new JCheckBox("ctl");
		
		
		JCheckBox tpaTraceCheckBox = new JCheckBox("TPA Trace");
		JCheckBox cspCheckBox = new JCheckBox("csp");
		
	
		
		JCheckBox sarCheckBox = new JCheckBox("sar");
		//JCheckBox saCheckBox = new JCheckBox("/var/log/sa"); //Pre-collecting sa from all nodes to one node could heavily consume node space. Holding off on it for now. 
		JCheckBox topCheckBox = new JCheckBox("top");
		JCheckBox awtMonCheckBox = new JCheckBox("awtmon");
		JCheckBox ampLoadCheckBox = new JCheckBox("ampload");
		JCheckBox pumaTAXOCheckBox = new JCheckBox("puma -TAXO");
		JCheckBox sarDCheckBox = new JCheckBox("sar -d");
		JCheckBox sarRCheckBox = new JCheckBox("sar -r");
		JCheckBox pumaMCheckBox = new JCheckBox("puma -m");
		JCheckBox gtwGlobalCheckBox = new JCheckBox("gtwglobal");
		JCheckBox psElTgrepTCheckBox = new JCheckBox("ps -elT | grep ' T '");
		
		
		JCheckBox tdwmdmpCheckBox = new JCheckBox("tdwmdmp -a");
		JCheckBox tdwmdmpVCheckBox = new JCheckBox("tdwmdmp -v");
		JCheckBox qrysessnCheckBox = new JCheckBox("qrysessn");
		JCheckBox sar55CheckBox = new JCheckBox("sar 5 5");
		JCheckBox showLocksCheckBox = new JCheckBox("showlocks");
		JCheckBox lokDispCheckBox = new JCheckBox("lokdisp");
		JCheckBox pumaZCheckBox = new JCheckBox("puma -Z");
		JCheckBox tskListCheckBox = new JCheckBox("tsklist");
		
		
		JCheckBox whoBCheckBox = new JCheckBox("who -b");
		//JCheckBox duShCheckBox = new JCheckBox("du -sh *"); This command usually takes long time. May not be needed anyways
		JCheckBox dfHCheckBox = new JCheckBox("df -h");
		JCheckBox bamSCheckBox = new JCheckBox("bam -s");
 
 
		//(2):
		generalCheckBoxes = new JCheckBox[] {messagesCheckBox, dbsControlCheckBox, ctlCheckBox, vprocManagerCheckBox/*, testCheckBox*/};
		restartExtCheckBoxes =  new JCheckBox[] {messagesCheckBox, dbsControlCheckBox, ctlCheckBox, vprocManagerCheckBox};
		restartCheckBoxes = new JCheckBox[] {tpaTraceCheckBox, cspCheckBox};
		hangExtCheckBoxes = new JCheckBox[] {dbsControlCheckBox, bamSCheckBox, messagesCheckBox, ctlCheckBox};
		hangCheckBoxes = new JCheckBox[] {sarCheckBox, /*saCheckBox,*/ topCheckBox, awtMonCheckBox, ampLoadCheckBox, pumaTAXOCheckBox, sarRCheckBox, pumaMCheckBox, gtwGlobalCheckBox, psElTgrepTCheckBox};
		sysPerfExtCheckBoxes = new JCheckBox[] {/*saCheckBox,*/sarRCheckBox, awtMonCheckBox, ampLoadCheckBox, pumaTAXOCheckBox,sarCheckBox,topCheckBox,ctlCheckBox, pumaMCheckBox, gtwGlobalCheckBox};
		sysPerfCheckBoxes = new JCheckBox[] {sarDCheckBox, tdwmdmpCheckBox, tdwmdmpVCheckBox, qrysessnCheckBox, showLocksCheckBox, sar55CheckBox, pumaZCheckBox, lokDispCheckBox, tskListCheckBox};
		miscCheckBoxes = new JCheckBox[] {whoBCheckBox/*, duShCheckBox*/, dfHCheckBox, vConfigCheckBox, bamSCheckBox};
		
		hangRestrictedCheckBoxes = new JCheckBox[] {qrysessnCheckBox, showLocksCheckBox, lokDispCheckBox, tdwmdmpCheckBox, tdwmdmpVCheckBox};
		
		//(3):
		
		/*new Command(testCheckBox, false)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText="mkdir -p "+ 
						serverSideDBSPath;
				SSHTunnel.executeCommand(commandText);
				if(getAvailableServerSpace() > 2097152l)
				{
					commandText = "cat /spare/puma.t > "+serverSideDBSPath+"/puma.t";
					SSHTunnel.executeCommand(commandText);
					
					try {
						SFTPTunnel.getFile(serverSideDBSPath+"/puma.t", clientSideDBSPath);
					} 
					catch (Exception e) {
					}
				}
				else
				{
					commandText = "cat /spare/puma.t";
					//String output = SSHTunnel.executeCommand(commandText);
					//SSHTunnel.writeStringToFile(output,"test.txt",clientSideDBSPath);
					SSHTunnel.executeCommandToFile(commandText, "test.txt", clientSideDBSPath);
				}
				
			}
		};*/
		
		new Command(messagesCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				//&& is used to chain the commands so no subsequent command will run unless the preceding one is successfull.
				String commandText = "mkdir -p "+ 
				serverSideDBSPath+
				"/varmsgs && /usr/pde/bin/pcl -collect /var/log/messages "+
				serverSideDBSPath+
				"/varmsgs && cd "+
				serverSideDBSPath+
				"/varmsgs && zip all_messages.zip messages.* && rm messages.*";
				//logger.info(commandText);				
				SSHTunnel.executeCommand(commandText);
				logger.info("Downloading: \""+serverSideDBSPath+"/varmsgs/all_messages.zip\"" );
				try {
					SFTPTunnel.getFile(serverSideDBSPath+"/varmsgs/all_messages.zip", clientSideDBSPath);
				} 
				catch (Exception e) {
				}
				finally {}
				
			}
		};
		new Command(vConfigCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				logger.info("Downloading: \"/etc/opt/teradata/tdconfig/vconfig.txt\"");
				try {
					SFTPTunnel.getFile("/etc/opt/teradata/tdconfig/vconfig.txt", clientSideDBSPath);
				}
				catch (Exception e) {
				} 
				finally {}
			}
		};		
		new Command(dbsControlCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "dbscontrol <<- [DBSC]\n"+
				"m i=true\n"+
				"d\n"+
				"quit\n"+
				"Q\n"+
				"[DBSC]";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" DBSControl ");
				commandInfo.setCommand(commandText);
				SSHTunnel.executeCommandToFile(commandText, "dbscontrol.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(ctlCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "ctl <<- [CTL]\n"+
				"sc ve\n"+
				"sc dbs\n"+
				"sc de\n"+
				"sc rss\n"+
				"sc tvs\n"+
				"print all\n"+
				"quit\n"+
				"[CTL]";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" ctl ");
				commandInfo.setCommand(commandText);
				SSHTunnel.executeCommandToFile(commandText, "ctl.txt", clientSideDBSPath, commandInfo.stringValue());
			}
		};
		new Command(vprocManagerCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "vprocmanager <<- [VPRCMNGR]\n"
						+ "st\n"
						+ "quit\n"
						+ "[VPRCMNGR]";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" VPROCManager ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("For displaying the status of the system VPROCs and physical configuration.");
				SSHTunnel.executeCommandToFile(commandText, "vprocmanager.txt", clientSideDBSPath, commandInfo.stringValue());
			}
		};
		new Command(tpaTraceCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "tpatrace 5 | grep -i \"reset rec\"\n"+
				"tpatrace";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" TPATrace ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("For displaying the details of the recent restart cycles.");
				SSHTunnel.executeCommandToFile(commandText, "tpatrace.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(cspCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "csp\n"+
				"csp -mode list -source table";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" csp ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("For displaying Database dumps.");
				SSHTunnel.executeCommandToFile(commandText, "csp.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(sarCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho sar";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" sar ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays system performance information for the current day at 5 minute intervals.");
				SSHTunnel.executeCommandToFile(commandText, "sar.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		/*new Command(saCheckBox)
		{
			protected void run()
			{

				
			}
		};*/
		new Command(topCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho /usr/bin/top -b -n 4 -H";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" top ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays CPU and memory consumption of the currently active threads.");
				SSHTunnel.executeCommandToFile(commandText, "top.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(awtMonCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "awtmon -s";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" awtmon ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays real time AMP Worker Task Inuse Counts for all AMPs.");
				SSHTunnel.executeCommandToFile(commandText, "awtmon.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(ampLoadCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho \"ampload -a\"";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" ampload -a ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays real time AMP Worker Task Inuse, Available, and Message Counts for all AMPs.");
				SSHTunnel.executeCommandToFile(commandText, "ampload.txt", clientSideDBSPath, commandInfo.stringValue());
			}
		};
		new Command(pumaTAXOCheckBox,"Please verify no customer restrictions exist on running this command...\nAre you sure?")
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho puma -TAXO";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" puma -TAXO ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays details of the sessions/transactions associated with the current active Database threads.");
				SSHTunnel.executeCommandToFile(keepTrackOfPIDs(commandText,serverSideDBSPath), "puma.TAXO", clientSideDBSPath,commandInfo.stringValue());				
			}
		};
		new Command(sarDCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho sar -d";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" sar -d ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays device IO details for investigating suspect slow LUNs / drives.");
				SSHTunnel.executeCommandToFile(commandText, "sar.d", clientSideDBSPath,commandInfo.stringValue());	
			}
		};
		new Command(sarRCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				//On SLES11, run sar -S to get the output equivalent to sar -r on SLES10. Then run sar -r anyways.
				String commandText = "egrep -q 'VERSION = 1[12]' /etc/SuSE-release && psh -netecho sar -S\n"+
					"psh -netecho sar -r\n"
					+ "psh -netecho sar -W";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" sar -S/r/W ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays performance information related to node memory swapping and paging.");
				SSHTunnel.executeCommandToFile(commandText, "sar.r", clientSideDBSPath, commandInfo.stringValue());
				
			}
		};
		new Command(pumaMCheckBox,"Please verify no customer restrictions exist on running this command...\nAre you sure?")
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho puma -m";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" puma -m ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays Database message counts and mailbox information, for investigating congested AMPs and Flow Control problems.");
				SSHTunnel.executeCommandToFile(keepTrackOfPIDs(commandText,serverSideDBSPath), "puma.m", clientSideDBSPath,commandInfo.stringValue());		
			}
		};
		new Command(tdwmdmpCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "tdwmdmp -a";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" tdwmdmp -a ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays current TDWM settings, and ruleset throttle statistics.");
				SSHTunnel.executeCommandToFile(commandText, "tdwmdmp.a", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(tdwmdmpVCheckBox,Command.DBSLOGONREQUIRED)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "tdwmdmp -v -u "
						+ this.getDBSUserName()
						+ " -p xxxx";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" tdwmdmp -v ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays current TDWM settings, and ruleset throttle statistics. Requires Database login");
				AppLogger.flagCommand(commandText.replace("xxxx", this.getDBSPassword()), commandText);
				SSHTunnel.executeCommandToFile(commandText.replace("xxxx", this.getDBSPassword()), "tdwmdmp.v", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(sar55CheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "sar 5 5";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" sar 5 5 ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays 5 snapshots of sar command results at 5 second intervals, for investigating live performance problems");
				SSHTunnel.executeCommandToFile(commandText, "sar_5_5.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(pumaZCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho puma -Z";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" puma -Z ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays syszone (Database Memory segment consumption) information");
				SSHTunnel.executeCommandToFile(keepTrackOfPIDs(commandText, serverSideDBSPath), "puma-Z.txt", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(tskListCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				
				String commandText="mkdir -p "+ 
						serverSideDBSPath;
				SSHTunnel.executeCommand(commandText);
				if(getAvailableServerSpace() > 2097152l)
				{
					commandText = "psh -netecho \"tsklist -a\" > "+serverSideDBSPath+"/tsklist.txt\n";
					SSHTunnel.executeCommand(commandText);
					try 
					{
						SFTPTunnel.getFile(serverSideDBSPath+"/tsklist.txt", clientSideDBSPath);
					} 
					catch (Exception e)
					{}
				}
				else
				{
					commandText = "psh -netecho tsklist -a";
					CommandInfoRecord commandInfo = new CommandInfoRecord(" tsklist -a ");
					commandInfo.setCommand(commandText);
					commandInfo.setDescription("Returns detailed active node process details and state information.");
					SSHTunnel.executeCommandToFile(commandText, "tsklist.txt", clientSideDBSPath, commandInfo.stringValue());
				}
			}
		};
		new Command(whoBCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho who -b";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" who -b ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Returns last boot date and time");
				SSHTunnel.executeCommandToFile(commandText, "who.b", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		/*new Command(duShCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho du -sh";
				//logger.info(commandText);
				SSHTunnel.executeCommandToFile(commandText, "du.sh", clientSideDBSPath,"## "+commandText+"\n\n");
			}
		};*/
		new Command(dfHCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho df -h";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" df -h ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays Inuse and Available space on mounted node file systems");
				SSHTunnel.executeCommandToFile(commandText, "df.h", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		
		new Command(psElTgrepTCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "psh -netecho \"ps -elT | grep ' T '\"";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" Stopped process details ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays details of stopped processes that may possibly cause DBS Hang scenarios");
				SSHTunnel.executeCommandToFile(commandText, "Stopped_process.out", clientSideDBSPath,commandInfo.stringValue());
			}
		};
		new Command(bamSCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "bam -s";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" bam -s ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays Bynet software versions and connection states");
				SSHTunnel.executeCommandToFile(commandText, "bam.s", clientSideDBSPath,commandInfo.stringValue());
			}			
		};
		new Command(gtwGlobalCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = "tdatcmd <<- [TDTCMD]\n"
						+ "xgtwglobal -nw <<- [GTWGLBL]\n"
						+ "di ne long\n"
						+ "di gtw all\n"
						+ "quit\n"
						+ "[GTWGLBL]\n"
						+ "exit\n"
						+ "[TDTCMD]";
				CommandInfoRecord commandInfo = new CommandInfoRecord(" gtwglobal ");
				commandInfo.setCommand(commandText);
				commandInfo.setDescription("Displays states of sessions at the gateways level");
				SSHTunnel.executeCommandToFile(commandText, "gtwglobal.txt",  clientSideDBSPath, commandInfo.stringValue()); 
			}
		};
		new Command(qrysessnCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = verifyCNSScreenCount(keepTrackOfPIDs("cnsrun -utility qrysessn -multi -commands \"{*} {*} {y} { }\" -output",serverSideDBSPath));
				CommandInfoRecord commandInfo = new CommandInfoRecord(" qrysessn ");
				commandInfo.setCommand("cnsrun -utility qrysessn -multi -commands \"{*} {*} {y} { }\" -output");
				commandInfo.setDescription("Displays online Database session states.\n*** The above command was run as part of a larger script. Check the log for details.");
				SSHTunnel.executeCommandToFile(commandText, "qrysessn.txt",  clientSideDBSPath, commandInfo.stringValue());
			}
		};
		new Command(showLocksCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{
				String commandText = verifyCNSScreenCount(keepTrackOfPIDs("cnsrun -utility showlocks -multi -commands \"{}\" -output",serverSideDBSPath));
				CommandInfoRecord commandInfo = new CommandInfoRecord(" showlocks ");
				commandInfo.setCommand("cnsrun -utility showlocks -multi -commands \"{}\" -output");
				commandInfo.setDescription("Displays Database active Host Utility Locks.\n*** The above command was run as part of a larger script. Check the log for details.");
				SSHTunnel.executeCommandToFile(commandText, "showlocks.txt",  clientSideDBSPath, commandInfo.stringValue());
			}
		};
		new Command(lokDispCheckBox)
		{
			protected void run(String serverSideDBSPath, String clientSideDBSPath)
			{		
				String commandText="mkdir -p "+ 
						serverSideDBSPath;
				SSHTunnel.executeCommand(commandText);
				if(getAvailableServerSpace() > 2097152l)
				{
					commandText = "tdatcmd <<- [TDTCMD] > "+serverSideDBSPath+"/lokdisp.txt\n"
							+ "lokdisp <<- [LKDSP]\n"
							+ "blockers\n"
							+ "all\n"
							+ "quit\n"
							+ "[LKDSP]\n"
							+ "exit\n"
							+ "[TDTCMD]";
					//logger.info(commandText);
					SSHTunnel.executeCommand(commandText);
					try 
					{
						SFTPTunnel.getFile(serverSideDBSPath+"/lokdisp.txt", clientSideDBSPath);
					} 
					catch (Exception e)
					{}
				}
				else
				{
					commandText = "tdatcmd <<- [TDTCMD]\n"
							+ "lokdisp <<- [LKDSP]\n"
							+ "blockers\n"
							+ "all\n"
							+ "quit\n"
							+ "[LKDSP]\n"
							+ "exit\n"
							+ "[TDTCMD]";
					CommandInfoRecord commandInfo = new CommandInfoRecord(" lokdisp ");
					commandInfo.setCommand(commandText);
					commandInfo.setDescription("Displays details of transactions that are involved in Database blockings");
					SSHTunnel.executeCommandToFile(commandText, "lokdisp.txt", clientSideDBSPath, commandInfo.stringValue());
				}
			}
		};
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		setBackground(Color.WHITE);
		setLayout(null);
		
		scrollPane_2 = new JScrollPane();
		scrollPane_2.setBounds(0, 0, 794, 399);
		add(scrollPane_2);
		
		panel = new JPanel();
		panel.setBackground(Color.WHITE);
		scrollPane_2.setViewportView(panel);
		panel.setLayout(null);
		
		progressDialog = new ProgressDialog((JFrame) SwingUtilities.getWindowAncestor(this),"Progress");
		progressArea = progressDialog.getTextArea();

		lblConnectionDetails = new JLabel("DBS/PDE status:", SwingConstants.RIGHT);
		lblConnectionDetails.setBounds(10, 10, 91, 14);
		lblConnectionDetails.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblConnectionDetails.setForeground(new Color(244, 106, 9));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(111, 10, 652, 72);
		
		DBSStatusLabel = new JTextArea();
		scrollPane.setViewportView(DBSStatusLabel);
		DBSStatusLabel.setEditable(false);
		
		JLabel Vproc = new JLabel("VPROC status:", SwingConstants.RIGHT);
		Vproc.setBounds(10, 95, 91, 14);
		Vproc.setForeground(new Color(244, 106, 9));
		Vproc.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(111, 95, 652, 98);
		
		VProcManagerLabel = new JTextArea();
		scrollPane_1.setViewportView(VProcManagerLabel);
		VProcManagerLabel.setEditable(false);
		
		lblDbsGeneral = new JLabel("DBS - General");
		lblDbsGeneral.setForeground(new Color(244, 106, 9));
		lblDbsGeneral.setFont(new Font("Tahoma", Font.BOLD, 32));
		lblDbsGeneral.setBounds(302, 11, 242, 37);
		panel.add(lblDbsGeneral);
		
		lblDbsVersion = new JLabel("DBS Version:", SwingConstants.RIGHT);
		lblDbsVersion.setForeground(new Color(244, 106, 9));
		lblDbsVersion.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblDbsVersion.setBounds(10, 73, 91, 14);
		panel.add(lblDbsVersion);
		
		lblPdeVersion = new JLabel("PDE Version:", SwingConstants.RIGHT);
		lblPdeVersion.setForeground(new Color(244, 106, 9));
		lblPdeVersion.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPdeVersion.setBounds(212, 73, 91, 14);
		panel.add(lblPdeVersion);
		
		DBSVersionLabel = new JLabel("");
		DBSVersionLabel.setBounds(111, 73, 91, 14);
		panel.add(DBSVersionLabel);
		
		PDEVersionLabel = new JLabel("");
		PDEVersionLabel.setBounds(313, 73, 91, 14);
		panel.add(PDEVersionLabel);
		
		proceedButton.setVisible(true);
		submitButtonPanel.add(proceedButton);
		collectButton.setVisible(false);
		submitButtonPanel.add(collectButton);
		
		lblOsVersion = new JLabel("OS Version:", SwingConstants.RIGHT);
		lblOsVersion.setForeground(new Color(244, 106, 9));
		lblOsVersion.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblOsVersion.setBounds(414, 73, 91, 14);
		panel.add(lblOsVersion);
		
		OSVersionLabel = new JLabel("");
		OSVersionLabel.setBounds(515, 73, 248, 14);
		panel.add(OSVersionLabel);
		
		lblCtlSettings = new JLabel("CTL Settings:", SwingConstants.RIGHT);
		lblCtlSettings.setForeground(new Color(244, 106, 9));
		lblCtlSettings.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblCtlSettings.setBounds(0, 195, 91, 14);
		
		
		lblStartDbs = new JLabel("Start DBS:", SwingConstants.RIGHT);
		lblStartDbs.setForeground(new Color(244, 106, 9));
		lblStartDbs.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblStartDbs.setBounds(101, 194, 91, 14);
		
		
		lblEnableLogons = new JLabel("Enable Logons:", SwingConstants.RIGHT);
		lblEnableLogons.setForeground(new Color(244, 106, 9));
		lblEnableLogons.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblEnableLogons.setBounds(282, 194, 91, 14);
		
		
		lblSaveDumps = new JLabel("Save Dumps:", SwingConstants.RIGHT);
		lblSaveDumps.setForeground(new Color(244, 106, 9));
		lblSaveDumps.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblSaveDumps.setBounds(101, 219, 91, 14);
		
		
		lblMaxDumps = new JLabel("Max Dumps:", SwingConstants.RIGHT);
		lblMaxDumps.setForeground(new Color(244, 106, 9));
		lblMaxDumps.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblMaxDumps.setBounds(449, 219, 91, 14);
		
		
		lblSnapshotCrash = new JLabel("Snapshot Crash:", SwingConstants.RIGHT);
		lblSnapshotCrash.setForeground(new Color(244, 106, 9));
		lblSnapshotCrash.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblSnapshotCrash.setBounds(282, 219, 91, 14);
		
		
		lblBreakStop = new JLabel("Break Stop:", SwingConstants.RIGHT);
		lblBreakStop.setForeground(new Color(244, 106, 9));
		lblBreakStop.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblBreakStop.setBounds(449, 195, 91, 14);
		
		
		ctl0label = new JLabel("");
		ctl0label.setBounds(202, 195, 91, 14);
		
		
		ctl4label = new JLabel("");
		ctl4label.setBounds(202, 219, 91, 14);
		
		
		ctl2label = new JLabel("");
		ctl2label.setBounds(383, 194, 91, 14);
		
		
		ctl5label = new JLabel("");
		ctl5label.setBounds(383, 219, 91, 14);
		
		
		ctl1label = new JLabel("");
		ctl1label.setBounds(550, 195, 91, 14);
		
		ctl6label = new JLabel("");
		ctl6label.setBounds(550, 219, 91, 14);
		
		btnNewButton = new JButton("");
		btnNewButton.setToolTipText("Home");
		btnNewButton.setBackground(Color.WHITE);
		btnNewButton.setForeground(Color.WHITE);
		btnNewButton.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/home.png")));
		btnNewButton.setDisabledIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/home_disabled.png")));
		btnNewButton.setBounds(10, 11, 42, 37);
		btnNewButton.setOpaque(false);
		btnNewButton.setContentAreaFilled(false);
		btnNewButton.setBorderPainted(false);
		btnNewButton.setFocusPainted(false);
		panel.add(btnNewButton);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(collectionWorker !=null && !collectionWorker.isDone()) //If the collection worker is already done with previous collection, then there is nothing to abort before signing out.
				{
					/*Before switching to the main panel, abort running commands*/
					
					collectionWorker.getLock().lock();
					if(JOptionPane.showConfirmDialog(DBSGeneralPanel.this,"This Will Abort. Are You Sure?","Confirmation",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)  
					{
						collectionWorker.getLock().unlock();
						return;
					}
					if(collectionWorker.isDone()) //If the collection is gone by the time the user answered YES to the previous dialog, then just finalize and go home.
					{
						finalizeAbort();
						return;
					}
					btnAbortButton.setEnabled(false);
					btnNewButton.setEnabled(false);
					NotificationPanel.updateNotification("Aborting...", null);
					SwingWorker<Void,Void> canceller = new SwingWorker<Void,Void>()
					{
						public void done()
						{
							//finalizeAbort();
						}
						public Void doInBackground()
						{
							collectionWorker.doWhenDoneCancel(new PropertyChangeListener()
							{
								/*This is for delegating the execution of finalizeAbort to the worker,
								 * while the definition is still within the scope of this action listener class.
								 * The intent is for this action listener not to go home until the cancellation process is done completely.*/
								public void propertyChange(PropertyChangeEvent pce)
								{
									finalizeAbort();
								}
							});
							collectionWorker.cancel(true);
							return null;
							/*Do not call finalizeAbort here or from done. The collectionWorker will call it for us via the property change listener.*/
						}
					};
					
					canceller.execute();
					
					while(true)
					{
						if(collectionWorker.isCancelled())
						{
							collectionWorker.getLock().unlock();
							break;
						}
					}
				}
				else
				{
					finalizeAbort();
				}
			}
			public void finalizeAbort()
			{
				btnAbortButton.setVisible(false);
				btnAbortButton.setEnabled(true);
				btnNewButton.setEnabled(true);
				GTSToolMain.showMainPanel();
			}
		});
		
		
		btnBackButton = new JButton("");
		btnBackButton.setToolTipText("Back");
		btnBackButton.setBackground(Color.WHITE);
		btnBackButton.setForeground(Color.WHITE);
		btnBackButton.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/back.png")));
		btnBackButton.setBounds(52, 11, 42, 37);
		btnBackButton.setOpaque(false);
		btnBackButton.setContentAreaFilled(false);
		btnBackButton.setBorderPainted(false);
		btnBackButton.setFocusPainted(false);
		btnBackButton.setVisible(false);
		panel.add(btnBackButton);
		btnBackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbsSubPanelLayout.show(dbsSubPanel,"dbsStatusPanel");
				proceedButton.setVisible(true);
				collectButton.setVisible(false);
				btnFwdButton.setVisible(true);
				btnBackButton.setVisible(false);
			}
		});

		
		btnFwdButton = new JButton("");
		btnFwdButton.setToolTipText("Fwd");
		btnFwdButton.setBackground(Color.WHITE);
		btnFwdButton.setForeground(Color.WHITE);
		btnFwdButton.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/fwd.png")));
		btnFwdButton.setBounds(52, 11, 42, 37);
		btnFwdButton.setOpaque(false);
		btnFwdButton.setContentAreaFilled(false);
		btnFwdButton.setBorderPainted(false);
		btnFwdButton.setFocusPainted(false);
		btnFwdButton.setVisible(true);
		panel.add(btnFwdButton);
		btnFwdButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dbsSubPanelLayout.show(dbsSubPanel,"dbsDataCollectionPanel");
				proceedButton.setVisible(false);
				collectButton.setVisible(true);
				btnFwdButton.setVisible(false);
				btnBackButton.setVisible(true);
			}
		});
		
		btnFileButton = new JButton("");
		btnFileButton.setToolTipText("Files");
		btnFileButton.setBackground(Color.WHITE);
		btnFileButton.setForeground(Color.WHITE);
		btnFileButton.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/file.png")));
		btnFileButton.setBounds(94, 11, 42, 37);
		btnFileButton.setOpaque(false);
		btnFileButton.setContentAreaFilled(false);
		btnFileButton.setBorderPainted(false);
		btnFileButton.setFocusPainted(false);
		btnFileButton.setVisible(false); //By default set as invisible until collection is executed.
		panel.add(btnFileButton);
		btnFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try
				{
					Runtime.getRuntime().exec("explorer.exe "+collectionWorker.getClientSideDBSPath());
				}
				catch(IOException ioe)
				{
					ioe.printStackTrace();
				}
			}
		});
		
		btnConsoleButton = progressDialog.getProgressButton();
		btnConsoleButton.setBounds(178, 11, 42, 37);
		panel.add(btnConsoleButton);
		
		
		btnAbortButton = new JButton("");
		btnAbortButton.setToolTipText("Abort");
		btnAbortButton.setBackground(Color.WHITE);
		btnAbortButton.setForeground(Color.WHITE);
		btnAbortButton.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/abort.png")));
		btnAbortButton.setDisabledIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/abort_disabled.png")));
		btnAbortButton.setBounds(136, 11, 42, 37);
		btnAbortButton.setOpaque(false);
		btnAbortButton.setContentAreaFilled(false);
		btnAbortButton.setBorderPainted(false);
		btnAbortButton.setFocusPainted(false);
		btnAbortButton.setVisible(false); //By default set as invisible until collection is executed.
		panel.add(btnAbortButton);
		btnAbortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(collectionWorker !=null)
				{
					/*Lock the collection worker where it's at until it's decided whether the command is abortable,
					 * and until the user has confirmed their decision of aborting.*/
					collectionWorker.getLock().lock();
					if(!SSHTunnel.commandAbortable() //Unless the command is abortable, nothing can be done. Only commands run via executeCommandToFile are abortable.
						|| JOptionPane.showConfirmDialog(DBSGeneralPanel.this,"This Will Abort. Are You Sure?","Confirmation",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION
						|| collectionWorker.isDone())  //If the collection is done by the time the user answers YES, then no need to do anything
					{
						collectionWorker.getLock().unlock();
						return;
					}
					btnAbortButton.setEnabled(false);
					btnNewButton.setEnabled(false);
					NotificationPanel.updateNotification("Aborting...", null);
					new SwingWorker<Void,Void>()
					{
						public void done()
						{
							//finalizeAbort();
						}
						public Void doInBackground()
						{
							collectionWorker.doWhenDoneCancel(new PropertyChangeListener()
							/*This is for delegating the execution of finalizeAbort to the worker,
							 * while the definition is still within the scope of this action listener class.
							 * The intent is for this action listener not to finalize until the cancellation process is done completely.*/
							{
								public void propertyChange(PropertyChangeEvent pce)
								{
									finalizeAbort();
								}
							});
							collectionWorker.cancel(true); 
							return null;
						}
					}.execute();
					while(true)
					/*Loops forever until it verifies that the execution of the last swing worker has successfully cancelled the collection worker
					 * before releasing the lock.
					 * So the later will then know to return when it finds it has been cancelled.*/
					{
						if(collectionWorker.isCancelled())
						{
							collectionWorker.getLock().unlock();
							break;
						}
					}
				}
			}
			public void finalizeAbort()
			{
				btnAbortButton.setVisible(false);
				btnAbortButton.setEnabled(true);
				btnNewButton.setEnabled(true);
			}
		});
		
		lblConnectedTo = new JLabel("Connected to:", SwingConstants.RIGHT);
		lblConnectedTo.setForeground(new Color(244, 106, 9));
		lblConnectedTo.setFont(new Font("Tahoma", Font.PLAIN, 11));
		lblConnectedTo.setBounds(312, 48, 91, 14);
		panel.add(lblConnectedTo);
		
		hostNameLabel = new JLabel("");
		hostNameLabel.setBounds(414, 48, 91, 14);
		panel.add(hostNameLabel);

		
		dbsSubPanel = new JPanel(new BorderLayout());
		dbsSubPanel.setBounds(10, 110, 769, 255);
		dbsSubPanel.setBackground(Color.WHITE);
		dbsSubPanel.setLayout(dbsSubPanelLayout);
		
		submitButtonPanel.setBounds(10,365,769,50);
		submitButtonPanel.setBackground(Color.WHITE);
		panel.add(submitButtonPanel);
		
		dbsStatusPanel = new JPanel();
		dbsStatusPanel.setLayout(null);
		dbsStatusPanel.setBackground(Color.WHITE);
		dbsSubPanel.add(dbsStatusPanel);
		dbsSubPanel.add(dbsDataCollectionPanel);
		dbsSubPanelLayout.addLayoutComponent(dbsStatusPanel, (Object)"dbsStatusPanel");
		dbsSubPanelLayout.addLayoutComponent(dbsDataCollectionPanel, (Object)"dbsDataCollectionPanel");
		dbsSubPanelLayout.show(dbsSubPanel,"dbsStatusPanel");
		dbsStatusPanel.add(lblConnectionDetails);
		dbsStatusPanel.add(Vproc);
		dbsStatusPanel.add(scrollPane);
		dbsStatusPanel.add(scrollPane_1);
		dbsStatusPanel.add(lblCtlSettings);
		dbsStatusPanel.add(lblStartDbs);
		dbsStatusPanel.add(lblEnableLogons);
		dbsStatusPanel.add(lblSaveDumps);
		dbsStatusPanel.add(lblMaxDumps);
		dbsStatusPanel.add(lblSnapshotCrash);
		dbsStatusPanel.add(lblBreakStop);
		dbsStatusPanel.add(ctl0label);
		dbsStatusPanel.add(ctl4label);
		dbsStatusPanel.add(ctl2label);
		dbsStatusPanel.add(ctl5label);
		dbsStatusPanel.add(ctl1label);
		dbsStatusPanel.add(ctl6label);
		panel.add(dbsSubPanel);
		
		dbsDataCollectionPanel.setLayout(new BoxLayout(dbsDataCollectionPanel,BoxLayout.Y_AXIS));
		dbsDataCollectionTopPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		dbsDataCollectionTopPanel.setBackground(Color.WHITE);
		dbsDataCollectionTopPanel.add(generalPanel);
		dbsDataCollectionTopPanel.add(restartPanel);
		dbsDataCollectionTopPanel.add(hangPanel);
		dbsDataCollectionTopPanel.add(sysPerfPanel);
		dbsDataCollectionTopPanel.add(miscPanel);
		

		dbsDataCollectionTopPanel.setPreferredSize(new Dimension(652,130));
		dbsDataCollectionBottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		dbsDataCollectionBottomPanel.setPreferredSize(new Dimension(652,50));
		dbsDataCollectionBottomPanel.setBackground(Color.WHITE);
		dbsDataCollectionBottomPanel.setBorder(BorderFactory.createTitledBorder("DBS Area"));
		dbsDataCollectionPanel.add(dbsDataCollectionTopPanel);
		dbsDataCollectionPanel.add(dbsDataCollectionBottomPanel);
		

		dbsDataCollectionBottomFirstPanel.setLayout(new BoxLayout(dbsDataCollectionBottomFirstPanel, BoxLayout.Y_AXIS));
		dbsDataCollectionBottomFirstPanel.setBackground(Color.WHITE);
		dbsDataCollectionBottomSecondPanel.setLayout(new BoxLayout(dbsDataCollectionBottomSecondPanel, BoxLayout.Y_AXIS));
		dbsDataCollectionBottomSecondPanel.setBackground(Color.WHITE);
		dbsDataCollectionBottomThirdPanel.setLayout(new BoxLayout(dbsDataCollectionBottomThirdPanel, BoxLayout.Y_AXIS));
		dbsDataCollectionBottomThirdPanel.setBackground(Color.WHITE);
		
		dbsDataCollectionBottomFirstPanel.setPreferredSize(new Dimension(100,80));
		dbsDataCollectionBottomSecondPanel.setPreferredSize(new Dimension(100,80));
		dbsDataCollectionBottomThirdPanel.setPreferredSize(new Dimension(100,80));
		dbsDataCollectionBottomPanel.add(dbsDataCollectionBottomFirstPanel);
		dbsDataCollectionBottomPanel.add(dbsDataCollectionBottomSecondPanel);	
		dbsDataCollectionBottomPanel.add(dbsDataCollectionBottomThirdPanel);		
		
		allCheckBox.setBackground(Color.WHITE);
		generalCheckBox.setBackground(Color.WHITE);
		sysPerfCheckBox.setBackground(Color.WHITE);
		hangCheckBox.setBackground(Color.WHITE);
		restartCheckBox.setBackground(Color.WHITE);
		miscCheckBox.setBackground(Color.WHITE);
		
		allCheckBoxes = new JCheckBox[generalCheckBoxes.length+restartCheckBoxes.length+hangCheckBoxes.length+sysPerfCheckBoxes.length+miscCheckBoxes.length];
		System.arraycopy(generalCheckBoxes,0,allCheckBoxes,0,generalCheckBoxes.length);
		System.arraycopy(restartCheckBoxes,0,allCheckBoxes,generalCheckBoxes.length,restartCheckBoxes.length);
		System.arraycopy(hangCheckBoxes,0,allCheckBoxes,generalCheckBoxes.length+restartCheckBoxes.length,hangCheckBoxes.length);
		System.arraycopy(sysPerfCheckBoxes,0,allCheckBoxes,generalCheckBoxes.length+restartCheckBoxes.length+hangCheckBoxes.length,sysPerfCheckBoxes.length);
		System.arraycopy(miscCheckBoxes,0,allCheckBoxes,generalCheckBoxes.length+restartCheckBoxes.length+hangCheckBoxes.length+sysPerfCheckBoxes.length,miscCheckBoxes.length);
		disableAllCheckBoxes(true);
		
		generalCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ie)
			{
				if(ie.getStateChange() == (ItemEvent.SELECTED))
				{
					enableGeneralCheckBoxes();
					if(allCheckBox.isSelected())
					{
						allCheckBox.setSelected(false);
						disableAllCheckBoxes();
					}
				}
				else
				{
					disableGeneralCheckBoxes();
				}
			}
		});
		
		restartCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ie)
			{
				if(ie.getStateChange() == (ItemEvent.SELECTED))
				{
					enableRestartCheckBoxes();
					if(allCheckBox.isSelected())
					{
						allCheckBox.setSelected(false);
						disableAllCheckBoxes();
					}
				}
				else
				{
					disableRestartCheckBoxes();
				}
			}
		});
		
		hangCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ie)
			{
				if(ie.getStateChange() == (ItemEvent.SELECTED))
				{
					enableHangCheckBoxes();
					if(allCheckBox.isSelected())
					{
						allCheckBox.setSelected(false);
						disableAllCheckBoxes();
					}
				}
				else
				{
					disableHangCheckBoxes();
				}
			}
		});
		
		sysPerfCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ie)
			{
				
				if(ie.getStateChange() == (ItemEvent.SELECTED))
				{
					enableSysPerfCheckBoxes();
					if(allCheckBox.isSelected())
					{
						allCheckBox.setSelected(false);
						disableAllCheckBoxes();
					}
				}
				else
				{
					disableSysPerfCheckBoxes();
				}
			}
		});
		
		miscCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ie)
			{
				if(ie.getStateChange() == (ItemEvent.SELECTED))
				{
					enableMiscCheckBoxes();
					if(allCheckBox.isSelected())
					{
						allCheckBox.setSelected(false);
						disableAllCheckBoxes();
					}
				}
				else
				{
					disableMiscCheckBoxes();
				}
			}
		});
		
		
		allCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ie)
			{
				boolean selected = ie.getStateChange() == (ItemEvent.SELECTED);
				if(selected)
				{
					generalCheckBox.setSelected(false);
					restartCheckBox.setSelected(false);
					hangCheckBox.setSelected(false);
					sysPerfCheckBox.setSelected(false);
					miscCheckBox.setSelected(false);
					enableAllCheckBoxes();
				}
				else
				{
					disableAllCheckBoxes();
				}
				
			}
		});
		
		dbsDataCollectionBottomFirstPanel.add(allCheckBox);
		dbsDataCollectionBottomFirstPanel.add(hangCheckBox);
		
		dbsDataCollectionBottomSecondPanel.add(generalCheckBox);
		dbsDataCollectionBottomSecondPanel.add(sysPerfCheckBox);
		
		dbsDataCollectionBottomThirdPanel.add(restartCheckBox);
		dbsDataCollectionBottomThirdPanel.add(miscCheckBox);		
		
		proceedButton.addActionListener(new ActionListener() //This is equivalent to btnFwdButton
		{
			public void actionPerformed(ActionEvent ae)
			{
				dbsSubPanelLayout.show(dbsSubPanel,"dbsDataCollectionPanel");
				proceedButton.setVisible(false);
				collectButton.setVisible(true);
				btnFwdButton.setVisible(false);
				btnBackButton.setVisible(true);
				//if(collectButton.isEnabled())
					//NotificationPanel.updateNotification("", "None");
			}
		});
		
		collectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				//notificationOwner = null;
				boolean allChecksCleared = true;
				for(int i=0 ; i< allCheckBoxes.length ; i++)
				{
					if(allCheckBoxes[i].isSelected())
					{
						allChecksCleared = false;
						break;
					}
				}
				if(allChecksCleared)
				{
					return;
				}
				/*Generating the list of hang restricted commands in html format, so it will be displayed obviously to the user when selecting "All" option.*/
				String restrictedCheckBoxesHtml = "";
				for(int i=0;i<hangRestrictedCheckBoxes.length;i++)
				{
					restrictedCheckBoxesHtml+="<br>";
					if(hasWarning(hangRestrictedCheckBoxes[i]))
						/*This condition is met in the unlikely case if a warning message is intentionally set on a command checkbox that is specified as hang restricted.
						 * This is because in that case, the checkbox text is ommited from the checkbox itself, and rather displayed as a TextIcon nested into a CompoundIcon in order to support the warning Icon after the text.*/
						restrictedCheckBoxesHtml+=((TextIcon)((CompoundIcon)hangRestrictedCheckBoxes[i].getIcon()).getIcon(1)).getText();
					else
						restrictedCheckBoxesHtml+=hangRestrictedCheckBoxes[i].getText();
				
				}
				restrictedCheckBoxesHtml+="<br>";
				if(JOptionPane.showConfirmDialog(DBSGeneralPanel.this,"<html>"
						+ "This will collect the selected data options."
						+ (allCheckBox.isSelected()?"<br><font color=RED>STOP!</font> You are selecting ALL option. If the Database is in a Hang state,"
								+ "the tool may hang on collecting the following data:"+restrictedCheckBoxesHtml:"")
						+ "<br> Are you sure?</html>","Confirmation",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
				{
					return;
				}
				
				collectionWorker = new CollectionWorker();
				collectionWorker.execute();
				//notificationOwner = collectionWorker;

			}
		});
		
		
		GridLayout generalGrid = new GridLayout(6,2);
		generalGrid.setHgap(0);
		generalGrid.setVgap(0);
		for(int i=0;i<generalCheckBoxes.length;i++)
		{
			generalCheckBoxes[i].setBackground(Color.WHITE);
		}
		generalPanel.setLayout(generalGrid);
		generalPanel.setBorder(BorderFactory.createTitledBorder("General"));
		generalPanel.setBackground(Color.WHITE);
		for(int i=0;i<generalCheckBoxes.length;i++)
		{
			generalPanel.add(generalCheckBoxes[i]);
		}
		generalPanel.setVisible(allPanelsVisible);
		
		
		for(int i=0;i<restartCheckBoxes.length;i++)
		{
			restartCheckBoxes[i].setBackground(Color.WHITE);
		}
		restartPanel.setLayout(generalGrid);
		restartPanel.setBorder(BorderFactory.createTitledBorder("Restart"));
		restartPanel.setBackground(Color.WHITE);
		for(int i=0;i<restartCheckBoxes.length;i++)
		{
			restartPanel.add(restartCheckBoxes[i]);
		}
		restartPanel.setVisible(allPanelsVisible);
		
		
		for (int i=0;i<hangCheckBoxes.length;i++)
		{
			hangCheckBoxes[i].setBackground(Color.WHITE);
		}
		
		hangPanel.setLayout(generalGrid);
		hangPanel.setBorder(BorderFactory.createTitledBorder("DBS Hang"));
		hangPanel.setBackground(Color.WHITE);
		for(int i=0;i<hangCheckBoxes.length;i++)
		{
			hangPanel.add(hangCheckBoxes[i]);
		}
		hangPanel.setVisible(allPanelsVisible);
		
		for(int i=0;i<sysPerfCheckBoxes.length;i++)
		{
			sysPerfCheckBoxes[i].setBackground(Color.WHITE);
		}
		
		
		sysPerfPanel.setLayout(generalGrid);
		sysPerfPanel.setBorder(BorderFactory.createTitledBorder("System Perf"));
		sysPerfPanel.setBackground(Color.WHITE);
		for(int i=0;i<sysPerfCheckBoxes.length;i++)
		{
			sysPerfPanel.add(sysPerfCheckBoxes[i]);
		}
		sysPerfPanel.setVisible(allPanelsVisible);
		
		for(int i=0;i<miscCheckBoxes.length;i++)
		{
			miscCheckBoxes[i].setBackground(Color.WHITE);
		}
		
		miscPanel.setLayout(generalGrid);
		miscPanel.setBorder(BorderFactory.createTitledBorder("Misc."));
		miscPanel.setBackground(Color.WHITE);
		for(int i=0;i<miscCheckBoxes.length;i++)
		{
			miscPanel.add(miscCheckBoxes[i]);
		}
		miscPanel.setVisible(allPanelsVisible);
		Enumeration<Command> commands = commandTable.elements();
		Command command;
		/*The following loop sets action listeners for the checkboxes that have restriction warnings specified,
		 * so when a restricted checkbox is checked, a confirmation dialog will popup with the associated string message (warnIfChecked)*/
		while(commands.hasMoreElements())
		{
			command = commands.nextElement();
			if(!command.autoCheck /*&& command.warnIfChecked !=null*/)
			{
				command.getCommandCheckBox().addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent ae)
					{ /*See Command for details on commandTable*/
						if(commandTable.get((JCheckBox)(ae.getSource())).warnIfChecked !=null && commandTable.get((JCheckBox)(ae.getSource())).warnIfChecked.equals(Command.DBSLOGONREQUIRED))
						{
							if(((JCheckBox)(ae.getSource())).isSelected())
							{
								JTextField DBSUsernameField = new JTextField();
								JTextField DBSPasswordField = new JPasswordField();
								Object[] DBSConnectionMessage = {
								    "Username:", DBSUsernameField,
								    "Password:", DBSPasswordField
								};

								int option;

								do {
									option = JOptionPane.showConfirmDialog(DBSGeneralPanel.this, DBSConnectionMessage, "DBS Login", JOptionPane.OK_CANCEL_OPTION);
									if (option == JOptionPane.OK_OPTION)
									{
										if (!DBSUsernameField.getText().trim().equals("") && !DBSPasswordField.getText().trim().equals("")) 
										{
											commandTable.get((JCheckBox)(ae.getSource())).setDBSUserName(DBSUsernameField.getText());
											commandTable.get((JCheckBox)(ae.getSource())).setDBSPassword(DBSPasswordField.getText());
										} 
									}
									else
									{
										((JCheckBox)(ae.getSource())).setSelected(false);
									}
								}
								while(option == JOptionPane.OK_OPTION && (DBSUsernameField.getText().trim().equals("") || DBSPasswordField.getText().trim().equals("")));
							}
							else
							{
								commandTable.get((JCheckBox)(ae.getSource())).setDBSUserName(null);
								commandTable.get((JCheckBox)(ae.getSource())).setDBSPassword(null);
							}
							
						}
						else if(commandTable.get((JCheckBox)(ae.getSource())).warnIfChecked !=null)
						{
							((JCheckBox)(ae.getSource())).setSelected(((JCheckBox)(ae.getSource())).isSelected()
							&& JOptionPane.showConfirmDialog(DBSGeneralPanel.this,commandTable.get((JCheckBox)(ae.getSource())).warnIfChecked,"Confirmation",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION);
						}	
						if(((JCheckBox)(ae.getSource())).isSelected())
						{
							removeIconFromLabel((JCheckBox)(ae.getSource()));
							/*revalidateRestriction(): The active warnings are revalidated to determine if the bottom restriction notification should be undisplayed.*/
							revalidateRestriction();
						}
					}
				});
			}
		}
	}
	

	
	private void lockAllComponents(boolean lock)
	{
		/*Enable/disable all the checkboxes except the ones that are already disabled due to Hang checkbox being checked. e.g, lokdisp .etc.*/
		boolean restricted;
		for(int i=0;i<allCheckBoxes.length;i++)
		{
			restricted=false;
			for(int j=0;j < hangRestrictedCheckBoxes.length;j++)
			{
				if(allCheckBoxes[i].equals(hangRestrictedCheckBoxes[j]))
				{
					restricted=true;
				}
			}
			if(!restricted || !hangCheckBox.isSelected())
				allCheckBoxes[i].setEnabled(!lock);
		}
		
		allCheckBox.setEnabled(!lock);
		generalCheckBox.setEnabled(!lock);
		restartCheckBox.setEnabled(!lock);
		hangCheckBox.setEnabled(!lock);
		sysPerfCheckBox.setEnabled(!lock);
		miscCheckBox.setEnabled(!lock);
		
		collectButton.setEnabled(!lock);
	}
	private void lockComponent(JCheckBox componentCheckBox, boolean lock)
	{
		/*Disabled/enables one single checkbox component for the purpose of restricting/unrestricting  use when Hang checkbox is checked/unchecked.*/
		componentCheckBox.setEnabled(!lock);
		if(lock)
		{
			componentCheckBox.setSelected(false);
			/*In the case that a checkbox should be disabled while it has a warning icon and a restriction notification,
			 *  the icon should be cleared and the notification should be revalidated*/
			if(hasWarning(componentCheckBox))
			{
				removeIconFromLabel(componentCheckBox);
				revalidateRestriction();
			}
		}
		else //unlock the checkbox. i.e, when Hang checkbox is unchecked
		{
			/*For the purpose of determining whether the given checkbox should be checked or not depending on the state of botttom checkboxes that are referencing it,
			 * we check the checkbox, and then attempt to uncheck it using disableCheckBoxes(). The later will have no impact if the given checkbox should not be unchecked.
			 * See disableCheckBoxes() for details.*/
			componentCheckBox.setSelected(true);
			disableCheckBoxes(new JCheckBox[] {componentCheckBox});
			/*If manual verification needs to be performed against a command that has just been checked in the last step,
			 *  then uncheck it again, and add a warning icon.*/
			if(componentCheckBox.isSelected() && !commandTable.get(componentCheckBox).autoCheck)
			{
				componentCheckBox.setSelected(false);
				addIconToLabel(componentCheckBox,"warning_small");
			}
		}
	}
	
	private void revalidateHangRestrictions()
	{
		/*When Hang checkbox is selected, this method will be called to go thru each hang restricted command, and disable its corresponding checkbox.*/
		for(int i=0;i<hangRestrictedCheckBoxes.length;i++)
		{
				lockComponent(hangRestrictedCheckBoxes[i],hangCheckBox.isSelected());
		}
	}

	private String getCheckBoxText(JCheckBox checkBox)
	{
		return checkBox.getText().equals("")?((TextIcon)((CompoundIcon)checkBox.getIcon()).getIcon(1)).getText():checkBox.getText();
	}
	private class CollectionWorker extends SwingWorker<Void,Void>
	{
		/*After "Collect Data" button has been clicked, this class is responsible for retreiving, sorting, and executing the list of checked commands,
		 * and handling the collection finalization phase after the execution of commands has been completed or cancelled:
		 * Checking and notifying for any left over processes (by calling getLeftOverProcesses()).
		 * Removing the working directory on the server.
		 * Archiving output files.*/
		
		private final String timeTag = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS-z").format(new java.util.Date());
		private String serverSideDBSPath;
		private String clientSideDBSPath;
		private int countOfSelections=0;
		private ReentrantLock cancellationLock = new ReentrantLock();
		private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
		private boolean aborted = false;
		public ReentrantLock getLock()
		{
			return cancellationLock;
		}
		private void setAborted(boolean aborted)
		{
			boolean oldValue = this.aborted;
			this.aborted = aborted;
			pcs.firePropertyChange("aborted", oldValue, aborted);
		}
		/*private boolean isAborted()
		{
			return aborted;
		}*/
		public void doWhenDoneCancel(PropertyChangeListener changeListener)
		{
			pcs.addPropertyChangeListener(changeListener);
		}
		
		public String getClientSideDBSPath()
		{
			return clientSideDBSPath;
		}
		public void done()
		{
			finalizeWorker();
		}
		public void finalizeWorker()
		{
			new SwingWorker<Void,Void/*AbstractMap.SimpleEntry<String, String>*/>()
			{
				public Void doInBackground()
				{
					NotificationPanel.updateNotification("Finalizing.", "loading");
					logger.info("Finalizing.");
					//publish(new AbstractMap.SimpleEntry<String, String>("Finalizing...", "loading"));
					if(CollectionWorker.this.isCancelled())
					{
						/*SwingWorker.cancel is exposed to Oracle bug:  JDK-6826514: After SwingWorker being cancelled,
						 * doInBackground will continue execution in the worker thread, while done() is running the Event Dispatcher thread.
						 * I am calling abortCommand here to abort the currently running command,
						 * so the logic from doInBackground can check and realize that it should return.
						 * Only the commands that are executed via executeCommandToFile() can be aborted.
						 * In case of the user trying to abort while the running command is not abortable,
						 * the abort/home button's listeners will determine by calling SSHTunnel.commandAbortable(),
						 * and will not attempt to abort this worker from the first place.*/
						SSHTunnel.abortCommand();
						try
						{
							/*Sleeping for few seconds to assure that the session is aborted prior to attempting to reconnect for collection finalization.*/
							Thread.sleep(3000);
						}
						catch(InterruptedException ie)
						{
							logger.log(Level.SEVERE, "An Unexpected Exception Occurred", ie);
						}
						
					}
					String leftOverProcesses = getLeftOverProcesses(serverSideDBSPath);
					if(!"".equals(leftOverProcesses))
					{
						logger.warning("Suspect Left Over Processes Exist\n\n"+leftOverProcesses);
						JOptionPane.showMessageDialog(DBSGeneralPanel.this, "Suspect Left Over Processes Exist\n\n"+leftOverProcesses, "Warning", JOptionPane.WARNING_MESSAGE);
					}
					logger.info("Checking for existance of working directory");
					String lsServerSidePath = SSHTunnel.executeCommand("ls -d "+serverSideDBSPath+" || true");
					logger.info(lsServerSidePath==null||!lsServerSidePath.contains(serverSideDBSPath)?"No working directory found":"Working directory will be removed");
					if(lsServerSidePath !=null
					&& lsServerSidePath.contains("GTSTOOL-DBS-"+timeTag)
					/*&& JOptionPane.showConfirmDialog(DBSGeneralPanel.this,"Do you want to remove "+serverSideDBSPath+" directory and all its subdirectories from the server?\n\nContent Information:\n"+
					SSHTunnel.executeCommand("ls -R "+serverSideDBSPath),"Confirmation",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION*/
					)
					{
						if(!(SSHTunnel.executeCommand("rm -rf "+serverSideDBSPath+" 2>/dev/null ; ls -d "+serverSideDBSPath+" || true").contains("GTSTOOL-DBS-"+timeTag)))
							logger.info("Server Directory - \""+serverSideDBSPath+"\" has been removed");
						else
							logger.warning("Server Directory - \""+serverSideDBSPath+"\" has NOT been removed successfully.");
						//SSHTunnel.executeCommand("rm -rf "+serverSideDBSPath);
					}
					logger.info("Archiving Output.");
					NotificationPanel.updateNotification("Archiving Output.", "loading");
					//publish(new AbstractMap.SimpleEntry<String, String>("Archiving Output.", "loading"));
					DirectoryZip zip = new DirectoryZip(clientSideDBSPath,"GTSTOOL-DBS-"+ timeTag+".zip");
					if(!zip.archive())
					{
						logger.severe("Archive Creation Error.");
						NotificationPanel.updateNotification("Archive Creation Error.", "fail");
						//publish(new AbstractMap.SimpleEntry<String, String>("Archive Creation Error.", "fail"));
					}
					else if(CollectionWorker.this.isCancelled())
					{
						logger.info("Collection Cancelled");
						NotificationPanel.updateNotification("Collection Cancelled", null);
						//publish(new AbstractMap.SimpleEntry<String, String>("Collection Cancelled", null));
					}
					else
					{
						logger.info("Data Collected");
						NotificationPanel.updateNotification("Data Collected.", "success");
						//publish(new AbstractMap.SimpleEntry<String, String>("Data Collected", "success"));
					}
					lockAllComponents(false);
					//AppLogger.endLogger("DBSGeneralPanel");
					AppLogger.removeFileHandler("DBSGeneralPanel");
					AppLogger.setTextArea(null);
					btnConsoleButton.setVisible(false);
					if(CollectionWorker.this.isCancelled());
						CollectionWorker.this.setAborted(true);
					return null;
				}
				/*public void process(List<AbstractMap.SimpleEntry<String, String>> chunks)
				{
					NotificationPanel.updateNotification(chunks.get(0).getKey(), chunks.get(0).getValue());
				}*/
				
			}.execute();
			
		}
		public void logCollectionOptions(String area, StringBuffer collectionOptions)
		{
			JCheckBox[] boxes;
			collectionOptions.append("\n"+area);
			switch(area)
			{
			case "All":
				boxes = allCheckBoxes;
				break;
			case "General":
				boxes = generalCheckBoxes;
				break;
			case "Restart":
				boxes = restartCheckBoxes;
				break;
			case "Hang":
				boxes = hangCheckBoxes;
				break;
			case "System Performance":
				boxes = sysPerfCheckBoxes;
				break;
			case "Misc":
				boxes = miscCheckBoxes;
				break;
			default:
				return;
			}
			int lengthBefore = collectionOptions.length();
			for (JCheckBox checkBox: boxes)
			{
				if(checkBox.isSelected())
				{
					collectionOptions.append("\n    "+checkBox.getText());
					countOfSelections++;
				}
				else if(hasWarning(checkBox))
				{
					collectionOptions.append("\n    "+getCheckBoxText(checkBox)+"  (Unselected - Manual Selection Required)");
				}
			}
			if(lengthBefore == collectionOptions.length())
			{
				collectionOptions.setLength(lengthBefore-area.length()-1);
			}
		}
		public Void doInBackground()
		{
			lockAllComponents(true);
			serverSideDBSPath = "/home/tdc/GTSTOOL-DBS-"+ timeTag;
			clientSideDBSPath = outputLocation+"\\GTSTOOL-DBS-"+ timeTag;
			/*if(AppLogger.registerPanel("DBSGeneralPanel", clientSideDBSPath, AppLogger.ONELINEFORMATTER))
			{
				logger = AppLogger.getMyLogger("DBSGeneralPanel");
			}
			else
			{
				NotificationPanel.updateNotification("Log file could not be created.", "warning");
				logger = null;
			}*/


			if(!new java.io.File(clientSideDBSPath).mkdir())
			{
				NotificationPanel.updateNotification("Output Directory Creation Error.", "fail");
				//logger.severe("Output Directory Creation Error");
				return null;
			}
			//logger = AppLogger.getLogger("DBSGeneralPanel", clientSideDBSPath, "GTSTOOL-DBS-"+ timeTag +".log", AppLogger.ONELINEFORMATTER);
			logger = AppLogger.getLogger();
			AppLogger.addFileHandler("DBSGeneralPanel", clientSideDBSPath, "GTSTOOL-DBS-"+ timeTag +".log");
			AppLogger.setTextArea(progressArea);
			/* Test
			try
			{
				AppLogger.addFileHandler("dbsParent", new java.io.File(clientSideDBSPath).getParent(), "GTSTOOL-DBS-2016-01-11-15-41-03-667-EST.log");
			}
			catch(Exception e)
			{
				logger.log(Level.SEVERE, "An Unexpected Exception Occurred", e);
			}
			*/
			//logger.info(AppLogger.LOGOBANNERDBS+systemInfo.stringValue());
			logger.log(AppLogger.DISPLAY, AppLogger.LOGOBANNERDBS+systemInfo.stringValue());
			
			StringBuffer collectionOptions = new StringBuffer("Selected Data Options");
			if(allCheckBox.isSelected())
			{
				logCollectionOptions("All",collectionOptions);
			}
			else
			{
				logCollectionOptions("General",collectionOptions);
				logCollectionOptions("Restart",collectionOptions);
				logCollectionOptions("Hang",collectionOptions);
				logCollectionOptions("System Performance",collectionOptions);
				logCollectionOptions("Misc",collectionOptions);
			}
			logger.info(collectionOptions.toString());
			logger.info("Starting Collection");
			//AppLogger.removeFileHandler("dbsParent");    Test
			btnFileButton.setVisible(true);
			Enumeration<Command> commands = commandTable.elements();
			List<Command> list = Collections.list(commands);
			Collections.sort(list);
			btnAbortButton.setVisible(true);
			btnConsoleButton.setVisible(true);
			String collectionStatus;
			int commandCounter=0;
			for(Command currentCommand : list)
			{
				if(currentCommand.getCommandCheckBox().isSelected())
				{
					collectionStatus = "Collecting: "+currentCommand.getCommandCheckBox().getText()+" ("+(++commandCounter)+"/"+countOfSelections+")";
					NotificationPanel.updateNotification(collectionStatus, "loading");
					logger.info(collectionStatus);
					//logger.log(AppLogger.DISPLAY, new Stage(collectionStatus).stringValue());
					if(!this.isCancelled())
					{
						currentCommand.run(serverSideDBSPath, clientSideDBSPath);
						cancellationLock.lock(); //Tries to get the lock to assure the worker is not blocked while the confirmation dialog is pending answer.
						cancellationLock.unlock(); //Once it gets the lock, it unlocks it. Nothing else is needed. 
					}
					if(this.isCancelled())
					{
						/*Oracle bug JDK-6826514. See finalizeWorker for details.*/
						logger.warning("Collection Aborted");
						return null;
					}
				}
			}
			btnAbortButton.setVisible(false);
			
			return null;
		}
	}
	
	private int commandCounter = 0;
	private abstract class Command implements Comparable<Command>
	{
		/*In addition to encapsulating the logic of the command execution in the form of the run() abstract method,
		 * the Command class maintains relationship between the command instance and:
		 * 1. parentCheckBox: The area checkbox that directly controls checking/unchecking the commandCheckBox.
		 * 		e.g, restartCheckBox is the parentCheckBox for csp command.
		 * 2. referencingCheckBoxes: An array of area checkboxes each of which indirectly references the commandCheckBox,
		 * by also controlling its checking and unchecking.
		 * 		e.g, restartCheckBox is a referencing checkbox for messages command.
		 * 3. siblingCheckBoxes: An array of checkboxes that share the same parentCheckBox with the command checkbox.
		 * 		e.g, tpaTrace is a sibling to csp command.
		 * The intent of establishing the above relations is to define the basis on which the controls will be implemented.
		 * 		e.g, we will later say,
		 * 		If restartCheckBox is checked, go and check all the checkboxes of all the children commands of restartChecBox,
		 * 		in addition to all other command CheckBoxes that restartCheckBox references.
		 * 		If  hangCheckBox was checked, and now is unchecked, uncheck it's children command checkboxes
		 * 		except for commands that are already referenced by currently checked area checkboxes.*/
		private int order;
		private JCheckBox commandCheckBox;
		private JCheckBox parentCheckBox;
		private JCheckBox[] referencingCheckBoxes;
		private JCheckBox[] siblingCheckBoxes = null;
		private boolean autoCheck = true; 
		public static final String DBSLOGONREQUIRED = "::DBSLOGONREQUIREDFORCOMMANDEXECUTION::";
		private String DBSUserName = null; //Only applicable if DBS Logon Required
		private String DBSPassword = null; //Only applicable if DBS Logon Required
		public void setDBSUserName(String userName)
		{
			this.DBSUserName = userName;
		}
		public String getDBSUserName()
		{
			return DBSUserName;
		}
		public String getDBSPassword()
		{
			return DBSPassword;
		}
		public void setDBSPassword(String password)
		{
			this.DBSPassword = password;
		}
		/*autoCheck=true is the default, and implies that if a parent,
		 *  or referencing checkbox is checked, automatically check me rather than showing a warning that I maybe restricted.*/
		private String warnIfChecked = null; /*This is the warning message, 
		and answering Yes means approve for the command checkbox to be checked.*/
		protected abstract void run(String serverSideDBSPath, String clientSideDBSPath);
		
		public int compareTo(Command c)
		{
			return order - c.order;
		}

		private JCheckBox retreiveParentCheckBox()
		{
			for(int i=0 ; i<generalCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(generalCheckBoxes[i]))
				{
					return generalCheckBox;
				}
			}
			for(int i=0 ; i<restartCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(restartCheckBoxes[i]))
				{
					return restartCheckBox;
				}
			}
			for(int i=0 ; i<hangCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(hangCheckBoxes[i]))
				{
					return hangCheckBox;
				}
			}
			for(int i=0 ; i<sysPerfCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(sysPerfCheckBoxes[i]))
				{
					return sysPerfCheckBox;
				}
			}
			for(int i=0 ; i<miscCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(miscCheckBoxes[i]))
				{
					return miscCheckBox;
				}
			}
			return null;
		}
		private JCheckBox[] retreiveReferencingCheckBoxes()
		{
			Vector<JCheckBox> referencingCheckBoxes = new Vector<JCheckBox>();
			for(int i=0 ; i<restartExtCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(restartExtCheckBoxes[i]))
				{
					referencingCheckBoxes.add(restartCheckBox);
				}

			}
			for(int i=0 ; i<hangExtCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(hangExtCheckBoxes[i]))
				{
					referencingCheckBoxes.add(hangCheckBox);
				}
			}
			for(int i=0 ; i<sysPerfExtCheckBoxes.length ; i++)
			{
				if(commandCheckBox.equals(sysPerfExtCheckBoxes[i]))
				{
					referencingCheckBoxes.add(sysPerfCheckBox);
				}
			}
			return referencingCheckBoxes.toArray(new JCheckBox[]{});
		}
		
		private JCheckBox[] retreiveSiblingCheckBoxes()
		{
			Component[] allChildrenComponents = commandCheckBox.getParent().getComponents();
			Vector<JCheckBox> siblingCheckBoxesVector = new Vector<JCheckBox>();
			for (int i=0 ; i<allChildrenComponents.length ; i++)
			{
				if(allChildrenComponents[i] instanceof JCheckBox)
				{
					siblingCheckBoxesVector.add((JCheckBox)allChildrenComponents[i]);
				}
			}
			this.siblingCheckBoxes =  siblingCheckBoxesVector.toArray(new JCheckBox[]{});
			return siblingCheckBoxes;
		}
		private JCheckBox getCommandCheckBox()
		{
			return commandCheckBox;
		}
		private JCheckBox getParentCheckBox()
		{
			return parentCheckBox;
		}
		private JCheckBox[] getReferencingCheckBoxes()
		{
			return referencingCheckBoxes;
		}
		private JCheckBox[] getSiblingCheckBoxes()
		{
			return (siblingCheckBoxes == null)?retreiveSiblingCheckBoxes():siblingCheckBoxes; //To prevent having to retreive it every time needed.
		}
		
		
		public Command(JCheckBox commandCheckBox)
		{
			/*Commands are sorted by the order in which they were constructed.*/
			this.order = ++commandCounter;
			/*Pre-retreiving command member variables' values to avoid extended processing of the corresponding getters*/
			this.commandCheckBox = commandCheckBox;
			this.parentCheckBox = retreiveParentCheckBox();
			this.referencingCheckBoxes = retreiveReferencingCheckBoxes();
			/*commandTable is a HashTable that maps each command checkbox to the corresponding Command object.
			 * It is used across the panel for looking up commands using the checkbox.
			 * The opposite (Looking up checkbox using the command) is accomplished thru the encapsulated variable commandCheckBox in the Command object*/
			commandTable.put(commandCheckBox,this);
			//this.siblingCheckBoxes = retreiveSiblingCheckBoxes(); This will throw NullPointerException since  at construction time, the parent containers will have not been associated to the checkboxes.
			//Call it sometime later. But make sure to call it before calling getSiblingCheckBoxes
		}
		public Command(JCheckBox commandCheckBox, boolean autoCheck)
		{
			this(commandCheckBox);
			this.autoCheck = autoCheck;
		}
		public Command(JCheckBox commandCheckBox, String warnIfChecked)
		{
			this(commandCheckBox, false);
			this.warnIfChecked = warnIfChecked;
		}
		
	}
	
	private void enablePanelsFor(JCheckBox[] checkBoxes)
	{
		/*This will be called if the boolean allPanelsVisible is set to false,
		 * meaning that a panel is being displayed only when any of the underlying checkboxes is being auto-checked.
		 * The method is called with a given array of checkboxes when those checkboxes are enabled,
		 * so their parent panels will be set to visible.*/
		
		for (int i=0;i<checkBoxes.length ; i++)
		{
			if(!checkBoxes[i].getParent().isVisible())
				checkBoxes[i].getParent().setVisible(true);
		}
	}
	private void disableAllCheckBoxes()
	{	
		disableCheckBoxes(allCheckBoxes);
	}
	private void disableAllCheckBoxes(boolean forced)
	{
		if(forced)
		{
			for (int i=0 ; i<allCheckBoxes.length ; i++ )
			{
				allCheckBoxes[i].setSelected(false);
			}
		}
		else
			disableAllCheckBoxes();
	}
	private void enableAllCheckBoxes()
	{
		for(int i=0 ; i<allCheckBoxes.length ; i++)
		{
			if(commandTable.get(allCheckBoxes[i]).autoCheck)
				allCheckBoxes[i].setSelected(true);
			else if(allCheckBoxes[i].isEnabled() && !allCheckBoxes[i].isSelected())
			{
				addIconToLabel(allCheckBoxes[i],"warning_small");
				
			}
		}
		if(!allPanelsVisible)
		{
			generalPanel.setVisible(true);
			restartPanel.setVisible(true);
			hangPanel.setVisible(true);
			sysPerfPanel.setVisible(true);
			miscPanel.setVisible(true);
		}
	}
	private void enableCheckBoxes(JCheckBox[] checkBoxes)
	{
		for (int i=0 ; i<checkBoxes.length ; i++)
		{
			if(commandTable.get(checkBoxes[i]).autoCheck)
			{
				if(checkBoxes[i].isEnabled())
					checkBoxes[i].setSelected(true);
			}
			else if(checkBoxes[i].isEnabled() && !checkBoxes[i].isSelected())
			{
				addIconToLabel(checkBoxes[i],"warning_small");
			}
		}
		if(!allPanelsVisible)
			enablePanelsFor(checkBoxes);
	}
	private void addIconToLabel(JCheckBox checkBox, String status)
	{
		TextIcon checkBoxTextIcon = new TextIcon(checkBox,
				checkBox.getText().equals("")?((TextIcon)((CompoundIcon)checkBox.getIcon()).getIcon(1)).getText():checkBox.getText(),
				TextIcon.Layout.HORIZONTAL);
		ImageIcon checkBoxImageIcon = new ImageIcon(getClass().getResource("img/"+status+".gif"));
		checkBox.setText("");
		checkBox.setIcon(new CompoundIcon(CompoundIcon.Axis.X_AXIS,4,CompoundIcon.RIGHT,CompoundIcon.CENTER,UIManager.getIcon("CheckBox.icon"),checkBoxTextIcon,
				checkBoxImageIcon));
		NotificationPanel.updateNotification("Restriction Verification Needed", "warning");
	}
	
	private void removeIconFromLabel(JCheckBox checkBox)
	{
		String iconText = ((TextIcon)((CompoundIcon)checkBox.getIcon()).getIcon(1)).getText();
		checkBox.setIcon(checkBox.getSelectedIcon());
		checkBox.setText(iconText);
	}
	private void disableCheckBoxes(JCheckBox[] checkBoxes)
	{
		/*Despite what the name implies, this method does not assure that all the checkboxes in the passed array will be disabled (unchecked).
		 * It selectively unchecks the boxes that should be unchecked depending on whether each is referenced by or a child of any selected boxes.
		 * As it unchecks the boxes, if all the siblings of a checkbox are all unchecked,
		 * it decides that the parent panel of all the sibling group should be set invisible (if allPanelsVisible is set to false)*/
		JCheckBox[] tempCheck;
		boolean disable;
		boolean allSiblingsCleared;
		if(allCheckBox.isSelected())
			return;
		for (int i=0 ; i<checkBoxes.length ; i++)
		{
			disable=true;
			tempCheck = commandTable.get(checkBoxes[i]).getReferencingCheckBoxes();
			for(int j=0 ; j<tempCheck.length ; j++)
			{
				if(tempCheck[j].isSelected())
				{
					disable=false;
					break;
				}
				disable=true;
			}
			if(commandTable.get(checkBoxes[i]).getParentCheckBox().isSelected())
			{
				disable=false;
			}
			
			if(disable)
			{
				checkBoxes[i].setSelected(false);	
				if(hasWarning(checkBoxes[i]))
				{
					removeIconFromLabel(checkBoxes[i]);
					revalidateRestriction();
				}
				tempCheck = commandTable.get(checkBoxes[i]).getSiblingCheckBoxes();
				allSiblingsCleared = true;
				for(int j=0 ; j<tempCheck.length ; j++)
				{
					if(tempCheck[j].isSelected())
					{
						allSiblingsCleared = false;
						break;
					}
				}
				if(!allPanelsVisible && allSiblingsCleared)
				{
					checkBoxes[i].getParent().setVisible(false);
				}
			}
		}
	}
	private boolean hasWarning(JCheckBox checkBox)
	{
		return (checkBox.getIcon() instanceof CompoundIcon && ((CompoundIcon)checkBox.getIcon()).getIcon(2) instanceof ImageIcon);		
	}
	private void revalidateRestriction()
	{
		Enumeration<Command> commands = commandTable.elements();
		Command command;
		while(commands.hasMoreElements())
		{
			command = commands.nextElement();
			if(!command.autoCheck && hasWarning(command.getCommandCheckBox()))
			{
				return;
			}
		}
		NotificationPanel.updateNotification("", "None");
	}
	private void enableGeneralCheckBoxes()
	{
		enableCheckBoxes(generalCheckBoxes);
	}
	private void disableGeneralCheckBoxes()
	{
		disableCheckBoxes(generalCheckBoxes);
	}
	
	private void enableRestartCheckBoxes()
	{
		enableCheckBoxes(restartCheckBoxes);
		enableCheckBoxes(restartExtCheckBoxes);
	}
	
	private void disableRestartCheckBoxes()
	{
		disableCheckBoxes(restartCheckBoxes);
		disableCheckBoxes(restartExtCheckBoxes);
	}
	private void enableHangCheckBoxes()
	{
		enableCheckBoxes(hangCheckBoxes);
		enableCheckBoxes(hangExtCheckBoxes);
		revalidateHangRestrictions();
	}
	
	private void disableHangCheckBoxes()
	{
		disableCheckBoxes(hangCheckBoxes);
		disableCheckBoxes(hangExtCheckBoxes);
		revalidateHangRestrictions();
	}
	
	private void enableSysPerfCheckBoxes()
	{
		enableCheckBoxes(sysPerfCheckBoxes);
		enableCheckBoxes(sysPerfExtCheckBoxes);
	}
	
	private void disableSysPerfCheckBoxes()
	{
		disableCheckBoxes(sysPerfCheckBoxes);
		disableCheckBoxes(sysPerfExtCheckBoxes);
	}
	
	private void enableMiscCheckBoxes()
	{
		enableCheckBoxes(miscCheckBoxes);
	}
	
	private void disableMiscCheckBoxes()
	{
		disableCheckBoxes(miscCheckBoxes);
	}
	
	
	public void setOutputLocation(String location){
		outputLocation = location;
	}
	
	public void updatePanel(){
		NotificationPanel.updateNotification("Getting Host Name", "loading");
		getHostName();
		NotificationPanel.updateNotification("Checking DBS Status", "loading");
		 getDBSStatus();
		 NotificationPanel.updateNotification("Checking VPROC Status", "loading");
		 getVPROCStatus();
		 NotificationPanel.updateNotification("Checking DBS Version", "loading");
		 NotificationPanel.updateNotification("Checking PDE Version", "loading");
		 NotificationPanel.updateNotification("Checking OS Version", "loading");
		 getOSinfo();
		 NotificationPanel.updateNotification("Checking CTL Info", "loading");
		 getCTLInfo();
	}
	
	public void getDBSStatus(){
		String output = SSHTunnel.executeCommand("psh pdestate -a");
		output = output.substring(1, output.length()-1);
		DBSStatusLabel.setText(output);
	}
	
	public void getVPROCStatus(){
		//SSHTunnel.executeCommand("echo st not > vprocmanagerin.txt");
		//SSHTunnel.executeCommand("echo quit >> vprocmanagerin.txt");
		
		String VprocOut = SSHTunnel.executeCommand("vprocmanager <<- [VPRCMNGR]\n"
				+ "st not\n"
				+ "quit\n"
				+ "[VPRCMNGR]");
		
		//SSHTunnel.executeCommand("rm vprocmanagerin.txt");
		
		String DBSVersion = VprocOut.substring(VprocOut.indexOf("Release")+8, VprocOut.indexOf("Version")-1).trim();
		String PDEVersion = VprocOut.substring(VprocOut.indexOf("Version")+8, VprocOut.indexOf("VprocManager")-1).trim();
		
		setDBSVersion(DBSVersion);
		setPDEVersion(PDEVersion);
		
		System.out.println(DBSVersion);
		System.out.println(PDEVersion);
		
		VprocOut = VprocOut.substring(VprocOut.indexOf(':')+3, VprocOut.lastIndexOf(':')-31);
		
		VProcManagerLabel.setText(VprocOut);
		
		//done = true;
	}
	
	
	public void getCTLInfo(){
		//SSHTunnel.executeCommand("echo screen debug > ctlin.txt");
		//SSHTunnel.executeCommand("echo quit >> ctlin.txt");
		
		String ctlOut = SSHTunnel.executeCommand("ctl <<- [CTL]\n"
				+ "sc de\n"
				+ "quit\n"
				+ "[CTL]");
		//SSHTunnel.executeCommand("rm ctlin.txt");
		
		String ctl0 = ctlOut.substring(ctlOut.indexOf(":")+1, ctlOut.indexOf("(1)")-1).trim();
		String ctl1 = ctlOut.substring(ctlOut.indexOf(":", ctlOut.indexOf("(1)")+4), ctlOut.indexOf("(2)")-1).replace(':', ' ').trim();
		String ctl2 = ctlOut.substring(ctlOut.indexOf(":", ctlOut.indexOf("(2)")+4), ctlOut.indexOf("(3)")-1).replace(':', ' ').trim();
		String ctl4 = ctlOut.substring(ctlOut.indexOf(":", ctlOut.indexOf("(4)")+4), ctlOut.indexOf("(5)")-1).replace(':', ' ').trim();
		String ctl5 = ctlOut.substring(ctlOut.indexOf(":", ctlOut.indexOf("(5)")+4), ctlOut.indexOf("(6)")-1).replace(':', ' ').trim();
		String ctl6 = ctlOut.substring(ctlOut.indexOf(":", ctlOut.indexOf("(6)")+4), ctlOut.indexOf("(7)")-1).replace(':', ' ').trim();
	
		System.out.println(ctl0);
		System.out.println(ctl1);
		System.out.println(ctl2);
		System.out.println(ctl4);
		System.out.println(ctl5);
		System.out.println(ctl6);
		
		ctl0label.setText(ctl0);
		ctl1label.setText(ctl1);
		ctl2label.setText(ctl2);
		ctl4label.setText(ctl4);
		ctl5label.setText(ctl5);
		ctl6label.setText(ctl6);
		
		done = true;
	}
	
	public void setDBSVersion(String version){
		 DBSVersionLabel.setText(version);
		 systemInfo.setDBS(version);
	}
	
	public void setPDEVersion(String version){
		 
	PDEVersionLabel.setText(version);
	systemInfo.setPDE(version);
	}

public void getOSinfo(){
	String OsVersion = SSHTunnel.executeCommand("cat /etc/issue | grep SUSE");
	OsVersion = OsVersion.substring(OsVersion.indexOf('S'),OsVersion.length());
	OsVersion = OsVersion.substring(0, OsVersion.indexOf('-') - 1);
	OSVersionLabel.setText(OsVersion);
	systemInfo.setSLES(OsVersion);
}


public void getHostName(){
	String hostname = SSHTunnel.executeCommand("hostname");
	hostNameLabel.setText(hostname);
	systemInfo.setHostName(hostname.trim());
}
}

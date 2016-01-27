import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.SwingWorker;
import javax.swing.SwingConstants;


public class ViewpointPanel extends JPanel {

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JLabel lblConnectionDetails;
	JLabel viewpointVersionLabel;
	JLabel isVpClustered;
	JLabel secondaryServerLabel;
	JLabel primaryServerLabel; 
	JTextArea VPServicesStatusLabel;
	JButton btnNewButton_2;
	JButton btnNewButton;
	String outputLocation=".";
	private JLabel lblClientViewpoint;
	private JLabel label;
	private JLabel hostNameLabel;
	private Logger logger = AppLogger.getLogger();
	private SystemInfoRecord systemInfo = new SystemInfoRecord();
	/**
	 * Create the panel.
	 */
	public ViewpointPanel() {
		setBackground(Color.WHITE);
		setLayout(null);
		

		lblConnectionDetails = new JLabel("Viewpoint Version:");
		lblConnectionDetails.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblConnectionDetails.setForeground(new Color(244, 106, 9));
		lblConnectionDetails.setBounds(43, 68, 122, 14);
		add(lblConnectionDetails);
		
		JLabel lblViewpointClustered = new JLabel("Viewpoint Clustered:");
		lblViewpointClustered.setForeground(new Color(244, 106, 9));
		lblViewpointClustered.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblViewpointClustered.setBounds(43, 93, 122, 14);
		add(lblViewpointClustered);
		
		JLabel lblPrimaryServer = new JLabel("Active Server:");
		lblPrimaryServer.setForeground(new Color(244, 106, 9));
		lblPrimaryServer.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPrimaryServer.setBounds(43, 118, 122, 14);
		add(lblPrimaryServer);
		
		JLabel lblBackupServer = new JLabel("Backup Server:");
		lblBackupServer.setForeground(new Color(244, 106, 9));
		lblBackupServer.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBackupServer.setBounds(349, 118, 97, 14);
		add(lblBackupServer);
		
		JLabel lblServicesStatus = new JLabel("Services Status:");
		lblServicesStatus.setForeground(new Color(244, 106, 9));
		lblServicesStatus.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblServicesStatus.setBounds(43, 162, 122, 14);
		add(lblServicesStatus);
		
		viewpointVersionLabel = new JLabel("");
		viewpointVersionLabel.setBounds(175, 68, 242, 14);
		add(viewpointVersionLabel);
		
		isVpClustered = new JLabel("");
		isVpClustered.setBounds(175, 93, 151, 14);
		add(isVpClustered);
		
		primaryServerLabel = new JLabel("");
		primaryServerLabel.setBounds(175, 118, 151, 14);
		add(primaryServerLabel);
		
		secondaryServerLabel = new JLabel("");
		secondaryServerLabel.setBounds(456, 118, 144, 14);
		add(secondaryServerLabel);
		
		VPServicesStatusLabel = new JTextArea();
		VPServicesStatusLabel.setBounds(43, 187, 611, 148);
		add(VPServicesStatusLabel);
		
		btnNewButton_2 = new JButton("Collect Support Archive");
		btnNewButton_2.setBounds(282, 363, 201, 23);
		add(btnNewButton_2);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				collectSupportArchive();	
			}
		});
		
		btnNewButton = new JButton("Restart Services");
		btnNewButton.setToolTipText("Restarts Viewpoint Services in the correct order (Even if system is clustered)");
		btnNewButton.setBounds(516, 158, 138, 23);
		add(btnNewButton);
		
		lblClientViewpoint = new JLabel("Client - Viewpoint");
		lblClientViewpoint.setForeground(new Color(244, 106, 9));
		lblClientViewpoint.setFont(new Font("Tahoma", Font.BOLD, 32));
		lblClientViewpoint.setBounds(234, 5, 299, 37);
		add(lblClientViewpoint);
		
		label = new JLabel("Connected to:", SwingConstants.RIGHT);
		label.setForeground(new Color(244, 106, 9));
		label.setFont(new Font("Tahoma", Font.PLAIN, 11));
		label.setBounds(293, 43, 91, 14);
		add(label);
		
		hostNameLabel = new JLabel("");
		hostNameLabel.setBounds(394, 43, 91, 14);
		add(hostNameLabel);
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restartVPServices();	
			}
		});

		
		JButton btnNewButton2 = new JButton("");
		btnNewButton2.setToolTipText("Back");
		btnNewButton2.setBackground(Color.WHITE);
		btnNewButton2.setForeground(Color.WHITE);
		btnNewButton2.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/back.png")));
		btnNewButton2.setBounds(10, 11, 42, 37);
		btnNewButton2.setOpaque(false);
		btnNewButton2.setContentAreaFilled(false);
		btnNewButton2.setBorderPainted(false);
		btnNewButton2.setFocusPainted(false);
		add(btnNewButton2);
		btnNewButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GTSToolMain.showMainPanel();	
			}
		});
		//logger.info(AppLogger.LOGOBANNERVP+systemInfo.stringValue());
	}
	
	public SystemInfoRecord getSystemInfo()
	{
		return systemInfo;
	}
	
	public void getViewpointVersion(){
		String vpVersion = SSHTunnel.executeCommand("rpm -qa | grep viewpoint");
		viewpointVersionLabel.setText(vpVersion);
		systemInfo.setViewPoint(vpVersion);
	}
	
	public void isViewpointClustered(){
		if ((SSHTunnel.executeCommand("ls /etc/opt/teradata/viewpoint")).contains("distributed.cluster.properties")){
			isVpClustered.setText("Yes");
			String activeIP = SSHTunnel.executeCommand("grep -Po '(?<=active.database.host=).*' /etc/opt/teradata/viewpoint/distributed.cluster.properties |tr -d \"\r\"");
			primaryServerLabel.setText(activeIP);
			String standbyIP = SSHTunnel.executeCommand("grep -Po '(?<=standby.database.host=).*' /etc/opt/teradata/viewpoint/distributed.cluster.properties");
			secondaryServerLabel.setText(standbyIP);
		}
		else{
			isVpClustered.setText("No");
			primaryServerLabel.setText("NA");
			secondaryServerLabel.setText("NA");
		}
		
	}
	
	public String getDCSStatus(){
		return SSHTunnel.executeCommand("/etc/init.d/dcs status");
		
	}
	
	public void getVPServicesStatus(){
		VPServicesStatusLabel.setText(SSHTunnel.executeCommand("/opt/teradata/viewpoint/bin/vp-control.sh status"));
		
	}
	
	public void restartVPServices(){
	
		NotificationPanel.updateNotification("Please Wait", "loading");

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					btnNewButton_2.setEnabled(false);
					btnNewButton.setEnabled(false);	
					SSHTunnel.executeCommand("/opt/teradata/viewpoint/bin/vp-control.sh restart");
					getVPServicesStatus();
					NotificationPanel.updateNotification("VP services restarted", "success");
					return true;
				} catch (Exception e) {
					// SQLQuery.JDBCDisconnect();
					NotificationPanel.updateNotification("VP services restarting failed", "fail");
					return false;
				} finally {
					btnNewButton_2.setEnabled(true);
					btnNewButton.setEnabled(true);	
					
				}

			}

			// Can safely update the GUI from this method.
			protected void done() {

				// boolean status;
				try {
					// Retrieve the return value of doInBackground.
					// status = get();
					get();
					// statusLabel.setText("Completed with status: " + status);
				} catch (InterruptedException e) {
					// This is thrown if the thread's interrupted.
				} catch (ExecutionException e) {
					// This is thrown if we throw an exception
					// from doInBackground.
				}
			}

			@Override
			// Can safely update the GUI from this method.
			protected void process(List<Integer> chunks) {
				chunks.get(chunks.size() - 1);

				// countLabel1.setText(Integer.toString(mostRecentValue));
			}

		};

		worker.execute();

	}
		
	
	
	public void collectSupportArchive(){
		
		NotificationPanel.updateNotification("Collecting Support archive, Please Wait", "loading");

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					btnNewButton_2.setEnabled(false);
					btnNewButton.setEnabled(false);
					String collectArchiveOutput = SSHTunnel.executeCommand("/opt/teradata/viewpoint/bin/supportarchive.sh");
					
				
					String Output = collectArchiveOutput.substring(collectArchiveOutput.lastIndexOf(':')+1);
					System.out.println(Output);
	
					String Filelocation = Output.trim();
					System.out.println(Filelocation);
	
					SFTPTunnel.getFile(Filelocation, outputLocation);
					NotificationPanel.updateNotification("Support archive collected", "success");
					//updatePanel();
				
					return true;
				} catch (Exception e) {
					// SQLQuery.JDBCDisconnect();
					NotificationPanel.updateNotification("Support archive collection failed", "fail");
					
					return false;
				} finally {
					btnNewButton_2.setEnabled(true);
					btnNewButton.setEnabled(true);	
					
				}

			}

			// Can safely update the GUI from this method.
			protected void done() {

				// boolean status;
				try {
					// Retrieve the return value of doInBackground.
					// status = get();
					get();
					// statusLabel.setText("Completed with status: " + status);
				} catch (InterruptedException e) {
					// This is thrown if the thread's interrupted.
				} catch (ExecutionException e) {
					// This is thrown if we throw an exception
					// from doInBackground.
				}
			}

			@Override
			// Can safely update the GUI from this method.
			protected void process(List<Integer> chunks) {
				chunks.get(chunks.size() - 1);

				// countLabel1.setText(Integer.toString(mostRecentValue));
			}

		};

		worker.execute();

	}
	
	public void setOutputLocation(String location){
		outputLocation = location;
	}
	
	public void updatePanel(){
		NotificationPanel.updateNotification("retrieving hostname", "loading");
		getHostName();
		NotificationPanel.updateNotification("Checking Version", "loading");
		getViewpointVersion();
		NotificationPanel.updateNotification("Checking Viewpoint Cluster configuration", "loading");
		isViewpointClustered();
		NotificationPanel.updateNotification("Verifying Service status", "loading");
		getVPServicesStatus();
		
		NotificationPanel.updateNotification("Collecting Other information", "loading");
		
		//Calling Chiran's Script:
		
		createFileName();
		getVPPackageVersions();
		getSUSEInfo();
		getUptime();
		getFreeMemory();
		getNodeType();
		getservicesStatus();
		getViewpointClustered();
		getdiskFree();
		getcheckDiskPolicy();
		getdeployedPortlets();
		getEnabledMonSystems();
		getenabledMonSystemsDbOsVersion();
		getDisabledCollectorsforEnabledSystems();
		getDisabledCollectorsforEnabledSystemsNoLogins();
		getCollectorSampleRates();
		getpossibleHungCollector();
		getTopGrowingTables();
		NotificationPanel.updateNotification("Done", "success");
		
	}
	public void getHostName(){
		String hostname = SSHTunnel.executeCommand("hostname");
		hostNameLabel.setText(hostname);
	}
	
	
	//################################################
	//############## Chiran's Script #################
	//################################################
	
	public void getViewpointClustered(){
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Checking Viewpoint Clustering.......");
		printNewLine();
		printLines();

		if ((SSHTunnel.executeCommand("ls /etc/opt/teradata/viewpoint")).contains("distributed.cluster.properties")){
			String activeIP = SSHTunnel.executeCommand("grep -Po '(?<=active.database.host=).*' /etc/opt/teradata/viewpoint/distributed.cluster.properties |tr -d \"\\r\"");
			String checkOnwhichServer = SSHTunnel.executeCommand("ifconfig |grep "+activeIP);
			String sendOnWhichWeAre;
			String noOfArchiveFiles;
			String archiveDirectorySize;
			String hostname = SSHTunnel.executeCommand("hostname").trim();
			if(!checkOnwhichServer.equals(""))	{
				sendOnWhichWeAre="You are on Active "+hostname+" : "+activeIP;
				noOfArchiveFiles="Number of files/folders in /data/archive folder are :"+SSHTunnel.executeCommand("ls -l /data/archive |wc -l");
				archiveDirectorySize="/data partition folder size\n"+SSHTunnel.executeCommand("cd /data; du -sh *");
				
				printNewLine();
				writeToFile("Viewpoint servers are cluetered");
				printNewLine();
				writeToFile(sendOnWhichWeAre);
				printNewLine();
				writeToFile(noOfArchiveFiles);
				printNewLine();
				writeToFile(archiveDirectorySize);
			
			}
			else	{
				sendOnWhichWeAre="You are not on Active Viewpoint";
				printNewLine();
				writeToFile(sendOnWhichWeAre);
			}
			primaryServerLabel.setText(activeIP);
			String standbyIP = SSHTunnel.executeCommand("grep -Po '(?<=standby.database.host=).*' /etc/opt/teradata/viewpoint/distributed.cluster.properties");
			secondaryServerLabel.setText(standbyIP);
		}
		else{
			isVpClustered.setText("No");
			primaryServerLabel.setText("NA");
			secondaryServerLabel.setText("NA");
			printNewLine();
			writeToFile("Viewpoint Server is not cluetered/standalone");
		}
		
	}		
	
	
	
	
	static String timeStamp;
	static String fileName;
	
	public static void createFileName()	{
		DateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
		timeStamp = format.format(new Date());
		fileName="viewScript"+timeStamp+".txt";
		System.out.println(fileName);
	}
	

	
	public  void getVPPackageVersions()	{
		String packageVersions = SSHTunnel.executeCommand("rpm -qa viewpoint dcs camalert tdactivemq tmsmonitor postgresql91-server");
		String versionHeader = "Viewpoint and related service Versions";
		printLines();
		printNewLine();
		writeToFile(versionHeader);
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(packageVersions);
	}
	
	public  void getSUSEInfo()	{
		String suseRelease = SSHTunnel.executeCommand("cat /etc/issue");
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("SUSE Version");
		printNewLine();
		printLines();
		writeToFile(suseRelease);
	}
	
	public  void getUptime()	{
		String uptimeValue = SSHTunnel.executeCommand("uptime");
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("System Uptime");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(uptimeValue);		
	}
	
	public  void getFreeMemory()	{
		String availMemory = SSHTunnel.executeCommand("free -m");
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Free memory in MBs");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(availMemory);
	}
	
	public  void getNodeType()	{
		String hummanNodeType;
		if( (SSHTunnel.executeCommand("/opt/teradata/gsctools/bin/nodetype").contains("tms_viewpoint")) )
			hummanNodeType="This is a Physical Viewpoint";
		else if( (SSHTunnel.executeCommand("/opt/teradata/gsctools/bin/nodetype").contains("tms_viewpoint_vm")) )
			hummanNodeType="This is VM Viewpoint";
		else
			hummanNodeType="Viewpoint type Physical/VM cannot be Identified.";
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Viewpoint Type is");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(hummanNodeType);
	}
	
	public  void getservicesStatus()	{		
		String viewpointServiceFlag;
		String viewpointServiceFlagCode;
		if ( (SSHTunnel.executeCommand("/etc/init.d/viewpoint status").contains("running")) )
			viewpointServiceFlagCode="t";
		else
			viewpointServiceFlagCode="f";
		
		String dcsServiceFlagCode;
		if ( (SSHTunnel.executeCommand("/etc/init.d/dcs status").contains("running")) )
			dcsServiceFlagCode="t";
		else
			dcsServiceFlagCode="f";
		
		String camalertServiceFlagCode;
		if ( (SSHTunnel.executeCommand("/etc/init.d/camalert status").contains("running")) )
			camalertServiceFlagCode="t";
		else
			camalertServiceFlagCode="f";
		
		String tdactivemqServiceFlagCode;
		if ( (SSHTunnel.executeCommand("/etc/init.d/tdactivemq status").contains("running")) )
			tdactivemqServiceFlagCode="t";
		else
			tdactivemqServiceFlagCode="f";
		
		String tmsmonitorServiceFlagCode;
		if ( (SSHTunnel.executeCommand("/etc/init.d/tmsmonitor status").contains("running")) )
			tmsmonitorServiceFlagCode="t";
		else
			tmsmonitorServiceFlagCode="f";
		
		String postgresqlServiceFlagCode;
		if ( (SSHTunnel.executeCommand("/etc/init.d/postgresql status|grep running").contains("running")) )
			postgresqlServiceFlagCode="t";
		else
			postgresqlServiceFlagCode="f";
		
		String allServiceStatus;
		String additionalCommand;
		if ( viewpointServiceFlagCode.equals("t") && dcsServiceFlagCode.equals("t") && camalertServiceFlagCode.equals("t") && tdactivemqServiceFlagCode.equals("t") && tmsmonitorServiceFlagCode.equals("t") && postgresqlServiceFlagCode.equals("t") )	{
			printNewLine();
			printNewLine();
			printLines();
			printNewLine();
			writeToFile("Viewpoint Services Status/Summary");
			printNewLine();
			printLines();
			printNewLine();
			allServiceStatus="All Viewpoint services are running.";
			writeToFile(allServiceStatus);
		}
		else	{
			allServiceStatus="One or more services are not running";
			additionalCommand=SSHTunnel.executeCommand("/opt/teradata/viewpoint/dcs/bin/vp-control.sh status");
			printNewLine();
			printNewLine();
			printLines();
			printNewLine();
			writeToFile("Viewpoint Services Status/Summary");
			printNewLine();
			printLines();
			printNewLine();
			writeToFile(allServiceStatus);
			printNewLine();
			writeToFile(additionalCommand);
		}
	}
	
	public  void getdiskFree()	{
		String diskFree = SSHTunnel.executeCommand("df -h");
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Avaialble space");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(diskFree);
	}
	
	public  void getcheckDiskPolicy()	{
		String diskPolicy = SSHTunnel.executeCommand("omreport storage vdisk controller=0 vdisk=1");
		String displayMessage;
		String machineType;
		String diskPolicyDisplay;
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Checking Disk write Policy.......");
		printNewLine();
		printLines();
		
		if ( (SSHTunnel.executeCommand("omreport storage vdisk controller=0 vdisk=1").contains("Write Back")))	{
			displayMessage="No issues found";
			printNewLine();
			writeToFile(displayMessage);
		}
		else	{
			displayMessage="One or more Disk policy is set to Write Through";
			machineType=SSHTunnel.executeCommand("machinetype");
			diskPolicyDisplay=SSHTunnel.executeCommand("omreport storage vdisk");
			
			printNewLine();
			writeToFile(displayMessage);
			printNewLine();
			writeToFile(machineType);
			printNewLine();
			writeToFile(diskPolicyDisplay);
		}
	}
	
	public  void getdeployedPortlets()	{
		String serverID = SSHTunnel.executeCommand("cat /etc/opt/teradata/viewpoint/server.id");
		String listDeployedPortlets = SSHTunnel.executeCommand("PGPASSWORD=\"TDv1i2e3w4\" psql -U viewpoint -d td_portal -P pager -c \"select portletid,version from portlet_registry where deployed='t' and serverid='"+serverID+"'\"");
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Deployed Portlets");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listDeployedPortlets);
		
	}
	
	public  void getEnabledMonSystems()	{
		String listEnabledMonitoredSystems = SSHTunnel.executeCommand("PGPASSWORD=\"TDd1c2s3\" psql -U dcs -d dcsdb -P pager -c \"select id, name, host, tasmtype from config.systems where enabled='t' and type='TERADATA' order by id\"");
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("List of Enabled Teradata Monitored systems");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listEnabledMonitoredSystems);
	}
	
	public  void getenabledMonSystemsDbOsVersion()	{
		String listenabledMonSystemsDbOsVersion = SSHTunnel.executeCommand("PGPASSWORD=\"TDd1c2s3\" psql -U dcs -d dcsdb -P pager -c \"select logtime, systemid, version, osversion from teradata_database_info where logtime = (select max(logtime) from teradata_database_info as tdi where tdi.systemid = teradata_database_info.systemid and tdi.systemid in (select id from config.systems where enabled='t' and type='TERADATA')) order by systemid\"");
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Enabled Teradata Monitored systems database and OS info");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listenabledMonSystemsDbOsVersion);
		
	}
	
	public  void getDisabledCollectorsforEnabledSystems()	{
		String listDisabledCollectorsforEnabledSystems  = SSHTunnel.executeCommand("PGPASSWORD=\"TDd1c2s3\" psql -U dcs -d dcsdb -P pager -c \"select ct.id, ct.systemid, ct.collectorname, config.logins.username from config.collection_targets as ct inner join config.logins on ct.loginid=config.logins.id where ct.enabled='f' and ct.systemid in (select id from config.systems where enabled='t' and type='TERADATA') order by ct.systemid\"");
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Disabled Collectors for enabled systems");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listDisabledCollectorsforEnabledSystems);
	}
	
	public  void getDisabledCollectorsforEnabledSystemsNoLogins()	{
		String listDisabledCollectorsforEnabledSystemsNoLogins = SSHTunnel.executeCommand("PGPASSWORD=\"TDd1c2s3\" psql -U dcs -d dcsdb -P pager -c \"select id,systemid, collectorname from config.collection_targets where enabled='f' and loginid is NULL and systemid in (select id from config.systems where enabled='t' and type='TERADATA') order by systemid\"");
	
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Disabled Collectors for enabled systems with no logins");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listDisabledCollectorsforEnabledSystemsNoLogins);
	}
	
	public  void getCollectorSampleRates()	{
		String listCollectorSampleRates = SSHTunnel.executeCommand("PGPASSWORD=\"TDd1c2s3\" psql -U dcs -d dcsdb -P pager -c \"select targetid, collection_targets.systemid, collection_targets.collectorname, config.logins.username, collectionrate from config.collection_triggers inner join config.collection_targets on config.collection_triggers.targetid=config.collection_targets.id inner join config.logins on config.logins.id=config.collection_targets.loginid and collection_targets.systemid in (select id from config.systems where enabled='t' and type='TERADATA') where enabled='t' order by collection_targets.systemid\"");
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Collector sample rates for enabled systems and enabled collectors");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listCollectorSampleRates);
		
	}
	
	public  void getpossibleHungCollector()	{
		String listPossibleHungCollector = SSHTunnel.executeCommand("PGPASSWORD=\"TDd1c2s3\" psql -U dcs -d dcsdb -P pager -c \"select config.collection_targets.systemid as \\\"System Id\\\", cp.collectorid as \\\"Collector Id\\\",cp.starttime as \\\"Collector Start Timestamp\\\",config.collection_targets.collectorname as \\\"Collector Name\\\" from collection_progress cp inner join config.collection_targets on cp.collectorid=config.collection_targets.id where cp.endtime is null and (current_timestamp)>(cp.starttime+interval '15 minutes') and cp.collectorid in (select id from config.collection_targets where enabled='t' and systemid in (select id from config.systems where enabled='t' and type='TERADATA')) order by starttime desc limit 100;\"");
		
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Possible Collector hung or Collector failed with Error");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listPossibleHungCollector);
	}
	
	public  void getTopGrowingTables()	{
		String listTopGrowingTables = SSHTunnel.executeCommand("PGPASSWORD=\"TDd1c2s3\" psql -U dcs -d dcsdb -P pager -c \"SELECT  table_schema || '.' || table_name AS table_full_name, pg_size_pretty(pg_total_relation_size('\\\"' || table_schema || '\\\".\\\"' || table_name || '\\\"')) AS size  FROM information_schema.tables ORDER BY pg_total_relation_size('\\\"' || table_schema || '\\\".\\\"' || table_name || '\\\"') DESC limit 150\"");
	
		printNewLine();
		printNewLine();
		printLines();
		printNewLine();
		writeToFile("Top 150 Growing tables");
		printNewLine();
		printLines();
		printNewLine();
		writeToFile(listTopGrowingTables);
		//JOptionPane.showMessageDialog(null, "Log File created and path is : "+System.getProperty("user.home")+"\\"+fileName);
	}
	
	
	public  void writeToFile(String contents)	{
		try	{
			String userHomeFolder = System.getProperty("user.home");
			String filename=fileName;
			System.out.println(outputLocation);
			FileWriter fw = new FileWriter( new File(outputLocation,fileName),true);
			fw.write(contents);
			fw.close();
			System.out.println("wrote toe:"+ outputLocation+"//"+fileName);
		}

		catch(IOException ioe)	{
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}
		
	public  void printLines()	{
		String equalDashes="============================================";
		writeToFile(equalDashes);
	}
	
	public  void printNewLine()	{
		String newLine="\n";
		writeToFile(newLine);
	}
	
	
	public void openFile()	{
		try	{
			java.awt.Desktop.getDesktop().open(new File(outputLocation+"\\"+fileName));
		}
		catch(FileNotFoundException ex){
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	
}

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.SwingConstants;


public class ViewScript extends JPanel {

	
	
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
	String outputLocation=".";
	private JLabel lblClientViewpoint;
	private JLabel label;
	private JLabel hostNameLabel;
	/**
	 * Create the panel.
	 */
	public ViewScript() {
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
		
		JLabel lblBackupServer = new JLabel("Standby Server:");
		lblBackupServer.setForeground(new Color(244, 106, 9));
		lblBackupServer.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblBackupServer.setBounds(349, 118, 97, 14);
		add(lblBackupServer);
		

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
		
		
		JButton btnNewButton_2 = new JButton("Open Log File");
		btnNewButton_2.setBounds(282, 235, 201, 23);
		add(btnNewButton_2);
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFile();
			}
		});
			
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
	}
	
	public void getViewpointVersion(){
		viewpointVersionLabel.setText(SSHTunnel.executeCommand("rpm -qa viewpoint"));
		
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
	
	
	public void setOutputLocation(String location){
		outputLocation = location;
	}
	
	public void updatePanel(){
		getHostName();
		getViewpointVersion();
		isViewpointClustered();
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
	}
	
	String timeStamp;
	String fileName;
	
	public void createFileName()	{
		DateFormat format = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");
		timeStamp = format.format(new Date());
		fileName="viewScript"+timeStamp+".txt";
	}
	
	public void getHostName(){
		String hostname = SSHTunnel.executeCommand("hostname");
		hostNameLabel.setText(hostname);	
		
	}
	
	public void getVPPackageVersions()	{
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
	
	public void getSUSEInfo()	{
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
	
	public void getUptime()	{
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
	
	public void getFreeMemory()	{
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
	
	public void getNodeType()	{
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
	
	public void getservicesStatus()	{		
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
	
	public void getdiskFree()	{
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
	
	public void getcheckDiskPolicy()	{
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
	
	public void getdeployedPortlets()	{
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
	
	public void getEnabledMonSystems()	{
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
	
	public void getenabledMonSystemsDbOsVersion()	{
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
	
	public void getDisabledCollectorsforEnabledSystems()	{
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
	
	public void getDisabledCollectorsforEnabledSystemsNoLogins()	{
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
	
	public void getCollectorSampleRates()	{
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
	
	public void getpossibleHungCollector()	{
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
	
	public void getTopGrowingTables()	{
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
		JOptionPane.showMessageDialog(null, "Log File created and path is : "+System.getProperty("user.home")+"\\"+fileName);
	}
	
	
	public void writeToFile(String contents)	{
		try	{
			String userHomeFolder = System.getProperty("user.home");
			String filename=fileName;
			FileWriter fw = new FileWriter( new File(userHomeFolder,filename),true);
			fw.write(contents);
			fw.close();
		}

		catch(IOException ioe)	{
			System.err.println("IOException: " + ioe.getMessage());
		}
		
	}
		
	public void printLines()	{
		String equalDashes="============================================";
		writeToFile(equalDashes);
	}
	
	public void printNewLine()	{
		String newLine="\n";
		writeToFile(newLine);
	}
	
	
	public void openFile()	{
		try	{
			java.awt.Desktop.getDesktop().open(new File(System.getProperty("user.home")+"\\"+fileName));
		}
		catch(FileNotFoundException ex){
			ex.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
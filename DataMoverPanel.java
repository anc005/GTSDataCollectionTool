import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.awt.Color;
import javax.swing.JScrollPane;
import javax.swing.border.LineBorder;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;

public class DataMoverPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	//Versions
	JTextArea DBSStatusLabel;
	JLabel dmDaemonVersion;
	JLabel dmAgentVersion;
	JLabel activeMQServiceStatusLabel;
	
	//Datamover service status
	JLabel dmDaemonServiceStatusLabel;
	JLabel dmAgentServiceStatusLabel;
	
	//Processes on all agents
	JTextArea arcmainProcessLabel;
	JTextArea tptProcessLabel;
	
	//Sync
	JLabel syncStatusLabel;
		
	String outputLocation=".";
	/**
	 * Create the panel.
	 */
	public DataMoverPanel() {
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBackground(Color.WHITE);
		setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 325, 503, -323);
		add(scrollPane);
		
		JLabel lblDataMoverChecklist = new JLabel("Data Mover Checklist");
		lblDataMoverChecklist.setForeground(new Color(244, 106, 9));
		lblDataMoverChecklist.setFont(new Font("Tahoma", Font.BOLD, 25));
		lblDataMoverChecklist.setBounds(250, 15, 274, 34);
		add(lblDataMoverChecklist);
		
		JLabel lblDmVersion = new JLabel("DMDaemon");
		lblDmVersion.setForeground(new Color(244, 106, 9));
		lblDmVersion.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblDmVersion.setBounds(83, 118, 99, 22);
		add(lblDmVersion);
		
		JLabel lblNewLabel = new JLabel("Teradata DB:");
		lblNewLabel.setForeground(new Color(244, 106, 9));
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel.setBounds(65, 251, 123, 18);
		add(lblNewLabel);
		
		//#3 Database status
		DBSStatusLabel = new JTextArea();
		DBSStatusLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		DBSStatusLabel.setEditable(false);
		DBSStatusLabel.setBounds(188, 251, 212, 22);
		add(DBSStatusLabel);
		
		dmDaemonVersion = new JLabel("");
		dmDaemonVersion.setFont(new Font("Tahoma", Font.PLAIN, 13));
		dmDaemonVersion.setBackground(Color.WHITE);
		dmDaemonVersion.setBounds(230, 118, 123, 16);
		add(dmDaemonVersion);
		
		dmAgentVersion = new JLabel("");
		dmAgentVersion.setFont(new Font("Tahoma", Font.PLAIN, 13));
		dmAgentVersion.setBounds(230, 159, 105, 16);
		add(dmAgentVersion);
		
		JLabel lblDmagentVersion = new JLabel("DMAgent");
		lblDmagentVersion.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblDmagentVersion.setForeground(new Color(244, 106, 9));
		lblDmagentVersion.setBounds(96, 159, 75, 22);
		add(lblDmagentVersion);
		
		dmDaemonServiceStatusLabel = new JLabel("");
		dmDaemonServiceStatusLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		dmDaemonServiceStatusLabel.setBounds(397, 118, 106, 16);
		add(dmDaemonServiceStatusLabel);
		
		dmAgentServiceStatusLabel = new JLabel("");
		dmAgentServiceStatusLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		dmAgentServiceStatusLabel.setBounds(398, 159, 105, 16);
		add(dmAgentServiceStatusLabel);
		
		JLabel lblVersion = new JLabel("Version");
		lblVersion.setForeground(new Color(244, 106, 9));
		lblVersion.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblVersion.setBounds(230, 79, 62, 16);
		add(lblVersion);
		
		JLabel lblServiceStatus = new JLabel("Service Status");
		lblServiceStatus.setForeground(new Color(244, 106, 9));
		lblServiceStatus.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblServiceStatus.setBounds(363, 79, 123, 16);
		add(lblServiceStatus);
		
		JLabel lblNewLabel_1 = new JLabel("ActiveMQ");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel_1.setForeground(new Color(244, 106, 9));
		lblNewLabel_1.setBounds(94, 198, 88, 25);
		add(lblNewLabel_1);
		
		activeMQServiceStatusLabel = new JLabel("");
		activeMQServiceStatusLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
		activeMQServiceStatusLabel.setBounds(397, 197, 105, 16);
		add(activeMQServiceStatusLabel);
		
		JLabel lblArcmainProcesses = new JLabel("Arcmain Processes:");
		lblArcmainProcesses.setForeground(new Color(244, 106, 9));
		lblArcmainProcesses.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblArcmainProcesses.setBounds(49, 304, 158, 16);
		add(lblArcmainProcesses);
		
		JLabel lblTptProcesses = new JLabel("TPT Processes:");
		lblTptProcesses.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblTptProcesses.setForeground(new Color(244, 106, 9));
		lblTptProcesses.setBounds(406, 304, 133, 16);
		add(lblTptProcesses);
		
		arcmainProcessLabel = new JTextArea("");
		arcmainProcessLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		arcmainProcessLabel.setBounds(49, 334, 308, 57);
		add(arcmainProcessLabel);
		
		tptProcessLabel = new JTextArea("");
		tptProcessLabel.setFont(new Font("Tahoma", Font.PLAIN, 10));
		tptProcessLabel.setBounds(406, 334, 337, 57);
		add(tptProcessLabel);
		
		JButton btnAutomaticFailure = new JButton("Get Automatic Failure Files");
		btnAutomaticFailure.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getFailoverFiles();
			}
		});
		btnAutomaticFailure.setBounds(554, 199, 189, 25);
		add(btnAutomaticFailure);
		
		JButton btnDmSynchronizationService = new JButton("Get DM Sync Service Files");
		btnDmSynchronizationService.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getSyncfiles();
			}
		});
		btnDmSynchronizationService.setBounds(554, 96, 189, 25);
		add(btnDmSynchronizationService);
		
		JButton btnGet = new JButton("Get DM Support Files");
		btnGet.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getFileLoc();
			}
		});
		btnGet.setFont(new Font("Tahoma", Font.PLAIN, 12));
		btnGet.setBounds(554, 237, 189, 25);
		add(btnGet);
		
		syncStatusLabel = new JLabel("");
		syncStatusLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		syncStatusLabel.setBounds(565, 137, 178, 38);
		add(syncStatusLabel);
	}
	
	/*
	 * Set Output Location - Used by GTSToolMain
	 */
	public void setOutputLocation(String location){
		outputLocation = location;
	}
	
	/* 
	 * Updates panels with automated information - Used by GTSToolMain
	 */
	public void updatePanel(){
		
		getDBSState();
		getDMVersions();
		getDMServiceStatus();
		getConfigFile();
		getAgentsFile();
		getJobsFile();
		getArcmainTPTProcesses();
		getPropertiesFiles();
	}	
	
	/*
	 * #1 ... get DMDaemon & DMAgent Versions
	 */
	public void getDMVersions(){
		
		String dmDaemonInfo, dmDaemonVer;
		String dmAgentInfo, dmAgentVer;
		
		//DMDaemon
		dmDaemonInfo = SSHTunnel.executeCommand("rpm -qa | grep DMDaemon");
		dmDaemonVer = dmDaemonInfo.substring(dmDaemonInfo.indexOf("-")+1, dmDaemonInfo.length()-3);
		dmDaemonVersion.setText(dmDaemonVer);
		
		//DMAgent
		dmAgentInfo = SSHTunnel.executeCommand("rpm -qa | grep DMAgent");
		dmAgentVer = dmAgentInfo.substring(dmAgentInfo.indexOf("-")+1, dmAgentInfo.length()-3);
		dmAgentVersion.setText(dmAgentVer);
	}
	/*
	 * +#1 - File locations
	 */
	public void getFileLoc() {
				
		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			
			@Override
			protected Boolean doInBackground() throws Exception {
				
				String output;										  //Output of support file
				ArrayList<String> fileLoc = new ArrayList<String>();  //ArrayList of file locations
				int outputLength;									  //Length of current output	
				int newStartIndex;									  //Start index of output for fileLoc 
				
				//Create file
				SSHTunnel.executeCommand("touch input.txt");
				SSHTunnel.executeCommand("echo INCIDENT > input.txt");
				SSHTunnel.executeCommand("echo \"GTS Collection Script\" >> input.txt");
				SSHTunnel.executeCommand("echo no >> input.txt");
				SSHTunnel.executeCommand("echo no >> input.txt");
				SSHTunnel.executeCommand("echo no >> input.txt");
				
				//Update panel notifications
				NotificationPanel.updateNotification("Please wait. Script Running.", "success");
				
				//Run Script
				output = SSHTunnel.executeCommand("/opt/teradata/datamover/support/dmsupport.sh < input.txt");
				
				NotificationPanel.updateNotification("Script completed. Extracting file locations.", "success");
				
			
				//Parse file locations
				output = output.substring(output.indexOf("ready for review")+12);
				outputLength = output.length();
				
				//Add file locations into fileLoc ArrayList while file locations exist
				while((outputLength >= 0 ) && (output.indexOf("zip") != -1)) {
					
					newStartIndex = output.indexOf("zip")+3;
					
					fileLoc.add(output.substring(output.indexOf("/"), newStartIndex));
					
					//Update output to not include old file loc
					output = output.substring(newStartIndex);
					outputLength = output.length();
				}
				
				NotificationPanel.updateNotification("File locations extracted. Downloading files.", "success");
				
				int fileLocLength = fileLoc.size();
				int locIndex = 0;
				
				try {
					//Loop through each file location to output
					while(locIndex < fileLocLength) {
						
						SFTPTunnel.getFile(fileLoc.get(locIndex), outputLocation);
//						NotificationPanel.updateNotification("Jobs List file" + locIndex + " downloaded", "success");
						locIndex++;
					}
					
					NotificationPanel.updateNotification("All Support Files downloaded", "success");
					
					SSHTunnel.executeCommand("rm input.txt");
					
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Support Files collection failed", "fail");
					return false;
				} finally {
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
	
	/*
	 * Description: #3: Verify state of underlying Teradata database
	 *                  on the DM server*/
	public void getDBSState(){
		
		String output = SSHTunnel.executeCommand("pdestate -a");
		output = output.substring(1, output.length()-1);
		DBSStatusLabel.setText(output);
	}
	
	/*
	 * #4 - List Jobs...NEED TO FORMAT
	 */
	public void getJobsFile(){
		
		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			
			@Override
			protected Boolean doInBackground() throws Exception {
				SSHTunnel.executeCommand("touch job_list.txt");
				
				SSHTunnel.executeCommand("datamove list_jobs > job_list.txt");
						
				NotificationPanel.updateNotification("Collecting Jobs List file, Please Wait", "loading");
				
				try {
					SFTPTunnel.getFile("job_list.txt", outputLocation);
					NotificationPanel.updateNotification("Jobs List file downloaded", "success");
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Jobs List file collection failed", "fail");
					return false;
				} finally {
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

	
	/*
	 * #5 Run "datamove list_configuration" and provide resulting configuration.xml file
	 */
	public void getConfigFile(){

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			
			@Override
			protected Boolean doInBackground() throws Exception {
				SSHTunnel.executeCommand("datamove list_configuration");
				
				NotificationPanel.updateNotification("Collecting Config file, Please Wait", "loading");
				
				try {
					SFTPTunnel.getFile("configuration.xml", outputLocation);
					NotificationPanel.updateNotification("Config file downloaded", "success");
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Config file collection failed", "fail");
					return false;
				} finally {
					
					
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
	
	/*
	 * #8 - List agents ...NEED TO FORMAT
	 */
	public void getAgentsFile(){

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				
				SSHTunnel.executeCommand("touch agent_list.txt");
				
				SSHTunnel.executeCommand("datamove list_agents >> agent_list.txt");
						
				NotificationPanel.updateNotification("Collecting Agent List file, Please Wait", "loading");
				
				try {
					SFTPTunnel.getFile("agent_list.txt", outputLocation);
					NotificationPanel.updateNotification("Agent List file downloaded", "success");
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Agent List file collection failed", "fail");
					return false;
				} finally {
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
	
	/*
	 * #10 - Datamover services' status
	 */
	public void getDMServiceStatus(){
		
		String dmDaemonStatus, dmAgentStatus, activeMQStatus;
		
		//DMDaemon
		dmDaemonStatus = SSHTunnel.executeCommand("/etc/init.d/dmdaemon status");
		dmDaemonStatus = dmDaemonStatus.substring(dmDaemonStatus.indexOf(":")+4, dmDaemonStatus.lastIndexOf("dmdaemon"));
		dmDaemonServiceStatusLabel.setText(dmDaemonStatus);
		
		//DMAgent
		dmAgentStatus = SSHTunnel.executeCommand("/etc/init.d/dmagent status");
		dmAgentStatus = dmAgentStatus.substring(dmAgentStatus.indexOf(":")+4, dmAgentStatus.lastIndexOf("dmagent"));
		dmAgentServiceStatusLabel.setText(dmDaemonStatus);
		
		//Active MQ
		activeMQStatus = SSHTunnel.executeCommand("/etc/init.d/tdactivemq status");
		activeMQStatus = activeMQStatus.substring(activeMQStatus.lastIndexOf(":")+4);
		activeMQServiceStatusLabel.setText(activeMQStatus);
	}

	/*
	 * #11 - Properties files from DM Server
	 */
	public void getPropertiesFiles() {

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				NotificationPanel.updateNotification("Collecting Properties Files, Please Wait", "loading");
				
				try {
					//agent
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/agent.properties", outputLocation);
					NotificationPanel.updateNotification("agent.properties downloaded", "success");
					
					//daemon
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/daemon.properties", outputLocation);
					NotificationPanel.updateNotification("daemon.properties downloaded", "success");
					
					//commandline
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/commandline.properties", outputLocation);
					NotificationPanel.updateNotification("commandline.properties downloaded", "success");
					
					//all
					NotificationPanel.updateNotification("Properties Files downloaded", "success");
					
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Properties Files collection failed", "fail");
					return false;
				} finally {
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
	
	
	/*
	 * #14 - Check if there are any running arcmain or TPT processes on all agents
	 */
	public void getArcmainTPTProcesses(){
		
		//arcmain
		String arcmainInfo = SSHTunnel.executeCommand("ps -aef | grep arcmain");
		arcmainProcessLabel.setText(arcmainInfo);
		
		//TPT 
		String tptInfo = SSHTunnel.executeCommand("ps -aef | grep DMTPT");
		tptProcessLabel.setText(tptInfo);
	}
	
	/*
	 * #15 - Datamover Sychronization service
	 */
	public void getSyncfiles() {

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				
				String syncServiceStatus = SSHTunnel.executeCommand("/opt/teradata/datamover/sync/15.00/dmsync status");
				syncStatusLabel.setText(syncServiceStatus);
				
				NotificationPanel.updateNotification("Collecting Sync Files, Please Wait", "loading");
				
				try {
					//sync.properties
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/sync.properties", outputLocation);
					NotificationPanel.updateNotification("sync.properties downloaded", "success");
					
					//dmSyncMaster.sql
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/dmSyncMaster.sql", outputLocation);
					NotificationPanel.updateNotification("dmSyncMaster.sql downloaded", "success");
					
					//dmSyncSlave.sql
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/dmSyncSlave.sql", outputLocation);
					NotificationPanel.updateNotification("dmSyncSlave.sql downloaded", "success");
					
					//dmSync.log
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/dmSync.log", outputLocation);
					NotificationPanel.updateNotification("dmSync.log downloaded", "success");
					
					//slave_x.lastread
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/slave_x.lastread", outputLocation);
					NotificationPanel.updateNotification("slave_x.lastread downloaded", "success");
					
					//slave_sql.lastExecuted
					SFTPTunnel.getFile("/etc/opt/teradata/datamover/slave_sql.lastExecuted", outputLocation);
					NotificationPanel.updateNotification("slave_sql.lastExecuted downloaded", "success");
					
					//all
					NotificationPanel.updateNotification("Sync Files downloaded", "success");
					
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Sync Files collection failed", "fail");
					return false;
				} finally {
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
	
	/*
	 * #16 - Failover log 
	 */
	public void getFailoverFiles(){
		
		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				
				SSHTunnel.executeCommand("touch serviceStatus.txt");
				
				SSHTunnel.executeCommand("/opt/teradata/client/15.00/datamover/failover/dmcluster status > serviceStatus.txt");
				
				NotificationPanel.updateNotification("Collecting Failover Log, Please Wait", "loading");

				try {
					//failover.properties
					SFTPTunnel.getFile("opt/teradata/client/15.00/datamover/failover/failover.properties", outputLocation);
					NotificationPanel.updateNotification("Failover Log downloaded", "success");
					
					//dmFailover.log
					SFTPTunnel.getFile("/var/opt/teradata/datamover/logs/dmFailover.log", outputLocation);
					NotificationPanel.updateNotification("Failover Log downloaded", "success");
					
					//serviceStatus.txt
					SFTPTunnel.getFile("serviceStatus.txt", outputLocation);
					NotificationPanel.updateNotification("serviceStatus.txt downloaded", "success");
					
					//all
					NotificationPanel.updateNotification("Failover Log downloaded", "success");
					
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Failover Log collection failed", "fail");
					return false;
				} finally {
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
}

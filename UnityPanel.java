import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Font;
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
import java.lang.String;

public class UnityPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	JLabel integrationStatusLabel;
	JTextArea dbInfoLabel;
	JLabel alertStatusLabel;
	
	String outputLocation=".";
	/**
	 * Create the panel.
	 */
	public UnityPanel() {
		setBorder(new LineBorder(new Color(0, 0, 0)));
		setBackground(Color.WHITE);
		setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 325, 503, -323);
		add(scrollPane);
		
		JLabel lblDataMoverChecklist = new JLabel("Unity Checklist");
		lblDataMoverChecklist.setForeground(new Color(244, 106, 9));
		lblDataMoverChecklist.setFont(new Font("Tahoma", Font.BOLD, 25));
		lblDataMoverChecklist.setBounds(276, 6, 201, 34);
		add(lblDataMoverChecklist);
		
		JButton btnUnityRecoveryFiles = new JButton("Get Recovery Files");
		btnUnityRecoveryFiles.setFont(new Font("Segoe UI", Font.PLAIN, 13));
		btnUnityRecoveryFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getRecoveryLogFiles();
			}
		});
		btnUnityRecoveryFiles.setBounds(90, 143, 176, 25);
		add(btnUnityRecoveryFiles);
		
		JLabel lblTmsmEmStatus = new JLabel("Unity TMSM/em Integration?: ");
		lblTmsmEmStatus.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblTmsmEmStatus.setForeground(new Color(244, 106, 9));
		lblTmsmEmStatus.setBounds(16, 239, 217, 16);
		add(lblTmsmEmStatus);
		
		integrationStatusLabel = new JLabel("");
		integrationStatusLabel.setBounds(230, 239, 88, 16);
		add(integrationStatusLabel);
		
		JLabel lblDatabaseVersionInfo = new JLabel("Database Version Info");
		lblDatabaseVersionInfo.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblDatabaseVersionInfo.setForeground(new Color(244, 106, 9));
		lblDatabaseVersionInfo.setBounds(517, 53, 160, 16);
		add(lblDatabaseVersionInfo);
		
		dbInfoLabel = new JTextArea();
		dbInfoLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
		dbInfoLabel.setBounds(488, 71, 276, 309);
		add(dbInfoLabel);
		
		JLabel lblHasUnityRaised = new JLabel("Has Unity raised any alerts?:");
		lblHasUnityRaised.setForeground(new Color(244, 106, 9));
		lblHasUnityRaised.setFont(new Font("Tahoma", Font.BOLD, 14));
		lblHasUnityRaised.setBounds(16, 296, 207, 16);
		add(lblHasUnityRaised);
		
		alertStatusLabel = new JLabel("Please wait for info.");
		alertStatusLabel.setBounds(230, 296, 229, 16);
		add(alertStatusLabel);
		
		JButton btnGetSupportFiles = new JButton("Get Support Files");
		btnGetSupportFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {	
				getSupportFile();
			}
		});
		btnGetSupportFiles.setBounds(90, 80, 176, 25);
		add(btnGetSupportFiles);
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
		getDBVersions();
		getAlertDetails();
		getTMSMStatus();
	}	
	
	/*
	 * #1 - Run Unity Support Script and grab split tarball 
	 */
	public void getSupportFile() {
			
		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				String output="";										  //Output of support file
				
				try {				
					SSHTunnel.executeCommand("touch input.txt");
					SSHTunnel.executeCommand("echo yes > input.txt");
					SSHTunnel.executeCommand("echo no >> input.txt");
					
					NotificationPanel.updateNotification("Script running", "success");
					
					output = SSHTunnel.executeCommand("/opt/teradata/unity/support/unity_support.sh < input.txt");
					
					/***************Parse file locations****************/
					
					//Condense string to optimize search
					output = output.substring(output.indexOf("Running gzip on /var/opt/teradata/unity/support//")+16, output.lastIndexOf("tar") + 3);
		
					SFTPTunnel.getFile(output + ".gz*", outputLocation);
					
					NotificationPanel.updateNotification("Support file(s) downloaded", "success");
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Support file(s) collection failed", "fail");
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
	 * #2 - Has unity raised any alerts?
	 * Grabs alert details and file
	 */
	public void getAlertDetails() {
		
		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				
				SSHTunnel.executeCommand("touch AlertDetails.txt");
				SSHTunnel.executeCommand("unityadmin -u admin -P admin -e \"alert list open\" > AlertDetails.txt");
								
				String output = SSHTunnel.executeCommand("unityadmin -u admin -P admin -e \"alert list open\" | grep \":\"");
				
				//If alerts were raised
				if(output.length() > 0) {
					alertStatusLabel.setText("Yes - Please refer to AlertDetails.txt");
				}
				//No alerts raised
				else {
					alertStatusLabel.setText("No.");
				}
				
				try {
					SFTPTunnel.getFile("AlertDetails.txt", outputLocation);
					NotificationPanel.updateNotification("Alert Details downloaded", "success");
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Alert Details collection failed", "fail");
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
	 * #4 - Unity recovery log file
	 * /var/opt/teradata/unity/support/logs
	 * grep for recovery
	 * google tar command syntax linux
	 */
	public void getRecoveryLogFiles() {
		
		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				NotificationPanel.updateNotification("tar in process", "success");
				SSHTunnel.executeCommand("tar -jcf /recovery/unity_recovery_log.tar.bz2 /recovery/recovery.log");
				
				try {
					SFTPTunnel.getFile("/recovery/unity_recovery_log.tar.bz2", outputLocation);
					NotificationPanel.updateNotification("Recovery Log downloaded", "success");
					return true;
				} catch (Exception e) {
					NotificationPanel.updateNotification("Recovery Log collection failed", "fail");
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
	
	/*#5 - DB Versions*/
	public void getDBVersions() {
		//create command input file
		String output = SSHTunnel.executeCommand("unityadmin -u admin -P admin -e \"system info\" | grep \"\"");
			
		String outLabel = "";
		int startIndex = output.indexOf("System ID");
		int endIndex = output.indexOf("Character sets");
		
		//while there are still systems, parse info 
		while(output.indexOf("System ID") != -1) {
			
			outLabel += output.substring(startIndex, endIndex) + "\n";
			
			output = output.substring(endIndex + 2);
			
			startIndex = output.indexOf("System ID");
			endIndex = output.indexOf("Character sets");
			
			
		}
		
		dbInfoLabel.setText(outLabel);
			
	}
	
	
	/*
	 * #8 - Is Unity integrated with TMSM?
	 */
	public void getTMSMStatus(){
		MainPanel mainpanel = new MainPanel();
		
		String outStatus = SSHTunnel.executeCommand("cat /etc/opt/teradata/config/unity.properties");
		
		//Integration Exists
		if(outStatus.indexOf("tdpid="+mainpanel.getHostName()) != -1 ) {
			integrationStatusLabel.setText("Yes");
		}
		//Integration does not exist
		else {
			integrationStatusLabel.setText("No");
		}
	}
}

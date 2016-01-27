import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.plaf.OptionPaneUI;


public class MainPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTextField[] hostname = new JTextField[4];
	private JTextField[] username = new JTextField[4];
	private JButton testConnectivityButton;
	private JFileChooser chooser;
	private JLabel lblConnectionDetails;
	private JLabel lblstHop;
	private JLabel lblOutputFileLocation;
	private JCheckBox[] hopsEnabled;
	private JPasswordField[] password = new JPasswordField[4];

	private JLabel fileSaveLocationTextfield;
	private JButton browseButton;
	private JButton connectButton;
	public static String outputLocation = "";
	public static String[] hosts;
	public static String[] usernames;
	public static String[] passwords;

	private JButton password0Toggle;
	private JButton password1Toggle;
	private JButton password2Toggle;
	private JButton password3Toggle;
	private JLabel lblProblemType;
	private JLabel lblRootPrivalages;
	private JPasswordField passwordField;
	private JComboBox comboBox;
	private Logger logger = null; 
	
	
	/**
	 * Create the panel.
	 */
	public MainPanel() {
		setBackground(Color.WHITE);
		setLayout(null);
		
		final JLabel connectionImageLabel = new JLabel("",SwingConstants.CENTER);
		connectionImageLabel.setVerticalAlignment(SwingConstants.CENTER);
		connectionImageLabel.setIcon(new ImageIcon(MainPanel.class.getResource("/img/1hop.jpg")));
		connectionImageLabel.setBounds(0, 11, 800, 64);
		add(connectionImageLabel);
		
		
		
		JButton btnNewButton2 = new JButton("");
		btnNewButton2.setToolTipText("About");
		btnNewButton2.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/about.png")));
		btnNewButton2.setBounds(755, 05, 32, 32);
		btnNewButton2.setOpaque(false);
		btnNewButton2.setContentAreaFilled(false);
		btnNewButton2.setBorderPainted(false);
		btnNewButton2.setFocusPainted(false);
		add(btnNewButton2);
		btnNewButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog((Component)SwingUtilities.getWindowAncestor(MainPanel.this),
					    "This is an Alpha version of the GTS Data Collection Tool. \n"
					    + "This tool will be used by Teradata GTS L1 and L2 support to collect troubleshooting information from customer systems.\n"
					    + "The tool is still under development, and bugs are still to be expected.\n\n\n"
					    + "Third Party License information:\n"
					    + "This software uses the JSch Open Source API. JSch is covered by the following notice:\n\n"
					    + "------------------------------------------------------------------------------------------\n\n"
					    + "JSch 0.0.* was released under the GNU LGPL license.  Later, we have switched\n"
					    + "over to a BSD-style license.\n"
					    + "\n"
+ "------------------------------------------------------------------------------\n"
+ "Copyright (c) 2002-2015 Atsuhiko Yamanaka, JCraft,Inc.\n"
+ "All rights reserved.\n"
+ "\n"
+ "Redistribution and use in source and binary forms, with or without\n"
+ "modification, are permitted provided that the following conditions are met:\n"
+ "\n"
  + "1. Redistributions of source code must retain the above copyright notice,\n"
  + "this list of conditions and the following disclaimer.\n"
+ "\n"
  + "2. Redistributions in binary form must reproduce the above copyright\n" 
  + "notice, this list of conditions and the following disclaimer in \n"
     + "the documentation and/or other materials provided with the distribution.\n"
+ "\n"
  + "3. The names of the authors may not be used to endorse or promote products\n"
  + "derived from this software without specific prior written permission.\n"
+ "\n"
+ "THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,\n"
+ "INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND\n"
+ "FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JCRAFT,\n"
+ "INC. OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,\n"
+ "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT\n"
+ "LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,\n"
+ "OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF\n"
+ "LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING\n"
+ "NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,\n"
+ "EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.",
					    
					    "About The GTS Collection Tool",
					    JOptionPane.WARNING_MESSAGE);	
			}
		});
		
		
//		hostname[0] = new JTextField("dbs124");
		hostname[0] = new JTextField("");
		hostname[0].setBounds(260, 129, 86, 20);
		add(hostname[0]);
		hostname[0].setColumns(10);

		username[0] = new JTextField("");
		username[0].setBounds(377, 129, 86, 20);
		add(username[0]);
		username[0].setColumns(10);

		hostname[1] = new JTextField();
		hostname[1].setBounds(260, 160, 86, 20);
		add(hostname[1]);
		hostname[1].setColumns(10);

		username[1] = new JTextField();
		username[1].setBounds(377, 160, 86, 20);
		add(username[1]);
		username[1].setColumns(10);

		hostname[2] = new JTextField();
		hostname[2].setBounds(260, 191, 86, 20);
		add(hostname[2]);
		hostname[2].setColumns(10);

		username[2] = new JTextField();
		username[2].setBounds(377, 191, 86, 20);
		add(username[2]);
		username[2].setColumns(10);

		hostname[3] = new JTextField();
		hostname[3].setBounds(260, 222, 86, 20);
		add(hostname[3]);
		hostname[3].setColumns(10);

		username[3] = new JTextField();
		username[3].setBounds(377, 222, 86, 20);
		add(username[3]);
		username[3].setColumns(10);

		JLabel lblNewLabel = new JLabel("IP/Hostname");
		lblNewLabel.setBounds(260, 102, 86, 14);
		add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Username");
		lblNewLabel_1.setBounds(377, 102, 86, 14);
		add(lblNewLabel_1);

		JLabel lblNewLabel_2 = new JLabel("Password");
		lblNewLabel_2.setBounds(493, 102, 86, 14);
		add(lblNewLabel_2);

		testConnectivityButton = new JButton("Test");
		testConnectivityButton.setBounds(493, 253, 89, 23);
		add(testConnectivityButton);
		testConnectivityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				testButtonPressed();
				

			}
		});

		lblConnectionDetails = new JLabel("Connection Details:");
		lblConnectionDetails.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblConnectionDetails.setForeground(new Color(244, 106, 9));
		lblConnectionDetails.setBounds(132, 102, 118, 14);
		add(lblConnectionDetails);

		lblstHop = new JLabel("  1st Hop");
		lblstHop.setBounds(185, 132, 48, 14);
		add(lblstHop);


		lblOutputFileLocation = new JLabel("Output file location:");
		lblOutputFileLocation.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblOutputFileLocation.setForeground(new Color(244, 106, 9));
		lblOutputFileLocation.setBounds(132, 320, 118, 14);
		add(lblOutputFileLocation);

		hopsEnabled = new JCheckBox[3];

		hopsEnabled[0] = new JCheckBox("Enable 2nd Hop");
		hopsEnabled[0].setBackground(Color.WHITE);
		hopsEnabled[0].setBounds(132, 159, 109, 23);
		add(hopsEnabled[0]);

		hopsEnabled[1] = new JCheckBox("Enable 3rd Hop");
		hopsEnabled[1].setBackground(Color.WHITE);
		hopsEnabled[1].setBounds(132, 190, 99, 23);
		add(hopsEnabled[1]);

		hopsEnabled[2] = new JCheckBox("Enable 4th Hop");
		hopsEnabled[2].setBackground(Color.WHITE);
		hopsEnabled[2].setBounds(132, 221, 101, 23);
		add(hopsEnabled[2]);

		password[0] = new JPasswordField("");
		password[0].setEchoChar('\u2022');
		password[0].setBounds(493, 129, 86, 20);
		add(password[0]);

		password[1] = new JPasswordField();
		password[1].setEchoChar('\u2022');
		password[1].setBounds(493, 160, 86, 20);
		add(password[1]);

		password[2] = new JPasswordField();
		password[2].setEchoChar('\u2022');
		password[2].setBounds(493, 191, 86, 20);
		add(password[2]);

		password[3] = new JPasswordField();
		password[3].setEchoChar('\u2022');
		password[3].setBounds(493, 222, 86, 20);
		add(password[3]);

		hostname[1].setEnabled(false);
		username[1].setEnabled(false);
		password[1].setEnabled(false);
		hostname[2].setEnabled(false);
		username[2].setEnabled(false);
		password[2].setEnabled(false);
		hostname[3].setEnabled(false);
		username[3].setEnabled(false);
		password[3].setEnabled(false);

		hopsEnabled[1].setEnabled(false);
		hopsEnabled[2].setEnabled(false);

		hopsEnabled[0].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				// the line below is the line that matters, that
				// enables/disables the text field
				if (hopsEnabled[0].isSelected()) {
					hostname[1].setEnabled(true);
					username[1].setEnabled(true);
					password[1].setEnabled(true);
					password1Toggle.setEnabled(true);
					hopsEnabled[1].setEnabled(true);
					connectionImageLabel.setIcon(new ImageIcon(MainPanel.class.getResource("/img/2hop.jpg")));
				} else {
					hostname[1].setEnabled(false);
					username[1].setEnabled(false);
					password[1].setEnabled(false);
					password1Toggle.setEnabled(false);
					hopsEnabled[1].setEnabled(false);
					hostname[2].setEnabled(false);
					username[2].setEnabled(false);
					password[2].setEnabled(false);
					password2Toggle.setEnabled(false);
					hopsEnabled[2].setEnabled(false);
					hostname[3].setEnabled(false);
					username[3].setEnabled(false);
					password[3].setEnabled(false);
					password3Toggle.setEnabled(false);
					hopsEnabled[1].setSelected(false);
					hopsEnabled[2].setSelected(false);
					connectionImageLabel.setIcon(new ImageIcon(MainPanel.class.getResource("/img/1hop.jpg")));

				}
			}
		});

		hopsEnabled[1].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				// the line below is the line that matters, that
				// enables/disables the text field
				if (hopsEnabled[1].isSelected()) {
					hostname[2].setEnabled(true);
					username[2].setEnabled(true);
					password[2].setEnabled(true);
					password2Toggle.setEnabled(true);
					hopsEnabled[2].setEnabled(true);
					connectionImageLabel.setIcon(new ImageIcon(MainPanel.class.getResource("/img/3hop.jpg")));
				} else {
					hostname[2].setEnabled(false);
					username[2].setEnabled(false);
					password[2].setEnabled(false);
					password2Toggle.setEnabled(false);
					hopsEnabled[2].setEnabled(false);
					hostname[3].setEnabled(false);
					username[3].setEnabled(false);
					password[3].setEnabled(false);
					password3Toggle.setEnabled(false);
					hopsEnabled[2].setSelected(false);
					connectionImageLabel.setIcon(new ImageIcon(MainPanel.class.getResource("/img/2hop.jpg")));
				}
			}
		});

		hopsEnabled[2].addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				// the line below is the line that matters, that
				// enables/disables the text field
				if (hopsEnabled[2].isSelected()) {
					hostname[3].setEnabled(true);
					username[3].setEnabled(true);
					password[3].setEnabled(true);
					password3Toggle.setEnabled(true);
					connectionImageLabel.setIcon(new ImageIcon(MainPanel.class.getResource("/img/4hop.jpg")));

				} else {
					hostname[3].setEnabled(false);
					username[3].setEnabled(false);
					password[3].setEnabled(false);
					password3Toggle.setEnabled(false);
					connectionImageLabel.setIcon(new ImageIcon(MainPanel.class.getResource("/img/3hop.jpg")));

				}
			}
		});


		fileSaveLocationTextfield = new JLabel("Output file location");
		fileSaveLocationTextfield.setBounds(260, 320, 203, 14);
		add(fileSaveLocationTextfield);

		browseButton = new JButton("Browse");
		browseButton.setBounds(493, 316, 89, 23);
		add(browseButton);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileSaveLocationTextfield.setText(startFileBrowser());
				fileSaveLocationTextfield
						.setHorizontalAlignment(SwingConstants.LEFT);
			}
		});

		connectButton = new JButton("Connect");
		connectButton.setBounds(493, 350, 89, 23);
		add(connectButton);
		connectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				connectButtonPressed();
			}
		});
		

		password0Toggle = new JButton("");
		password0Toggle.setIcon(new ImageIcon(MainPanel.class
				.getResource("/img/eye_icon.png")));
		password0Toggle.setFocusPainted(false);
		password0Toggle.setContentAreaFilled(false);
		password0Toggle.setBorder(BorderFactory.createEmptyBorder());
		password0Toggle.setBounds(581, 128, 25, 20);
		add(password0Toggle);
		password0Toggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (password[0].getEchoChar() == (char) 0) {
					password[0].setEchoChar('\u2022');
				} else {
					password[0].setEchoChar((char) 0);
				}
			}
		});

		password1Toggle = new JButton("");
		password1Toggle.setIcon(new ImageIcon(MainPanel.class
				.getResource("/img/eye_icon.png")));
		password1Toggle.setFocusPainted(false);
		password1Toggle.setContentAreaFilled(false);
		password1Toggle.setBorder(BorderFactory.createEmptyBorder());
		password1Toggle.setBounds(581, 159, 25, 20);
		password1Toggle.setEnabled(false);
		add(password1Toggle);
		password1Toggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (password[1].getEchoChar() == (char) 0) {
					password[1].setEchoChar('\u2022');
				} else {
					password[1].setEchoChar((char) 0);
				}
			}
		});

		password2Toggle = new JButton("");
		password2Toggle.setIcon(new ImageIcon(MainPanel.class
				.getResource("/img/eye_icon.png")));
		password2Toggle.setFocusPainted(false);
		password2Toggle.setContentAreaFilled(false);
		password2Toggle.setBorder(BorderFactory.createEmptyBorder());
		password2Toggle.setBounds(581, 190, 25, 20);
		password2Toggle.setEnabled(false);
		add(password2Toggle);
		password2Toggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (password[2].getEchoChar() == (char) 0) {
					password[2].setEchoChar('\u2022');
				} else {
					password[2].setEchoChar((char) 0);
				}
			}
		});

		password3Toggle = new JButton("");
		password3Toggle.setIcon(new ImageIcon(MainPanel.class
				.getResource("/img/eye_icon.png")));
		password3Toggle.setFocusPainted(false);
		password3Toggle.setContentAreaFilled(false);
		password3Toggle.setBorder(BorderFactory.createEmptyBorder());
		password3Toggle.setBounds(581, 221, 25, 20);
		password3Toggle.setEnabled(false);
		add(password3Toggle);
		
		lblProblemType = new JLabel("Troubleshooting Type:",SwingConstants.CENTER );
		lblProblemType.setForeground(new Color(244, 106, 9));
		lblProblemType.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblProblemType.setBounds(108, 354, 142, 14);
		add(lblProblemType);
		
		comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"< Select Area >", "Client - Viewpoint","DBS - General", "Client - Data Mover", "Client - Unity"/*,"Client - ViewScript"*/}));
		comboBox.setBounds(260, 351, 203, 20);
		add(comboBox);
		
		lblRootPrivalages = new JLabel("Get root privalages:");
		lblRootPrivalages.setForeground(new Color(244, 106, 9));
		lblRootPrivalages.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblRootPrivalages.setBounds(132, 288, 118, 14);
		add(lblRootPrivalages);
		
		JRadioButton rdbtnSudo = new JRadioButton("sudo");
		rdbtnSudo.setEnabled(false);
		rdbtnSudo.setBackground(Color.WHITE);
		rdbtnSudo.setBounds(260, 284, 48, 23);
		add(rdbtnSudo);
		
		JRadioButton rdbtnSu = new JRadioButton("su");
		rdbtnSu.setEnabled(false);
		rdbtnSu.setBackground(Color.WHITE);
		rdbtnSu.setBounds(310, 284, 42, 23);
		add(rdbtnSu);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(493, 285, 86, 20);
		passwordField.setEnabled(false);
		add(passwordField);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setForeground(new Color(244, 106, 9));
		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPassword.setBounds(377, 288, 61, 14);
		add(lblPassword);
		password3Toggle.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (password[3].getEchoChar() == (char) 0) {
					password[3].setEchoChar('\u2022');
				} else {
					password[3].setEchoChar((char) 0);
				}
			}
		});
	}
	public String startFileBrowser() {
		String directory = "";
		
		chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Select Output Directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);

		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			System.out.println("getCurrentDirectory(): "
					+ chooser.getCurrentDirectory());
			System.out.println("getSelectedFile() : "
					+ chooser.getSelectedFile());

			directory += chooser.getSelectedFile();
		} else {
			System.out.println("No Selection ");

		}
		outputLocation = directory;
		
		return directory;
	}
	public void testButtonPressed() {
		// get and construct the hosts, usernames and passwords String arrays by
		// looping the checkboxes
		int arraySizeCounter = 1; // always start with one because you need
		// atleast one hop to connect (direct
		// connection)
		// PUALogger.startlogger("C:\\Users\\RR186044\\Desktop");
		for (int i = 0; i < 3; i++) {
			if (hopsEnabled[i].isSelected()) {
				arraySizeCounter++;
			} else {
				break;
			}
		}

		// initialize arrays with the correct size
		hosts = new String[arraySizeCounter];
		usernames = new String[arraySizeCounter];
		passwords = new String[arraySizeCounter];
		for (int j = 0; j < arraySizeCounter; j++) {
			hosts[j] = hostname[j].getText();
			usernames[j] = username[j].getText();
			passwords[j] = String.valueOf(password[j].getPassword());
			;
		}

		NotificationPanel.updateNotification("Please Wait", "loading");

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					
					if (SSHTunnel.createConnection(hosts, usernames, passwords)) {
						NotificationPanel.updateNotification("Connection succesful", "success");
					} else {
						NotificationPanel.updateNotification("Connection failed", "fail");
					}

					return true;
				} catch (Exception e) {
					e.printStackTrace();
					NotificationPanel.updateNotification("Connection failed", "fail");

					return false;
				} finally {
					SSHTunnel.disconnect();
					
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
	
	
	public void connectButtonPressed() {
		
		if (comboBox.getSelectedIndex()==0 ){
			NotificationPanel.updateNotification("Please select the Troubleshooting Type", "fail");
		}
		else if (outputLocation.equals("")){
			NotificationPanel.updateNotification("Please select an output directory", "fail");
			
		}
		else{
			/*Instantiating the logger here to assure the output location has already been input in the file chooser, 
			 * and in case the incident number input is going to be implemented later, it will have been already input at this point.*/
			String timeTag = new java.text.SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS-z").format(new java.util.Date());
			/*Replace outputLocation with the String variable containing the incident number, when implemented (if other than outputLocation).
			 * Make sure if a different variable is instantiated for the output containing the incident number,
			 * pass it (instead of outputLocation) to the panels using GTSToolMain.showXXXPanel().*/
			logger = AppLogger.getLogger("MainPanel", outputLocation, "GTSTOOL-"+ timeTag +".log", AppLogger.ONELINEFORMATTER);
			logger.info(AppLogger.LOGOBANNERMAIN);
			GTSToolMain.showLoadingPanel();
		NotificationPanel.updateNotification("connecting", "loading");
		
		// get and construct the hosts, usernames and passwords String arrays by
		// looping the checkboxes
		int arraySizeCounter = 1; // always start with one because you need
		// atleast one hop to connect (direct
		// connection)

		for (int i = 0; i < 3; i++) {
			if (hopsEnabled[i].isSelected()) {
				arraySizeCounter++;
			} else {
				break;
			}
		}

		// initialize arrays with the correct size
		hosts = new String[arraySizeCounter];
		usernames = new String[arraySizeCounter];
		passwords = new String[arraySizeCounter];
		for (int j = 0; j < arraySizeCounter; j++) {
			hosts[j] = hostname[j].getText();
			usernames[j] = username[j].getText();
			passwords[j] = String.valueOf(password[j].getPassword());
			;

		}

		NotificationPanel.updateNotification("Please Wait", "loading");

		SwingWorker<Boolean, Integer> worker = new SwingWorker<Boolean, Integer>() {
			@Override
			protected Boolean doInBackground() throws Exception {
				try {
					
					NotificationPanel.updateNotification("Connecting to the server", "loading");					
					if (SSHTunnel.createConnection(hosts, usernames, passwords)) {
						SFTPTunnel.createConnection(hosts, usernames, passwords);
						
						if(comboBox.getSelectedIndex()==1){
							
							GTSToolMain.showViewpointPanel(outputLocation);
						}
						else if(comboBox.getSelectedIndex()==2){
							
							GTSToolMain.showDBSGeneralPanel(outputLocation);
						}
						else if(comboBox.getSelectedIndex() == 3){
						
							GTSToolMain.showDataMoverPanel(outputLocation);
						}
						else if(comboBox.getSelectedIndex() == 4) {
							GTSToolMain.showUnityPanel(outputLocation);
						}
						//else if(comboBox.getSelectedIndex() == 5) {
						//	GTSToolMain.showViewScript(outputLocation);
						//}
					} else {
						GTSToolMain.showMainPanel();
						NotificationPanel.updateNotification("Connection failed", "fail");
						}
					return true;
				} catch (Exception e) {
					// SQLQuery.JDBCDisconnect();
					 SSHTunnel.disconnect();
					 SFTPTunnel.disconnect();
					e.printStackTrace();

					return false;
				} finally {
					
					// Just for development we close connection here
					// SQLQuery.JDBCDisconnect();
					// SSHTunnel.disconnect();
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

	/*
	 * Get Hostname to be compared in #8 UnityPanel
	 */
	public String getHostName() {
		return hostname[0].getText();
	}
}



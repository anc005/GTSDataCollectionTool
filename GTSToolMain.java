import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.UIManager;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;

public class GTSToolMain {

	
	private JFrame frame;
	public static JLayeredPane panel;
	public static MainPanel mainPanel;
	public static ViewpointPanel viewpointPanel;
	public static DBSGeneralPanel DBSGeneralPanel;
	public static DataMoverPanel DataMoverPanel;
	public static UnityPanel UnityPanel;
	public static ViewScript ViewScript;
	public static loadingPanel loadingPanel;
	Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
	private final static String version = "0.1 Alpha";
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
					GTSToolMain window = new GTSToolMain();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GTSToolMain() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame("GTS Data Collection Tool - V"+version);
		frame.setBounds(0, 0, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation((dim.width / 2)
				- (frame.getSize().width / 2), (dim.height / 2)
				- (frame.getSize().height / 2));
		
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);
		
		panel = new JLayeredPane();
		panel.setForeground(Color.YELLOW);
		panel.setBackground(Color.RED);
		panel.setBounds(0, 83, 794, 399);
		frame.getContentPane().add(panel);
		
		
		NotificationPanel notificationPanel = new NotificationPanel();
		notificationPanel.setBackground(Color.DARK_GRAY);
		notificationPanel.setBounds(0, 481, 794, 94);
		frame.getContentPane().add(notificationPanel);
		
		
		mainPanel = new MainPanel();
		mainPanel.setBounds(0, 0, 794, 399);
		panel.add(mainPanel);
		
		
		loadingPanel = new loadingPanel();
		loadingPanel.setBounds(0, 0, 794, 399);
		loadingPanel.setVisible(false);
		panel.add(loadingPanel);
		
		
				JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.DARK_GRAY);
		panel_1.setBounds(0, 0, 806, 80);
		frame.getContentPane().add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(GTSToolMain.class.getResource("/img/logo.png")));
		lblNewLabel.setBackground(Color.DARK_GRAY);
		lblNewLabel.setBounds(10, 5, 280, 66);
		panel_1.add(lblNewLabel);
		
		JLabel label = new JLabel("");
		label.setIcon(new ImageIcon(GTSToolMain.class.getResource("/img/logo2.png")));
		label.setBounds(307, 26, 477, 34);
		panel_1.add(label);
	}
	public static void showViewpointPanel(String outputLocation){
		
		viewpointPanel = new ViewpointPanel();
		
		
		viewpointPanel.updatePanel();
		viewpointPanel.setOutputLocation(outputLocation);
		viewpointPanel.setBounds(0, 0, 794, 399);
		panel.add(viewpointPanel);
		loadingPanel.setVisible(false);
		viewpointPanel.setVisible(true);
		NotificationPanel.updateNotification("Review the Viewpoint information and collect support bundle if required", "None");
		AppLogger.getLogger().info(AppLogger.LOGOBANNERVP+viewpointPanel.getSystemInfo().stringValue());
	}
	
	
	public static void showViewScript(String outputLocation){
		
		ViewScript = new ViewScript();
		
		
		ViewScript.updatePanel();
		ViewScript.setOutputLocation(outputLocation);
		ViewScript.setBounds(0, 0, 794, 399);
		panel.add(ViewScript);
		loadingPanel.setVisible(false);
		ViewScript.setVisible(true);
		NotificationPanel.updateNotification("Please wait it takes upto 5 minutes to collect the information.\nLogs are Stored in "+System.getProperty("user.home")+" Directory, make sure you pick correct one.", "None");
	}
	
	
	public static void showDBSGeneralPanel(String outputLocation){
		
		//showLoadingPanel();
		
		DBSGeneralPanel = new DBSGeneralPanel();
		
		
		DBSGeneralPanel.setOutputLocation(outputLocation);
		DBSGeneralPanel.updatePanel();
		
		while (DBSGeneralPanel.done ==false){
			if (DBSGeneralPanel.done ==true)
				break;
		}
		
		loadingPanel.setVisible(false);
		DBSGeneralPanel.setBounds(0, 0, 794, 399);
		panel.add(DBSGeneralPanel, 99);
		DBSGeneralPanel.setVisible(true);
		NotificationPanel.updateNotification("Review the DBS status and take necessary action", "None");
	}
	/*
	 * Display Data Mover Panel - Used in MainPanel
	 */
	public static void showDataMoverPanel(String outputLocation){
		DataMoverPanel = new DataMoverPanel();
		
		DataMoverPanel.setOutputLocation(outputLocation);
		DataMoverPanel.updatePanel();
		
		loadingPanel.setVisible(false);
		DataMoverPanel.setBounds(0, 0, 794, 399);
		panel.add(DataMoverPanel);
		DataMoverPanel.setVisible(true);
		NotificationPanel.updateNotification("Review the Data Mover status and take necessary action", "None");
	}
	
	/*
	 * Display Unity Panel - Used in MainPanel
	 */
	public static void showUnityPanel(String outputLocation){
		UnityPanel = new UnityPanel();
		
		UnityPanel.setOutputLocation(outputLocation);
		UnityPanel.updatePanel();
		
		loadingPanel.setVisible(false);
		UnityPanel.setBounds(0, 0, 794, 399);
		panel.add(UnityPanel);
		UnityPanel.setVisible(true);
		NotificationPanel.updateNotification("Review the Unity status and take necessary action", "None");
	}
	
	public static void showLoadingPanel(){
		if(mainPanel != null){
		mainPanel.setVisible(false);
		}
		
		if(DBSGeneralPanel != null){
		DBSGeneralPanel.setVisible(false);
		}
		
		if(viewpointPanel != null){
		viewpointPanel.setVisible(false);
		}
		
		loadingPanel.setVisible(true);
	}
	
	
	public static void showMainPanel(){
		if(mainPanel != null){
			if(AppLogger.loggerExists())
			{
				AppLogger.endLogger("MainPanel");
			}
			mainPanel.setVisible(true);
		}
		
		if(DBSGeneralPanel != null){
		DBSGeneralPanel.setVisible(false);
		}
		
		if(viewpointPanel != null){
		viewpointPanel.setVisible(false);
		}
		
		if(loadingPanel != null){
		loadingPanel.setVisible(false);
		}
		
		if (SSHTunnel.session!= null){
			SSHTunnel.disconnect();
		}
		if (SFTPTunnel.session!= null){
			SFTPTunnel.disconnect();
		}
	}

	
}

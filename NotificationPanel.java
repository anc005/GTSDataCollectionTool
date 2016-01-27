import javax.swing.JPanel;

import java.awt.Color;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.SwingConstants;


public class NotificationPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
public static JLabel notificationTextMiddle;
public static JLabel statusIcon;
public static JButton btnNewButton;
	/**
	 * Create the panel.
	 */
	public NotificationPanel() {
		setBackground(Color.DARK_GRAY);
		setLayout(null);
		
		JLabel notificationTextTop = new JLabel("", SwingConstants.CENTER);
		notificationTextTop.setVerticalAlignment(SwingConstants.CENTER);
		notificationTextTop.setBounds(10, 11, 780, 14);
		add(notificationTextTop);
		
		JLabel notificationTextBottom = new JLabel("", SwingConstants.CENTER);
		notificationTextBottom.setVerticalAlignment(SwingConstants.CENTER);
		notificationTextBottom.setBounds(10, 49, 780, 32);
		add(notificationTextBottom);
		

		
		notificationTextMiddle = new JLabel("Enter the connection details", SwingConstants.CENTER);
		notificationTextMiddle.setVerticalAlignment(SwingConstants.CENTER);
		notificationTextMiddle.setForeground(Color.WHITE);
		notificationTextMiddle.setBounds(10, 36, 780, 14);
		add(notificationTextMiddle);

		statusIcon =  new JLabel("");
		statusIcon.setVerticalAlignment(SwingConstants.CENTER);
		statusIcon.setBounds(265, 36, 63, 14);
		add(statusIcon);
		
		/*btnNewButton = new JButton("");
		btnNewButton.setBackground(Color.DARK_GRAY);
		btnNewButton.setOpaque(false);
		btnNewButton.setContentAreaFilled(false);
		btnNewButton.setBorderPainted(false);
		btnNewButton.setIcon(new ImageIcon(NotificationPanel2.class.getResource("/img/back.png")));
		btnNewButton.setBounds(20, 24, 43, 39);
		add(btnNewButton);*/
	}

	public static void updateNotification(String message, String status){
		notificationTextMiddle.setText(message);
		java.net.URL imageURL = GTSToolMain.class.getResource("img/" + status
				+ ".gif");

		ImageIcon statusImage = null;
		if (imageURL != null) {

			statusImage = new ImageIcon(imageURL);
		}
		statusIcon.setIcon(statusImage);
		statusIcon.setVisible(true);
	}
}

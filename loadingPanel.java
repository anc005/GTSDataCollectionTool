import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.ImageIcon;


public class loadingPanel extends JPanel {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Create the panel.
	 */
	public loadingPanel() {
		setBackground(Color.WHITE);
		setLayout(null);
		
		JLabel lblNewLabel = new JLabel("");
		lblNewLabel.setIcon(new ImageIcon(loadingPanel.class.getResource("/img/bigloading.gif")));
		lblNewLabel.setBounds(204, 48, 412, 359);
		add(lblNewLabel);
		
		JLabel lblNewLabel_1 = new JLabel("");
		lblNewLabel_1.setIcon(new ImageIcon(loadingPanel.class.getResource("/img/loadingTxt.jpg")));
		lblNewLabel_1.setBounds(329, 0, 195, 52);
		add(lblNewLabel_1);
		
		
	}
	public void hidePanel(){
		setVisible(false);
	}
	public void showPanel(){
		setVisible(true);
	}
	
}

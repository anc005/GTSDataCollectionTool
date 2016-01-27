import java.awt.Color;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class ProgressDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JButton openProgressButton = new JButton("");
	private JTextArea progressTextArea = new JTextArea();
	private JScrollPane progressScrollPane = new JScrollPane(progressTextArea);
	
	public ProgressDialog(Frame owner, String title)
	{
		super(owner,title);
		
		openProgressButton.setToolTipText("Console");
		openProgressButton.setBackground(Color.WHITE);
		openProgressButton.setForeground(Color.WHITE);
		openProgressButton.setIcon(new ImageIcon(DBSGeneralPanel.class.getResource("/img/console.png")));
		openProgressButton.setOpaque(false);
		openProgressButton.setContentAreaFilled(false);
		openProgressButton.setBorderPainted(false);
		openProgressButton.setFocusPainted(false);
		openProgressButton.setVisible(false); //By default set as invisible until collection is executed.	
		
		progressTextArea.setEditable(false);
		//this.setLocationRelativeTo(openProgressButton);
		this.setModalityType(Dialog.ModalityType.valueOf("APPLICATION_MODAL"));
		this.add(progressScrollPane);
		this.setSize(1200, 400);
		
		
		openProgressButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ae)
			{
				ProgressDialog.this.setLocationRelativeTo(ProgressDialog.this.openProgressButton);
				ProgressDialog.this.setVisible(true);
			}
		});
	}
	public JTextArea getTextArea()
	{
		return progressTextArea;
	}
	public JButton getProgressButton()
	{
		return openProgressButton;
	}
	
	

}

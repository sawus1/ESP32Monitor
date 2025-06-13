package Dialogs;

import javax.swing.JDialog;
import javax.swing.JFrame;

public class SystemAnomalyDialog extends JDialog{
	private String title, message;
	public SystemAnomalyDialog(String title, String message) {
		super((JFrame)null, title, false);
		
	}

}

package net;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.net.ssl.SSLSocket;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CommandHandler {
	
	private PrintWriter out;
	private BufferedReader in;
	private DataReceiverThread t;
	private SSLSocket socket;
	private JTextField pathField;
	
	public CommandHandler(ConnectionManager m)
	{
		this.out = m.getOutput();
		this.in = m.getInput();
		this.socket = m.getSocket();
		t = m.getThread();
	}
	
	public void startMonitor() {
		if (out != null && !out.checkError()) {
			out.println("/start-monitor");
		}
	}

	public void stopMonitor() {
		if (out != null && !out.checkError()) {
			out.println("/stop-monitor");
		}
	}

	public void getSystemInfo() {
		if (out != null && !out.checkError()) {
			out.println("/get-sys-info");
		}
	}

	public void exitClient() {
		if (out != null && !out.checkError()) {
			stopMonitor();
			out.println("/disconnect");
			try {
				in.close();
				out.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		t.setState(false);
		System.exit(0);
	}
	
	public void resetConn()
	{
		if(out != null && !out.checkError()) {
			out.println("/reset-conn");
		}
	}
	
	public void systemReboot()
	{
		if(out != null && !out.checkError()) {
			out.println("/reboot-system");
		}
	}
	
	public void executeScript() {
		String path = pathField.getText(); 
		if(out != null && !out.checkError()) {
			out.println("/execute " + path);
		}
	}
	
	public void killProc(int pid) {
		if (out != null && !out.checkError()) {
			out.println("/kill-proc " + pid);
		}
	}

	public void getProcInfo(int pid) {
		if (out != null && !out.checkError()) {
			out.println("/get-proc-info " + pid);
		}
	}
	
	public void getScriptPath()
	{
		JDialog dialog = new JDialog((JFrame)null, "ESP32Monitor", true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setSize(300, 150);
		dialog.setLayout(new BorderLayout(10, 10));
		
		dialog.add(new JLabel("Please input the absolute script path:"), BorderLayout.NORTH);
		
		pathField = new JTextField();
		pathField.setBorder(BorderFactory.createLoweredSoftBevelBorder());
		dialog.add(pathField, BorderLayout.CENTER);
		
		JButton execButton = new JButton("Execute");
		execButton.addActionListener(e -> {
			executeScript();
			dialog.dispose();
		});
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dialog.dispose());
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(execButton);
		buttonPanel.add(closeButton);
		dialog.add(buttonPanel, BorderLayout.SOUTH);
		
		dialog.setLocationRelativeTo(null);
		dialog.setVisible(true);
	}

}

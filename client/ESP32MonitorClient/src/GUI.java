import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GUI implements ActionListener{
	
	private JLabel label;
	public GUI()
	{
		JFrame frame = new JFrame();
		JButton button = new JButton("ON");
		JButton button2 = new JButton("OFF");
		JButton button3 = new JButton("Get system data");
		JButton button4 = new JButton("Start monitoring");
		JButton button5 = new JButton("Stop monitoring");
		button.addActionListener(this);
		button2.addActionListener(new OFFAction());
		button3.addActionListener(new GetSysDataAction());
		button4.addActionListener(new StartMonitorAction());
		button5.addActionListener(new StopMonitorAction());
		label = new JLabel("Change LED state");
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
		panel.setLayout(new GridLayout());
		panel.add(label);
		panel.add(button);
		panel.add(button2);
		panel.add(button3);
		panel.add(button4);
		panel.add(button5);
		frame.add(panel, BorderLayout.CENTER);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("GUI");
		frame.pack();
		frame.setSize(900, 300);
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ESP32MonitorClient.setON();
		setLabelString("State: ON");
	}
	
	public void setLabelString(String s)
	{
		label.setText(s);
	}
}

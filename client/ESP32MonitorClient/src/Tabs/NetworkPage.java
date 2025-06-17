package Tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import SystemLoad.NetworkInterface;

public class NetworkPage extends JPanel{
	public void displayNetworkLoad(List<NetworkInterface> netIfs)
	{
		JPanel netPanel = new JPanel(new GridLayout(0,2));
		netPanel.setBorder(new TitledBorder("Network Interfaces"));
		for(NetworkInterface netIf : netIfs)
		{
			JPanel interfacePanel = new JPanel(new GridLayout(0,1));
			interfacePanel.setBorder(new TitledBorder(netIf.getName()));
			interfacePanel.add(new JLabel("Receive Bytes: " + netIf.getReceiveBytes()));
			interfacePanel.add(new JLabel("Transmit Bytes: " + netIf.getTransmitBytes()));
			netPanel.add(interfacePanel);
		}
		this.removeAll();
		this.add(netPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}
}

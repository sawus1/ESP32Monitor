package Tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import SystemLoad.Memory;
import common.ESP32MonitorClient;

public class MemoryPage extends JPanel{
	
	private LineChartPanel memUsagePanel, swapUsagePanel;
	
	public LineChartPanel getMemUsagePanel() {return memUsagePanel;}
	public LineChartPanel getSwapUsagePanel() {return swapUsagePanel; }
	
	public void setMemoryPanels(LineChartPanel memUsagePanel, LineChartPanel swapUsagePanel)
	{
		this.memUsagePanel = memUsagePanel;
		this.swapUsagePanel = swapUsagePanel;
	}

	
	public void displayMemoryLoad(Memory mem)
	{
		int memUsage = (int) mem.getUsedMemoryPercent();
		memUsagePanel.addDataPoint(memUsage);
		int swapUsage = (int) mem.getUsedSwapPercent();
		swapUsagePanel.addDataPoint(swapUsage);
		JPanel memPanel = new JPanel(new GridLayout(0,2));
		memPanel.setBorder(new TitledBorder("Memory Load"));
		memPanel.add(new JLabel("Memory Total: " + mem.getMemTotal() + " KB"));
		memPanel.add(new JLabel("Memory Free: " + mem.getMemFree() + " KB"));
		memPanel.add(new JLabel("Buffers: " + mem.getBuffers() + " KB"));
		memPanel.add(new JLabel("Cached: " + mem.getCached() + " KB"));
		memPanel.add(new JLabel("Swap Total: " + mem.getSwapTotal() + " KB"));
		memPanel.add(new JLabel("Swap Free: " + mem.getSwapFree() + " KB"));
		
		JPanel chartPanel = new JPanel(new GridLayout(0,2));
		
		chartPanel.add(memUsagePanel);
		chartPanel.add(swapUsagePanel);
		this.add(chartPanel);
		this.add(memPanel, BorderLayout.SOUTH);
		this.revalidate();
		this.repaint();
	}

}

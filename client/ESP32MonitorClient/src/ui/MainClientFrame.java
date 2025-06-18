package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import SystemInfo.SystemInfo;
import SystemLoad.ProcessInfo;
import SystemLoad.SysProc;
import SystemLoad.SystemLoadData;
import Tabs.*;
import net.CommandHandler;
import net.ConnectionManager;
import net.DataReceiverThread;

public class MainClientFrame extends JFrame{
	
	private ControlsPanel controlsC;
	private TabsPanel tabsC;
	private LineChartPanel cpuLoadPanel, memUsagePanel, swapUsagePanel;
	private SystemInfo info;
	private SystemLoadData load;
	private CommandHandler h;
	private DataReceiverThread t;
	private boolean ignore = false;
	
	public MainClientFrame(ConnectionManager m)
	{
		super("ESP32 Monitor");
		h = new CommandHandler(m);
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.setSize(800, 500);
    	this.setLayout(new BorderLayout());
    	
    	controlsC = new ControlsPanel(this, h);
    	this.add(controlsC, BorderLayout.WEST);
    	
    	tabsC = new TabsPanel(h);
    	this.add(tabsC, BorderLayout.CENTER);
		
		setCpuLoadPanel(new LineChartPanel("CPU Load", 100, 300, 200, Color.BLUE));
		setMemUsagePanel(new LineChartPanel("Memory Usage", 100, 200, 200, Color.BLUE));
		setSwapUsagePanel(new LineChartPanel("Swap Usage", 100, 200, 200, Color.GREEN));
		
		tabsC.getCpuPage().setCpuLoadPanel(cpuLoadPanel);
		tabsC.getMemoryPage().setMemoryPanels(memUsagePanel, swapUsagePanel);
    	
    	this.setLocationRelativeTo(null);
    	this.setVisible(true);
    	t = new DataReceiverThread(m, this);
    	t.start();
	}
	
	public void displaySystemInfo() {
	    tabsC.getSystemPage().displaySystemInfo(info);
	}
	public void displaySystemLoadData()
	{
		tabsC.getCpuPage().displayCPULoad(load.getCpuStat());
		tabsC.getMemoryPage().displayMemoryLoad(load.getMemory());
		tabsC.getNetworkPage().displayNetworkLoad(load.getNetworkInterfaces());
		tabsC.getDiskPage().displayDiskUsage(load.getDiskUsage());
		tabsC.getProcessPage().displayProcesses(load.getProcesses());
	}
	
	public void displayProcessInfo(SysProc info)
	{
		tabsC.getProcessPage().displayProcessInfo(info);
	}
	
	public void showDevMessage(String msg)
	{
		JDialog dialog = new JDialog((JFrame)null, "ESP32 Monitor", true);
	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	    dialog.setSize(300, 100);
	    dialog.setLayout(new BorderLayout());

	    dialog.add(new JLabel("Message from ESP32: " + msg), BorderLayout.CENTER);

	    JButton connectButton = new JButton("Retry");
	    connectButton.addActionListener(e -> {
	        h.resetConn();
	        dialog.dispose();
	    });
	    JButton closeButton = new JButton("Exit");
	    closeButton.addActionListener(e -> System.exit(0));

	    JPanel buttonPanel = new JPanel();
	    buttonPanel.add(connectButton);
	    buttonPanel.add(closeButton);
	    dialog.add(buttonPanel, BorderLayout.SOUTH);

	    dialog.setLocationRelativeTo(null);
	    dialog.setVisible(true);
	}
	
	public void checkForAnomalies()
	{
		List<Integer> cpuLoadPercents = cpuLoadPanel.getDataPoints();
		List<Integer> memoryLoadPercents = memUsagePanel.getDataPoints();
		List<Integer> swapUsagePercents = swapUsagePanel.getDataPoints();
		
		if(cpuLoadPercents.size() > 12 && memoryLoadPercents.size() > 12 && swapUsagePercents.size() > 12)
		{
			int cpucnt = 0;
			for(int i = cpuLoadPercents.size() - 1; i > cpuLoadPercents.size() - 12; i--)
			{
				if(cpuLoadPercents.get(i) > 95) cpucnt++;
			}
			if(cpucnt > 10 && !ignore)
			{
				JOptionPane.showMessageDialog(null, "Cpu load anomaly detected");
				ignore = true;
			}
			else if (cpucnt < 6) ignore = false;
			
			int memcnt = 0;
			for(int i = memoryLoadPercents.size() - 1; i > memoryLoadPercents.size() - 12; i--)
			{
				if(memoryLoadPercents.get(i) > 95) memcnt++;
			}
			if(memcnt > 10 && !ignore)
			{
				JOptionPane.showMessageDialog(null, "Memory usage anomaly detected");
				ignore = true;
			}
			else if (memcnt < 6) ignore = false;
			
			int swpcnt = 0;
			for(int i = swapUsagePercents.size() - 1; i > swapUsagePercents.size() - 12; i--)
			{
				if(swapUsagePercents.get(i) > 95) swpcnt++;
			}
			if(swpcnt > 10 && !ignore)
			{
				JOptionPane.showMessageDialog(null, "Swap usage anomaly detected");
				ignore = true;
			}
			else if (swpcnt < 6) ignore = false;
		}
	}

	public LineChartPanel getCpuLoadPanel() {return cpuLoadPanel;}
	public void setCpuLoadPanel(LineChartPanel cpuLoadPanel) {this.cpuLoadPanel = cpuLoadPanel;}

	public LineChartPanel getMemUsagePanel() {return memUsagePanel;}
	public void setMemUsagePanel(LineChartPanel memUsagePanel) {this.memUsagePanel = memUsagePanel;}

	public LineChartPanel getSwapUsagePanel() {return swapUsagePanel;}
	public void setSwapUsagePanel(LineChartPanel swapUsagePanel) {this.swapUsagePanel = swapUsagePanel;}
	
	public SystemLoadData getSystemLoad() {return this.load;}
	public void setSystemLoad(SystemLoadData load) {this.load = load;}
	
	public SystemInfo getSystemInfo() {return this.info;}
	public void setSystemInfo(SystemInfo info) {this.info = info;}
	
}

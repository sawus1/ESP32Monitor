package Tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import SystemLoad.CpuStat;

public class CpuPage extends JPanel {
	
	private CpuStat cpuprev;
	private LineChartPanel cpuLoadPanel;
	
	public LineChartPanel getCpuLoadPanel() {return cpuLoadPanel;}
	public void setCpuLoadPanel(LineChartPanel cpuLoadPanel) {this.cpuLoadPanel = cpuLoadPanel;}
	
	public void displayCPULoad(CpuStat cpu) {
		int cpuLoad = 0;
	    if (cpuprev != null) {
	        cpuLoad = (int)CpuStat.computeCpuUsage(cpuprev, cpu);
	        cpuLoadPanel.addDataPoint(cpuLoad);
	    }
	    cpuprev = cpu;

	    JPanel cpuPanel = new JPanel(new GridLayout(0, 2));
	    cpuPanel.setBorder(new TitledBorder("CPU Load"));
	    cpuPanel.add(new JLabel("user: " + cpu.getUser()));
	    cpuPanel.add(new JLabel("nice: " + cpu.getNice()));
	    cpuPanel.add(new JLabel("system: " + cpu.getSystem()));
	    cpuPanel.add(new JLabel("idle: " + cpu.getIdle()));
	    cpuPanel.add(new JLabel("iowait: " + cpu.getIowait()));
	    cpuPanel.add(new JLabel("irq: " + cpu.getIrq()));
	    cpuPanel.add(new JLabel("softirq: " + cpu.getSoftirq()));
	    cpuPanel.add(new JLabel("steal: " + cpu.getSteal()));
	    cpuPanel.add(new JLabel("guest: " + cpu.getGuest()));
	    cpuPanel.add(new JLabel("guest_nice: " + cpu.getGuestNice()));
	    cpuPanel.add(new JLabel("Current CPU Load: " + cpuLoad + "%"));
	    
	    this.removeAll();
	    
	    this.add(cpuLoadPanel);
	    this.add(cpuPanel, BorderLayout.SOUTH);
	    this.revalidate();
	    this.repaint();
	}
}

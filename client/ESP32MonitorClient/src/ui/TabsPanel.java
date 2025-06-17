package ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import Tabs.CpuPage;
import Tabs.DiskPage;
import Tabs.MemoryPage;
import Tabs.NetworkPage;
import Tabs.ProcessPage;
import Tabs.SystemPage;
import net.CommandHandler;

public class TabsPanel extends JPanel{
	
	private CommandHandler h;
	private CpuPage cpuPage;
	private DiskPage diskPage;
	private MemoryPage memoryPage;
	private NetworkPage networkPage;
	private ProcessPage processPage;
	private SystemPage systemPage;
	
	public TabsPanel(CommandHandler h)
	{
		this.setLayout(new BorderLayout());
		this.h = h;
		createTabs();
	}
	
	public void createTabs()
	{
		JTabbedPane tabPanel = new JTabbedPane();

        systemPage = new SystemPage();
        cpuPage = new CpuPage();
        memoryPage = new MemoryPage();
        networkPage = new NetworkPage();
        diskPage = new DiskPage();
        processPage = new ProcessPage(h);

        tabPanel.addTab("System", systemPage);
        tabPanel.addTab("CPU", cpuPage);
        tabPanel.addTab("Memory", memoryPage);
        tabPanel.addTab("Network", networkPage);
        tabPanel.addTab("Disk", diskPage);
        tabPanel.addTab("Processes", processPage);
        
        cpuPage.setLayout(new BorderLayout());
        memoryPage.setLayout(new BorderLayout());
        diskPage.setLayout(new BorderLayout());
        
        this.add(tabPanel);
		
	}
	
	public CpuPage getCpuPage() {return cpuPage;}
	public DiskPage getDiskPage() {return diskPage;}
	public MemoryPage getMemoryPage() {return memoryPage;}
	public NetworkPage getNetworkPage() {return networkPage;}
	public ProcessPage getProcessPage() {return processPage;}
	public SystemPage getSystemPage() {return systemPage;}

}

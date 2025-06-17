package Tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import SystemInfo.*;

public class SystemPage extends JPanel{
	
	public void displaySystemInfo(SystemInfo sysinfo) {
	    this.removeAll();
	    this.setLayout(new BorderLayout());

	    JPanel contentPanel = new JPanel();
	    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

	    JPanel kernelPanel = new JPanel(new GridLayout(0, 1));
	    kernelPanel.setBorder(new TitledBorder("Kernel Information"));
	    kernelPanel.add(new JLabel("Version: " + sysinfo.getKernel().getVersion()));
	    kernelPanel.add(new JLabel("Architecture: " + sysinfo.getKernel().getArchitecture()));
	    contentPanel.add(kernelPanel);

	    JPanel osPanel = new JPanel(new GridLayout(0, 1));
	    osPanel.setBorder(new TitledBorder("Operating System"));
	    OS os = sysinfo.getOs();
	    osPanel.add(new JLabel("Name: " + os.getName()));
	    osPanel.add(new JLabel("Version: " + os.getVersion()));
	    osPanel.add(new JLabel("ID: " + os.getId()));
	    osPanel.add(new JLabel("ID Like: " + os.getId_like()));
	    osPanel.add(new JLabel("Pretty Name: " + os.getPretty_name()));
	    osPanel.add(new JLabel("Version ID: " + os.getVersion_id()));
	    contentPanel.add(osPanel);

	    JPanel processorsPanel = new JPanel();
	    processorsPanel.setLayout(new BoxLayout(processorsPanel, BoxLayout.Y_AXIS));
	    processorsPanel.setBorder(new TitledBorder("Processors"));

	    List<Processor> processors = sysinfo.getProcessors();
	    for (int i = 0; i < processors.size(); i++) {
	        Processor p = processors.get(i);
	        JPanel procPanel = new JPanel(new GridLayout(0, 1));
	        procPanel.setBorder(BorderFactory.createTitledBorder("Processor " + (i + 1)));
	        procPanel.add(new JLabel("Model Name: " + p.getModel_name()));
	        procPanel.add(new JLabel("Vendor: " + p.getVendor()));
	        procPanel.add(new JLabel("Cores: " + p.getCores()));
	        procPanel.add(new JLabel("Cache Size: " + p.getCache_size() + " KB"));
	        procPanel.add(new JLabel("Clock Speed: " + p.getCpu_mhz() + " MHz"));
	        processorsPanel.add(procPanel);
	    }

	    contentPanel.add(processorsPanel);

	    JScrollPane scrollPane = new JScrollPane(contentPanel,
	            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
	    scrollPane.setBorder(null);

	    this.add(scrollPane, BorderLayout.CENTER);
	    this.revalidate();
	    this.repaint();
	}

}

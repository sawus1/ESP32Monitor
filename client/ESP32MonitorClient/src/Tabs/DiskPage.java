package Tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import SystemLoad.DiskUsage;

public class DiskPage extends JPanel{
	
	public void displayDiskUsage(List<DiskUsage> disks)
	{
	    JPanel disksPanel = new JPanel(new GridLayout(0, 2));
	    disksPanel.setBorder(new TitledBorder("Disks"));
	    int i = 0;
	    for (DiskUsage disk : disks)
	    {
	        JPanel diskPanel = new JPanel(new GridLayout(0, 1));
	        diskPanel.setBorder(new TitledBorder("Disk " + i));
	        diskPanel.add(new JLabel("Filesystem: " + disk.getFilesystem()));
	        diskPanel.add(new JLabel("Size: " + disk.getSize()));
	        diskPanel.add(new JLabel("Used: " + disk.getUsed()));
	        diskPanel.add(new JLabel("Available: " + disk.getAvailable()));
	        diskPanel.add(new JLabel("Usage Percent: " + disk.getUsePercent()));
	        diskPanel.add(new JLabel("Mount point: " + disk.getMountPoint()));
	        disksPanel.add(diskPanel);
	        i++;
	    }

	    JScrollPane scrollPane = new JScrollPane(disksPanel,
	        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
	        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
	    scrollPane.setBorder(null);

	    this.removeAll();
	    this.setLayout(new BorderLayout());
	    this.add(scrollPane, BorderLayout.CENTER);
	    this.revalidate();
	    this.repaint();
	}

}

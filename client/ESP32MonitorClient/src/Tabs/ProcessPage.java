package Tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import SystemLoad.SysProc;
import net.CommandHandler;

public class ProcessPage extends JPanel{
	
	
	private CommandHandler h;
	private Map<String, String> procs;
	
	public ProcessPage(CommandHandler h)
	{
		this.h = h;
	}
	
	public void displayProcesses(Map<String, String> processes) {
		if (!processes.equals(procs)) {
			procs = processes;
			this.removeAll();
			this.setLayout(new BorderLayout());

			DefaultListModel<String> listModel = new DefaultListModel<>();
			for (Map.Entry<String, String> entry : processes.entrySet()) {
				String pid = entry.getKey();
				String name = entry.getValue();
				listModel.addElement(pid + " - " + name);
			}

			JList<String> processList = new JList<>(listModel);
			processList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			processList.setBorder(new TitledBorder("Running Processes"));

			processList.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent evt) {
					if (evt.getClickCount() == 2) {
						String selected = processList.getSelectedValue();
						if (selected != null) {
							try {
								int pid = Integer.parseInt(selected.split(" - ")[0]);
								h.getProcInfo(pid);
							} catch (NumberFormatException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			});

			JScrollPane scrollPane = new JScrollPane(processList);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			this.add(scrollPane, BorderLayout.CENTER);
			this.revalidate();
			this.repaint();
		}
	}

	public void displayProcessInfo(SysProc info) {
		JFrame frame = new JFrame("Process Details - PID " + info.getPid());
		frame.setSize(350, 150);
		frame.setLayout(new BorderLayout());
	
		JPanel panel = new JPanel(new GridLayout(0, 2));
		panel.setBorder(new TitledBorder(""));
	
		panel.add(new JLabel("PID: " + info.getPid()));
		panel.add(new JLabel("Name: " + info.getName()));
		panel.add(new JLabel("State: " + info.getState()));
		panel.add(new JLabel("Parent PID: " + info.getPpid()));
		panel.add(new JLabel("Threads: " + info.getThreads()));
		panel.add(new JLabel("VM Size: " + info.getVmsize() + " KB"));
		panel.add(new JLabel("VM RSS: " + info.getVmrss() + " KB"));
		panel.add(new JLabel("VM Swap: " + info.getVmswap() + " KB"));
	
		frame.add(panel, BorderLayout.CENTER);
		JPanel controls = new JPanel(new GridLayout(0,2));
		
		JButton b = new JButton("Kill process");
		b.addActionListener(e -> h.killProc(info.getPid()));
		JButton ok = new JButton("OK");
		ok.addActionListener(e -> frame.dispose());
		controls.add(b);
		controls.add(ok);
		frame.add(controls, BorderLayout.SOUTH);
		frame.setLocationRelativeTo(this);
		frame.setVisible(true);
	}
}

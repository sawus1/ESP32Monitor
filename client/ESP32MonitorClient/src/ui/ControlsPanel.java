package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.SSLSocket;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import SystemInfo.SystemInfo;
import SystemLoad.SystemLoadData;
import net.*;

public class ControlsPanel extends JPanel{
	private JButton rebootButton, monitorStartButton, monitorStopButton, 
	getSystemInfoButton, runButton, exitButton, saveButton, loadButton;
	private CommandHandler h;
	private SaveLoad sv;
	
	public ControlsPanel(MainClientFrame frame, CommandHandler h)
	{
		this.h = h;
		sv = new SaveLoad(frame);
		this.setLayout(new GridLayout(7,1,10,10));
		createControls();
	}
	
	public void createControls() {
	    this.setLayout(new BorderLayout());

	    JPanel buttonsPanel = new JPanel();
	    buttonsPanel.setLayout(new GridLayout(0, 1, 10, 10));
	    buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

	    monitorStartButton = createStyledButton("▶ Start Monitoring", new Color(0, 102, 0));
	    monitorStopButton = createStyledButton("■ Stop Monitoring", new Color(153, 0, 0));
	    getSystemInfoButton = createStyledButton("🖥 Get System Info", new Color(0, 51, 102));
	    runButton = createStyledButton("⚙ Run Script", new Color(51, 51, 51));
	    saveButton = createStyledButton("💾 Save", new Color(0, 102, 204));
	    loadButton = createStyledButton("Load", new Color(0, 102, 204));
	    exitButton = createStyledButton("Disconnect & exit", new Color(0, 0, 0));
	    rebootButton = createStyledButton("⏻ Reboot System", Color.GREEN.darker());

	    monitorStartButton.addActionListener(e -> h.startMonitor());
	    monitorStopButton.addActionListener(e -> h.stopMonitor());
	    getSystemInfoButton.addActionListener(e -> h.getSystemInfo());
	    exitButton.addActionListener(e -> h.exitClient());
	    saveButton.addActionListener(e -> sv.saveData());
	    loadButton.addActionListener(e -> sv.loadData());
	    runButton.addActionListener(e -> h.executeScript());
	    rebootButton.addActionListener(e -> h.systemReboot());

	    buttonsPanel.add(monitorStartButton);
	    buttonsPanel.add(monitorStopButton);
	    buttonsPanel.add(getSystemInfoButton);
	    buttonsPanel.add(runButton);
	    buttonsPanel.add(saveButton);
	    buttonsPanel.add(loadButton);
	    buttonsPanel.add(rebootButton);
	    buttonsPanel.add(exitButton);

	    this.add(buttonsPanel, BorderLayout.CENTER);

	    this.revalidate();
	    this.repaint();
	}
	
	private JButton createStyledButton(String text, Color fgColor) {
	    JButton button = new JButton(text);
	    button.setFocusPainted(false);
	    button.setForeground(fgColor);
	    button.setFont(new Font("Segoe UI", Font.BOLD, 14));
	    button.setBorder(BorderFactory.createRaisedSoftBevelBorder());
	    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	    return button;
	}
}

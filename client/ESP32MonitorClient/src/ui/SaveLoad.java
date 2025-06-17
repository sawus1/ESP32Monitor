package ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import SystemInfo.SystemInfo;
import SystemLoad.SystemLoadData;

public class SaveLoad {
	
	private MainClientFrame frame;
	
	public SaveLoad(MainClientFrame frame)
	{
		this.frame = frame;
	}
	
	public void saveData() {
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("Save Monitoring Data");
	    int userSelection = fileChooser.showSaveDialog(null);

	    if (userSelection == JFileChooser.APPROVE_OPTION) {
	        File fileToSave = fileChooser.getSelectedFile();

	        List<Integer> cpuChartData = frame.getCpuLoadPanel().getDataPoints();
	        List<Integer> memChartData = frame.getMemUsagePanel().getDataPoints();
	        List<Integer> swapChartData = frame.getSwapUsagePanel().getDataPoints();

	        String sysInfoJson = frame.getSystemInfo().serializeJson();
	        String sysLoadJson = frame.getSystemLoad().serializeJson();

	        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
	            writer.write(cpuChartData.toString() + "\n");
	            writer.write(memChartData.toString() + "\n");
	            writer.write(swapChartData.toString() + "\n");
	            writer.write(sysInfoJson + "\n");
	            writer.write(sysLoadJson + "\n");
	            JOptionPane.showMessageDialog(null, "Data saved successfully.");
	        } catch (IOException e) {
	            e.printStackTrace();
	            JOptionPane.showMessageDialog(null, "Failed to save data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }
	}

	public void loadData() {
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("Open Monitoring Data File");
	    int userSelection = fileChooser.showOpenDialog(null);

	    if (userSelection == JFileChooser.APPROVE_OPTION) {
	        File fileToOpen = fileChooser.getSelectedFile();

	        try (BufferedReader reader = new BufferedReader(new FileReader(fileToOpen))) {
	            List<Integer> cpuChartData = parseDataLine(reader.readLine());
	            List<Integer> memChartData = parseDataLine(reader.readLine());
	            List<Integer> swapChartData = parseDataLine(reader.readLine());

	            String sysInfoJson = reader.readLine();
	            String sysLoadJson = reader.readLine();

	            frame.getCpuLoadPanel().setDataPoints(cpuChartData);
	            frame.getMemUsagePanel().setDataPoints(memChartData);
	            frame.getSwapUsagePanel().setDataPoints(swapChartData);

	            frame.setSystemInfo(new SystemInfo(sysInfoJson));
	            frame.setSystemLoad(new SystemLoadData(sysLoadJson));
	            
	            frame.displaySystemInfo();
	            frame.displaySystemLoadData();

	            JOptionPane.showMessageDialog(null, "Data loaded successfully.");
	        } catch (IOException | RuntimeException e) {
	            JOptionPane.showMessageDialog(null, "Failed to load data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
	        }
	    }
	}

	private List<Integer> parseDataLine(String line) {
	    return Arrays.stream(line.replaceAll("[\\[\\]\\s]", "").split(","))
	                 .filter(s -> !s.isEmpty())
	                 .map(Integer::parseInt)
	                 .collect(Collectors.toList());
	}	

}

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.gson.Gson;

import SystemInfo.*;
import SystemLoad.*;

public class ESP32MonitorClient extends JFrame{

    private static final long serialVersionUID = 1L;
	private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientThread t;
    
    public SSLSocket getSocket() {return this.socket;}
    
    private CpuStat cpuprev;
    LineChartPanel cpuLoadPanel, memUsagePanel, swapUsagePanel;
    

    private SystemInfo sysinfo;
    private SystemLoadData data;
    private Map<String, String> procs;
    
    public void setSysInfo(SystemInfo i) { this.sysinfo = i; }
    public void setSysLoad(SystemLoadData d) { this.data = d; }
    
    private Container tabsC, controlsC;
    private JPanel systemPage, cpuPage, memoryPage, networkPage, diskPage, processPage;
    private JButton monitorStartButton, monitorStopButton, getSystemInfoButton, runButton, saveButton, exitButton, loadButton;
    private JTextField ipField;
    private String ip;
    
    
    
    public ESP32MonitorClient() {
    	super("ESP32 Monitor");
    	procs = new TreeMap<String, String>();
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.setSize(800, 500);
    	this.setLayout(new BorderLayout());
    	
    	controlsC = new JPanel();
    	controlsC.setLayout(new GridLayout(7,1,10,10));
    	this.add(controlsC, BorderLayout.WEST);
    	
    	tabsC = new JPanel(new BorderLayout());
    	this.add(tabsC, BorderLayout.CENTER);
    	this.createTabs();
    	
		cpuPage.setLayout(new BorderLayout());
		memoryPage.setLayout(new BorderLayout());
		diskPage.setLayout(new BorderLayout());
		
		cpuLoadPanel = new LineChartPanel("CPU Load", 100, 300, 200, Color.BLUE);
		memUsagePanel = new LineChartPanel("Memory Usage", 100, 200, 200, Color.BLUE);
		swapUsagePanel = new LineChartPanel("Swap Usage", 100, 200, 200, Color.GREEN);
    	
		boolean connected;
		do
		{
			getIpWindow();
			connected = this.initTLSConnection(ip, 4433);
		} while(!connected);
    	this.createControls();
    	this.setLocationRelativeTo(null);
    	this.setVisible(true);
    	
    	
    }
	public static void main(String[] args) {
		
		
		ESP32MonitorClient client = new ESP32MonitorClient();
		
		client.t = new ClientThread(client);
		client.t.start();
	}
	
	private void saveData() {
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setDialogTitle("Save Monitoring Data");
	    int userSelection = fileChooser.showSaveDialog(null);

	    if (userSelection == JFileChooser.APPROVE_OPTION) {
	        File fileToSave = fileChooser.getSelectedFile();

	        List<Integer> cpuChartData = cpuLoadPanel.getDataPoints();
	        List<Integer> memChartData = memUsagePanel.getDataPoints();
	        List<Integer> swapChartData = swapUsagePanel.getDataPoints();

	        String sysInfoJson = sysinfo.serializeJson();
	        String sysLoadJson = data.serializeJson();

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

	private void loadData() {
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

	            cpuLoadPanel.setDataPoints(cpuChartData);
	            memUsagePanel.setDataPoints(memChartData);
	            swapUsagePanel.setDataPoints(swapChartData);

	            sysinfo = new SystemInfo(sysInfoJson);
	            data = new SystemLoadData(sysLoadJson);
	            
	            displaySystemInfo();
	            displaySystemLoadData();

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
 	public BufferedReader getInput()
	{
		return this.in;
	}
	public void createTabs()
	{
		JTabbedPane tabPanel = new JTabbedPane();

        systemPage = new JPanel();
        cpuPage = new JPanel();
        memoryPage = new JPanel();
        networkPage = new JPanel();
        diskPage = new JPanel();
        processPage = new JPanel();

        tabPanel.addTab("System", systemPage);
        tabPanel.addTab("CPU", cpuPage);
        tabPanel.addTab("Memory", memoryPage);
        tabPanel.addTab("Network", networkPage);
        tabPanel.addTab("Disk", diskPage);
        tabPanel.addTab("Processes", processPage);
        
        tabsC.add(tabPanel);
		
	}
	public void createControls() {
	    controlsC.removeAll();
	    controlsC.setLayout(new BorderLayout());

	    JPanel buttonsPanel = new JPanel();
	    buttonsPanel.setLayout(new GridLayout(0, 1, 10, 10));
	    buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

	    monitorStartButton = createStyledButton("▶ Start Monitoring", new Color(0, 102, 0));
	    monitorStopButton = createStyledButton("■ Stop Monitoring", new Color(153, 0, 0));
	    getSystemInfoButton = createStyledButton("🖥 Get System Info", new Color(0, 51, 102));
	    runButton = createStyledButton("⚙ Run Command", new Color(51, 51, 51));
	    saveButton = createStyledButton("💾 Save", new Color(0, 102, 204));
	    loadButton = createStyledButton("Load", new Color(0, 102, 204));
	    exitButton = createStyledButton("⏻ Exit", new Color(0, 0, 0));

	    monitorStartButton.addActionListener(e -> startMonitor());
	    monitorStopButton.addActionListener(e -> stopMonitor());
	    getSystemInfoButton.addActionListener(e -> getSystemInfo());
	    exitButton.addActionListener(e -> {
	    	stopMonitor();
	        disconnectClient();
	        t.setState(false);
	        try {
	            t.join();
	        } catch (InterruptedException e1) {
	            e1.printStackTrace();
	            System.exit(-1);
	        }
	        System.exit(0);
	    });
	    saveButton.addActionListener(e -> saveData());
	    loadButton.addActionListener(e -> loadData());

	    buttonsPanel.add(monitorStartButton);
	    buttonsPanel.add(monitorStopButton);
	    buttonsPanel.add(getSystemInfoButton);
	    buttonsPanel.add(runButton);
	    buttonsPanel.add(saveButton);
	    buttonsPanel.add(loadButton);
	    buttonsPanel.add(exitButton);

	    controlsC.add(buttonsPanel, BorderLayout.CENTER);

	    controlsC.revalidate();
	    controlsC.repaint();
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
	public void getIpWindow() {
	    JDialog dialog = new JDialog((JFrame)null, "ESP32 Monitor", true);
	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	    dialog.setSize(300, 150);
	    dialog.setLayout(new BorderLayout(10, 10));

	    dialog.add(new JLabel("Please input the device IP address:"), BorderLayout.NORTH);

	    ipField = new JTextField();
	    ipField.setBorder(BorderFactory.createLoweredSoftBevelBorder());
	    dialog.add(ipField, BorderLayout.CENTER);

	    JButton connectButton = new JButton("Connect");
	    connectButton.addActionListener(e -> {
	        getIP();
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

	public void getIP()
	{
		this.ip = ipField.getText();
	}
	public boolean initTLSConnection(String ip, int port) {
	    String truststorePath = "esp32-truststore.jks";
	    String truststorePassword = "changeit";
	    int timeoutMillis = 10_000;

	    try (InputStream trustStoreStream = new FileInputStream(truststorePath)) {

	        KeyStore trustStore = KeyStore.getInstance("JKS");
	        trustStore.load(trustStoreStream, truststorePassword.toCharArray());

	        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        tmf.init(trustStore);

	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

	        SSLSocketFactory factory = sslContext.getSocketFactory();
	        
	        Socket plainSocket = new Socket();
	        plainSocket.connect(new InetSocketAddress(ip, port), timeoutMillis);

	        socket = (SSLSocket) factory.createSocket(plainSocket, ip, port, true);
	        socket.startHandshake();

	        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        JOptionPane.showMessageDialog(null, "TLS connection established successfully!");
	        return true;

	    } catch (FileNotFoundException e) {
	        JOptionPane.showMessageDialog(null, "Truststore file not found: " + truststorePath, "Error", JOptionPane.ERROR_MESSAGE);
	    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
	        JOptionPane.showMessageDialog(null, "Error loading or initializing the truststore:", "Error", JOptionPane.ERROR_MESSAGE);
	    } catch (KeyManagementException e) {
	        JOptionPane.showMessageDialog(null, "Error initializing SSL context:", "Error", JOptionPane.ERROR_MESSAGE);
	    } catch (SSLHandshakeException e) {
	        JOptionPane.showMessageDialog(null, "TLS handshake failed. Certificate might not be trusted:", "Error", JOptionPane.ERROR_MESSAGE);
	    } catch (SocketTimeoutException e) {
	        JOptionPane.showMessageDialog(null, "Connection timed out after " + timeoutMillis + " ms", "Error", JOptionPane.ERROR_MESSAGE);
	    } catch (Exception e) {
	        JOptionPane.showMessageDialog(null, "Unexpected error during TLS connection:", "Error", JOptionPane.ERROR_MESSAGE);
	    }

	    return false;
	}
	public void connectionUnavailable()
	{
		JOptionPane.showMessageDialog(null, "Connection with device has been lost", "Error", JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}
	public void connTimeout()
	{
		JOptionPane.showMessageDialog(null, "Connection timed out", "Error", JOptionPane.ERROR_MESSAGE);
		System.exit(-1);
	}
	public void showDevMessage(String msg)
	{
		JOptionPane.showMessageDialog(null, "Message from ESP32: " + msg);
	}

	public void displaySystemInfo() {
	    systemPage.removeAll();
	    systemPage.setLayout(new BorderLayout());

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

	    systemPage.add(scrollPane, BorderLayout.CENTER);
	    systemPage.revalidate();
	    systemPage.repaint();
	}
	public void displaySystemLoadData()
	{
		cpuPage.removeAll();
		memoryPage.removeAll();
		networkPage.removeAll();
		diskPage.removeAll();
		processPage.removeAll();
		
		displayCPULoad(data.getCpuStat());
		displayMemoryLoad(data.getMemory());
		displayNetworkLoad(data.getNetworkInterfaces());
		displayDiskUsage(data.getDiskUsage());
		displayProcesses(data.getProcesses());
		
		
	}
	public void displayCPULoad(CpuStat cpu) {
		int cpuLoad = 0;
	    if (cpuprev != null) {
	        cpuLoad = (int)CpuStat.computeCpuUsage(cpuprev, cpu);
	        cpuLoadPanel.addDataPoint(cpuLoad);
	        System.out.println(cpuLoad);
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
	    
	    cpuPage.add(cpuLoadPanel);
	    

	    cpuPage.add(cpuPanel, BorderLayout.SOUTH);
	    cpuPage.revalidate();
	    cpuPage.repaint();
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
		memoryPage.add(chartPanel);
		memoryPage.add(memPanel, BorderLayout.SOUTH);
		memoryPage.revalidate();
		memoryPage.repaint();
	}
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
		networkPage.add(netPanel, BorderLayout.CENTER);
		networkPage.revalidate();
		networkPage.repaint();
	}
	public void displayDiskUsage(List<DiskUsage> disks)
	{
		JPanel disksPanel = new JPanel(new GridLayout(0,2));
		disksPanel.setBorder(new TitledBorder("Disks"));
		int i = 0;
		for(DiskUsage disk : disks)
		{
			JPanel diskPanel = new JPanel(new GridLayout(0,1));
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
		diskPage.add(disksPanel, BorderLayout.SOUTH);
		diskPage.revalidate();
		diskPage.repaint();
	}
	public void displayProcesses(Map<String, String> processes) {
		if (!processes.equals(procs)) {
			procs = processes;
			processPage.removeAll();
			processPage.setLayout(new BorderLayout());

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
								getProcInfo(pid); // request server to send detailed info
							} catch (NumberFormatException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			});

			JScrollPane scrollPane = new JScrollPane(processList);
			scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

			processPage.add(scrollPane, BorderLayout.CENTER);
			processPage.revalidate();
			processPage.repaint();
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
		b.addActionListener(e -> killProc(info.getPid()));
		JButton ok = new JButton("OK");
		ok.addActionListener(e -> frame.dispose());
		controls.add(b);
		controls.add(ok);
		frame.add(controls, BorderLayout.SOUTH);
		frame.setLocationRelativeTo(this);
		frame.setVisible(true);
	}
	public void startMonitor()
	{
		out.println("/start-monitor");
	}
	public void stopMonitor()
	{
		out.println("/stop-monitor");
	}
	public void getSystemInfo()
	{
		out.println("/get-sys-info");
	}
	public void disconnectClient()
	{
		out.println("/disconnect");
	}
	public void killProc(int pid)
	{
		out.println("/kill-proc " + pid);
	}
	public void getProcInfo(int pid)
	{
		out.println("/get-proc-info " + pid);
	}
}

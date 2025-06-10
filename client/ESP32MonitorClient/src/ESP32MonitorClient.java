import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.gson.Gson;

import SystemInfo.*;

public class ESP32MonitorClient extends JFrame{

    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientThread t;
    
    private Container tabsC, controlsC, dataC;
    private JPanel systemPage, cpuPage, memoryPage, networkPage, diskPage, processPage;
    private SystemInfo info;
    
    public ESP32MonitorClient(String ip, int port) {
    	super("ESP32 Monitor");
    	
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.setSize(800, 500);
    	this.setLayout(new BorderLayout());
    	
    	controlsC = new JPanel();
    	controlsC.setLayout(new GridLayout(7,1,10,10));
    	this.add(controlsC, BorderLayout.WEST);
    	
    	tabsC = new JPanel(new BorderLayout());
    	this.add(tabsC, BorderLayout.CENTER);
    	this.createTabs();
    	
    	//tabsC = new JPanel();
    	
    	this.initTLSConnection(ip, port);
    	this.createControls();
    	this.setVisible(true);
    	
    	
    }

	public static void main(String[] args) {
		ESP32MonitorClient client = new ESP32MonitorClient("192.168.178.74", 4433);
		
		client.t = new ClientThread(client);
		client.t.start();
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

	    JButton monitorStartButton = createStyledButton("▶ Start Monitoring", new Color(0, 102, 0));
	    JButton monitorStopButton = createStyledButton("■ Stop Monitoring", new Color(153, 0, 0));
	    JButton getSystemInfoButton = createStyledButton("🖥 Get System Info", new Color(0, 51, 102));
	    JButton killProcButton = createStyledButton("✖ Kill Process", new Color(102, 0, 0));
	    JButton runButton = createStyledButton("⚙ Run Command", new Color(51, 51, 51));
	    JButton saveButton = createStyledButton("💾 Save", new Color(0, 102, 204));
	    JButton exitButton = createStyledButton("⏻ Exit", new Color(0, 0, 0));

	    monitorStartButton.addActionListener(e -> startMonitor());
	    monitorStopButton.addActionListener(e -> stopMonitor());
	    getSystemInfoButton.addActionListener(e -> getSystemInfo());
	    exitButton.addActionListener(e -> {
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

	    buttonsPanel.add(monitorStartButton);
	    buttonsPanel.add(monitorStopButton);
	    buttonsPanel.add(getSystemInfoButton);
	    buttonsPanel.add(killProcButton);
	    buttonsPanel.add(runButton);
	    buttonsPanel.add(saveButton);
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
	
	public void initTLSConnection(String ip, int port) {
	    String truststorePath = "esp32-truststore.jks";
	    String truststorePassword = "changeit";

	    try (InputStream trustStoreStream = new FileInputStream(truststorePath)) {

	        KeyStore trustStore = KeyStore.getInstance("JKS");
	        trustStore.load(trustStoreStream, truststorePassword.toCharArray());

	        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
	        tmf.init(trustStore);

	        SSLContext sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(null, tmf.getTrustManagers(), new SecureRandom());

	        SSLSocketFactory factory = sslContext.getSocketFactory();

	        socket = (SSLSocket) factory.createSocket(ip, port);
	        socket.startHandshake();

	        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
	        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

	        System.out.println("TLS connection established successfully.");

	    } catch (FileNotFoundException e) {
	        System.err.println("Truststore file not found: " + truststorePath);
	        e.printStackTrace();
	    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
	        System.err.println("Error loading or initializing the truststore:");
	        e.printStackTrace();
	    } catch (KeyManagementException e) {
	        System.err.println("Error initializing SSL context:");
	        e.printStackTrace();
	    } catch (SSLHandshakeException e) {
	        System.err.println("TLS handshake failed. Certificate might not be trusted:");
	        e.printStackTrace();
	    } catch (Exception e) {
	        System.err.println("Unexpected error during TLS connection:");
	        e.printStackTrace();
	    }
	}
	
	public void displaySystemInfo(SystemInfo info) {
	    systemPage.removeAll();
	    systemPage.setLayout(new BorderLayout());

	    JPanel contentPanel = new JPanel();
	    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

	    JPanel kernelPanel = new JPanel(new GridLayout(0, 1));
	    kernelPanel.setBorder(new TitledBorder("Kernel Information"));
	    kernelPanel.add(new JLabel("Version: " + info.getKernel().getVersion()));
	    kernelPanel.add(new JLabel("Architecture: " + info.getKernel().getArchitecture()));
	    contentPanel.add(kernelPanel);

	    JPanel osPanel = new JPanel(new GridLayout(0, 1));
	    osPanel.setBorder(new TitledBorder("Operating System"));
	    OS os = info.getOs();
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

	    List<Processor> processors = info.getProcessors();
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

import java.awt.BorderLayout;
import java.awt.Container;
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
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.google.gson.Gson;

public class ESP32MonitorClient extends JFrame{

    private SSLSocket socket;
    private BufferedReader in;
    private PrintWriter out;
    private ClientThread t;
    
    private Container controlsC, dataC;
    
    public ESP32MonitorClient(String ip, int port) {
    	super("ESP32 Monitor");
    	
    	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	this.setSize(500, 300);
    	this.setLayout(new BorderLayout());
    	
    	controlsC = new JPanel();
    	controlsC.setLayout(new GridLayout(7,1,10,10));
    	this.add(controlsC, BorderLayout.WEST);
    	
    	this.initTLSConnection(ip, port);
    	this.createControls();
    	this.setVisible(true);
    	
    	
    }

	public static void main(String[] args) {
		ESP32MonitorClient client = new ESP32MonitorClient("192.168.178.74", 4433);
		
		client.t = new ClientThread(client.in, client.out);
		client.t.start();
	}
	
	public void createControls()
	{
		JButton monitorStartButton = new JButton("Start Monitoring");
		JButton monitorStopButton = new JButton("Stop Monitoring");
		JButton getSystemInfoButton = new JButton("Get system info");
		JButton killProcButton = new JButton("Kill process");
		JButton runButton = new JButton("Run command");
		JButton saveButton = new JButton("Save");
		JButton exitButton = new JButton("Exit");
		
		monitorStartButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				startMonitor();
			}
		});
		monitorStopButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				stopMonitor();
			}
		});
		getSystemInfoButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				getSystemInfo();
			}
		});
		exitButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				disconnectClient();
				t.setState(false);
				try {
					t.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.exit(-1);
				}
				System.exit(0);
				//stop thread, call exit
			}
		});
		
		controlsC.add(monitorStartButton);
		controlsC.add(monitorStopButton);
		controlsC.add(getSystemInfoButton);
		controlsC.add(killProcButton);
		controlsC.add(runButton);
		controlsC.add(saveButton);
		controlsC.add(exitButton);
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

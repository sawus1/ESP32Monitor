package net;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ConnectionManager {
	
	private SSLSocket socket;
	private JTextField ipField;
	private BufferedReader in;
	private CommandHandler h;
	 private DataReceiverThread t;
    private PrintWriter out;
	private String ip;
	
	public ConnectionManager()
	{
		while(!initTLSConnection());
		h = new CommandHandler(this);
	}
	
	public DataReceiverThread getThread() {return this.t; }

	private void getIpWindow() {
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
	
	private void getIP()
	{
		this.ip = ipField.getText();
	}
	
	private boolean initTLSConnection() {
		getIpWindow();
		getIP();
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
	        plainSocket.connect(new InetSocketAddress(ip, 4433), timeoutMillis);

	        socket = (SSLSocket) factory.createSocket(plainSocket, ip, 4433, true);
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
	
	public SSLSocket getSocket() {return socket;}
	public BufferedReader getInput() {return in;}
	public PrintWriter getOutput() {return out;}
}

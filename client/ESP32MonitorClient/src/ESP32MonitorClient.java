import javax.net.ssl.*;
import javax.swing.JFrame;

import com.google.gson.Gson;

import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class ESP32MonitorClient extends JFrame{

    private static final long serialVersionUID = 1L;
    private static SSLSocket socket;
    private static BufferedReader in;
    private static PrintWriter out;

	public static void main(String[] args) throws Exception {
        // Disable certificate validation (INSECURE)
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                public X509Certificate[] getAcceptedIssuers() { return null; }
            }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        SSLSocketFactory factory = sc.getSocketFactory();

        try (SSLSocket s = (SSLSocket) factory.createSocket("192.168.178.74", 4433)) {
        	socket = s;
            socket.startHandshake();
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("/get-sys-info");
            out.flush();
            
            // Read response
            String line;
            while ((line = in.readLine()).trim() != null) {
            	System.out.println(line);
            	if (line.startsWith("\u0000")) {
            		line = line.substring(1);
            	}
            	if (line.contains("system_load_data")) {
            	    Gson gson = new Gson();
            	    SystemLoadData data = gson.fromJson(line, SystemLoadData.class);
            	}
            	else if(line.contains("system_info"))
            	{
            		Gson gson = new Gson();
            		SystemInfo info = gson.fromJson(line, SystemInfo.class);
            		
            		System.out.println("Kernel: " + (info.getKernel().getVersion()));
            		System.out.println("Processor Model: " + (info.getProcessors().get(0).getModel_name()));
            	}
           
                if (line.isEmpty()) break;
            }
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
        finally
        {
        	stopMonitor();
        }
    }
	public void startMonitor()
	{
		out.println("/start-monitor");
	}
	public static void stopMonitor()
	{
		out.println("/stop-monitor");
	}
}

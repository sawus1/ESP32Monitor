import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class ESP32MonitorClient {

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

        try (SSLSocket socket = (SSLSocket) factory.createSocket("192.168.178.74", 4433)) {
            socket.startHandshake();
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println("/start-monitor");
            out.flush();

            // Read response
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.isEmpty()) break;
            }
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
        }
    }
}

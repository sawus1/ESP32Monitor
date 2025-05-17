import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ESP32MonitorClient
{
	//private Socket so;
	private static String host = null;
	private static int port;
	public static GUI window;
	//private BufferedReader in = null;
	private static BufferedReader inSo;
	private static PrintWriter outSo;
	public static void main(String[] args)
	{
		Socket so = null;
		host = args[0];
		port = Integer.valueOf(args[1]).intValue();
		
		try 
		{
			so = new Socket(host, port);
		}
		catch(IOException e) {
			System.out.println(e);
			System.exit(-1);
		}
		
		try
		{
			BufferedReader in = new  BufferedReader(
					new InputStreamReader(System.in));
			inSo = new BufferedReader(
					new InputStreamReader(so.getInputStream()));
		outSo = new PrintWriter(so.getOutputStream(), true);
		System.out.println("> Input:");
		String input;
		window = new GUI();
		while((input = in.readLine()) != null && !input.equals("quit")) {
			outSo.println(input);
			System.out.println("> Answer from Server: ");
			String line;
			while ((line = inSo.readLine()) != null) {
			    if (line.equals("")) break;
			    System.out.println(line);
			}
			System.out.println("> Input: ");
		}
		while(!input.equals("quit")) {}
		in.close();
		so.close();
					
		}
		catch(IOException e)
		{
			System.out.println(e);
			System.exit(-1);
		}
	}
	
	public static void sendGETRequest(String uri) {
	    try (Socket socket = new Socket(host, port);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

	        StringBuilder request = new StringBuilder("GET " + uri + " HTTP/1.1\n");
	        request.append("Host: ").append(host).append("\n");
	        request.append("Connection: close\n\n"); // Explicitly ask to close

	        out.println(request.toString());

	        String line;
	        while ((line = in.readLine()) != null) {
	            System.out.println(line);
	        }

	    } catch (IOException e) {
	        System.out.println(e.getMessage());
	    }
	}
	
	public static void getSysData()
	{
		try {
			sendGETRequest("/systemdata");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setON()
	{
		try {
			sendGETRequest("/H");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setOFF()
	{
		try {
			sendGETRequest("/L");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
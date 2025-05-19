import java.io.BufferedReader;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ESP32MonitorClient
{
	//private Socket so;
	private static String host = null;
	private static int portHttp;
	private static int portSo;
	public static GUI window;
	//private BufferedReader in = null;
	private static BufferedReader inSo;
	private static PrintWriter outSo;
	public static void main(String[] args)
	{
		Socket so = null;
		host = args[0];
		portSo = 5000;
		portHttp = 80;
		try 
		{
			so = new Socket(host, portHttp);
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
	
	public static String sendGETRequest(String uri) {
	    try (Socket socket = new Socket(host, portHttp);
	         PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

	        StringBuilder request = new StringBuilder("GET " + uri + " HTTP/1.1\n");
	        request.append("Host: ").append(host).append("\n");
	        request.append("Connection: close\n\n"); // Explicitly ask to close

	        out.println(request.toString());

	        String line;
	        StringBuilder response = new StringBuilder("");
	        while ((line = in.readLine()) != null) {
	            System.out.println(line);
	            response.append(line);
	        }
	        return response.toString();
	        
	    } catch (IOException e) {
	        System.out.println(e.getMessage());
	    }
	    return null;
	}
	
	public static void getSysData()
	{
		try {
			String response = sendGETRequest("/systemdata");
			String responseBody = "";
			if(response.contains("200 OK"))
			{
				responseBody = response.substring(response.indexOf('{'));
			}	
			SystemInfo info = new SystemInfo(responseBody);
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
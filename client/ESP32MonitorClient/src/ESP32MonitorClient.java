import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ESP32MonitorClient
{
	public static void main(String[] args)
	{
		Socket so = null;
		String host = args[0];
		int port = Integer.valueOf(args[1]).intValue();
		
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
			BufferedReader inSo = new BufferedReader(
					new InputStreamReader(so.getInputStream()));
		PrintWriter outSo = new PrintWriter(so.getOutputStream(), true);
		System.out.println("> Input:");
		String input;
		while((input = in.readLine()) != null && !input.equals("quit")) {
			outSo.println(input);
			//System.out.println("> Answer from Server: ");
			//System.out.println(inSo.readLine());
			System.out.println("> Input: ");
		}
		in.close();
		so.close();
					
		}
		catch(IOException e)
		{
			System.out.println(e);
			System.exit(-1);
		}
	}
}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class ClientThread extends Thread {
	
	private BufferedReader in;
	private PrintWriter out;
	private boolean state;
	
	public ClientThread(BufferedReader in, PrintWriter out)
	{
		this.in = in;
		this.out = out;
		state = true;
	}
	
	@Override
	public void run() {
	    try {

	        while (state) {
	            String line = in.readLine();
	            System.out.println(line);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void setState(boolean val)
	{
		state = val;
	}
}

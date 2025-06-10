import java.io.BufferedReader;
import java.io.IOException;

import com.google.gson.Gson;

public class ClientThread extends Thread {
	
	private BufferedReader in;
	private boolean state;
	private ESP32MonitorClient client;
	
	public ClientThread(ESP32MonitorClient client)
	{
		this.in = client.getInput();
		this.client = client;
		state = true;
	}
	
	@Override
	public void run() {
	    try {

	        while (state) {
	            String line = in.readLine().trim();
	            System.out.println(line);
	            if (line.startsWith("\u0000")) {
            		line = line.substring(1);
            	}
            	if (line.contains("system_info")) {
            	    this.processSystemInfo(line);
            	}
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void setState(boolean val)
	{
		state = val;
	}
	
	public void processSystemInfo(String line)
	{
		Gson gson = new Gson();
		SystemInfo info = gson.fromJson(line, SystemInfo.class);
		client.displaySystemInfo(info);
	}
}

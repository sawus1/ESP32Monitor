import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

import com.google.gson.Gson;

import SystemLoad.ProcessInfo;

public class ClientThread extends Thread {
	
	private BufferedReader in;
	private boolean state;
	private ESP32MonitorClient client;
	private SSLSocket s;
	
	public ClientThread(ESP32MonitorClient client)
	{
		this.s = client.getSocket();
		this.in = client.getInput();
		this.client = client;
		state = true;
	}
	
	@Override
	public void run() {
	    while (state) {
		    try {
		        if (!s.isClosed() && s.isConnected() && !s.isInputShutdown()) {
		            String line = in.readLine();

		            if (line == null) {
		                client.connectionUnavailable();
		                break;
		            }

		            line = line.trim();

		            System.out.println(line);

		            if (line.startsWith("\u0000")) {
		                line = line.substring(1);
		            }

		            if (line.contains("system_info")) {
		                this.processSystemInfo(line);
		            } else if (line.contains("system_load_data")) {
		                this.processSystemLoad(line);
		            } else if (line.contains("process_info")) {
		                this.processProcInfo(line);
		            } else if (line.contains("device_message")) {
		            	this.processMessage(line);
		            } else if (line.contains("script_execution")) {
		            	this.processScriptExec(line);
		            }
		        } else {
		            client.connectionUnavailable();
		            break;
		        }
		    } catch (SocketTimeoutException e) {
		        System.out.println("Connection timeout");
		        client.connTimeout();
		        break;
		    } catch (IOException e) {
		        System.out.println("IOException during read: " + e.getMessage());
		        client.connectionUnavailable();
		        break;
		    }
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
		client.setSysInfo(info);
		client.displaySystemInfo();
	}
	public void processSystemLoad(String line)
	{
		Gson gson = new Gson();
		SystemLoadData data = gson.fromJson(line, SystemLoadData.class);
		client.setSysLoad(data);
		client.displaySystemLoadData();
	}
	public void processProcInfo(String line)
	{
		Gson gson = new Gson();
		ProcessInfo info = gson.fromJson(line, ProcessInfo.class);
		client.displayProcessInfo(info.getProc());
	}
	public void processMessage(String line)
	{
		String msg = line.substring(line.indexOf("message\" : \"") + 12, line.lastIndexOf("\""));
		client.showDevMessage(msg);
	}
	public void processScriptExec(String line)
	{
		String res = line.substring(line.indexOf("result\" : \"") + 11, line.lastIndexOf("\""));
		client.showDevMessage(res);
	}
}

package net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLSocket;

import com.google.gson.Gson;

import SystemInfo.SystemInfo;
import SystemLoad.ProcessInfo;
import SystemLoad.SystemLoadData;
import common.ESP32Message;
import ui.MainClientFrame;

public class DataReceiverThread extends Thread{
	private BufferedReader in;
	private boolean state;
	private ConnectionManager conn;
	private SSLSocket s;
	private MainClientFrame frame;
	
	public DataReceiverThread(ConnectionManager conn, MainClientFrame frame)
	{
		this.s = conn.getSocket();
		this.in = conn.getInput();
		this.conn = conn;
		state = true;
		this.frame = frame;
	}
	
	public ConnectionManager getConnection() {return conn;}
	
	@Override
	public void run() {
	    while (state) {
		    try {
		        if (!s.isClosed() && s.isConnected() && !s.isInputShutdown()) {
		            String line = in.readLine();

		            if (line == null) {
		            	conn.connectionUnavailable();
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
		        	conn.connectionUnavailable();
		            break;
		        }
		    } catch (SocketTimeoutException e) {
		        System.out.println("Connection timeout");
		        conn.connTimeout();
		        break;
		    } catch (IOException e) {
		        System.out.println("IOException during read: " + e.getMessage());
		        conn.connectionUnavailable();
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
		frame.setSystemInfo(info);
		frame.displaySystemInfo();
	}
	public void processSystemLoad(String line)
	{
		Gson gson = new Gson();
		SystemLoadData data = gson.fromJson(line, SystemLoadData.class);
		frame.setSystemLoad(data);
		frame.displaySystemLoadData();
		frame.checkForAnomalies();
	}
	public void processProcInfo(String line)
	{
		Gson gson = new Gson();
		ProcessInfo info = gson.fromJson(line, ProcessInfo.class);
		frame.displayProcessInfo(info.getProc());
	}
	public void processMessage(String line)
	{
		Gson gson = new Gson();
		ESP32Message msg = gson.fromJson(line, ESP32Message.class);
		frame.showDevMessage(msg.getMessage());
	}
	public void processScriptExec(String line)
	{
		Gson gson = new Gson();
		ESP32Message msg = gson.fromJson(line, ESP32Message.class);
		frame.showDevMessage(msg.getResult());
	}
}

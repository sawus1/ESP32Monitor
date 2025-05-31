import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StopMonitorAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		ESP32MonitorClient.stopMonitor();
	}

}

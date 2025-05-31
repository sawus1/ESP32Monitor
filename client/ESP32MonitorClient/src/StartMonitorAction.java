import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartMonitorAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		ESP32MonitorClient.startMonitor();

	}

}

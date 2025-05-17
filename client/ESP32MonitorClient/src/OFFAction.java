import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OFFAction implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent e) {
		ESP32MonitorClient.window.setLabelString("State: OFF");
		ESP32MonitorClient.setOFF();
	}

}

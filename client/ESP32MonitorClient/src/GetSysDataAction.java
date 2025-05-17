import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GetSysDataAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
		ESP32MonitorClient.getSysData();

	}

}

package fiatlux.os.localization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class X11LinuxLocalizer extends Localizer {

	public boolean inZone() {
		ArrayList<String> BSSIDs = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec("gksudo iwlist scan");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String outLine = "";
			while ((outLine = reader.readLine()) != null) {
				if (outLine.contains("Address")) {
					int colonIndex = outLine.indexOf(':');
					String BSSID = outLine.substring(colonIndex + 2);
					BSSIDs.add(BSSID);
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (BSSIDs.isEmpty()) {
			return this.checkIP();
		}
		return bssidProcessor.deepContains(BSSIDs);
	}
}
package fiatlux.os.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class WindowsWifiDetector implements WifiDetector {
	
	public ArrayList<String> getBSSIDs() {
		ArrayList<String> BSSIDList = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec(
					"netsh wlan show networks mode=Bssid");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String outLine = "";
			while ((outLine = reader.readLine()) != null) {
				if (outLine.contains("BSSID")) {
					int colonIndex = outLine.indexOf(':');
					String BSSID = outLine.substring(colonIndex + 2);
					BSSIDList.add(BSSID);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return BSSIDList;
	}
}
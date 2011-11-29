package fiatlux.os.wifi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class X11LinuxWifiDetector implements WifiDetector {

	public ArrayList<String> getBSSIDs() {
		ArrayList<String> BSSIDList = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec("iwlist");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String outLine = "";
			while ((outLine = reader.readLine()) != null) {
				// process BSSIDs out and add them to BSSIDList
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return BSSIDList;
	}
}
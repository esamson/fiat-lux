package fiatlux.os.localization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MacOSXLocalizer extends Localizer {

	public boolean inZone() {
		ArrayList<String> BSSIDs = new ArrayList<String>();
		try {
			Process p = Runtime.getRuntime().exec(
					"/System/Library/PrivateFrameworks/Apple80211.framework/Versions/Current"
							+ "/Resources/airport -I");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String outLine = "";
			while ((outLine = reader.readLine()) != null) {
				if (outLine.contains("BSSID")) {
					int colonIndex = outLine.indexOf(':');
					String BSSID = outLine.substring(colonIndex + 2);
					BSSIDs.add(BSSID);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return processor.deepContains(BSSIDs);
	}
}
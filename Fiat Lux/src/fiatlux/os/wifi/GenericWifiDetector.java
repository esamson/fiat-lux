package fiatlux.os.wifi;

import java.util.ArrayList;

public class GenericWifiDetector implements WifiDetector {

	public ArrayList<String> getBSSIDs() {
		ArrayList<String> BSSIDList = new ArrayList<String>();
		BSSIDList.add("PASS");
		return BSSIDList;
	}

}

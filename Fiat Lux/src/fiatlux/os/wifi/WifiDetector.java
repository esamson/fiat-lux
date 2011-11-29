package fiatlux.os.wifi;

import java.util.ArrayList;

public interface WifiDetector {
	/**
	 * Get a list of detected BSSIDs.
	 * 
	 * @return An ArrayList of Strings corresponding to the detected BSSIDs.
	 */
	public ArrayList<String> getBSSIDs();
}

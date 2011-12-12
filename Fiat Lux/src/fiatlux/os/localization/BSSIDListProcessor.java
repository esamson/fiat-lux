package fiatlux.os.localization;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class BSSIDListProcessor {
	public BSSIDListProcessor() {
		BSSIDList = new ArrayList<String>();
		BSSIDList.add("PASS");

		try {
			// add floor 4 BSSIDs
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					this.getClass()
							.getResource("/resources/bssids/floor4bssids.txt")
							.openStream()));
			String BSSID = "";
			while ((BSSID = reader.readLine()) != null) {
				BSSIDList.add(BSSID);
			}

			// add floor 6 BSSIDs
			reader = new BufferedReader(new InputStreamReader(this.getClass()
					.getResource("/resources/bssids/floor6bssids.txt")
					.openStream()));
			while ((BSSID = reader.readLine()) != null) {
				BSSIDList.add(BSSID);
			}

			// add floor 7 BSSIDs
			reader = new BufferedReader(new InputStreamReader(this.getClass()
					.getResource("/resources/bssids/floor7bssids.txt")
					.openStream()));
			while ((BSSID = reader.readLine()) != null) {
				BSSIDList.add(BSSID);
			}

			// add random extra BSSIDs
			BSSIDList.add("a0:21:b7:b4:e3:4a");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Checks to see if any of the wifi BSSIDs in range are on the list of
	 * approved BSSIDs.
	 * 
	 * @param BSSIDs
	 *            A list of BSSIDs to check.
	 * @return Whether or not any of the BSSIDs on the input list are on the
	 *         whitelist.
	 */
	public boolean deepContains(ArrayList<String> BSSIDs) {
		for (String s : BSSIDs) {
			if (BSSIDList.contains(s)) {
				return true;
			}
		}
		return false;
	}

	public static ArrayList<String> BSSIDList;
}

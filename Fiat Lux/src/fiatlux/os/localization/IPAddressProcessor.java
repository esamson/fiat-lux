package fiatlux.os.localization;

import java.util.ArrayList;
import java.util.Collections;

public class IPAddressProcessor {
	public IPAddressProcessor() {
		IPList = new ArrayList<String>();
		for (int i = 1; i < 255; i++) {
			IPList.add("128.32.156." + i);
		}
		for (int i = 1; i < 255; i++) {
			IPList.add("128.32.247." + i);
		}
		for (int i = 1; i < 255; i++) {
			IPList.add("169.229.222." + i);
			IPList.add("169.229.223." + i);
		}
		Collections.sort(IPList);
	}

	/**
	 * Checks to see if the given IP address is on the approved list.
	 * 
	 * @param IP
	 *            The IP address to check.
	 * @return Whether or not any of the IP is approved.
	 */
	public boolean contains(String IP) {
		return IPList.contains(IP);
	}

	public ArrayList<String> IPList;
}
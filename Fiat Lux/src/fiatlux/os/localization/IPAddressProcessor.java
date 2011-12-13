package fiatlux.os.localization;

import java.util.ArrayList;

public class IPAddressProcessor {
	public IPAddressProcessor() {
		IPList = new ArrayList<String>();
		for (int i = 0; i < 256; i++) {
			IPList.add("128.32.156." + i);
		}
		for (int i = 0; i < 256; i++) {
			IPList.add("128.32.247." + i);
		}
		for (int i = 0; i < 512; i++) {
			IPList.add("169.229.222." + i);
		}
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
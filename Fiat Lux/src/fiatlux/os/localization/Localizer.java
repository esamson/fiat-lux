package fiatlux.os.localization;

import java.net.InetAddress;
import java.net.UnknownHostException;

public abstract class Localizer {
	public Localizer() {
		this.bssidProcessor = new BSSIDProcessor();
		this.ipProcessor = new IPAddressProcessor();
	}

	/**
	 * Determines whether or not an approved BSSID is visible.
	 * 
	 * @return Whether or not a BSSID on the whitelist is in range.
	 */
	public abstract boolean inZone();

	/**
	 * Determines whether or not the current IP is approved.
	 * 
	 * @return Whether or not the current IP is approved.
	 */
	public boolean checkIP() {
		try {
			String ip = InetAddress.getLocalHost().getHostAddress();
			return ipProcessor.contains(ip);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return false;
	}

	protected BSSIDProcessor bssidProcessor;
	private IPAddressProcessor ipProcessor;
}

package fiatlux.os.localization;

public abstract class Localizer {
	public Localizer() {
		this.processor = new BSSIDListProcessor();
	}

	/**
	 * Get a list of detected BSSIDs.
	 * 
	 * @return An ArrayList of Strings corresponding to the detected BSSIDs.
	 */
	public abstract boolean inZone();

	protected BSSIDListProcessor processor;
}

package fiatlux.os.localization;

import java.util.ArrayList;

public class GenericLocalizer extends Localizer {

	public boolean inZone() {
		ArrayList<String> BSSIDs = new ArrayList<String>();
		BSSIDs.add("PASS");
		return processor.deepContains(BSSIDs);
	}

}

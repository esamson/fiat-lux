package fiatlux.launcher;

import java.io.File;
import java.io.IOException;

public class Launcher {
	public static void main(String[] args) {
		try {
			System.out.println(System.getProperty("java.version"));
			int version = System.getProperty("java.version").charAt(2);
			System.out.println(version);

			// if java is version 5 or higher, run standard version
			if (version > 5) {
				Runtime.getRuntime().exec(
						"java -jar Standard" + File.separator + "fiatlux.jar");
			}

			// run legacy mode
			else {
				Runtime.getRuntime().exec(
						"java -jar Legacy" + File.separator
								+ "fiatluxlegacy.jar");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

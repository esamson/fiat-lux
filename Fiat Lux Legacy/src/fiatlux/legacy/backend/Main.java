package fiatlux.legacy.backend;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiatlux.legacy.frontend.LegacyFrontend;

public class Main {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		LegacyBackend back = new LegacyBackend();
		back.init(new LegacyFrontend());
	}
}

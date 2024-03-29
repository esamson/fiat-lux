package fiatlux.backend;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fiatlux.frontend.Frontend;

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
		Backend back = new Backend();
		back.init(new Frontend());
	}
}

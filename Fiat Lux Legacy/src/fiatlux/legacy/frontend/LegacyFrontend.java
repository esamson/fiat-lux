package fiatlux.legacy.frontend;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import fiatlux.frontend.Frontend;
import fiatlux.legacy.backend.*;
import fiatlux.os.*;

public class LegacyFrontend implements ActionListener, ItemListener {

	// constructor
	public LegacyFrontend(LegacyBackend back) {
		this.back = back;
	}

	// initialize stuff and start extend loop
	public void init() {
		// get OS
		this.setOS();

		// set up window
		frame = new ImageFrame("Fiat Lux (Legacy Version)");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);

		// set icon
		try {
			switch (status) {
			case Frontend.STATUS_OK:
				frame.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconok.png")));
				break;
			case Frontend.STATUS_STANDBY:
				frame.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconstandby.png")));
				break;
			case Frontend.STATUS_ERROR:
				frame.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconerror.png")));
				break;
			default:
				frame.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbaricon.png")));
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");

		// set up menu
		JMenuItem login = new JMenuItem("Login");
		login.addActionListener(this);
		JMenuItem settings = new JMenuItem("Settings");
		settings.addActionListener(this);
		JMenuItem extend = new JMenuItem("Extend");
		extend.addActionListener(this);
		JCheckBoxMenuItem standbyItem = new JCheckBoxMenuItem("Standby");
		standbyItem.setState(false);
		standbyItem.addItemListener(this);
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(this);

		menu.add(login);
		menu.add(settings);
		menu.add(extend);
		menu.add(standbyItem);
		menu.addSeparator();
		menu.add(exit);

		menuBar.add(menu);

		frame.add(menuBar);
		frame.setJMenuBar(menuBar);
		frame.setSize(400, 300);

		frame.setLocationRelativeTo(null);

		SystemCallHandler sys = null;

		// create call handler
		switch (this.os) {
		case (LegacyFrontend.WINDOWS):
			sys = new WindowsCallHandler();
			break;
		case (LegacyFrontend.MAC):
			sys = new OSXCallHandler();
			break;
		case (LegacyFrontend.OTHER_OS):
			sys = new GenericCallHandler();
			break;
		}

		frame.setVisible(true);

		// set the status and welcome the user
		this.setStatus(LegacyFrontend.STANDBY_NOINFO);

		// ask for login info
		back.startLogin();
		back.load();

		// extend the light timer every amount of time
		while (true) {
			if (!standby) {
				back.extend(true);
			}
			try {
				int toMinute = 60000;
				Thread.sleep(10 * toMinute);

				long idleTime = sys.getSystemIdleTime();

				long currTime = System.currentTimeMillis();
				long interval = currTime - timestamp;
				if (interval > 11 * toMinute) {
					// implement location behavior
				}

				if (idleTime > 9 * toMinute) {
					// implement idle behavior
				}
				this.timestamp = currTime;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// let the user set up their login info
	public LinkedList<String> loginDialogue() {
		LegacyLoginDialogue login = new LegacyLoginDialogue(this);
		return login.getLoginInfo();
	}

	// let the user set up settings
	public LinkedList<String> settingsDialogue(int floor, int zone) {
		LegacySettingsDialogue settings = new LegacySettingsDialogue(floor,
				zone, this.back, this);
		return settings.getSettingsInfo();
	}

	// set the visible status of the program
	public void setStatus(String status) {
		this.statusString = status;
		Image image = null;
		if (status.equals(STANDBY_MANUAL) || status.equals(STANDBY_NOINFO)) {
			this.standby = true;
			this.forceStandby();
			this.status = Frontend.STATUS_STANDBY;
			try {
				image = ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconstandby.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (status.equals(ERROR_CONNECTION) || status.equals(ERROR_INFO)) {
			this.standby = true;
			this.forceStandby();
			this.status = Frontend.STATUS_ERROR;
			try {
				image = ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconerror.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.standby = false;
			this.clearStandby();
			this.status = Frontend.STATUS_OK;
			try {
				image = ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconok.png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		frame.setIconImage(image);
		frame.setImage(image);
		frame.setStatus(statusString);
		frame.repaint();
	}

	// handle actions
	public void actionPerformed(ActionEvent e) {
		String source = ((JMenuItem) e.getSource()).getText();
		if (source.equals("Login")) {
			back.calnetLogin();
		} else if (source.equals("Settings")) {
			back.settings();
		} else if (source.equals("Extend")) {
			back.extend(true);
		} else if (source.equals("Exit")) {
			back.exit();
		}
	}

	// handle items
	public void itemStateChanged(ItemEvent e) {
		String source = ((JMenuItem) e.getSource()).getText();
		if (source.equals("Standby")) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				// do the whole standbying thing here
			} else {
				back.extend(true);
			}
		}
	}

	// utility method to ensure proper UI reflection of current state
	public void clearStandby() {
		this.standby = false;
		int limit = frame.getJMenuBar().getMenu(0).getComponentCount();
		for (int i = 0; i < limit; i++) {
			JMenuItem current = frame.getJMenuBar().getMenu(0).getItem(i);
			if (current.getName().equals("Standby")) {
				JCheckBoxMenuItem found = (JCheckBoxMenuItem) current;
				found.setState(false);
			}
		}
	}

	// utility method to ensure proper UI reflection of current state
	public void forceStandby() {
		this.standby = true;
		int limit = frame.getJMenuBar().getMenu(0).getComponentCount();
		for (int i = 0; i < limit; i++) {
			JMenuItem current = frame.getJMenuBar().getMenu(0).getItem(i);
			if (current.getName().equals("Standby")) {
				JCheckBoxMenuItem found = (JCheckBoxMenuItem) current;
				found.setState(true);
			}
		}
	}

	// utility method to set OS flag
	public void setOS() {
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			this.os = LegacyFrontend.WINDOWS;
		} else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			this.os = LegacyFrontend.MAC;
		} else {
			this.os = LegacyFrontend.OTHER_OS;
		}
	}

	// utility method to maintain proper focus
	public ImageFrame getFrame() {
		return this.frame;
	}

	private LegacyBackend back;
	private ImageFrame frame;
	private boolean standby;
	private long timestamp;

	private int status;
	private String statusString;
	private int os;

	// possible status messages
	public static final String ACTIVE = "Active - Keeping the lights on!";
	public static final String STANDBY_MANUAL = "Standby - Select menu option to activate Fiat "
			+ "Lux.";
	public static final String STANDBY_NOINFO = "Standby - Please enter Calnet Authentication "
			+ "information.";
	public static final String ERROR_INFO = "Error - Calnet Authentication information "
			+ "incorrect.";
	public static final String ERROR_CONNECTION = "Error - Please check internet "
			+ "connection.";

	// OS flags
	public static final int WINDOWS = 0;
	public static final int MAC = 1;
	public static final int OTHER_OS = 2;
}

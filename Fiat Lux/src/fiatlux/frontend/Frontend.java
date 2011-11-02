package fiatlux.frontend;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;

import com.growl.GrowlWrapper;

import fiatlux.backend.*;
import fiatlux.os.*;

public class Frontend implements ActionListener, ItemListener {

	// constructor
	public Frontend(Backend back) {
		this.back = back;
	}

	// initialize stuff and start extend loop
	public void init() {
		// get OS
		this.setOS();

		// check to see if the tray is supported
		if (!SystemTray.isSupported()) {
			System.out.println("SystemTray is not supported.");
			return;
		}

		// create tray icon
		PopupMenu popup = new PopupMenu();
		try {
			trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResource(
					"/resources/images/trayicon.png")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// set up menu
		MenuItem login = new MenuItem("Login");
		login.addActionListener(this);
		MenuItem settings = new MenuItem("Settings");
		settings.addActionListener(this);
		MenuItem extend = new MenuItem("Extend");
		extend.addActionListener(this);
		CheckboxMenuItem standbyItem = new CheckboxMenuItem("Standby");
		standbyItem.setState(false);
		standbyItem.addItemListener(this);
		MenuItem exit = new MenuItem("Exit");
		exit.addActionListener(this);

		popup.add(login);
		popup.add(settings);
		popup.add(extend);
		popup.add(standbyItem);
		popup.addSeparator();
		popup.add(exit);

		SystemCallHandler sys = null;

		// create call handler
		switch (this.os) {
		case (Frontend.OS_WINDOWS):
			sys = new WindowsCallHandler();
			break;
		case (Frontend.OS_MAC):
			sys = new OSXCallHandler();
			break;
		case (Frontend.OS_OTHER):
			sys = new GenericCallHandler();
			break;
		}

		tray = SystemTray.getSystemTray();
		trayIcon.setPopupMenu(popup);

		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			System.out.println("TrayIcon could not be added.");
		}

		// set the status and welcome the user
		this.setStatus(Frontend.STANDBY_NOINFO);
		this.balloon("Welcome to Fiat Lux!",
				"Please enter your CalNet Authentication information to begin!");

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
					this.setStatus(Frontend.STANDBY_MANUAL);
					this.forceStandby();
					this.balloon("Where are you?",
							"If you're still in Sutardja Dai, please select the standby option "
									+ "to continue using Fiat Lux.");
				}

				if (idleTime > 9 * toMinute) {
					this.setStatus(Frontend.STANDBY_MANUAL);
					this.forceStandby();
					this.balloon("Idle",
							"You've gone idle.  Please select the standby option to continue "
									+ "using Fiat Lux.");
				}
				this.timestamp = currTime;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// let the user set up their login info
	public LinkedList<String> loginDialogue() {
		LoginDialogue login = new LoginDialogue(this.status);
		return login.getLoginInfo();
	}

	// let the user set up settings
	public LinkedList<String> settingsDialogue(int floor, int zone,
			boolean extendNotifications) {
		SettingsDialogue settings = new SettingsDialogue(floor, zone,
				this.back, extendNotifications, this.status);
		return settings.getSettingsInfo();
	}

	// display a balloon notification
	public void balloon(String title, String message) {
		if (this.os != Frontend.OS_MAC) {
			trayIcon.displayMessage(title, message, MessageType.NONE);
		} else {
			String[] notifications = { "Info" };
			GrowlWrapper growl = new GrowlWrapper("Fiat Lux", "Finder",
					notifications, notifications);
			if (growl.getState() == GrowlWrapper.GROWL_OK) {
				growl.notify("Info", title, message);
			} else {
				trayIcon.displayMessage(title, message, MessageType.NONE);
			}
		}
	}

	// set the tooltip for the tray icon
	public void setStatus(String status) {
		trayIcon.setToolTip(status);
		if (status.equals(STANDBY_MANUAL) || status.equals(STANDBY_NOINFO)) {
			this.standby = true;
			this.forceStandby();
			this.status = Frontend.STATUS_STANDBY;
			try {
				trayIcon.setImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/trayiconstandby.png")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (status.equals(ERROR_CONNECTION) || status.equals(ERROR_INFO)) {
			this.standby = true;
			this.forceStandby();
			this.status = Frontend.STATUS_ERROR;
			try {
				trayIcon.setImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/trayiconerror.png")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			this.standby = false;
			this.clearStandby();
			this.status = Frontend.STATUS_OK;
			try {
				trayIcon.setImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/trayiconok.png")));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// clean up and exit
	public void exit() {
		tray.remove(trayIcon);
	}

	// handle actions
	public void actionPerformed(ActionEvent e) {
		String source = ((MenuItem) e.getSource()).getLabel();
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
		String source = ((MenuItem) e.getSource()).getLabel();
		if (source.equals("Standby")) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				this.setStatus(Frontend.STANDBY_MANUAL);
			} else {
				back.extend(true);
			}
		}
	}

	// utility method to ensure proper UI reflection of current state
	public void clearStandby() {
		int limit = trayIcon.getPopupMenu().getItemCount();
		for (int i = 0; i < limit; i++) {
			MenuItem current = trayIcon.getPopupMenu().getItem(i);
			if (current.getLabel().equals("Standby")) {
				CheckboxMenuItem found = (CheckboxMenuItem) current;
				found.setState(false);
			}
		}
	}

	// utility method to ensure proper UI reflection of current state
	public void forceStandby() {
		int limit = trayIcon.getPopupMenu().getItemCount();
		for (int i = 0; i < limit; i++) {
			MenuItem current = trayIcon.getPopupMenu().getItem(i);
			if (current.getLabel().equals("Standby")) {
				CheckboxMenuItem found = (CheckboxMenuItem) current;
				found.setState(true);
			}
		}
	}

	// utility method to set OS flag
	public void setOS() {
		if (System.getProperty("os.name").toLowerCase().contains("win")) {
			this.os = Frontend.OS_WINDOWS;
		} else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			this.os = Frontend.OS_MAC;
		} else {
			this.os = Frontend.OS_OTHER;
		}
	}

	private Backend back;
	private TrayIcon trayIcon;
	private SystemTray tray;
	private boolean standby;
	private long timestamp;

	private int status;
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
	public static final int OS_WINDOWS = 0;
	public static final int OS_MAC = 1;
	public static final int OS_OTHER = 2;

	// status flags
	public static final int STATUS_OK = 0;
	public static final int STATUS_STANDBY = 1;
	public static final int STATUS_ERROR = 2;
}

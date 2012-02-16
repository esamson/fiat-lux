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
import javax.swing.JMenuItem;

import com.growl.GrowlWrapper;

import fiatlux.backend.*;
import fiatlux.os.localization.*;
import fiatlux.os.time.*;

public class Frontend implements ActionListener, ItemListener {

	// initialize stuff and start extend loop
	public void init(Backend back) {
		this.back = back;

		// get OS
		this.setOS();
		IdleTimeDetector sys = this.getIdleTimeDetector();
		this.localizer = this.getLocalizer();

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
		back.startLogin(false);
		back.load();

		// extend the light timer regularly
		while (true) {
			try {
				int toMinute = 60000;

				long idleTime = sys.getSystemIdleTime();

				if (idleTime > 5 * toMinute && !standby) {
					this.setStatus(Frontend.STANDBY_MANUAL);
					this.forceStandby();
					this.balloon("Idle",
							"You've gone idle.  Please select the standby option to "
									+ "continue using Fiat Lux.");
				}
				if (!standby) {
					back.extend(true);
				}
				Thread.sleep(1 * toMinute);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// let the user set up their login info
	public LinkedList<String> loginDialogue(boolean err) {
		LoginDialogue login = new LoginDialogue(this.status, this.os, err);
		return login.getLoginInfo();
	}

	// let the user set up settings
	public LinkedList<String> settingsDialogue(int floor, int zone,
			boolean extendNotifications) {
		SettingsDialogue settings = new SettingsDialogue(floor, zone,
				this.back, extendNotifications, this.status, this.os);
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
		if (status.equals(STANDBY_MANUAL) || status.equals(STANDBY_NOINFO)
				|| status.equals(STANDBY_LOCATION)) {
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
		} else if (status.equals(COMMUNICATING)) {
			this.status = STATUS_COMMUNICATING;
			try {
				trayIcon.setImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/trayicon.png")));
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
		String source = "";
		try {
			source = ((MenuItem) e.getSource()).getLabel();
		} catch (ClassCastException e2) {
			source = ((JMenuItem) e.getSource()).getText();
		}
		if (source.equals("Login")) {
			back.calnetLogin(false);
		} else if (source.equals("Settings")) {
			this.setStatus(Frontend.COMMUNICATING);
			back.settings();
		} else if (source.equals("Extend")) {
			this.setStatus(Frontend.COMMUNICATING);
			back.extend(true);
		} else if (source.equals("Exit")) {
			back.exit();
		}
	}

	// handle items
	public void itemStateChanged(ItemEvent e) {
		String source = "";
		try {
			source = ((MenuItem) e.getSource()).getLabel();
		} catch (ClassCastException e2) {
			source = ((JMenuItem) e.getSource()).getText();
		}
		if (source.equals("Standby")) {
			if (e.getStateChange() == ItemEvent.SELECTED) {
				if (!standby) {
					this.setStatus(Frontend.STANDBY_MANUAL);
				}
			} else {
				this.setStatus(Frontend.COMMUNICATING);
				back.extend(true);
			}
		}
	}

	// utility method to ensure proper UI reflection of current state
	public void clearStandby() {
		this.standby = false;
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
		this.standby = true;
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

		} else if (System.getProperty("os.name").toLowerCase().contains("nix")) {
			this.os = Frontend.OS_UNIX;

		} else {
			this.os = Frontend.OS_OTHER;
		}
	}

	public IdleTimeDetector getIdleTimeDetector() {
		IdleTimeDetector sys = null;

		// create call handler
		switch (this.os) {
		case (Frontend.OS_WINDOWS):
			sys = new WindowsIdleTimeDetector();
			break;
		case (Frontend.OS_MAC):
			sys = new MacOSXIdleTimeDetector();
			break;
		case (Frontend.OS_UNIX):
			sys = new X11LinuxIdleTimeDetector();
			break;
		case (Frontend.OS_OTHER):
			sys = new GenericIdleTimeDetector();
			break;
		}

		return sys;
	}

	public Localizer getLocalizer() {
		Localizer sys = null;

		// create call handler
		switch (this.os) {
		case (Frontend.OS_WINDOWS):
			sys = new WindowsLocalizer();
			break;
		case (Frontend.OS_MAC):
			sys = new MacOSXLocalizer();
			break;
		case (Frontend.OS_UNIX):
			sys = new X11LinuxLocalizer();
			break;
		case (Frontend.OS_OTHER):
			sys = new GenericLocalizer();
			break;
		}

		return sys;
	}

	public boolean inZone() {
		return localizer.inZone();
	}

	public boolean inStandby() {
		return standby;
	}

	protected Backend back;
	private TrayIcon trayIcon;
	private SystemTray tray;
	protected boolean standby;

	protected int status;
	protected int os;
	protected Localizer localizer;

	// possible status messages
	public static final String ACTIVE = "Active - Keeping the lights on!";
	public static final String STANDBY_MANUAL = "Standby - Select menu option to activate "
			+ "Fiat Lux.";
	public static final String STANDBY_NOINFO = "Standby - Please enter Calnet "
			+ "Authentication information.";
	public static final String STANDBY_LOCATION = "Standby - You are not in Sutardja "
			+ "Dai Hall.";
	public static final String ERROR_INFO = "Error - Calnet Authentication information "
			+ "incorrect.";
	public static final String ERROR_CONNECTION = "Error - Please check internet "
			+ "connection.";
	public static final String COMMUNICATING = "Communicating - Contacting the server.";

	// OS flags
	public static final int OS_OTHER = 0;
	public static final int OS_WINDOWS = 1;
	public static final int OS_MAC = 2;
	public static final int OS_UNIX = 3;

	// status flags
	public static final int STATUS_OK = 0;
	public static final int STATUS_COMMUNICATING = 1;
	public static final int STATUS_STANDBY = 2;
	public static final int STATUS_ERROR = 3;
}

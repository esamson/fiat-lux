package fiatlux.legacy.frontend;

import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import fiatlux.backend.Backend;
import fiatlux.frontend.Frontend;
import fiatlux.os.time.IdleTimeDetector;

public class LegacyFrontend extends Frontend implements ActionListener,
		ItemListener {

	// initialize stuff and start extend loop
	@Override
	public void init(Backend back) {
		this.back = back;

		// get OS
		this.setOS();
		IdleTimeDetector sys = this.getIdleTimeDetector();
		localizer = this.getLocalizer();

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

		frame.setVisible(true);

		// set the status and welcome the user
		this.setStatus(LegacyFrontend.STANDBY_NOINFO);

		// ask for login info
		back.startLogin(false);
		back.load();

		// extend the light timer every amount of time
		while (true) {
			try {
				int toMinute = 60000;

				long idleTime = sys.getSystemIdleTime();

				if (idleTime > 5 * toMinute && !standby) {
					this.setStatus(Frontend.STANDBY_MANUAL);
					this.forceStandby();
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
	@Override
	public LinkedList<String> loginDialogue(boolean err) {
		LegacyLoginDialogue login = new LegacyLoginDialogue(this, err);
		return login.getLoginInfo();
	}

	// let the user set up settings
	@Override
	public LinkedList<String> settingsDialogue(int floor, int zone,
			boolean extendNotifications) {
		LegacySettingsDialogue settings = new LegacySettingsDialogue(floor,
				zone, this.back, this);
		return settings.getSettingsInfo();
	}

	// set the visible status of the program
	@Override
	public void setStatus(String status) {
		this.statusString = status;
		Image image = null;
		if (status.equals(STANDBY_MANUAL) || status.equals(STANDBY_NOINFO)
				|| status.equals(STANDBY_LOCATION)) {
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
		} else if (status.equals(COMMUNICATING)) {
			this.status = Frontend.STATUS_COMMUNICATING;
			try {
				image = ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbaricon.png"));
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

	@Override
	public void balloon(String title, String message) {
		System.out.println(title + ": " + message);
	}

	// utility method to ensure proper UI reflection of current state
	@Override
	public void clearStandby() {
		this.standby = false;
		int limit = frame.getJMenuBar().getMenu(0).getItemCount();
		for (int i = 0; i < limit; i++) {
			JMenuItem current = frame.getJMenuBar().getMenu(0).getItem(i);
			if (current.getText().equals("Standby")) {
				JCheckBoxMenuItem found = (JCheckBoxMenuItem) current;
				found.setSelected(false);
				return;
			}
		}
	}

	// utility method to ensure proper UI reflection of current state
	@Override
	public void forceStandby() {
		this.standby = true;
		int limit = frame.getJMenuBar().getMenu(0).getItemCount();
		for (int i = 0; i < limit; i++) {
			JMenuItem current = frame.getJMenuBar().getMenu(0).getItem(i);
			if (current.getText().equals("Standby")) {
				JCheckBoxMenuItem found = (JCheckBoxMenuItem) current;
				found.setSelected(true);
				return;
			}
		}
	}

	// utility method to maintain proper focus
	public ImageFrame getFrame() {
		return this.frame;
	}

	public boolean inZone() {
		return localizer.inZone();
	}

	@Override
	public void exit() {
	}

	private ImageFrame frame;
	private String statusString;
}

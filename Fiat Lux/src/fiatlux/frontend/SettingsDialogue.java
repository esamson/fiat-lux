package fiatlux.frontend;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedList;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import fiatlux.backend.Backend;

public class SettingsDialogue extends JFrame implements ActionListener,
		HyperlinkListener {

	private static final long serialVersionUID = 1L;

	public SettingsDialogue(int floor, int zone, Backend back,
			boolean extendNotifications, int status, int os) {
		super();

		// set up options
		Integer[] op0 = { 4, 6, 7 };
		selectFloor = new JComboBox(op0);
		selectFloor.setName("Floor");
		selectFloor.addActionListener(this);
		Integer[] op1 = { 1, 2, 3, 4, 5 };
		zones4 = new DefaultComboBoxModel(op1);
		Integer[] op2 = { 1 };
		zones6 = new DefaultComboBoxModel(op2);
		Integer[] op3 = { 1, 2, 3, 4 };
		zones7 = new DefaultComboBoxModel(op3);
		String[] op4 = { "Off", "Low", "Medium", "High" };
		brightness = new DefaultComboBoxModel(op4);
		String[] op5 = { "Off", "On" };
		brightness6 = new DefaultComboBoxModel(op5);
		this.floor = floor;
		this.zone = zone;
		this.back = back;
		this.extendNotifications = extendNotifications;

		// set JFrame variables
		try {
			switch (status) {
			case Frontend.STATUS_OK:
				this.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconok.png")));
				break;
			case Frontend.STATUS_STANDBY:
				this.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconstandby.png")));
				break;
			case Frontend.STATUS_ERROR:
				this.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbariconerror.png")));
				break;
			default:
				this.setIconImage(ImageIO.read(this.getClass().getResource(
						"/resources/images/taskbaricon.png")));
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.setUndecorated(true);
		this.setVisible(true);
		this.setLocationRelativeTo(null);
		this.settingsInfo = new LinkedList<String>();
		this.settingsDialogue();
		this.dispose();
	}

	public void settingsDialogue() {
		this.brightnessLevels = back.getBrightness(true);
		myPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();

		selectZone = new JComboBox();
		selectZone.addActionListener(this);
		selectBrightness = new JComboBox();

		selectExtendNotifications = new JCheckBox(
				"Notify me when the timer is extended");
		selectExtendNotifications.setSelected(this.extendNotifications);

		selectZone.setModel(zones4);
		selectBrightness.setModel(brightness);

		// label
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		myPanel.add(new JLabel(
				"Please tell us where you sit!  Maps are available "), c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		JEditorPane link = new JEditorPane();
		try {
			link.setContentType("text/html");
			link.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
			link.setText("<html><a href=\"http://green.millennium.berkeley.edu/power/"
					+ "SutardjaDaiHall\">on our website.</a></html>");
			link.setEditable(false);
			link.setOpaque(false);
			link.setFont(UIManager.getFont("Label.font"));
			link.addHyperlinkListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		myPanel.add(link, c);
		c.gridy = 2;
		myPanel.add(Box.createVerticalStrut(15), c);

		// floor
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.weightx = .5;
		myPanel.add(new JLabel("Floor"), c);
		c.gridy = 4;
		myPanel.add(Box.createVerticalStrut(5), c);
		c.gridy = 5;
		myPanel.add(selectFloor, c);
		c.gridy = 6;
		myPanel.add(Box.createVerticalStrut(15), c);

		// zone
		c.gridx = 2;
		c.gridy = 3;
		myPanel.add(new JLabel("Zone"), c);
		c.gridy = 4;
		myPanel.add(Box.createVerticalStrut(5), c);
		c.gridy = 5;
		myPanel.add(selectZone, c);
		c.gridy = 6;
		myPanel.add(Box.createVerticalStrut(15), c);

		// brightness
		c.gridx = 1;
		c.gridy = 7;
		c.weightx = 1;
		myPanel.add(new JLabel("Brightness"), c);
		c.gridy = 8;
		myPanel.add(Box.createVerticalStrut(5), c);
		c.gridy = 9;
		myPanel.add(selectBrightness, c);
		c.gridy = 10;
		myPanel.add(selectExtendNotifications, c);
		c.gridy = 11;
		myPanel.add(Box.createVerticalStrut(15), c);

		String[] buttons = new String[1];
		buttons[0] = "Confirm";

		selectFloor.setSelectedItem(floor);
		this.updateMenus();
		selectZone.setSelectedItem(zone);
		selectBrightness.setSelectedIndex(brightnessLevels[floor][zone]);

		int exitType = JOptionPane.showOptionDialog(null, myPanel, "Settings",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
				buttons, buttons[0]);
		settingsInfo.add(exitType + "");
		settingsInfo.add(selectFloor.getSelectedItem() + "");
		settingsInfo.add(selectZone.getSelectedItem() + "");
		settingsInfo.add(selectBrightness.getSelectedIndex() + "");
		settingsInfo.add(selectExtendNotifications.isSelected() + "");
	}

	// update options on start
	public void updateMenus() {
		switch ((Integer) selectFloor.getSelectedItem()) {
		case 4:
			selectZone.setModel(zones4);
			selectBrightness.setModel(brightness);
			break;
		case 6:
			selectZone.setModel(zones6);
			selectBrightness.setModel(brightness6);
			break;
		case 7:
			selectZone.setModel(zones7);
			selectBrightness.setModel(brightness);
			break;
		}
	}

	// update options on change
	public void actionPerformed(ActionEvent a) {
		try {
			JComboBox cb = (JComboBox) a.getSource();
			if (cb.getName() != null && cb.getName().equals("Floor")) {
				switch ((Integer) cb.getSelectedItem()) {
				case 4:
					selectZone.setModel(zones4);
					selectBrightness.setModel(brightness);
					break;
				case 6:
					selectZone.setModel(zones6);
					selectBrightness.setModel(brightness6);
					break;
				case 7:
					selectZone.setModel(zones7);
					selectBrightness.setModel(brightness);
					break;
				}
			}
			int bright = this.brightnessLevels[(Integer) selectFloor
					.getSelectedItem()][(Integer) selectZone.getSelectedItem()];
			selectBrightness.setSelectedIndex(bright);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	// take user to the maps
	public void hyperlinkUpdate(HyperlinkEvent h) {
		if (h.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
			try {
				Desktop desktop = Desktop.getDesktop();
				desktop.browse(new URI(h.getURL().toString()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// return settings info
	public LinkedList<String> getSettingsInfo() {
		return settingsInfo;
	}

	// models for switching
	private DefaultComboBoxModel zones4;
	private DefaultComboBoxModel zones6;
	private DefaultComboBoxModel zones7;
	private DefaultComboBoxModel brightness;
	private DefaultComboBoxModel brightness6;

	// actual components
	private JPanel myPanel;
	private GridBagConstraints c;
	private JComboBox selectFloor;
	private JComboBox selectZone;
	private JComboBox selectBrightness;
	private JCheckBox selectExtendNotifications;

	private int[][] brightnessLevels;

	// old settings
	private int floor;
	private int zone;
	private boolean extendNotifications;

	// selection data
	private LinkedList<String> settingsInfo;

	private Backend back;
}

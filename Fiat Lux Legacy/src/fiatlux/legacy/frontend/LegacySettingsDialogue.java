package fiatlux.legacy.frontend;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import fiatlux.legacy.backend.LegacyBackend;

public class LegacySettingsDialogue implements ActionListener {

	public LegacySettingsDialogue(int floor, int zone, LegacyBackend back,
			LegacyFrontend front) {
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
		this.front = front;

		this.settingsInfo = new LinkedList<String>();
		this.settingsDialogue();
	}

	public void settingsDialogue() {
		this.brightnessLevels = back.getBrightness(true);
		myPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();

		selectZone = new JComboBox();
		selectZone.addActionListener(this);
		selectBrightness = new JComboBox();

		selectZone.setModel(zones4);
		selectBrightness.setModel(brightness);

		// label
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		myPanel.add(new JLabel(
				"Please tell us where you sit!  Maps are available at"), c);
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = 1;
		JTextField text = new JTextField(
				" http://green.millennium.berkeley.edu/power/"
						+ "SutardjaDaiHall");
		text.setEditable(false);
		text.setBorder(null);
		text.setForeground(UIManager.getColor("Label.foreground"));
		text.setBackground(UIManager.getColor("Label.background"));
		text.setFont(UIManager.getFont("Label.font"));
		myPanel.add(text, c);
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
		myPanel.add(Box.createVerticalStrut(15), c);

		String[] buttons = new String[1];
		buttons[0] = "Confirm";

		selectFloor.setSelectedItem(floor);
		this.updateMenus();
		selectZone.setSelectedItem(zone);
		selectBrightness.setSelectedIndex(brightnessLevels[floor][zone]);

		int exitType = JOptionPane.showOptionDialog(front.getFrame(), myPanel,
				"Settings", JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, buttons, buttons[0]);
		settingsInfo.add(exitType + "");
		settingsInfo.add(selectFloor.getSelectedItem() + "");
		settingsInfo.add(selectZone.getSelectedItem() + "");
		settingsInfo.add(selectBrightness.getSelectedIndex() + "");
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

	private int[][] brightnessLevels;

	// old settings
	private int floor;
	private int zone;

	// selection data
	private LinkedList<String> settingsInfo;

	private LegacyBackend back;
	private LegacyFrontend front;
}

package fiatlux.legacy.frontend;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.LinkedList;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LegacyLoginDialogue {

	public LegacyLoginDialogue(LegacyFrontend front) {
		this.front = front;
		this.loginInfo = new LinkedList<String>();
		this.loginDialogue();
	}

	// let the user set up their login info
	private void loginDialogue() {
		JTextField username = new JTextField(16);
		JPasswordField password = new JPasswordField(16);

		JPanel login = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		login.add(new JLabel("Username"), c);
		c.gridy = 1;
		c.anchor = GridBagConstraints.CENTER;
		login.add(Box.createVerticalStrut(5), c);
		c.gridy = 2;
		login.add(username, c);
		c.gridy = 3;
		login.add(Box.createVerticalStrut(15), c);
		c.gridy = 4;
		c.anchor = GridBagConstraints.WEST;
		login.add(new JLabel("Password"), c);
		c.gridy = 5;
		c.anchor = GridBagConstraints.CENTER;
		login.add(Box.createVerticalStrut(5), c);
		c.gridy = 6;
		login.add(password, c);

		String[] buttons = new String[2];
		buttons[0] = "Login";
		buttons[1] = "Cancel";

		int exitType = JOptionPane.showOptionDialog(front.getFrame(), login,
				"CalNet Authentication", JOptionPane.DEFAULT_OPTION,
				JOptionPane.PLAIN_MESSAGE, null, buttons, buttons[0]);
		loginInfo.add(username.getText());
		char[] pass = password.getPassword();
		String passString = "";
		for (char ch : pass) {
			passString += ch;
		}
		loginInfo.add(passString);
		loginInfo.add(exitType + "");
	}

	// return login info
	public LinkedList<String> getLoginInfo() {
		return loginInfo;
	}

	private LinkedList<String> loginInfo;

	private LegacyFrontend front;
}

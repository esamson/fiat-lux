package fiatlux.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.ServerSocket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.UUID;

import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import fiatlux.frontend.Frontend;

public class Backend {

	// set up variables to begin extending
	public void init(Frontend front) {
		sessionId = UUID.randomUUID();
		try {
			ServerSocket instance = new ServerSocket(43657);
			instance.getLocalPort();
		} catch (Exception e) {
			System.exit(0);
		}
		this.front = front;
		this.front.init(this);
	}

	// automatically extend lighting
	public void extend(boolean auto) {
		if (!front.inZone() /*&& !front.inStandby()*/) {
			front.setStatus(Frontend.STANDBY_LOCATION);
			front.forceStandby();
			front.balloon("Where are you?",
					"We've detected that you're no longer in Sutardja Dai, and "
							+ "won't extend the lights until you return.");
			return;
		}

		if (auto) {
			this.brightness = this.getBrightness(false)[this.floor][this.zone];
		}
		String bright = "";
		switch (this.brightness) {
		case 0:
			bright = "Off";
			break;
		case 1:
			bright = "Low";
			break;
		case 2:
			bright = "Medium";
			break;
		case 3:
			bright = "High";
			break;
		}
		if (extendNotifications) {
			front.balloon("Extending...",
					"Currently extending the lights for Floor " + this.floor
							+ ", Zone " + this.zone
							+ ".  Current brightness level is " + bright + ".");
		}
		try {
			ClientCookie info = this.getSessionCookie();
			HttpClient client = info.getClient();
			String metaCookie = info.getCookie();
			// set the lighting level
			HttpPost connectForSet = new HttpPost(
					"http://green.millennium.berkeley.edu/power/"
							+ "SutardjaDaiHall/" + "Floor" + this.floor
							+ "/Zone" + this.zone + "/setlevel/"
							+ this.brightness);

			connectForSet.addHeader("Cookie", metaCookie);
			System.out.println(connectForSet.getURI());
			HttpResponse responseToSet = client.execute(connectForSet);

			// print the data and clear the connection
			Backend.printData(responseToSet);
			Backend.inputStreamToString(responseToSet.getEntity().getContent());

			// at long last, set the timer if necessary
			if (this.brightness != 0) {
				int toMinute = 60;
				int period = 2 * toMinute;
				System.out.println(period);
				HttpPost connectForExtend = new HttpPost(
						"http://green.millennium.berkeley.edu/power/"
								+ "SutardjaDaiHall/" + "Floor" + this.floor
								+ "/Zone" + this.zone + "/extend?time="
								+ period + "&sessonid="
								+ String.valueOf(sessionId));

				connectForSet.addHeader("Cookie", metaCookie);

				HttpResponse responseToExtend = client
						.execute(connectForExtend);

				// print the data and clear the connection
				Backend.printData(responseToExtend);
				System.out.println(connectForExtend.getURI());
				Backend.inputStreamToString(responseToExtend.getEntity()
						.getContent());
			}

			// check login information
			if (responseToSet.getStatusLine().getStatusCode() == 200) {
				if (extendNotifications) {
					front.balloon("Extension success!",
							"Lighting for the selected zone has been extended.");
				}
				front.setStatus(Frontend.ACTIVE);
				// front.clearStandby();
			} else {
				front.balloon("Authentication Failure",
						"Please re-enter your CalNet Authentication information.");
				front.setStatus(Frontend.ERROR_INFO);
			}
		} catch (Exception e) {
			front.balloon("Connection Error",
					"Please check your internet connection.");
			front.setStatus(Frontend.ERROR_CONNECTION);
		}
	}

	// get lighting information for the zone
	public int[][] getBrightness(boolean inform) {
		if (inform) {
			front.balloon("Retreiving Brightness Settings...",
					"Getting current brightness settings from the server.");
		}
		try {
			ClientCookie info = this.getSessionCookie();
			HttpClient client = info.getClient();
			String metaCookie = info.getCookie();

			// get the lighting levels
			int[][] levels = new int[8][6];
			for (int floor = 0; floor < 8; floor++) {
				for (int zone = 0; zone < 6; zone++) {
					HttpPost connectForGet = new HttpPost(
							"http://green.millennium.berkeley.edu/power/"
									+ "SutardjaDaiHall/" + "Floor" + floor
									+ "/Zone" + zone + "/status");

					connectForGet.addHeader("Cookie", metaCookie);
					System.out.println(connectForGet.getURI());
					HttpResponse responseToGet = client.execute(connectForGet);

					// print the data and clear the connection
					Backend.printData(responseToGet);

					// check login information
					if (responseToGet.getStatusLine().getStatusCode() != 200) {
						front.balloon("Authentication Failure",
								"Unable to retrieve current lighting level.  Please re-enter "
										+ "login information.");
						front.setStatus(Frontend.ERROR_INFO);
						return new int[8][6];
					}

					// process json
					else {
						String json = Backend.inputStreamToString(responseToGet
								.getEntity().getContent());
						int receivedBrightness;
						try {
							receivedBrightness = (Integer.parseInt(""
									+ json.charAt(json.length() - 3)));
						} catch (Exception e) {
							receivedBrightness = 0;
						}
						levels[floor][zone] = receivedBrightness;
					}
				}
			}
			front.setStatus(Frontend.ACTIVE);
			return levels;
		} catch (Exception e) {
			front.balloon("Connection Error",
					"Please check your internet connection.");
			front.setStatus(Frontend.ERROR_CONNECTION);
		}
		return new int[8][6];
	}

	// check login information
	public boolean checkLogin(int floor, int zone) {
		front.balloon("Checking Login Information...",
				"Authenticating login information on the server.");
		try {
			ClientCookie info = this.getSessionCookie();
			HttpClient client = info.getClient();
			String metaCookie = info.getCookie();

			// get the lighting level
			HttpPost connectForGet = new HttpPost(
					"http://green.millennium.berkeley.edu/power/"
							+ "SutardjaDaiHall/" + "Floor" + floor + "/Zone"
							+ zone + "/status");

			connectForGet.addHeader("Cookie", metaCookie);
			System.out.println(connectForGet.getURI());
			HttpResponse responseToGet = client.execute(connectForGet);

			// print the data and clear the connection
			Backend.printData(responseToGet);

			// check login information
			boolean success = (responseToGet.getStatusLine().getStatusCode() == 200);
			if (success) {
				front.setStatus(Frontend.ACTIVE);
			} else {
				front.balloon("Authentication Failure", "Please re-enter "
						+ "login information.");
				front.setStatus(Frontend.ERROR_INFO);
			}
			return success;
		} catch (Exception e) {
			front.balloon("Connection Error",
					"Please check your internet connection.");
			front.setStatus(Frontend.ERROR_CONNECTION);
			e.printStackTrace();
		}
		return false;
	}

	// ask for login info
	public void calnetLogin(boolean err) {
		LinkedList<String> info;
		if (!dialogueOn) {
			this.dialogueOn = true;
			info = front.loginDialogue(err);
			this.dialogueOn = false;
		} else {
			return;
		}
		if (Integer.parseInt(info.get(2)) == 0) {
			this.username = info.get(0);
			this.password = info.get(1);
			if (this.checkLogin(this.floor, this.zone)) {
				this.extend(true);
			} else {
				this.calnetLogin(true);
			}
		}
	}

	// ask for login info on startup
	public void startLogin(boolean err) {
		LinkedList<String> info;
		if (!dialogueOn) {
			this.dialogueOn = true;
			info = front.loginDialogue(err);
			this.dialogueOn = false;
		} else {
			return;
		}
		if (Integer.parseInt(info.get(2)) == 0) {
			this.username = info.get(0);
			this.password = info.get(1);
			if (this.checkLogin(this.floor, this.zone)) {
			} else {
				this.startLogin(true);
			}
		}
	}

	// allow changing settings
	public void settings() {
		LinkedList<String> info;
		if (!dialogueOn) {
			this.dialogueOn = true;
			info = front.settingsDialogue(this.floor, this.zone,
					this.extendNotifications);
			this.dialogueOn = false;
		} else {
			return;
		}
		if (Integer.parseInt(info.get(0)) != JOptionPane.CLOSED_OPTION) {
			this.floor = Integer.parseInt(info.get(1));
			this.zone = Integer.parseInt(info.get(2));
			this.brightness = Integer.parseInt(info.get(3));
			try {
				this.extendNotifications = Boolean.parseBoolean(info.get(4));
			} catch (IndexOutOfBoundsException e) {

			}
			this.save();
			this.extend(false);
		}
	}

	// clean up and exit
	public void exit() {
		front.exit();
		System.exit(0);
	}

	// save settings
	private void save() {
		Writer saver = null;
		String saveContents = "" + this.floor + ' ' + this.zone + ' '
				+ this.extendNotifications;

		try {
			saver = new BufferedWriter(new FileWriter("settings.flx"));
			saver.write(saveContents);
			saver.close();
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// load settings
	public void load() {
		BufferedReader loader = null;

		try {
			loader = new BufferedReader(new FileReader("settings.flx"));
			StringTokenizer parser = new StringTokenizer(loader.readLine());
			loader.close();
			this.floor = Integer.parseInt(parser.nextToken());
			this.zone = Integer.parseInt(parser.nextToken());
			this.extendNotifications = Boolean.parseBoolean(parser.nextToken());
		}

		catch (FileNotFoundException e) {
			this.settings();
		}

		catch (NoSuchElementException e) {
			return;
		}

		catch (IOException e) {
			e.printStackTrace();
		}
	}

	// get session cookie to get data from the website or post data to it
	private ClientCookie getSessionCookie() throws Exception {
		front.setStatus(Frontend.COMMUNICATING);

		// get login URL
		HttpClient client = new DefaultHttpClient();
		HttpParams params = client.getParams();
		HttpClientParams.setRedirecting(params, false);

		HttpGet connectForURL = new HttpGet(
				"http://green.millennium.berkeley.edu/power/SutardjaDaiHall/"
						+ "Floor" + floor + "/Zone" + zone);
		System.out.println(connectForURL.getURI());
		HttpResponse responseToURL = client.execute(connectForURL);

		// print the data and clear the connection
		Backend.printData(responseToURL);
		Backend.inputStreamToString(responseToURL.getEntity().getContent());

		// jump over intermediate steps
		HttpResponse passedResponse = responseToURL;
		String locForJump = "";
		while (true) {
			Header[] headersForJump = passedResponse.getAllHeaders();
			for (Header h : headersForJump) {
				if (h.getName().equals("Location")) {
					locForJump = h.getValue();
					break;
				}
			}
			if (passedResponse.getStatusLine().getStatusCode() == 200) {
				break;
			}

			System.out.println(locForJump);
			HttpGet connectForJump = new HttpGet(locForJump);
			HttpResponse responseToJump = client.execute(connectForJump);

			// print the data and clear the connection
			Backend.printData(responseToJump);
			Backend.inputStreamToString(responseToJump.getEntity().getContent());

			passedResponse = responseToJump;
		}

		// get JSESSION info, lt, eventId
		String locForJs = locForJump;
		HttpGet connectForJs = new HttpGet(locForJs);
		System.out.println(locForJs);
		HttpResponse responseToJs = client.execute(connectForJs);

		// print the data and clear the connection
		Backend.printData(responseToJs);

		// get information for login (lt, _eventId)
		String html = Backend.inputStreamToString(responseToJs.getEntity()
				.getContent());
		String hidden = "type=\"hidden\"";
		int beginIndex = html.indexOf(hidden);
		int lineEnd = html.indexOf('\n', beginIndex);
		String lt = html.substring(beginIndex, lineEnd);
		String eventId = html.substring(html.indexOf(hidden, beginIndex + 1),
				html.indexOf('\n', lineEnd + 1));
		lt = lt.substring(lt.indexOf("value="));
		lt = lt.substring(lt.indexOf('"') + 1);
		lt = lt.substring(0, lt.indexOf('"'));
		eventId = eventId.substring(eventId.indexOf("value="));
		eventId = eventId.substring(eventId.indexOf('"') + 1);
		eventId = eventId.substring(0, eventId.indexOf('"'));

		// create jsCookie
		String jsCookie = "";
		Header[] headersForLogin = responseToJs.getAllHeaders();
		for (Header h : headersForLogin) {
			if (h.getName().equals("Set-Cookie")) {
				jsCookie = h.getValue();
				break;
			}
		}

		// get ticket-generating cookie and ticket using user's login
		HttpPost connectForLogin = new HttpPost(locForJs);

		// set POST data
		List<NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("username", this.username));
		postParams.add(new BasicNameValuePair("password", this.password));
		postParams.add(new BasicNameValuePair("lt", lt));
		postParams.add(new BasicNameValuePair("_eventId", eventId));
		UrlEncodedFormEntity postEntity = new UrlEncodedFormEntity(postParams,
				HTTP.UTF_8);
		connectForLogin.setEntity(postEntity);

		// set cookie
		connectForLogin.addHeader("Cookie", jsCookie);

		System.out.println(connectForLogin.getURI());
		HttpResponse responseToLogin = client.execute(connectForLogin);

		// print the data and clear the connection
		Backend.printData(responseToLogin);
		Backend.inputStreamToString(responseToLogin.getEntity().getContent());

		// jump over intermediate steps; this time we get some delicious
		// cookies
		passedResponse = responseToLogin;
		LinkedList<String> cookies = new LinkedList<String>();
		while (true) {
			Header[] headersForJump = passedResponse.getAllHeaders();
			for (Header h : headersForJump) {
				if (h.getName().equals("Location")) {
					locForJump = h.getValue();
				}
				if (h.getName().equals("Set-Cookie")) {
					cookies.add(h.getValue());
				}
			}
			if (passedResponse.getStatusLine().getStatusCode() == 200) {
				break;
			}

			System.out.println(locForJump);
			HttpGet connectForJump = new HttpGet(locForJump);
			HttpResponse responseToJump = client.execute(connectForJump);

			// print the data and clear the connection
			Backend.printData(responseToJump);
			Backend.inputStreamToString(responseToJump.getEntity().getContent());

			passedResponse = responseToJump;
		}

		// make a meta-cookie
		String metaCookie = jsCookie;
		for (String s : cookies) {
			metaCookie += "; " + s;
		}
		return new ClientCookie(client, metaCookie);
	}

	// utility method to print http data to console
	private static void printData(HttpResponse response) throws IOException {
		System.out.println(response.getStatusLine());
		HeaderIterator it = response.headerIterator();
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		System.out.println();
	}

	// utility method; self-explanatory
	private static String inputStreamToString(InputStream is)
			throws IOException {
		String line = "";
		String total = "";

		// Wrap a BufferedReader around the InputStream
		BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		// Read response until the end
		while ((line = rd.readLine()) != null) {
			total += (line);
			total += ('\n');
		}

		rd.close();

		// Return full string
		return total;
	}

	// utility methods for testing
	public void setUsername(String u) {
		this.username = u;
	}

	public void setPassword(String p) {
		this.password = p;
	}

	protected String username;
	protected String password;
	protected int floor;
	protected int zone;
	protected int brightness;
	private boolean extendNotifications;
	// private String ip;
	protected UUID sessionId;

	protected Frontend front;

	// booleans for preventing multiple dialogues
	private boolean dialogueOn;
}
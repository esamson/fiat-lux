package fiatlux.legacy.backend;

import java.net.ServerSocket;
import java.util.UUID;

import fiatlux.legacy.frontend.*;
import fiatlux.backend.*;
import fiatlux.frontend.Frontend;

public class LegacyBackend extends Backend {

	/**
	 * Sets up variables to start Fiat Lux.
	 * 
	 * @param front
	 *            The Frontend object that handles the visible portion of the
	 *            app.
	 */
	@Override
	public void init(Frontend front) {
		this.sessionId = UUID.randomUUID();
		try {
			ServerSocket instance = new ServerSocket(43657);
			instance.getLocalPort();
		} catch (Exception e) {
			System.exit(0);
		}
		this.front = (LegacyFrontend) front;
		this.front.init(this);
	}
}
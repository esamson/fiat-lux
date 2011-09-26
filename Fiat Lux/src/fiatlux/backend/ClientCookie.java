package fiatlux.backend;

import org.apache.http.client.HttpClient;

public class ClientCookie {
	public ClientCookie(HttpClient c, String s) {
		client = c;
		cookie = s;
	}

	public HttpClient getClient() {
		return client;
	}

	public String getCookie() {
		return cookie;
	}

	private HttpClient client;
	private String cookie;
}

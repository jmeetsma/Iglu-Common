package org.ijsberg.iglu.server.telnet.test;

import org.ijsberg.iglu.server.connection.ClientSession;
import org.ijsberg.iglu.server.connection.ClientSessionAware;

/**
 */
public class TestObject implements ClientSessionAware {
	private String message = "Hi!";
	private ClientSession session;


	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void exit() {
		session.sendMessage("Bye");
		session.terminate();
	}

	@Override
	public void setSession(ClientSession session) {
		this.session = session;
	}
}

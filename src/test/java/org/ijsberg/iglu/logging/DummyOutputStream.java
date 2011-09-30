package org.ijsberg.iglu.logging;

import java.io.ByteArrayOutputStream;

/**
 */
public class DummyOutputStream extends ByteArrayOutputStream {
	private String lastOutput;

	public String getLastOutput() {
		byte[] bytes = toByteArray();
		if(bytes.length != 0) {
			lastOutput = new String(bytes);
			reset();
		}
		return lastOutput;
	}

}

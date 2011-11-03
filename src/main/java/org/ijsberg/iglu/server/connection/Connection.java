package org.ijsberg.iglu.server.connection;

/**
 * Represents a low level, active connection to a client.
 */
public interface Connection
{
	/**
	 * @param cause
	 */
	public void close(String cause);

	/**
	 */
	public void close();

	/**
	 *
	 * @return
	 */
	public boolean isClosed();
}

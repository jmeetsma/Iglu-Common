package org.ijsberg.iglu.runtime;

/**
 * Expresses the fact that an implementations can be put in
 * an active and an inactive state.
 */
public interface Startable {

	/**
	 *
	 */
	void start();

	/**
	 *
	 */
	boolean isStarted();

	/**
	 *
	 */
	void stop();

}

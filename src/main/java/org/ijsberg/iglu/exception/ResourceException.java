package org.ijsberg.iglu.exception;

/**
 * Thrown if a resource or system in the environment is unavailable or behaves in an unexpected manner.
 */
public class ResourceException extends RuntimeException {

	public ResourceException() {
	}

	public ResourceException(String message) {
		super(message);
	}

	public ResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResourceException(Throwable cause) {
		super(cause);
	}
}

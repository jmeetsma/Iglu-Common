package org.ijsberg.iglu;

/**
 *
 * Sometimes a request can not be handled, because the state of the
 * part that must handle it, does not permit it.
 *
 * In some cases such an exceptional situation is not critical and
 * may be handled by the user (interface).
 *
 */
public class StateException extends RuntimeException
{
	private Object details;

	/**
	 *
	 */
	public StateException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public StateException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public StateException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public StateException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param message
	 * @param details data needed for analysis
	 */
	public StateException(String message, Object details)
	{
		super(message);
		this.details = details;
	}

	/**
	 * @param message
	 * @param details data needed for analysis
	 * @param cause
	 */
	public StateException(String message, Object details, Throwable cause)
	{
		super(message, cause);
		this.details = details;
	}

	/**
	 * @return
	 */
	public Object getDetails()
	{
		return details;
	}

}

package org.ijsberg.iglu.mvc.mapping;

import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.Collection;

/**
 * Marks the start of a path that is followed if an invocation throws an exception.
 *
 */
public class ExceptionHandler extends InvocationResultExpression
{
	private Collection caughtExceptions;

	/**
	 * @param value
	 * @param depth
	 * @param lineNr
	 */
	public ExceptionHandler(String value, int depth, int lineNr)
	{
		super(value, org.ijsberg.iglu.mvc.mapping.InvocationResultExpression.EQ, depth, lineNr);
		caughtExceptions = StringSupport.split(value, " ,");
	}


	/**
	 * @return
	 */
	public String toString()
	{
		return "exceptionHandler of: " + getArgument();
	}

	/**
	 *
	 * @param t
	 * @return true if the throwable encountered is listed
	 */
	public boolean doesCatch(Throwable t)
	{
		return caughtExceptions.contains(t.getClass().getName());
	}
}

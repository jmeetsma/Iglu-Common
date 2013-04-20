package org.ijsberg.iglu.mvc.mapping;

import org.ijsberg.iglu.mvc.RequestDispatcher;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;


/**
 * Stands for a possible return value of the invocation of a method.
 */
public class InvocationResultExpression extends MapElement
{
	private ArrayList elements = new ArrayList();
	public static final int EQ = 0;// == (or =)
	public static final int NE = 1;// !=
	public static final int GT = 2;// >
	public static final int LT = 3;// <
	public static final int GE = 4;// >=
	public static final int LE = 5;// <=
	private int operatorType = EQ;
	public static final String[] operatorTypeStr = new String[]{"EQ", "NE", "GT", "LT", "GE", "LE"};

	/**
	 * @param argument
	 * @param operatorType
	 * @param depth
	 * @param lineNr
	 */
	public InvocationResultExpression(String argument, int operatorType, int depth, int lineNr)
	{
		super(argument, depth, lineNr);
		if(operatorType < 0 || operatorType > 5)
		{
			throw new IllegalArgumentException("operator type " + operatorType + " invalid");
		}
		this.operatorType = operatorType;
	}

	/**
	 * @param dispatcher
	 * @return
	 */
	public String check(RequestDispatcher dispatcher)
	{
		if (argument == null || "".equals(argument))
		{
			return "missing label";
		}
		if (elements == null || elements.isEmpty())
		{
			return "Missing tasks or redirects";
		}
		return null;
	}

	/**
	 * @param fe
	 * @return
	 */
	public boolean addFlowElement(MapElement fe)
	{
		//we accept invocations, response writers

		if (terminated)
		{
			//we cannot add dead code
			return false;
		}

		if ((fe instanceof Invocation) ||
				(fe instanceof ResponseWriter))
		{
			if (fe instanceof ResponseWriter)
			{
				terminated = true;
			}
			elements.add(fe);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param processArray
	 * @param requestProperties
	 * @param dispatcher
	 * @return
	 * @throws Throwable
	 */
	public boolean processRequest(String[] processArray, Properties requestProperties, RequestDispatcher dispatcher)
	{
		timesProcessed++;

		Iterator i = elements.iterator();
		while (i.hasNext())
		{
			MapElement fe = (MapElement) i.next();
			if (fe.processRequest(processArray, requestProperties, dispatcher))
			{
				//success is defined by a successful dispatch or redirect
				return true;
			}
		}
		return false;
	}

	/**
	 * @param result
	 * @return true if the return value of the method matches the specified result
	 */
	public boolean isMatch(Object result)
	{
		if (result == null)
		{
			return false;
		}

		if (operatorType == EQ)
		{
			return (argument.equals(result.toString()));
		}
		if (operatorType == NE)
		{
			return (!argument.equals(result.toString()));
		}

		if(StringSupport.isNumeric(result.toString()) && StringSupport.isNumeric(argument.toString()))
		{
			long numericResult = Long.parseLong(argument);
			long numericArgument = Long.parseLong(argument);

			if (operatorType == GT)
			{
				return (numericResult > numericArgument);
			}
			if (operatorType == LT)
			{
				return (numericResult < numericArgument);
			}
			if (operatorType == GE)
			{
				return (numericResult >= numericArgument);
			}
			if (operatorType == LE)
			{
				return (numericResult <= numericArgument);
			}
		}
		return false;
	}

	/**
	 * @return
	 */
	public String toString()
	{
		StringBuffer result = new StringBuffer(indent() + "RESULT=" + getArgument() + ": -> " + timesProcessed + "\n");
		if (elements != null)
		{
			Iterator i = elements.iterator();
			while (i.hasNext())
			{
				MapElement fe = (MapElement) i.next();
				result.append(fe.toString());
			}
		}
		return result.toString();
	}
}

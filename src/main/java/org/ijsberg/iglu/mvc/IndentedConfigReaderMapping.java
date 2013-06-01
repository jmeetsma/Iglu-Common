/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.mvc;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.mvc.mapping.*;
import org.ijsberg.iglu.mvc.mapping.Process;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


/**
 * Tree of objects
 * <ul>
 * <li>representing an MVC mapping</li>
 * <li>facilitating processing of a request</li>
 * </ul>
 */
public class IndentedConfigReaderMapping implements Mapping
{
	private String mappingName;
	private String fileName;
	private File file;
	private long timeLastModified;
	private boolean isLoaded;
	private ArrayList loadMessages;
	private ArrayList sanityCheckMessages;
//	private boolean strict;

	private ArrayList discoveredMapElements = new ArrayList();
	private ArrayList lines = new ArrayList();
	private Process rootProcess;
//	private Request initialRequest;

	private boolean tabsUsedAsIndentation;
	private boolean spacesUsedAsIndentation;

	private Assembly assembly;

	/**
	 * @param flowName
	 * @param fileName
	 * @paran initialRequest
	 */
	public IndentedConfigReaderMapping(String flowName, String fileName, Assembly assembly)
	{
		this.mappingName = flowName;
		this.fileName = fileName;

		loadMessages = new ArrayList();
		sanityCheckMessages = new ArrayList();

		loadFlow();

		file = new File(fileName);
		if (file.exists())
		{
			timeLastModified = file.lastModified();
		}


		this.assembly = assembly;
	}


	/**
	 * @return name of the MVC tree
	 */
	public String getName()
	{
		return mappingName;
	}

	/**
	 * @return
	 */
	public String getFileName()
	{
		return fileName;
	}


	/**
	 * @return true if the physical MVC file has a newer timestamp than its last check
	 */
	public boolean isModified()
	{
		if (file.exists())
		{
			if (timeLastModified != file.lastModified())
			{
				System.out.println(new LogEntry("MVC modified"));
				timeLastModified = file.lastModified();
				return true;
			}
		}
		return false;
	}


	/**
	 * @return true if the tree was successfully constructed from the definition
	 */
	public boolean isLoaded()
	{
		return isLoaded;
	}


	/**
	 * @param fe
	 * @return a string describing the MVC element
	 */
	public static String getFlowElementDescription(MapElement fe)
	{
		return fe.getClass().getSimpleName().toLowerCase() + ':' + fe.getArgument();
	}

	/**
	 * @return a text containing messages collected while loading the mvc tree
	 */
	public List getLoadMessages()
	{
		return loadMessages;
	}

	/**
	 * Adds a message to the list.
	 *
	 * @param fileName
	 * @param lineNr
	 * @param line
	 * @param errorMessage
	 */
	private static void reportError(List messages, String fileName, int lineNr, String line, String errorMessage)
	{
		messages.add(new Date() + " ERROR in \"" + fileName + "\" at line " + lineNr + ':');
		messages.add(errorMessage);
	}


	/**
	 * @param line
	 * @param mapElements
	 * @param depth
	 * @param lineNr
	 * @return
	 */
	private static boolean getSubProcess(String line, ArrayList mapElements, int depth, int lineNr)
	{
		String label = line.trim();
		Process process = new Process(label, depth, lineNr);
		mapElements.add(process);
		return true;
	}

	/**
	 * @param line
	 * @param mapElements
	 * @param depth
	 * @param lineNr
	 * @return
	 */
	private boolean getTask(String line, ArrayList mapElements, int depth, int lineNr)
	{
        if (line.indexOf("INVOKE") == 0)
		{
			boolean async = false;
			String taskName = null;

			if (line.indexOf("INVOKE ASYNC") == 0)
			{
				taskName = line.substring(12).trim();
				async = true;
			}
			else
			{
				taskName = line.substring(6).trim();
			}

			Invocation task = new Invocation(assembly, taskName, depth, lineNr, async);
			mapElements.add(task);
			return true;
		}
		return false;
	}

	/**
	 * @param lineIn
	 * @param mapElements
	 * @param depth
	 * @param lineNr
	 * @return
	 */
	private boolean getResponseWriter(String lineIn, ArrayList mapElements, int depth, int lineNr)
	{
		String line = lineIn;
        if (line.startsWith("REDIRECT"))
        {
            return mapElements.add(getRedirect(lineIn, depth, lineNr, line));
        }
        else if (line.startsWith("DISPATCH"))
        {
            return mapElements.add(getDispatch(lineIn, depth, lineNr, line));
        }
        else if (line.startsWith("RESPOND"))
        {
            return mapElements.add(getResponseWriter(lineIn, depth, lineNr, line));
        }
		return false;
	}

    private MapElement getResponseWriter(String lineIn, int depth, int lineNr, String line) {
        MapElement responseWriter;
        line = line.substring(7).trim();
        //separate command from arguments inside ()
//				String[] command = disectCommandLine(line);
        //line now contains a reference to the thing that produces a response
        if(line.length() == 0)
        {
            reportError(loadMessages, fileName, lineNr, lineIn, "response must have an argument");
            responseWriter = new UnparsableLine("response must have an argument", depth, lineNr);
        }
        else
        {
            responseWriter = new ResponseWritingInvocation(assembly, line, depth, lineNr);
        }
        return responseWriter;
    }

    private MapElement getDispatch(String lineIn, int depth, int lineNr, String line) {
        MapElement responseWriter;
        line = line.substring(8).trim();
        //separate command from arguments inside ()
        String[] command = disectCommandLine(line);
        if(command.length == 0)
        {
            reportError(loadMessages, fileName, lineNr, lineIn, "response must have an argument");
            responseWriter = new UnparsableLine("response must have an argument", depth, lineNr);
        }
        else
        {
            //line now contains a reference to the thing that produces a response
            responseWriter = new Dispatch(command, depth, lineNr);
        }
        return responseWriter;
    }

    private MapElement getRedirect(String lineIn, int depth, int lineNr, String line) {
        MapElement responseWriter;//indicates that request parameters should be forwarded to redirected target
        boolean copyParameters = false;
        //indicates that the target to which is redirected should be approached in some secure mode
        boolean switchSecure = false;
        //indicates that the target to which is redirected may be approached in an insecure mode
        boolean switchInsecure = false;

        line = line.substring(8).trim();
        //separate command from arguments inside ()
        //TODO don't
        String[] command = disectCommandLine(line);
        //line now contains a reference to the thing that produces a response
        if(command.length == 0)
        {
            reportError(loadMessages, fileName, lineNr, lineIn, "response must have an argument");
            responseWriter = new UnparsableLine("response must have an argument", depth, lineNr);
        }
        else
        {
            responseWriter = new Redirect(command, /*switchSecure, switchInsecure, copyParameters,*/ depth, lineNr);
        }
        return responseWriter;
    }

    /**
	 * @param line
	 * @param mapElements
	 * @param depth
	 * @param lineNr
	 * @return
	 */
	private boolean getResult(String line, ArrayList mapElements, int depth, int lineNr)
	{
		int colon = -1;
		int equaloffset = -1;

		if (line.indexOf("RESULT") == 0 || line.indexOf("TRUE") == 0 || line.indexOf("FALSE") == 0 || line.indexOf("ERROR") == 0)
		{
			colon = line.indexOf(':');
			String resultValue = null;
			int operatorType = InvocationResultExpression.EQ;
			if (line.indexOf("TRUE") == 0)
			{
				resultValue = "true";
			}
			else if (line.indexOf("FALSE") == 0)
			{
				resultValue = "false";
			}
			else if (line.indexOf("ERROR") == 0)
			{
				resultValue = "false";
			}
			else if (line.indexOf("RESULT") == 0)
			{
				int operatorSize = 0;

				if ((equaloffset = line.indexOf("==")) != -1)
				{
					operatorSize = 2;
					operatorType = InvocationResultExpression.EQ;
				}
				else if ((equaloffset = line.indexOf("!=")) != -1)
				{
					operatorSize = 2;
					operatorType = InvocationResultExpression.NE;
				}
				else if ((equaloffset = line.indexOf('>')) != -1)
				{
					operatorSize = 1;
					operatorType = InvocationResultExpression.GT;
				}
				else if ((equaloffset = line.indexOf('<')) != -1)
				{
					operatorSize = 1;
					operatorType = InvocationResultExpression.LT;
				}
				else if ((equaloffset = line.indexOf(">=")) != -1)
				{
					operatorSize = 2;
					operatorType = InvocationResultExpression.GE;
				}
				else if ((equaloffset = line.indexOf("<=")) != -1)
				{
					operatorSize = 2;
					operatorType = InvocationResultExpression.LE;
				}
				else if ((equaloffset = line.indexOf('=')) != -1)
				{
					operatorSize = 1;
					operatorType = InvocationResultExpression.EQ;
				}
				if (colon != -1)
				{
					//TODO indexoutofboundsexception for 'RESULT empty: DISPATCH /practice/question.jsp'
					resultValue = line.substring(equaloffset + operatorSize, colon).trim();
				}
				else
				{
					resultValue = line.substring(equaloffset + operatorSize).trim();
				}
				if (equaloffset == -1 || equaloffset > colon)
				{
					UnparsableLine unparsableLine = new UnparsableLine("Syntax error", depth, lineNr);
					loadMessages.add(unparsableLine.toString());
					mapElements.add(unparsableLine);
					return false;
				}
			}

			InvocationResultExpression resultExpression = new InvocationResultExpression(resultValue, operatorType, depth, lineNr);
			mapElements.add(resultExpression);

			if (colon != -1)
			{
				line = line.substring(colon + 1).trim();
				getTask(line, mapElements, depth + 1, lineNr);
				getResponseWriter(line, mapElements, depth + 1, lineNr);
			}
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param line
	 * @param mapElements
	 * @param depth
	 * @param lineNr
	 * @return
	 */
	private static boolean getExceptionHandler(String line, ArrayList mapElements, int depth, int lineNr)
	{
		String line1 = line;
		if (line1.indexOf("CATCH") == 0)
		{
			line1 = line1.substring(5).trim();
			ExceptionHandler handler = new ExceptionHandler(line1, depth, lineNr);
			mapElements.add(handler);
			return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	private Process loadFlow()
	{
		loadMessages.clear();
		isLoaded = true;
//		ArrayList discoveredMapElements = new ArrayList();
//		ArrayList lines = new ArrayList();
		rootProcess = new Process(this.mappingName, -1, 0);
		discoveredMapElements.add(rootProcess);

		try
		{
			InputStream input;

			File file = new File(fileName);
			if(file.exists())
			{
				input = new FileInputStream(file);
			}
			else
			{
				//load file from classpath
				input = getClass().getClassLoader().getResourceAsStream(fileName);
			}
/*			if(input == null)
			{
				file = new File(/ *Environment.currentApplication().getWorkingDirectory() +* / fileName);
				if(file.exists())
				{
					input = new FileInputStream(file);
				}
			}    */
			if(input == null)
			{
				throw new ConfigurationException("can not load flow: file '" + fileName + "' can not be found at exact location or classpath");
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(input));

//			RandomAccessFile raf;
			String line;
			int lineCount = 0;

//			raf = new RandomAccessFile(fileName, "r");

			readline:
			do
			{
				line = reader.readLine();
				String lineSav = line;

				if (line != null)
				{
					lineCount++;

					//determine the depth of the String
					int depth = 0;
					while ((line.length() > depth) && Character.isWhitespace(line.charAt(depth)))
					{
						if(line.charAt(depth) == '\t')
						{
							this.tabsUsedAsIndentation = true;
						}
						else if(line.charAt(depth) == ' ')
						{
							this.spacesUsedAsIndentation = true;
						}


						depth++;
					}

					line = line.trim();

					//save line for error messages
					lines.add(line);

					if (line.startsWith("#") || line.startsWith("//") || line.startsWith(";"))
					{
						//comment
						continue readline;
					}

					if (line.length() > 0)
					{
/*						if (getForm(line, discoveredMapElements, depth, lineCount))
						{
							continue readline;
						}*/
/*						if (getEvent(line, discoveredMapElements, depth, lineCount))
						{
							continue readline;
						}*/
						if (getTask(line, discoveredMapElements, depth, lineCount))
						{
							continue readline;
						}
						if (getResult(line, discoveredMapElements, depth, lineCount))
						{
							continue readline;
						}
						if (getExceptionHandler(line, discoveredMapElements, depth, lineCount))
						{
							continue readline;
						}
						if (getResponseWriter(line, discoveredMapElements, depth, lineCount))
						{
							continue readline;
						}
						if (getSubProcess(line, discoveredMapElements, depth, lineCount))
						{
							continue readline;
						}

						//line contains rubbish
						isLoaded = false;
						reportError(loadMessages, fileName, lineCount, line, "syntax error");
					}
					//line empty
				}
			}
			while (line != null);

			input.close();

			Iterator i = discoveredMapElements.iterator();
			lineCount = 0;

			loop:
			while (i.hasNext())
			{
				MapElement fe = (MapElement) i.next();
				if (fe instanceof UnparsableLine)
				{
					isLoaded = false;
					reportError(loadMessages, fileName, fe.getLineNr(), ((String) lines.get(fe.getLineNr() - 1)), fe.getArgument());
					continue loop;
				}
				MapElement parent = getParent(discoveredMapElements, lineCount, fe.getDepth());
				if (parent == null)
				{
					rootProcess.addFlowElement(fe);
					fe.setParent(rootProcess);
/*					if (fe instanceof ActionBundle)
					{
						//if a form by this name already exists, it's an error
						if (forms.containsKey(mappingName + '.' + fe.getLabel()))
						{
							isLoaded = false;
							reportError(fileName, fe.getLineNr(), ((String) lines.get(fe.getLineNr() - 1)), "multiple mapping of form");
						}
						forms.put(mappingName + '.' + fe.getLabel(), fe);
					}
					else
					{
						isLoaded = false;
						reportError(fileName, fe.getLineNr(), ((String) lines.get(fe.getLineNr() - 1)), "mvc element " + getFlowElementDescription(fe) + " must be part of another element");
					}*/
				}
				else
				{
					if (!parent.addFlowElement(fe))
					{
						isLoaded = false;

						if (parent.isTerminated())
						{
							reportError(loadMessages, fileName, fe.getLineNr(), ((String) lines.get(fe.getLineNr() - 1)), "unreachable statement");
						}
						else
						{
							reportError(loadMessages, fileName, fe.getLineNr(), ((String) lines.get(fe.getLineNr() - 1)), "can not add " + getFlowElementDescription(fe) + " to " + getFlowElementDescription(parent));
						}
					}
					else
					{
						fe.setParent(parent);
					}
				}

				lineCount++;
			}
			return rootProcess;
		}
		catch (IOException ioe)
		{
			System.out.println(new LogEntry(Level.CRITICAL, "", ioe));
			isLoaded = false;
			loadMessages.add("Loading of mapping '" + mappingName + "' resulted in exception with message: " + ioe.getMessage());

			return null;
		}
	}

	public Process getRootProcess()
	{
		return rootProcess;
	}

	public boolean checkSanity(RequestDispatcher handle)
	{
		boolean ok  = true;
		//perform sanity check
		//errors and results must have children
		//warning if MVC does not result in redirect
		//warning for orphan code
		sanityCheckMessages.clear();

		Iterator i = discoveredMapElements.iterator();
		while (i.hasNext())
		{
			MapElement fe = (MapElement) i.next();
			String message = fe.check(handle);
			if (message != null)
			{
				ok = false;
				reportError(sanityCheckMessages, fileName, fe.getLineNr(), ((String) lines.get(fe.getLineNr() - 1)), message);
			}
		}
		if(this.tabsUsedAsIndentation && this.spacesUsedAsIndentation)
		{
			sanityCheckMessages.add("WARNING: both SPACEs and TABs used for indentation; this may cause unexpected behaviour!");
		}
		sanityCheckMessages.add(isLoaded ? "Loading of mapping '" + mappingName + "' succeeded" : "Loading of mapping '" + mappingName + "' failed with messages:\n" + loadMessages);
		if(isLoaded)
		{
			sanityCheckMessages.add("Sanity check " + (ok ? "passed" : "NOT passed"));
		}

		return isLoaded && ok;
	}

	/**
	 * @param mapElements
	 * @param index
	 * @param depth
	 * @return
	 */
	private static MapElement getParent(ArrayList mapElements, int index, int depth)
	{
		for (int i = index; i >= 1; i--)
		{
			MapElement fe = (MapElement) mapElements.get(i);

			if (fe.getDepth() == (depth - 1))
			{
				return fe;
			}
			else
			{
				if (fe.getDepth() < (depth - 1))
				{
					return null;
				}
			}
		}

		return null;
	}


	/**
	 * @return a text describing the mvc tree
	 */
	public String toString()
	{
		return mappingName + " -> " + this.rootProcess.toString();
	}


	public List getSanityCheckMessages()
	{
		return sanityCheckMessages;
	}


	/**
	 * Expects a command and 0 or more arguments like so:
	 * command(argument1,argument2,argument3)
	 *
	 * @param line Command line
	 * @return String list, #0 is the command, the rest are arguments
	 */
	public static String[] disectCommandLine(String line)
	{
		return StringSupport.split(line, "() ,;", "\"", false, false, false).toArray(new String[0]);
	}

}

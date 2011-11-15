/*
 * Copyright 2011 Jeroen Meetsma
 *
 *
 * This file is part of Iglu.
 *
 * Iglu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Iglu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.ijsberg.iglu.invocation;

import org.ijsberg.iglu.Configuration;
import org.ijsberg.iglu.util.misc.StringSupport;

import java.lang.reflect.InvocationTargetException;

/**
 */
public class CommandLineProcessor {
	private Configuration configuration;

	public CommandLineProcessor(Configuration configuration) {
		this.configuration = configuration;
	}

	/**
	 * Splits command-line in command and arguments.
	 * Expects a command and 0 or more arguments like so:
	 * command(argument1,argument2,argument3)
	 *
	 * @param line Command line
	 * @return String list, #0 is the command, the rest are arguments
	 */
	public static String[] splitCommandLine(String line) {
		return (String[]) StringSupport.split(line, "()", "").toArray(new String[0]);
	}

	public static String[] splitArguments(String line) {
		return (String[]) StringSupport.split(line, ", ", "\"").toArray(new String[0]);
	}

	/**
	 * Splits command in cluster ID, module ID and method name.
	 *
	 * @param line
	 * @return
	 */
	public static String[] splitCommand(String line) {
		return (String[]) StringSupport.split(line, ".", "\"").toArray(new String[0]);
	}

	/**
	 * @param commandLine
	 * @return
	 */
	protected Object processCommandLine(String commandLine) throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

		String[] commandAndArguments = splitCommandLine(commandLine);

		String[] moduleAndMethodIds = splitCommand(commandAndArguments[0]);

		String[] arguments;
		if(commandAndArguments.length > 1) {
			arguments = splitArguments(commandAndArguments[1]);
		} else {
			arguments = new String[0];
		}

		if (moduleAndMethodIds.length < 2) {
			throw new IllegalArgumentException("can not process command line: '" + commandLine + "'\nmissing method");
		}
		return invoke(moduleAndMethodIds[0], moduleAndMethodIds[1], moduleAndMethodIds[2], arguments);
	}

	protected Object invoke(String clusterId, String moduleId, String methodName, Object... arguments) throws InvocationTargetException, NoSuchMethodException {
		return configuration.getClusters().get(clusterId).getInternalComponents().get(moduleId).invoke(methodName, arguments);
	}
}

/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
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

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.server.invocation.AssemblyCommandLine;

import java.lang.reflect.InvocationTargetException;

/**
 */
public class CommandLineProcessor {
	private Assembly assembly;

	public CommandLineProcessor(Assembly assembly) {
		this.assembly = assembly;
	}


	/**
	 * @param commandLine
	 * @return
	 */
	protected Object processCommandLine(String commandLine) throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
		AssemblyCommandLine command = new AssemblyCommandLine(commandLine);
		return invoke(command.getClusterId(), command.getComponentId(), command.getMethodName(), command.getArguments());
	}

	protected Object invoke(String clusterId, String moduleId, String methodName, Object... arguments) throws InvocationTargetException, NoSuchMethodException {
		//TODO provide more precise exception than NullPointers if statement fails
		return assembly.getClusters().get(clusterId).getInternalComponents().get(moduleId).invoke(methodName, arguments);
	}
}

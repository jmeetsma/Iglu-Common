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

package org.ijsberg.iglu.server.invocation;

import org.ijsberg.iglu.server.connection.ClientSessionAware;
import org.ijsberg.iglu.server.connection.CommandLineClientAdapter;
import org.ijsberg.iglu.server.connection.CommandLineInterpreter;
import org.ijsberg.iglu.util.misc.StringSupport;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Provides a basic command-line interface.
 */
public class CommandLineObjectInvoker implements CommandLineInterpreter {

	private Object invokable;
	private CommandLineClientAdapter adapter;

	/**
	 * @param invokable
	 */
	public CommandLineObjectInvoker(Object invokable) {
		this.invokable = invokable;
	}

	/**
	 * @param adapter
	 */
	@Override
	public void initiateSession(CommandLineClientAdapter adapter) {
		this.adapter = adapter;
		if (invokable instanceof ClientSessionAware) {
			((ClientSessionAware) invokable).setSession(this);
		}
		adapter.send("Welcome. You may invoke public methods on " + invokable.toString() + ".\n\r>");
	}

	/**
	 * @param message
	 */
	@Override
	public void onAdapterTermination(String message) {
	}

	/**
	 * @param rawInput input from the client connection
	 */
	@Override
	public void processRawInput(byte[] rawInput) {
	}

	/**
	 * @param commandLine input from the client connection
	 * @return
	 */
	@Override
	public String processCommandLine(String commandLine) {
		String response = "";
		if (!"".equals(commandLine)) {
			try {
				Object returnValue = executeCommandLine(commandLine);
				response = (returnValue != null ? returnValue : "ok") + "\r\n";

			} catch (NoSuchMethodException e) {
				response = "no such method\r\n";
			} catch (Exception e) {
				//invokable is not trusted and may throw anything
				response = StringSupport.getStackTrace(e) + "\r\n";
			}
		}
		return response + ">";
	}

	private Object executeCommandLine(String commandLine) throws NoSuchMethodException, InvocationTargetException {
		String[] commandAndArguments = AssemblyCommandLine.splitCommandLine(commandLine);
		String command = commandAndArguments[0];
		Object[] arguments;
		if (commandAndArguments.length > 1) {
			arguments = AssemblyCommandLine.splitArguments(commandAndArguments[1]);
		} else {
			arguments = new String[0];
		}
		return ReflectionSupport.invokeMethod(invokable, command, arguments);
	}

	/**
	 *
	 */
	@Override
	public void abortSubProcessMode() {
	}

	@Override
	public boolean isInSubProcessMode() {
		return false;
	}

	/**
	 * @param unfinishedCommand
	 * @return
	 */
	@Override
	public String completeCommand(String unfinishedCommand) {
		Method[] invokableMethods = invokable.getClass().getMethods();
		for (int i = 0; i < invokableMethods.length; i++) {
			if (invokableMethods[i].getName().startsWith(unfinishedCommand)) {
				return invokableMethods[i].getName() + "(";
			}
		}
		return unfinishedCommand;
	}

	/**
	 *
	 */
	@Override
	public void sendMessage(String message) {
		adapter.send(message);
	}

	/**
	 *
	 */
	@Override
	public void terminate() {
		adapter.terminateSession();
	}
}

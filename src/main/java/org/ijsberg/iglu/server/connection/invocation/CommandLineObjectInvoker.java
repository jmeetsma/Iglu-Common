package org.ijsberg.iglu.server.connection.invocation;

import org.ijsberg.iglu.invocation.CommandLineProcessor;
import org.ijsberg.iglu.server.connection.ClientSessionAware;
import org.ijsberg.iglu.server.connection.CommandLineClientAdapter;
import org.ijsberg.iglu.server.connection.CommandLineInterpreter;
import org.ijsberg.iglu.util.collection.ArraySupport;
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
	 *
	 * @param invokable
	 */
	public CommandLineObjectInvoker(Object invokable) {
		this.invokable = invokable;
	}

	/**
	 *
	 * @param adapter
	 */
	@Override
	public void initiateSession(CommandLineClientAdapter adapter) {
		this.adapter = adapter;
		if(invokable instanceof ClientSessionAware) {
			((ClientSessionAware) invokable).setSession(this);
		}
		adapter.send("Welcome. You may invoke public methods on " + invokable.toString() + ".\n\r>");
	}

	/**
	 *
	 * @param message
	 */
	@Override
	public void onAdapterTermination(String message) {
	}

	/**
	 *
	 * @param rawInput input from the client connection
	 */
	@Override
	public void processRawInput(byte[] rawInput) {
	}

	/**
	 *
	 * @param commandLine input from the client connection
	 * @return
	 */
	@Override
	public String processCommandLine(String commandLine) {
		String response = "";
		if(!"".equals(commandLine))
		{
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
		String[] commandAndArguments = CommandLineProcessor.splitCommandLine(commandLine);
		String command = commandAndArguments[0];
		Object[] arguments;
		if(commandAndArguments.length > 1) {
			arguments = CommandLineProcessor.splitArguments(commandAndArguments[1]);
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
	 *
	 * @param unfinishedCommand
	 * @return
	 */
	@Override
	public String completeCommand(String unfinishedCommand) {
		Method[] invokableMethods = invokable.getClass().getMethods();
		for(int i = 0; i < invokableMethods.length; i++) {
			if(invokableMethods[i].getName().startsWith(unfinishedCommand)) {
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

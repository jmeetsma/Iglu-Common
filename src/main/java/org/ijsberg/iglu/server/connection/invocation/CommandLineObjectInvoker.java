package org.ijsberg.iglu.server.connection.invocation;

import org.ijsberg.iglu.invocation.CommandLineProcessor;
import org.ijsberg.iglu.server.connection.ClientSessionAware;
import org.ijsberg.iglu.server.connection.CommandLineClientAdapter;
import org.ijsberg.iglu.server.connection.CommandLineInterpreter;
import org.ijsberg.iglu.util.collection.ArraySupport;
import org.ijsberg.iglu.util.misc.StringSupport;
import org.ijsberg.iglu.util.reflection.ReflectionSupport;

import java.lang.reflect.Method;

/**
 */
public class CommandLineObjectInvoker implements CommandLineInterpreter {

	private Object invokable;
	private CommandLineClientAdapter adapter;

	public CommandLineObjectInvoker(Object invokable) {
		this.invokable = invokable;
	}

	@Override
	public void initiateSession(CommandLineClientAdapter adapter) {
		this.adapter = adapter;
		if(invokable instanceof ClientSessionAware) {
			((ClientSessionAware) invokable).setSession(this);
		}
		adapter.send("Welcome. You may invoke public methods on " + invokable.toString() + ".\n\r");
	}

	@Override
	public void onAdapterTermination(String message) {
	}

	@Override
	public void processRawInput(byte[] rawInput) {
	}

	@Override
	public String processCommandLine(String commandLine) {
		try {
			System.out.println(commandLine);
			String[] commandAndArguments = CommandLineProcessor.splitCommandLine(commandLine);
			String command = commandAndArguments[0];
			System.out.println("-> " + ArraySupport.format(commandAndArguments, "<->"));
			Object[] arguments;
			if(commandAndArguments.length > 1) {
				arguments = CommandLineProcessor.splitArguments(commandAndArguments[1]);
			} else {
				arguments = new String[0];
			}
			return ReflectionSupport.invokeMethod(invokable, command, arguments) + "\n";
		} catch (Exception e) {
			//invokable is not trusted and may throw anything
			return StringSupport.getStackTrace(e) + "\n";
		}
	}

	@Override
	public void abortSubProcessMode() {
	}

	@Override
	public boolean isInSubProcessMode() {
		return false;
	}

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

	@Override
	/**
	 *
	 */
	public void sendMessage(String message) {
		adapter.send(message);
	}

	@Override
	/**
	 *
	 */
	public void terminate() {
		adapter.terminateSession();
	}
}

package org.ijsberg.iglu.invocation;

import org.ijsberg.iglu.Configuration;
import org.ijsberg.iglu.util.collection.ArraySupport;
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

		System.out.println("processing command-line: '" + commandLine + "'");

		String[] commandAndArguments = splitCommandLine(commandLine);

		String[] moduleAndMethodIds = splitCommand(commandAndArguments[0]);
		String[] arguments = splitArguments(commandAndArguments[1]);

		System.out.println(ArraySupport.format("[", arguments, "]") + "]");

		if (moduleAndMethodIds.length < 2) {
			throw new IllegalArgumentException("can not process command line: '" + commandLine + "'\nmissing method");
		}
		return invoke(moduleAndMethodIds[0], moduleAndMethodIds[1], moduleAndMethodIds[2], arguments);
	}

	protected Object invoke(String clusterId, String moduleId, String methodName, Object... arguments) throws InvocationTargetException, NoSuchMethodException {
		return configuration.getClusters().get(clusterId).getInternalModules().get(moduleId).invoke(methodName, arguments);
	}
}

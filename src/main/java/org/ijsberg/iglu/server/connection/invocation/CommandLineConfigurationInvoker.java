package org.ijsberg.iglu.server.connection.invocation;


import org.ijsberg.iglu.Configuration;
import org.ijsberg.iglu.invocation.CommandLineProcessor;
import org.ijsberg.iglu.server.connection.CommandLineClientAdapter;
import org.ijsberg.iglu.server.connection.CommandLineInterpreter;
import org.ijsberg.iglu.util.formatting.PatternMatchingSupport;
import org.ijsberg.iglu.util.misc.StringSupport;


/**
 * Hooks up an adapter directly to an agent so that all its exposed methods may be
 * invoked by command line.
 */
public class CommandLineConfigurationInvoker extends CommandLineProcessor implements CommandLineInterpreter {

	public static final String CSV_PATTERN = "(|([0-9]+|\".*[^\"]\")(,[0-9]+|,\".*[^\"]\")*)";
	public static final String CL_PATTERN = "[A-Za-z_][A-Za-z0-9_]*\\(" + CSV_PATTERN + "\\)";

	private CommandLineClientAdapter adapter;

	/**
	 *
	 * @param line
	 * @return
	 */
	static boolean isCommand(String line) {
		return PatternMatchingSupport.valueMatchesRegularExpression(line, CL_PATTERN);
	}

	static boolean isCSVArray(String line) {
		return PatternMatchingSupport.valueMatchesRegularExpression(line, CSV_PATTERN);
	}


	/**
	 *
	 */
	public CommandLineConfigurationInvoker(Configuration configuration) {
		super(configuration);
	}


	/**
	 * @param adapter
	 */
	public void initiateSession(CommandLineClientAdapter adapter) {
		this.adapter = adapter;
	}

	/**
	 * Releases user request.
	 *
	 * @param string
	 */
	public void onAdapterTermination(String string) {
	}

	/**
	 * Does nothing.
	 *
	 * @param bytes
	 */
	public void processRawInput(byte[] bytes) {
	}

	/**
	 * Processes a client command line.
	 * The response consists of a few lines.
	 * The first line is either 'OK' or an 'ERROR',
	 * followed by the result.toString() or respectively
	 * an error message.
	 * Collections are printed as a series of lines prifixed by dashes.
	 *
	 * @param commandLine
	 * @return
	 */
	public String processCommandLine(String commandLine) {
		StringBuffer response = new StringBuffer();
		try {
				response.append(super.processCommandLine(commandLine));
		} catch (Throwable t) {
			response.append(StringSupport.getStackTrace(t, 5) + "\n");
		}
		return response.toString();
	}

	/**
	 * Does nothing.
	 */
	public void abortSubProcessMode() {
	}

	/**
	 * @return false
	 */
	public boolean isInSubProcessMode() {
		return false;
	}

	/**
	 * @param unfinishedCommand
	 * @return null
	 */
	public String completeCommand(String unfinishedCommand) {
		return null;
	}

	/**
	 * @param message
	 */
	public synchronized void sendMessage(String message) {
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

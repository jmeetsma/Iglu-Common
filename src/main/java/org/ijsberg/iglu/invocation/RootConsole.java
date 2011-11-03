package org.ijsberg.iglu.invocation;

import org.ijsberg.iglu.Configuration;
import org.ijsberg.iglu.exception.ResourceException;
import org.ijsberg.iglu.logging.Level;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.runtime.Startable;
import org.ijsberg.iglu.util.io.FileSupport;

import java.io.*;
import java.util.Properties;

/**
 * Processes command-line instructions that are read from an input file.
 * Writes results to an output file.
 * <p/>
 * Configuration may provide:
 * <ul>
 * <li>input_file_location (default: console/input.txt)</li>
 * <li>output_file_location (default: console/output.txt)</li>
 * <li>temp_output_file_location (default: console/tempoutput.txt)</li>
 * </ul>
 * <p/>
 * The output file is temporary during the processing phase. It gets renamed to its
 * final name once processing is done. The appearance of the (final) output file may act as
 * a trigger for scripts to display or evaluate the result.
 * The instructions passed are considered to be internal requests.
 * Access to the input file should therefore be restricted to the OS-user running the application.
 */
public class RootConsole extends CommandLineProcessor implements Startable, Runnable {
	private Thread thread;
	private boolean isRunning = false;
	private BufferedReader commandLineInput;
	private String baseDir = ".";
	private String inputFileLocation = "console/input.txt";
	private String outputFileLocation = "console/output.txt";
	private String tempOutputFileLocation = "console/tempoutput.txt";

	public RootConsole(Configuration configuration) {
		super(configuration);
	}


	/**
	 * Determines file locations.
	 */
	public void setProperties(Properties properties) {
		baseDir = properties.getProperty("base_dir", baseDir);
		inputFileLocation = baseDir + '/' + properties.getProperty("input_file_location", inputFileLocation);
		outputFileLocation = baseDir + '/' + properties.getProperty("output_file_location", outputFileLocation);
		tempOutputFileLocation = baseDir + '/' + properties.getProperty("temp_output_file_location", tempOutputFileLocation);
	}

	/**
	 * Starts monitoring the input file.
	 */
	public void start() {
		//thread must have rights to process any request
		try {
			openFileReader();
		}
		catch (IOException ioe) {
			throw new ResourceException("can not open file reader", ioe);
		}
		thread = new Thread(this);
		isRunning = true;
		thread.start();
	}

	public boolean isStarted() {
		return isRunning;
	}

	/**
	 * Stops monitoring the input file.
	 */
	public void stop() {
		isRunning = false;
		thread.interrupt();
		try {
			closeFileReader();
		}
		catch (IOException ioe) {
			System.out.println(new LogEntry(Level.CRITICAL, "I/O exception while closing file reader: " + ioe.getMessage(), ioe));
		}
	}

	/**
	 *
	 */
	private void openFileReader() throws IOException {
		File inputFile = FileSupport.createFile(inputFileLocation);
		inputFile.delete();
		inputFile.createNewFile();
		commandLineInput = new BufferedReader(new FileReader(inputFile));
	}

	/**
	 *
	 */
	private void resetFileReader() throws IOException {
		closeFileReader();
		openFileReader();
	}

	/**
	 *
	 */
	private void closeFileReader() throws IOException {
		if (commandLineInput != null) {
			commandLineInput.close();
		}
	}

	/**
	 *
	 */
	public void run() {
		try {
			while (isRunning) {
				String commandLine = commandLineInput.readLine();
				if (commandLine != null) {
					processCommandLineSafely(commandLine);
				}
				try {
					Thread.sleep(25);
				}
				catch (InterruptedException ie) {
					System.out.println(new LogEntry("console interrupted"));
					isRunning = false;
				}
			}
		}
		catch (IOException ioe) {
			System.out.println(new LogEntry(Level.CRITICAL, "console interrupted by I/O exception: " + ioe.getMessage(), ioe));
			stop();
		}
	}

	private void processCommandLineSafely(String commandLine) throws IOException {
		Object result;
		try {
			result = processCommandLine(commandLine);
			System.out.println(new LogEntry("command line '" + commandLine + "' successfully processed"));
		}
		catch (Throwable t) {
			result = t;
			System.out.println(new LogEntry(Level.CRITICAL, "console can not process command line '" + commandLine + "'", t));
		}
		writeResult(result);
	}

	/**
	 * Writes result to the output file.
	 *
	 * @param result
	 */
	private void writeResult(Object result) throws IOException {
		System.out.println("writing result...");
		File outputFile = new File(outputFileLocation);
		File tempfile = FileSupport.createFile(tempOutputFileLocation);
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(tempfile);
			PrintStream ps = new PrintStream(new FileOutputStream(tempfile));
			if (result != null) {
				if (result instanceof Throwable) {
					((Throwable) result).printStackTrace(ps);
				}
				ps.println(result.toString());
			}
			ps.close();
		}
		finally {fos.close();}

		if (outputFile.exists()) {
			System.out.println("writing result...0");
			boolean success = outputFile.delete();
			System.out.println("writing result...1 " + success);
		}

		boolean success = tempfile.renameTo(new File(outputFileLocation));
	}
}

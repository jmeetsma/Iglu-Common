package org.ijsberg.iglu.server.connection.invocation;

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class CommandLineConfigurationInvokerTest
{
	@Test
	public void isCommandTest() throws Exception {
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp()"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("start_app()"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("start"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("()"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("start)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("start-app()"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("start15()"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("s2tart15()"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("1start()"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp();"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(())"));
	}

	@Test
	public void isCommandWithNumericArgsTest() throws Exception {
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(20)"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(20,7)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(20, 7)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(20,)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(,20)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(,20,7)"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(20,7,3)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(20,7,)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(20,7,hop)"));
	}

	@Test
	public void isCommandWithStringAndNumericArgsTest() throws Exception {
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(\"test\")"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(\"test\",3)"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(20,\"test\")"));

		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp( \"test\")"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(\"test\" ,3)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(20 ,\"test\")"));

		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp( \"te\"st\")"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(\"te\"st\" ,3)"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCommand("startApp(20 ,\"te\"st\")"));

		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(\"te st\")"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(\"te st\",3)"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCommand("startApp(20,\"te st\")"));
	}

	@Test
	public void isArrayTest() {
		Assert.assertTrue(CommandLineConfigurationInvoker.isCSVArray("20"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCSVArray("20,3"));
		Assert.assertFalse(CommandLineConfigurationInvoker.isCSVArray(",20)"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCSVArray(""));

		Assert.assertTrue(CommandLineConfigurationInvoker.isCSVArray("\"twenty\",3"));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCSVArray("20,\"three\""));
		Assert.assertTrue(CommandLineConfigurationInvoker.isCSVArray("\"twe\"nty\",3"));
	}

}

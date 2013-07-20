/*
 * Copyright 2011-2013 Jeroen Meetsma - IJsberg
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

import org.junit.Assert;
import org.junit.Test;

/**
 */
public class CommandLineConfigurationInvokerTest {
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

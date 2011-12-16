package org.ijsberg.iglu.invocation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.module.StandardCluster;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.sample.configuration.TestAssembly;
import org.ijsberg.iglu.sample.configuration.TestObject;
import org.junit.Test;

/**
 */
public class CommandLineProcessorTest {

	protected TestAssembly assembly;




	protected void setUpAssembly() {

		assembly = new TestAssembly();
		Cluster cluster = new StandardCluster();



		Component module = new StandardComponent(new TestObject("Hello "));
		cluster.connect("object", module);

		assembly.addCluster("cluster", cluster);
	}

	@Test
	public void testSplitCommandLine() throws Exception {

		String[] command = CommandLineProcessor.splitCommandLine("service.agent.process(14)");
		assertEquals("service.agent.process", command[0]);
		assertEquals("14", command[1]);

		command = CommandLineProcessor.splitCommandLine("service.agent.process(14, 12, \"hello world\") ");
		assertEquals("service.agent.process", command[0]);
		assertEquals("14, 12, \"hello world\"", command[1]);
	}

	@Test
	public void testSplitArguments() throws Exception {

		String[] arguments = CommandLineProcessor.splitArguments("14, \"some kind of string\", 20");
		assertEquals("14", arguments[0]);
		assertEquals("some kind of string", arguments[1]);
		assertEquals("20", arguments[2]);

	}

	@Test
	public void testSplitCommand() throws Exception {
		String[] command = CommandLineProcessor.splitCommand("service.agent.process");
		assertEquals("service", command[0]);
		assertEquals("agent", command[1]);
		assertEquals("process", command[2]);
	}

	@Test
	public void testProcessCommandLine() throws Exception {
		setUpAssembly();
		CommandLineProcessor processor = new CommandLineProcessor(assembly);
		Object result = processor.processCommandLine("cluster.object.getMessage(\"World!\")");
		assertEquals("Hello World!", result);

		try {
			processor.processCommandLine("cluster.object.bogus()");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {};

		try {
			processor.processCommandLine("cluster.object.getMessage()");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {};

		try {
			processor.processCommandLine("cluster.object.getMessageNotDefinedInInterface(\"World!\")");
			fail("NoSuchMethodException expected");
		} catch (NoSuchMethodException expected) {};

		result = processor.processCommandLine("cluster.object.getMessageInt(10)");
		assertEquals("Hello 10", result);

		try {
			processor.processCommandLine("cluster.object.getMessageInt(\"world?\")");
			fail("IllegalArgumentException expected");
		} catch (IllegalArgumentException expected) {};
	}



}

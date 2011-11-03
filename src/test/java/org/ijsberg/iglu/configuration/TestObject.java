package org.ijsberg.iglu.configuration;

/**
 */
public class TestObject implements TestInterface {

	private String prefix;

	public TestObject(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getMessage(String argument) {
		return prefix + argument;
	}

	@Override
	public String getMessageInt(int argument) {
		return prefix + argument;
	}

	public String getMessageNotDefinedInInterface(String argument) {
		return prefix + argument;
	}
}

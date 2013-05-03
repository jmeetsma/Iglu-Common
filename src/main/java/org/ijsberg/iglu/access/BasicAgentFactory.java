package org.ijsberg.iglu.access;

import java.util.Properties;

/**
 */
public abstract class BasicAgentFactory<T> implements AgentFactory<T> {

	private String agentId;
	private Properties properties;

	public BasicAgentFactory(String agentId) {
		this(agentId, new Properties());
	}

	public BasicAgentFactory(String agentId, Properties properties) {
		this.agentId = agentId;
		this.properties = properties;
	}

	@Override
	public String getAgentId() {
		return agentId;
	}

	@Override
	public abstract T createAgentImpl();

	@Override
	public Properties getAgentProperties() {
		return properties;
	}

}

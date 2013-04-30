package org.ijsberg.iglu.usermanagement.domain;

import org.ijsberg.iglu.usermanagement.Account;

import java.util.Properties;

/**
 */
public class SimpleAccount implements Account {

	private String userId;
	private String hashedPassword;
	private Properties properties = new Properties();


	public SimpleAccount(String userId) {
		this.userId = userId;
	}

	public SimpleAccount(String userId, String hashedPassword, Properties properties) {
		this.userId = userId;
		this.hashedPassword = hashedPassword;
		this.properties = properties;
	}

	public SimpleAccount(String userId, String hashedPassword) {
		this.userId = userId;
		this.hashedPassword = hashedPassword;
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public String getHashedPassword() {
		return hashedPassword;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	@Override
	public void putProperty(String key, String value) {
		properties.put(key, value);
	}

	@Override
	public void removeProperty(String key) {
		properties.remove(key);
	}
}

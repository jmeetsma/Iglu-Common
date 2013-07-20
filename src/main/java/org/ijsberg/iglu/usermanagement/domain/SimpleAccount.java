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

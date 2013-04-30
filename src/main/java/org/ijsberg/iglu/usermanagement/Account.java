package org.ijsberg.iglu.usermanagement;

import java.io.Serializable;
import java.util.Properties;

/**
 */
public interface Account extends Serializable {
	String getUserId();

	String getHashedPassword();

	Properties getProperties();

	void setHashedPassword(String hashedPassword);

	void putProperty(String key, String value);

	void removeProperty(String key);
}

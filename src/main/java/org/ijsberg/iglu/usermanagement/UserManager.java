package org.ijsberg.iglu.usermanagement;

import org.ijsberg.iglu.access.Authenticator;
import org.ijsberg.iglu.access.User;

/**
 */
public interface UserManager {


	void addAccount(String userId, String password);

	void addAccount(User user, String password);
}

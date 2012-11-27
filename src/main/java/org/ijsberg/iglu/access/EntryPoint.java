package org.ijsberg.iglu.access;

import java.util.Properties;


/**
 * Marks the location where a client request enters the
 * application. Implementations are typically http-servlets
 * or -filters.
 * <p/>
 * Implementations are responsible of binding and releasing the
 * user request to their threads of execution.
 *
 * @see Application#bindRequest(EntryPoint)
 * @see Application#releaseRequest()
 */
public interface EntryPoint
{
	/**
	 * Is invoked when a session is created somewhere inside the application.
	 *
	 * @param currentRequest request that must be bound to current thread
	 * @param session the newly created session
	 */
	void onSessionUpdate(Request currentRequest, Session session);

	/**
	 * Is invoked when a session is destroyed somewhere inside the application.
	 *
	 * @param currentRequest request that must be bound to current thread
	 * @param session session that gets destroyed
	 */
	void onSessionDestruction(Request currentRequest, Session session);

	/**
	 * Exports (user) properties to a client, for instance in the form of cookies.
	 *
	 * @param currentRequest request that must be bound to current thread
	 * @param userSettings properties to export to client
	 */
	void exportUserSettings(Request currentRequest, Properties userSettings);

	/**
	 * Imports (user) properties, such as cookies, from a client into a property bundle.
	 *
	 * @param currentRequest request that must be bound to current thread
	 * @param properties property bundle in which imported properties are set
	 */
	void importUserSettings(Request currentRequest, Properties properties);

	/**
	 * @return the realm to which the users entering here, belong
	 */
//	Realm getRealm();
}

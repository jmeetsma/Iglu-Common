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
package org.ijsberg.iglu.access;


import org.ijsberg.iglu.configuration.Component;

import java.util.Collection;
import java.util.Properties;

/**
 * Administers user requests and sessions, which are defined as a coherent series
 * of requests.
 * <p/>
 * A request must temporarily be attached to a thread, so that it may be
 * retrieved at any point in the application during the course of a request.
 */
public interface AccessManager extends Authenticator {
	/**
	 * Note: implementations of this method must be aware of the performance
	 * impact on session management.
	 *
	 * @return a collection of references to current sessions
	 */
	Collection getSessions();

	/**
	 * This method is invoked at the point where a (stateless) request accesses the application.
	 * By doing so, it's possible to track down a request from every point in the application.
	 * This is useful for gathering profiling information and the possibility to perform security
	 * checks in any layer of the application.
	 *
	 * @param entryPoint point of entry that will be informed as soon as a session is
	 *                   registered or unregistered
	 * @return a request attached to the current thread
	 * @see this#getCurrentRequest()
	 * @see this#releaseRequest()
	 */
	Request bindRequest(EntryPoint entryPoint);

	/**
	 * Binds current request to another thread.
	 *
	 * @param thread
	 * @return
	 */
//	Request rebindRequest(Thread thread);

	/**
	 * Performs an unbind of the request.
	 * This method is invoked when a request is processed and leaves the application.
	 *
	 * @return request that was bound to the current thread
	 * @see this#bindRequest(EntryPoint)
	 */
	Request releaseRequest();

	/**
	 * Binds an internal process, such as asynchronous cleanup,
	 * the same way as a user request.
	 * Internal processes must be bound to ensure they have
	 * full access to resources.
	 * Implementations may ensure that this method is invoked only
	 * by other internal requests or a root request.
	 *
	 * @param realm the Realm the internal process (virtually) resides in
	 * @param thread thread used by the process
	 * @param processDescription description of the internal process
	 * @return a request attached to the thread the internal process uses
	 * @see this#bindRequest(EntryPoint)
	 */
	//Request bindInternalProcess(Thread thread, String processDescription);

	/**
	 * Creates a new session containing user preferences such as a language setting.
	 * These settings may be altered by the user or replaced when the user logs in.
	 *
	 * @param initialUserSettings initial user preferences
	 * @return a new session
	 */
	StandardSession createSession(Properties initialUserSettings);

	/**
	 * Unregisters current session from all collections so that it becomes eligible for garbage collection.
	 * This method does not have to be protected since it affects the current user only.
	 */
	void destroyCurrentSession();

	/**
	 * Unbinds request from internal thread.
	 *
	 * @param thread thread used by the internal process
	 * @return request that was bound to the thread
	 */
	Request releaseRequest(Thread thread);

	/**
	 * @param token token, provided by client, to identify session
	 * @return a session identified by token
	 */
	StandardSession getSessionByToken(String token);

	/**
	 * Returns the current request provided that the request is bound to and
	 * released from the current thread at the start and end of a request.
	 * <p/>
	 * <em>Note: implementations must not log anything inside this method,
	 * since the standard logger may invoke this method itself!</em>
	 *
	 * @return the request that is attached to the current thread
	 * @see this#bindRequest(EntryPoint)
	 * @see this#releaseRequest()
	 */
	Request getCurrentRequest();


	Component createAgent(String id);

	void removeAgent(Component agent);


}

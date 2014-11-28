/*
 * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
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

import java.util.Properties;


/**
 * Entry point that does not interact with a client.
 * It can be used to act as entry point for internal requests.
 */
public class DummyEntryPoint implements EntryPoint {
//	private Realm realm;

	/**
	 * @param realm
	 */
	public DummyEntryPoint(/*Realm realm*/) {
//		this.realm = realm;
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param session
	 */
	public void onSessionUpdate(Request request, Session session) {
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param session
	 */
	public void onSessionDestruction(Request request, Session session) {
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param properties
	 */
	public void exportUserSettings(Request request, Properties properties) {
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param properties
	 */
	public void importUserSettings(Request request, Properties properties) {
	}

	/**
	 * @return
	 */
/*	public Realm getRealm()
	{
		return realm;
	}*/
}

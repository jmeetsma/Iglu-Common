/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.access;

import java.util.Properties;


/**
 * Entry point that does not interact with a client.
 * It can be used to act as entry point for internal requests.
 */
public class DummyEntryPoint implements EntryPoint
{
//	private Realm realm;

	/**
	 * @param realm
	 */
	public DummyEntryPoint(/*Realm realm*/)
	{
//		this.realm = realm;
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param session
	 */
	public void onSessionUpdate(Request request, Session session)
	{
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param session
	 */
	public void onSessionDestruction(Request request, Session session)
	{
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param properties
	 */
	public void exportUserSettings(Request request, Properties properties)
	{
	}

	/**
	 * Does nothing.
	 *
	 * @param request
	 * @param properties
	 */
	public void importUserSettings(Request request, Properties properties)
	{
	}

	/**
	 * @return
	 */
/*	public Realm getRealm()
	{
		return realm;
	}*/
}

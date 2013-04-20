/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.server.database;

import java.sql.Types;

/**
 * Is used to pass nulls in an array of params parameters (Object[]) and still specifying the required SQL type
 */
public class JdbcNullObject
{
	private int sqlType = Types.NULL;

	/**
	 * @param type
	 */
	public JdbcNullObject(int type)
	{
		sqlType = type;
	}


	/**
	 * @return
	 */
	public int getType()
	{
		return sqlType;
	}
}

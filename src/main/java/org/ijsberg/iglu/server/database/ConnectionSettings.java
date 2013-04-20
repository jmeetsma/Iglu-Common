package org.ijsberg.iglu.server.database;

import java.sql.Connection;

/**
 */
public class ConnectionSettings
{
	public boolean readOnly;
	public int isolationLevel = Connection.TRANSACTION_READ_UNCOMMITTED;
	public boolean invokeStoredProcedure;

	public ConnectionSettings()
	{
	}

	public ConnectionSettings(boolean readOnly)
	{
		this.readOnly = readOnly;
	}


	public ConnectionSettings(int isolationLevel)
	{
		this.isolationLevel = isolationLevel;
	}


	public ConnectionSettings(boolean readOnly, int isolationLevel)
	{
		this.readOnly = readOnly;
		this.isolationLevel = isolationLevel;
	}

	public ConnectionSettings(boolean readOnly, boolean invokeStoredProcedure)
	{
		this.readOnly = readOnly;
		this.invokeStoredProcedure = invokeStoredProcedure;
	}


	public ConnectionSettings(int isolationLevel, boolean invokeStoredProcedure)
	{
		this.isolationLevel = isolationLevel;
		this.invokeStoredProcedure = invokeStoredProcedure;
	}


	public ConnectionSettings(boolean readOnly, int isolationLevel, boolean invokeStoredProcedure)
	{
		this.readOnly = readOnly;
		this.isolationLevel = isolationLevel;
		this.invokeStoredProcedure = invokeStoredProcedure;
	}

}

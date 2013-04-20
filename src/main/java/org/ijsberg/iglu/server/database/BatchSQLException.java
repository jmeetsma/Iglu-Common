package org.ijsberg.iglu.server.database;

import java.sql.SQLException;

/**
 */
public class BatchSQLException extends SQLException
{
	private int rowIndex;

	public BatchSQLException()
	{
	}

	public BatchSQLException(String reason)
	{
		super(reason);
	}

	public BatchSQLException(String reason, String SQLState)
	{
		super(reason, SQLState);
	}

	public BatchSQLException(String reason, String SQLState, int vendorCode)
	{
		super(reason, SQLState, vendorCode);
	}
}

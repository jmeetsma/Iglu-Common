package org.ijsberg.iglu.server.database;

/**
 */
public class StatementInput
{
	public Object[] params;
	public int[] sqlTypes;
	public int returnValType;
	public boolean returnsVoid;

	public StatementInput()
	{
		params = new Object[0];
	}

	public StatementInput(Object[] params)
	{
		this.params = params;
	}


	public StatementInput(Object[] params, int[] sqlTypes)
	{
		this.params = params;
		this.sqlTypes = sqlTypes;
	}


	public StatementInput(int returnValType)
	{
		params = new Object[0];
		this.returnValType = returnValType;
	}


	public StatementInput(boolean returnsVoid)
	{
		params = new Object[0];
		this.returnsVoid = returnsVoid;
	}

	public StatementInput(Object[] params, int returnValType)
	{
		this.returnValType = returnValType;
		this.params = params;
	}


	public StatementInput(Object[] params, boolean returnsVoid)
	{
		this.returnsVoid = returnsVoid;
		this.params = params;
	}


	public StatementInput(Object[] params, int[] sqlTypes, int returnValType)
	{
		this.params = params;
		this.sqlTypes = sqlTypes;
		this.returnValType = returnValType;
	}


	public StatementInput(Object[] params, int[] sqlTypes, boolean returnsVoid)
	{
		this.params = params;
		this.sqlTypes = sqlTypes;
		this.returnsVoid = returnsVoid;
	}

	/**
	 * Executes a prepared statement on a relational database
	 *
	 * @param statement		declaration of the prepared statement
	 * @param outParamType	 expected result type e.g. the code for VARCHAR
	 * @param inParams		 an array of parameters to feed the stored procedure
	 * @param specificSqlTypes an array of SQL-types to specify the params parameters
	 * @param hasReturnVal	 a flag indicated if a return value is expected
	 * @param resolveLOBs	  a flag indicating that LOBs should be read into a String or byte array
	 * @param readOnly		 a flag indicating that a read-only connection should be used
	 * @param isCallable	   must be true if the statement should be regerded as a stored procedure instead of an SQL statment
	 * @return the outcome of the execution of the stored procedure, this may be null, any kind of object, or a ResultSetCopy
	 */
}

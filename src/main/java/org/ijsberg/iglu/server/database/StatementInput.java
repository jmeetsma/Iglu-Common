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

}

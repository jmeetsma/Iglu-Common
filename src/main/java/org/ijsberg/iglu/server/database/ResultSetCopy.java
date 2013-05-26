/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.server.database;


import org.ijsberg.iglu.util.io.StreamSupport;

import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * A ResultSetCopy acts as a placeholder for a genuine ResultSet. A ResultSetCopy does not
 * keep a connection open to the database, so it may live as long as necessary and it may also
 * be serialized.
 * <p/>
 * Results may be obtained as Values so that they can easily be transformed into the required data types
 */
public class ResultSetCopy implements Serializable
{
	//TODO introduce max size of column to log
	private int colCount;
	private int rowCount;
	private int maxRowLog = 100;
	private ArrayList result = new ArrayList();
	private int index = -1;
	private Object[] currentRow;
	private String[][] colTypeNames;
	private String[] colName;
	private HashMap colNames;
	private boolean resolveLOBs;
	private IOException lobReadException;


	/**
	 * Forces a String into a fixed length String
	 *
	 * @param str String
	 * @param len length
	 * @return
	 */
	private static String format(String str, int len)
	{
		return format(str, len, false);
	}

	/**
	 * Forces a String into a fixed length String
	 *
	 * @param str String
	 * @param len length
	 * @param postfix true: append spaces to fill up to necessary length
	 * false: insert spaces at beginning
	 * @return
	 */
	private static String format(String str, int len, boolean postfix)
	{
		StringBuffer s = new StringBuffer("");
		int amount = len - str.length();
		if (!postfix)
		{
			for (int i = 0; i < amount; i++)
			{
				s.append(' ');
			}
		}
		if (str.length() > len)
		{
			s.append(str.substring(0, len - 3) + "...");
		}
		else
		{
			s.append(str);
		}
		if (postfix)
		{
			for (int i = 0; i < amount; i++)
			{
				s.append(' ');
			}
		}
		return s.toString();
	}

	/**
	 * @return A description of the ResultSet in the form of a list of column descriptions
	 */
	public String getDescription()
	{
		StringBuffer s = new StringBuffer("");
		for (int i = 0; i < colCount; i++)
		{
			s.append(format(String.valueOf(i), 3));
			s.append(": ");
			//TODO try to keep fixed length as short as possible
			s.append(format(colTypeNames[i][0], 30, true));
			s.append(": ");
			s.append(format(colTypeNames[i][1], 40, true));
			s.append('\n');
		}
		return s.toString();
	}


	/**
	 * @param rs original result set
	 * @param maxRowLog maximum number of rows to log
	 * @param resolveLOBs read LOB's into byte array
	 * @throws SQLException
	 * @throws IOException
	 */
	public ResultSetCopy(ResultSet rs, int maxRowLog, boolean resolveLOBs) throws SQLException, IOException
	{
		this.maxRowLog = maxRowLog;
		this.resolveLOBs = resolveLOBs;
		copy(rs);
		if (lobReadException != null)
		{
			throw lobReadException;
		}
	}


	/**
	 * @param rs original result set
	 * @param maxRowLog maximum number of rows to log
	 * @throws SQLException
	 */
	public ResultSetCopy(ResultSet rs, int maxRowLog) throws SQLException
	{
		this.maxRowLog = maxRowLog;
		copy(rs);
	}


	/**
	 * Copies ResultSet into ResultSetCopy
	 *
	 * @param rs original result set
	 * @throws SQLException
	 */
	public void copy(ResultSet rs) throws SQLException
	{
		ResultSetMetaData meta = rs.getMetaData();
		colCount = meta.getColumnCount();

		int[] colType = new int[colCount];
		colTypeNames = new String[colCount][2];
		colNames = new HashMap(colCount);
		colName = new String[colCount];

		for (int i = 0; i < colCount; i++)
		{
			colType[i] = meta.getColumnType(i + 1);
			colTypeNames[i][0] = meta.getColumnClassName(i + 1);
			colTypeNames[i][1] = meta.getColumnName(i + 1).toLowerCase();
			colNames.put(meta.getColumnName(i + 1).toLowerCase(), new Integer(i + 1));
			colName[i] = meta.getColumnName(i + 1);
		}

		while (rs.next())
		{
			Object[] row = new Object[colCount];
			for (int i = 0; i < colCount; i++)
			{
				row[i] = rs.getObject(i + 1);
				if (resolveLOBs)
				{
					if (row[i] instanceof Clob)
					{
						Clob clob = rs.getClob(i + 1);
						if (clob != null)
						{
							try
							{
								row[i] = new String(StreamSupport.absorbInputStream(clob.getAsciiStream()));
							}
							catch (IOException e)
							{
								lobReadException = e;
							}
						}
					}
					else if (row[i] instanceof Blob)
					{
						Blob blob = rs.getBlob(i + 1);
						if (blob != null)
						{
							row[i] = blob.getBytes(1, (int) blob.length());
						}
					}
				}
			}
			rowCount++;
			result.add(row);
		}
	}

	/**
	 * @return number of columns in result set
	 */
	public int getColCount()
	{
		return colCount;
	}


	/**
	 * @return number of rows in result set
	 */
	public int size()
	{
		return rowCount;
	}


	/**
	 * @return number of rows in result set
	 */
	public int getRowCount()
	{
		return rowCount;
	}


	/**
	 * @param colName column name as defined in the relational database
	 * @return column index number
	 */
	private int getColumnIndexByName(String colName)
	{
		Integer column = (Integer) colNames.get(colName.toLowerCase());
		if (column == null)
		{
			throw new IndexOutOfBoundsException("column '" + colName + "' does not exist");
		}
		return (column.intValue());
	}


	/**
	 * @param colName column name as defined in the relational database
	 * @return true if the column name is available in the result set
	 */
	public boolean isValidColumnName(String colName)
	{
		Integer column = (Integer) colNames.get(colName.toLowerCase());
		return (column != null);
	}


	/**
	 * Resets the iteration index so that ResultSetCopy mey be iteratied again using the next() method
	 */
	public void reset()
	{
		index = -1;
	}


	/**
	 * Sets the index to the next row in the result set
	 *
	 * @return
	 */
	public boolean next()
	{
		if (index == -1)
		{
			if (result == null || result.isEmpty())
			{
				return false;
			}
			index = 0;
		}

		if (index < result.size())
		{
			currentRow = (Object[]) result.get(index++);

			return true;
		}
		return false;
	}


	/**
	 * Sets the index to the next row in the result set
	 *
	 * @return the next row wrapped in a context
	 */
/*	public GenericPropertyBundle nextAsContext()
	{
		if (index == -1)
		{
			if (result == null || result.isEmpty())
			{
				return null;
			}
			index = 0;
		}

		if (index < result.size())
		{
			currentRow = (Object[]) result.get(index++);
			GenericPropertyBundle retval = new GenericPropertyBundle("1 row of result of database query");
			for (int i = 0; i < colName.length; i++)
			{
				retval.setProperty(colName[i], getObject(i));
			}
			return retval;
		}
		return null;
	}

	/**
	 * @param colName name of the column to obtain an Object from
	 * @return An object in the indicated column at the present row
	 */
	public Object getObject(String colName)
	{
		return getObject(getColumnIndexByName(colName));
	}

	public int getInt(String colName)
	{
		return Integer.parseInt(getObject(getColumnIndexByName(colName)).toString());
	}

	public Long getLong(String colName)
	{
		Object o = getObject(getColumnIndexByName(colName));
		if(o != null) {
			return Long.parseLong(getObject(getColumnIndexByName(colName)).toString());
		}
		return null;
	}

	public String getString(String colName)
	{
		Object o = getObject(getColumnIndexByName(colName));
		if(o != null) {
			return o.toString();
		}
		return null;
	}

	/**
	 * @param columnIndex index of the column to obtain an Object from
	 * @return An object in the indicated column at the present row
	 */
	public Object getObject(int columnIndex)
	{
		checkCurrentRowBeforeRetrieval();
		return currentRow[columnIndex - 1];
	}



	private void checkCurrentRowBeforeRetrieval()
	{
		if(currentRow == null)
		{
			throw new IllegalStateException("current row can not be determined, please invoke next() before retrieving data from row");
		}
	}


	/**
	 * @param colName name of the column to obtain an Object from
	 * @return A CLOB in the indicated column at the present row
	 */
	public Clob getClob(String colName)
	{
		return getClob(getColumnIndexByName(colName));
	}


	/**
	 * @param columnIndex name of the column to obtain an Object from
	 * @return A CLOB in the indicated column at the present row
	 */
	public Clob getClob(int columnIndex)
	{
		checkCurrentRowBeforeRetrieval();
		return (Clob) currentRow[columnIndex - 1];
	}


	/**
	 * @param colName name of the column to obtain an Object from
	 * @return A CLOB converted to a String in the indicated column at the present row
	 */
	public String getClobAsString(String colName)
	{
		return getClobAsString(getColumnIndexByName(colName));
	}


	/**
	 * @param columnIndex name of the column to obtain an Object from
	 * @return A CLOB converted to a String in the indicated column at the present row
	 */
	public String getClobAsString(int columnIndex)
	{
		checkCurrentRowBeforeRetrieval();
		return (String) currentRow[columnIndex - 1];
	}


	/**
	 * @param colName name of the column to obtain an Object from
	 * @return A BLOB converted to a byte array in the indicated column at the present row
	 */
	public byte[] getBlobAsByteArray(String colName)
	{
		return getBlobAsByteArray(getColumnIndexByName(colName));
	}


	/**
	 * @param columnIndex name of the column to obtain an Object from
	 * @return A CLOB converted to a String in the indicated column at the present row
	 */
	public byte[] getBlobAsByteArray(int columnIndex)
	{
		checkCurrentRowBeforeRetrieval();
		return (byte[]) currentRow[columnIndex - 1];
	}

	/**
	 * @param colName name of the column to obtain an Object from
	 * @return true if the column exists
	 */
	public boolean isNull(String colName)
	{
		return isNull(getColumnIndexByName(colName));
	}


	/**
	 * @param columnIndex name of the column to obtain an Object from
	 * @return true if the value in the coulumn is null
	 */
	public boolean isNull(int columnIndex)
	{
		checkCurrentRowBeforeRetrieval();
		return currentRow[columnIndex - 1] == null;
	}


	/**
	 * @return A String representation of the result set which is suitable for logging
	 */
	public String toString()
	{
		if (result == null)
		{
			return "Resultset not initialized...";
		}

		StringBuffer sb = new StringBuffer("resultset contains " + result.size() + " row(s) and " + colName.length + " column(s)\n");

		for (int j = 0; j < colName.length; j++)
		{
			sb.append(colName[j]).append('(').append(colTypeNames[j][0]).append(")\t");
		}

		sb.append("\n");

		Iterator i = result.iterator();
		int count = 0;

		while (i.hasNext() && ((maxRowLog <= 0) || (count++ < maxRowLog)))
		{
			Object[] row = (Object[]) i.next();
			sb.append('(');
			for (int j = 0; j < row.length; j++)
			{
				if (colTypeNames[j][0].endsWith("LOB"))
				{
					if (row[j] != null)
					{
						sb.append("LOB");
						if (row[j] instanceof String)
						{
							sb.append(" (size:" + ((String) row[j]).length() + ')');
						}
						if (row[j] instanceof byte[])
						{
							sb.append(" (size:" + ((byte[]) row[j]).length + ')');
						}
					}
					else
					{
						sb.append("null");
					}
				}
				else
				{
					//TODO make configurable
					if (row[j] instanceof String && ((String) row[j]).length() > 100)
					{
						sb.append(((String) row[j]).substring(0, 100) + "...");
					}
					else
					{
						sb.append(row[j]);
					}
				}
				if (j < row.length - 1)
				{
					sb.append(",\t");
				}
			}
			sb.append(")\n");
		}
		if (i.hasNext() && (count >= maxRowLog))
		{
			sb.append("max nr of rows to log exceeded...\n");
		}
		return sb.toString();
	}

	//TODO convert a row to PropertyBundle when reading
	//an Entity now only has to implement setFromContext and getAsContext
}
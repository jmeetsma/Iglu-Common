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

package org.ijsberg.iglu.database;

/**
 */
public class StatementInput {
	public Object[] params;
	public int[] sqlTypes;
	public int returnValType;
	public boolean returnsVoid;

	public StatementInput() {
		params = new Object[0];
	}

	public StatementInput(Object[] params) {
		this.params = params;
	}


	public StatementInput(Object[] params, int[] sqlTypes) {
		this.params = params;
		this.sqlTypes = sqlTypes;
	}


	public StatementInput(int returnValType) {
		params = new Object[0];
		this.returnValType = returnValType;
	}


	public StatementInput(boolean returnsVoid) {
		params = new Object[0];
		this.returnsVoid = returnsVoid;
	}

	public StatementInput(Object[] params, int returnValType) {
		this.returnValType = returnValType;
		this.params = params;
	}


	public StatementInput(Object[] params, boolean returnsVoid) {
		this.returnsVoid = returnsVoid;
		this.params = params;
	}


	public StatementInput(Object[] params, int[] sqlTypes, int returnValType) {
		this.params = params;
		this.sqlTypes = sqlTypes;
		this.returnValType = returnValType;
	}


	public StatementInput(Object[] params, int[] sqlTypes, boolean returnsVoid) {
		this.params = params;
		this.sqlTypes = sqlTypes;
		this.returnsVoid = returnsVoid;
	}

}

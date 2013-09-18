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

package org.ijsberg.iglu.database;

import java.util.Properties;

/**
 */
public class TableConfig {

	private String tableName;
	private String pkAttrName;
	private Properties orMapping;
	private Class<? extends DataObject> type;

	public TableConfig(String tableName, String pkColumnName, Class<? extends DataObject> type, Properties orMapping) {
		this.tableName = tableName;
		this.pkAttrName = pkColumnName;
		this.orMapping = orMapping;
		this.type = type;
	}

	public String getTableName() {
		return tableName;
	}

	public Class<? extends DataObject> getType() {
		return type;
	}

	public String getPkAttrName() {
		return pkAttrName;
	}

	public Properties getOrMapping() {
		return orMapping;
	}
}

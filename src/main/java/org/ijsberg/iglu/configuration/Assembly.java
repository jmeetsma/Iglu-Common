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

package org.ijsberg.iglu.configuration;


import org.ijsberg.iglu.invocation.ExposeInConsole;

import java.io.IOException;
import java.util.Map;

/**
 * Implementations create an assembly of components and clusters that form an application.
 * This interface provides access for administrative purposes.
 */
public interface Assembly {

	/**
	 * @return all clusters in the configuration
	 */
	Map<String, Cluster> getClusters();

	/**
	 * @return the cluster that contains the application's core components
	 */
	Cluster getCoreCluster();

	/**
	 * Creates the assembly.
	 *
	 * @param args command line arguments
	 */
	void initialize(String[] args);

	@ExposeInConsole(description = "saves properties of components registered with", paramDesc = {})
	void saveProperties() throws IOException;
}

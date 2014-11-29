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

package org.ijsberg.iglu.sample.configuration;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.module.BasicAssembly;
import org.ijsberg.iglu.configuration.module.StandardCluster;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class TestAssembly extends BasicAssembly {

	Cluster core;

	public TestAssembly() {
		super();
		core = new StandardCluster();
		clusters.put("core", core);
	}

	private Map<String, Cluster> clusters = new HashMap<String, Cluster>();

	public void addCluster(String clusterId, Cluster cluster) {
		clusters.put(clusterId, cluster);
	}

	@Override
	public Map<String, Cluster> getClusters() {
		return clusters;
	}

	@Override
	public Cluster getCoreCluster() {
		return core;
	}

	@Override
	public void initialize(String[] args) {
	}
}

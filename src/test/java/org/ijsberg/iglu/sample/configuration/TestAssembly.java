package org.ijsberg.iglu.sample.configuration;

import java.util.HashMap;
import java.util.Map;

import org.ijsberg.iglu.configuration.Assembly;
import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.module.StandardCluster;

/**
 */
public class TestAssembly implements Assembly {

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

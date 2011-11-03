package org.ijsberg.iglu.configuration;

import org.ijsberg.iglu.Cluster;
import org.ijsberg.iglu.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class TestConfiguration implements Configuration {

	private Map<String, Cluster> clusters = new HashMap<String, Cluster>();

	public void addCluster(String clusterId, Cluster cluster) {
		clusters.put(clusterId, cluster);
	}

	@Override
	public Map<String, Cluster> getClusters() {
		return clusters;
	}
}

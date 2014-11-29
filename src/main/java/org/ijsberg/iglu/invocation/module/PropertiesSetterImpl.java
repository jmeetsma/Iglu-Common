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

package org.ijsberg.iglu.invocation.module;

import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.Startable;
import org.ijsberg.iglu.invocation.Parameters;
import org.ijsberg.iglu.invocation.PropertiesSetter;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.util.collection.ArraySupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 */
public class PropertiesSetterImpl implements PropertiesSetter {

	private List<Cluster> clusters = new ArrayList<Cluster>();



	public void register(Cluster cluster) {
		clusters.add(cluster);

	}

	@Override
	@Parameters(decriptions = {"component name", "property name", "property value"})
	public void setProperty(String componentName, String propertyName, String propertyValue) {
		for(Cluster cluster : clusters) {
			Component component = cluster.getInternalComponents().get(componentName);
			if(component != null) {
				Startable startable = null;
				if(component.implementsInterface(Startable.class)) {
					startable = component.createProxy(Startable.class);
					System.out.println(new LogEntry("stopping component " + componentName));
					startable.stop();
				}
				System.out.println(new LogEntry("setting property " + propertyName + " with value " + propertyValue + " on " + componentName));
				Properties properties = component.getProperties();
				properties.setProperty(propertyName, propertyValue);
				if(startable != null) {
					System.out.println(new LogEntry("starting component " + componentName));
					startable.start();
				}
			}
		}
	}

	/**
	 *
	 * @param args
	 */
	@Parameters(decriptions = "hop")
	public static void main(String[] args) throws Exception {
		Parameters param = PropertiesSetterImpl.class.getMethod("test", new Class<?>[]{}).getAnnotation(Parameters.class);
		System.out.println(ArraySupport.format(param.decriptions(),","));

	}


}

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
import org.ijsberg.iglu.invocation.ExposeInConsole;
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
	@ExposeInConsole(description = "sets module property, you may need to restart the module", paramDesc = {"component name", "property name", "property value"})
	public void setProperty(String componentName, String propertyName, String propertyValue) {
		for(Cluster cluster : clusters) {
			Component component = cluster.getInternalComponents().get(componentName);
			if(component != null) {
				System.out.println(new LogEntry("setting property " + propertyName + " with value " + propertyValue + " on " + componentName));
				Properties properties = component.getProperties();
				properties.setProperty(propertyName, propertyValue);
			}
		}
	}

	@ExposeInConsole(description = "lists module properties", paramDesc = {"module name"})
	public String listProperties(String componentName) {
		for(Cluster cluster : clusters) {
			Component component = cluster.getInternalComponents().get(componentName);
			if(component != null) {
				return component.getProperties().toString();
			}
		}
		return "component with name '" + componentName + "' not found in clusters " + clusters;
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		ExposeInConsole param = PropertiesSetterImpl.class.getMethod("test", new Class<?>[]{}).getAnnotation(ExposeInConsole.class);
		System.out.println(ArraySupport.format(param.paramDesc(),","));

	}


}

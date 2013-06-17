/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.scheduling;

/**
 * The scheduler has two tasks:
 * <ol>
 * <li>It pages subsystems that need to perform scheduled tasks.</li>
 * <li>It collects (performance) data from monitored subsystems.</li>
 * </ol>
 */
public interface Scheduler
{
	/**
	 * Registers a pageable or monitored system.
	 *
	 * @param pageable
	 */
	void register(Pageable pageable);

	String getReport();
}

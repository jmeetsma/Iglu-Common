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
 * Implementations of this interface may register with a scheduler that
 * will page them at regular times.
 */
public interface Pageable
{
	/**
	 * This method must be implemented to tell the pager when to page the pageable.
	 * The actual time a pageable gets paged is a multitude of the page interval since january 1st 1970.
	 * This means that predictable results occur when intervals are returned which divide hours or days
	 * into rounded figures, such as:
	 * <ul>
	 * <li>1: paged every minute</li>
	 * <li>5: paged every 5 minutes</li>
	 * <li>15: paged every quarter, on the quarter</li>
	 * <li>60: paged every hour, on the hour</li>
	 * <li>360: paged every 6 hours, on 0, 6, 12 and 18 hours</li>
	 * <li>1440: paged every day at midnight</li>
	 * <li>10080: paged every week on wednesdaynight at midnight (january 1st 1970 was a thursday)</li>
	 * </ul>
	 *
	 * @return
	 */
	int getPageIntervalInMinutes();

	/**
	 * The actual time paging occurs is based on the page interval plus the offset that is specified here.
	 * A negative offset is allowed.
	 * <p/>
	 * E.g. if the interval is 60 and the offset is -3, the pageable will get paget at 00:57, 01:57, 02:57 etc.
	 *
	 * @return
	 */
	int getPageOffsetInMinutes();

	/**
	 * This method should contain the actions taken if paged.
	 * It's not mandatory that the implementation of the
	 * scheduler invokes this method in an asynchronous way.
	 * Therefor implementations must handle this call fast
	 * and safe. 
	 *
	 * @param officialTime the exact time the page event was scheduled (actual time may differ some milliseconds)
	 */
	void onPageEvent(long officialTime);

	/**
	 * @return true if the implementation should be paged
	 */
	boolean isActive();
}

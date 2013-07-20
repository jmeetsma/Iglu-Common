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
package org.ijsberg.iglu.scheduling;


/**
 * Implementations of this interface may register with a scheduler that
 * will page them at regular times.
 */
public interface Pageable {
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
	boolean isStarted();
}

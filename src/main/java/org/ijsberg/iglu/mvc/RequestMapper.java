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

package org.ijsberg.iglu.mvc;


import java.util.List;
import java.util.Properties;

/**
 * Is a GUI independent request handler, that maps requests to an actual handler,
 * according to a loaded mapping.
 * <p/>
 * Implementations contains the the wiring to handle MVC requests.
 */
public interface RequestMapper {
	/**
	 * @return messages collected when loading MVC mapping
	 */
	List getMessages();


	/**
	 * @return true if all MVC trees have been loaded successfully
	 */
	boolean isLoaded();

	/**
	 * @return true if an MVC tree has been loaded successfully
	 */
	boolean isLoaded(String mappingName);

	/**
	 * Checks if a mapping definition was modified.
	 *
	 * @return true if the mvc tree definition was modified and succesfully reloaded
	 */
	boolean reloadIfUpdated();

	/**
	 * Processes a request for a GUI-specific request dispatcher.
	 *
	 * @param processPath
	 * @param requestProperties
	 * @param dispatcher
	 * @return
	 */
	boolean processRequest(String processPath, Properties requestProperties, RequestDispatcher dispatcher);

	/**
	 * Checks if the mapping is consistent and refers to existing resources.
	 *
	 * @param dispatcher request dispatcher that initiates the request and handles dispatching
	 * @return true if the mapping is usable
	 */
	boolean checkSanity(RequestDispatcher dispatcher);
}

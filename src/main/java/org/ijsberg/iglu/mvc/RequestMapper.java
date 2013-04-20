package org.ijsberg.iglu.mvc;


import java.util.List;
import java.util.Properties;

/**
 * Is a GUI independent request handler, that maps requests to an actual handler,
 * according to a loaded mapping.
 *
 * Implementations contains the the wiring to handle MVC requests.
 *
 */
public interface RequestMapper
{
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

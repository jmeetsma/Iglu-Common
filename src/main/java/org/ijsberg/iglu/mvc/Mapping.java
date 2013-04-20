package org.ijsberg.iglu.mvc;

import org.ijsberg.iglu.mvc.mapping.Process;

import java.util.List;

/**
 */
public interface Mapping
{
	String getName();

	String getFileName();

	boolean isModified();

	boolean isLoaded();

	List getLoadMessages();

	Process getRootProcess();

	boolean checkSanity(RequestDispatcher handle);

	List getSanityCheckMessages();

}

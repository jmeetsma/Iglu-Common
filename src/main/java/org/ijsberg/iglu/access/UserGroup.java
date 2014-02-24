package org.ijsberg.iglu.access;

/**
 */
public class UserGroup {

	protected String name;
	protected String description;

	protected UserGroup() {
	}

	public UserGroup(String name, String description) {
		this.name = name;
		this.description = description;
	}


	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String toString() {
		return name;
	}
}

package org.ijsberg.iglu.access;

import java.util.Map;
import java.util.Properties;


/**
 * Represents a request to the application.
 * It's available at all levels in the application.
 * It contains trace information and, if available, references to session and user data.
 * <p/>
 * A request is issued when a request passes an entry point.
 * <p/>
 * A request may represent three types of users:
 * <ol>
 * <li>An internal process, which is given full access to resources.</li>
 * <li>An administrator, who can be authorized to access control and domain components.</li>
 * <li>A regular user, who can't access core components and may be
 * subject to dedicated access control on domain objects.</li>
 * </ol>
 * A request may temporarily represent another request, which has
 * an effect on the accessibility of secured objects.
 * An internal request, may represent a user request, so that access is limited
 * to the user's access rights. This may occur when a request is forwarded an asynchronous handler.
 * A user request may represent an internal request (if known by a subsystem), thus gaining
 * unlimited access to secured components.
 *
 * @see Application#getCurrentRequest()
 * @see EntryPoint
 */
public interface Request
{
	/**
	 * @return the application this request is accessing
	 */
	//Application getApplication();

	/**
	 *
	 * @param create indicates if a session must be created if unavailable
	 * @return a session if available
	 */
	Session getSession(boolean create);

	/**
	 * Destroys current session.
	 */
	void destroySession();

	/**
	 * @return the realm that is associated with the request through an entry point
	 * @see EntryPoint
	 */
	//Realm getRealm();

	/**
	 * @return true if the request is made by internal processes such as a startup sequence or a cleanup routine
	 */
	//boolean isInternalRequest();

	/**
	 * @return true if the request is made by the root administrator or an internal process
	 */
	//boolean isRootRequest();

	/**
	 * @return true if the request is made by an administrator or an internal process
	 */
	//boolean isAdminRequest();

	/**
	 * Passes user credentials to the realm which logs
	 * a user in if authenticated.
	 * A logged in user is stored on a session.
	 * The type of credentials used, is entirely up to the implementing framework.
	 *
	 * Implementations may throw some runtime exception if authentication fails
	 * for another reason than simply invalid credentials,
	 * such as an expired password.
	 *
	 * @param credentials user credentials
	 * @return the authenticated user
	 * @see Realm
	 */
	User login(Credentials credentials);

	/**
	 * Logs out a user that was logged in previously.
	 */
	void logout();

	/**
	 * @return a logged in user
	 */
	User getUser();

	/**
	 *
	 * @return true if a session was resolved or just created
	 */
	boolean isSessionAvailable();

	/**
	 * Notification of a request invoking a component's method.
	 * Meant for internal use, which may be asserted by checking if
	 * this is an internal request (by representation).
	 * @see this#startRepresentingRequest(Request)
	 *
	 * @param component component currently being entered
	 */
//	void onEnterComponent(Component component);

	/**
	 * Notification of a request returning from a component's method invocation.
	 * Meant for internal use, which may be asserted by checking if
	 * this is an internal request (by representation).
	 * @see this#startRepresentingRequest(Request)
	 *
	 * @return the component left according to the request
	 */
//	Component onLeaveComponent();

	/**
	 * By passing an internal request via this method, a user request will
	 * temporarily be regarded as internal request.
	 * An internal request can, for instance, be obtained during startup phase
	 * by a component.
	 * <p/>
	 * It's also possible to let an internal request represent a user
	 * request in order to temporarily downgrade its access rights.
	 *
	 * @param requestToBeRepresented request to be represented
	 * @return true if the request is able to represent the given request
	 * @see Request#stopRepresentingRequest()
	 */
//	boolean startRepresentingRequest(Request requestToBeRepresented);

	/**
	 * @return the request represented or null
	 */
//	Request getRepresentedRequest();

	/**
	 * Stops acting as some other request.
	 *
	 * @see Request#startRepresentingRequest(Request)
	 */
//	void stopRepresentingRequest();

	/**
	 * Stores some object as attribute on the request.
	 *
	 * @param key attribute key
	 * @param value attribute value
	 */
	void setAttribute(Object key, Object value);

	/**
	 * @param key attribute key
	 * @return an object previously stored under the given key
	 */
	Object getAttribute(Object key);

	/**
	 *
	 * @return a map containing all stored attributes
	 */
	Map getAttributeMap();


	/**
	 * A user request may, due to redirects, pass an entry point several times.
	 * This fact must be dealt with when, for instance, handling exceptions,
	 * that must preferably only be caught at the point a request leaves the application.
	 *
	 * @return the number of times a user request has passed an entry point
	 */
	int getTimesEntered();
	
	void increaseTimesEntered();
	
	/**
	 * @return a layer passed or currently accessed by the user request
	 */
//	Layer getCurrentLayer();

	/**
	 * @return a module passed or currently accessed by the user request
	 */
//	Module getCurrentModule();

	/**
	 * @return the component the request has currently reached
	 */
//	Component getCurrentComponent();

	/**
	 * @return a set of properties defining the user's settings
	 */
	Properties getUserSettings();

	/**
	 * @return a description of the internal process if the request is an internal request
	 */
//	String getInternalProcessDescription();

	/**
	 * Exports user settings to the entry point which in turn may export to the client.
	 */
	void exportUserSettings();


	/**
	 * @return a transaction for the request
	 */
//	Transaction getTransaction();

	/**
	 *
	 * @param formId ID of a currently processed form
	 * @return an empty or partially or fully completed form that exists during the course
	 * of the current request or during (part of) the course of a session
	 */
//	Form getForm(String formId);

	/**
	 * Stores a form on the request or the session if it exists.
	 *
	 * @param formId ID of form to be processed
	 * @param form form to be processed
	 */
//	void putForm(Object formId, Form form);

	/**
	 *
	 * @return true if this request is bound to the current thread
	 * @see Application#bindRequest(EntryPoint)
	 */
//	public boolean isBoundToCurrentThread();

	/**
	 * Retrieves session and, if found, stores a reference in the request.  
	 * @param sessionToken (secret) token stored in client
	 * @param userId user ID, if available
	 * @return session resolved by the given token or null
	 */
	Session resolveSession(String sessionToken, String userId);
}

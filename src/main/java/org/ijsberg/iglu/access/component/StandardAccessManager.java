/* =======================================================================
 * Copyright (c) 2003-2010 IJsberg Automatisering BV. All rights reserved.
 * Redistribution and use of this code are permitted provided that the
 * conditions of the Iglu License are met.
 * The license can be found in org.ijsberg.iglu.StandardApplication.java
 * and is also published on http://iglu.ijsberg.org/LICENSE.
 * =======================================================================
 */
package org.ijsberg.iglu.access.component;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.TreeMap;

import org.ijsberg.iglu.access.*;
import org.ijsberg.iglu.configuration.Cluster;
import org.ijsberg.iglu.configuration.Component;
import org.ijsberg.iglu.configuration.ConfigurationException;
import org.ijsberg.iglu.configuration.module.StandardComponent;
import org.ijsberg.iglu.logging.LogEntry;
import org.ijsberg.iglu.scheduling.Pageable;


/**
 * Provides tracking of requests and sessions in an application.
 * This is achieved by binding requests to the treads that execute them.
 * The hash code of a thread is used as identifier. This should be reliable
 * since the hash code mechanism used is inherited from Object.
 * The hashCode method defined by class <tt>Object</tt> returns distinct
 * integers for distinct objects.
 */

public class StandardAccessManager implements AccessManager, Pageable, RequestKeeper {
    //session storage
    private final HashMap sessionsMirror = new HashMap(10);//mirror is used for cleanup
    private final HashMap sessions = new HashMap(10);
    //requests that currently access the application
    private final HashMap<Integer, Request> requestsByThreadId = new HashMap<Integer, Request>();
    //page interval used for cleanup
    private int pageInterval = 1;//min
    //default session timeout
    private int sessionTimeout = 1800;//sec
    private Authenticator authenticator;

    private Cluster serviceCluster;

    public static final String defaultAdminAccountName = "admin";
    private String defaultAdminPassword = "admin";


    private HashMap<String, AgentFactory> agentFactoriesByAgentId = new HashMap();

    /**
     *
     */
    public StandardAccessManager() {
    }

    /**
     * Used for injection
     *
     * @param serviceCluster
     */
    public void setServiceCluster(Cluster serviceCluster) {
        this.serviceCluster = serviceCluster;
    }

    /**
     * @return number of concurrent requests and sessions
     */
    public String getReport() {
        StringBuffer sb = new StringBuffer();
        sb.append("number of concurrent requests: " + requestsByThreadId.size() + "\n");
        sb.append("number of sessions: " + sessions.size() + "\n");
        return sb.toString();
    }


    /**
     * Initializes the service. Reads configuration properties.
     * Stores a reference to the initial request when it's invoked for the first time.
     * <p/>
     * Properties:
     * <ul>
     * <li>session_timeout : session timeout in seconds</li>
     * </ul>
     */
    protected void setProperties(Properties properties) {
        sessionTimeout = Integer.valueOf(properties.getProperty("session_timeout", "" + sessionTimeout));
        defaultAdminPassword = properties.getProperty("default_admin_password");
    }

    /**
     * @return a collection of sessions sorted by creation date
     */
    public Collection getSessions() {
        synchronized (sessionsMirror) {
            return new TreeMap(sessionsMirror).values();
        }
    }


    /**
     * Removes the current, bound thread from the list so that it no longer points to a request.
     * This method should be invoked at the point where a (stateless) request leaves the application
     * <p/>
     * Note: If a thread has been bound to a session multiple times, for instance when entry points are
     * wrapped, a thread must be released just as many times before it's released definitely.
     * <p/>
     * If an entry point fails to release a thread, two things might go wrong:
     * <ul>
     * <li>A following request with an unbound thread could still be bound to this session.</li>
     * <li>A memory leak could appear, because some application(server)s constantly construct new threads.</li>
     * </ul>
     *
     * @return the detached request if found
     * @see EntryPoint
     */
    public Request releaseRequest() {
        return releaseRequest(Thread.currentThread());
    }


    /**
     * Binds a user request when it enters they application throuqh an entry point.
     * This method should be invoked at the point where a (stateless) request accesses the application.
     * By doing so, it's possible to track down a request from every point in the application
     * This is useful for gathering profiling information and doing underwater security checks.
     *
     * @param entryPoint
     * @return
     * @see this#getCurrentRequest()
     * @see this#releaseRequest()
     */
    public Request bindRequest(EntryPoint entryPoint) {
        return bindThreadToRequest(Thread.currentThread(), /*null, */entryPoint);
    }

    /**
     * Internal bind implementation.
     *
     * @param thread
     * @param entryPoint
     * @return
     */
    private Request bindThreadToRequest(Thread thread, EntryPoint entryPoint) {
        StandardRequest boundRequest; /*= rebindingRequest;*/
//		thread.setContextClassLoader(contextClassLoader);

        int threadId = thread.hashCode();
        synchronized (requestsByThreadId) {
            StandardRequest previouslyBoundRequest = (StandardRequest) requestsByThreadId.get(threadId);

            if (previouslyBoundRequest == null) {
                boundRequest = new StandardRequest(entryPoint, this);
                requestsByThreadId.put(threadId, boundRequest);
            } else {
                // request is already bound, it will not be overwritten
                previouslyBoundRequest.increaseTimesEntered();
                boundRequest = previouslyBoundRequest;
            }
        }
        return boundRequest;
    }


    public void register(Authenticator authenticator) {
        this.authenticator = authenticator;
        System.out.println(new LogEntry("authenticator registered with request manager"));
    }


    /**
     * Removes a specific bound thread from the list so that it no longer points to a specific request.
     * This method should be invoked at the point where a (stateless) request leaves the application
     * If the thread was not released, a following request with an unbound thread would still be bound
     * to this request.
     * Some application (server)s constantly construct new threads, so cleaning up is necessary.
     * <p/>
     * Note: If a thread has been bound to a request multiple times,
     * for instance when entry points are wrapped,
     * a thread must be released as many times before it's released definitely.
     *
     * @return a request which has been found bound to the current thread
     */
    public Request releaseRequest(Thread thread) {
        int threadId = thread.hashCode();

        synchronized (requestsByThreadId) {
            StandardRequest request = (StandardRequest) requestsByThreadId.get(threadId);
            if (request != null) {
                if (request.getTimesEntered() > 0) {
                    request.decreaseTimesEntered();
                } else {
                    requestsByThreadId.remove(threadId);
                }
                return request;
            }
        }
        return null;
    }


    /**
     * Returns a session if found
     * and updates the last time it was accessed.
     *
     * @param token session identifier stored by the client
     * @return a session stored by the given session ID or null if it can't be found
     */
    public StandardSession getSessionByToken(String token) {
        StandardSession session;
        synchronized (sessions) {
            session = (StandardSession) sessions.get(token);
            if (session != null) {
                session.updateLastAccessedTime();
            }
        }
        return session;
    }


    /**
     * Returns current request data,
     * which only exists if request was bound previously.
     *
     * @return the request that is handled by the current thread
     * @see this#bindRequest(EntryPoint)
     */
    public Request getCurrentRequest() {
        //no logging allowed, since this is called by logging process itself
        //this won't work in a remote component
        synchronized (requestsByThreadId) {
            return requestsByThreadId.get(Thread.currentThread().hashCode());
        }
    }

    @Override
    public Component createAgent(String id) {
        AgentFactory agentFactory = agentFactoriesByAgentId.get(id);
        if (agentFactory == null) {
            throw new ConfigurationException("no AgentFactory for agents with id " + id + " present, register one in cluster");
        }
        Object implementation = agentFactory.createAgentImpl();
        Component component = new StandardComponent(implementation);
        //connect component anonymously
        //TODO serviceCluster may be null
        serviceCluster.getFacade().connect(component);
        return component;
    }

    @Override
    public void removeAgent(Component agent) {
        serviceCluster.getFacade().disconnect(agent);
    }

    /**
     * Returns current session,
     * which only exists if request was bound previously,
     * passing a valid session token.
     *
     * @return the session that is attached to the current request
     */
    private Session getCurrentSession() {
        //no logging allowed, since this is called by logging process itself
        //this won't work in a remote component
        synchronized (requestsByThreadId) {
            StandardRequest request = (StandardRequest) requestsByThreadId.get(Thread.currentThread().hashCode());
            if (request != null) {
                return request.getSession(false);
            }
        }
        return null;
    }

    /**
     * Creates and registers a session.
     *
     * @param defaultUserSettings
     * @return
     */
    public StandardSession createSession(Properties defaultUserSettings) {
        StandardSession session = new StandardSession(this, sessionTimeout, defaultUserSettings);
        synchronized (sessions) {
            synchronized (sessionsMirror) {
                sessions.put(session.getToken(), session);
                sessionsMirror.put(session.getToken(), session);
            }
        }
        System.out.println(new LogEntry("session " + session + " created"));
        return session;
    }


    /**
     * Destroys current session.
     */
    public void destroyCurrentSession() {
        Session session = getCurrentSession();
        if (session != null) {
            destroySessionById(session.getToken());
        }
    }

    /**
     * Unregisters a session from all collections so that it becomes eligible for garbage collection.
     *
     * @param id session id
     */
    private void destroySessionById(String id) {
        System.out.println(new LogEntry("destroying session " + id));
        synchronized (sessions) {
            synchronized (sessionsMirror) {
                StandardSession session = (StandardSession) sessions.get(id);
                if (session != null) {
                    session.onDestruction();
                    sessions.remove(id);
                    sessionsMirror.remove(id);
                }
            }
        }
    }

    /**
     * Cleans up expired sessions.
     *
     * @param officialTime
     */
    public void onPageEvent(long officialTime) {
        //cleanup expired sessions
        ArrayList garbage = new ArrayList(sessionsMirror.size());
        Iterator i;
        StandardSession session;
        synchronized (sessionsMirror) {
            i = sessionsMirror.values().iterator();
            while (i.hasNext()) {
                session = (StandardSession) i.next();
                if (session.isExpired()) {
                    garbage.add(session);
                }
            }
        }
        i = garbage.iterator();
        while (i.hasNext()) {
            session = (StandardSession) i.next();
            System.out.println(new LogEntry("session " + session.getToken() + " expired..."));
            this.destroySessionById(session.getToken());
        }
    }

    /**
     * @return 0
     */
    public int getPageOffsetInMinutes() {
        return 0;
    }

    /**
     * @return the amount of time between cleanups of expired sessions
     */
    public int getPageIntervalInMinutes() {
        return pageInterval;
    }

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public User authenticate(Credentials credentials) throws AuthenticationException {

        if(authenticator == null) {
            if(credentials.equals(new SimpleCredentials(defaultAdminAccountName, defaultAdminPassword))) {
                return new BasicUser(credentials.getUserId());
            } else {
                throw new AuthenticationException(AuthenticationException.CREDENTIALS_INVALID);
            }
        }
        return authenticator.authenticate(credentials);
    }

    @Override
    public User authenticate(Credentials expiredCredentials, Credentials newCredentials) throws AuthenticationException {
        return authenticator.authenticate(expiredCredentials, newCredentials);
    }

    /**
     * Used for injection of factories that create session-bound agents.
     * @param agentFactory
     * @param <T>
     */
    public <T> void register(AgentFactory<T> agentFactory) {
        agentFactoriesByAgentId.put(agentFactory.getAgentId(), agentFactory);
    }

}

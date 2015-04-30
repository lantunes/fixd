/*
 * Copyright (C) 2014 BigTesting.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigtesting.fixd.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bigtesting.fixd.marshalling.UnmarshallerProvider;
import org.bigtesting.fixd.request.impl.SimpleHttpRequest;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
import org.bigtesting.routd.Route;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

/**
 * 
 * @author Luis Antunes
 */
class SessionManager {

    private static final String SESSION_COOKIE_NAME = "Fixd-Session";
    
    /*
     * a ConcurrentHashMap, as opposed to synchronized map, is chosen here because,
     * from the perspective of a client, an up-to-date view of the map is not 
     * a requirement; i.e. a given client is not interested in sessions created
     * for other clients 
     */
    private final Map<String, Session> sessions = 
            new ConcurrentHashMap<String, Session>();
    
    public Session getSessionIfExists(Request request) {
        
        Cookie cookie = request.getCookie(SESSION_COOKIE_NAME);
        if (cookie != null) {
            String sessionId = cookie.getValue();
            Session session = sessions.get(sessionId);
            if (session != null && !session.isValid()) {
                sessions.remove(sessionId);
                return null;
            }
            return session;
        }
        return null;
    }
    
    public void createNewSession(Request request, Response response, 
            Route route, SessionHandler sessionHandler, 
            UnmarshallerProvider unmarshallerProvider) {
        
        Session session = new Session();
        sessionHandler.onCreate(new SimpleHttpRequest(request, session, 
                route, unmarshallerProvider));
        sessions.put(session.getSessionId(), session);
        
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, session.getSessionId());
        response.setCookie(cookie);
    }
}

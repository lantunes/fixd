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

import org.bigtesting.fixd.Method;
import org.bigtesting.fixd.util.RequestUtils;
import org.bigtesting.routd.Route;
import org.bigtesting.routd.Router;
import org.bigtesting.routd.TreeRouter;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Request;
import org.simpleframework.http.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luis Antunes
 */
class RequestResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestResolver.class);
    
    /**
     * TODO issue #9
     * create a separate router for before and after handlers;
     * otherwise, a before handler for /* will handle requests
     * for specific routes that have other handlers  
     */
    private final Router router = new TreeRouter();
    
    private final Map<HandlerKey, RequestHandlerImpl> handlerMap = 
            new ConcurrentHashMap<HandlerKey, RequestHandlerImpl>();
    
    private final Map<HandlerKey, Upon> uponHandlers = 
            new ConcurrentHashMap<HandlerKey, Upon>();
    
    public HandlerKey addHandler(RequestHandlerImpl handler, 
            Method method, String resource, String contentType) {
        
        Route route = new Route(resource);
        HandlerKey key = new HandlerKey(method.name(), route, contentType);
        handlerMap.put(key, handler);
        router.add(route);
        return key;
    }
    
    public void addUponHandler(FixtureContainer container, Upon upon) {
        
        RequestHandlerImpl uponHandler = 
                (RequestHandlerImpl)new RequestHandlerImpl(container).with(200, "text/plain", "");
        HandlerKey uponKey = addHandler(uponHandler, upon.getMethod(), 
                upon.getResource(), upon.getContentType());
        uponHandlers.put(uponKey, upon);
    }
    
    public boolean requestIsForUponHandler(ResolvedRequest resolved) {
        
        return uponHandlers.containsKey(resolved.key);
    }
    
    public Upon getUpon(ResolvedRequest resolved) {
        
        return uponHandlers.get(resolved.key);
    }
    
    public ResolvedRequest resolve(Request request) {
        
        ResolvedRequest resolved = new ResolvedRequest();
        String method = request.getMethod();
        String path = RequestUtils.getUndecodedPath(request);
        ContentType requestContentType = request.getContentType();
        
        /* get the route and the handler for this request */
        Route route = router.route(path);
        if (route == null) {
            logger.error("could not find a route for " + path);
            resolved.errorStatus = Status.NOT_FOUND;
            return resolved;
        }
        String contentType = requestContentType != null ? 
                requestContentType.getType() : null;
        HandlerKey key = new HandlerKey(method, route, contentType);
        RequestHandlerImpl handler = handlerMap.get(key);
        if (handler == null) {
            logger.error("could not find a handler for " + 
                    method + " - " + path + " - " + contentType);
            resolved.errorStatus = Status.METHOD_NOT_ALLOWED;
            return resolved;
        }
        
        resolved.handler = handler;
        resolved.route = route;
        resolved.key = key;
        return resolved;
    }

}

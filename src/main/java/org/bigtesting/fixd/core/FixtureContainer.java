/*
 * Copyright (C) 2013 BigTesting.org
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

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.capture.impl.SimpleCapturedRequest;
import org.bigtesting.fixd.routing.RegexRouteMap;
import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.routing.RouteMap;
import org.simpleframework.http.ContentType;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.Status;
import org.simpleframework.http.core.Container;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Luis Antunes
 */
public class FixtureContainer implements Container {
    
    private static final Logger logger = LoggerFactory.getLogger(FixtureContainer.class);
    
    private static final String SESSION_COOKIE_NAME = "Simple-Session";

    private final Map<HandlerKey, RequestHandler> handlerMap = 
            new HashMap<HandlerKey, RequestHandler>();
    
    private final RouteMap routeMap = new RegexRouteMap();
    
    private final Map<String, Session> sessions = new HashMap<String, Session>();
    
    private final Executor asyncExecutor;
    
    private final Set<HandlerKey> uponHandlers = new HashSet<HandlerKey>();
    
    private final BlockingQueue<Broadcast> broadcasts = new LinkedBlockingQueue<Broadcast>();
    
    private final Queue<CapturedRequest> capturedRequests = new LinkedList<CapturedRequest>();
    
    public FixtureContainer(int aysncThreadPoolSize) {
        asyncExecutor = Executors.newFixedThreadPool(aysncThreadPoolSize);
    }
    
    public HandlerKey addHandler(RequestHandler handler, 
            Method method, String resource) {
        
        return addHandler(handler, method, resource, null);
    }
    
    public HandlerKey addHandler(RequestHandler handler, 
            Method method, String resource, String contentType) {
        
        Route route = new Route(resource);
        HandlerKey key = new HandlerKey(method.name(), route, contentType);
        handlerMap.put(key, handler);
        routeMap.add(route);
        return key;
    }
    
    public Queue<CapturedRequest> getCapturedRequests() {
        
        return capturedRequests;
    }
    
    public CapturedRequest nextCapturedRequest() {
        
        return capturedRequests.poll();
    }
    
    public void handle(Request request, Response response) {

        try {
            
            capturedRequests.add(new SimpleCapturedRequest(request));
            
            String responseContentType = "text/plain";
            String responseBody = "";
            
            ResolvedRequest resolved = resolve(request);
            if (resolved.errorStatus != null) {
                response.setStatus(resolved.errorStatus);
                sendAndCommitResponse(response, responseContentType, responseBody);
                return;
            }
            
            if (uponHandlers.contains(resolved.key)) {
                
                broadcasts.add(new Broadcast(request, resolved.route, 
                        request.getPath().getPath()));
                /* continue handling the request, as an 
                 * upon handler won't itself contain an Upon,
                 * and it needs to return a normal response */
            }
            
            Upon upon = resolved.handler.upon();
            if (upon != null) {
                RequestHandler uponHandler = 
                        new RequestHandler().with(200, "text/plain", "");
                HandlerKey uponKey = addHandler(uponHandler, upon.getMethod(), 
                        upon.getResource(), upon.getContentType());
                uponHandlers.add(uponKey);            
            }
            
            /* set the content type */
            String handlerContentType = resolved.handler.contentType();
            if (handlerContentType != null && 
                    handlerContentType.trim().length() != 0) {
                
                responseContentType = handlerContentType;
            }
            
            /* set the response body */
            if (!resolved.handler.isSuspend()) {
                Session session = getSessionIfExists(request);
                String path = request.getPath().getPath();
                String handlerBody = resolved.handler.body(path, 
                        resolved.route.pathParameterElements(), session, request);
                if (handlerBody != null && handlerBody.trim().length() != 0) {
                    responseBody = handlerBody;
                }
            }
            
            /* create a new session if required */
            SessionHandler sessionHandler = resolved.handler.sessionHandler();
            if (sessionHandler != null) {
                createNewSession(request, response, resolved.route, sessionHandler);
            }
            
            /* set the response status code */
            int handlerStatusCode = resolved.handler.statusCode();
            if (handlerStatusCode == -1) {
                throw new RuntimeException("a response status code must be specified");
            }
            response.setCode(resolved.handler.statusCode());
            
            /* handle the response */
            if (resolved.handler.isAsync()) {
                doAsync(response, resolved.handler, responseContentType, responseBody);
            } else {
                sendAndCommitResponse(response, responseContentType, responseBody);
            }
            
        } catch (Throwable e) {
            
            logger.error("internal server error", e);
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            sendAndCommitResponse(response, "text/plain", "");
        }
    }
    
    /*----------------------------------------------------------*/
    
    private Session getSessionIfExists(Request request) {
        
        Cookie cookie = request.getCookie(SESSION_COOKIE_NAME);
        if (cookie != null) {
            String sessionId = cookie.getValue();
            return sessions.get(sessionId);
        }
        return null;
    }
    
    private void createNewSession(Request request, Response response, 
            Route route, SessionHandler sessionHandler) {
        
        Session session = new Session();
        sessionHandler.onCreate(request, route, session);
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, session);
        
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        response.setCookie(cookie);
    }
    
    private void doAsync(Response response, RequestHandler handler, 
            String responseContentType, String responseBody) {
        
        AsyncTask task = new AsyncTask(response, handler, 
                responseContentType, responseBody);
        asyncExecutor.execute(task);
    }
    
    private void sendAndCommitResponse(Response response, 
            String responseContentType, String responseBody) {
        
        try {
            
            PrintStream body = 
                    sendResponse(response, responseContentType, responseBody);
            body.close();
            
         } catch(Exception e) {
            throw new RuntimeException(e);
         }
    }

    private PrintStream sendResponse(Response response, 
            String responseContentType, String responseBody)
            throws IOException {
        
        PrintStream body = response.getPrintStream();
        addStandardHeaders(response, responseContentType);
        body.println(responseBody);
        body.flush();
        return body;
    }

    private void addStandardHeaders(Response response, String responseContentType) {
        
        long time = System.currentTimeMillis();
        response.setValue("Content-Type", responseContentType);
        response.setValue("Server", "HelloWorld/1.0 (Simple 5.1.4)");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
    }
    
    private ResolvedRequest resolve(Request request) {
        
        ResolvedRequest resolved = new ResolvedRequest();
        String method = request.getMethod();
        String path = request.getPath().getPath();
        ContentType requestContentType = request.getContentType();
        
        /* get the route and the handler for this request */
        Route route = routeMap.getRoute(path);
        if (route == null) {
            logger.error("could not find a route for " + path);
            resolved.errorStatus = Status.NOT_FOUND;
            return resolved;
        }
        String contentType = requestContentType != null ? 
                requestContentType.toString() : null;
        HandlerKey key = new HandlerKey(method, route, contentType);
        RequestHandler handler = handlerMap.get(key);
        if (handler == null) {
            logger.error("could not find a handler for " + 
                    method + " - " + path);
            resolved.errorStatus = Status.METHOD_NOT_ALLOWED;
            return resolved;
        }
        
        resolved.handler = handler;
        resolved.route = route;
        resolved.key = key;
        return resolved;
    }
    
    private class ResolvedRequest {
        
        public Route route;
        public RequestHandler handler;
        public HandlerKey key;
        public Status errorStatus;
    }
    
    private class AsyncTask implements Runnable {

        private Response response;
        private RequestHandler handler;
        private String responseContentType; 
        private String responseBody;
        
        public AsyncTask(Response response, 
                RequestHandler handler, 
                String responseContentType, String responseBody) {
            
            this.response = response;
            this.handler = handler;
            this.responseContentType = responseContentType;
            this.responseBody = responseBody;
        }

        public void run() {
            
            delayIfRequired(handler);
            
            if (handler.isSuspend()) {
                
                handleBroadcasts();
                
            } else {
            
                long period = handler.period();
                if (period > -1) {
                    respondPeriodically(period);
                } else {
                    sendAndCommitResponse(response, responseContentType, responseBody);
                }
            }
        }

        private void handleBroadcasts() {
            
            while(true) {
                try {
                    
                    Broadcast broadcast = broadcasts.take();
                    if (broadcast instanceof StopBroadcasting) {
                        response.getPrintStream().close();
                        break;
                    }
                    
                    delayIfRequired(handler);
                    
                    Request request = broadcast.getRequest();
                    Route route = broadcast.getRoute();
                    String path = broadcast.getPath();
                    
                    /* no support for session variables for now */
                    String handlerBody = handler.body(path, 
                            route.pathParameterElements(), null, request);
                    
                    sendResponse(response, responseContentType, handlerBody);
                    
                } catch (Exception e) {
                    logger.error("error waiting for, or handling, a broadcast", e);
                }
            }
        }
        
        private void delayIfRequired(RequestHandler handler) {
            
            long delay = handler.delay();
            if (delay > -1) {
                
                try {
                    
                    TimeUnit delayUnit = handler.delayUnit();
                    long delayInMillis = delayUnit.toMillis(delay);
                    Thread.sleep(delayInMillis);
                    
                } catch (Exception e) {
                    throw new RuntimeException("error delaying response", e);
                }
            }
        }

        private void respondPeriodically(long period) {
            
            TimeUnit periodUnit = handler.periodUnit();
            long periodInMillis = periodUnit.toMillis(period);
            final int times = handler.periodTimes();
            final Timer timer = new Timer("ServerFixtureTimer", true);
            timer.scheduleAtFixedRate(new TimerTask() {
                
                private int count = 0;
                
                @Override
                public void run() {
                    try {
                        
                        if (times > -1 && count >= times) {
                            timer.cancel();
                            timer.purge();
                            response.getPrintStream().close();
                            return;
                        }
                        
                        sendResponse(response, responseContentType, responseBody);
                        
                        count++;
                        
                    } catch (Exception e) {
                        logger.error("error sending async response at fixed rate", e);
                    }
                }
            }, 0, periodInMillis);
        }
    }
    
    private class Broadcast {
        
        private final Request request;
        private final Route route;
        private final String path;
        
        public Broadcast(Request request, Route route, String path) {
            this.request = request;
            this.route = route;
            this.path = path;
        }
        
        public Request getRequest() {
            return request;
        }
        
        public Route getRoute() {
            return route;
        }
        
        public String getPath() {
            return path;
        }
    }
    
    private class StopBroadcasting extends Broadcast {
        public StopBroadcasting() {
            super(null, null, null);
        }
    }
}

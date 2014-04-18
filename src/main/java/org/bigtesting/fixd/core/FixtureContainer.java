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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.capture.impl.SimpleCapturedRequest;
import org.bigtesting.fixd.request.impl.SimpleHttpRequest;
import org.bigtesting.fixd.routing.RegexRouteMap;
import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.routing.RouteMap;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
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
            new ConcurrentHashMap<HandlerKey, RequestHandler>();
    
    private final Set<HandlerKey> uponHandlers = 
            Collections.newSetFromMap(new ConcurrentHashMap<HandlerKey, Boolean>());
    
    private final RouteMap routeMap = new RegexRouteMap();
    
    /*
     * a ConcurrentHashMap, as opposed to synchronized map, is chosen here because,
     * from the perspective of a client, an up-to-date view of the map is not 
     * a requirement; i.e. a given client is not interested in sessions created
     * for other clients 
     */
    private final Map<String, Session> sessions = 
            new ConcurrentHashMap<String, Session>();
    
    private final Queue<CapturedRequest> capturedRequests = 
            new ConcurrentLinkedQueue<CapturedRequest>();
    
    private final List<Queue<Broadcast>> subscribers = 
            Collections.synchronizedList(new ArrayList<Queue<Broadcast>>());
    
    private final ExecutorService asyncExecutor;
    
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
    
    public void addUponHandler(Upon upon) {
       
        RequestHandler uponHandler = 
                new RequestHandler(this).with(200, "text/plain", "");
        HandlerKey uponKey = addHandler(uponHandler, upon.getMethod(), 
                upon.getResource(), upon.getContentType());
        uponHandlers.add(uponKey);
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
            ResponseBody responseBody = new StringResponseBody("");
            
            ResolvedRequest resolved = resolve(request);
            if (resolved.errorStatus != null) {
                response.setStatus(resolved.errorStatus);
                sendAndCommitResponse(response, responseContentType, responseBody);
                return;
            }
            
            if (requestIsForUponHandler(resolved)) {
                
                broadcastToSubscribers(request, resolved.route);
                /* continue handling the request, as it needs to 
                 * return a normal response */
            }
            
            /* create a new session if required */
            SessionHandler sessionHandler = resolved.handler.sessionHandler();
            if (sessionHandler != null) {
                createNewSession(request, response, resolved.route, sessionHandler);
            }
            
            /* set the response body */
            if (!resolved.handler.isSuspend()) {
                Session session = getSessionIfExists(request);
                ResponseBody handlerBody = resolved.handler.body(
                        new SimpleHttpRequest(request, session, resolved.route));
                if (handlerBody != null && handlerBody.hasContent()) {
                    responseBody = handlerBody;
                }
            }
            
            /* set the content type */
            String handlerContentType = resolved.handler.contentType();
            if (handlerContentType != null && 
                    handlerContentType.trim().length() != 0) {
                
                responseContentType = handlerContentType;
            }
            
            /* set the response status code */
            int handlerStatusCode = resolved.handler.statusCode();
            if (handlerStatusCode == -1) {
                throw new RuntimeException("a response status code must be specified");
            }
            response.setCode(resolved.handler.statusCode());
            
            /* set any headers */
            Set<SimpleImmutableEntry<String, String>> headers = 
                    resolved.handler.headers();
            for (SimpleImmutableEntry<String, String> header : headers) {
                response.addValue(header.getKey(), header.getValue());
            }
            
            /* handle the response */
            if (resolved.handler.isAsync()) {
                doAsync(response, resolved.handler, responseContentType, responseBody);
            } else {
                sendAndCommitResponse(response, responseContentType, responseBody);
            }
            
        } catch (Throwable e) {
            
            logger.error("internal server error", e);
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            sendAndCommitResponse(response, "text/plain", new StringResponseBody(""));
        }
    }

    /*----------------------------------------------------------*/
    
    private boolean requestIsForUponHandler(ResolvedRequest resolved) {
        
        return uponHandlers.contains(resolved.key);
    }
    
    private void broadcastToSubscribers(Request request, Route route) {
        
        synchronized (subscribers) {
            for (Queue<Broadcast> broadcasts : subscribers) {
                broadcasts.add(new Broadcast(request, route));
            }
        }
    }
    
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
        sessionHandler.onCreate(new SimpleHttpRequest(request, session, route));
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, session);
        
        Cookie cookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        response.setCookie(cookie);
    }
    
    private void doAsync(Response response, RequestHandler handler, 
            String responseContentType, ResponseBody responseBody) {
        
        AsyncTask task = new AsyncTask(response, handler, 
                responseContentType, responseBody);
        asyncExecutor.execute(task);
    }
    
    private void sendAndCommitResponse(Response response, 
            String responseContentType, ResponseBody responseBody) {
        
        responseBody.sendAndCommit(response, responseContentType);
    }

    private void sendResponse(Response response, 
            String responseContentType, ResponseBody responseBody) {
        
        responseBody.send(response, responseContentType);
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
        private ResponseBody responseBody;
        
        private final BlockingQueue<Broadcast> broadcasts = 
                new LinkedBlockingQueue<Broadcast>();
        
        private Timer broadcastSubscribeTimeoutTimer;
        
        public AsyncTask(Response response, 
                RequestHandler handler, 
                String responseContentType, ResponseBody responseBody) {
            
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
            
            subscribers.add(broadcasts);
            
            startTimeoutCountdownIfRequired();
            
            while(true) {
                try {
                    
                    Broadcast broadcast = broadcasts.take();
                    if (broadcast instanceof SubscribeTimeout) {
                        response.setStatus(Status.REQUEST_TIMEOUT);
                        response.getPrintStream().close();
                        break;
                    }
                    
                    restartTimeoutCountdownIfRequired();
                    
                    delayIfRequired(handler);
                    
                    Request request = broadcast.getRequest();
                    Route route = broadcast.getRoute();

                    /* no support for session variables for now */
                    ResponseBody handlerBody = handler.body(
                            new SimpleHttpRequest(request, null, route));
                    
                    sendResponse(response, responseContentType, handlerBody);
                    
                } catch (Exception e) {
                    logger.error("error waiting for, or handling, a broadcast", e);
                }
            }
            
            subscribers.remove(broadcasts);
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
        
        private void startTimeoutCountdownIfRequired() {
            
            if (handler.hasTimeout()) {
                broadcastSubscribeTimeoutTimer = new Timer("TimeoutTask", true);
                broadcastSubscribeTimeoutTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        broadcasts.add(new SubscribeTimeout());
                        broadcastSubscribeTimeoutTimer.cancel();
                        broadcastSubscribeTimeoutTimer.purge();
                    }
                }, handler.timeoutUnit().toMillis(handler.timeout()));
            }
        }
        
        private void restartTimeoutCountdownIfRequired() {
            
            if (broadcastSubscribeTimeoutTimer != null) {
                broadcastSubscribeTimeoutTimer.cancel();
                broadcastSubscribeTimeoutTimer.purge();
            }
            startTimeoutCountdownIfRequired();
        }
    }
    
    private class Broadcast {
        
        private final Request request;
        private final Route route;
        
        public Broadcast(Request request, Route route) {
            this.request = request;
            this.route = route;
        }
        
        public Request getRequest() {
            return request;
        }
        
        public Route getRoute() {
            return route;
        }
    }
    
    private class SubscribeTimeout extends Broadcast {
        public SubscribeTimeout() {
            super(null, null);
        }
    }
}

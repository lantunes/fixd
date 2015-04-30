/*
 * Copyright (C) 2015 BigTesting.org
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
package org.bigtesting.fixd.core.container;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;

import org.bigtesting.fixd.Method;
import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.capture.impl.SimpleCapturedRequest;
import org.bigtesting.fixd.core.RequestHandlerImpl;
import org.bigtesting.fixd.core.RequestMarshallerImpl;
import org.bigtesting.fixd.core.RequestUnmarshallerImpl;
import org.bigtesting.fixd.core.Upon;
import org.bigtesting.fixd.core.async.AsyncHandler;
import org.bigtesting.fixd.core.body.ResponseBody;
import org.bigtesting.fixd.core.body.StringResponseBody;
import org.bigtesting.fixd.request.impl.SimpleHttpRequest;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
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
    
    private final RequestResolver requestResolver = new RequestResolver();
    
    private final MarshallerContainer marshallerContainer = new MarshallerContainer();
    
    private final SessionManager sessionManager = new SessionManager();
    
    private final CapturedRequestContainer capturedRequestContainer = new CapturedRequestContainer();
    
    private final AsyncHandler asyncHandler;
    
    public FixtureContainer() {
        asyncHandler = new AsyncHandler(Executors.newCachedThreadPool());
    }
    
    public FixtureContainer(int aysncThreadPoolSize) {
        asyncHandler = new AsyncHandler(Executors.newFixedThreadPool(aysncThreadPoolSize));
    }
    
    public HandlerKey addHandler(RequestHandlerImpl handler, 
            Method method, String resource) {
        
        return addHandler(handler, method, resource, null);
    }
    
    public HandlerKey addHandler(RequestHandlerImpl handler, 
            Method method, String resource, String contentType) {
        return requestResolver.addHandler(handler, method, resource, contentType);
    }
    
    public void addUponHandler(Upon upon) {
        requestResolver.addUponHandler(this, upon);
    }
    
    public Queue<CapturedRequest> getCapturedRequests() {
        return capturedRequestContainer.getCapturedRequests();
    }
    
    public CapturedRequest nextCapturedRequest() {
        return capturedRequestContainer.nextCapturedRequest();
    }
    
    public void setCapturedRequestLimit(int limit) {
        capturedRequestContainer.setCapturedRequestLimit(limit);
    }
    
    public void addContentMarshaller(String contentType, RequestMarshallerImpl marshaller) {
        marshallerContainer.addContentMarshaller(contentType, marshaller);
    }
    
    public void addContentUnmarshaller(String contentType, RequestUnmarshallerImpl unmarshaller) {
        marshallerContainer.addContentUnmarshaller(contentType, unmarshaller);
    }
    
    public void handle(Request request, Response response) {

        try {
            
            SimpleCapturedRequest captured = new SimpleCapturedRequest(request);
            capturedRequestContainer.addCapturedRequest(captured);
            
            String responseContentType = "text/plain";
            ResponseBody responseBody = new StringResponseBody("");
            int handlerStatusCode = Status.OK.code;
            
            ResolvedRequest resolved = requestResolver.resolve(request);
            if (resolved.errorStatus != null) {
                response.setStatus(resolved.errorStatus);
                sendAndCommitResponse(response, responseContentType, responseBody);
                return;
            }
            
            if (requestResolver.requestIsForUponHandler(resolved)) {
                
                asyncHandler.broadcastToSubscribers(request, resolved.route, 
                        requestResolver.getUpon(resolved), captured);
                /* continue handling the request, as it needs to 
                 * return a normal response */
            }
            
            /* create a new session if required */
            SessionHandler sessionHandler = resolved.handler.sessionHandler();
            if (sessionHandler != null) {
                sessionManager.createNewSession(request, response, resolved.route, sessionHandler, 
                        marshallerContainer.newUnmarshallerProvider());
            }
            
            /* set the response body */
            if (!resolved.handler.isSuspend()) {
                Session session = sessionManager.getSessionIfExists(request);
                ResponseBody handlerBody = resolved.handler.body(
                        new SimpleHttpRequest(request, session, resolved.route, 
                                marshallerContainer.newUnmarshallerProvider()), 
                        response, marshallerContainer.newMarshallerProvider());
                if (handlerBody != null && handlerBody.hasContent()) {
                    responseBody = handlerBody;
                }
                handlerStatusCode = resolved.handler.statusCode();
            }
            
            /* set the content type */
            String handlerContentType = resolved.handler.contentType();
            if (handlerContentType != null && 
                    handlerContentType.trim().length() != 0) {
                
                responseContentType = handlerContentType;
            }
            
            /* set the response status code */
            if (handlerStatusCode == -1) {
                throw new RuntimeException("a response status code must be specified");
            }
            response.setCode(handlerStatusCode);
            
            /* set any headers */
            Set<SimpleImmutableEntry<String, String>> headers = 
                    resolved.handler.headers();
            for (SimpleImmutableEntry<String, String> header : headers) {
                response.addValue(header.getKey(), header.getValue());
            }
            
            /* handle the response */
            if (resolved.handler.isAsync()) {
                asyncHandler.doAsync(request, response, resolved.handler, responseContentType, responseBody, 
                        marshallerContainer.newMarshallerProvider(), 
                        marshallerContainer.newUnmarshallerProvider());
            } else {
                sendAndCommitResponse(response, responseContentType, responseBody);
            }
            
        } catch (Throwable e) {
            
            logger.error("internal server error", e);
            response.setStatus(Status.INTERNAL_SERVER_ERROR);
            sendAndCommitResponse(response, "text/plain", new StringResponseBody(""));
        }
    }
    
    public void stop() {
        
        asyncHandler.stop();
    }

    private void sendAndCommitResponse(Response response, 
            String responseContentType, ResponseBody responseBody) {
        responseBody.sendAndCommit(response, responseContentType);
    }
}

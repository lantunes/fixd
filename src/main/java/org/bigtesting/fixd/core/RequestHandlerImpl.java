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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.Method;
import org.bigtesting.fixd.RequestHandler;
import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.request.HttpRequestHandler;
import org.bigtesting.fixd.response.impl.SimpleHttpResponse;
import org.bigtesting.fixd.session.SessionHandler;
import org.simpleframework.http.Response;

/**
 * 
 * @author Luis Antunes
 */
public class RequestHandlerImpl implements RequestHandler {

    private int statusCode = -1;
    private String contentType;
    private String body;
    private SessionHandler sessionHandler;
    private long delay = -1;
    private TimeUnit delayUnit;
    private long period = -1;
    private TimeUnit periodUnit;
    private int periodTimes = -1;
    private long timeout = -1;
    private TimeUnit timeoutUnit;
    private Upon upon;
    private Set<SimpleImmutableEntry<String, String>> headers = 
            new HashSet<SimpleImmutableEntry<String,String>>();
    
    private HttpRequestHandler httpHandler;
    
    private final FixtureContainer container;
    
    public RequestHandlerImpl(FixtureContainer container) {
        this.container = container;
    }
    
    public RequestHandler with(int statusCode, String contentType, String body) {
        
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
        return this;
    }
    
    public RequestHandler with(int statusCode, String contentType, Object entity) {
        
        this.statusCode = statusCode;
        this.contentType = contentType;
        //TODO implement content marshalling
        return this;
    }
    
    public RequestHandler with(HttpRequestHandler customHandler) {
        
        this.httpHandler = customHandler;
        return this;
    }

    public RequestHandler withSessionHandler(SessionHandler sessionHandler) {
        
        this.sessionHandler = sessionHandler;
        return this;
    }
    
    public RequestHandler withHeader(String name, String value) {
        
        headers.add(new SimpleImmutableEntry<String, String>(name, value));
        return this;
    }
    
    public RequestHandler after(long delay, TimeUnit delayUnit) {
        
        this.delay = delay;
        this.delayUnit = delayUnit;
        return this;
    }
    
    public RequestHandler every(long period, TimeUnit periodUnit) {
        
        this.period = period;
        this.periodUnit = periodUnit;
        return this;
    }
    
    public RequestHandler every(long period, TimeUnit periodUnit, int times) {
        
        this.periodTimes = times;
        return every(period, periodUnit);
    }
    
    public RequestHandler withTimeout(long timeout, TimeUnit timeoutUnit) {
        
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        return this;
    }
    
    public RequestHandler upon(Method method, String resource) {
        
        this.upon = new Upon(method, resource);
        container.addUponHandler(this.upon);
        return this;
    }
    
    public RequestHandler upon(Method method, String resource, String contentType) {
        
        this.upon = new Upon(method, resource, contentType);
        container.addUponHandler(this.upon);
        return this;
    }
    
    public RequestHandler withRedirect(String location) {
        //TODO implement convenient redirects
        return this;
    }
    
    public RequestHandler withRedirect(String location, int statusCode) {
        //TODO implement convenient redirects
        return this;
    }
    
    /*-----------------------------------------*/
    
    public int statusCode() {
        return statusCode;
    }

    public String contentType() {
        return contentType;
    }

    public ResponseBody body(HttpRequest request, Response resp) {
        
        if (httpHandler != null) {
            
            SimpleHttpResponse response = new SimpleHttpResponse(request, resp);
            httpHandler.handle(request, response);
            this.contentType = response.getContentType();
            this.statusCode = response.getStatusCode();
            return response.getBody();
        }
        
        return new InterpolatedResponseBody(body, request);
    }
    
    public SessionHandler sessionHandler() {
        return sessionHandler;
    }
    
    public boolean isAsync() {
        return delay > -1 || period > -1 || isSuspend();
    }
    
    public long delay() {
        return delay;
    }
    
    public TimeUnit delayUnit() {
        return delayUnit;
    }
    
    public long period() {
        return period;
    }
    
    public TimeUnit periodUnit() {
        return periodUnit;
    }
    
    public int periodTimes() {
        return periodTimes;
    }
    
    public boolean hasTimeout() {
        return timeout != -1;
    }
    
    public long timeout() {
        return timeout;
    }
    
    public TimeUnit timeoutUnit() {
        return timeoutUnit;
    }
    
    public Upon upon() {
        return upon;
    }
    
    public boolean isSuspend() {
        return upon != null;
    }
    
    public Set<SimpleImmutableEntry<String, String>> headers() {
        return new HashSet<SimpleImmutableEntry<String, String>>(headers);
    }
    
    public HttpRequestHandler customHandler() {
        return httpHandler;
    }
}

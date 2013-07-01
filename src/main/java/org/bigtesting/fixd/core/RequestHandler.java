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

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.routing.Route.PathParameterElement;
import org.bigtesting.fixd.util.interpreter.ResponseBodyInterpreter;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class RequestHandler {

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
    
    public RequestHandler with(int statusCode, String contentType, String body) {
        
        this.statusCode = statusCode;
        this.contentType = contentType;
        this.body = body;
        return this;
    }

    public RequestHandler withNewSession(SessionHandler sessionHandler) {
        
        this.sessionHandler = sessionHandler;
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
        return this;
    }
    
    public RequestHandler upon(Method method, String resource, String contentType) {
        
        this.upon = new Upon(method, resource, contentType);
        return this;
    }
    
    /*-----------------------------------------*/
    
    int statusCode() {
        return statusCode;
    }

    String contentType() {
        return contentType;
    }

    String body(String path, List<PathParameterElement> pathParams, 
            Session session, Request request) {
        
        return ResponseBodyInterpreter.interpret(body, path, 
                pathParams, session, request);
    }
    
    SessionHandler sessionHandler() {
        return sessionHandler;
    }
    
    boolean isAsync() {
        return delay > -1 || period > -1 || isSuspend();
    }
    
    long delay() {
        return delay;
    }
    
    TimeUnit delayUnit() {
        return delayUnit;
    }
    
    long period() {
        return period;
    }
    
    TimeUnit periodUnit() {
        return periodUnit;
    }
    
    int periodTimes() {
        return periodTimes;
    }
    
    long timeout() {
        return timeout;
    }
    
    TimeUnit timeoutUnit() {
        return timeoutUnit;
    }
    
    Upon upon() {
        return upon;
    }
    
    boolean isSuspend() {
        return upon != null;
    }
}

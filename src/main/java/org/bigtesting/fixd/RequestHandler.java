/*
 * Copyright (C) 2011-2013 Bigtesting.org
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
package org.bigtesting.fixd;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigtesting.fixd.routing.RouteHelper;
import org.bigtesting.fixd.routing.Route.PathParameterElement;

/**
 * 
 * @author Luis Antunes
 */
public class RequestHandler {

    private static final Pattern SESSION_VALUE_PATTERN = Pattern.compile("\\{([^}]*)\\}");
    
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

    String body(String path, List<PathParameterElement> pathParams, Session session) {
        
        /* handle any values that start with ':' */
        String responseBody = body;
        String[] pathTokens = RouteHelper.getPathElements(path);
        for (PathParameterElement param : pathParams) {
            responseBody = responseBody.replaceAll(":" + param.name(), pathTokens[param.index()]);
        }
        
        if (session != null) {
            //TODO this should be moved into its own class with tests
            /* 
             * handle any values that are enclosed in '{}'
             * - replacement values can consist of "{}"
             */
            Matcher m = SESSION_VALUE_PATTERN.matcher(responseBody);
            StringBuilder result = new StringBuilder();
            int start = 0;
            while (m.find()) {
                String key = m.group(1);
                Object val = session.get(key);
                if (val != null) {
                    String stringVal = val.toString();
                    result.append(responseBody.substring(start, m.start()));
                    result.append(stringVal);
                    start = m.end();
                }
            }
            result.append(responseBody.substring(start));
            responseBody = result.toString();
        }
        
        return responseBody;
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
